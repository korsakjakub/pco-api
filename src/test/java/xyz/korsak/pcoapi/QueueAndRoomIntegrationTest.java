package xyz.korsak.pcoapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdTokenResponse;
import xyz.korsak.pcoapi.responses.RoomCreatedResponse;
import xyz.korsak.pcoapi.room.Room;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class QueueAndRoomIntegrationTest {
    private final MockMvc mockMvc;

    public QueueAndRoomIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testRoomCreationAndPlayerAddition() throws Exception {

        RoomCreatedResponse r = Utils.createRoom(mockMvc, "My Room");

        Assertions.assertNotNull(r.getId());
        Assertions.assertNotNull(r.getToken());

        Room room = Utils.getRoom(mockMvc, r.getToken());

        IdTokenResponse idTokenPlayer1 = Utils.createPlayer(mockMvc, room.getQueueId(), "Player 1");

        Assertions.assertNotNull(idTokenPlayer1.getId());
        Assertions.assertNotNull(idTokenPlayer1.getToken());

        IdTokenResponse idTokenPlayer2 = Utils.createPlayer(mockMvc, room.getQueueId(), "Player 2");
        Assertions.assertNotNull(idTokenPlayer2.getId());
        Assertions.assertNotNull(idTokenPlayer2.getToken());

        GetPlayersResponse updatedQueueResponse = Utils.getPlayersInQueue(mockMvc, room.getQueueId());

        Assertions.assertNotNull(updatedQueueResponse);
        Assertions.assertEquals(2, updatedQueueResponse.getPlayers().size());

        GetPlayersResponse updatedRoomResponse = Utils.getPlayersInRoom(mockMvc, r.getId());

        Assertions.assertNotNull(updatedRoomResponse);
        Assertions.assertEquals(0, updatedRoomResponse.getPlayers().size());

        Player movePlayerToRoomResponse = Utils.movePlayerToRoom(mockMvc, r.getId(), idTokenPlayer1.getId(), r.getToken());

        Assertions.assertNotNull(movePlayerToRoomResponse);
        Assertions.assertEquals(movePlayerToRoomResponse.getId(), idTokenPlayer1.getId());

        GetPlayersResponse updatedRoomResponse2 = Utils.getPlayersInRoom(mockMvc, r.getId());

        Assertions.assertNotNull(updatedRoomResponse2);
        Assertions.assertEquals(1, updatedRoomResponse2.getPlayers().size());

        GetPlayersResponse updatedQueueResponse2 = Utils.getPlayersInQueue(mockMvc, room.getQueueId());
        Assertions.assertNotNull(updatedQueueResponse2);
        Assertions.assertEquals(1, updatedQueueResponse2.getPlayers().size());

    }
}
