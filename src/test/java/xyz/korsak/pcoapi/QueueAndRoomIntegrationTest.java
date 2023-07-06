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
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"My Room\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        Room r = new ObjectMapper().readValue(json, Room.class);

        Assertions.assertEquals(r.getName(), "My Room");
        Assertions.assertNotNull(r.getId());
        Assertions.assertNotNull(r.getQueueId());
        Assertions.assertNotNull(r.getToken());

        MvcResult player1Result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/player/create?queueId=" + r.getQueueId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Player 1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Player playerCreated = new ObjectMapper().readValue(player1Result.getResponse().getContentAsString(), Player.class);
        Assertions.assertEquals(playerCreated.getName(), "Player 1");
        Assertions.assertNotNull(playerCreated.getId());
        Assertions.assertNotNull(playerCreated.getToken());


        MvcResult player2Result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/player/create?queueId=" + r.getQueueId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Player 2\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Player player2Created = new ObjectMapper().readValue(player2Result.getResponse().getContentAsString(), Player.class);
        Assertions.assertEquals(player2Created.getName(), "Player 2");
        Assertions.assertNotNull(player2Created.getId());
        Assertions.assertNotNull(player2Created.getToken());

        MvcResult queueUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/queue/" + r.getQueueId() + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        GetPlayersResponse updatedQueueResponse = new ObjectMapper().readValue(queueUpdatedResult.getResponse().getContentAsString(), GetPlayersResponse.class);
        Assertions.assertNotNull(updatedQueueResponse);
        Assertions.assertEquals(updatedQueueResponse.getPlayers().size(), 2);


        MvcResult roomUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room/" + r.getId() + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        GetPlayersResponse updatedRoomResponse = new ObjectMapper().readValue(roomUpdatedResult.getResponse().getContentAsString(), GetPlayersResponse.class);
        Assertions.assertNotNull(updatedRoomResponse);
        Assertions.assertEquals(updatedRoomResponse.getPlayers().size(), 0);


        MvcResult movePlayerToRoomResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/" + r.getId() + "/players/" + playerCreated.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + r.getToken()))
                .andExpect(status().isOk())
                .andReturn();
        Player movePlayerToRoomResponse = new ObjectMapper().readValue(movePlayerToRoomResult.getResponse().getContentAsString(), Player.class);
        Assertions.assertNotNull(movePlayerToRoomResponse);
        Assertions.assertEquals(movePlayerToRoomResponse.getName(), playerCreated.getName());
        Assertions.assertEquals(movePlayerToRoomResponse.getId(), playerCreated.getId());


        MvcResult roomUpdatedResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room/" + r.getId() + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        GetPlayersResponse updatedRoomResponse2 = new ObjectMapper().readValue(roomUpdatedResult2.getResponse().getContentAsString(), GetPlayersResponse.class);
        Assertions.assertNotNull(updatedRoomResponse2);
        Assertions.assertEquals(updatedRoomResponse2.getPlayers().size(), 1);
    }
}
