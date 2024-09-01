/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.exports.privatesdatas.tables.Table;





/**
 *
 * @author antonio.perez
 */
public class BaseDeDoneesHelper implements Serializable {

    /**
     * Paremetres fixes
     */

    public String version_bdd;
    public String versionBddCurrent;
    public String version_Opentheso;
    public ArrayList<BaseDeDoneesHelper> info;
    
    /**
     * Permet de savoir si le nom de le utilisateur déjà exist o pas
     * @param ds
     * @param nomAdmin
     * @return 
     */
    public boolean isUserExist(HikariDataSource ds, String nomAdmin)
    {
        ResultSet resultSet;
        try {
            Connection conn = ds.getConnection();
            Statement stmt;
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT usename from pg_user";
                    resultSet = stmt.executeQuery(query);
                    while(resultSet.next())
                    {
                        if(resultSet.getString("usename").equals(nomAdmin)) return true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, sqle);
                return false;
        }
        return false;
    }
    /**
     * Permet de savoir si le nom du BDD déjà exist o pas
     * @param ds
     * @param databasename
     * @return 
     */
    public boolean isBddExist(HikariDataSource ds, String databasename)
    {
        ResultSet resultSet;
        try {
            Connection conn = ds.getConnection();
            Statement stmt;
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT datname from pg_database;";
                    resultSet = stmt.executeQuery(query);
                    while(resultSet.next())
                    {
                        if(resultSet.getString("datname").equals(databasename)) return true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, sqle);
                return false;
        } 
        return false;
    }
    /**
     * 
     * @param ds
     * @param nomAdmin
     * @param pass
     * @return 
     */
    public boolean createUser(HikariDataSource ds, String nomAdmin, String pass)
    {
        try {
            Connection conn = ds.getConnection();
            Statement stmt;
            try {
                stmt = conn.createStatement();
                try {
                    String query = "CREATE USER "+ nomAdmin
                            +" PASSWORD '"+ pass+"';";
                    stmt.executeUpdate(query);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, sqle);
                return false;
        }
        return true;
    }
    
    /**
     * Funtion principal de la class, creation de toutes les statements et de le
     * connections faire la BDD avec le nom proporcioné, inyection de les
     * tables, MAJ, et données de la basse;
     *
     * @param ds
     * @param DBname
     * @param user
     * @return
     */
    public boolean createBdD(HikariDataSource ds, String DBname, String user) {
        if (DBname != null) {
            Statement stmt;
            try {
                Connection conn = ds.getConnection();
                stmt = conn.createStatement();
                try {
                    String query = "create Database " + DBname +" OWNER "+ user;
                    stmt.executeUpdate(query);
                } finally {
                    stmt.close();
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    
//    public boolean changerPermis(HikariDataSource ds, String nomAdmin, String databaseName)
//    {
//        Statement stmt;
//            try {
//                Connection conn = ds.getConnection();
//                stmt = conn.createStatement();
//                try {
//                    String query = "GRANT ALL PRIVILEGES ON DATABASE "+ databaseName+" to "+nomAdmin;
//                    stmt.executeUpdate(query);
//                    query = "ALTER DATABASE "+ databaseName+" owner to "+nomAdmin;
//                    stmt.execute(query);
//                } finally {
//                    stmt.close();
//                    conn.close();
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
//                return false;
//            }
//    return true;
//    }
    
    
    
    
    
    /**
     * Funtion que permet de avoir une connection avec la BDD pour pouvoir faire
     * la nouvelle BDD; Nous donne un type connection
     *
     * @param ds
     * @param inputStream
     * @param userName
     * @return
     */
    public boolean insertDonnees(HikariDataSource ds, InputStream inputStream, String userName) {
        Statement stmt;
        String scriptBdd;
        try {
            Connection conn = ds.getConnection();
            stmt = conn.createStatement();
            scriptBdd = prepareScript(inputStream,userName);// tout la information de touts le tables et "Insert into languages_iso639"
            try {
                stmt.execute(scriptBdd);
            } finally {
                stmt.close();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Avec le fichier de données nous prendons tout pour faire le la creation
     * de tout les tables, se returne une String que se utilises dans la funtion
     * principal.
     *
     * @param inputstream
     * @param userName
     * @return
     */
    public String prepareScript(InputStream inputstream, String userName) {
        String line;
        String retorno = "";
        boolean sault = true;
        BufferedReader bf;

        try {
            bf = new BufferedReader(new InputStreamReader(inputstream, "UTF8"));
            while ((line = bf.readLine()) != null) {
                if(line.contains("-- version=") && sault)
                {
                    versionBddCurrent = line.substring(line.indexOf("=")+1, line.length()).trim();
                    sault=false;
                }
                    if (!line.contains("--")) {//ne prendre pas le lignes que commence par -- (contiens)
                        if (!line.isEmpty()) {
                            if( line.contains("SET role = opentheso"));
                            {                            
                                line = line.replace("opentheso", userName);

                            }
                            retorno += line;
                            retorno += "\n";
                        }
                    }
        }
            bf.close();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BaseDeDoneesHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BaseDeDoneesHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
    
    /**
     * Permet de savoir qui est le propietaire de la BDD
     * que on l'envoy comme parametres
     * @param ds
     * @param dbName
     * @return 
     */
    public String getNameOwner(HikariDataSource ds, String dbName)
    {
        String owner="";
        
        try (Connection conn = ds.getConnection()) {
            
            try (Statement stmt = conn.createStatement()) {
                String query="SELECT pg_user.usename " 
                        + "FROM pg_catalog.pg_user,"
                        + "  pg_catalog.pg_database" 
                        + " WHERE pg_user.usesysid = pg_database.datdba" 
                        + "  and pg_database.datname ='"+ dbName+"'";
                
                try (ResultSet resultSet = stmt.executeQuery(query)) {
                    if (resultSet.next()){
                        owner=resultSet.getString("usename");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return owner;
    }
    /**
     * Rempli la tabla "info" dans la BDD pour garder l'information
     * On garde la version de Opetheso
     * @param ds
     * @param version_Opentheso
     * @return 
     */
    public boolean updateVersionOpentheso(HikariDataSource ds, String version_Opentheso)
    {
        boolean ok = true;
        Statement stmt;
        try {
            Connection conn = ds.getConnection();
            stmt = conn.createStatement();
            try {
                String query ="Update info set version_opentheso = '"+ version_Opentheso+"'"; 
                stmt.execute(query);
            } finally {
                stmt.close();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            ok= false;
        }
        return ok;
    }
    /**
     * Permet de avoir la derniere version disponible
     * @param ds
     * @return 
     */
    public boolean updateVersionBdd(HikariDataSource ds)
    {
        Statement stmt;
        try {
            Connection conn = ds.getConnection();
            stmt = conn.createStatement();
            try {
                String query ="Update info set version_bdd = '"+ versionBddCurrent+"'";
                stmt.execute(query);
            } finally {
                stmt.close();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    /**
     * Permet de inserté la version instalée dans la BDD 
     * S'utilise seulement pour la premiere instalation
     * @param ds
     * @return 
     */
    public boolean insertVersionBdd(HikariDataSource ds)
    {
        Statement stmt;
        boolean status = false;
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query ="INSERT INTO info VALUES( 'xyz','"+ versionBddCurrent+"');";
                    stmt.execute(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, sqle);
        }         
        return status;
    }
        
    /**
     * Permet de savoir l'information 
     * version_Opentheso versionbdd, versionBddCurrent
     * @param ds
     * @return 
     */
    public ArrayList<BaseDeDoneesHelper> info_out(HikariDataSource ds)
    {
        BaseDeDoneesHelper outinfo = new BaseDeDoneesHelper();
        info = new ArrayList<>();
        Statement stmt;
        ResultSet resultSet;
        chercherVersionBdd(ds);
           
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Select * from info ;";
                    resultSet =stmt.executeQuery(query);
                    while(resultSet.next())
                    {
                        outinfo.setVersion_Opentheso(resultSet.getString("version_Opentheso"));
                        outinfo.setVersion_bdd(resultSet.getString("version_bdd"));
                        outinfo.setVersionBddCurrent(versionBddCurrent);
                        info.add(outinfo);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, sqle);
        }            
            
        return info;
    }
    /**
     * on recupere la derniere version disponible dans le fichier
     * @param ds 
     */
    public void chercherVersionBdd(HikariDataSource ds)
    {
        InputStream inputStream = this.getClass().getResourceAsStream("/install/opentheso_current.sql");
        boolean sault = true;
        BufferedReader bf;
        String line;
        
        
        try {
            bf = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
            while ((line = bf.readLine()) != null && sault) {
                if(line.contains("-- version=") && sault)
                {
                    versionBddCurrent = line.substring(line.indexOf("=")+1, line.length()).trim();
                    sault=false;
                }
            }
            bf.close();
        }catch(Exception e)
        {

        }
    }

    /**
     * Cette fonction permet de réupérer la version du logiciel Opentheso
     * @param ds
     * @return 
     */
    public String getVersionOfOpentheso(HikariDataSource ds) {
        Statement stmt;
        ResultSet resultSet;
        String version = "";
        
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Select version_opentheso from info";
                    resultSet =stmt.executeQuery(query);
                    if(resultSet.next()) {
                        version = resultSet.getString("version_opentheso");
                    }
                    resultSet.close();

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return version;
    }
    
    
    public String getVersion_bdd() {
        return version_bdd;
    }

    public void setVersion_bdd(String version_bdd) {
        this.version_bdd = version_bdd;
    }

    public String getVersion_Opentheso() {
        return version_Opentheso;
    }

    public void setVersion_Opentheso(String version_Opentheso) {
        this.version_Opentheso = version_Opentheso;
    }

    public String getVersionBddCurrent() {
        return versionBddCurrent;
    }

    public void setVersionBddCurrent(String versionBddCurrent) {
        this.versionBddCurrent = versionBddCurrent;
    }
    
    
}
