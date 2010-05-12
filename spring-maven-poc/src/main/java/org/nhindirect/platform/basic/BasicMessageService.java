package org.nhindirect.platform.basic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.nhindirect.platform.DomainService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageServiceException;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStore;
import org.nhindirect.platform.MessageStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

public class BasicMessageService extends AbstractUserAwareClass implements MessageService {

    @Autowired
    protected MessageStore messageStore;
    
    @Autowired
    protected DomainService domainService;

    public List<Message> getNewMessages(HealthAddress address) throws MessageStoreException, MessageServiceException{

        validateUserForAddress(address);
        
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

    private void validateUserForAddress(HealthAddress address) throws MessageServiceException {
        if (!domainService.isValidAddressForUser(getUser().getUsername(), address)) {
            throw new MessageServiceException("User " + getUser().getUsername() + " not provisioned for address " + address);
        }
    }

    public Message handleMessage(HealthAddress address, String rawMessage) throws MessageStoreException, MessageServiceException {
        Message message = createMessage(rawMessage);
        
        if (hasRole("ROLE_EDGE")) {
            validateEdgeSender(address, message);

            // If it's a local address, store the message, otherwise send to remote HISP
            if (domainService.isLocalAddress(message.getTo())) {
                storeMessage(message);
            } else {        
                sendMessage(message);
            }
            
        } else if (hasRole("ROLE_HISP")) {
            validateHispSender(message);
            storeMessage(message);            
        } 
        
        return message;
    }

    private void validateHispSender(Message message) throws MessageServiceException {
        String userName = getUser().getUsername();
        
        // Does the authenticated HISP doesn't match the From address on the message?
        if (!userName.equals(message.getFrom().getDomain())) {
            throw new MessageServiceException("User " + userName + " does not have permission to send message from address " + message.getFrom());
        }
        
        // Is the To address valid on this HISP?
        if (!domainService.isLocalAddress(message.getTo())) {
            throw new MessageServiceException("Address " + message.getTo() + " is not a valid address on this HISP");
        }
    }

    private void validateEdgeSender(HealthAddress address, Message message) throws MessageServiceException {
        String userName = getUser().getUsername();
        
        // Does the To address of the message match the address on the URI
        if (!address.equals(message.getTo())) {
            throw new MessageServiceException("Message must be addressed to health address on URI. " + message.getTo() + " is not " + address);
        }
        
        // Is the From address a valid address on this HISP assigned to the requesting user
        if (!domainService.isValidAddressForUser(userName, message.getFrom())) {
            throw new MessageServiceException("User " + userName + " does not have permission to send messages from address " + message.getFrom());
        }
    }
    
    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException, MessageServiceException {

        validateUserForAddress(address);
        
        Message message = messageStore.getMessage(address, messageId); 
        return message; 
    }

    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status)
            throws MessageStoreException, MessageServiceException {
        
        validateUserForAddress(address);
        
        messageStore.setMessageStatus(address, messageId, status);
    }
    

    private Message createMessage(String rawMessage) throws MessageStoreException {
        Message message = new Message();
        
        UUID messageId = UUID.randomUUID();
 
        message.setData(rawMessage.getBytes());
        message.setStatus(MessageStatus.NEW);
        message.setMessageId(messageId);
        message.parseMetaData();
        
        return message;
    }
    
    private void storeMessage(Message message) throws MessageStoreException{
        System.out.println("Storing message -- \n" + new String(message.getData()));
        messageStore.putMessage(message.getTo(), message);
    }
    
    private String sendMessage(Message message) {
        // send to remote HISP
        
        System.out.println("Sending message to remote HISP -- \n" + new String(message.getData()));
        return null;
    }
    
}
