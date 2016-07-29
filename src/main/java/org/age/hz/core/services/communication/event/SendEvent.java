package org.age.hz.core.services.communication.event;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.UUID;

public class SendEvent implements Serializable {

    private final String senderId;

    private final String recipientId;

    private final UUID messageId;

    private final long sendTimestamp;

    public SendEvent(String senderId, String recipientId, UUID messageId, long sendTimestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageId = messageId;
        this.sendTimestamp = sendTimestamp;
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
