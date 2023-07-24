package xyz.korsak.pcoapi.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class IdTokenResponse {
    private String id;
    private String token;
}
