package org.nhindirect.platform;

import java.util.Set;

public interface DomainService {

    public Set<String> getDomains();
    public boolean isLocalAddress(HealthAddress address);
    public boolean isValidAddressForUser(String user, HealthAddress address);
    
}
