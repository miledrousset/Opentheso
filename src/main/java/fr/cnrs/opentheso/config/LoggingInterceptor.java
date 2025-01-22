package fr.cnrs.opentheso.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Request URL: {}, Method: {}, IP: {}", request.getRequestURL(), request.getMethod(), request.getRemoteAddr());
        return true; // Continue la chaîne des handlers
    }
}
