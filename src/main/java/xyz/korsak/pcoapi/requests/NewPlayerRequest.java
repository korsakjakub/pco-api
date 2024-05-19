package xyz.korsak.pcoapi.requests;

import xyz.korsak.pcoapi.player.PlayerAvatar;

public record NewPlayerRequest(String name, PlayerAvatar avatar) {
}
