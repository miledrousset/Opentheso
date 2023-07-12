package fr.cnrs.opentheso.utils;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import fr.cnrs.opentheso.ws.openapi.helper.DataHelper;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;


@Slf4j
@ApplicationScoped
public class LiquibaseInitializer {

    @PostConstruct
    public void initialize() throws SQLException, LiquibaseException {
        System.out.println("Début de l'initialisation de la base de donnée avec liquibase !");
        // Chargement du pilote JDBC
        Connection connection = DataHelper.connect().getConnection();

        // Création de l'objet Liquibase
        Liquibase liquibase = new Liquibase(
                "changelog/db.changelog.xml",
                new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
        );

        // Exécution des changements de schéma
        liquibase.update("");

        // Fermeture de la connexion à la base de données
        connection.close();
        System.out.println("Fin de l'initialisation de la base de donnée avec liquibase !");
    }
}
