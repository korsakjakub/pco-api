package xyz.korsak.pcoapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoomCreationAndPlayerAddition() throws Exception {

        RoomCreatedResponse r = Utils.createRoom(mockMvc, "My Room");

        Assertions.assertNotNull(r.id());
        Assertions.assertNotNull(r.token());

        Room room = Utils.getRoom(mockMvc, r.token());

        IdTokenResponse idTokenPlayer1 = Utils.createPlayer(mockMvc, room.queueId(), "Player 1");

        Assertions.assertNotNull(idTokenPlayer1.id());
        Assertions.assertNotNull(idTokenPlayer1.token());

        IdTokenResponse idTokenPlayer2 = Utils.createPlayer(mockMvc, room.queueId(), "Player 2");
        Assertions.assertNotNull(idTokenPlayer2.id());
        Assertions.assertNotNull(idTokenPlayer2.token());

        GetPlayersResponse updatedQueueResponse = Utils.getPlayersInQueue(mockMvc, room.queueId());

        Assertions.assertNotNull(updatedQueueResponse);
        Assertions.assertEquals(2, updatedQueueResponse.getPlayers().size());

        GetPlayersResponse updatedRoomResponse = Utils.getPlayersInRoom(mockMvc, r.id());

        Assertions.assertNotNull(updatedRoomResponse);
        Assertions.assertEquals(0, updatedRoomResponse.getPlayers().size());

        Player movePlayerToRoomResponse = Utils.movePlayerToRoom(mockMvc, r.id(), idTokenPlayer1.id(), r.token());

        Assertions.assertNotNull(movePlayerToRoomResponse);
        Assertions.assertEquals(movePlayerToRoomResponse.getId(), idTokenPlayer1.id());

        GetPlayersResponse updatedRoomResponse2 = Utils.getPlayersInRoom(mockMvc, r.id());

        Assertions.assertNotNull(updatedRoomResponse2);
        Assertions.assertEquals(1, updatedRoomResponse2.getPlayers().size());

        GetPlayersResponse updatedQueueResponse2 = Utils.getPlayersInQueue(mockMvc, room.queueId());
        Assertions.assertNotNull(updatedQueueResponse2);
        Assertions.assertEquals(1, updatedQueueResponse2.getPlayers().size());

    }
}
