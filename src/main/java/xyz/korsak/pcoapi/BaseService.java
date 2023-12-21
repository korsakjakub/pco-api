package xyz.korsak.pcoapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class BaseService {

    protected final Map<String, List<SseEmitter>> emitters = new HashMap<>();

    protected SseEmitter newEmitter(String roomId) {
        SseEmitter emitter = new SseEmitter(1000*60*60L);
        List<SseEmitter> roomEmitters = emitters.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());

        roomEmitters.add(emitter);

        emitter.onTimeout(() -> roomEmitters.remove(emitter));

        emitter.onCompletion(() -> roomEmitters.remove(emitter));
        return emitter;
    }
    protected void notifySubscribers(Object data, String roomId) {
        if (!emitters.containsKey(roomId)) {
            log.warn("emitters has no key with this id : " + roomId);
            return;
        }
        emitters.get(roomId).forEach(emitter -> {
            ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
            sseMvcExecutor.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data(data);
                    emitter.send(event);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
        });
    }

    protected String generateRandomString(String prefix) {
        return prefix + "-" + RandomStringUtils.randomAlphanumeric(10);
    }
}
