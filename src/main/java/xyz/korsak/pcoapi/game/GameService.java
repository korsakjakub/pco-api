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
import xyz.korsak.pcoapi.player.PlayerState;
import xyz.korsak.pcoapi.responses.GetGameResponse;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;
import xyz.korsak.pcoapi.rules.PokerRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GameService extends BaseService {
    private final Authorization auth;
    private final RoomRepository roomRepository;

    public GameService(Authorization authorization, RoomRepository roomRepository) {
        this.auth = authorization;
        this.roomRepository = roomRepository;
    }

    /***
     * Distributes the pot to the winners.
     * Start from the smallest common stack.
     * @return a map of player IDs and their winnings
     */
    public static Map<String, Integer> distributeWinnings(List<Player> players, List<Player> winners) {
        var resultStakes = new HashMap<String, Integer>();
        players.forEach(p -> resultStakes.put(p.getId(), 0));

        List<Player> playersToDistribute = new ArrayList<>(players);
        while (playersToDistribute.size() > 1) {
            List<Player> winnersToDistribute = playersToDistribute.stream().filter(p -> winners.stream().map(Player::getId).toList().contains(p.getId())).toList();
            if (winnersToDistribute.isEmpty()) {
                break;
            }

            final int stackPerPlayer = playersToDistribute.stream().mapToInt(Player::getInvestedChips).min().orElseThrow();
            final int stackToDistribute = stackPerPlayer * playersToDistribute.size();

            playersToDistribute.forEach(p -> p.setInvestedChips(p.getInvestedChips() - stackPerPlayer));
            winnersToDistribute.forEach(w -> resultStakes.put(w.getId(), resultStakes.get(w.getId()) + stackToDistribute / winnersToDistribute.size()));
            playersToDistribute = playersToDistribute.stream().filter(p -> p.getInvestedChips() > 0).toList();
        }
        if (playersToDistribute.size() == 1) {
            resultStakes.put(playersToDistribute.getFirst().getId(), resultStakes.get(playersToDistribute.getFirst().getId()) + playersToDistribute.getFirst().getInvestedChips());
            playersToDistribute.forEach(p -> p.setInvestedChips(0));
        }
        return resultStakes;
    }

    public void pushData(String roomId) {
        notifySubscribers(getGameResponse(roomId), roomId);
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
            room.players().forEach(player -> {
                if (player.getChips() == 0) {
                    player.setState(PlayerState.Folded);
                } else {
                    player.setState(PlayerState.Active);
                }
            });

            game = room.game().toBuilder()
                    .state(GameState.IN_PROGRESS)
                    .currentTurnIndex(nextActivePlayer(room.players(), room.game().smallBlindIndex() - 1))
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
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
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

        if (winner.getState().equals(PlayerState.Folded)) {
            throw new GameException("The winner cannot be a player who folded");
        }

        endHandWithWinner(winner, builder, players);

        roomRepository.create(room.toBuilder()
                .game(builder.build())
                .players(players)
                .build());
    }

    public void playFold(String roomId, String playerToken) {
        roomRepository.create(fold(getRoomWithCurrentPlayerToken(roomId, playerToken)));
    }

    public void playCall(String roomId, String playerToken) {
        roomRepository.create(call(getRoomWithCurrentPlayerToken(roomId, playerToken)));
    }

    public void playBet(String roomId, String playerToken, int betSize) {
        roomRepository.create(bet(getRoomWithCurrentPlayerToken(roomId, playerToken), betSize));
    }

    public void playCheck(String roomId, String playerToken) {
        roomRepository.create(check(getRoomWithCurrentPlayerToken(roomId, playerToken)));
    }

    public void playRaise(String roomId, String playerToken, int betSize) {
        roomRepository.create(raise(getRoomWithCurrentPlayerToken(roomId, playerToken), betSize));
    }

    public Room fold(Room room) {
        return performAction(room, (game, currentPlayer) -> {
            currentPlayer.setState(PlayerState.Folded);
            return game.toBuilder();
        }).build();
    }

    public Room call(Room room) {
        return performAction(room, (game, currentPlayer) -> {
            final int leftToCall = game.currentBetSize() - currentPlayer.getStakedChips();
            final int finalBetAmount = Math.min(leftToCall, currentPlayer.getChips());
            currentPlayer.addToStake(finalBetAmount);
            currentPlayer.setChips(Math.max(currentPlayer.getChips() - leftToCall, 0));
            currentPlayer.setInvestedChips(currentPlayer.getInvestedChips() + leftToCall);
            return game.toBuilder().addToStake(finalBetAmount);
        }).build();
    }

    public Room bet(Room room, int betSize) {
        return performAction(room, (game, currentPlayer) -> {
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
            currentPlayer.setInvestedChips(currentPlayer.getInvestedChips() + betSize);
            return game.toBuilder().addToStake(betSize)
                    .currentBetSize(betSize);
        }).build();
    }

    public Room check(Room room) {
        return performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() > currentPlayer.getStakedChips()) {
                throw new GameException("Cannot check");
            }
            return game.toBuilder();
        }).build();
    }

    public Room raise(Room room, int betAddition) {
        return performAction(room, (game, currentPlayer) -> {
            if (game.currentBetSize() == 0) {
                throw new GameException("Current bet size is zero");
            }
            final int betSize = betAddition + currentPlayer.getStakedChips();

            if (betSize - game.currentBetSize() <= 0 || betSize < 2 * game.rules().bigBlind()) {
                throw new GameException("The bet size is too low");
            }

            // final int betAddition = betSize - currentPlayer.getStakedChips();

            final int newPlayerBalance = currentPlayer.getChips() - betAddition;

            if (newPlayerBalance < 0) {
                throw new GameException("Insufficient amount of chips for the player with ID: " + currentPlayer.getId());
            }

            currentPlayer.setChips(newPlayerBalance);
            currentPlayer.addToStake(betAddition);
            currentPlayer.setInvestedChips(currentPlayer.getInvestedChips() + betAddition);
            return game.toBuilder().addToStake(betAddition)
                    .currentBetSize(betSize);
        }).build();
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
            currentPlayer.setInvestedChips(currentPlayer.getInvestedChips() + blind);
            return game.toBuilder().addToStake(blind)
                    .currentBetSize(blind)
                    .stage(game.stage().next())
                    .decActionsTakenThisRound();
        }).build();
    }

    private Room.RoomBuilder performAction(Room room, Action action) {
        final Game game = room.game();

        if (game.stage() == GameStage.SHOWDOWN) {
            return room.toBuilder();
        }

        final Player currentPlayer = getCurrentPlayer(room, game.currentTurnIndex());
        List<Player> players = room.players();
        Game.GameBuilder builder = action.execute(game, currentPlayer);

        if (currentPlayer.getChips() == 0) {
            currentPlayer.setState(PlayerState.AllIn);
        }

        final List<Player> allInPlayers = players.stream().filter(player -> player.getState().equals(PlayerState.AllIn)).toList();
        final List<Player> activePlayers = players.stream().filter(player -> player.getState().equals(PlayerState.Active)).toList();

        if (activePlayers.size() <= 1) {
            if (!allInPlayers.isEmpty()) {
                // Cards up
                builder.stage(GameStage.SHOWDOWN);
            } else {
                // One player left, others folded
                endHandWithWinner(activePlayers.getFirst(), builder, players);
            }
        }
        // All players matched the highest bet, and everybody played at least once
        else if (areAllPlayersMatchingBetSize(players, game.currentBetSize())
                && game.actionsTakenThisRound() + 1 >= activePlayers.size()) {
            int currentTurnIndex = nextActivePlayer(players, game.smallBlindIndex() - 1);
            builder.stage(game.stage().next())
                    .currentBetSize(0)
                    .currentTurnIndex(currentTurnIndex)
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

    private void endHandWithWinner(Player winner, Game.GameBuilder builder, List<Player> players) {
        var winnings = distributeWinnings(players, List.of(winner));

        final int playersLeftToDistribute = players.stream().filter(p -> p.getInvestedChips() > 0).toList().size();

        players.forEach(p -> {
            p.setStakedChips(0);
            p.setChips(p.getChips() + winnings.get(p.getId()));
            p.setState(PlayerState.Active);
        });

        if (playersLeftToDistribute == 0) {
            builder.state(GameState.WAITING)
                    .stakedChips(0)
                    .stage(GameStage.SMALL_BLIND)
                    .currentBetSize(0)
                    .incHandsCompleted()
                    .incDealerIndex();
        }
    }

    private static boolean areAllPlayersMatchingBetSize(List<Player> players, int betSize) {
        return players.stream()
                .filter(player -> player.getState().equals(PlayerState.Active))
                .allMatch(player -> player.getStakedChips() == betSize || player.getChips() == 0);
    }

    private static int nextActivePlayer(List<Player> players, int startIndex) {
        int i = (startIndex + 1) % players.size();
        while (!players.get(i).getState().equals(PlayerState.Active)) {
            i = (i + 1) % players.size();
        }
        return i;
    }

    @FunctionalInterface
    private interface Action {
        Game.GameBuilder execute(Game game, Player currentPlayer);
    }
}
