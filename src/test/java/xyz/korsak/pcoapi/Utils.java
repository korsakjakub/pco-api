package xyz.korsak.pcoapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;
import xyz.korsak.pcoapi.responses.IdTokenResponse;
import xyz.korsak.pcoapi.responses.RoomCreatedResponse;
import xyz.korsak.pcoapi.room.Room;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class Utils {

    public static String fold(MockMvc mockMvc, String roomId, String playerToken) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/game/fold?roomId=" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();

        return createResult.getResponse().getContentAsString();
    }

    public static String call(MockMvc mockMvc, String roomId, String playerToken) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/game/call?roomId=" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();

        return createResult.getResponse().getContentAsString();
    }

    public static String check(MockMvc mockMvc, String roomId, String playerToken) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/game/check?roomId=" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();

        return createResult.getResponse().getContentAsString();
    }

    public static String raise(MockMvc mockMvc, String roomId, String playerToken, int chips) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/game/raise?roomId=" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + playerToken)
                        .content("{\"chips\": \"" + chips + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return createResult.getResponse().getContentAsString();
    }

    public static String bet(MockMvc mockMvc, String roomId, String playerToken, int chips) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/game/bet?roomId=" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + playerToken)
                        .content("{\"chips\": \"" + chips + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return createResult.getResponse().getContentAsString();
    }

    public static RoomCreatedResponse createRoom(MockMvc mockMvc, String name) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \""+ name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        return new ObjectMapper().readValue(json, RoomCreatedResponse.class);
    }

    public static Room getRoom(MockMvc mockMvc, String token) throws Exception {
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room/token/" + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        return new ObjectMapper().readValue(json, Room.class);
    }


    public static IdTokenResponse createPlayer(MockMvc mockMvc, String queueId, String name) throws Exception {
        MvcResult player1Result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/player/create?queueId=" + queueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(player1Result.getResponse().getContentAsString(), IdTokenResponse.class);
    }


    public static GetPlayersResponse getPlayersInQueue(MockMvc mockMvc, String queueId) throws Exception {
        MvcResult queueUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/queue/" + queueId + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(queueUpdatedResult.getResponse().getContentAsString(), GetPlayersResponse.class);
    }

    public static GetPlayersResponse getPlayersInRoom(MockMvc mockMvc, String roomId) throws Exception {
        MvcResult roomUpdatedResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room/" + roomId + "/players")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(roomUpdatedResult.getResponse().getContentAsString(), GetPlayersResponse.class);
    }

    public static Player movePlayerToRoom(MockMvc mockMvc, String roomId, String playerId, String roomToken) throws Exception {
        MvcResult movePlayerToRoomResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/" + roomId + "/players/" + playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + roomToken))
                .andExpect(status().isOk())
                .andReturn();
        return new ObjectMapper().readValue(movePlayerToRoomResult.getResponse().getContentAsString(), Player.class);
    }

    public static IdResponse decideWinner(MockMvc mockMvc, String roomId, String playerId, String roomToken) throws Exception {
        MvcResult decideWinnerResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/game/decide-winner?roomId=" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + roomToken)
                        .content("{\"id\": \""+ playerId + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return new ObjectMapper().readValue(decideWinnerResult.getResponse().getContentAsString(), IdResponse.class);
    }
}
