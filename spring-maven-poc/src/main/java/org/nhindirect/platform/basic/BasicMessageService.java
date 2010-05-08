package org.nhindirect.platform.basic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStore;
import org.nhindirect.platform.MessageStoreException;
import org.springframework.beans.factory.annotation.Autowired;

public class BasicMessageService implements MessageService {

    @Autowired
    protected MessageStore messageStore;

    public List<Message> getNewMessages(HealthAddress address) throws MessageStoreException {

        List<Message> messages = messageStore.getMessages(address);

        // Filter out non-new messages
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
            Message message = iterator.next();
            
            if (message.getStatus() != MessageStatus.NEW) {
                iterator.remove();
            }
        }

        // Sort remaining messages by timestamp
        Collections.sort(messages, new Comparator<Message>() {
            public int compare(Message m1, Message m2) {
                return m1.getTimestamp().compareTo(m2.getTimestamp());
            };
        });
        
        return messages;
    }

    
    public Message createMessage(HealthAddress address, String rawMessage) throws MessageStoreException {
        Message message = new Message();
        
        UUID messageId = UUID.randomUUID();
 
        message.setData(rawMessage.getBytes());
        message.setStatus(MessageStatus.NEW);
        message.setMessageId(messageId);
        messageStore.putMessage(address, message);
        
        return message;
    }

    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException {
        return messageStore.getMessage(address, messageId);
    }

    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status)
            throws MessageStoreException {
        messageStore.setMessageStatus(address, messageId, status);
    }
}
