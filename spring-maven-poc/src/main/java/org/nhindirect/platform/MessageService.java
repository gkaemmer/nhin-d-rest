package org.nhindirect.platform;

import java.util.List;
import java.util.UUID;


public interface MessageService {

    public List<Message> getNewMessages(HealthAddress address) throws MessageStoreException;
    public Message createMessage(HealthAddress address, String rawMessage) throws MessageStoreException;
    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException;
    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status) throws MessageStoreException;
    
}
