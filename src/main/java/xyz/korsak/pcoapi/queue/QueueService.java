package xyz.korsak.pcoapi.queue;

import org.springframework.stereotype.Service;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;

import java.util.ArrayList;

@Service
public class QueueService extends BaseService {
    private final QueueRepository queueRepository;

    public QueueService(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    public Queue createQueue(String roomId) {
        String id = generateRandomString("qid");
        Queue queue = new Queue(roomId, id, new ArrayList<>());
        queueRepository.create(queue);
        return queue;
    }

    public Player addPlayerToQueue(String queueId, String name) {
        Queue queue = queueRepository.findById(queueId);
        if (queue == null) {
            throw new NotFoundException(queueId);
        }

        String token = generateRandomString("ptk");
        String id = generateRandomString("pid");
        Player player = new PlayerBuilder(id, name, token).build();
        queue.getPlayers().add(player);
        queueRepository.create(queue);
        return player;
    }

    public GetPlayersResponse getPlayersInQueue(String queueId) {
        Queue queue = queueRepository.findById(queueId);
        if (queue == null) {
            throw new NotFoundException(queueId);
        }
        return new GetPlayersResponse(queue.getPlayers());
    }

    public Player removePlayerFromQueue(String queueId, String playerId) {
        return queueRepository.removePlayer(queueId, playerId);
    }

    public IdResponse getRoomId(String queueId) {
        Queue queue = queueRepository.findById(queueId);
        if (queue == null) {
            throw new NotFoundException(queueId);
        }
        return new IdResponse(queue.getRoomId());
    }
}
