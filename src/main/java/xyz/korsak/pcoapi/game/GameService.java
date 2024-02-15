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

        return new GetGameResponse(game.state(), game.stage(), game.stakedChips(), game.currentBetSize(),
                p.get(game.currentTurnIndex() % p.size()).getId(),
                p.get(game.dealerIndex() % p.size()).getId(),
                p.get(game.smallBlindIndex() % p.size()).getId(),
                p.get(game.bigBlindIndex() % p.size()).getId());
    }

    public void start(String roomId, String roomToken) {
        final Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        final Game game;

        if (room.game().numberOfHandsCompleted() == 0) {
            room.players().forEach(player -> player.setChips(room.game().rules().startingChips()));
            game = new Game.GameBuilder()
                    .state(GameState.IN_PROGRESS)
                    .numberOfPlayers(room.players().size())
                    .dealerBlindsAndCurrentIndices(0)
                    .stage(GameStage.SMALL_BLIND).build();
        } else {
            game = room.game().toBuilder()
                    .state(GameState.IN_PROGRESS)
                    .currentTurnIndex(room.game().smallBlindIndex())
                    .numberOfPlayers(room.players().size())
                    .stage(GameStage.SMALL_BLIND).build();
        }
        roomRepository.create(blind(blind(room.toBuilder().game(game).build())));
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

        roomRepository.create(room.toBuilder()
                        .game(builder.build())
                        .players(players)
                        .build());
    }

    public void fold(String roomId, String playerToken) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        roomRepository.create(performAction(room, (game, currentPlayer) -> {
            currentPlayer.setActive(false);
            return game.toBuilder();
        }).build());
    }

    public void call(String roomId, String playerToken) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        roomRepository.create(performAction(room, (game, currentPlayer) -> {
            int leftToCall = game.currentBetSize() - currentPlayer.getStakedChips();
            currentPlayer.setChips(currentPlayer.getChips() - leftToCall);
            currentPlayer.addToStake(leftToCall);
            return game.toBuilder().addToStake(leftToCall);
        }).build());
    }

    public void bet(String roomId, String playerToken, int betSize) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        roomRepository.create(performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() != 0) {
                throw new GameException("Current bet size is nonzero");
            }

            if (betSize < game.rules().bigBlind()) {
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
        }).build());
    }

    public void check(String roomId, String playerToken) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        roomRepository.create(performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() > currentPlayer.getStakedChips()) {
                throw new GameException("Cannot check");
            }
            return game.toBuilder();
        }).build());
    }

    public void raise(String roomId, String playerToken, int betSize) {
        Room room = getRoomWithCurrentPlayerToken(roomId, playerToken);
        roomRepository.create(performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() == 0) {
                throw new GameException("Current bet size is zero");
            }

            if (betSize - game.currentBetSize() <= 0 || betSize < 2 * game.rules().bigBlind()) {
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
        }).build());
    }

    /***
     * Blinds are played at the beginning of each hand. Therefore, they can be played:
     * a) at the beginning of the game
     * b) after a fold if only 1 player remains
     * c) after showdown
     */
    public Room blind(Room room) {
        return performAction(room, (game, currentPlayer) -> {
            final int blind = game.stage().equals(GameStage.SMALL_BLIND) ? game.rules().smallBlind() : game.rules().bigBlind();

            int newPlayerBalance = currentPlayer.getChips() - blind;
            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + currentPlayer.getId());
            }

            currentPlayer.setChips(newPlayerBalance);
            currentPlayer.addToStake(blind);
            return game.toBuilder().addToStake(blind)
                    .currentBetSize(blind)
                    .stage(game.stage().next())
                    .decActionsTakenThisRound();
        }).build();
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
            builder.currentTurnIndex(nextActivePlayer(players, game.currentTurnIndex()))
                    .incActionsTakenThisRound();
        }
        Game updatedGame = builder.build(); // we need to build here so players can use updated fields
        players.forEach(
                p -> p.setActions(PlayerActions.createActionsBasedOnBet(updatedGame.currentBetSize(), p.getStakedChips())));
        return room.toBuilder().game(updatedGame).players(players);
    }

    private void endHandWithWinner(Player winner, Game.GameBuilder builder, Game game, List<Player> players) {
        players.forEach(p -> {
            p.setStakedChips(0);
            if (p.getId().equals(winner.getId())) {
                p.setChips(winner.getChips() + game.stakedChips());
            }
            p.setActive(true);
        });
        builder.state(GameState.WAITING)
                .stage(GameStage.SMALL_BLIND)
                .stakedChips(0)
                .currentBetSize(0)
                .incHandsCompleted()
                .incDealerIndex();
    }

    private boolean areAllPlayersMatchingBetSize(List<Player> players, int betSize) {
        return players.stream()
                .filter(Player::isActive)
                .allMatch(player -> player.getStakedChips() == betSize);
    }

    private int nextActivePlayer(List<Player> players, int startIndex) {
        int i = (startIndex + 1) % players.size();
        while (!players.get(i).isActive()) {
            i = (i + 1) % players.size();
        }
        return i;
    }
}
