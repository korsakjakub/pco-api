package xyz.korsak.pcoapi.responses;

import lombok.Setter;
import lombok.Value;

@Value
public class StreamResponse {
    GetGameResponse getGameResponse;
    GetPlayersResponse getPlayersResponse;
    // IdResponse getCurrentPlayer;
}
