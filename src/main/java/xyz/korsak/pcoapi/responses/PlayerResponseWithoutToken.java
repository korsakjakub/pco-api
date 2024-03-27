package xyz.korsak.pcoapi.responses;

import xyz.korsak.pcoapi.player.PlayerState;

public record PlayerResponseWithoutToken(String id, String name, int chips, int stakedChips, PlayerState state) {
}
