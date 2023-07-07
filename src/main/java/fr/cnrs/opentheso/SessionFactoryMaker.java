package fr.cnrs.opentheso;

import fr.cnrs.opentheso.entites.Release;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SessionFactoryMaker {

    private static SessionFactory factory;

    private static void configureFactory() {
        try {
            factory = new Configuration()
                    .addAnnotatedClass(Release.class)
                    .configure()
                    .buildSessionFactory();
        } catch (Exception ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static org.hibernate.SessionFactory getFactory() {
        if (factory == null) {
            configureFactory();
        }

        return factory;
    }

}
