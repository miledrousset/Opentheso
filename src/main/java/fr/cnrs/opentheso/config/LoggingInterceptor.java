package fr.cnrs.opentheso.config;

import fr.cnrs.opentheso.services.ThesaurusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private final ThesaurusService thesaurusService;

    public LoggingInterceptor(ThesaurusService thesaurusService) {
        this.thesaurusService = thesaurusService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        /* permet de savoir si le thésaurus est privé, on interdit le téléchargement */
        if (request.getRequestURL().toString().contains("openapi/v1/thesaurus")){ //|| request.getRequestURL().toString().contains("api/all/theso")) {
            // reste à faire la condition pour le deuxième cas
            Boolean isPrivate = thesaurusService.isPrivateThesaurus(request.getRequestURL().toString().split("openapi/v1/thesaurus/")[1]);
            if(isPrivate == null || isPrivate == true) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"FORBIDDEN : the thesaurus is privated or not found\"}");
                response.getWriter().flush();
                return false;
            }
            return true;
        }

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
