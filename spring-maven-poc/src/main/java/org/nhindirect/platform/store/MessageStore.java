package org.nhindirect.platform.store;

import java.util.List;
import java.util.UUID;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageStatus;

public interface MessageStore {

    public List<Message> getMessages(HealthAddress address) throws MessageStoreException;
    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException;
    public void putMessage(HealthAddress address, Message message) throws MessageStoreException;
    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status) throws MessageStoreException;
    
}
