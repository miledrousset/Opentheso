package fr.cnrs.opentheso.utils;

import javax.annotation.PostConstruct;

import fr.cnrs.opentheso.ws.openapi.helper.DataHelper;
import java.io.Serializable;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import javax.enterprise.context.SessionScoped;


@Slf4j
@SessionScoped
public class LiquibaseInitializer implements Serializable {

    @PostConstruct
    public void initialize() {
        try {
            System.out.println("– *********************************************************************");
            System.out.println("– Update Database Script Liquibase");
            System.out.println("– *********************************************************************");
            // Création de l'objet Liquibase
            try (Connection connection = DataHelper.connect().getConnection()) {
                // Création de l'objet Liquibase
                Liquibase liquibase = new Liquibase(
                        "changelog/db.changelog.xml",
                        new ClassLoaderResourceAccessor(),
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
                );  // Exécution des changements de schéma
                liquibase.update("");
                // Fermeture de la connexion à la base de données
            }
            System.out.println("– *********************************************************************");
            System.out.println("– End Database Script Liquibase");
            System.out.println("– *********************************************************************");
        } catch (SQLException | LiquibaseException ex) {
            System.out.println("– *********************************************************************");
            System.out.println("Erreur Liquibase : " + ex.getMessage());
            System.out.println("– *********************************************************************");            
        }
    }
}
