package xyz.korsak.pcoapi.queue;

import xyz.korsak.pcoapi.player.Player;

public interface QueueRepository {
    void create(Queue queue);

    Queue findById(String id);

    void delete(String id);

    Player removePlayer(String queueId, String playerId);
}
