package org.nhindirect.platform;


/**
 * Simple non-robust implementation of the relevant portions of the
 * NHIN Direct Addressing specification.
 */
public class HealthAddress implements Comparable<HealthAddress> {

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

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HealthAddress other = (HealthAddress) obj;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (endpoint == null) {
            if (other.endpoint != null)
                return false;
        } else if (!endpoint.equals(other.endpoint))
            return false;
        return true;
    }

    public int compareTo(HealthAddress o) {
        return this.toString().compareTo(o.toString());
    }
    
}
