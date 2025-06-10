package fr.cnrs.opentheso.config;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.users.UserRoleGroupService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.*;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final KeycloakProperties keycloakProps;
    private final UserRoleGroupService userRoleGroupService;

    public SecurityConfig(KeycloakProperties keycloakProps, UserRoleGroupService userRoleGroupService) {
        this.keycloakProps = keycloakProps;
        this.userRoleGroupService = userRoleGroupService;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionFixation().none() // évite que la session soit perdue après login
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login","/logout", "/oauth2/**", "/javax.faces.resource/**", "/").permitAll()
                        .anyRequest().permitAll() // tout est public
                )
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String logoutUrl = String.format(
                                    "%s/realms/%s/protocol/openid-connect/logout?client_id=%s&redirect_uri=%s",
                                    keycloakProps.getServerUrl(),
                                    keycloakProps.getRealm(),
                                    keycloakProps.getClientId(),
                                    keycloakProps.getPostLogoutRedirectUri()
                            );
                            response.sendRedirect(logoutUrl);
                        })
                )
                //   .csrf(csrf -> csrf.disable())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(request -> request.getServletPath().endsWith(".xhtml"))
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler()) // <-- important
                        .failureHandler(authenticationFailureHandler()) // <-- important
                        //.defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(this.userAuthoritiesMapper())
                        )
                );
        return http.build();
    }


    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");

            log.info("Authentification réussie. Email : {}", email);

            ///******* //////
            ///  ici, je n'arrive pas à récupérer le bean currentUser pour charger les données le l'utilisateur qui arrive de KeyCloak ///
            /// ****** ////
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            CurrentUser currentUser;
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession(false); // false pour ne pas créer de session si elle n'existe pas

                if (session != null) {
                    // Le nom du bean @Named est "currentUser" par défaut
                    currentUser = (CurrentUser) session.getAttribute("currentUser");
                }
            }
            userRoleGroupService.findUserByEmail(email).ifPresentOrElse(localUser -> {
                try {
                  //  userSessionBridgeService.updateCurrentUserInSession(
                   //         localUser.getMail(), true
                   // );
                } catch (Exception e) {
                    log.error("Erreur lors de l'initialisation de currentUser", e);
                    try {
                        response.sendRedirect("/authFailure");
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                    return;
                }
                log.info("Utilisateur {} transféré dans currentUser", localUser.getUsername());
            }, () -> {
                log.warn("Aucun utilisateur trouvé pour l’email : {}", email);
            });

            response.sendRedirect("/");
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            // Rediriger vers une page d'erreur JSF
            response.sendRedirect("/authFailure");
        };
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            for (GrantedAuthority authority : authorities) {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    Map<String, Object> attributes = oidcUserAuthority.getAttributes();
                    Map<String, Object> realmAccess = (Map<String, Object>) attributes.get("realm_access");
                    if (realmAccess != null) {
                        List<String> roles = (List<String>) realmAccess.get("roles");
                        for (String role : roles) {
                            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                        }
                    }
                }
            }

            return mappedAuthorities;
        };
    }
}
