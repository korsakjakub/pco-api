package xyz.korsak.pcoapi.responses;

import xyz.korsak.pcoapi.game.GameStage;
import xyz.korsak.pcoapi.game.GameState;

public record GetGameResponse(GameState state, GameStage stage, int stakedChips, int currentBetSize,
                              String currentTurnPlayerId, String dealerPlayerId, String smallBlindPlayerId,
                              String bigBlindPlayerId) {
}
