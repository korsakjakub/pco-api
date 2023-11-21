package xyz.korsak.pcoapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import xyz.korsak.pcoapi.game.GameStage;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameStartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final RedisTemplate<String, Room> redisTemplate;

    private Room room;

    @Autowired
    public GameStartIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-NeedQRnwfN";
        String json = "{" +
                "\"id\": \"rid-NeedQRnwfN\"," +
                "\"name\": null," +
                "\"players\": [" +
                "{" +
                "\"id\": \"pid-8oTjJJ54VC\"," +
                "\"name\": \"jk\"," +
                "\"chips\": 1000," +
                "\"stakedChips\": 0," +
                "\"token\": \"ptk-ZQH749MkZP\"," +
                "\"actions\": [\"Fold\", \"Check\", \"Bet\"]" +
                "}," +
                "{" +
                "\"id\": \"pid-rFvX5M1QDh\"," +
                "\"name\": \"Ola\"," +
                "\"chips\": 1000," +
                "\"stakedChips\": 0," +
                "\"token\": \"ptk-NWAl02JrwM\"," +
                "\"actions\": [\"Fold\", \"Check\", \"Bet\"]" +
                "}" +
                "]," +
                "\"token\": \"rtk-q9Eh8BE0Cg\"," +
                "\"game\": {" +
                "\"rules\": {" +
                "\"startingChips\": 1000," +
                "\"ante\": 5," +
                "\"smallBlind\": 10," +
                "\"bigBlind\": 20" +
                "}," +
                "\"state\": \"IN_PROGRESS\"," +
                "\"stage\": \"PRE_FLOP\"," +
                "\"stakedChips\": 0," +
                "\"currentBetSize\": 0," +
                "\"currentTurnIndex\": 0," +
                "\"dealerIndex\": 0," +
                "\"smallBlindIndex\": 1," +
                "\"bigBlindIndex\": 2" +
                "}," +
                "\"queueId\": \"qid-NeE9GM0gbD\"" +
                "}";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testBetting() throws Exception {
        Player p1 = room.getPlayers().get(0);
        Player p2 = room.getPlayers().get(1);
        String roomId = room.getId();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p1.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p2.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.getPlayers().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

//        room = Utils.getRoom(mockMvc, roomId);
//        p1 = room.getPlayers().get(0);
//        p2 = room.getPlayers().get(1);

//        Assertions.assertEquals(GameStage.FLOP, room.getGame().getStage());
//        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
//        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
    }
}
