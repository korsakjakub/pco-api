package xyz.korsak.pcoapi.queue;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;

import java.util.ArrayList;

@Service
public class QueueService extends BaseService {
    private final QueueRepository queueRepository;
    private final Authorization auth;

    public QueueService(QueueRepository queueRepository, Authorization authorization) {
        this.queueRepository = queueRepository;
        this.auth = authorization;
    }

    public void pushData(String queueId) {
        notifySubscribers(getPlayersInQueue(queueId), queueId);
    }

    public SseEmitter streamPlayersInQueue(String queueId) {
        SseEmitter emitter = newEmitter(queueId);
        notifySubscribers(getPlayersInQueue(queueId), queueId);
        return emitter;
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
        Player player = new Player(id, name, token);
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

    public Player removePlayerFromQueue(String queueId, String playerId, String token) {
        Queue queue = queueRepository.findById(queueId);
        if (queue == null) {
            throw new UnauthorizedAccessException();
        }
        if (!auth.authorizeOwner(queue.getRoomId(), token)) {
            throw new UnauthorizedAccessException();
        }
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
