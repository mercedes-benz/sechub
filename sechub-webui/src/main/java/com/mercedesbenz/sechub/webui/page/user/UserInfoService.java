package com.mercedesbenz.sechub.webui.page.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {
    
    public String getUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    public String getEmailAddress() {
        /* FIXME Albert Tregnaghi, 2024-02-28:implement */
        return getUserId()+"_calculated@example.org";
    }
    
    private Authentication getAuthentication() {
        SecurityContext context = getContext();
        if (context == null) {
            return null;
        }
        return getContext().getAuthentication();
    }

    private SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }
}
