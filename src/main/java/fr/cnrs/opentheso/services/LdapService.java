package fr.cnrs.opentheso.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;


@Service
public class LdapService {

    private final static String LDAPS_PROTOCOL = "ldaps://";

    @Value("${ldap.server.url}")
    private String serverUrl;

    @Value("${ldap.security.authentication}")
    private String securityAuthentication;

    @Value("${ldap.initial.context.factory}")
    private String contextFactory;

    @Value("${ldap.key.store.path}")
    private String keyStorePath;

    @Value("${ldap.key.store.password}")
    private String keyStorePassword;

    @Value("${ldap.key.trust.path}")
    private String trueStorePath;

    @Value("${ldap.key.trust.password}")
    private String trueStorePassword;


    public boolean authentificationLdapCheck(String login, String password) {

        System.setProperty("javax.net.ssl.keyStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);

        System.setProperty("javax.net.ssl.trustStore", trueStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", trueStorePassword);

        //1er tentative de connexion avec la valeur de DN = ou=people,dc=huma-num,dc=fr
        try {
            new InitialDirContext(createEnvironnementBean("uid="+login+",ou=people,dc=huma-num,dc=fr", password));
            return true;
        } catch (Exception e) {
            System.out.println(">> Erreur 1 : " + e.getMessage());
        }

        //2eme tentative de connexion avec la valeur de DN = ou=people,ou=notre-dame-paris,dc=huma-num,dc=fr
        try {
            new InitialDirContext(createEnvironnementBean("uid="+login+",ou=people,ou=notre-dame-paris,dc=huma-num,dc=fr", password));
            return true;
        } catch (Exception e) {
            System.out.println(">> Erreur 2 : " + e.getMessage());}

        //3eme tentative de connexion sans la valeur de DN
        try {
            new InitialDirContext(createEnvironnementBean(login, password));
            return true;
        } catch (Exception e) {
            System.out.println(">> Erreur 3 : " + e.getMessage());}

        return false;
    }

    private Hashtable<String,String> createEnvironnementBean(String login, String password) {
        var env = new Hashtable <String,String>();
        env.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
        env.put(Context.SECURITY_PRINCIPAL, login);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        env.put(Context.PROVIDER_URL, LDAPS_PROTOCOL + serverUrl);
        return env;
    }

}
