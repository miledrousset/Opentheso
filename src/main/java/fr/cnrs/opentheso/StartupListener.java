package fr.cnrs.opentheso;

import fr.cnrs.opentheso.utils.LiquibaseInitializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LiquibaseInitializer initializer = new LiquibaseInitializer();
        initializer.initialize();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Logique de nettoyage ou d'arrêt si nécessaire
    }
}
