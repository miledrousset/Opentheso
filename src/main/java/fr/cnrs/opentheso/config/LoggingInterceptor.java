package fr.cnrs.opentheso.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!request.getRequestURL().toString().contains("api/info/list")) {
            Map<String, String[]> parameterMap = request.getParameterMap(); // Récupérer tous les paramètres
            String parameters = parameterMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                    .collect(Collectors.joining(", "));
            log.info("Request URL: {}, Method: {}, Query Parameters: {}, IP: {}", request.getRequestURL(), request.getMethod(), parameters, request.getRemoteAddr());
        }
        return true; // Continue la chaîne des handlers
    }
}
