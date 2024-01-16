package xyz.korsak.pcoapi.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.GameException;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerActions;
import xyz.korsak.pcoapi.responses.GetGameResponse;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;
import xyz.korsak.pcoapi.rules.PokerRules;

import java.util.List;

@Slf4j
@Service
public class GameService extends BaseService {
    private final Authorization auth;
    private final RoomRepository roomRepository;

    public void pushData(String roomId) {
        notifySubscribers(getGameResponse(roomId), roomId);
    }

    public GameService(Authorization authorization, RoomRepository roomRepository) {
        this.auth = authorization;
        this.roomRepository = roomRepository;
    }

    public SseEmitter streamGame(String roomId) {
        return newEmitter(roomId);
    }

    public GetGameResponse getGameResponse(String roomId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        List<Player> p = room.getPlayers();

        if (p.isEmpty()) {
            throw new NotFoundException("No players found");
        }

        return new GetGameResponse(game, room.getPlayers());
    }

    public void start(String roomId, String roomToken) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        room.setGame(new Game(GameState.IN_PROGRESS, 0, room.getPlayers().size()));

        room.getPlayers().forEach(player -> player.setChips(room.getGame().getRules().getStartingChips()));

        // Game starts with mandatory bets - the small and big blinds.
        smallBlind(room);
        bigBlind(room);
        roomRepository.create(room);
    }

    public void setRules(String roomId, String roomToken, PokerRules rules) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        room.getGame().setRules(rules);
        roomRepository.create(room);
    }

    public Player getCurrentPlayer(Room room, int currentTurnIndex) {
        if (currentTurnIndex < 0 || currentTurnIndex >= room.getPlayers().size()) {
            throw new IllegalStateException("Invalid turn index");
        }
        return room.getPlayers().get(currentTurnIndex);
    }

    public Room getRoomWithCurrentPlayerToken(String roomId, String playerToken) {
        Room room = roomRepository.findById(roomId);
        Game game = room.getGame();
        String playerId = getCurrentPlayer(room, game.getCurrentTurnIndex()).getId();

        if (auth.playerIsNotAuthorized(roomId, playerId, playerToken)) {
            throw new UnauthorizedAccessException();
        }
        if (game.getState() != GameState.IN_PROGRESS || game.getCurrentTurnIndex() < 0) {
            throw new GameException("Invalid game state");
        }
        return room;
    }

    public void decideWinner(String roomId, String winnerId, String roomToken) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);
        Player winner = auth.getPlayerWithAuthorization(roomId, winnerId, roomToken);
        endHandWithWinner(winner, room);

        roomRepository.create(room);
    }

    public void fold(String roomId, String playerToken) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(r, (room, game, player) -> {
            player.setActive(false);
            return game.toBuilder();
        });
        roomRepository.create(updatedRoom);
    }

    public void call(String roomId, String playerToken) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(r, (room, game, player) -> {
            int leftToCall = room.getGame().getCurrentBetSize() - player.getStakedChips();
            player.setChips(player.getChips() - leftToCall);
            player.addToStake(leftToCall);
            return game.toBuilder().addToStake(leftToCall);
        });
        roomRepository.create(updatedRoom);
    }

    public void bet(String roomId, String playerToken, int betSize) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(r, (room, game, player) -> {
            if (game.getCurrentBetSize() != 0) {
                throw new GameException("Current bet size is nonzero");
            }

            if (betSize < game.getRules().getBigBlind()) {
                throw new GameException("Cannot bet lower than a big blind");
            }

            int newPlayerBalance = player.getChips() - betSize;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            player.addToStake(betSize);
            return game.toBuilder().addToStake(betSize)
                        .currentBetSize(betSize);
        });
        roomRepository.create(updatedRoom);
    }

    public void check(String roomId, String playerToken) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(r, (room, game, player) -> {
            if (game.getCurrentBetSize() > player.getStakedChips()) {
                throw new GameException("Cannot check");
            }
            return game.toBuilder();
        });
        roomRepository.create(updatedRoom);
    }

    public void raise(String roomId, String playerToken, int betSize) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(r, (room, game, player) -> {
            if (game.getCurrentBetSize() == 0) {
                throw new GameException("Current bet size is zero");
            }

            if (betSize - game.getCurrentBetSize() <= 0 || betSize < 2 * game.getRules().getBigBlind()) {
                throw new GameException("The bet size is too low");
            }

            final int betAddition = betSize - player.getStakedChips();

            final int newPlayerBalance = player.getChips() - betAddition;

            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            player.addToStake(betAddition);
            return game.toBuilder().addToStake(betAddition)
                        .currentBetSize(betSize);
        });
        roomRepository.create(updatedRoom);
    }

    public void smallBlind(Room r) {
        blind(r, true);
    }
    public void bigBlind(Room r) {
        blind(r, false);
    }

    public void blind(Room r, boolean isSmall) {
        final Room updatedRoom = performAction(r, (room, game, player) -> {
            int b = isSmall ? game.getRules().getSmallBlind() : game.getRules().getBigBlind();

            int newPlayerBalance = player.getChips() - b;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            player.addToStake(b);
            return game.toBuilder().addToStake(b)
                    .currentBetSize(b)
                    .decrementActionsTakenThisRound();
        });
        roomRepository.create(updatedRoom);
    }

    @FunctionalInterface
    private interface Action {
        Game.GameBuilder execute(Room room, Game game, Player player);
    }

    /***
     * There are two scenarios in which a betting round with blinds can end:
     * - One player is left -> he is the winner
     * - All players matched the highest bet (game.getCurrentBetSize())
     * NOTE: If we play without blinds, another rule has to be implemented - if currentBetSize starts with 0,
     * then automatically the round ends after first player's action.
     */
    private Room performAction(Room room, Action action) {
        final Game game = room.getGame();

        if (game.getStage() == GameStage.SHOWDOWN) {
            return room;
        }

        final Player player = getCurrentPlayer(room, game.getCurrentTurnIndex());
        Game.GameBuilder builder = action.execute(room, game, player);
        List<Player> players = room.getPlayers();

        final Game newGame;
        final List<Player> activePlayers = players.stream().filter(Player::isActive).toList();
        // First case - one player left
        if (activePlayers.size() == 1) {
            newGame = endHandWithWinner(activePlayers.getFirst(), room);
        }
        // Second case - all players matched the highest bet, and everybody played at least once
        else if (areAllPlayersMatchingBetSize(players, game.getCurrentBetSize())
                && game.getActionsTakenThisRound() + 1 >= players.size()) {
            newGame = builder.stage(game.getStage().next())
                    .currentBetSize(0)
                    .currentTurnIndex(game.getSmallBlindIndex())
                    .actionsTakenThisRound(0).build();
            players.forEach(p -> p.setStakedChips(0));
        } // The round doesn't end
        else {
            newGame = builder.nextTurnIndex()
                    .incrementActionsTakenThisRound().build();
        }
        players.forEach(
                p -> p.setActions(PlayerActions.createActionsBasedOnBet(game.getCurrentBetSize(), p.getStakedChips())));
        room.setGame(newGame);
        room.setPlayers(players);

        return room;
    }

    private Game endHandWithWinner(Player winner, Room room) {
        Game game = room.getGame();
        Game.GameBuilder builder = game.toBuilder();
        List<Player> players = room.getPlayers();
        players.forEach(p -> {
            p.setStakedChips(0);
            if (p.getId().equals(winner.getId())) {
                p.setChips(winner.getChips() + game.getStakedChips());
            }
            p.setActive(true);
        });

        final Game newGame = builder.stage(GameStage.PRE_FLOP)
                .stakedChips(0)
                .currentBetSize(0)
                .dealerIndex(game.getDealerIndex() + 1)
                .currentTurnIndex(game.getSmallBlindIndex())
                .build();

        room.setPlayers(players);
        room.setGame(newGame);
        smallBlind(room);
        bigBlind(room);
        return newGame;
    }

    private boolean areAllPlayersMatchingBetSize(List<Player> players, int betSize) {
        return players.stream()
                .filter(Player::isActive)
                .allMatch(player -> player.getStakedChips() == betSize);
    }
}
