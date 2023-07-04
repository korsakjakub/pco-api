package xyz.korsak.pcoapi.player;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PlayerResponseWithoutToken {
    private String id;
    private String name;
    private Long balance;
}
