package org.social.chatservice;

import lombok.RequiredArgsConstructor;
import org.social.chatservice.messaging.publishers.PingPublisher;
import org.social.common.dto.ApiResponse;
import org.social.common.events.PingEvent;
import org.social.common.events.AnalyzeSentimentEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/ping")
@RequiredArgsConstructor
public class PingController {

    private final PingPublisher pingPublisher;

    @GetMapping
    public ResponseEntity<ApiResponse<PingEvent>> ping(
            @RequestParam(name = "msg", defaultValue = "hello from chat-service") String msg) {
        PingEvent event = new PingEvent("chat-service", msg, Instant.now());
        pingPublisher.sendPing(event);
        return ApiResponse.ok("Ping sent", event);
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalyzeSentimentEvent>> analyze(
            @RequestBody AnalyzeSentimentEvent event) {
        pingPublisher.sendAnalyzeRequest(event);
        return ApiResponse.ok("Analyze request sent", event);
    }
}
