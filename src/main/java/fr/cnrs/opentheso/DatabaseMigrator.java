package fr.cnrs.opentheso;

import fr.cnrs.opentheso.ws.openapi.helper.DataHelper;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;


public class DatabaseMigrator {

    public static void main(String[] args) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
