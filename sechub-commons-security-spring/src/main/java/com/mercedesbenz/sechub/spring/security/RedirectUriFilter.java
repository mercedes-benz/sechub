package com.mercedesbenz.sechub.spring.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class RedirectUriFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String redirectUri = httpRequest.getParameter("redirectUri");

        if (redirectUri != null && !redirectUri.isBlank()) {
            HttpSession session = httpRequest.getSession();
            session.setAttribute("redirectUri", redirectUri);
        }

        chain.doFilter(request, response);
    }
}
