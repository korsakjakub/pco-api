package xyz.korsak.pcoapi.queue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    public void testAddPlayerToQueue() {
        String queueId = "456";
        String name = "John Doe";
        Queue queue = new Queue("123", queueId);
        queue.setPlayers(new ArrayList<>());
        when(queueRepository.findById(queueId)).thenReturn(queue);

        Player addedPlayer = queueService.addPlayerToQueue(queueId, name);

        Assertions.assertNotNull(addedPlayer);
        Assertions.assertNotNull(addedPlayer.getId());
        Assertions.assertEquals(name, addedPlayer.getName());
        Mockito.verify(queueRepository, Mockito.times(1)).create(queue);
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

    @Test
    public void testRemovePlayerFromQueue() {
        String queueId = "456";
        String playerId = "player1";
        Queue queue = new Queue("123", queueId);
        Player playerToRemove = new PlayerBuilder(playerId).build();
        List<Player> players = new ArrayList<>();
        players.add(playerToRemove);
        players.add(new PlayerBuilder("player2").build());
        queue.setPlayers(players);
        when(queueRepository.findById(queueId)).thenReturn(queue);
        when(queueRepository.removePlayer(queueId, playerId)).thenReturn(playerToRemove);

        Player removedPlayer = queueService.removePlayerFromQueue(queueId, playerId);

        Assertions.assertNotNull(removedPlayer);
        Assertions.assertEquals(playerToRemove, removedPlayer);
        Mockito.verify(queueRepository, Mockito.times(1)).removePlayer(queueId, playerId);
    }
}
