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
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class QueueAndRoomIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoomCreationAndPlayerAddition() throws Exception {

        Room r = createRoom("My Room");

        Assertions.assertEquals(r.getName(), "My Room");
        Assertions.assertNotNull(r.getId());
        Assertions.assertNotNull(r.getQueueId());
        Assertions.assertNotNull(r.getToken());

        Player playerCreated = createPlayer(r.getQueueId(), "Player 1");

        Assertions.assertEquals(playerCreated.getName(), "Player 1");
        Assertions.assertNotNull(playerCreated.getId());
        Assertions.assertNotNull(playerCreated.getToken());

        Player player2Created = createPlayer(r.getQueueId(), "Player 2");
        Assertions.assertEquals(player2Created.getName(), "Player 2");
        Assertions.assertNotNull(player2Created.getId());
        Assertions.assertNotNull(player2Created.getToken());

        GetPlayersResponse updatedQueueResponse = getPlayersInQueue(r.getQueueId());

        Assertions.assertNotNull(updatedQueueResponse);
        Assertions.assertEquals(updatedQueueResponse.getPlayers().size(), 2);

        GetPlayersResponse updatedRoomResponse = getPlayersInRoom(r.getId());

        Assertions.assertNotNull(updatedRoomResponse);
        Assertions.assertEquals(updatedRoomResponse.getPlayers().size(), 0);

        Player movePlayerToRoomResponse = movePlayerToRoom(r.getId(), playerCreated.getId(), r.getToken());

        Assertions.assertNotNull(movePlayerToRoomResponse);
        Assertions.assertEquals(movePlayerToRoomResponse.getName(), playerCreated.getName());
        Assertions.assertEquals(movePlayerToRoomResponse.getId(), playerCreated.getId());

        GetPlayersResponse updatedRoomResponse2 = getPlayersInRoom(r.getId());

        Assertions.assertNotNull(updatedRoomResponse2);
        Assertions.assertEquals(updatedRoomResponse2.getPlayers().size(), 1);

        GetPlayersResponse updatedQueueResponse2 = getPlayersInQueue(r.getQueueId());
        Assertions.assertNotNull(updatedQueueResponse2);
        Assertions.assertEquals(updatedQueueResponse2.getPlayers().size(), 1);

    }
    private Room createRoom(String name) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \""+ name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        return new ObjectMapper().readValue(json, Room.class);
    }

    private Player createPlayer(String queueId, String name) throws Exception {
        MvcResult player1Result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/player/create?queueId=" + queueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(player1Result.getResponse().getContentAsString(), Player.class);
    }


    private GetPlayersResponse getPlayersInQueue(String queueId) throws Exception {
        MvcResult queueUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/queue/" + queueId + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(queueUpdatedResult.getResponse().getContentAsString(), GetPlayersResponse.class);
    }

    private GetPlayersResponse getPlayersInRoom(String roomId) throws Exception {
        MvcResult roomUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room/" + roomId + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(roomUpdatedResult.getResponse().getContentAsString(), GetPlayersResponse.class);
    }

    private Player movePlayerToRoom(String roomId, String playerId, String roomToken) throws Exception {
        MvcResult movePlayerToRoomResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/" + roomId + "/players/" + playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + roomToken))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(movePlayerToRoomResult.getResponse().getContentAsString(), Player.class);
    }
}
