package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.concept.NodePath;
import fr.cnrs.opentheso.models.concept.Path;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.NonPreferredTermRepository;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;



@Data
@Service
@RequiredArgsConstructor
public class PathService {

    private final NoteHelper noteHelper;
    private final GroupHelper groupHelper;
    private final TermService termService;
    private final ConceptHelper conceptHelper;
    private final RelationsHelper relationsHelper;
    private final PreferenceService preferenceService;
    private final NonPreferredTermRepository nonPreferredTermRepository;

    private String message;


    /**
     * methode pour retrouver tous les chemins vers la racine exemple de retour
     */   
    public List<String> getGraphOfConcept(String idConcept, String idThesaurus) {
        message = null; 
        List<String> path = new ArrayList<>();
        getGraph(idConcept, idThesaurus, path);
        return path;
    }

    private void getGraph(String idConcept, String idThesaurus, List<String> path){

        List<String> idBTs = relationsHelper.getListIdBT(idConcept, idThesaurus);
        if(idBTs == null || idBTs.isEmpty()){
            if(!path.contains(idConcept)) {
                path.add(idConcept);
            }            
        } else {
            for (String idBT : idBTs) {
                if(!path.contains(idConcept + "##" + idBT) && !path.contains(idBT + "##" + idConcept)) {
                    path.add(idConcept + "##" + idBT);
                    getGraph(idBT, idThesaurus, path);
                }
            }       
        }
    }
    
    public List<List<String>> getPathFromGraph(List<String> graph){
        List<String> topTerms = getTopTermsFromGraph(graph);
        
        List<List<String>> paths = new ArrayList<>();
        List<String> savedPath = new ArrayList<>();
        for (String topTerm : topTerms) {
            List<String> path = new ArrayList<>();
            getNT(topTerm, path, savedPath, paths, graph);
            if(!path.isEmpty()){
                paths.add(path);
            }
        }
        return paths;
    }
    private void getNT(String idConcept, List<String> path, List<String> savedPath, List<List<String>> paths, List<String> graph){

        List<String> idNTs = getListNT(idConcept, graph);
        path.add(idConcept);
        
        if(idNTs != null && idNTs.size() > 1){
            for (String idC : path) {
                savedPath.add(idC);
            }
        }
        
        if(idNTs == null || idNTs.isEmpty()){
            List<String> pathTemp = new ArrayList<>();
            for (String path1 : path) {
                pathTemp.add(path1);
            }
            paths.add(pathTemp);
            path.clear();
            if(!savedPath.isEmpty()){
                for (String idC : savedPath) {
                   path.add(idC);
                }
                savedPath.clear();
            }
        } else {
            for (String idNT : idNTs) {
                getNT(idNT, path, savedPath, paths, graph);
            }       
        }        
    }
    
    private List<String> getListNT(String idConcept, List<String> graph){
        List<String> listNTs = new ArrayList<>();
        for (String node : graph) {
            if(StringUtils.endsWith(node, "##"+idConcept)){
                listNTs.add(StringUtils.substringBefore(node, "##"));
            }
        }
        return listNTs;
    }
    
    
    private List<String> getTopTermsFromGraph(List<String> graph){
        List<String> topTerms = new ArrayList<>();
        for (String node : graph) {
            if(!node.contains("##")){
                topTerms.add(node);
            }
        }
        return topTerms;
    }
    
    public List<List<NodePath>> getPathWithLabel2(List<List<String>> paths, String idTheso, String idLang) {
        
        ArrayList<List<NodePath>> pathLabel1 = new ArrayList<>();
        boolean isStartNewPath;
        String label;
        
        for (List<String> path : paths) {
            isStartNewPath = true;
            List<NodePath> nodePaths = new ArrayList<>();
            for (String idConcept : path) {
                NodePath nodePath = new NodePath();
                nodePath.setIdConcept(idConcept);
                label = conceptHelper.getLexicalValueOfConcept(idConcept, idTheso, idLang);
                if(label.isEmpty())
                    label = "("+ idConcept+")";
                nodePath.setTitle(label);
                nodePath.setStartOfPath(isStartNewPath);
                nodePaths.add(nodePath);
                isStartNewPath = false;
            }
            pathLabel1.add(nodePaths);
        }
        return pathLabel1;
    }
    
    public JsonArrayBuilder getPathWithLabelAsJson(List<Path> paths, JsonArrayBuilder jsonArrayBuilder,
                                                   String idTheso, String idLang, String format) {

        String label;
        int i = 0;
         
        if(idLang == null || idLang.isEmpty()) {
            idLang = preferenceService.getWorkLanguageOfThesaurus(idTheso);
        }

        for (Path path1 : paths) {
            JsonArrayBuilder jsonArrayBuilderPath = Json.createArrayBuilder();            
            for (String idConcept : path1.getPath()) {
                JsonObjectBuilder job = Json.createObjectBuilder();

                label = conceptHelper.getLexicalValueOfConcept(idConcept, idTheso, idLang);
                if(label.isEmpty())
                    label = "("+ idConcept+")";
                job.add("id", idConcept);
                job.add("arkId", conceptHelper.getIdArkOfConcept(idConcept, idTheso));
                job.add("label", label);
                
                
                if(format != null && format.equalsIgnoreCase("full")) {
                    if(i++ == path1.getPath().size() - 1){
                        // synonymes
                        var altLabels = nonPreferredTermRepository.findAltLabelsByConceptAndThesaurusAndLang(idConcept, idTheso, idLang);
                        
                        JsonArrayBuilder jsonArrayBuilderAltLabels = Json.createArrayBuilder();
                        for (String altLabel : altLabels) {
                            jsonArrayBuilderAltLabels.add(altLabel);
                        }
                        if(jsonArrayBuilderAltLabels != null && !altLabels.isEmpty())
                            job.add("altLabel", jsonArrayBuilderAltLabels.build());  
                        
                        // défintion
                        ArrayList<String> definitions = noteHelper.getDefinition(idConcept, idTheso, idLang);
                        JsonArrayBuilder jsonArrayBuilderDefinitions = Json.createArrayBuilder();
                        for (String def : definitions) {
                            jsonArrayBuilderDefinitions.add(def);
                        }
                        if(jsonArrayBuilderDefinitions != null && !definitions.isEmpty())
                            job.add("definition", jsonArrayBuilderDefinitions.build());      
                        
                        // note d'application
                        ArrayList<String> scopeNotes = noteHelper.getScopeNote(idConcept, idTheso, idLang);
                        JsonArrayBuilder jsonArrayBuilderScopeNotes = Json.createArrayBuilder();
                        for (String scope : scopeNotes) {
                            jsonArrayBuilderScopeNotes.add(scope);
                        }
                        if(jsonArrayBuilderScopeNotes != null && !scopeNotes.isEmpty())
                            job.add("scopeNote", jsonArrayBuilderScopeNotes.build());
                        
                        // traductions
                        List<NodeTermTraduction> traductions = termService.getTraductionsOfConcept(idConcept, idTheso, idLang);

                        JsonObjectBuilder jobTrad = Json.createObjectBuilder();
                        for (NodeTermTraduction trad : traductions) {
                            jobTrad.add(trad.getLang(), trad.getLexicalValue());                            
                        }
                        if(jobTrad != null){
                            if(!traductions.isEmpty()) {
                                job.add("labels", jobTrad.build());
                            }
                        }
                    }
                }
                jsonArrayBuilderPath.add(job.build());
            }
            jsonArrayBuilder.add(jsonArrayBuilderPath.build());//.toString());
            i = 0;
        }

        return jsonArrayBuilder;
    }
    
    public ArrayList<Path> getPathOfConcept(String idConcept, String idThesaurus) {
        message = null;
        ArrayList<Path> allPaths = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        path.add(idConcept);
        
        ArrayList<String> firstPath = new ArrayList<>();
        allPaths = getPathOfConcept(idConcept, idThesaurus, firstPath, path, allPaths);
        return allPaths;
    }
    
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<Path> getPathOfConcept(String idConcept, String idThesaurus,
                                             ArrayList<String> firstPath, ArrayList<String> path, ArrayList<Path> allPaths) {


        ArrayList<String> idBTs = relationsHelper.getListIdBT(idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
          //  Collections.reverse(path);
            if(path.equals(firstPath)){
                message = "!! Attention, erreur de boucle détectée :" + firstPath.toString();
                return allPaths;
            }
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
                if (pathTemp.indexOf(id1) == -1) {
                    pathTemp.add(id1);//(0,id1);
                }
            }
            
            Path path1 = new Path();
            Collections.reverse(pathTemp);
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            if(!path.contains(idBT)){
                path.add(idBT);
                getPathOfConcept(idBT, idThesaurus, firstPath, path, allPaths);
            } else {
                message = "!! Attention, erreur de boucle détectée, vérifier le concept  :" + idBT;
                message = message + "  ___ et le chemin " + path;
            }
        }
        
        return allPaths;

    }

    private class Link {

        public Link(String origin, String destination) {
            this.destination = destination;
            this.origin = origin;
        }
        
        private final String origin;
        private final String destination;

        public String getOrigin() {
            return origin;
        }

        public String getDestination() {
            return destination;
        }

        @Override
        public String toString() {
            return origin + " --> " + destination;
        }
        
    }

    /**
     * permet de rtourner le chemin vers le groupe sélectionné
     * @param idGroup
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getPathOfGroup(String idGroup, String idThesaurus) {

        ArrayList<String> path = new ArrayList<>();
        path.add(idGroup);


        path = getPathOfGroup(idGroup, idThesaurus, path);

        return path;
    }

        /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param idGroup
     * @param idThesaurus
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<String> getPathOfGroup(String idGroup, String idThesaurus, ArrayList<String> path) {

        String idGroupParent = groupHelper.getIdFather(idGroup, idThesaurus);
        if(idGroupParent == null) return path;

        path.add(0,idGroupParent);
        getPathOfGroup(idGroupParent, idThesaurus, path);

        return path;
    }

}
