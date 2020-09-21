/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.rdf4j;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupLabel;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.bean.importexport.ExportFileBean;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import javax.faces.context.FacesContext;

/**
 *
 * @author Quincy
 */
public class ExportRdf4jHelper {

    private String langueSource;
    private HikariDataSource ds;
    private String formatDate;
    private String adressSite;
    private boolean useArk;
    
    private NodePreference nodePreference;

    private String idTheso;
    private SKOSXmlDocument skosXmlDocument;
    // le thésaurus avec ses traductions
    private NodeThesaurus nodeThesaurus;

    private ArrayList<String> rootGroupList;
    private HashMap<String, String> superGroupHashMap;

    ArrayList<NodeUri> nodeTTs = new ArrayList<>();
    String urlSite;

    private double progress;

    public ExportRdf4jHelper() {
        skosXmlDocument = new SKOSXmlDocument();
        superGroupHashMap = new HashMap();

    }

    public boolean setInfos(HikariDataSource ds,
            String formatDate, boolean useArk, String adressSite,String urlSite) {
        this.ds = ds;
        this.formatDate = formatDate;
        this.useArk = useArk;
        this.adressSite = adressSite;
        this.urlSite = urlSite;

        return true;
    }

    public void exportCollections(HikariDataSource ds, String idTheso){
        GroupHelper groupHelper = new GroupHelper();
        ArrayList<String> rootGroupList = groupHelper.getListIdOfRootGroup(ds, idTheso);
        NodeGroupLabel nodeGroupLabel;
        for (String idGroup : rootGroupList) {
            nodeGroupLabel = groupHelper.getNodeGroupLabel(ds, idGroup, idTheso);
            SKOSResource sKOSResource = new SKOSResource(getUriFromGroup(nodeGroupLabel), SKOSProperty.ConceptGroup);
            sKOSResource.addRelation(getUriFromGroup(nodeGroupLabel), SKOSProperty.microThesaurusOf);
            addChildsGroupRecursive(ds, idTheso, idGroup, sKOSResource);
        }
    }
    
    private void addChildsGroupRecursive(HikariDataSource ds,
            String idTheso,
            String idParent,
            SKOSResource sKOSResource) {
        GroupHelper groupHelper = new GroupHelper();

        ArrayList<String> listIdsOfGroupChilds = groupHelper.getListGroupChildIdOfGroup(ds, idParent, idTheso);
        writeGroupInfo(ds, sKOSResource, idTheso, idParent);

        for (String idOfGroupChild : listIdsOfGroupChilds) {
            sKOSResource = new SKOSResource();
            addChildsGroupRecursive(ds, idTheso, idOfGroupChild, sKOSResource);
        }
    }

    private void writeGroupInfo(HikariDataSource ds, SKOSResource sKOSResource,
            String idTheso, String idOfGroupChild) {

        NodeGroupLabel nodeGroupLabel;
        nodeGroupLabel = new GroupHelper().getNodeGroupLabel(ds, idOfGroupChild, idTheso);

        sKOSResource.setUri(getUriFromGroup(nodeGroupLabel));
        sKOSResource.setProperty(SKOSProperty.ConceptGroup);

        for (NodeGroupTraductions traduction : nodeGroupLabel.getNodeGroupTraductionses()) {
            sKOSResource.addLabel(traduction.getTitle(), traduction.getIdLang(), SKOSProperty.prefLabel);

            //dates
            String created;
            String modified;
            created = traduction.getCreated().toString();
            modified = traduction.getModified().toString();
            if (created != null) {
                sKOSResource.addDate(created, SKOSProperty.created);
            }
            if (modified != null) {
                sKOSResource.addDate(modified, SKOSProperty.modified);
            }
        }

        ArrayList<String> childURI = new GroupHelper().getListGroupChildIdOfGroup(ds, idOfGroupChild, idTheso);
//        ArrayList<NodeUri> nodeUris = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, idOfGroupChild, idTheso);

        ArrayList<NodeUri> nodeUris = new ConceptHelper().getListConceptsOfGroup(ds, idTheso, idOfGroupChild);

        for (NodeUri nodeUri : nodeUris) {
            sKOSResource.addRelation(getUriFromNodeUri(nodeUri, idTheso), SKOSProperty.member);
      //      addMember(ds, nodeUri.getIdConcept(), idTheso, sKOSResource);
        }

        for (String id : childURI) {
            sKOSResource.addRelation(getUriFromId(id), SKOSProperty.subGroup);
            superGroupHashMap.put(id, idOfGroupChild);
        }

        String idSuperGroup = superGroupHashMap.get(idOfGroupChild);

        if (idSuperGroup != null) {
            sKOSResource.addRelation(getUriFromId(idSuperGroup), SKOSProperty.superGroup);
            superGroupHashMap.remove(idOfGroupChild);
        }

        // ajout de la notation
        if (nodeGroupLabel.getNotation() != null && !nodeGroupLabel.getNotation().equals("null")) {
            if(!nodeGroupLabel.getNotation().isEmpty())
                sKOSResource.addNotation(nodeGroupLabel.getNotation());
        }
        skosXmlDocument.addGroup(sKOSResource);
    }


    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////// Nouvelles fonctions ///////////////////////////////    
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////    

    ///// étapes pour exporter un thésaurus sans limitation de langues ni de collections :
    ///// - export des infos sur le thésaurus
    ///// - ajout des balises (hasTopConcept) au thésaurus thésaurus
    ///// - export des collections et les membres
    ///// - export des concepts 
    
/*    public void exportTheso(String idThesaurus) {
        nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(ds, idThesaurus);
        String uri = getUriFromId(nodeThesaurus.getIdThesaurus());
        SKOSResource conceptScheme = new SKOSResource(uri, SKOSProperty.ConceptScheme);
        idTheso = nodeThesaurus.getIdThesaurus();
        String creator;
        String contributor;
        String title;
        String language;

        for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {

            boolean isInSelectedLanguages = false;

            for (NodeLang nodeLang : selectedLanguages) {
                if (nodeLang.getCode().equals(thesaurus.getLanguage())) {
                    isInSelectedLanguages = true;
                    break;
                }
            }

            if (!isInSelectedLanguages) {
                break;
            }

            creator = thesaurus.getCreator();
            contributor = thesaurus.getContributor();
            title = thesaurus.getTitle();
            language = thesaurus.getLanguage();

            /*[...]*/
 /*           if (creator != null && !creator.equalsIgnoreCase("null")) {
                conceptScheme.addCreator(creator, SKOSProperty.creator);
            }
            if (contributor != null && !contributor.equalsIgnoreCase("null")) {
                conceptScheme.addCreator(creator, SKOSProperty.contributor);
            }
            if (title != null && language != null) {
                conceptScheme.addLabel(title, language, SKOSProperty.prefLabel);
            }

            //dates
            String created = thesaurus.getCreated().toString();
            String modified = thesaurus.getModified().toString();
            if (created != null) {
                conceptScheme.addDate(created, SKOSProperty.created);
            }
            if (modified != null) {
                conceptScheme.addDate(modified, SKOSProperty.modified);
            }

        }

        skosXmlDocument.setConceptScheme(conceptScheme);

    }*/
    

    
        
        
        
        
        
        
        
        
        
        
        
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /////////////////////Fin des nouvelles fonctions ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////// 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public void addConcept(String idThesaurus, ExportFileBean downloadBean, List<NodeLangTheso> selectedLanguages) {
        // récupération de tous les concepts
        for (NodeUri nodeTT1 : nodeTTs) {
            SKOSResource sKOSResource = new SKOSResource();
            sKOSResource.addRelation(getUriFromId(idTheso), SKOSProperty.topConceptOf);
            //fils top concept
            addFilsConceptRecursif(idThesaurus, nodeTT1.getIdConcept(), sKOSResource, downloadBean, selectedLanguages);
        }
    }

    public void addBranch(String idThesaurus, String idConcept) {
        idTheso = idThesaurus;
        addFilsConceptRecursif(idTheso, idConcept, new SKOSResource());
    }
    
    public void addSignleConcept(String idThesaurus, String idConcept) {
        idTheso = idThesaurus;
        ConceptHelper conceptHelper = new ConceptHelper();
        SKOSResource sKOSResource = new SKOSResource();
        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idThesaurus, false);

        if (nodeConcept == null) {
            return;
        }

    //    concept.setUri(getUriFromId(idConcept));
        sKOSResource.setUri(getUri(nodeConcept));
        sKOSResource.setProperty(SKOSProperty.Concept);

        // prefLabel
        for (NodeTermTraduction traduction : nodeConcept.getNodeTermTraductions()) {
            sKOSResource.addLabel(traduction.getLexicalValue(), traduction.getLang(), SKOSProperty.prefLabel);
        }
        // altLabel
        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
            if(nodeEM.isHiden())
                sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.hiddenLabel);
            else
                sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.altLabel);            
        }
        ArrayList<NodeNote> nodeNotes = nodeConcept.getNodeNoteConcept();
        nodeNotes.addAll(nodeConcept.getNodeNoteTerm());
        addNoteGiven(nodeNotes, sKOSResource);
        addGPSGiven(nodeConcept.getNodeGps(), sKOSResource);
        addAlignementGiven(nodeConcept.getNodeAlignmentsList(), sKOSResource);
        addRelationGiven(nodeConcept.getNodeListOfBT(), nodeConcept.getNodeListOfNT(),
                nodeConcept.getNodeListIdsOfRT(), sKOSResource, nodeConcept.getConcept().getIdThesaurus());

        String notation = nodeConcept.getConcept().getNotation();
        String created = nodeConcept.getConcept().getCreated().toString();
        String modified = nodeConcept.getConcept().getModified().toString();

        if (notation != null && !notation.equals("null")) {
            sKOSResource.addNotation(notation);
        }
        if (created != null) {
            sKOSResource.addDate(created, SKOSProperty.created);
        }
        if (modified != null) {
            sKOSResource.addDate(modified, SKOSProperty.modified);
        }
        sKOSResource.addRelation(getUriFromId(idTheso), SKOSProperty.inScheme);
        for (NodeUri nodeUri : nodeConcept.getNodeListIdsOfConceptGroup()) {
            sKOSResource.addRelation(getUriGroupFromNodeUri(nodeUri,idTheso), SKOSProperty.memberOf);
        }           
        sKOSResource.addIdentifier(idConcept, SKOSProperty.identifier);
        

        ArrayList<String> first = new ArrayList<>();
        first.add(idConcept);
        ArrayList<ArrayList<String>> paths = new ArrayList<>();
            
        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds, idConcept, idThesaurus, first, paths);
        ArrayList<String> pathFromArray = getPathFromArray(paths);
        if(!pathFromArray.isEmpty())
            sKOSResource.setPaths(pathFromArray);
    //    sKOSResource.setPath("A/B/C/D/"+idConcept);
        skosXmlDocument.addconcept(sKOSResource);
    }
    
    private ArrayList<String> getPathFromArray(ArrayList<ArrayList<String>> paths) {
        String pathFromArray = "";
        ArrayList<String> allPath = new ArrayList<>();
        for (ArrayList<String> path : paths) {
            for (String string1 : path) {
                if(pathFromArray.isEmpty())
                    pathFromArray = string1;
                else
                    pathFromArray = pathFromArray + "##" + string1;
            }
            allPath.add(pathFromArray);
            pathFromArray = "";
        }
        return allPath;
    }

    /**
     * Permet de préparer tous les alignements d'un thésaurus pour l'export
     * @param selectedGroups
     * @param idThesaurus 
     */
    public void getAllAlignment(List<NodeGroup> selectedGroups, String idThesaurus) {
        idTheso = idThesaurus;
        
        ConceptHelper conceptHelper = new ConceptHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        ArrayList<NodeAlignmentSmall> nodeAlignmentSmalls;
        ArrayList<String> allIds;

        for (NodeGroup selectedGroup : selectedGroups) {
            allIds = conceptHelper.getAllIdConceptOfThesaurusByGroup(ds, idTheso, selectedGroup.getConceptGroup().getIdgroup());
            for (String idConcept : allIds) {
                nodeAlignmentSmalls = alignmentHelper.getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus);
                if(!nodeAlignmentSmalls.isEmpty()) {
                    SKOSResource sKOSResource = new SKOSResource();
                    sKOSResource.setUri(getUriFromId(idConcept));
                    sKOSResource.setProperty(SKOSProperty.Concept);

                    addAlignementGiven(nodeAlignmentSmalls, sKOSResource);
                    sKOSResource.addIdentifier(idConcept, SKOSProperty.identifier);
                    skosXmlDocument.addconcept(sKOSResource);
                }
            }
        }
    }

    private void addFilsConceptRecursif(String idThesaurus, String idPere, SKOSResource sKOSResource, ExportFileBean downloadBean, List<NodeLangTheso> selectedLanguages) {

        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList<String> listIdsOfConceptChildren = conceptHelper.getListChildrenOfConcept(ds, idPere, idThesaurus);

        writeConceptInfo(conceptHelper, sKOSResource, idThesaurus, idPere, downloadBean, selectedLanguages);

        for (String idOfConceptChildren : listIdsOfConceptChildren) {
            sKOSResource = new SKOSResource();
            addFilsConceptRecursif(idThesaurus, idOfConceptChildren, sKOSResource, downloadBean, selectedLanguages);
        }
    }

    private void addFilsConceptRecursif(String idThesaurus, String idPere, SKOSResource sKOSResource) {

        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList<String> listIdsOfConceptChildren = conceptHelper.getListChildrenOfConcept(ds, idPere, idThesaurus);

        writeConceptInfo(conceptHelper, sKOSResource, idThesaurus, idPere);

        for (String idOfConceptChildren : listIdsOfConceptChildren) {
            sKOSResource = new SKOSResource();
            addFilsConceptRecursif(idThesaurus, idOfConceptChildren, sKOSResource);
        }

    }

    private void writeConceptInfo(ConceptHelper conceptHelper, SKOSResource sKOSResource,
            String idThesaurus, String idOfConceptChildren, ExportFileBean downloadBean, List<NodeLangTheso> selectedLanguages) {

        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idOfConceptChildren, idThesaurus, false);

        if (nodeConcept == null) {
            return;
        }

        sKOSResource.setUri(getUri(nodeConcept));
        sKOSResource.setProperty(SKOSProperty.Concept);

        // prefLabel
        for (NodeTermTraduction traduction : nodeConcept.getNodeTermTraductions()) {

            boolean isInselectedLanguages = false;
            for (NodeLangTheso nodeLang : selectedLanguages) {
                if (nodeLang.getCode().equals(traduction.getLang())) {
                    isInselectedLanguages = true;
                    break;
                }

            }
            if (isInselectedLanguages) {
                sKOSResource.addLabel(traduction.getLexicalValue(), traduction.getLang(), SKOSProperty.prefLabel);
            }
        }
        // altLabel
        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
            boolean isInselectedLanguages = false;
            for (NodeLangTheso nodeLang : selectedLanguages) {
                if (nodeLang.getCode().equals(nodeEM.getLang())) {
                    isInselectedLanguages = true;

                    break;
                }

            }
            if (isInselectedLanguages) {
                if(nodeEM.isHiden())
                    sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.hiddenLabel);
                else
                    sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.altLabel);
            }
        }
        ArrayList<NodeNote> nodeNotes = nodeConcept.getNodeNoteConcept();
        nodeNotes.addAll(nodeConcept.getNodeNoteTerm());
        addNoteGiven(nodeNotes, sKOSResource, selectedLanguages);
        addGPSGiven(nodeConcept.getNodeGps(), sKOSResource);
        addAlignementGiven(nodeConcept.getNodeAlignmentsList(), sKOSResource);
        addRelationGiven(nodeConcept.getNodeListOfBT(), nodeConcept.getNodeListOfNT(),
                nodeConcept.getNodeListIdsOfRT(), sKOSResource, nodeConcept.getConcept().getIdThesaurus());

        String notation = nodeConcept.getConcept().getNotation();
        String created = nodeConcept.getConcept().getCreated().toString();
        String modified = nodeConcept.getConcept().getModified().toString();

        if (notation != null && !notation.equals("null")) {
            sKOSResource.addNotation(notation);
        }
        if (created != null) {
            sKOSResource.addDate(created, SKOSProperty.created);
        }
        if (modified != null) {
            sKOSResource.addDate(modified, SKOSProperty.modified);
        }

        sKOSResource.addRelation(getUriFromId(idTheso), SKOSProperty.inScheme);
        for (NodeUri nodeUri : nodeConcept.getNodeListIdsOfConceptGroup()) {
            sKOSResource.addRelation(getUriGroupFromNodeUri(nodeUri,idTheso), SKOSProperty.memberOf);
        }        
        
        sKOSResource.addIdentifier(nodeConcept.getConcept().getIdConcept(), SKOSProperty.identifier);

        downloadBean.setProgressBar(downloadBean.getProgressStep() + downloadBean.getProgressBar());

        skosXmlDocument.addconcept(sKOSResource);

    }

    private void writeConceptInfo(ConceptHelper conceptHelper, SKOSResource sKOSResource,
            String idThesaurus, String idOfConceptChildren) {

        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idOfConceptChildren, idThesaurus, false);

        if (nodeConcept == null) {
            return;
        }

        sKOSResource.setUri(getUri(nodeConcept));
        sKOSResource.setProperty(SKOSProperty.Concept);

        // prefLabel
        for (NodeTermTraduction traduction : nodeConcept.getNodeTermTraductions()) {

            sKOSResource.addLabel(traduction.getLexicalValue(), traduction.getLang(), SKOSProperty.prefLabel);
        }
        // altLabel
        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {

            sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.altLabel);
        }
        ArrayList<NodeNote> nodeNotes = nodeConcept.getNodeNoteConcept();
        nodeNotes.addAll(nodeConcept.getNodeNoteTerm());
        addNoteGiven(nodeNotes, sKOSResource);
        addGPSGiven(nodeConcept.getNodeGps(), sKOSResource);
        addAlignementGiven(nodeConcept.getNodeAlignmentsList(), sKOSResource);
        addRelationGiven(nodeConcept.getNodeListOfBT(), nodeConcept.getNodeListOfNT(),
                nodeConcept.getNodeListIdsOfRT(), sKOSResource, nodeConcept.getConcept().getIdThesaurus());

        String notation = nodeConcept.getConcept().getNotation();
        String created = nodeConcept.getConcept().getCreated().toString();
        String modified = nodeConcept.getConcept().getModified().toString();

        if (notation != null && !notation.equals("null")) {
            sKOSResource.addNotation(notation);
        }
        if (created != null) {
            sKOSResource.addDate(created, SKOSProperty.created);
        }
        if (modified != null) {
            sKOSResource.addDate(modified, SKOSProperty.modified);
        }

        sKOSResource.addRelation(getUriFromId(idTheso), SKOSProperty.inScheme);
        for (NodeUri nodeUri : nodeConcept.getNodeListIdsOfConceptGroup()) {
            sKOSResource.addRelation(getUriGroupFromNodeUri(nodeUri,idTheso), SKOSProperty.memberOf);
        }
        
        sKOSResource.addIdentifier(nodeConcept.getConcept().getIdConcept(), SKOSProperty.identifier);
        skosXmlDocument.addconcept(sKOSResource);

    }

    private void addMember(String id, String idThesaurus, SKOSResource resource) {

    //    System.out.println("idConcept = " + id);
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<NodeHieraRelation> listChildren = relationsHelper.getListNT(ds, id, idThesaurus);

        for (NodeHieraRelation idChildren : listChildren) {
    //        System.out.println(idChildren.getUri().getIdConcept());
            resource.addRelation(getUriFromNodeUri(idChildren.getUri(), idThesaurus), SKOSProperty.member);
            addMember(idChildren.getUri().getIdConcept(), idThesaurus, resource);
        }

    }


    public void addGroup(String idThesaurus, List<NodeLangTheso> selectedLanguages, List<NodeGroup> selectedGroups) {

        if(idTheso == null || idTheso.isEmpty())
            idTheso = idThesaurus;
        
        GroupHelper groupHelper = new GroupHelper();
        rootGroupList = groupHelper.getListIdOfRootGroup(ds, idTheso);
        NodeGroupLabel nodeGroupLabel;
        
        
        for (String idGroup : rootGroupList) {
            for (NodeGroup nodeGroup : selectedGroups) {
                if (nodeGroup.getConceptGroup().getIdgroup().equals(idGroup)) {
                   
                    nodeGroupLabel = groupHelper.getNodeGroupLabel(ds, idGroup, idThesaurus);
                    
                    SKOSResource sKOSResource = new SKOSResource(getUriFromGroup(nodeGroupLabel), SKOSProperty.ConceptGroup);                    
                    sKOSResource.addRelation(getUriFromGroup(nodeGroupLabel), SKOSProperty.microThesaurusOf);
                    addFilsGroupRcursif(idThesaurus, idGroup, sKOSResource, selectedLanguages);                    
                }
            }
        }
    }
    
    

    private void addFilsGroupRcursif(String idThesaurus, String idPere, SKOSResource sKOSResource, List<NodeLangTheso> selectedLanguages) {

        GroupHelper groupHelper = new GroupHelper();

        ArrayList<String> listIdsOfGroupChildren = groupHelper.getListGroupChildIdOfGroup(ds, idPere, idThesaurus);

        writeGroupInfo(sKOSResource, idThesaurus, idPere, selectedLanguages);

        for (String idOfGroupChildren : listIdsOfGroupChildren) {
            sKOSResource = new SKOSResource();

            //writeGroupInfo(groupHelper, group, idThesaurus, idOfGroupChildren, selectedLanguages);
            //if (!groupHelper.getListGroupChildIdOfGroup(ds, idOfGroupChildren, idThesaurus).isEmpty()) {
            addFilsGroupRcursif(idThesaurus, idOfGroupChildren, sKOSResource, selectedLanguages);
            //}
        }
    }

    private void writeGroupInfo(SKOSResource sKOSResource,
            String idThesaurus, String idOfGroupChildren, List<NodeLangTheso> selectedLanguages) {

        NodeGroupLabel nodeGroupLabel;
        nodeGroupLabel = new GroupHelper().getNodeGroupLabel(ds, idOfGroupChildren, idThesaurus);

        sKOSResource.setUri(getUriFromGroup(nodeGroupLabel));
        sKOSResource.setProperty(SKOSProperty.ConceptGroup);

        for (NodeGroupTraductions traduction : nodeGroupLabel.getNodeGroupTraductionses()) {

            boolean isInSelectedLanguages = false;

            for (NodeLangTheso nodeLang : selectedLanguages) {

                if (nodeLang.getCode().equals(traduction.getIdLang())) {
                    isInSelectedLanguages = true;
                    break;
                }
            }

            if (!isInSelectedLanguages) {
                continue;
            }

            sKOSResource.addLabel(traduction.getTitle(), traduction.getIdLang(), SKOSProperty.prefLabel);

            //dates
            String created;
            String modified;
            created = traduction.getCreated().toString();
            modified = traduction.getModified().toString();
            if (created != null) {
                sKOSResource.addDate(created, SKOSProperty.created);
            }
            if (modified != null) {
                sKOSResource.addDate(modified, SKOSProperty.modified);
            }

        }

        ArrayList<String> childURI = new GroupHelper().getListGroupChildIdOfGroup(ds, idOfGroupChildren, idThesaurus);
        ArrayList<NodeUri> nodeUris = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, idOfGroupChildren, idThesaurus);

        for (NodeUri nodeUri : nodeUris) {
            sKOSResource.addRelation(getUriFromNodeUri(nodeUri, idThesaurus), SKOSProperty.member);
            addMember(nodeUri.getIdConcept(), idThesaurus, sKOSResource);

        }

        for (String id : childURI) {
            sKOSResource.addRelation(getUriFromId(id), SKOSProperty.subGroup);
            superGroupHashMap.put(id, idOfGroupChildren);
        }

    //    addNotes(idOfGroupChildren, group, selectedLanguages);
    //    addGPS(idOfGroupChildren, group);
    //    addAlignement(idOfGroupChildren, group);
    //    addRelation(idOfGroupChildren, group);

        String idSuperGroup = superGroupHashMap.get(idOfGroupChildren);

        if (idSuperGroup != null) {
            sKOSResource.addRelation(getUriFromId(idSuperGroup), SKOSProperty.superGroup);
            superGroupHashMap.remove(idOfGroupChildren);
        }
        
        // ajout de la notation
        if (nodeGroupLabel.getNotation() != null && !nodeGroupLabel.getNotation().equals("null")) {
            if(!nodeGroupLabel.getNotation().isEmpty())
                sKOSResource.addNotation(nodeGroupLabel.getNotation());
        }

        skosXmlDocument.addGroup(sKOSResource);

        //liste top concept
        nodeTTs.addAll(nodeUris);
        for (NodeUri topConcept : nodeTTs) {
            if(skosXmlDocument.getConceptScheme() != null)
                skosXmlDocument.getConceptScheme().addRelation(getUriFromNodeUri(topConcept, idThesaurus), SKOSProperty.hasTopConcept);
        }

    }
    
    /**
     * permet d'ajouter une branche entière d'un domaine ou microthésaurus
     * @param idThesaurus
     * @param idGroup 
     * #MR
     */
    public void addWholeGroup(String idThesaurus, String idGroup) {
        SKOSResource sKOSResource = new SKOSResource(getUriFromId(idGroup), SKOSProperty.ConceptGroup);
        sKOSResource.addRelation(getUriFromId(idThesaurus), SKOSProperty.microThesaurusOf);
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        List<NodeLangTheso> languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(ds, idThesaurus);
        
        addFilsGroupRcursif(idThesaurus, idGroup, sKOSResource, languagesOfTheso);
        
        for (NodeUri nodeTT1 : nodeTTs) {
            SKOSResource sKOSResource1 = new SKOSResource();
            sKOSResource1.addRelation(getUriFromId(idTheso), SKOSProperty.topConceptOf);
            //fils top concept
            addFilsConceptRecursif(idThesaurus, nodeTT1.getIdConcept(), sKOSResource1);
        }      
    }
    
    
    
    
    public void addSingleGroup(String idThesaurus, String idGroup) {

        NodeGroupLabel nodeGroupLabel;
        nodeGroupLabel = new GroupHelper().getNodeGroupLabel(ds, idGroup, idThesaurus);
        SKOSResource sKOSResource = new SKOSResource();
        sKOSResource.setUri(getUriFromGroup(nodeGroupLabel));
        sKOSResource.setProperty(SKOSProperty.ConceptGroup);

        for (NodeGroupTraductions traduction : nodeGroupLabel.getNodeGroupTraductionses()) {
            sKOSResource.addLabel(traduction.getTitle(), traduction.getIdLang(), SKOSProperty.prefLabel);
            //dates
            String created;
            String modified;
            created = traduction.getCreated().toString();
            modified = traduction.getModified().toString();
            if (created != null) {
                sKOSResource.addDate(created, SKOSProperty.created);
            }
            if (modified != null) {
                sKOSResource.addDate(modified, SKOSProperty.modified);
            }
        }
        
        // pour exporter les membres (tous les concepts du group
    /*    ArrayList<NodeUri> nodeUris = new ConceptHelper().getListIdsOfTopConceptsForExport(ds, idGroup, idThesaurus);
        for (NodeUri nodeUri : nodeUris) {
            sKOSResource.addRelation(getUriFromNodeUri(nodeUri, idThesaurus), SKOSProperty.member);
            addMember(nodeUri.getIdConcept(), idThesaurus, sKOSResource);
        }*/

        ArrayList<String> childURI = new GroupHelper().getListGroupChildIdOfGroup(ds, idGroup, idThesaurus);
        HashMap<String, String> superGroupHashMapTemp = new HashMap();
        for (String id : childURI) {
            sKOSResource.addRelation(getUriFromId(id), SKOSProperty.subGroup);
            superGroupHashMapTemp.put(id, idGroup);
        }
        String idSuperGroup = superGroupHashMapTemp.get(idGroup);

        if (idSuperGroup != null) {
            sKOSResource.addRelation(getUriFromId(idSuperGroup), SKOSProperty.superGroup);
            superGroupHashMapTemp.remove(idGroup);
        }
        sKOSResource.addIdentifier(idGroup, SKOSProperty.identifier);
        skosXmlDocument.addGroup(sKOSResource);
    }    

    public void addThesaurus(String idThesaurus, List<NodeLangTheso> selectedLanguages) {

        nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(ds, idThesaurus);
        String uri = getUriFromId(nodeThesaurus.getIdThesaurus());
        SKOSResource conceptScheme = new SKOSResource(uri, SKOSProperty.ConceptScheme);
        idTheso = nodeThesaurus.getIdThesaurus();
        String creator;
        String contributor;
        String title;
        String language;

        for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {

            boolean isInSelectedLanguages = false;

            for (NodeLangTheso nodeLang : selectedLanguages) {
                if (nodeLang.getCode().equals(thesaurus.getLanguage())) {
                    isInSelectedLanguages = true;
                    break;
                }
            }

            if (!isInSelectedLanguages) {
                break;
            }

            creator = thesaurus.getCreator();
            contributor = thesaurus.getContributor();
            title = thesaurus.getTitle();
            language = thesaurus.getLanguage();

            /*[...]*/
            if (creator != null && !creator.equalsIgnoreCase("null")) {
                conceptScheme.addCreator(creator, SKOSProperty.creator);
            }
            if (contributor != null && !contributor.equalsIgnoreCase("null")) {
                conceptScheme.addCreator(creator, SKOSProperty.contributor);
            }
            if (title != null && language != null) {
                conceptScheme.addLabel(title, language, SKOSProperty.prefLabel);
            }

            //dates
            String created = thesaurus.getCreated().toString();
            String modified = thesaurus.getModified().toString();
            if (created != null) {
                conceptScheme.addDate(created, SKOSProperty.created);
            }
            if (modified != null) {
                conceptScheme.addDate(modified, SKOSProperty.modified);
            }

        }

        skosXmlDocument.setConceptScheme(conceptScheme);

    }

    private void addAlignementGiven(ArrayList<NodeAlignmentSmall> nodeAlignments, SKOSResource resource) {
        for (NodeAlignmentSmall alignment : nodeAlignments) {

            int prop = -1;
            switch (alignment.getAlignement_id_type()) {

                case 1:
                    prop = SKOSProperty.exactMatch;
                    break;
                case 2:
                    prop = SKOSProperty.closeMatch;
                    break;
                case 3:
                    prop = SKOSProperty.broadMatch;
                    break;
                case 4:
                    prop = SKOSProperty.relatedMatch;
                    break;
                case 5:
                    prop = SKOSProperty.narrowMatch;
                    break;
            }
            resource.addMatch(alignment.getUri_target(), prop);
        }
    }

    private void addRelationGiven(ArrayList<NodeHieraRelation> btList, ArrayList<NodeHieraRelation> ntList,
            ArrayList<NodeHieraRelation> rtList, SKOSResource resource, String idTheso) {
        for (NodeHieraRelation rt : rtList) {
            int prop;

            switch (rt.getRole()) {
                case "RHP":
                    prop = SKOSProperty.relatedHasPart;
                    break;
                case "RPO":
                    prop = SKOSProperty.relatedPartOf;
                    break;
                default:
                    prop = SKOSProperty.related;
            }
            resource.addRelation(getUriFromNodeUri(rt.getUri(), idTheso), prop);
        }
        for (NodeHieraRelation nt : ntList) {
            int prop;
            switch (nt.getRole()) {
                case "NTG":
                    prop = SKOSProperty.narrowerGeneric;
                    break;
                case "NTP":
                    prop = SKOSProperty.narrowerPartitive;
                    break;
                case "NTI":
                    prop = SKOSProperty.narrowerInstantial;
                    break;
                default:
                    prop = SKOSProperty.narrower;
            }
            resource.addRelation(getUriFromNodeUri(nt.getUri(), idTheso), prop);
        }
        for (NodeHieraRelation bt : btList) {

            int prop;
            switch (bt.getRole()) {
                case "BTG":
                    prop = SKOSProperty.broaderGeneric;
                    break;
                case "BTP":
                    prop = SKOSProperty.broaderPartitive;
                    break;
                case "BTI":
                    prop = SKOSProperty.broaderInstantial;
                    break;
                default:
                    prop = SKOSProperty.broader;
            }
            resource.addRelation(getUriFromNodeUri(bt.getUri(), idTheso), prop);
        }
    }

    private void addNoteGiven(ArrayList<NodeNote> nodeNotes, SKOSResource resource, List<NodeLangTheso> selectedLanguages) {
        for (NodeNote note : nodeNotes) {

            boolean isInselectedLanguages = false;
            for (NodeLangTheso nodeLang : selectedLanguages) {
                if (nodeLang.getCode().equals(note.getLang())) {
                    isInselectedLanguages = true;

                    break;
                }

            }
            if (!isInselectedLanguages) {
                continue;
            }

            int prop;
            switch (note.getNotetypecode()) {
                case "scopeNote":
                    prop = SKOSProperty.scopeNote;
                    break;
                case "historyNote":
                    prop = SKOSProperty.historyNote;
                    break;
                case "definition":
                    prop = SKOSProperty.definition;
                    break;
                case "editorialNote":
                    prop = SKOSProperty.editorialNote;
                    break;
                default:
                    prop = SKOSProperty.note;
                    break;
            }
            resource.addDocumentation(note.getLexicalvalue(), note.getLang(), prop);
        }
    }

    private void addNoteGiven(ArrayList<NodeNote> nodeNotes, SKOSResource resource) {
        for (NodeNote note : nodeNotes) {
            int prop;
            switch (note.getNotetypecode()) {
                case "scopeNote":
                    prop = SKOSProperty.scopeNote;
                    break;
                case "historyNote":
                    prop = SKOSProperty.historyNote;
                    break;
                case "definition":
                    prop = SKOSProperty.definition;
                    break;
                case "editorialNote":
                    prop = SKOSProperty.editorialNote;
                    break;
                default:
                    prop = SKOSProperty.note;
                    break;
            }
            resource.addDocumentation(note.getLexicalvalue(), note.getLang(), prop);
        }
    }

    private void addGPSGiven(NodeGps gps, SKOSResource resource) {
        if (gps == null) {
            return;
        }

        double lat = gps.getLatitude();
        double lon = gps.getLongitude();

        resource.setGPSCoordinates(new SKOSGPSCoordinates(lat, lon));

    }

     public String getUriFromId(String id) {
        String uri;

        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/" + id;
        } else {
            uri = getPath() + "/" + id;
        }
        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @param nodeConceptExport
     * @return
     */
    private String getUri(NodeConceptExport nodeConceptExport) {
        String uri = "";
        if (nodeConceptExport == null) {
            //      System.out.println("nodeConcept = Null");
            return uri;
        }
        if (nodeConceptExport.getConcept() == null) {
            //    System.out.println("nodeConcept.getConcept = Null");
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if(nodePreference.isOriginalUriIsArk()) {
            if (nodeConceptExport.getConcept().getIdArk() != null) {
                if (!nodeConceptExport.getConcept().getIdArk().trim().isEmpty()) {
                    uri = nodePreference.getUriArk()+ nodeConceptExport.getConcept().getIdArk();
                    return uri;
                }
            }
        }
        
        if(nodePreference.isOriginalUriIsHandle()) {
            // URI de type Handle
            if (nodeConceptExport.getConcept().getIdHandle() != null) {
                if (!nodeConceptExport.getConcept().getIdHandle().trim().isEmpty()) {
                    uri = "https://hdl.handle.net/" + nodeConceptExport.getConcept().getIdHandle();
                    return uri;
                }
            }
        }
        // si on ne trouve pas ni Handle, ni Ark
        //    uri = nodePreference.getCheminSite() + nodeConceptExport.getConcept().getIdConcept();
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri()+ "/?idc=" + nodeConceptExport.getConcept().getIdConcept()
                        + "&idt=" + nodeConceptExport.getConcept().getIdThesaurus();
        } else {
            uri = getPath()+ "/?idc=" + nodeConceptExport.getConcept().getIdConcept()
                        + "&idt=" + nodeConceptExport.getConcept().getIdThesaurus();
        }
//        uri = nodePreference.getCheminSite() + nodeConceptExport.getConcept().getIdConcept();

        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @param nodeConceptExport
     * @return
     */
    private String getUriFromGroup(NodeGroupLabel nodeGroupLabel) {
        String uri = "";
        if (nodeGroupLabel == null) {
            //      System.out.println("nodeConcept = Null");
            return uri;
        }
        if (nodeGroupLabel.getIdGroup() == null) {
            //    System.out.println("nodeConcept.getConcept = Null");
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if(nodePreference.isOriginalUriIsArk()) {
            if (nodeGroupLabel.getIdArk() != null) {
                if (!nodeGroupLabel.getIdArk().trim().isEmpty()) {
                    uri = nodePreference.getUriArk() + nodeGroupLabel.getIdArk();
                    return uri;
                }
            }
        }
        if(nodePreference.isOriginalUriIsHandle()) {        
            // URI de type Handle
            if (nodeGroupLabel.getIdHandle() != null) {
                if (!nodeGroupLabel.getIdHandle().trim().isEmpty()) {
                    uri = "https://hdl.handle.net/" + nodeGroupLabel.getIdHandle();
                    return uri;
                }
            }
        }
        // si on ne trouve pas ni Handle, ni Ark
//        uri = nodePreference.getCheminSite() + nodeGroupLabel.getIdGroup();
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idg=" + nodeGroupLabel.getIdGroup()
                        + "&idt=" + nodeGroupLabel.getIdThesaurus();
        } else {
            uri = getPath() + "/?idg=" + nodeGroupLabel.getIdGroup()
                        + "&idt=" + nodeGroupLabel.getIdThesaurus();
        }

    //    uri = nodePreference.getCheminSite() + nodeGroupLabel.getIdGroup();
        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @param nodeConceptExport
     * @return
     */
    private String getUriGroupFromNodeUri(NodeUri nodeUri, String idTheso) {
        String uri = "";
        if (nodeUri == null) {
            //      System.out.println("nodeConcept = Null");
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if (nodeUri.getIdArk() != null) {
            if (!nodeUri.getIdArk().trim().isEmpty()) {
                uri = nodePreference.getUriArk() + nodeUri.getIdArk();
                return uri;
            }
        }
        // URI de type Handle
        if (nodeUri.getIdHandle() != null) {
            if (!nodeUri.getIdHandle().trim().isEmpty()) {
                uri = "https://hdl.handle.net/" + nodeUri.getIdHandle();
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        //    uri = nodePreference.getCheminSite() + nodeUri.getIdConcept();

        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idg=" + nodeUri.getIdConcept()
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idg=" + nodeUri.getIdConcept()
                            + "&idt=" + idTheso;
        }


  //      uri = nodePreference.getOriginalUri() + nodeUri.getIdConcept();

        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @param nodeConceptExport
     * @return
     */
    private String getUriFromNodeUri(NodeUri nodeUri, String idTheso) {
        String uri = "";
        if (nodeUri == null) {
            //      System.out.println("nodeConcept = Null");
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark

        if(nodePreference.isOriginalUriIsArk()) {
            if (nodeUri.getIdArk() != null) {
                if (!nodeUri.getIdArk().trim().isEmpty()) {
                    uri = nodePreference.getUriArk() + nodeUri.getIdArk();
                    return uri;
                }
            }
        }
        if(nodePreference.isOriginalUriIsHandle()) {
            // URI de type Handle
            if (nodeUri.getIdHandle() != null) {
                if (!nodeUri.getIdHandle().trim().isEmpty()) {
                    uri = "https://hdl.handle.net/" + nodeUri.getIdHandle();
                    return uri;
                }
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idc=" + nodeUri.getIdConcept()
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idc=" + nodeUri.getIdConcept()
                + "&idt=" + idTheso;
        }

                        //+ "&amp;idt=" + idTheso;
    //    uri = nodePreference.getCheminSite() + nodeUri.getIdConcept();
        return uri;
    }  

    /**
     * permet de retourner le Path de l'application
     * exp:  //http://localhost:8082/opentheso2
     * @return
     */
    private String getPath(){
        if(FacesContext.getCurrentInstance() != null) {
            String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
            path = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
            return path;
        } else
            return "https://localhost";

    }

    public SKOSXmlDocument getSkosXmlDocument() {
        return skosXmlDocument;
    }

    public void setSkosXmlDocument(SKOSXmlDocument skosXmlDocument) {
        this.skosXmlDocument = skosXmlDocument;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public ArrayList<NodeUri> getNodeTTs() {
        return nodeTTs;
    }

    
}
