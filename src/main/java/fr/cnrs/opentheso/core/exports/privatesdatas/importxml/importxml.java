/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.privatesdatas.importxml;

import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.core.exports.privatesdatas.LineOfData;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Table;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author antonio.perez
 */



public class importxml {

    
    private String nomBDD="";
    /*  
        for (Table user : DataTable) {
            for (LineOfData lineOfData : user.getLineOfDatas()) {
                writeLine(lineOfData.getColomne(), lineOfData.getValue());
            }
     */
    
    /*
    public void choisirfichier (HikariDataSource ds){
        JFileChooser fileChooser = new JFileChooser();
        int seleccion = fileChooser.showOpenDialog(null);
        fichero = fileChooser.getSelectedFile();
               //Acciones que se quieran realizar
        ouvreFichier();
       
    }*/ 
    
    /**
     * cette funtion permet de ouvrir un fichier pour comencée a faire une 
     * injection de données. 
     * C'est seulement pour la creation de un nouvelle BDD.
     * la funtion generique est plus ba
     * @param con
     * @param archive
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public void ouvreFichier(Connection con, File archive) throws ClassNotFoundException, SQLException {

        SAXBuilder builder = new SAXBuilder();
        ArrayList<Table> toutTables = new ArrayList<>();
        ArrayList<LineOfData> lineOfDatas = new ArrayList<>();
        try {

            //on crée le document a partir du fichier que on a selectioné
            Document document = (Document) builder.build(archive);
            //Se obtiene la raiz 'tables'
            Element rootNode = document.getRootElement();
            
            // ici on a toutes les tables (les enfants de la racine)
            List list = rootNode.getChildren("table");           

            // ici on fait le tour pour les enfants de 'tables'
            for (int i = 0; i < list.size(); i++) {

                // ici on a la première table
                Element tabla = (Element) list.get(i);

                //ici on a le nom de la table
                String nombreTabla = tabla.getAttributeValue("nom");
                // ici c'est la liste des lignes de la table
                List lista_campos = tabla.getChildren();

                //ici on découpe la liste des lignes
                for (int j = 0; j < lista_campos.size(); j++) {
                    
                    //ici on a une ligne de la table
                    Element campo = (Element) lista_campos.get(j);
                    for (Element colonne : campo.getChildren()) {
                        LineOfData lineOfData = new LineOfData();
                        lineOfData.setColomne(colonne.getName());//  le nom de la colone
                        lineOfData.setValue(colonne.getText());// le value que le correspond
                        lineOfDatas.add(lineOfData);
                    }              
                    insertLine(con, lineOfDatas, nombreTabla);
                    lineOfDatas.clear();
                }
                /// mettre à jour la table dans la BDD
            } 
        } catch (IOException | JDOMException io) {
            System.out.println("error");
        }
        //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("impBDD.info1")));
    }
/**
 * cette funtion permet de faire la inyection a la BDD line par line de la table "nomtable"
 * et le données que ils sont dans une ArrayList
 * @param ds
 * @param table
 * @param nomtable 
 */
    private void insertLine(Connection con, ArrayList<LineOfData> table, String nomtable) throws ClassNotFoundException, SQLException {
        Statement stmt;
        String nomcolun = "";
        String values = "";
        boolean first = true;
        for (LineOfData lineOfData : table) {
            if (!first) {
                nomcolun += ",";
                values += ",";
            }
            nomcolun += lineOfData.getColomne();
            values += "'" + lineOfData.getValue() + "'";
            first = false;
        }
        values += ");";     
        try {
            //Connection conn = ds.getConnection();
            stmt = con.createStatement();
            try {
                // récupération des noms des colonnes de la table
                String query = "INSERT INTO " + nomtable + " ( "
                        + nomcolun + ") VALUES (" + values;
                stmt.executeUpdate(query);
            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    /**
     * cette funtion permet de ouvrir un fichier pour comencée a faire une 
     * injection de données. 
     * Cette funtion c'est la generique
     * @param ds
     * @param archive
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
public void ouvreFichier2(HikariDataSource ds, File archive) throws ClassNotFoundException, SQLException {

        SAXBuilder builder = new SAXBuilder();
        ArrayList<Table> toutTables = new ArrayList<>();
        
        ArrayList<LineOfData> lineOfDatas = new ArrayList<>();
        try {

            //on crée le document a partir du fichier que on a selectioné
            Document document = (Document) builder.build(archive);
            //Se obtiene la raiz 'tables'
            Element rootNode = document.getRootElement();
            
            // ici on a toutes les tables (les enfants de la racine)
            List list = rootNode.getChildren("table");           

            // ici on fait le tour pour les enfants de 'tables'
            for (int i = 0; i < list.size(); i++) {

                // ici on a la première table
                Element tabla = (Element) list.get(i);

                //ici on a le nom de la table
                String nombreTabla = tabla.getAttributeValue("nom");
                // ici c'est la liste des lignes de la table
                List lista_campos = tabla.getChildren();

                //ici on découpe la liste des lignes
                for (int j = 0; j < lista_campos.size(); j++) {
                    
                    //ici on a une ligne de la table
                    Element campo = (Element) lista_campos.get(j);
                    for (Element colonne : campo.getChildren()) {
                        LineOfData lineOfData = new LineOfData();
                        lineOfData.setColomne(colonne.getName());
                        lineOfData.setValue(colonne.getText());
                        lineOfDatas.add(lineOfData);
                    }              
                    insertLine2(ds, lineOfDatas, nombreTabla);
                    lineOfDatas.clear();
                }
                /// mettre à jour la table dans la BDD
            } 
        } catch (IOException | JDOMException io) {
            System.out.println(io.toString());
        }
        //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(langueBean.getMsg("info") + " :", langueBean.getMsg("impBDD.info1")));
    }
/**
 * cette funtion permet de faire la inyection a la BDD line par line de la table "nomtable"
 * et le données que ils sont dans une ArrayList
 * Cette funtion c'est la generique;
 * @param ds
 * @param table
 * @param nomtable 
 */
    private void insertLine2(HikariDataSource ds, ArrayList<LineOfData> table, String nomtable) throws ClassNotFoundException, SQLException {
        Statement stmt;
        String nomcolun = "";
        String values = "";
        boolean first = true;
        Connection con = ds.getConnection();
        
        String temp = "";
        
        for (LineOfData lineOfData : table) {
            if (!first) {
                nomcolun += ",";
                values += ",";
            }
            nomcolun += lineOfData.getColomne();//nomcolun contiens toutes les camps de la table "nomtable"
            values += "'" + lineOfData.getValue() + "'";//contiens toutes les values de la memme ligne
            first = false;
        }
        values += ");";     
        try {
            stmt = con.createStatement();
            try {
                // récupération des noms des colonnes de la table
                String query = "INSERT INTO " + nomtable + " ( "
                        + nomcolun + ") VALUES (" + values;
                temp = query;
                stmt.executeUpdate(query);
                
            } finally {
                stmt.close();
                con.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(temp);
        }
    } 
    
}

