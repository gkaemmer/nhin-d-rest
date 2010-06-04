package org.nhindirect.platform.auth;

import java.util.ArrayList;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * NhinDirectTrustedUserDetailsService is a Spring Security implementation of UserDetailsService
 * that will authenticate all users that have trusted x509 certs. The users are assigned a role
 * (authority) configurable with the assumedRole property.
 */
public class NhinDirectTrustedUserDetailsService implements UserDetailsService {

    private String assumedRole;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl(assumedRole));

        User user = new User(username, "", true, true, true, true, authorities);
        return user;
    }

    public void setAssumedRole(String role) {
        assumedRole = role;
    }
}
