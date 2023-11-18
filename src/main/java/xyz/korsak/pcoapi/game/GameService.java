package xyz.korsak.pcoapi.game;

import org.springframework.stereotype.Service;

import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.GameException;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;
import xyz.korsak.pcoapi.rules.PokerRules;


@Service
public class GameService {
    private final Authorization auth;
    private final RoomRepository roomRepository;
    public GameService(Authorization authorization, RoomRepository roomRepository) {
        this.auth = authorization;
        this.roomRepository = roomRepository;
    }

    public void start(String roomId, String roomToken) {
        if (!auth.authorizeRoomOwner(roomId, roomToken)) {
            throw new UnauthorizedAccessException();
        }
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        room.setGame(new Game(GameState.IN_PROGRESS, 0));

        room.getPlayers().forEach(player -> {
            player.setChips(room.getGame().getRules().getStartingChips());
        });
        roomRepository.create(room);
    }

    public void setRules(String roomId, String roomToken, PokerRules rules) {
        if (!auth.authorizeRoomOwner(roomId, roomToken)) {
            throw new UnauthorizedAccessException();
        }
        Room room = roomRepository.findById(roomId);
        Game game = room.getGame();
        game.setRules(rules);
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

        if (!auth.authorizePlayer(roomId, playerId, playerToken)) {
            throw new UnauthorizedAccessException();
        }
        if (game.getState() != GameState.IN_PROGRESS || game.getCurrentTurnIndex() < 0) {
            throw new GameException("Invalid game state");
        }
        return room;
    }

    public Room getRoomWithAuthorization(String roomId, String playerId, String playerToken) {
        if (!auth.authorizePlayer(roomId, playerId, playerToken)) {
            throw new UnauthorizedAccessException();
        }
        Room room = roomRepository.findById(roomId);
        Game game = room.getGame();
        int turnIndex = game.getCurrentTurnIndex();

        if (game.getState() != GameState.IN_PROGRESS || turnIndex < 0) {
            throw new GameException("Invalid game state");
        }
        return room;
    }

    @FunctionalInterface
    private interface Action {
        void execute(Room room, Game game, int turnIndex, Player player);
    }

    private void performAction(String roomId, String playerToken, Action action) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        Game game = room.getGame();
        int turnIndex = game.getCurrentTurnIndex();
        Player player = getCurrentPlayer(room, turnIndex);

        action.execute(room, game, turnIndex, player);

        game.setCurrentTurnIndex((turnIndex + 1) % room.getPlayers().size());

        room.setGame(game);
        roomRepository.create(room);
    }

    public void call(String roomId, String playerToken) {
        performAction(roomId, playerToken, (room, game, turnIndex, player) -> {
            player.setChips(player.getChips() - game.getCurrentBetSize());
            player.addToStake(game.getCurrentBetSize());
            game.addToStake(game.getCurrentBetSize());
        });
    }

    public void bet(String roomId, String playerToken, int betSize) {
        performAction(roomId, playerToken, (room, game, turnIndex, player) -> {
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
        performAction(roomId, playerToken, (room, game, turnIndex, player) -> {
            if (game.getCurrentBetSize() != 0) {
                throw new GameException("Current bet size is nonzero");
            }
        });
    }

    public void raise(String roomId, String playerToken, int betSize) {
        performAction(roomId, playerToken, (room, game, turnIndex, player) -> {
            if (game.getCurrentBetSize() == 0) {
                throw new GameException("Current bet size is zero");
            }

            if (betSize - game.getCurrentBetSize() < 0 || betSize < 2 *game.getRules().getBigBlind()) {
                throw new GameException("The bet size is too low");
            }

            int newPlayerBalance = player.getChips() - betSize;

            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + player.getId());
            }

            player.setChips(newPlayerBalance);
            game.addToStake(betSize);
            player.addToStake(betSize);
            game.setCurrentBetSize(betSize);
        });
    }
}