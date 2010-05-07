package org.nhindirect.platform.rest;

import java.util.List;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.store.MessageStore;
import org.nhindirect.platform.store.MessageStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for NHIN Direct REST API as defined here: http://nhindirect.org/REST+Implementation
 * 
 * This is not intended as a secure, reliable, or complete implementation. It's intended
 * as a proof of concept on using Spring MVC 3.0 for the REST implementation.
 *
 */
@Controller
@RequestMapping("/v1/{healthDomain}/{healthEndpoint}/messages")
public class NhinDirect10Controller {

    @Autowired
    protected MessageStore messageStore;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String getMessages(@PathVariable("healthDomain") String healthDomain,
                              @PathVariable("healthEndpoint") String healthEndpoint) throws MessageStoreException {
        
        HealthAddress ha = new HealthAddress(healthDomain, healthEndpoint);
        List<Message> messages = messageStore.getMessages(ha);
        return AtomPublisher.createFeed(messages);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public String postMessage(@PathVariable("healthDomain") String healthDomain,
                              @PathVariable("healthEndpoint") String healthEndpoint,
                              @RequestBody String message)
            throws MessageStoreException {

        HealthAddress ha = new HealthAddress(healthDomain, healthEndpoint);
        Message m = new Message();
        m.setData(message.getBytes());
        m.setStatus("new");

        long messageId = messageStore.putMessage(ha, m);

        return Long.toString(messageId);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    @ResponseBody
    public String getMessage(@PathVariable("healthDomain") String healthDomain,
                             @PathVariable("healthEndpoint") String healthEndpoint, 
                             @PathVariable("messageId") String messageId)
            throws MessageStoreException {

        HealthAddress ha = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageStore.getMessage(ha, Long.parseLong(messageId));
        return new String(message.getData());
    }

    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.GET)
    @ResponseBody
    public String getMessageStatus(@PathVariable("healthDomain") String healthDomain,
                                   @PathVariable("healthEndpoint") String healthEndpoint,
                                   @PathVariable("messageId") String messageId)
            throws MessageStoreException {

        HealthAddress ha = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageStore.getMessage(ha, Long.parseLong(messageId));
        return message.getStatus();
    }

    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.PUT)
    @ResponseBody
    public String setMessageStatus(@PathVariable("healthDomain") String healthDomain,
                                   @PathVariable("healthEndpoint") String healthEndpoint,
                                   @PathVariable("messageId") String messageId, 
                                   @RequestBody String status)
            throws NumberFormatException, MessageStoreException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        messageStore.setMessageStatus(address, Long.parseLong(messageId), new MessageStatus(status));
        return "message status updated to " + status + " for message id " + messageId + " for address " + address.toEmailAddress();
    }
}