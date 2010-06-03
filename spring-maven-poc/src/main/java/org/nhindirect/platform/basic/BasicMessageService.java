package org.nhindirect.platform.basic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.nhindirect.platform.DomainService;
import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageServiceException;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStore;
import org.nhindirect.platform.MessageStoreException;
import org.nhindirect.platform.rest.RestClient;
import org.nhindirect.platform.security.agent.NhinDAgentAdapter;

import org.springframework.beans.factory.annotation.Autowired;

public class BasicMessageService extends AbstractUserAwareClass implements MessageService {
    @Autowired
    protected MessageStore messageStore;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected RestClient restClient;

    @Autowired
    protected NhinDAgentAdapter agent;

    private boolean smimeEnabled = false;

    private Log log = LogFactory.getLog(BasicMessageService.class);

    public List<Message> getNewMessages(HealthAddress address) throws MessageStoreException, MessageServiceException {
        // If it's an edge, validate the user can access this address
        if (!validateUserForAddress(address)) {
            throw new MessageServiceException("User " + getUser().getUsername() + " not provisioned for address "
                    + address);
        }

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

    public Message handleMessage(HealthAddress address, String rawMessage) throws MessageStoreException,
            MessageServiceException {

        // Either my client or Spring MVC or something is stripping off carriage returns.
        // If the message doesn't have carriage returns, fix it. This is temporary until I
        // track down the real problem.
        if (rawMessage.indexOf("\r\n") < 0) {
            rawMessage = rawMessage.replaceAll("\n", "\r\n");
        }

        Message message = createMessage(rawMessage);

        log.debug("handle message called, smime " + (smimeEnabled ? "enabled" : "disabled"));

        if (hasRole("ROLE_EDGE")) {
            validateEdgeSender(address, message);

            // If S/MIME is enabled encrypt the message
            if (smimeEnabled) {
                agent.encryptMessage(message);
            }

            // If it's a remote address then send it to the remote HISP.
            if (!domainService.isLocalAddress(message.getTo())) {
                sendMessage(message);

            } else {

                // If it's a local address, we still need to make sure that
                // the trust model allows delivery to the local address. We'll
                // attempt to decrypt the message to validate that.
                if (smimeEnabled) {
                    agent.decryptMessage(message);
                }
            }

        } else if (hasRole("ROLE_HISP")) {
            validateHispSender(message);

            // If S/MIME is enabled decrypt the message
            if (smimeEnabled) {
                agent.decryptMessage(message);
            }

        }

        storeMessage(message);

        return message;
    }

    private void validateHispSender(Message message) throws MessageServiceException {
        // temporarily eliminating this validation since the client HISP cert no longer contains the
        // domain of the sender.

        /*
         * // Does the authenticated HISP doesn't match the From address on the message? String
         * userName = getUser().getUsername(); if (!userName.equals(message.getFrom().getDomain()))
         * { throw new MessageServiceException("User " + userName +
         * " does not have permission to send message from address " + message.getFrom()); }
         */

        // Is the To address valid on this HISP?
        if (!domainService.isLocalAddress(message.getTo())) {
            throw new MessageServiceException("Address " + message.getTo() + " is not a valid address on this HISP");
        }
    }

    private void validateEdgeSender(HealthAddress address, Message message) throws MessageServiceException {
        String userName = getUser().getUsername();

        // Does the To address of the message match the address on the URI
        if (!address.equals(message.getTo())) {
            throw new MessageServiceException("Message must be addressed to health address on URI. " + message.getTo()
                    + " is not " + address);
        }

        // Is the From address a valid address on this HISP assigned to the requesting user
        if (!domainService.isValidAddressForUser(userName, message.getFrom())) {
            throw new MessageServiceException("User " + userName
                    + " does not have permission to send messages from address " + message.getFrom());
        }
    }

    public Message getMessage(HealthAddress address, UUID messageId) throws MessageStoreException,
            MessageServiceException {

        // Pull a message out of the store... the user might be the recipient or the sender.
        // THIS is not always the case with S/MIME support. A user won't be able to retrieve a sent
        // message, since they are unable to decrypt and we store encrypted messages.
        Message message = messageStore.getMessage(address, messageId);

        // Check to see if the user is the recipient
        if (!validateUserForAddress(address)) {
            // The user is not the recipient, check to see if he's the sender

            if (message != null) {
                if (!validateUserForAddress(message.getFrom())) {
                    // The user is neither the sender or the recipient, reject
                    // the request
                    throw new MessageServiceException("User " + getUser().getUsername()
                            + " not provisioned for address " + address);
                }
            }
        }

        return message;
    }

    private boolean validateUserForAddress(HealthAddress address) throws MessageServiceException {
        return domainService.isValidAddressForUser(getUser().getUsername(), address);
    }

    public void setMessageStatus(HealthAddress address, UUID messageId, MessageStatus status)
            throws MessageStoreException, MessageServiceException {

        // If it's an edge, validate the user can access this address
        if (hasRole("ROLE_EDGE")) {
            if (!validateUserForAddress(address)) {
                throw new MessageServiceException("User " + getUser().getUsername() + " not provisioned for address "
                        + address);
            }
        }

        // If it were an HISP, we may want to validate that the HISP has permission to set status
        // for this message by checking to see if the CN in the cert (username) has the same DNS
        // resolution as the domain from the TO address

        // But for now, we won't.

        messageStore.setMessageStatus(address, messageId, status);

        if (hasRole("ROLE_EDGE")) {

            Message message = messageStore.getMessage(address, messageId);

            if (!domainService.isLocalAddress(message.getFrom())) {
                restClient.putStatus(message);
            }
        }
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

    private void storeMessage(Message message) throws MessageStoreException {
        messageStore.putMessage(message.getTo(), message);
    }

    private void sendMessage(Message message) throws MessageServiceException {
        // send to remote HISP
        String locationHeaderValue = restClient.postMessage(message);
        String uuid = StringUtils.substringAfterLast(locationHeaderValue, "/");

        // Record the UUID generated by the destination HISP.
        message.setMessageId(UUID.fromString(uuid));
    }

    public void setSmimeEnabled(boolean smimeEnabled) {
        this.smimeEnabled = smimeEnabled;
    }

}
