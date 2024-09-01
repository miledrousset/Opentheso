/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.exports.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.models.exports.tabulate.ThesaurusDatas;


/**
 * Cette classe permet d'exporter toutes les données d'un thésaurus
 * et les préparer pour un export spécifique (Skos, tabulé ....)
 *
 * @author miled.rousset
 */
public class ExportThesaurus {

    private ThesaurusDatas thesaurusDatas;
    private StringBuffer message;
    
    public ExportThesaurus() {
    }
    
    public boolean exportAllDatas(HikariDataSource ds, String idThesaurus){
        this.thesaurusDatas = new ThesaurusDatas();
        if(!thesaurusDatas.exportAllDatas(ds, idThesaurus)){
            return false;
        }
        return true;
    }

    public ThesaurusDatas getThesaurusDatas() {
        return thesaurusDatas;
    }
    
    public StringBuilder exportAltLabel(HikariDataSource ds, 
            String idTheso, String idLang) {
        GroupHelper groupHelper = new GroupHelper();
        WriteAltLabel writeAltLabel = new WriteAltLabel();
        String GroupLabel;
        
        ArrayList <String> GroupLists = groupHelper.getListIdOfGroup(ds, idTheso);//getAllBottomGroup(ds, idTheso);
        if(GroupLists == null) return null;
        if(GroupLists.isEmpty()) return new StringBuilder("");
        
        for (String GroupList : GroupLists) {
            GroupLabel = groupHelper.getLexicalValueOfGroup(ds, GroupList, idTheso, idLang);
            writeAltLabel.setGroup(GroupList, GroupLabel);
            writeAltLabel.setHeader();
            writeAltLabel.AddAltLabelByGroup(ds, idTheso, GroupList, idLang);
        }
        return writeAltLabel.getAllAltLabels();
    }
    
    /**
     * permetr d'exporter les identifiants pérennes et internes d'un thésaurus par groupe(s) 
     * @param ds
     * @param idTheso
     * @param idLang
     * @param selectedGroups
     * @param nodePreference
     * @return 
     */
    public StringBuilder exportIdentifier(HikariDataSource ds, 
            String idTheso, String idLang, List<NodeGroup> selectedGroups,
            NodePreference nodePreference) {

        WriteIdentifier writeIdentifier = new WriteIdentifier();
        writeIdentifier.setHeader();
        writeIdentifier.AppendConcept(ds, idTheso, idLang, selectedGroups, nodePreference);
        message = writeIdentifier.getMessage();
        return writeIdentifier.getAllIdentifiers();
    }
    
          
    
    /**
     * Permet d'exporter tout le thésaurus en filtrant par langue et Groupe
     * @param ds
     * @param idThesaurus
     * @param selectedLanguages
     * @param selectedGroups
     * @return 
     */
    public boolean exportAllDatas(HikariDataSource ds,
            String idThesaurus, 
             List<NodeLangTheso> selectedLanguages,
            List<NodeGroup> selectedGroups){
        this.thesaurusDatas = new ThesaurusDatas();
        if(!thesaurusDatas.exportAllDatas(ds,
                idThesaurus, 
                selectedLanguages, 
                selectedGroups)){
            return false;
        }
        return true;
    }    

    public StringBuffer getMessage() {
        return message;
    }
    
}
