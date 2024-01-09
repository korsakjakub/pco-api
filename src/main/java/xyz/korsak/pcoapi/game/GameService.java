package xyz.korsak.pcoapi.game;

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
import java.util.Optional;

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

        room.setGame(new Game(GameState.IN_PROGRESS, 1));

        room.getPlayers().forEach(player -> player.setChips(room.getGame().getRules().getStartingChips()));

        // Game starts with mandatory bets - the small and big blinds.
        smallBlind(room);
        bigBlind(room);
        roomRepository.create(room);
    }

    public void setRules(String roomId, String roomToken, PokerRules rules) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        Game game = room.getGame();
        game.setRules(rules);
        roomRepository.create(room);
    }

    public Player getCurrentPlayer(Room room, int currentTurnIndex) {
        if (currentTurnIndex < 0 || currentTurnIndex >= room.getPlayers().size()) {
            throw new IllegalStateException("Invalid turn index");
        }
        Player player = room.getPlayers().get(currentTurnIndex);
        if (player.getChips() < room.getGame().getCurrentBetSize()) {
            throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
        }
        return player;
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
        performAction(r, (room, game, player) -> player.setActive(false));
    }

    public void call(String roomId, String playerToken) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        performAction(r, (room, game, player) -> {
            int leftToCall = game.getCurrentBetSize() - player.getStakedChips();
            player.setChips(player.getChips() - leftToCall);
            player.addToStake(leftToCall);
            game.addToStake(leftToCall);
        });
    }

    public void bet(String roomId, String playerToken, int betSize) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        performAction(r, (room, game, player) -> {
            if (game.getCurrentBetSize() != 0) {
                throw new GameException("Current bet size is nonzero");
            }

            int newPlayerBalance = player.getChips() - betSize;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            player.addToStake(betSize);
            game.addToStake(betSize);
            game.setCurrentBetSize(betSize);
        });
    }

    public void check(String roomId, String playerToken) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        performAction(r, (room, game, player) -> {
            if (game.getCurrentBetSize() > player.getStakedChips()) {
                throw new GameException("Cannot check");
            }
        });
    }

    public void raise(String roomId, String playerToken, int betSize) {
        Room r = getRoomWithCurrentPlayerToken(roomId, playerToken);
        performAction(r, (room, game, player) -> {
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
            game.addToStake(betAddition);
            player.addToStake(betAddition);
            game.setCurrentBetSize(betSize);
        });
    }

    public void smallBlind(Room r) {
        performAction(r, (room, game, player) -> {
            int sb = game.getRules().getSmallBlind();

            int newPlayerBalance = player.getChips() - sb;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            player.addToStake(sb);
            game.addToStake(sb);
            game.setCurrentBetSize(sb);
            game.decrementActionsTakenThisRound();
        });
    }

    public void bigBlind(Room r) {
        performAction(r, (room, game, player) -> {
            int bb = game.getRules().getBigBlind();

            int newPlayerBalance = player.getChips() - bb;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            player.addToStake(bb);
            game.addToStake(bb);
            game.setCurrentBetSize(bb);
            game.decrementActionsTakenThisRound();
        });
    }

    @FunctionalInterface
    private interface Action {
        void execute(Room room, Game game, Player player);
    }

    /***
     * There are two scenarios in which a betting round with blinds can end:
     * - One player is left -> he is the winner
     * - All players matched the highest bet (game.getCurrentBetSize())
     * NOTE: If we play without blinds, another rule has to be implemented - if currentBetSize starts with 0,
     * then automatically the round ends after first player's action.
     */
    private void performAction(Room room, Action action) {
        Game game = room.getGame();

        if (game.getStage() == GameStage.SHOWDOWN) {
            return;
        }

        Player player = getCurrentPlayer(room, game.getCurrentTurnIndex());
        action.execute(room, game, player);
        List<Player> players = room.getPlayers();

        // First case - one player left
        if (activePlayersCount(players) == 1) {
            Optional<Player> lastPlayer = players.stream().filter(Player::isActive).findFirst();
            if (lastPlayer.isPresent()) {
                Player winner = lastPlayer.get();
                endHandWithWinner(winner, room);
            }
        }
        // Second case - all players matched the highest bet, and everybody played at least once
        else if (areAllPlayersMatchingBetSize(players, game.getCurrentBetSize())
                && game.getActionsTakenThisRound() + 1 >= players.size()) {
            game.nextStage();
            game.setCurrentBetSize(0);
            game.setCurrentTurnIndex(game.firstToPlayIndex(players.size()));
            game.setActionsTakenThisRound(0);
            players.forEach(p -> p.setStakedChips(0));
        } // The round doesn't end
        else {
            game.nextTurnIndex(players.size());
            game.incrementActionsTakenThisRound();
        }
        players.forEach(
                p -> p.setActions(PlayerActions.createActionsBasedOnBet(game.getCurrentBetSize(), p.getStakedChips())));
        room.setGame(game);
        room.setPlayers(players);

        roomRepository.create(room);
    }

    private void endHandWithWinner(Player winner, Room room) {
        Game game = room.getGame();
        List<Player> players = room.getPlayers();
        players.forEach(p -> {
            p.setStakedChips(0);
            if (p.getId().equals(winner.getId())) {
                p.setChips(winner.getChips() + game.getStakedChips());
            }
            p.setActive(true);
        });
        game.setStage(GameStage.PRE_FLOP);
        game.setCurrentBetSize(0);
        game.setStakedChips(0);
        game.setCurrentTurnIndex(game.firstToPlayIndex(players.size()));

        room.setPlayers(players);
        room.setGame(game);
        smallBlind(room);
        bigBlind(room);
    }

    private int activePlayersCount(List<Player> players) {
        return (int) players.stream().filter(Player::isActive).count();
    }

    private boolean areAllPlayersMatchingBetSize(List<Player> players, int betSize) {
        return players.stream()
                .filter(Player::isActive)
                .allMatch(player -> player.getStakedChips() == betSize);
    }
}
