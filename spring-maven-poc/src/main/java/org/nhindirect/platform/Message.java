package org.nhindirect.platform;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

public class Message {

    private UUID messageId;
    private byte[] data;
    private MessageStatus status;

    private HealthAddress to;
    private HealthAddress from;

    private String subject;

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void parseMetaData() throws AddressException {

        // TODO : Graceful handle failures when the message can't be parsed or these
        // headers aren't found.

        Session session = Session.getDefaultInstance(new Properties());

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            MimeMessage message = new MimeMessage(session, in);
            this.from = HealthAddress.parseEmailAddress(message.getHeader("from", ","));
            this.to = HealthAddress.parseEmailAddress(message.getHeader("to", ","));
            String tmpSubject = message.getSubject();
            if (tmpSubject != null && !tmpSubject.isEmpty()) {
            	this.subject = tmpSubject;
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}
