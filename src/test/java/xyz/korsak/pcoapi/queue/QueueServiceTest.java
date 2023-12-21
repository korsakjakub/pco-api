package xyz.korsak.pcoapi.queue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class QueueServiceTest {

    private QueueService queueService;

    @Mock
    private QueueRepository queueRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        queueService = new QueueService(queueRepository);
    }

    @Test
    public void testCreateQueue() {
        String roomId = "123";

        Queue createdQueue = queueService.createQueue(roomId);

        Assertions.assertNotNull(createdQueue);
        Assertions.assertEquals(roomId, createdQueue.getRoomId());
    }

    @Test
    public void testGetPlayersInQueue() {
        String queueId = "456";
        Queue queue = new Queue("123", queueId);
        List<Player> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(new PlayerBuilder("player1").build());
        expectedPlayers.add(new PlayerBuilder("player2").build());
        queue.setPlayers(expectedPlayers);
        when(queueRepository.findById(queueId)).thenReturn(queue);

        GetPlayersResponse retrievedPlayers = queueService.getPlayersInQueue(queueId);

        Assertions.assertEquals(expectedPlayers.get(0).getName(), retrievedPlayers.getPlayers().get(0).getName());
        Assertions.assertEquals(expectedPlayers.get(1).getName(), retrievedPlayers.getPlayers().get(1).getName());
        Assertions.assertEquals(expectedPlayers.get(0).getId(), retrievedPlayers.getPlayers().get(0).getId());
        Assertions.assertEquals(expectedPlayers.get(1).getId(), retrievedPlayers.getPlayers().get(1).getId());
    }
}
