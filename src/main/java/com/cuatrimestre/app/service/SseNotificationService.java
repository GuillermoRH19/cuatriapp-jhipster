package com.cuatrimestre.app.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseNotificationService {

    private final Logger log = LoggerFactory.getLogger(SseNotificationService.class);

    // Map login -> SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String login) {
        // 1 hour timeout
        SseEmitter emitter = new SseEmitter(3600000L);
        emitters.put(login, emitter);

        emitter.onCompletion(() -> emitters.remove(login));
        emitter.onTimeout(() -> emitters.remove(login));
        emitter.onError((e) -> emitters.remove(login));

        log.debug("User {} subscribed to SSE", login);
        return emitter;
    }

    public void notifyUserDeleted(String login) {
        SseEmitter emitter = emitters.get(login);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("USER_DELETED").data("DELETED"));
                log.debug("Notified user {} of deletion", login);
                emitter.complete();
            } catch (IOException e) {
                emitters.remove(login);
            }
        }
    }

    public void notifyPermissionsUpdated(String login) {
        SseEmitter emitter = emitters.get(login);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("PERMISSIONS_UPDATED").data("UPDATED"));
                log.debug("Notified user {} of permission update", login);
            } catch (IOException e) {
                emitters.remove(login);
            }
        }
    }

    public void notifyAllByPerfilId(Integer perfilId) {
        emitters.forEach((login, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("PERMISSIONS_UPDATED").data(perfilId));
            } catch (IOException e) {
                emitters.remove(login);
            }
        });
    }

    // Helper to clear if needed
    public void removeUser(String login) {
        emitters.remove(login);
    }
}
