package org.nhindirect.platform;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    private static Pattern MIME_META_PATTERN = Pattern.compile("From: (.*)[\\r\\n]*To: (.*)[\\r\\n]*");
    
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
    
    public void parseMetaData() {
        Matcher m = MIME_META_PATTERN.matcher(new String(data));
        if (m.find()) {
            from = HealthAddress.parseEmailAddress(m.group(1));
            to = HealthAddress.parseEmailAddress(m.group(2));
        }
    }
}
