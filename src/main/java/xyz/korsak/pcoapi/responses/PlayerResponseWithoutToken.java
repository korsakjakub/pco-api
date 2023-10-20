package xyz.korsak.pcoapi.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PlayerResponseWithoutToken {
    private String id;
    private String name;
    private int chips;
    private int stakedChips;
}
