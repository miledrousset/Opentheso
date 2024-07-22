/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author miled.rousset
 */
public class ConnexionTest {

    public ConnexionTest(){
        
    }
    
    
    private HikariDataSource openConnexionPool() {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(100);
        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");

        // Pactols2
        config.addDataSourceProperty("user", "opentheso");
        config.addDataSourceProperty("password", "opentheso");
        config.addDataSourceProperty("databaseName", "opentheso");
        config.addDataSourceProperty("portNumber", "5432");
        config.addDataSourceProperty("serverName", "localhost");

        HikariDataSource poolConnexion1 = new HikariDataSource(config);
        return poolConnexion1;

    }

    public HikariDataSource getConnexionPool(){
        return openConnexionPool();
    }
    
}
