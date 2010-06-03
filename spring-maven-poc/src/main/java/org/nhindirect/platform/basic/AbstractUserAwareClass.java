package org.nhindirect.platform.basic;

import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class AbstractUserAwareClass {

    protected UserDetails getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDetails user = null;

        if (principal instanceof UserDetails) {
            user = (UserDetails) principal;
        }

        return user;
    }

    protected boolean hasRole(String role) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean hasRole = false;

        if (principal instanceof UserDetails) {
            UserDetails user = (UserDetails) principal;
            hasRole = user.getAuthorities().contains(new GrantedAuthorityImpl(role));
        }

        return hasRole;
    }
}
