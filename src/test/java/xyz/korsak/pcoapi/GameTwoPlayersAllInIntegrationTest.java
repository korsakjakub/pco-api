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
import xyz.korsak.pcoapi.game.GameState;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.IdResponse;
import xyz.korsak.pcoapi.room.Room;

import java.io.File;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameTwoPlayersAllInIntegrationTest {
    private final RedisTemplate<String, Room> redisTemplate;
    @Autowired
    private MockMvc mockMvc;
    private Room room;

    @Autowired
    public GameTwoPlayersAllInIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-test";
        File json = new File("src/test/resources/GameTwoPlayersAllInIntegrationTest.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testCallRaiseAllIn() throws Exception {
        Player p1 = room.players().getFirst();
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 980));

        room = Utils.getRoom(mockMvc, roomId);
        Player p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));
    }

    @Test
    public void testRaiseAllInFold() throws Exception {
        Player p1 = room.players().getFirst();
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 980));

        room = Utils.getRoom(mockMvc, roomId);
        Player p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);

        Assertions.assertEquals(GameState.WAITING, room.game().state());
        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
    }

    @Test
    public void testAllInWinnerHasMoreChips() throws Exception {
        Player p1 = room.players().getFirst();
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 480));

        room = Utils.getRoom(mockMvc, roomId);
        Player p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);

        Assertions.assertEquals(GameState.IN_PROGRESS, room.game().state());
        Assertions.assertEquals(GameStage.FLOP, room.game().stage());

        room = Utils.getRoom(mockMvc, roomId);

        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);
        p2 = room.players().get(1);

        Assertions.assertEquals(GameState.WAITING, room.game().state());
        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
        Assertions.assertEquals(1500, p1.getChips());
        Assertions.assertEquals(500, p2.getChips());

        Assertions.assertEquals("Game started successfully", Utils.startGame(mockMvc, roomId, room.token()));
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 1490));
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));
        Assertions.assertEquals(new IdResponse(p1.getId()), Utils.decideWinner(mockMvc, roomId, p1.getId(), room.token()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);
        p2 = room.players().get(1);

        Assertions.assertEquals(2000, p1.getChips());
        Assertions.assertEquals(0, p2.getChips());
    }
}
