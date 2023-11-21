package xyz.korsak.pcoapi.queue;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QueueService extends BaseService {
    private final QueueRepository queueRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private void notifySubscribers(String queueId) {
        GetPlayersResponse r = getPlayersInQueue(queueId);
        emitters.forEach(emitter -> {
            try {
                emitter.send(r);
            } catch(IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        });
    }

    public SseEmitter streamPlayersInQueue(String queueId) {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onCompletion(() -> emitters.remove(emitter));

        notifySubscribers(queueId);
        return emitter;
    }
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
        notifySubscribers(queueId);
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
        Player removedPlayer = queueRepository.removePlayer(queueId, playerId);
        notifySubscribers(queueId);
        return removedPlayer;
    }

    public IdResponse getRoomId(String queueId) {
        Queue queue = queueRepository.findById(queueId);
        if (queue == null) {
            throw new NotFoundException(queueId);
        }
        return new IdResponse(queue.getRoomId());
    }
}
