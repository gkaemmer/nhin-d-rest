package org.nhindirect.platform.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.nhindirect.platform.DomainService;
import org.nhindirect.platform.HealthAddress;

public class BasicDomainService implements DomainService {

    private String domainPropsFilename;
    
    private Set<String> domains;
    private Map<HealthAddress, Set<String>> addressUsers;
    private Map<String, HealthAddress> domainAddresses;
    private Map<String, Set<HealthAddress>> userAddresses;
    
    public void init() throws IOException {        
        domains = new TreeSet<String>();
        addressUsers = new TreeMap<HealthAddress, Set<String>>();
        domainAddresses = new TreeMap<String, HealthAddress>();
        userAddresses = new TreeMap<String, Set<HealthAddress>>();
        
        
        BufferedReader in = new BufferedReader(new FileReader(domainPropsFilename));
        
        Properties props = new Properties();
        props.load(in);
        in.close();
        
        Set<String> names = props.stringPropertyNames();
        
        for (String propName : names) {
            HealthAddress address = HealthAddress.parseEmailAddress(propName);
            String propValue = props.getProperty(propName);
            Set<String> users = getUserSet(propValue);
            
            domains.add(address.getDomain());            
            
            if (addressUsers.containsKey(address)) {
                addressUsers.get(address).addAll(users);
            } else {
                addressUsers.put(address, users);
            }
                        
            domainAddresses.put(address.getDomain(), address);
            
            for (String user : users) {
                if (userAddresses.containsKey(user)) {
                    userAddresses.get(user).add(address);
                } else {
                    Set<HealthAddress> addressSet = new TreeSet<HealthAddress>();
                    addressSet.add(address);
                    userAddresses.put(user, addressSet);
                }                
            }            
        }
    }
    
    private Set<String> getUserSet(String userprop) {
        String[] users = userprop.split(",");
        
        Set<String> userSet = new TreeSet<String>();
        
        for (String user : users) {
            String filteredUser = user.trim();
            if (filteredUser.length() > 0) {
                userSet.add(filteredUser);
            }
        }
        
        return userSet;
    }
    
    public Set<String> getDomains() {
        return domains;
    }

    public boolean isLocalAddress(HealthAddress address) {
        return (address != null) && addressUsers.containsKey(address);
    }

    public boolean isValidAddressForUser(String user, HealthAddress address) {
        Set<HealthAddress> addresses = userAddresses.get(user);
        
        return (user != null) && (address != null) && (addresses != null) && addresses.contains(address); 
    }

    public String getDomainPropsFilename() {
        return domainPropsFilename;
    }

    public void setDomainPropsFilename(String domainPropsFilename) {
        this.domainPropsFilename = domainPropsFilename;
    }

}
