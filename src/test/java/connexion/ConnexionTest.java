package connexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


/**
 *
 * @author miled.rousset
 */
public class ConnexionTest {

    private HikariDataSource openConnexionPool() {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(100);
        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");

        // Pactols2
        config.addDataSourceProperty("user", "opentheso");
        config.addDataSourceProperty("password", "opentheso");
        config.addDataSourceProperty("databaseName", "mom");
        config.addDataSourceProperty("portNumber", "5433");
        config.addDataSourceProperty("serverName", "localhost");

        HikariDataSource poolConnexion1 = new HikariDataSource(config);
        return poolConnexion1;

    }

    public HikariDataSource getConnexionPool(){
        return openConnexionPool();
    }
    
}
