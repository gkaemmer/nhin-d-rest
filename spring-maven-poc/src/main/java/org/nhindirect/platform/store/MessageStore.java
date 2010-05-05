package org.nhindirect.platform.store;

import java.util.List;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageStatus;

public interface MessageStore {

    public List<Message> getMessages(HealthAddress address) throws MessageStoreException;
    public Message getMessage(HealthAddress address, long messageId) throws MessageStoreException;
    public long putMessage(HealthAddress address, Message message) throws MessageStoreException;
    public void setMessageStatus(HealthAddress address, long messageId, MessageStatus status) throws MessageStoreException;
    
}
