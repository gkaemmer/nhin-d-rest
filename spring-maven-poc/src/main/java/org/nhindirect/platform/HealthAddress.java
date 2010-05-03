package org.nhindirect.platform;

public class HealthAddress {

    private String domain;
    private String endpoint;
    
    public HealthAddress(String domain, String endpoint) {
        this.domain = domain;
        this.endpoint = endpoint;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String toEmailAddress() {
        return endpoint + "@" + domain;
    }
    
    public String toString() {
        return toEmailAddress();
    }
    
    /**
     * Brain dead implementation that doesn't handle malformed addresses 
     */
    public static HealthAddress parseEmailAddress(String email) {
        String[] parts = email.split("@");
        return new HealthAddress(parts[1], parts[0]); 
    }

    /**
     * Brain dead implementation that doesn't handle malformed addresses 
     */
    public static HealthAddress parseUrnAddress(String urn) {
        String[] parts = urn.split(":");
        return new HealthAddress(parts[2], parts[3]); 
    }
    
}
