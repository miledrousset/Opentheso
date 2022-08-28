package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.skosapi.SKOSDiscussion;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSStatus;
import fr.cnrs.opentheso.skosapi.SKOSVote;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;



public class ExportHelper {

    private final static String SEPERATEUR = "##";
    private final static String SUB_SEPERATEUR = "@";

    public List<SKOSResource> getAllConcepts(HikariDataSource ds, String idTheso, 
            String idLang) throws Exception {

        List<SKOSResource> concepts = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from get_concepts('"+idTheso+"', 'http://localhost:8080', '"+idLang+"') as x(URI text, TYPE varchar,  LOCAL_URI text, " +
                        "IDENTIFIER varchar, ARK_ID varchar, prefLab varchar, altLab varchar, altLab_hiden varchar, definition text, example text, " +
                        "editorialNote text, changeNote text, secopeNote text, note text, historyNote text, notation varchar, narrower text, " +
                        "narrowerId text, broader text, broaderId text, related text, relatedId text, exactMatch text, closeMatch text, broadMatch text, " +
                        "relatedMatch text, narrowMatch text, latitude double precision, longitude double precision, membre text, " +
                        "created timestamp with time zone, modified timestamp with time zone, img text, status_candidat varchar, date_candidat varchar, " +
                        "message_candidat varchar, vote_candidat text, messages_candidat text);");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        SKOSResource sKOSResource = new SKOSResource();
                        sKOSResource.setProperty(SKOSProperty.Concept);
                        sKOSResource.setUri(resultSet.getString("uri"));
                        sKOSResource.setLocalUri(resultSet.getString("local_uri"));
                        
                        sKOSResource.addIdentifier(resultSet.getString("identifier"), SKOSProperty.identifier);
                        
                        if (!StringUtils.isEmpty(resultSet.getString("ark_id"))){
                           sKOSResource.setArkId(resultSet.getString("ark_id"));
                        }

                        setStatusOfConcept(resultSet.getString("type"), sKOSResource);

                        getLabels(resultSet.getString("prefLab"), sKOSResource, SKOSProperty.prefLabel);
                        getLabels(resultSet.getString("altLab"), sKOSResource, SKOSProperty.altLabel);
                        getLabels(resultSet.getString("altLab_hiden"), sKOSResource, SKOSProperty.hiddenLabel);

                        addDocumentation(resultSet.getString("note"), sKOSResource, SKOSProperty.note);
                        addDocumentation(resultSet.getString("definition"), sKOSResource, SKOSProperty.definition);
                        addDocumentation(resultSet.getString("secopeNote"), sKOSResource, SKOSProperty.scopeNote);
                        addDocumentation(resultSet.getString("historyNote"), sKOSResource, SKOSProperty.historyNote);
                        addDocumentation(resultSet.getString("example"), sKOSResource, SKOSProperty.example);
                        addDocumentation(resultSet.getString("editorialNote"), sKOSResource, SKOSProperty.editorialNote);
                        addDocumentation(resultSet.getString("changeNote"), sKOSResource, SKOSProperty.changeNote);

                        addAlignementGiven(resultSet.getString("exactMatch"), sKOSResource, SKOSProperty.exactMatch);
                        addAlignementGiven(resultSet.getString("closeMatch"), sKOSResource, SKOSProperty.closeMatch);
                        addAlignementGiven(resultSet.getString("broadMatch"), sKOSResource, SKOSProperty.broadMatch);
                        addAlignementGiven(resultSet.getString("relatedmatch"), sKOSResource, SKOSProperty.relatedMatch);
                        //addAlignementGiven(resultSet.getString("narrowmatch"), sKOSResource, SKOSProperty.narrowMatch);

                        addRelations(resultSet.getString("broader"), sKOSResource, SKOSProperty.topConceptOf, idTheso);

                        addReplaced(resultSet.getString("replaces_by"), sKOSResource, SKOSProperty.isReplacedBy);

                        addReplaced(resultSet.getString("replaces"), sKOSResource, SKOSProperty.replaces);
                        
                        addRelationsGiven(resultSet.getString("narrower"), sKOSResource);
                        addRelationsGiven(resultSet.getString("broader"), sKOSResource);
                        addRelationsGiven(resultSet.getString("related"), sKOSResource);
                        
                        if (!StringUtils.isEmpty(resultSet.getString("notation"))) {
                            sKOSResource.addNotation(resultSet.getString("notation"));
                        }
                        
                        /*
                        conceptDao.setMembre(resultSet.getString("membre"));*/

                        if (resultSet.getString("latitude") != null || resultSet.getString("longitude") != null) {
                            sKOSResource.setGPSCoordinates(new SKOSGPSCoordinates(
                                    resultSet.getDouble("latitude"), resultSet.getDouble("longitude")));
                        }

                        /*
                        // createur et contributeur
                        if (nodeConcept.getConcept().getCreatorName()!= null && !nodeConcept.getConcept().getCreatorName().isEmpty()) {
                            sKOSResource.addCreator(nodeConcept.getConcept().getCreatorName(), SKOSProperty.creator);
                        }
                        if (nodeConcept.getConcept().getContributorName()!= null && !nodeConcept.getConcept().getContributorName().isEmpty()) {
                            sKOSResource.addCreator(nodeConcept.getConcept().getContributorName(), SKOSProperty.contributor);
                        }
                         */
                        if (StringUtils.isNotEmpty(resultSet.getString("created"))) {
                            sKOSResource.addDate(resultSet.getString("created"), SKOSProperty.created);
                        }

                        if (StringUtils.isNotEmpty(resultSet.getString("modified"))) {
                            sKOSResource.addDate(resultSet.getString("modified"), SKOSProperty.modified);
                        }
                        
                        
                        // pour l'export des données du module candidat
                        if ("CA".equalsIgnoreCase(resultSet.getString("type"))) {
                            
                            SKOSStatus skosStatus = new SKOSStatus();
                            skosStatus.setDate(resultSet.getString("date_candidat"));
                            skosStatus.setIdConcept(resultSet.getString("identifier"));
                            skosStatus.setIdStatus(resultSet.getString("status_candidat"));
                            skosStatus.setMessage(resultSet.getString("message_candidat"));
                            skosStatus.setIdThesaurus(idTheso);
                            //skosStatus.setIdUser(nodeStatus.getIdUser());
                            sKOSResource.setSkosStatus(skosStatus);
                            
                            addCandidatDiscussions(sKOSResource, resultSet.getString("messages_candidat"));
                            
                            addCandidatVote(ds, sKOSResource, resultSet.getString("vote_candidat"), 
                                    idTheso, resultSet.getString("identifier"));
                        }

                        ArrayList<String> first = new ArrayList<>();
                        first.add(resultSet.getString("identifier"));
                        ArrayList<ArrayList<String>> paths = new ArrayList<>();

                        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds, 
                                resultSet.getString("identifier"), idTheso, first, paths);
                        ArrayList<String> pathFromArray = getPathFromArray(paths);
                        if (!pathFromArray.isEmpty()) {
                            sKOSResource.setPaths(pathFromArray);
                        }

                        concepts.add(sKOSResource);
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }

        return concepts;
    }

    private ArrayList<String> getPathFromArray(ArrayList<ArrayList<String>> paths) {
        String pathFromArray = "";
        ArrayList<String> allPath = new ArrayList<>();
        for (ArrayList<String> path : paths) {
            for (String string1 : path) {
                if(pathFromArray.isEmpty())
                    pathFromArray = string1;
                else
                    pathFromArray = pathFromArray + SEPERATEUR + string1;
            }
            allPath.add(pathFromArray);
            pathFromArray = "";
        }
        return allPath;
    }
    
    private void addCandidatDiscussions(SKOSResource resource, String textBrut) {
        
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                SKOSDiscussion skosDiscussion = new SKOSDiscussion();
                skosDiscussion.setMsg(element[0]);
                skosDiscussion.setIdUser(Integer.valueOf(element[1]));
                skosDiscussion.setDate(element[2]);
                resource.addMessage(skosDiscussion);
            }
        }
    }
    
    private void addCandidatVote(HikariDataSource ds, SKOSResource resource, String textBrut, String idTheso, String idConcept) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                
                SKOSVote skosVote = new SKOSVote();
                skosVote.setIdNote(element[2]);
                skosVote.setIdUser(Integer.parseInt(element[0]));
                skosVote.setIdThesaurus(idTheso);
                skosVote.setIdConcept(idConcept);
                skosVote.setTypeVote(element[1]);
                if (!StringUtils.isEmpty(element[2]) && !"null".equalsIgnoreCase(element[2])) {
                    String htmlTagsRegEx = "<[^>]*>";
                    NodeNote nodeNote = new NoteHelper().getNoteByIdNote(ds, Integer.parseInt(element[2]));
                    if (nodeNote != null) {
                        String str = ConceptHelper.formatLinkTag(nodeNote.getLexicalvalue());
                        skosVote.setValueNote(str.replaceAll(htmlTagsRegEx, ""));
                    }
                }
                resource.addVote(skosVote);
            }
        }
    }
    
    /*private void getImages() {
        
        if(nodeConcept.getNodeimages() != null || (!nodeConcept.getNodeimages().isEmpty())) {
            for (String imageUri : nodeConcept.getNodeimages()) {
                sKOSResource.addImageUri(imageUri);
            }
        }
    }*/
    private void setStatusOfConcept(String status, SKOSResource sKOSResource) {
        switch (status.toLowerCase()) {
            case "ca":
                sKOSResource.setStatus(SKOSProperty.candidate);
                break;
            case "dep":
                sKOSResource.setStatus(SKOSProperty.deprecated);
                break;
            default:
                sKOSResource.setStatus(SKOSProperty.Concept);
                break;
        }

    }

    private void addRelationsGiven(String textBrut, SKOSResource sKOSResource)
            throws SQLException, Exception {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                sKOSResource.addRelation(element[2], element[0], getType(element[1]));
            }
        }
    }

    private int getType(String role) {
        switch (role) {
            case "RHP":
                return SKOSProperty.relatedHasPart;
            case "RPO":
                return SKOSProperty.relatedPartOf;
            case "RT":
                return SKOSProperty.related;
            case "NTG":
                return SKOSProperty.narrowerGeneric;
            case "NTP":
                return SKOSProperty.narrowerPartitive;
            case "NTI":
                return SKOSProperty.narrowerInstantial;
            case "NT":
                return SKOSProperty.narrower;
            case "BTG":
                return SKOSProperty.broaderGeneric;
            case "BTP":
                return SKOSProperty.broaderPartitive;
            case "BTI":
                return SKOSProperty.broaderInstantial;
            default:
                return SKOSProperty.broader;
        }
    }

    private void addRelations(String textBrut, SKOSResource sKOSResource, int type, String thesoID)
            throws SQLException, Exception {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                SKOSRelation relation = new SKOSRelation(thesoID, tab, type);
                if (CollectionUtils.isEmpty(sKOSResource.getRelationsList())) {
                    sKOSResource.setRelationsList(new ArrayList<>());
                }
                sKOSResource.getRelationsList().add(relation);
            }
        }
    }

    private void addReplaced(String textBrut, SKOSResource sKOSResource, int type)
            throws SQLException, Exception {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                sKOSResource.addReplaces(tab, type);
            }
        }
    }

    private void addAlignementGiven(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                sKOSResource.addMatch(tab, type);
            }
        }
    }

    private void addDocumentation(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                sKOSResource.addDocumentation(element[0], element[1], type);
            }
        }
    }

    private void getLabels(String labelBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                sKOSResource.addLabel(element[0], element[1], type);
            }
        }
    }

    /*
        
        sKOSResource.addRelation(idTheso, getUriFromId(idTheso), SKOSProperty.inScheme);

        for (NodeUri nodeUri : nodeConcept.getNodeListIdsOfConceptGroup()) {
            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriGroupFromNodeUri(nodeUri, idTheso), SKOSProperty.memberOf);
        }

        if(nodeConcept.getListFacetsOfConcept() != null) {
            for (String idFacette : nodeConcept.getListFacetsOfConcept()) {
                int prop = SKOSProperty.subordinateArray;
                sKOSResource.addRelation(idFacette, getUriForFacette(idFacette, idTheso), prop);
            }
        }
     */
}
