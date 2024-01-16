package xyz.korsak.pcoapi.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class RoomCreatedResponse {
    private String id;
    private String queueId;
    private String token;
}
