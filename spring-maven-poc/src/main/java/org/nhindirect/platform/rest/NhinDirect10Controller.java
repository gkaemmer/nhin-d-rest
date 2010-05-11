package org.nhindirect.platform.rest;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.nhindirect.platform.HealthAddress;
import org.nhindirect.platform.Message;
import org.nhindirect.platform.MessageService;
import org.nhindirect.platform.MessageStatus;
import org.nhindirect.platform.MessageStoreException;
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
    protected MessageService messageService;

    /** 
     * Get messages addressed to a specified health address.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String getMessages(HttpServletRequest request, 
                              @PathVariable("healthDomain") String healthDomain,
                              @PathVariable("healthEndpoint") String healthEndpoint) throws MessageStoreException {        
        
        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        List<Message> messages = messageService.getNewMessages(address);
        
        return AtomPublisher.createFeed(request.getRequestURL().toString(), address, messages);
    }

    /**
     * Post a message to a specified health address 
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String postMessage(@PathVariable("healthDomain") String healthDomain,
                              @PathVariable("healthEndpoint") String healthEndpoint,
                              @RequestBody String rawMessage)
            throws MessageStoreException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.createMessage(address, rawMessage);

        return message.getMessageId().toString();
    }
    
    /**
     * Get a specific message that was sent to the specified health address 
     */
    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    @ResponseBody
    public String getMessage(@PathVariable("healthDomain") String healthDomain,
                             @PathVariable("healthEndpoint") String healthEndpoint, 
                             @PathVariable("messageId") String messageId)
            throws MessageStoreException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.getMessage(address, UUID.fromString(messageId));

        return new String(message.getData());
    }

    /**
     * Get the status of a specific message sent to the specified health address   
     */
    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.GET)
    @ResponseBody
    public String getMessageStatus(@PathVariable("healthDomain") String healthDomain,
                                   @PathVariable("healthEndpoint") String healthEndpoint,
                                   @PathVariable("messageId") String messageId)
            throws MessageStoreException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        Message message = messageService.getMessage(address, UUID.fromString(messageId));

        return message.getStatus().toString();
    }

    /**
     * Set the status of a specific message sent to the specified health address
     */
    @RequestMapping(value = "/{messageId}/status", method = RequestMethod.PUT)
    @ResponseBody
    public String setMessageStatus(@PathVariable("healthDomain") String healthDomain,
                                   @PathVariable("healthEndpoint") String healthEndpoint,
                                   @PathVariable("messageId") String messageId, 
                                   @RequestBody String status)
            throws NumberFormatException, MessageStoreException {

        HealthAddress address = new HealthAddress(healthDomain, healthEndpoint);
        messageService.setMessageStatus(address, UUID.fromString(messageId), MessageStatus.valueOf(status.toUpperCase()));
        
        return "message status updated to " + status + " for message id " + messageId + " for address " + address.toEmailAddress();
    }
}