/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.bdd.helper.newclasses;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miledrousset
 * Classe qui permet de communiquer directement avec les fonctions PlpgSQL
 * 
 */
public class plpgsqlHelper {
   
    
    public void getConcept(){
        
    }
    
    
    /**
     * Nouvelle version pour récupérer un thésaurus entier en utilisant les 
     * requêtes plpgsql 
     * @param ds
     * @param idTheso
     * @param baseUrl
     * @param idGroup
     * @param originalUri
     * @param nodePreference
     * @return
     * @throws Exception 
     */
    public List<SKOSResource> getAllConcepts(HikariDataSource ds, String idTheso,
            String baseUrl, String idGroup, String originalUri, NodePreference nodePreference) throws Exception {

        List<SKOSResource> concepts = new ArrayList<>();
/*        String [] contributors;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery(getSQLRequest(idTheso, baseUrl, idGroup));
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        SKOSResource sKOSResource = new SKOSResource();
                        sKOSResource.setProperty(SKOSProperty.Concept);
                        sKOSResource.setUri(resultSet.getString("uri"));
                        sKOSResource.setLocalUri(resultSet.getString("local_uri"));

                        sKOSResource.addIdentifier(resultSet.getString("identifier"), SKOSProperty.identifier);

                        if (!StringUtils.isEmpty(resultSet.getString("ark_id"))) {
                            sKOSResource.setArkId(resultSet.getString("ark_id"));
                        }

                        setStatusOfConcept(resultSet.getString("type"), sKOSResource);

                        getLabels(resultSet.getString("prefLab"), sKOSResource, SKOSProperty.prefLabel);
                        getLabels(resultSet.getString("altLab_hiden"), sKOSResource, SKOSProperty.hiddenLabel);
                        getLabels(resultSet.getString("altLab"), sKOSResource, SKOSProperty.altLabel);

                        if (resultSet.getString("broader") == null || resultSet.getString("broader").isEmpty()) {
                            sKOSResource.getRelationsList().add(new SKOSRelation(idTheso, getUriThesoFromId(idTheso, originalUri, nodePreference),
                                    SKOSProperty.topConceptOf));
                        }

                        addRelationsGiven(resultSet.getString("related"), sKOSResource);

                        addDocumentation(resultSet.getString("definition"), sKOSResource, SKOSProperty.definition);
                        addDocumentation(resultSet.getString("note"), sKOSResource, SKOSProperty.note);
                        addDocumentation(resultSet.getString("editorialNote"), sKOSResource, SKOSProperty.editorialNote);
                        addDocumentation(resultSet.getString("secopeNote"), sKOSResource, SKOSProperty.scopeNote);
                        addDocumentation(resultSet.getString("historyNote"), sKOSResource, SKOSProperty.historyNote);
                        addDocumentation(resultSet.getString("example"), sKOSResource, SKOSProperty.example);
                        addDocumentation(resultSet.getString("changeNote"), sKOSResource, SKOSProperty.changeNote);

                        addAlignementGiven(resultSet.getString("broadMatch"), sKOSResource, SKOSProperty.broadMatch);
                        addAlignementGiven(resultSet.getString("closeMatch"), sKOSResource, SKOSProperty.closeMatch);
                        addAlignementGiven(resultSet.getString("exactMatch"), sKOSResource, SKOSProperty.exactMatch);
                        addAlignementGiven(resultSet.getString("narrowMatch"), sKOSResource, SKOSProperty.narrowMatch);
                        addAlignementGiven(resultSet.getString("relatedmatch"), sKOSResource, SKOSProperty.relatedMatch);

                        addRelationsGiven(resultSet.getString("narrower"), sKOSResource);

                        if (resultSet.getString("broader") != null) {
                            addRelationsGiven(resultSet.getString("broader"), sKOSResource);
                        }

                        sKOSResource.addRelation(idTheso, getUriThesoFromId(idTheso, originalUri, nodePreference), SKOSProperty.inScheme);
                        
                        addReplaced(resultSet.getString("replaces"), sKOSResource, SKOSProperty.replaces);
                        
                        addReplaced(resultSet.getString("replaced_by"), sKOSResource, SKOSProperty.isReplacedBy);
                        
                        if (!StringUtils.isEmpty(resultSet.getString("notation"))) {
                            sKOSResource.addNotation(resultSet.getString("notation"));
                        }

                        addImages(sKOSResource, resultSet.getString("img"));
                        
                        addMembres(sKOSResource, resultSet.getString("membre"), resultSet.getString("IDENTIFIER"));

                        addFacets(sKOSResource, resultSet.getString("facets"), idTheso, originalUri);
                        
                        addExternalResources(sKOSResource, resultSet.getString("externalResources"));

                        addGps(sKOSResource, resultSet.getString("gpsData"));
                        
                        if (resultSet.getString("creator") != null) {
                            sKOSResource.addAgent(resultSet.getString("creator"), SKOSProperty.creator);
                        }
                        
                        if(!StringUtils.isEmpty(resultSet.getString("contributor"))){
                            contributors = resultSet.getString("contributor").split(SEPERATEUR);
                            for (String contributor : contributors) {
                                sKOSResource.addAgent(contributor, SKOSProperty.contributor);
                            }
                        }

                        String created = resultSet.getString("created");
                        if (StringUtils.isNotEmpty(created)) {
                            sKOSResource.addDate(created.substring(0, created.indexOf(" ")), SKOSProperty.created);
                        }

                        String modified = resultSet.getString("modified");
                        if (StringUtils.isNotEmpty(modified)) {
                            sKOSResource.addDate(modified.substring(0, modified.indexOf(" ")), SKOSProperty.modified);
                        }

                    /*    ArrayList<String> first = new ArrayList<>();
                        first.add(resultSet.getString("identifier"));
                        ArrayList<ArrayList<String>> paths = new ArrayList<>();

                        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds,
                                resultSet.getString("identifier"), idTheso, first, paths);
                        ArrayList<String> pathFromArray = getPathFromArray(paths);
                        if (!pathFromArray.isEmpty()) {
                            sKOSResource.setPaths(pathFromArray);
                        }*/

                /*        concepts.add(sKOSResource);
                    //System.out.println(">> " + "Ajout d'un concept " + sKOSResource.getIdentifier());                         
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }*/

        return concepts;
    }    
    private String getSQLRequest(String idTheso, String baseUrl, String idGroup) {
        String baseSQL = "SELECT * FROM ";
        if (StringUtils.isEmpty(idGroup)) {
            baseSQL = baseSQL + "opentheso_get_concepts('" + idTheso + "', '" + baseUrl;
        } else {
            baseSQL = baseSQL + "opentheso_get_concepts_by_group('" + idTheso + "', '" + baseUrl + "', '" + idGroup;
        }
        return baseSQL + "') as x(URI text, TYPE varchar, LOCAL_URI text, IDENTIFIER varchar, ARK_ID varchar, "
                + "prefLab varchar, altLab varchar, altLab_hiden varchar, definition text, example text, editorialNote text, changeNote text, "
                + "secopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                + "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text, "
                + "membre text, created timestamp with time zone, modified timestamp with time zone, img text, creator text, contributor text, "
                + "replaces text, replaced_by text, facets text, externalResources text);";
    }    
}
