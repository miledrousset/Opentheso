/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.tools.StringPlus;

/**
 *
 * @author Miled.Rousset
 */
public class PreferencesHelper {
    
    /**
     * permet de retourner le code JavaScript de GoogleAnalytics
     * @param ds
     * @return 
     */
    public String getCodeGoogleAnalytics(HikariDataSource ds) {
        String codeAnalytics = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT googleanalytics FROM info" ;
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        codeAnalytics = resultSet.getString("googleanalytics");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codeAnalytics;
    } 
    
    /**
     * Permet de mettre à jour le code javaScript de GoogleAnalytics 
     * @param ds
     * @param codeJavaScript
     * @return 
     */
    public boolean setCodeGoogleAnalytics(HikariDataSource ds, String codeJavaScript){
        Connection conn;
        Statement stmt;
        boolean status= false;
        StringPlus stringPlus = new StringPlus();
        codeJavaScript = stringPlus.addQuotes(codeJavaScript);

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "update info set googleanalytics = '" + codeJavaScript + "'";
                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;        
    }     
    
    /**
     * permet de retourner les préferences d'un thésaurus 
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public NodePreference getThesaurusPreferences(HikariDataSource ds, String idThesaurus) {
        NodePreference np = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM preferences where id_thesaurus = '" + idThesaurus + "'";
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        np = new NodePreference();
                        np.setSourceLang(resultSet.getString("source_lang"));
                        np.setIdentifierType(resultSet.getInt("identifier_type"));
                        np.setPreferredName(resultSet.getString("preferredname")); 
                        np.setAuto_expand_tree(resultSet.getBoolean("auto_expand_tree"));
                        
                        // Ark
                        np.setUseArk(resultSet.getBoolean("use_ark"));
                        np.setServeurArk(resultSet.getString("server_ark"));
                        np.setUriArk(resultSet.getString("uri_ark"));                        
                        np.setIdNaan(resultSet.getString("id_naan"));
                        np.setPrefixArk(resultSet.getString("prefix_ark"));
                        np.setUserArk(resultSet.getString("user_ark"));
                        np.setPassArk(resultSet.getString("pass_ark"));
                        np.setGenerateHandle(resultSet.getBoolean("generate_handle"));
                        
                        // Handle
                        np.setUseHandle(resultSet.getBoolean("use_handle"));
                        np.setUserHandle(resultSet.getString("user_handle"));
                        np.setPassHandle(resultSet.getString("pass_handle"));
                        np.setPathKeyHandle(resultSet.getString("path_key_handle"));
                        np.setPathCertHandle(resultSet.getString("path_cert_handle"));
                        np.setUrlApiHandle(resultSet.getString("url_api_handle"));                        
                        np.setPrefixIdHandle(resultSet.getString("prefix_handle"));
                        np.setPrivatePrefixHandle(resultSet.getString("private_prefix_handle"));
                        
                        np.setPathImage(resultSet.getString("path_image"));
                        np.setDossierResize(resultSet.getString("dossier_resize"));
                        np.setBddActive(resultSet.getBoolean("bdd_active"));
                        np.setBddUseId(resultSet.getBoolean("bdd_use_id"));
                        np.setUrlBdd(resultSet.getString("url_bdd"));
                        np.setUrlCounterBdd(resultSet.getString("url_counter_bdd"));
                        np.setZ3950actif(resultSet.getBoolean("z3950actif"));
                        np.setCollectionAdresse(resultSet.getString("collection_adresse"));
                        np.setNoticeUrl(resultSet.getString("notice_url"));
                        np.setUrlEncode(resultSet.getString("url_encode"));
                        np.setPathNotice1(resultSet.getString("path_notice1"));
                        np.setPathNotice2(resultSet.getString("path_notice2"));
                        np.setCheminSite(resultSet.getString("chemin_site"));
                        np.setWebservices(resultSet.getBoolean("webservices"));
                        np.setOriginalUri(resultSet.getString("original_uri"));
                        np.setOriginalUriIsArk(resultSet.getBoolean("original_uri_is_ark"));
                        np.setOriginalUriIsHandle(resultSet.getBoolean("original_uri_is_handle"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return np;
    }
    
    /**
     * Permet de retourner la langue de préference pour un thésaurus 
     * @param ds
     * @param idThesourus
     * @return 
     */
    public String getWorkLanguageOfTheso(HikariDataSource ds, String idThesourus){
        String workLanguage = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select source_lang from preferences where" +
                            " id_thesaurus = '" + idThesourus + "'";
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        workLanguage = resultSet.getString("source_lang");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return workLanguage;        
    }
    
    /**
     * Permet de mettre à jour la langue source pour un thésaurus 
     * @param ds
     * @param idLang
     * @param idTheso
     * @return 
     */
    public boolean setWorkLanguageOfTheso(HikariDataSource ds, String idLang, String idTheso){
        Connection conn;
        Statement stmt;
        boolean status= false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "update preferences set source_lang = '" + idLang + "'" 
                            + " where id_thesaurus = '" + idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;        
    }    
    
    public boolean isWebservicesOn(HikariDataSource ds, String idThesaurus) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        
        boolean status = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT webservices FROM preferences where id_thesaurus = '" + idThesaurus + "'";
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        status = resultSet.getBoolean("webservices");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }    
   
    /**
     * retourne l'id du thésaurus d'après son nom dans les préferances 
     * @param ds
     * @param thesoName
     * @return 
     */
    public String getIdThesaurusFromName(HikariDataSource ds, String thesoName){
        Connection conn;
        Statement stmt;
        String idTheso = null;
        ResultSet resultSet;        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_thesaurus from preferences "
                            + " where preferredname ilike '"
                            + thesoName + "'";
                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        idTheso = resultSet.getString("id_thesaurus");
                    }                    
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idTheso;
    }    
    
    /**
     * Cette fonction permet d'initialiser les préférences d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @param workLanguage
     */
    public void initPreferences(HikariDataSource ds, String idThesaurus,
            String workLanguage) {
        Connection conn;
        Statement stmt;
        try {
            // Get connection from pool
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "insert into preferences "
                            + "(id_thesaurus,source_lang, preferredname)"
                            + " values"
                            + " ('" + idThesaurus + "',"
                            + "'" + workLanguage + "'," 
                            + "'" + idThesaurus + "')";
                    stmt.executeUpdate(query);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Permet de mettre à jour toutes les préférence
     *
     * @param ds
     * @param np
     * @param idThesaurus
     * @return
     */
    public boolean updateAllPreferenceUser(HikariDataSource ds, NodePreference np, String idThesaurus) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        StringPlus stringPlus = new StringPlus();
        np = normalizeDatas(np);
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "update preferences set "
                            + "source_lang='" + stringPlus.convertString(np.getSourceLang()) + "'"
                         //   + ", nb_alert_cdt='" + np.getNbAlertCdt() + "'"
                         //   + ", alert_cdt='" + np.isAlertCdt() + "'"
                            + ", identifier_type='" + np.getIdentifierType() + "'"
                            + ", preferredname='" + np.getPreferredName()+ "'"
                            + ", auto_expand_tree='" + np.isAuto_expand_tree() + "'"
                            
                            // Ark
                            + ", use_ark='" + np.isUseArk() + "'"
                            + ", server_ark='" + stringPlus.convertString(np.getServeurArk()) + "'"
                            + ", uri_ark='" + stringPlus.convertString(np.getUriArk()) + "'"
                            + ", id_naan='" + np.getIdNaan() + "'"
                            + ", prefix_ark ='" + np.getPrefixArk() + "'"                            
                            + ", user_ark='" + np.getUserArk() + "'"
                            //+ ", pass_ark='" + MD5Password.getEncodedPassword(np.getPassArk()) + "'"
                            + ", pass_ark='" + np.getPassArk() + "'"
                             + ", generate_handle='" + np.isGenerateHandle() + "'"
                            

                            // Handle
                            + ", use_handle = '" + np.isUseHandle() + "'"
                            + ", user_handle = '" + np.getUserHandle() + "'"
                            + ", pass_handle = '" + np.getPassHandle() + "'"
                            + ", path_key_handle = '" + np.getPathKeyHandle() + "'"
                            + ", path_cert_handle = '" + np.getPathCertHandle() + "'"
                            + ", url_api_handle = '" + np.getUrlApiHandle() + "'"
                            + ", prefix_handle = '" + np.getPrefixIdHandle() + "'"
                            + ", private_prefix_handle = '" + np.getPrivatePrefixHandle() + "'"
                            
                            + ", path_image='"+stringPlus.convertString(np.getPathImage()) + "'"
                            + ", dossier_resize='"+stringPlus.convertString(np.getDossierResize()) + "'"
                            + ", bdd_active='"+np.isBddActive() + "'"
                            + ", bdd_use_id='"+np.isBddUseId() + "'"
                            + ", url_bdd='"+stringPlus.convertString(np.getUrlBdd()) + "'"
                            + ", url_counter_bdd='"+stringPlus.convertString(np.getUrlCounterBdd()) + "'"
                            + ", z3950actif='"+np.isZ3950actif()+ "'"
                            + ", collection_adresse='"+stringPlus.convertString(np.getCollectionAdresse()) + "'"
                            + ", notice_url='"+ stringPlus.convertString(np.getNoticeUrl())+ "'"
                            + ", url_encode='"+stringPlus.convertString(np.getUrlEncode()) + "'"
                            + ", path_notice1='"+stringPlus.convertString(np.getPathNotice1()) + "'"
                            + ", path_notice2='"+stringPlus.convertString(np.getPathNotice2()) + "'"
                            + ", chemin_site='"+stringPlus.convertString(np.getCheminSite())+"'"
                            + ", webservices='" + np.isWebservices()+"'"
                            + ", original_uri='" + stringPlus.convertString(np.getOriginalUri())+"'"
                            + ", original_uri_is_ark=" + np.isOriginalUriIsArk()
                            + ", original_uri_is_handle=" + np.isOriginalUriIsHandle()
                            
                            + " WHERE"
                            + " id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;
    }
    
    /**
     * Permet de mettre à jour toutes les préférence
     *
     * @param ds
     * @param np
     * @param idThesaurus
     * @return
     */
    public boolean addPreference(HikariDataSource ds, NodePreference np, String idThesaurus) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        StringPlus stringPlus = new StringPlus();
        np = normalizeDatas(np);
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "insert into preferences("
                            + "id_thesaurus, source_lang, identifier_type, path_image,"
                            + " dossier_resize, bdd_active, bdd_use_id, url_bdd,"
                            + " url_counter_bdd, z3950actif, collection_adresse,"
                            + " notice_url, url_encode, path_notice1, path_notice2," 
                            + " chemin_site, webservices, use_ark, server_ark, uri_ark," 
                            + " id_naan, prefix_ark, user_ark, pass_ark, generate_handle,"
                            + " use_handle,"
                            + " user_handle, pass_handle, path_key_handle, path_cert_handle,"
                            + " url_api_handle, prefix_handle, private_prefix_handle, preferredname, auto_expand_tree, original_uri,"
                            + " original_uri_is_ark, original_uri_is_handle)"
 
                            + " values('" + idThesaurus + "'"
                            + ",'" + stringPlus.convertString(np.getSourceLang()) + "'"
                            + ",'" + np.getIdentifierType() + "'"
                            + ",'"+stringPlus.convertString(np.getPathImage()) + "'"                            
                            + ",'"+stringPlus.convertString(np.getDossierResize()) + "'"
                            + ",'"+np.isBddActive() + "'"
                            + ",'"+np.isBddUseId() + "'"
                            + ",'"+stringPlus.convertString(np.getUrlBdd()) + "'"
                            + ",'"+stringPlus.convertString(np.getUrlCounterBdd()) + "'"
                            + ",'"+np.isZ3950actif()+ "'"
                            + ",'"+stringPlus.convertString(np.getCollectionAdresse()) + "'"
                            + ",'"+ stringPlus.convertString(np.getNoticeUrl())+ "'"
                            + ",'"+stringPlus.convertString(np.getUrlEncode()) + "'"
                            + ",'"+stringPlus.convertString(np.getPathNotice1()) + "'"
                            + ",'"+stringPlus.convertString(np.getPathNotice2()) + "'"
                            + ",'"+stringPlus.convertString(np.getCheminSite())+"'"
                            + ",'" + np.isWebservices()+"'"                            
                            
                            
                            // Ark
                            + ",'" + np.isUseArk() + "'"
                            + ",'" + stringPlus.convertString(np.getServeurArk()) + "'"
                            + ",'" + stringPlus.convertString(np.getUriArk()) + "'"
                            + ",'" + np.getIdNaan() + "'"
                            + ",'" + np.getPrefixArk() + "'"                            
                            + ",'" + np.getUserArk() + "'"
                            //+ ", pass_ark='" + MD5Password.getEncodedPassword(np.getPassArk()) + "'"
                            + ",'" + np.getPassArk() + "'"
                            + ",'" + np.isGenerateHandle() + "'"
                            
                            // Handle
                            + ",'" + np.isUseHandle() + "'"
                            + ",'" + np.getUserHandle() + "'"
                            + ",'" + np.getPassHandle() + "'"
                            + ",'" + np.getPathKeyHandle() + "'"
                            + ",'" + np.getPathCertHandle() + "'"
                            + ",'" + np.getUrlApiHandle() + "'"
                            + ",'" + np.getPrefixIdHandle() + "'"
                            + ",'" + np.getPrivatePrefixHandle() + "'"
                            
                            + ",'" + np.getPreferredName() + "'"
                            + "," + np.isAuto_expand_tree()                            
                            + ",'" + np.getOriginalUri() + "'"
                            + "," + np.isOriginalUriIsArk()
                            + "," + np.isOriginalUriIsHandle()                            
                            + ")";
                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;
    }    
    
    /**
     * permet de nettoyer les "/" et préparer les paramètres correctement 
     * @param nodePreference
     * @return 
     */
    private NodePreference normalizeDatas(NodePreference nodePreference) {

        // vérification des "/" à la fin 
        if(!nodePreference.getCheminSite().isEmpty()) {
            if(!nodePreference.getCheminSite().substring(nodePreference.getCheminSite().length() - 1, nodePreference.getCheminSite().length()).equalsIgnoreCase("/")) {
                nodePreference.setCheminSite(nodePreference.getCheminSite() + "/");
            }
        }
        if(!nodePreference.getServeurArk().isEmpty()) {
            if(!nodePreference.getServeurArk().substring(nodePreference.getServeurArk().length() - 1, nodePreference.getServeurArk().length()).equalsIgnoreCase("/")) {
                nodePreference.setServeurArk(nodePreference.getServeurArk() + "/");
            }
        }
        if(!nodePreference.getPathImage().isEmpty()) {
            if(!nodePreference.getPathImage().substring(nodePreference.getPathImage().length() - 1, nodePreference.getPathImage().length()).equalsIgnoreCase("/")) {
                nodePreference.setPathImage(nodePreference.getPathImage() + "/");
            }
        }         
        return nodePreference;
    } 
    
    

    

}
