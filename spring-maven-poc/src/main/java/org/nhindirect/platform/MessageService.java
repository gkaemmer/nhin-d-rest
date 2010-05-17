package org.nhindirect.platform;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    public List<Message> getNewMessages(HealthAddress address) throws MessageStoreException, MessageServiceException;
    public Message handleMessage(HealthAddress address, String rawMessage) throws MessageStoreException, MessageServiceException;
    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException, MessageServiceException;
    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status) throws MessageStoreException, MessageServiceException;
    
}
