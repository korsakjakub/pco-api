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
        endHandWithWinner(winner, room.getPlayers(), room.getGame());
    }

    public void fold(String roomId, String playerToken) {
        performAction(roomId, playerToken, (room, game, player) -> player.setActive(false));
    }

    public void call(String roomId, String playerToken) {
        performAction(roomId, playerToken, (room, game, player) -> {
            int leftToCall = game.getCurrentBetSize() - player.getStakedChips();
            player.setChips(player.getChips() - leftToCall);
            player.addToStake(leftToCall);
            game.addToStake(leftToCall);
        });
    }

    public void bet(String roomId, String playerToken, int betSize) {
        performAction(roomId, playerToken, (room, game, player) -> {
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
        performAction(roomId, playerToken, (room, game, player) -> {
            if (game.getCurrentBetSize() != 0) {
                throw new GameException("Current bet size is nonzero");
            }
        });
    }

    public void raise(String roomId, String playerToken, int betSize) {
        performAction(roomId, playerToken, (room, game, player) -> {
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
            game.updateLastToPlay(room.getPlayers().size());
        });
    }

    @FunctionalInterface
    private interface Action {
        void execute(Room room, Game game, Player player);
    }

    private void performAction(String roomId, String playerToken, Action action) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        Game game = room.getGame();
        Player player = getCurrentPlayer(room, game.getCurrentTurnIndex());
        action.execute(room, game, player);
        List<Player> players = room.getPlayers();

        if (activePlayersCount(players) == 1) {
            Optional<Player> lastPlayer = players.stream().filter(Player::isActive).findFirst();
            if (lastPlayer.isPresent()) {
                Player winner = lastPlayer.get();
                endHandWithWinner(winner, players, game);
            }
        }

        if (game.getActionsTakenThisRound() == 0) {
            game.updateLastToPlay(players.size());
        }

        if (isBettingRoundOver(players, game.getCurrentTurnIndex(), game.getLastToPlayIndex(), game.getActionsTakenThisRound())) {
            game.nextStage();
            game.setCurrentBetSize(0);
            game.setCurrentTurnIndex(game.firstToPlayIndex(players.size()));
            game.setActionsTakenThisRound(0);
            players.forEach(p -> p.setStakedChips(0));
        } else {
            game.nextTurnIndex(players.size());
            game.incrementActionsTakenThisRound();
        }
        room.getPlayers().forEach(p -> p.setActions(PlayerActions.createActionsBasedOnBet(game.getCurrentBetSize(), p.getStakedChips())));
        room.setGame(game);
        room.setPlayers(players);

        roomRepository.create(room);
    }

    private void endHandWithWinner(Player winner, List<Player> players, Game game) {
        winner.setChips(winner.getChips() + game.getStakedChips());
        game.setStage(GameStage.PRE_FLOP);
        game.setCurrentBetSize(0);
        game.setStakedChips(0);
        players.forEach(p -> p.setStakedChips(0));
        game.setCurrentTurnIndex(game.firstToPlayIndex(players.size()));
    }

    private boolean isBettingRoundOver(List<Player> players, int currentTurnIndex, int lastToPlayIndex, int actionsTakenThisRound) {
        if (actionsTakenThisRound == 0) {
            return false;
        }
        if (!areBettingAmountsEqual(players)) {
            return false;
        }
        return currentTurnIndex == lastToPlayIndex;
    }

    private int activePlayersCount(List<Player> players) {
        return (int) players.stream().filter(Player::isActive).count();
    }

    private boolean areBettingAmountsEqual(List<Player> players) {
        int referenceBet = players.stream()
                .filter(Player::isActive)
                .findFirst()
                .map(Player::getStakedChips)
                .orElse(0);

        return players.stream()
                .filter(Player::isActive)
                .allMatch(player -> player.getStakedChips() == referenceBet);
    }
}