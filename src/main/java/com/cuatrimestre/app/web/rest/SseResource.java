package com.cuatrimestre.app.web.rest;

import com.cuatrimestre.app.security.SecurityUtils;
import com.cuatrimestre.app.service.SseNotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class SseResource {

    private final SseNotificationService sseNotificationService;

    public SseResource(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return SecurityUtils.getCurrentUserLogin()
            .map(sseNotificationService::subscribe)
            .orElseGet(() -> {
                SseEmitter emitter = new SseEmitter();
                emitter.completeWithError(new RuntimeException("Not authenticated"));
                return emitter;
            });
    }
}
