/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.tabulate;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTT;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupLabel;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;

/**
 * cette classe contient toutes les données d'un thésaurus
 * 
 * @author miled.rousset
 */
public class ThesaurusDatas {
    
    // le thésaurus avec ses traductions
    private NodeThesaurus nodeThesaurus;
    
    // la liste des domaines avec les traductions
    private ArrayList<NodeGroupLabel> nodeGroupLabels;
    
    // la liste des Ids des Topconcepts
    private ArrayList<NodeTT> nodeTTs;
    
    // la liste des concepts et les termes traduits
    private ArrayList<NodeConceptExport> nodeConceptExports;

    public ThesaurusDatas() {
        this.nodeGroupLabels = new ArrayList<>();
        this.nodeConceptExports = new ArrayList<>();
    }
    
    /**
     * permet de récupérer toutes les données d'un thésaurus
     * puis les chargées dans les variables de la classe
     * 
     * @param ds
     * @param idThesaurus
     * @return 
     */
    
    public boolean exportAllDatas(HikariDataSource ds, String idThesaurus) {
        
        // récupération du thésaurus 
        this.nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(ds, idThesaurus);
        
        // récupération des groupes
        ArrayList<String> idGroups = new GroupHelper().getListIdOfGroup(ds, idThesaurus);
        for (String idGroup : idGroups) {
            this.nodeGroupLabels.add(new GroupHelper().getNodeGroupLabel(ds, idGroup, idThesaurus));
        }
        
        // récupération des ids des Tops Concepts
        nodeTTs = new ConceptHelper().getAllListIdsOfTopConcepts(ds, idThesaurus);
        
        // récupération de tous les concepts
        for (NodeTT nodeTT1 : nodeTTs) {
            
            
            new ConceptHelper().exportAllConcepts(ds, nodeTT1.getIdConcept(),
                    idThesaurus, nodeConceptExports);
        }
        
        return true;
    }
    
    /**
     * permet d'exporter les identifiants Ark, Handle et identifiant interne
     * + des options
     * format d'export :
     * id, idArk, idHandle, label // options // synonyme, note, alignement 
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param selectedGroups
     * @return 
     */
    
    public boolean exportIdentifiers(HikariDataSource ds, String idThesaurus, 
            String idLang, List<NodeGroup> selectedGroups) {
        
        // récupération du thésaurus 
        this.nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(ds, idThesaurus);
        
        // récupération des groupes
        ArrayList<String> idGroups = new GroupHelper().getListIdOfGroup(ds, idThesaurus);
        for (String idGroup : idGroups) {
            this.nodeGroupLabels.add(new GroupHelper().getNodeGroupLabel(ds, idGroup, idThesaurus));
        }
        
        // récupération des ids des Tops Concepts
        nodeTTs = new ConceptHelper().getAllListIdsOfTopConcepts(ds, idThesaurus);
        
        // récupération de tous les concepts
        for (NodeTT nodeTT1 : nodeTTs) {
            new ConceptHelper().exportAllConcepts(ds, nodeTT1.getIdConcept(),
                    idThesaurus, nodeConceptExports);
        }
        return true;
    }    
    
    /**
     * permet de récupérer toutes les données d'un thésaurus
     * puis les chargées dans les variables de la classe
     * en filrant par langue et Groupe
     * 
     * @param ds
     * @param idThesaurus
     * @param selectedLanguages
     * @param selectedGroups
     * @return 
     */
    
    public boolean exportAllDatas(HikariDataSource ds,
            String idThesaurus,
            List<NodeLangTheso> selectedLanguages,
            List<NodeGroup> selectedGroups) {
        
        // récupération du thésaurus 
        this.nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(ds, idThesaurus);
        
        // récupération des groupes suivant le filtre selectedGroups
        for (NodeGroup nodeGroup : selectedGroups) {
            this.nodeGroupLabels.add(new GroupHelper().getNodeGroupLabel(ds, nodeGroup.getConceptGroup().getIdgroup(), idThesaurus));
        }
        
        ArrayList<String> listTopConcept;
        for (NodeGroup nodeGroup : selectedGroups) {
            listTopConcept = new ConceptHelper().getListIdsOfTopConceptsByGroup(ds, nodeGroup.getConceptGroup().getIdgroup(), idThesaurus);
            // récupération de tous les concepts
            for (String idTop : listTopConcept) {
                new ConceptHelper().exportAllConcepts(ds, idTop,
                        idThesaurus, nodeConceptExports);
            }
        }
        
        return true;
    }    

    public NodeThesaurus getNodeThesaurus() {
        return nodeThesaurus;
    }

    public void setNodeThesaurus(NodeThesaurus nodeThesaurus) {
        this.nodeThesaurus = nodeThesaurus;
    }

    public ArrayList<NodeGroupLabel> getNodeGroupLabels() {
        return nodeGroupLabels;
    }

    public void setNodeGroupLabels(ArrayList<NodeGroupLabel> nodeGroupLabels) {
        this.nodeGroupLabels = nodeGroupLabels;
    }

    public ArrayList<NodeTT> getNodeTTs() {
        return nodeTTs;
    }

    public void setNodeTTs(ArrayList<NodeTT> nodeTTs) {
        this.nodeTTs = nodeTTs;
    }

    public ArrayList<NodeConceptExport> getNodeConceptExports() {
        return nodeConceptExports;
    }

    public void setNodeConceptExports(ArrayList<NodeConceptExport> nodeConceptExports) {
        this.nodeConceptExports = nodeConceptExports;
    }
    
    
}
