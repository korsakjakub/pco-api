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

import java.io.File;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameTwoPlayersIntegrationTest {

    private final RedisTemplate<String, Room> redisTemplate;
    @Autowired
    private MockMvc mockMvc;
    private Room room;

    @Autowired
    public GameTwoPlayersIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-test";
        File json = new File("src/test/resources/GameTwoPlayersIntegrationTest.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testBetRaiseCall() throws Exception {
        Player p2 = room.players().get(1);
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.players().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.players().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);
        p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals(400, room.game().stakedChips());
        Assertions.assertEquals(0, room.game().currentBetSize());
        Assertions.assertEquals(0, p1.getStakedChips());
        Assertions.assertEquals(0, p2.getStakedChips());
    }

    @Test
    public void testBetRaiseReRaise() throws Exception {
        Player p2 = room.players().get(1);
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.players().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.players().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p2.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);
        p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals(600, room.game().stakedChips());
        Assertions.assertEquals(0, room.game().currentBetSize());
        Assertions.assertEquals(0, p1.getStakedChips());
        Assertions.assertEquals(0, p2.getStakedChips());
    }


    @Test
    public void testBetFoldCallIsDealerChanged() throws Exception {
        Player p2 = room.players().get(1);
        String roomId = room.id();

        Assertions.assertEquals(0, room.game().dealerIndex());
        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.players().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);
        p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals(1000, p1.getChips());
        Assertions.assertEquals(1000, p2.getChips());
        Assertions.assertEquals(0, room.game().stakedChips());
        Assertions.assertEquals(0, room.game().currentBetSize());
        Assertions.assertEquals(1, room.game().dealerIndex());

    }
}
