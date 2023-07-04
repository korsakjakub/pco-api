package xyz.korsak.pcoapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomGetPlayersInRoomResponse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RoomIntegrationTest {
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
        Assertions.assertNotNull(r.getToken());

        MvcResult player1Result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/" + r.getId() + "/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Player 1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Player playerCreated = new ObjectMapper().readValue(player1Result.getResponse().getContentAsString(), Player.class);
        Assertions.assertEquals(playerCreated.getName(), "Player 1");
        Assertions.assertNotNull(playerCreated.getId());
        Assertions.assertNotNull(playerCreated.getToken());


        MvcResult player2Result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/" + r.getId() + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Player 2\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Player player2Created = new ObjectMapper().readValue(player2Result.getResponse().getContentAsString(), Player.class);
        Assertions.assertEquals(player2Created.getName(), "Player 2");
        Assertions.assertNotNull(player2Created.getId());
        Assertions.assertNotNull(player2Created.getToken());

        MvcResult roomUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room/" + r.getId() + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        RoomGetPlayersInRoomResponse updatedRoomResponse = new ObjectMapper().readValue(roomUpdatedResult.getResponse().getContentAsString(), RoomGetPlayersInRoomResponse.class);
        Assertions.assertNotNull(updatedRoomResponse);
        Assertions.assertEquals(updatedRoomResponse.getPlayers().size(), 2);
    }
}
