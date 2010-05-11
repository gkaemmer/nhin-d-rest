package org.nhindirect.platform;

import java.util.List;
import java.util.UUID;

/**
 * MessageStore interface. This interface defines the bare minimum functionality 
 * needed to store messages in an NHIN Direct REST system. 
 */
public interface MessageStore {

    public List<Message> getMessages(HealthAddress address) throws MessageStoreException;
    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException;
    public void putMessage(HealthAddress address, Message message) throws MessageStoreException;
    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status) throws MessageStoreException;
    
} 
