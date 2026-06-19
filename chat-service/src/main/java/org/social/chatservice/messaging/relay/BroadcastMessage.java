package org.social.chatservice.messaging.relay;

import com.fasterxml.jackson.databind.JsonNode;

public record BroadcastMessage(
        String destination,
        String sendToUser,
        JsonNode payload) {
}
