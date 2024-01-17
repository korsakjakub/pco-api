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
        Game game = room.game();
        List<Player> p = room.players();

        if (p.isEmpty()) {
            throw new NotFoundException("No players found");
        }

        return new GetGameResponse(game, room.players());
    }

    public void start(String roomId, String roomToken) {
        final Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        room.players().forEach(player -> player.setChips(room.game().rules().getStartingChips()));

        final Game updatedGame = new Game.GameBuilder(GameState.IN_PROGRESS, 0, room.players().size()).build();
        Room updatedRoom = room.toBuilder()
                .game(updatedGame).build();

        // Game starts with mandatory bets - the small and big blinds.
        smallBlind(updatedRoom);
        bigBlind(updatedRoom);
        roomRepository.create(updatedRoom);
    }

    public void setRules(String roomId, String roomToken, PokerRules rules) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        roomRepository.create(room.toBuilder().game(room.game().toBuilder().rules(rules).build()).build());
    }

    public Player getCurrentPlayer(Room room, int currentTurnIndex) {
        if (currentTurnIndex < 0 || currentTurnIndex >= room.players().size()) {
            throw new IllegalStateException("Invalid turn index");
        }
        return room.players().get(currentTurnIndex);
    }

    public Room getRoomWithCurrentPlayerToken(String roomId, String playerToken) {
        Room room = roomRepository.findById(roomId);
        Game game = room.game();
        String playerId = getCurrentPlayer(room, game.currentTurnIndex()).getId();

        if (auth.playerIsNotAuthorized(roomId, playerId, playerToken)) {
            throw new UnauthorizedAccessException();
        }
        if (game.state() != GameState.IN_PROGRESS || game.currentTurnIndex() < 0) {
            throw new GameException("Invalid game state");
        }
        return room;
    }

    public void decideWinner(String roomId, String winnerId, String roomToken) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);
        Player winner = auth.getPlayerWithAuthorization(roomId, winnerId, roomToken);
        Game game = room.game();
        Game.GameBuilder builder = game.toBuilder();
        List<Player> players = room.players();

        endHandWithWinner(winner, builder, game, players);

        roomRepository.create(
                room.toBuilder()
                        .game(builder.build())
                        .players(players)
                        .build()
        );
    }

    public void fold(String roomId, String playerToken) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(room, (game, currentPlayer) -> {
            currentPlayer.setActive(false);
            return game.toBuilder();
        }).build();
        roomRepository.create(updatedRoom);
    }

    public void call(String roomId, String playerToken) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(room, (game, currentPlayer) -> {
            int leftToCall = game.currentBetSize() - currentPlayer.getStakedChips();
            currentPlayer.setChips(currentPlayer.getChips() - leftToCall);
            currentPlayer.addToStake(leftToCall);
            return game.toBuilder().addToStake(leftToCall);
        }).build();
        roomRepository.create(updatedRoom);
    }

    public void bet(String roomId, String playerToken, int betSize) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() != 0) {
                throw new GameException("Current bet size is nonzero");
            }

            if (betSize < game.rules().getBigBlind()) {
                throw new GameException("Cannot bet lower than a big blind");
            }

            int newPlayerBalance = currentPlayer.getChips() - betSize;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + currentPlayer.getId());
            }

            currentPlayer.setChips(newPlayerBalance);
            currentPlayer.addToStake(betSize);
            return game.toBuilder().addToStake(betSize)
                    .currentBetSize(betSize);
        }).build();
        roomRepository.create(updatedRoom);
    }

    public void check(String roomId, String playerToken) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() > currentPlayer.getStakedChips()) {
                throw new GameException("Cannot check");
            }
            return game.toBuilder();
        }).build();
        roomRepository.create(updatedRoom);
    }

    public void raise(String roomId, String playerToken, int betSize) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        final Room updatedRoom = performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() == 0) {
                throw new GameException("Current bet size is zero");
            }

            if (betSize - game.currentBetSize() <= 0 || betSize < 2 * game.rules().getBigBlind()) {
                throw new GameException("The bet size is too low");
            }

            final int betAddition = betSize - currentPlayer.getStakedChips();

            final int newPlayerBalance = currentPlayer.getChips() - betAddition;

            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + currentPlayer.getId());
            }

            currentPlayer.setChips(newPlayerBalance);
            currentPlayer.addToStake(betAddition);
            return game.toBuilder().addToStake(betAddition)
                    .currentBetSize(betSize);
        }).build();
        roomRepository.create(updatedRoom);
    }

    public void smallBlind(Room r) {
        blind(r, true);
    }
    public void bigBlind(Room r) {
        blind(r, false);
    }

    public void blind(Room room, boolean isSmall) {
        final Room updatedRoom = performAction(room, (game, currentPlayer) -> {
            int b = isSmall ? game.rules().getSmallBlind() : game.rules().getBigBlind();

            int newPlayerBalance = currentPlayer.getChips() - b;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + currentPlayer.getId());
            }

            currentPlayer.setChips(newPlayerBalance);
            currentPlayer.addToStake(b);
            return game.toBuilder().addToStake(b)
                    .currentBetSize(b)
                    .decrementActionsTakenThisRound();
        }).build();
        roomRepository.create(updatedRoom);
    }

    @FunctionalInterface
    private interface Action {
        Game.GameBuilder execute(Game game, Player currentPlayer);
    }

    /***
     * There are two scenarios in which a betting round with blinds can end:
     * - One player is left -> he is the winner
     * - All players matched the highest bet (game.getCurrentBetSize())
     * NOTE: If we play without blinds, another rule has to be implemented - if currentBetSize starts with 0,
     * then automatically the round ends after first player's action.
     */
    private Room.RoomBuilder performAction(Room room, Action action) {
        final Game game = room.game();

        if (game.stage() == GameStage.SHOWDOWN) {
            return room.toBuilder();
        }

        final Player currentPlayer = getCurrentPlayer(room, game.currentTurnIndex());
        List<Player> players = room.players();
        Game.GameBuilder builder = action.execute(game, currentPlayer);

        final List<Player> activePlayers = players.stream().filter(Player::isActive).toList();
        // First case - one player left
        if (activePlayers.size() == 1) {
            endHandWithWinner(activePlayers.getFirst(), builder, game, players);
        }
        // Second case - all players matched the highest bet, and everybody played at least once
        else if (areAllPlayersMatchingBetSize(players, game.currentBetSize())
                && game.actionsTakenThisRound() + 1 >= players.size()) {
            builder.stage(game.stage().next())
                    .currentBetSize(0)
                    .currentTurnIndex(game.smallBlindIndex())
                    .actionsTakenThisRound(0);
            players.forEach(p -> p.setStakedChips(0));
        } // The round doesn't end
        else {
            builder.nextTurnIndex()
                    .incrementActionsTakenThisRound();
        }
        players.forEach(
                p -> p.setActions(PlayerActions.createActionsBasedOnBet(game.currentBetSize(), p.getStakedChips())));

        return room.toBuilder().game(builder.build()).players(players);
    }

    private void endHandWithWinner(Player winner, Game.GameBuilder builder, Game game, List<Player> players) {
        players.forEach(p -> {
            p.setStakedChips(0);
            if (p.getId().equals(winner.getId())) {
                p.setChips(winner.getChips() + game.stakedChips());
            }
            p.setActive(true);
        });
        builder.stage(GameStage.PRE_FLOP)
                .stakedChips(0)
                .currentBetSize(0)
                .dealerIndex(game.dealerIndex() + 1)
                .currentTurnIndex(game.smallBlindIndex())
                .build();
    }

    private boolean areAllPlayersMatchingBetSize(List<Player> players, int betSize) {
        return players.stream()
                .filter(Player::isActive)
                .allMatch(player -> player.getStakedChips() == betSize);
    }
}
