package org.age.hz.core.services.communication.event;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.UUID;

public class ReceiveEvent implements Serializable {

    private final String senderId;

    private final String recipientId;

    private final UUID messageId;

    private final long sendTimestamp;

    public ReceiveEvent(String senderId, String recipientId, UUID messageId, long sendTimestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageId = messageId;
        this.sendTimestamp = sendTimestamp;
    }

    public ReceiveEvent(SendEvent sendEvent) {
        this.senderId = sendEvent.getSenderId();
        this.recipientId = sendEvent.getRecipientId();
        this.messageId = sendEvent.getMessageId();
        this.sendTimestamp = sendEvent.getSendTimestamp();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public long getSendTimestamp() {
        return sendTimestamp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("senderId", senderId)
                .add("recipientId", recipientId)
                .add("messageId", messageId)
                .add("sendTimestamp", sendTimestamp)
                .toString();
    }
}
