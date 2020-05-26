/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class AccessThesaurusHelper {
   private final Log log = LogFactory.getLog(ThesaurusHelper.class);
    //private int nbThesoPublic;
    
    public AccessThesaurusHelper(){
        
        
    }
        
 //// restructuration de la classe AccessThesaurusHelper le 05/04/2018 #MR//////    
    
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////// Nouvelles fontions //////////////////////////////
 ////////////////////////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////// 
    
    /**
     * permet de mettre à jour la visibilité du thésaurus en publique ou privé 
     * 
     * @param ds
     * @param idTheso
     * @param isPrivate
     * @return 
     * #MR
     */
    public boolean updateVisibility(HikariDataSource ds,
            String idTheso,
            boolean isPrivate) {
        
        Statement stmt;
        boolean status = false;
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE thesaurus SET private = " +
                            isPrivate + 
                            " WHERE id_thesaurus='" + 
                            idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;                    
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            log.error("error while updating visibility of thesaurus = " +idTheso, ex);
        }
        return status;        
    }
    
  
    
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 //////// fin des nouvelles fontions ////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////      
    
    
    
    /**
     * getLisOfPrivateThesaurus
     * #JM 
     * doit récuperer la liste de thesaurus privé d'un utilisateur, pour une langue d'utilisation
     * On peut noter que dans la bdd la valeur de la conlonne private a true correspond à un thesaurus privé
     * 
     * @param ds
     * @param idLang
     * @param idUser
     * @return 
     */
    private HashMap getListPrivateThesaurusUser(HikariDataSource ds,String idLang, int idUser){
        Connection conn;
        PreparedStatement stmt;
        HashMap listTheoUser=new HashMap();
        if(ds == null) return listTheoUser;
        ResultSet rs;
        try{
            conn=ds.getConnection();
            try{
              
              
                String sql="SELECT thesaurus_label.id_thesaurus,thesaurus_label.title FROM thesaurus_label INNER JOIN( SELECT thesaurus.id_thesaurus FROM thesaurus INNER JOIN  user_role ON thesaurus.id_thesaurus=user_role.id_thesaurus WHERE user_role.id_user=? and thesaurus.private=true) AS theso ON theso.id_thesaurus=thesaurus_label.id_thesaurus WHERE lang=?";
                stmt=conn.prepareStatement(sql);
                stmt.setInt(1, idUser);
                stmt.setString(2, idLang);
                                             
                
                try{
                    rs=stmt.executeQuery();
                    while(rs.next()){
                        String id=rs.getString("id_thesaurus");
                        String title=rs.getString("title");
                        listTheoUser.put(title+" ("+id+") ",id);
                    }
                }
                finally{
                    stmt.close();
                }
                
            }finally{
                conn.close();
            }
        }catch(SQLException e){
            
            log.error("Erro while retrieving list of private thesaurus of the user : "+listTheoUser, e);
        }
        
        return listTheoUser;
    }
    
    /**
     * Fonction getLustPublicThesaurus
     * #JM
     * doit récupérer la liste des thesaurus publics de la BDD par langue d'utilisation
     * on notera que la colonne private à false dans la bdd correspond à un thesaurus public
     *
     * @param ds
     * @param idLang
     * @return 
     */
    private HashMap getListPublicThesaurus(HikariDataSource ds,String idLang){
        
        Connection conn;
        PreparedStatement stmt;
        HashMap listOfPrivate=new HashMap();
        if(ds == null) return listOfPrivate;
        ResultSet rs;
        
        try{
            conn=ds.getConnection();
            try{
                String sql="SELECT thesaurus_label.id_thesaurus,thesaurus_label.title "
                        + "FROM thesaurus_label INNER JOIN thesaurus ON thesaurus_label.id_thesaurus=thesaurus.id_thesaurus  "
                        + "where thesaurus.private=? AND thesaurus_label.lang=?";
                stmt=conn.prepareStatement(sql);
                stmt.setBoolean(1, false);
                stmt.setString(2, idLang);
                try{
                    rs=stmt.executeQuery();
                    while(rs.next()){
                        String id=rs.getString("id_thesaurus");
                        String title=rs.getString("title");
                        
                        listOfPrivate.put(title+" ("+id+") ",id);
                    }
                }
                finally{
                    stmt.close();
                }
            }finally{
                conn.close();
            }
                
        }catch(SQLException e){
            log.error("error while retrieving private thesuarus of the database", e);
        }
        
        return listOfPrivate;
    }
    
    /**
     * fonction getListThesaurusUser
     * #JM
     * Permet de récupérer une liste de thesaurus pour un utilisateur en particulier, avec 2 appels à deux autres fonctions,
     * le premier à une fonction pour récupérer tous les thésaurus publics getListPublicThesaurus ,
     * et ensuite une seconde fonction geListPrivateThesaurusUser pour 
     * récupérer les thésaurus privés de l'utilisateur
     * @param ds
     * @param idLang
     * @param idUser
     * @return HashMap :liste de thesaurus
     */
    public HashMap getListThesaurusUser(HikariDataSource ds, String idLang, int idUser){
        
        
        HashMap list1=this.getListPublicThesaurus(ds, idLang);
         
        HashMap list2=this.getListPrivateThesaurusUser(ds, idLang, idUser);
       
        list1.putAll(list2);
        
        return list1;
    }
    
    /**
     * fonction getListThesaurus 
     * #JM
     * Permet de récupéert la liste des thésaurus public en appellant getLisPublicThesaurus
     * @param ds
     * @param idLang
     * @return HashMap liste de thesaurus
     */
    public HashMap getListThesaurus(HikariDataSource ds, String idLang){
        
        HashMap list1=this.getListPublicThesaurus(ds, idLang);
        return list1;
        
    }
    
    /**
     * retourne la liste de tous les thésaurus (cas d'un SuperAdmin)
     * @param ds
     * @param idLang
     * @return 
     */
    public HashMap getListThesaurusSA(HikariDataSource ds,String idLang){
        
       Connection conn;
       PreparedStatement stmt;
       HashMap listOfAll=new HashMap();
       ResultSet rs;
       try{
           conn=ds.getConnection();
           try{
               String sql="SELECT thesaurus_label.id_thesaurus,thesaurus_label.title "
                        + "FROM thesaurus_label WHERE lang=?";
               stmt=conn.prepareStatement(sql);
               stmt.setString(1,idLang);
               try{
                   rs=stmt.executeQuery();
                   while(rs.next()){
                       String id=rs.getString("id_thesaurus");
                       String title=rs.getString("title");
                       listOfAll.put(title+" ("+id+") ",id);
                   }
               }
               finally{
                   stmt.close();
               }
           }
           finally{
               conn.close();
           }
       }
       catch(SQLException e){
           log.error("error while retrieving list of thesaurus for a super admin account in getListThesaurusSA");
           
       }
        
       return listOfAll;
    }
    /**
     * Fonction getAThesaurus
     * #JM
     * permet de récupérer un thésaurus d'après son identifiant
     * @param ds
     * @param idTheso
     * @param idLang
     * @return 
     */
    public Thesaurus getAThesaurus(HikariDataSource ds, String idTheso,String idLang){
        
        Connection conn;
        PreparedStatement stmt;
        Thesaurus th=new Thesaurus();
        ResultSet rs;
        try{
            conn=ds.getConnection();
            try{
                String sql="SELECT *  FROM thesaurus INNER JOIN thesaurus_label ON thesaurus.id_thesaurus=thesaurus_label.id_thesaurus WHERE thesaurus.id_thesaurus=? AND lang=?";
                
                stmt=conn.prepareStatement(sql);
                stmt.setString(1,idTheso);
                stmt.setString(2,idLang);
                try{
                    rs=stmt.executeQuery();
                    if(rs.next()){
                        th.setContributor(rs.getString("contributor"));
                        th.setCoverage(rs.getString("coverage"));
                        th.setCreated(rs.getDate("created"));
                        th.setCreator(rs.getString("creator"));
                        th.setDescription(rs.getString("description"));
                        th.setFormat(rs.getString("format"));
                        th.setId_ark(rs.getString("id_ark"));
                        th.setId_thesaurus(rs.getString("id_thesaurus"));
                        th.setLanguage(rs.getString("lang"));
                        th.setModified(rs.getDate("modified"));
                        th.setPublisher(rs.getString("publisher"));
                        th.setRelation(rs.getString("relation"));
                        th.setRights(rs.getString("rights"));
                        th.setSource(rs.getString("source"));
                        th.setSubject(rs.getString("subject"));
                        th.setTitle(rs.getString("title"));
                        th.setType(rs.getString("type"));
                        th.setPrivateTheso(rs.getBoolean("private"));
                        
                        
                    }
                    else{
                    
                       log.error("la requete n'a pas trouver de thesaurus associé à l'idenitifiant"); 
                    }
                    
                }
                finally{
                    stmt.close();
                }
            }finally{
                conn.close();
            }
        }
        catch(SQLException e){
            log.error("error while retrieving a thesaurus from the bdd "+th.toString(),e);
        }
        
        
        return th;
        
    }
    
    
    
    /**
     * Fonction getListThesaurusOwned
     * #JM
     * Fonction pour récupérer une liste des thésaurus ou la table user_role indique que le table est lié à cet utilisateur
     * @param ds
     * @param idLang
     * @param idUser
     * @return 
     */
    public HashMap getListThesaurusOwned(HikariDataSource ds, String idLang,int idUser){
        Connection conn;
        HashMap listOfOwned = new HashMap();
        PreparedStatement stmt;
        ResultSet rs;
        try{
            conn=ds.getConnection();
            try{
                String sql="SELECT thesaurus_label.id_thesaurus,title FROM thesaurus_label JOIN user_role ON "
                        + "thesaurus_label.id_thesaurus=user_role.id_thesaurus AND id_user=? and lang=?";
                stmt=conn.prepareStatement(sql);
                stmt.setInt(1, idUser);
                stmt.setString(2, idLang);
                try{
                    rs=stmt.executeQuery();
                    while(rs.next()){
                        String id=rs.getString("id_thesaurus");
                        String title=rs.getString("title");
                        
                        listOfOwned.put(title+" ("+id+") ",id);
                    }
                }
                finally{
                    stmt.close();
                }
            }finally{
                conn.close();
            }
        }
        catch(SQLException e){
            log.error("error while retrieving owned thesaurus  list ="+listOfOwned,e);
            
        }

        return listOfOwned;
        
    }
    /**
     * #JM
     * Fonction pour vérifier si une valeur existe déjà pour un identifiant de thésaurus dans
     * la table thesaurus
     * @param ds
     * @param idTheso
     * @return 
     */
    private boolean valueExist(HikariDataSource ds,String idTheso){
        Connection conn;
        PreparedStatement stmt;
        ResultSet rs;
        boolean ret=false;
        
        try{
            conn=ds.getConnection();
            try{
                String sql="SELECT * FROM thesaurus WHERE id_thesaurus=?";
                stmt=conn.prepareStatement(sql);
                stmt.setString(1, idTheso);
                try{
                    rs=stmt.executeQuery();
                    ret=rs.next();
                }
                finally{
                    stmt.close();
                }
            }finally{
                conn.close();
            }
        }
        catch(SQLException e){
            log.error("error while Select from thesaurus table id_thesaurus ="+idTheso,e);
        }
        return ret;
        
    }
    /**
     * Fonction insertVisbility()
     * #JM
     * Fonction pour update ou insert de la valeur private (false ou true) dans la table thesaurus
     * par défaut la valeur de la table est false (pour un thésaurus public)
     * @param ds
     * @param visible
     * @param idTheso
     * @return 
     */
    public int insertVisibility(HikariDataSource ds, boolean visible, String idTheso) {
        boolean existAlready=this.valueExist(ds,idTheso);
        Connection conn;
        PreparedStatement stmt;
        int execute=-1;
        try{
            conn=ds.getConnection();
            try{
                String sql;
                if(existAlready){
                   sql="UPDATE thesaurus SET private=? WHERE id_thesaurus=?";
                }
                else{
                   sql="INSERT INTO thesaurus (private,id_thesaurus) VALUES(?,?)";
                }
                stmt=conn.prepareStatement(sql);
                stmt.setBoolean(1,visible);
                stmt.setString(2,idTheso);
                try{
                    execute=stmt.executeUpdate();
                    
                }
                finally{
                    stmt.close();
                }
            }
            finally{
                conn.close();
            }
        }
        catch(SQLException e){
            log.error("error while Altering or Inserting Table thesaurus id_thesaurus="+idTheso,e);
        }
        return execute;
    }
    public int supprVisibility(HikariDataSource ds, String idTheso){
        Connection conn;
        PreparedStatement stmt;
        int execute=-1;
        try{
            conn=ds.getConnection();
            try{
                String sql="DELETE FROM thesaurus WHERE id_thesaurus=?";
                stmt=conn.prepareStatement(sql);
                stmt.setString(1,idTheso);
                try{
                    execute=stmt.executeUpdate();
                }
                finally{
                    stmt.close();
                }
            }
            finally{
                conn.close();
                
            }
        }
        catch(SQLException e){
            log.error("error while deleting value in thesaurus table of id thesaurus ="+idTheso, e);
        }
        return execute;
    }
  
}
