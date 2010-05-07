package org.nhindirect.platform;

import java.util.Date;
import java.util.UUID;

public class Message {
    
    private UUID messageId;
    private byte[] data;
    private MessageStatus status;
    
    private HealthAddress to;
    private HealthAddress from;
    
    private Date timestamp;

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }
    
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public HealthAddress getTo() {
        return to;
    }

    public void setTo(HealthAddress to) {
        this.to = to;
    }

    public HealthAddress getFrom() {
        return from;
    }

    public void setFrom(HealthAddress from) {
        this.from = from;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
