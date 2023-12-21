package xyz.korsak.pcoapi;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseService {

    protected final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private Long eventId = 0L;
    protected void notifySubscribers(Object r) {
        emitters.forEach(emitter -> {
            ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
            sseMvcExecutor.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data(r)
                            .id(String.valueOf(eventId));
                    emitter.send(event);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
        });
        eventId++;
    }

    protected String generateRandomString(String prefix) {
        return prefix + "-" + RandomStringUtils.randomAlphanumeric(10);
    }
}
