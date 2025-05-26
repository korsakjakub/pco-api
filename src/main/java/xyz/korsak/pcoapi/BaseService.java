package xyz.korsak.pcoapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PreDestroy;

@Slf4j
public abstract class BaseService {

    protected final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    protected SseEmitter newEmitter(String roomId) {
        SseEmitter emitter = new SseEmitter(1000 * 60 * 60L);
        List<SseEmitter> roomEmitters = emitters.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());

        roomEmitters.add(emitter);

        emitter.onTimeout(() -> roomEmitters.remove(emitter));

        emitter.onCompletion(() -> roomEmitters.remove(emitter));
        return emitter;
    }

    protected void notifySubscribers(Object data, String roomId) {
        List<SseEmitter> roomEmitters = emitters.get(roomId);
        if (roomEmitters == null || roomEmitters.isEmpty()) {
            log.warn("No emitters found for room id: {}", roomId);
            return;
        }
        roomEmitters.forEach(emitter -> {
            sseExecutor.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data(data);
                    emitter.send(event);
                } catch (Exception ex) {
                    log.warn("Failed to send SSE event to emitter: {}", ex.getMessage());
                    emitter.completeWithError(ex);
                }
            });
        });
    }

    protected String generateRandomString(String prefix) {
        return prefix + "-" + RandomStringUtils.randomAlphanumeric(10);
    }

    @PreDestroy
    public void cleanup() {
        if (sseExecutor != null && !sseExecutor.isShutdown()) {
            sseExecutor.shutdown();
        }
    }
}
