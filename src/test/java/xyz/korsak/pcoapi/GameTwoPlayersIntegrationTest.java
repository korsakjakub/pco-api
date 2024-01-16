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
public class GameTwoPlayersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final RedisTemplate<String, Room> redisTemplate;

    private Room room;

    @Autowired
    public GameTwoPlayersIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-test";
        String json = "{" +
                "\"id\": \"rid-test\"," +
                "\"players\": [" +
                "{" +
                "\"id\": \"pid-player1\"," +
                "\"name\": \"jk\"," +
                "\"chips\": 1000," +
                "\"stakedChips\": 0," +
                "\"token\": \"ptk-player1\"," +
                "\"actions\": [\"Fold\", \"Check\", \"Bet\"]," +
                "\"active\": true" +
                "}," +
                "{" +
                "\"id\": \"pid-player2\"," +
                "\"name\": \"ola\"," +
                "\"chips\": 1000," +
                "\"stakedChips\": 0," +
                "\"token\": \"ptk-player2\"," +
                "\"actions\": [\"Fold\", \"Check\", \"Bet\"]," +
                "\"active\": true" +
                "}" +
                "]," +
                "\"token\": \"rtk-test\"," +
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
                "\"currentTurnIndex\": 1," +
                "\"dealerIndex\": 0," +
                "\"smallBlindIndex\": 1," +
                "\"bigBlindIndex\": 2," +
                "\"actionsTakenThisRound\": 0," +
                "\"numberOfPlayers\": 2" +
                "}," +
                "\"queueId\": \"qid-test\"" +
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
    public void testBetFold() throws Exception {
        Player p2 = room.getPlayers().get(1);
        String roomId = room.getId();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.getPlayers().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.getPlayers().get(0);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals(990, p1.getChips());
        Assertions.assertEquals(980, p2.getChips());
        Assertions.assertEquals(30, room.getGame().getStakedChips());
        Assertions.assertEquals(20, room.getGame().getCurrentBetSize());
    }

    @Test
    public void testBetRaiseCall() throws Exception {
        Player p2 = room.getPlayers().get(1);
        String roomId = room.getId();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.getPlayers().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.getPlayers().get(0);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals(GameStage.FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals(400, room.getGame().getStakedChips());
        Assertions.assertEquals(0, room.getGame().getCurrentBetSize());
        Assertions.assertEquals(0, p1.getStakedChips());
        Assertions.assertEquals(0, p2.getStakedChips());
    }

    @Test
    public void testBetRaiseReRaise() throws Exception {
        Player p2 = room.getPlayers().get(1);
        String roomId = room.getId();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.getPlayers().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p2.getToken(), 300));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.getPlayers().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.getPlayers().get(0);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals(GameStage.FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals(600, room.getGame().getStakedChips());
        Assertions.assertEquals(0, room.getGame().getCurrentBetSize());
        Assertions.assertEquals(0, p1.getStakedChips());
        Assertions.assertEquals(0, p2.getStakedChips());
    }


    @Test
    public void testBetFoldCallIsDealerChanged() throws Exception {
        Player p2 = room.getPlayers().get(1);
        String roomId = room.getId();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.getPlayers().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.getPlayers().get(0);
        p2 = room.getPlayers().get(1);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.getGame().getStage());
        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals(990, p1.getChips());
        Assertions.assertEquals(980, p2.getChips());
        Assertions.assertEquals(30, room.getGame().getStakedChips());
        Assertions.assertEquals(20, room.getGame().getCurrentBetSize());

        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));
    }
}
