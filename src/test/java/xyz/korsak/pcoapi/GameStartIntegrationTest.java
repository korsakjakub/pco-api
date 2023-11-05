package xyz.korsak.pcoapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.IdTokenResponse;
import xyz.korsak.pcoapi.responses.RoomCreatedResponse;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameStartIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoomCreationAndPlayerAddition() throws Exception {

        RoomCreatedResponse r = createRoom("My Room");

        Assertions.assertNotNull(r.getId());
        Assertions.assertNotNull(r.getToken());

        /*
        Room room = getRoom(r.getToken());

        IdTokenResponse idTokenPlayer1 = createPlayer(room.getQueueId(), "Player 1");

        Assertions.assertNotNull(idTokenPlayer1.getId());
        Assertions.assertNotNull(idTokenPlayer1.getToken());

        IdTokenResponse idTokenPlayer2 = createPlayer(room.getQueueId(), "Player 2");
        Assertions.assertNotNull(idTokenPlayer2.getId());
        Assertions.assertNotNull(idTokenPlayer2.getToken());

        GetPlayersResponse updatedQueueResponse = getPlayersInQueue(room.getQueueId());

        Assertions.assertNotNull(updatedQueueResponse);
        Assertions.assertEquals(2, updatedQueueResponse.getPlayers().size());

        GetPlayersResponse updatedRoomResponse = getPlayersInRoom(r.getId());

        Assertions.assertNotNull(updatedRoomResponse);
        Assertions.assertEquals(0, updatedRoomResponse.getPlayers().size());

        Player movePlayerToRoomResponse = movePlayerToRoom(r.getId(), idTokenPlayer1.getId(), r.getToken());

        Assertions.assertNotNull(movePlayerToRoomResponse);
        Assertions.assertEquals(movePlayerToRoomResponse.getId(), idTokenPlayer1.getId());

        GetPlayersResponse updatedRoomResponse2 = getPlayersInRoom(r.getId());

        Assertions.assertNotNull(updatedRoomResponse2);
        Assertions.assertEquals(1, updatedRoomResponse2.getPlayers().size());

        GetPlayersResponse updatedQueueResponse2 = getPlayersInQueue(room.getQueueId());
        Assertions.assertNotNull(updatedQueueResponse2);
        Assertions.assertEquals(1, updatedQueueResponse2.getPlayers().size());
        */

    }

    private RoomCreatedResponse createRoom(String name) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        return new ObjectMapper().readValue(json, RoomCreatedResponse.class);
    }
}
