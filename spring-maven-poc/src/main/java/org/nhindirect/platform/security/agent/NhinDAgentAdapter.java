package org.nhindirect.platform.security.agent;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nhindirect.platform.DomainService;
import org.nhindirect.platform.Message;
import org.nhindirect.stagent.NHINDAgent;
import org.springframework.beans.factory.annotation.Autowired;

public class NhinDAgentAdapter {
    
    @Autowired
    private DomainService domainService;
    
    @Autowired
    private CertificateServiceAdapter certificateServiceAdapter;
    
    @Autowired
    private CertificateStoreAdapter certificateStoreAdapter;
    
    @Autowired
    private TrustSettingsStoreAdapter trustSettingsStoreAdapter;
    
    public Log log = LogFactory.getLog(NhinDAgentAdapter.class);
    
    private Map<String, NHINDAgent> agentMap;
    
    public void init() {

        log.debug("initializing NhinDAgentAdapter");
        
        agentMap = new TreeMap<String, NHINDAgent>();
        
        Set<String> domains = domainService.getDomains();
        
        for (String domain : domains) {
            // for each domain that we're managing, create an NHINDAgent, since the current 
            // agent is tied to a domain
            
            NHINDAgent agent = new NHINDAgent(domain, certificateServiceAdapter, certificateStoreAdapter, trustSettingsStoreAdapter);
            
            agentMap.put(domain, agent);
        
            log.debug("initializing agent for domain " + domain);
        }
    }    

    public Message encryptMessage(Message message) {
        log.debug("encrypting message id: " + message.getMessageId() + " from: " + message.getFrom() + " to: " + message.getTo());
        
        String domain = message.getFrom().getDomain();
        
        NHINDAgent agent = agentMap.get(domain);
        
        if (agent == null) {
            throw new AgentException("Encryption failed, no NHINDAgent found for domain: " + domain);
        }
        
        String encryptedContent = agent.processOutgoing(new String(message.getData()));
        
        message.setData(encryptedContent.getBytes());
        return message;
    }

    public Message decryptMessage(Message message) {
        log.debug("decrypting message id: " + message.getMessageId() + " from: " + message.getFrom() + " to: " + message.getTo());
        
        String domain = message.getTo().getDomain();
        
        NHINDAgent agent = agentMap.get(domain);
        
        if (agent == null) {
            throw new AgentException("Decryption failed, no NHINDAgent found for domain: " + domain);
        }
        
        String decryptedContent = agent.processIncoming(new String(message.getData()));
        
        message.setData(decryptedContent.getBytes());
        return message;
    }
}
