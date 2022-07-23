/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author miledrousset
 */
public class PathHelper {

    
    public ArrayList<NodePath> getPathWithLabel(HikariDataSource ds, ArrayList<Path> paths,
            String idTheso, String idLang, String selectedIdConcept) {
        
        ArrayList<NodePath> pathLabel1 = new ArrayList<>();
        boolean isStartNewPath = true;
        String label;
        
        for (Path path1 : paths) {
            for (String idConcept : path1.getPath()) {
                if(!idConcept.equalsIgnoreCase(selectedIdConcept)) {
                    NodePath nodePath = new NodePath();
                    nodePath.setIdConcept(idConcept);
                    label = new ConceptHelper().getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
                    if(label.isEmpty())
                        label = "("+ idConcept+")";
                    nodePath.setTitle(label);
                    nodePath.setIsStartOfPath(isStartNewPath);
                    pathLabel1.add(nodePath);
                    isStartNewPath = false;
                }
            }
            isStartNewPath = true;
        }
        return pathLabel1;
    }  
    
    public void getPathWithLabelAsJson(HikariDataSource ds,
            ArrayList<Path> paths, JsonArrayBuilder jsonArrayBuilder,
            String idTheso, String idLang, String selectedIdConcept) {
        String label;
        ConceptHelper conceptHelper = new ConceptHelper();
        GroupHelper groupHelper = new GroupHelper();
       // ArrayList<NodeGroup> nodeGroup;
        ArrayList<NodeUri> nodeGroup;
         
        for (Path path1 : paths) {
            JsonArrayBuilder jsonArrayBuilderPath = Json.createArrayBuilder();            
            for (String idConcept : path1.getPath()) {
                JsonObjectBuilder job = Json.createObjectBuilder();

                label = conceptHelper.getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
                if(label.isEmpty())
                    label = "("+ idConcept+")";
                job.add("id", idConcept);
                job.add("arkId", conceptHelper.getIdArkOfConcept(ds, idConcept, idTheso));
                job.add("label", label);
                
                // ajout des groups
                // temps 2 secondes
                nodeGroup = groupHelper.getListGroupOfConceptArk(ds, idTheso, idConcept);//ListGroupOfConcept(ds, idTheso, idConcept, idLang);
                JsonArrayBuilder jsonArrayBuilderGroup = Json.createArrayBuilder();
                for (NodeUri nodeUri : nodeGroup) {
                    JsonObjectBuilder jobGroup = Json.createObjectBuilder();
                    jobGroup.add("id", nodeUri.getIdConcept());
                    jobGroup.add("arkId", nodeUri.getIdArk());
                    jobGroup.add("label", groupHelper.getLexicalValueOfGroup(ds, nodeUri.getIdConcept(), idTheso, idLang));
                    jsonArrayBuilderGroup.add(jobGroup.build());
                }                
                
                
                // temps 6 secondes
                /*
                nodeGroup = groupHelper.getListGroupOfConcept(ds, idTheso, idConcept, idLang);
                for (NodeGroup nodeGroup1 : nodeGroup) {
                    job.add("group", nodeGroup1.getConceptGroup().getIdARk());
                    job.add("group", nodeGroup1.getLexicalValue());
                }
                */
                job.add("group", jsonArrayBuilderGroup.build());
                jsonArrayBuilderPath.add(job.build());
            }
            jsonArrayBuilder.add(jsonArrayBuilderPath.build());//.toString());
        }
    }      
    
    public ArrayList<Path> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        ArrayList<Path> allPaths = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        path.add(idConcept);
        
        ArrayList<String> firstPath = new ArrayList<>();
        allPaths = getPathOfConcept(ds, idConcept,
                idThesaurus,
                firstPath,
                path, allPaths);

        return allPaths;
    }
    
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<Path> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
            for (String idBT1 : path) {
                firstPath.add(0,idBT1);
            }
        }
        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                if (pathTemp.indexOf(id1) == -1) {
                    pathTemp.add(0,id1);
                }
            }
            
            Path path1 = new Path();
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            path.add(idBT);
            getPathOfConcept(ds, idBT, idThesaurus, firstPath, path, allPaths);
        }
        
        return allPaths;

    }
    
    public ArrayList<Path> getPathOfFacet(HikariDataSource ds,
            String idFacet, String idThesaurus) {

        ArrayList<Path> allPaths = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        path.add(idFacet);
        
        FacetHelper facetHelper = new FacetHelper();
        String idConceptParentOfFacet = facetHelper.getIdConceptParentOfFacet(ds, idFacet, idThesaurus);        
        path.add(0,idConceptParentOfFacet);
        ArrayList<String> firstPath = new ArrayList<>();
        allPaths = getPathOfConcept(ds, idConceptParentOfFacet,
                idThesaurus,
                firstPath,
                path, allPaths);

        return allPaths;
    }    
    
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
  /*  private ArrayList<Path> getPathOfFacet(HikariDataSource ds,
            String idFacet, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        FacetHelper facetHelper = new FacetHelper();
        String idFacetParent = facetHelper.getIdParentOfFacet(ds, idFacet, idThesaurus);
       
        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
            for (String idBT1 : path) {
                firstPath.add(0,idBT1);
            }
        }
        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                if (pathTemp.indexOf(id1) == -1) {
                    pathTemp.add(0,id1);
                }
            }
            
            Path path1 = new Path();
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            path.add(idBT);
            getPathOfConcept(ds, idBT, idThesaurus, firstPath, path, allPaths);
        }
        
        return allPaths;

    }    */

    /**
     * permet de rtourner le chemin vers le groupe sélectionné
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return 
     */
    public ArrayList<String> getPathOfGroup(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        ArrayList<String> path = new ArrayList<>();
        path.add(idGroup);
        

        path = getPathOfGroup(ds, idGroup,
                idThesaurus, path);

        return path;
    }    

        /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<String> getPathOfGroup(HikariDataSource ds,
            String idGroup, String idThesaurus,
            ArrayList<String> path) {

        GroupHelper groupHelper = new GroupHelper();

        String idGroupParent = groupHelper.getIdFather(ds, idGroup, idThesaurus);
        if(idGroupParent == null) return path;

        path.add(0,idGroupParent);
        getPathOfGroup(ds, idGroupParent, idThesaurus, path);
        
        return path;
    }
    
    
    /**
     * Pour tester 
     * #MR
     * 
     */
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<Path> getInvertPathOfConcept2(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
            for (String idBT1 : path) {
                if(!firstPath.contains(idBT1))
                    firstPath.add(idBT1);
            }
        }

        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                pathTemp.add(id1);
            }
            
            Path path1 = new Path();
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            path.add(0,idBT);
            getInvertPathOfConcept2(ds, idBT, idThesaurus, firstPath, path, allPaths);
        }
        
        return allPaths;

    }    
    
}
