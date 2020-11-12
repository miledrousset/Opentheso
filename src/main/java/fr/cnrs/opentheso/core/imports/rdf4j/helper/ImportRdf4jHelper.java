/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.imports.rdf4j.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.ConceptGroupLabel;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GpsHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.ImagesHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTerm;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.tools.StringPlus;

import fr.cnrs.opentheso.bean.condidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.condidat.dto.VoteDto;
import fr.cnrs.opentheso.skosapi.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Quincy
 */
public class ImportRdf4jHelper {

    private ArrayList<String> idGroups; // tous les idGroupes du thésaurus
    private String langueSource;
    private HikariDataSource ds;
    private String formatDate;
    private int idUser;
    private int idGroupUser;
    private ArrayList<String> idLangsFound;

//    private boolean useArk;
    private String selectedIdentifier;
    private String prefixHandle;

    private NodePreference nodePreference;
    private StringBuilder message;

    private int resourceCount;

    HashMap<String, String> memberHashMap = new HashMap<>();
    HashMap<String, String> groupSubGroup = new HashMap<>(); // pour garder en mémoire les relations de types (member) pour détecter ce qui est groupe ou concept 

    ArrayList<String> hasTopConcceptList = new ArrayList<>();

    private SKOSXmlDocument skosXmlDocument;
    
    boolean isFirst = true;

    public ImportRdf4jHelper() {
        idGroups = new ArrayList<>();
        message = new StringBuilder();
        idLangsFound = new ArrayList<>();
        isFirst = true;
    }

    /**
     * Classe pour construire un concept sépcifique
     */
    class AddConceptsStruct {

        Concept concept;
        ConceptHelper conceptHelper;
        SKOSResource conceptResource;
        NodeStatus status;
        String collectionToAdd;
        // pour intégrer les coordonnées GPS 
        NodeGps nodeGps = new NodeGps();
        GpsHelper gpsHelper = new GpsHelper();
        //ajout des termes et traductions
        NodeTerm nodeTerm = new NodeTerm();
        ArrayList<NodeTermTraduction> nodeTermTraductionList = new ArrayList<>();
        //Enregister les synonymes et traductions
        ArrayList<NodeEM> nodeEMList = new ArrayList<>();
        // ajout des notes
        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        //ajout des relations 
        ArrayList<HierarchicalRelationship> hierarchicalRelationships = new ArrayList<>();
        // ajout des relations Groups
        ArrayList<String> idGrps = new ArrayList<>();
        // ajout des alignements 
        ArrayList<NodeAlignment> nodeAlignments = new ArrayList<>();
        TermHelper termHelper = new TermHelper();
        NoteHelper noteHelper = new NoteHelper();
        boolean isTopConcept = false;
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        ImagesHelper imagesHelper = new ImagesHelper();
        List<VoteDto> votes = new ArrayList<>();
        List<MessageDto> messages = new ArrayList<>();
        
        ArrayList<String> nodeImages = new ArrayList<>();
        Term term = new Term();

    }

    /**
     * initialisation des paramètres d'import
     *
     * @param ds
     * @param formatDate
     * @param idGroupUser
     * @param idUser
     * @param langueSource
     * @return
     */
    public boolean setInfos(HikariDataSource ds,
            String formatDate, int idUser,
            int idGroupUser,
            String langueSource) {
        this.ds = ds;
        this.formatDate = formatDate;
        this.idUser = idUser;
        this.idGroupUser = idGroupUser;
        this.langueSource = langueSource;
        return true;
    }

    /**
     * Cette fonction permet de créer un thésaurus avec ses traductions (Import)
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String addThesaurus() throws SQLException {
        Thesaurus thesaurus = new Thesaurus();

        SKOSResource conceptScheme = skosXmlDocument.getConceptScheme();
        if (conceptScheme == null) {
            message.append("Erreur SKOS !!! manque balise conceptSheme");
            return null;
        }

        String creator = "";
        String contributor = "";

        for (SKOSCreator c : conceptScheme.getCreatorList()) {
            if (c.getProperty() == SKOSProperty.creator) {
                creator = c.getCreator();
            } else if (c.getProperty() == SKOSProperty.contributor) {
                contributor = c.getCreator();
            }
        }

        thesaurus.setCreator(creator);
        thesaurus.setContributor(contributor);

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        thesaurusHelper.setIdentifierType("2");
        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        String idTheso1;
        if (thesaurus.getLanguage() == null) {
            thesaurus.setLanguage(langueSource);
        }
        if ((idTheso1 = thesaurusHelper.addThesaurusRollBack(conn, "", false)) == null) {
            conn.rollback();
            conn.close();
            message.append("Erreur lors de la création du thésaurus");
            return null;
        }
        // Si le Titre du thésaurus n'est pas detecter, on donne un nom par defaut
        if (skosXmlDocument.getConceptScheme().getLabelsList().isEmpty()) {
            if (thesaurus.getTitle().isEmpty()) {
                thesaurus.setTitle("theso_" + idTheso1);
                //thesaurusHelper.addThesaurusTraduction(ds, thesaurus);
            }
        }

        thesaurus.setId_thesaurus(idTheso1);

        // boucler pour les traductions
        for (SKOSLabel label : skosXmlDocument.getConceptScheme().getLabelsList()) {
            thesaurus.setTitle(label.getLabel());
            thesaurus.setLanguage(label.getLanguage());
            if (thesaurus.getLanguage() == null) {
                thesaurus.setLanguage("fr"); // cas où la langue n'est pas définie dans le SKOS
            }
            if (!thesaurusHelper.addThesaurusTraductionRollBack(conn, thesaurus)) {
                conn.rollback();
                conn.close();
                message.append("Erreur lors de la création des traductions du thésaurus");
                return null;
            }
        }

        // ajouter le thésaurus dans le group de l'utilisateur
        if (idGroupUser != -1) { // si le groupeUser = - 1, c'est le cas d'un SuperAdmin, alors on n'intègre pas le thésaurus dans un groupUser
            UserHelper userHelper = new UserHelper();
            if (!userHelper.addThesoToGroup(conn, thesaurus.getId_thesaurus(),
                    idGroupUser)) {
                conn.rollback();
                conn.close();
                message.append("Erreur lors de l'ajout du thésaurus au projet");
                return null;
            }
        }
        conn.commit();
        conn.close();
//        idGroupDefault = "orphans";//getNewGroupId();

        for (SKOSRelation relation : skosXmlDocument.getConceptScheme().getRelationsList()) {
            hasTopConcceptList.add(relation.getTargetUri());
        }
        setPreferences(idTheso1, skosXmlDocument.getTitle());
        return idTheso1; 
    }
    
    private void setPreferences(String idTheso, String uri){
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if (nodePreference == null) {
            preferencesHelper.initPreferences(
                    ds,
                    idTheso,
                    langueSource);
            nodePreference = preferencesHelper.getThesaurusPreferences(
                    ds,
                    idTheso);
            nodePreference.setCheminSite(uri);
            nodePreference.setPreferredName(idTheso);
            nodePreference.setOriginalUri(uri);
            if(selectedIdentifier.equalsIgnoreCase("ark")){
                nodePreference.setOriginalUriIsArk(true);
            }
            if(selectedIdentifier.equalsIgnoreCase("handle")){
                nodePreference.setOriginalUriIsHandle(true);
            }            
            preferencesHelper.updateAllPreferenceUser(ds, nodePreference, idTheso);            
        } else {
            nodePreference.setCheminSite(uri);
            nodePreference.setPreferredName(idTheso);
            nodePreference.setOriginalUri(uri);
            if(selectedIdentifier.equalsIgnoreCase("ark")){
                nodePreference.setOriginalUriIsArk(true);
            }
            if(selectedIdentifier.equalsIgnoreCase("handle")){
                nodePreference.setOriginalUriIsHandle(true);
            }             
            preferencesHelper.addPreference(ds, nodePreference, idTheso); 
        }
    }
    
    private void setOriginalUri(String idTheso, String uri){
        PreferencesHelper preferencesHelper = new PreferencesHelper();       
        if (nodePreference == null) return;
        
        nodePreference.setCheminSite(uri);
        nodePreference.setPreferredName(idTheso);
        nodePreference.setOriginalUri(uri);
        preferencesHelper.updateAllPreferenceUser(ds, nodePreference, idTheso);  
    }    
    
    public void addGroups(ArrayList<SKOSResource> groupResource, String idTheso) {
        // récupération des groupes ou collections
        GroupHelper groupHelper = new GroupHelper();

        String idGroup;
        String notationValue;
        SKOSNotation notation;
        ArrayList<SKOSNotation> notationList;
        String type;     
        //sub group
        String idSubGroup;
        //concept group concept
        String idSubConcept;        

        for (SKOSResource group : groupResource) {
            notation = null;
            idGroup = getIdFromUri(group.getUri());
            if(idGroup == null || idGroup.isEmpty()) 
                idGroup = group.getUri();
            
            notationList = group.getNotationList();

            if (notationList != null && !notationList.isEmpty()) {
                notation = notationList.get(0);
            }

            if (notation == null) {
                notationValue = "";
            } else {
                notationValue = notation.getNotation();
            }
            switch (group.getProperty()) {
                case SKOSProperty.Collection:
                    type = "C";
                    break;
                case SKOSProperty.ConceptGroup:
                    type = "G";
                    break;
                case SKOSProperty.MicroThesaurus:
                default:
                    type = "MT";
                    break;
                case SKOSProperty.Theme:
                    type = "T";
                    break;
            }
            
            String idArkHandle = null;
            // option cochée
            if (selectedIdentifier.equalsIgnoreCase("ark")) {
                idArkHandle = getIdArkFromUri(group.getUri());
            }
            if (selectedIdentifier.equalsIgnoreCase("handle")) {
                idArkHandle = getIdHandleFromUri(group.getUri());
            }
            if(idArkHandle == null)
                idArkHandle = "";
            groupHelper.insertGroup(ds, idGroup, idTheso, idArkHandle, type, notationValue, "", false, idUser);
            
            // group/sous_group
            for (SKOSRelation relation : group.getRelationsList()) {
                int prop = relation.getProperty();
                switch (prop) {
                    case SKOSProperty.subGroup:
                        idSubGroup = getIdFromUri(relation.getTargetUri());
                        groupHelper.addSubGroup(ds, idGroup, idSubGroup, idTheso);
                        break;
                    case SKOSProperty.member:
                        // option cochée
                        /*   if(identifierType.equalsIgnoreCase("sans")){
                            idSubConcept = getIdFromUri(relation.getTargetUri());
                        } else {*/
                        // Récupération de l'Id d'origine sauvegardé à l'import (idArk -> identifier)
                        idSubConcept = getOriginalId(relation.getTargetUri());
                        //     }
                        groupSubGroup.put(idSubConcept, idGroup);
                        groupHelper.addConceptGroupConcept(ds, idGroup, idSubConcept, idTheso);
                //        memberHashMap.put(relation.getTargetUri(), idGroup);
                        break;
             //       case SKOSProperty.hasTopConcept:
              //          hasTopConcceptList.add(relation.getTargetUri());
               //         break;
                    default:
                        break;
                }

            }

            for (SKOSLabel label : group.getLabelsList()) {
                // ajouter les traductions des Groupes
                ConceptGroupLabel conceptGroupLabel = new ConceptGroupLabel();
                conceptGroupLabel.setIdgroup(idGroup);
                conceptGroupLabel.setIdthesaurus(idTheso);
                conceptGroupLabel.setLang(label.getLanguage());
                conceptGroupLabel.setLexicalvalue(label.getLabel());

                groupHelper.addGroupTraduction(ds, conceptGroupLabel, idUser);
            }
        }
        addGroupConceptGroup(idTheso);
        /*
        groupHelper.insertGroup(ds,
                idGroupDefault,
                thesaurus.getId_thesaurus(),
                "MT",
                "", //notation
                adressSite,
                useArk,
                idUser);
         */
        // Création du domaine par défaut 
        // ajouter les traductions des Groupes
        /*ConceptGroupLabel conceptGroupLabel = new ConceptGroupLabel();
        conceptGroupLabel.setIdgroup(idGroupDefault);
        conceptGroupLabel.setIdthesaurus(thesaurus.getId_thesaurus());
        
        conceptGroupLabel.setLang(langueSource);
        conceptGroupLabel.setLexicalvalue("groupDefault");
        groupHelper.addGroupTraduction(ds, conceptGroupLabel, idUser);*/
    }  
    
    /**
     * permet d'ajouter les relations entre les groupes / sousGroupes
     * et les groupes / concepts
     */
    private void addGroupConceptGroup(String idTheso){
        // groupSubGroup : compositon du HashMap = idSubGroup(ou idConcept) -> idGroup 
        // c'est pour séparer les concepts des groupes
        GroupHelper groupHelper = new GroupHelper();
        for (String idSubGroup : groupSubGroup.keySet()) {
          if(idGroups.contains(idSubGroup)) {
              // si la relation member est vers un sous groupe, alors on créé une relation groupe/sousGroupe
              groupHelper.addSubGroup(ds, groupSubGroup.get(idSubGroup), idSubGroup, idTheso);
          } else
              groupHelper.addConceptGroupConcept(ds, groupSubGroup.get(idSubGroup), idSubGroup, idTheso);
    //      System.out.println("key: " + i + " value: " + capitalCities.get(i));
        }
    }    

    public void addConcept(SKOSResource conceptResource, String idTheso, boolean isCandidatImport) {
        AddConceptsStruct acs = new AddConceptsStruct();
        acs.conceptHelper = new ConceptHelper();
        initAddConceptsStruct(acs, conceptResource, idTheso, isCandidatImport);
        addRelation(acs, idTheso);

        if (isCandidatImport) {
            acs.concept.setStatus("CA");
        }
        //acs.concept.setStatus("DE");

        // envoie du concept à la BDD
        addConceptToBdd(acs, idTheso, isCandidatImport);
 /*       if (!isConceptEmpty(acs.nodeTermTraductionList)) {
            if (acs.idGrps.isEmpty()) {
                acs.concept.setTopConcept(acs.isTopConcept);
                acs.concept.setIdGroup(idGroupDefault);
                acs.conceptHelper.insertConceptInTable(ds, acs.concept, idUser);

                new GroupHelper().addConceptGroupConcept(ds, idGroupDefault, acs.concept.getIdConcept(), acs.concept.getIdThesaurus());
                defaultGroupToAdd = true;
            } else {
                for (String idGrp : acs.idGrps) {
                    acs.concept.setTopConcept(acs.isTopConcept);
                    acs.concept.setIdGroup(idGrp);

                    if (!acs.conceptHelper.insertConceptInTable(ds, acs.concept, idUser)) {
                        System.out.println("Erreur sur le Concept = " + acs.concept.getIdConcept());
                    }
                }
            }

            finalizeAddConceptStruct(acs, idTheso);

        }*/

   //     addLangsToThesaurus(ds, idTheso);
   //     addGroupDefault();
    }
    
    private void initAddConceptsStruct(AddConceptsStruct acs, SKOSResource conceptResource,
            String idTheso, boolean isCandidatImport) {

        acs.conceptResource = conceptResource;
        acs.concept = new Concept();

        String idConcept = getOriginalId(conceptResource.getUri());
        acs.concept.setIdConcept(idConcept);


        // option cochée
        if ("ark".equalsIgnoreCase(selectedIdentifier)) {
            acs.concept.setIdArk(getIdArkFromUri(conceptResource.getUri()));
        }
        if ("handle".equalsIgnoreCase(selectedIdentifier)) {
            acs.concept.setIdHandle(getIdHandleFromUri(conceptResource.getUri()));
        }

        acs.concept.setIdThesaurus(idTheso);
        addNotation(acs);
        addGPSCoordinates(acs);
        addLabel(acs);
        addDocumentation(acs);
        addDate(acs);

        if (isCandidatImport) {
            addMessages(acs);
            addVotes(acs);
            addStatut(acs, conceptResource.getSkosStatus(), idTheso, idConcept);
        }

        addAlignment(acs, idTheso);
        addImages(acs);

        if(isFirst){
            isFirst = false;
            String uri = conceptResource.getUri().substring(0, conceptResource.getUri().lastIndexOf("/"));
            if(uri == null || uri.isEmpty()) 
                uri = conceptResource.getUri();
            setOriginalUri(idTheso, uri);
        }        
        
        //autre
        //ajout des termes et traductions
        acs.nodeTerm.setNodeTermTraduction(acs.nodeTermTraductionList);
        acs.nodeTerm.setIdTerm(acs.concept.getIdConcept());
        acs.nodeTerm.setIdConcept(acs.concept.getIdConcept());
        acs.nodeTerm.setIdThesaurus(idTheso);
        acs.nodeTerm.setSource("");
        acs.nodeTerm.setStatus("");
        acs.nodeTerm.setCreated(acs.concept.getCreated());
        acs.nodeTerm.setModified(acs.concept.getModified());
    }

    private void addStatut(AddConceptsStruct acs, SKOSStatus skosStatus, String idTheso, String idConcept) {
        acs.status = new NodeStatus();
        acs.status.setIdThesaurus(idTheso);
        acs.status.setIdConcept(idConcept);
        acs.status.setMessage(skosStatus.getMessage());
        acs.status.setIdStatus(skosStatus.getIdStatus());
    }
    
    private void addConceptToBdd(AddConceptsStruct acs, String idTheso, boolean isCandidatImport) {
        if (!acs.conceptHelper.insertConceptInTable(ds, acs.concept, idUser)) {
            System.out.println("Erreur sur le Concept = " + acs.concept.getIdConcept());
        }
        acs.termHelper.insertTerm(ds, acs.nodeTerm, idUser);

        if (isCandidatImport) {
            acs.conceptHelper.setNodeStatus(ds, acs.status.getIdConcept(), acs.status.getIdThesaurus(), acs.status.getIdStatus(),
                    new Date().toString(), idUser, acs.status.getMessage());
        }

        RelationsHelper relationsHelper = new RelationsHelper();

        for (HierarchicalRelationship hierarchicalRelationship : acs.hierarchicalRelationships) {
            switch (hierarchicalRelationship.getRole()) {
                case "NT":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    // pour créer la relation réciproque si elle n'existe pas
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BT",
                            hierarchicalRelationship.getIdConcept1())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }                    
                    break;
                case "BT":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    // pour créer la relation réciproque si elle n'existe pas
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "NT",
                            hierarchicalRelationship.getIdConcept1())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }             
                    break;
                case "RT":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    // pour créer la relation réciproque si elle n'existe pas
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "RT",
                            hierarchicalRelationship.getIdConcept1())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }             
                    break;
                case "NTP":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    // pour créer la relation réciproque si elle n'existe pas
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BTP",
                            hierarchicalRelationship.getIdConcept1())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }             
                    break;
                case "NTG":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    // pour créer la relation réciproque si elle n'existe pas
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BTG",
                            hierarchicalRelationship.getIdConcept1())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }             
                    break;
                case "NTI":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    // pour créer la relation réciproque si elle n'existe pas
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BTI",
                            hierarchicalRelationship.getIdConcept1())) {
                        //System.out.println("Erreur sur la relation = " + acs.concept.getIdConcept() + " ## " + hierarchicalRelationship.getRole());
                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }             
                    break;                     
            }
        }
        
    //    addRelationNoBTHiera(acs); 
    

        // For Concept : customnote ; scopeNote ; historyNote
        // For Term : definition; editorialNote; historyNote;
        if (isCandidatImport) {
            for (NodeNote nodeNoteList1 : acs.nodeNotes) {

                String str = formatLinkToHtmlTag(nodeNoteList1.getLexicalvalue());

                if (nodeNoteList1.getNotetypecode().equals("customnote") || nodeNoteList1.getNotetypecode().equals("scopeNote") || nodeNoteList1.getNotetypecode().equals("note")) {
                    acs.noteHelper.addConceptNote(ds, acs.concept.getIdConcept(), nodeNoteList1.getLang(),
                            idTheso, str, nodeNoteList1.getNotetypecode(), idUser);
                }

                if (nodeNoteList1.getNotetypecode().equals("definition") || nodeNoteList1.getNotetypecode().equals("historyNote") || nodeNoteList1.getNotetypecode().equals("editorialNote")) {
                    acs.noteHelper.addTermNote(ds, acs.nodeTerm.getIdTerm(), nodeNoteList1.getLang(),
                            idTheso, str, nodeNoteList1.getNotetypecode(), idUser);
                }

                for (VoteDto vote : acs.votes) {
                    if (vote.getIdNote() != null) {
                        NodeNote newNote = acs.noteHelper.getNoteByValue(ds, str.replaceAll("'", "''"));
                        vote.setIdNote(newNote.getId_note()+"");
                        break;
                    }
                }

            }
        } else {
            for (NodeNote nodeNoteList1 : acs.nodeNotes) {

                if (nodeNoteList1.getNotetypecode().equals("customnote") || nodeNoteList1.getNotetypecode().equals("scopeNote") || nodeNoteList1.getNotetypecode().equals("note")) {
                    acs.noteHelper.addConceptNote(ds, acs.concept.getIdConcept(), nodeNoteList1.getLang(),
                            idTheso, nodeNoteList1.getLexicalvalue(), nodeNoteList1.getNotetypecode(), idUser);
                }

                if (nodeNoteList1.getNotetypecode().equals("definition") || nodeNoteList1.getNotetypecode().equals("historyNote") || nodeNoteList1.getNotetypecode().equals("editorialNote")) {
                    acs.noteHelper.addTermNote(ds, acs.nodeTerm.getIdTerm(), nodeNoteList1.getLang(),
                            idTheso, nodeNoteList1.getLexicalvalue(), nodeNoteList1.getNotetypecode(), idUser);
                }

            }
        }

        if (isCandidatImport && !StringUtils.isEmpty(acs.collectionToAdd)) {
            new ConceptHelper().addNewGroupOfConcept(ds, acs.concept.getIdConcept(), acs.collectionToAdd, idTheso);
        }

        for (NodeAlignment nodeAlignment : acs.nodeAlignments) {
            acs.alignmentHelper.addNewAlignment(ds, nodeAlignment);
        }
        for (NodeEM nodeEMList1 : acs.nodeEMList) {
            acs.term.setId_concept(acs.concept.getIdConcept());
            acs.term.setId_term(acs.nodeTerm.getIdTerm());
            acs.term.setLexical_value(nodeEMList1.getLexical_value());
            acs.term.setLang(nodeEMList1.getLang());
            acs.term.setId_thesaurus(idTheso);//thesaurus.getId_thesaurus());
            acs.term.setSource(nodeEMList1.getSource());
            acs.term.setStatus(nodeEMList1.getStatus());
            acs.term.setHidden(nodeEMList1.isHiden());
            acs.termHelper.addNonPreferredTerm(ds, acs.term, idUser);
        }

        if (acs.nodeGps.getLatitude() != null && acs.nodeGps.getLongitude() != null) {
            // insertion des données GPS
            acs.gpsHelper.insertCoordonees(ds, acs.concept.getIdConcept(),
                    idTheso,//thesaurus.getId_thesaurus(),
                    acs.nodeGps.getLatitude(), acs.nodeGps.getLongitude());
        }

        if(acs.isTopConcept) {
            if (!acs.conceptHelper.setTopConcept(ds, acs.concept.getIdConcept(), idTheso)) {//thesaurus.getId_thesaurus())) {
                // erreur;
            }
        }
        
        // ajout des images externes URI
        for (String imageUri : acs.nodeImages) {
            acs.imagesHelper.addExternalImage(ds, acs.concept.getIdConcept(), idTheso, "", "", imageUri, idUser);
        }

        if (isCandidatImport) {
            for (MessageDto message : acs.messages) {
                new MessageDao().addNewMessage(ds, message.getMsg(), message.getIdUser(), acs.concept.getIdConcept(),
                        idTheso, message.getDate());
            }

            int status = 1;
            try {
                status = Integer.parseInt(acs.status.getIdStatus());
            } catch (Exception ex) { }
            new CandidatDao().setStatutForCandidat(ds, status, acs.status.getIdConcept(), idTheso,
                    acs.status.getIdUser(), acs.status.getDate());

            for (VoteDto vote : acs.votes) {
                new CandidatDao().addVote(ds, idTheso, vote.getIdConcept(), vote.getIdUser(), vote.getIdNote(), vote.getTypeVote());
            }
        }


        // initialisation des variables
        acs.concept = new Concept();
        acs.nodeTerm = new NodeTerm();
        acs.nodeTermTraductionList = new ArrayList<>();
        acs.nodeEMList = new ArrayList<>();
        acs.nodeNotes = new ArrayList<>();
        acs.nodeAlignments = new ArrayList<>();
        acs.hierarchicalRelationships = new ArrayList<>();
        acs.idGrps = new ArrayList<>();
        acs.isTopConcept = false;
        acs.nodeGps = new NodeGps();
        acs.nodeImages = new ArrayList<>();
    }

    private String formatLinkToHtmlTag(String str) {
        Pattern MY_PATTERN = Pattern.compile("[a-zA-Z]+ \\(http(.*?)\\)");
        Matcher m = MY_PATTERN.matcher(str);
        while (m.find()) {
            String brut = m.group();
            String titre = brut.substring(0, brut.indexOf('(') - 1);
            String value = brut.substring(brut.indexOf('(') + 1, brut.indexOf(')'));

            String link = "<a href='" + value + "'>" + titre + "</a>";
            str = str.replace(brut, link);
        }
        return str;
    }


    private void addAlignment(AddConceptsStruct acs, String idTheso) {
        int prop;
        int id_type = -1;
        NodeAlignment nodeAlignment;
        for (SKOSMatch match : acs.conceptResource.getMatchList()) {
            prop = match.getProperty();
            nodeAlignment = new NodeAlignment();
            switch (prop) {
                case SKOSProperty.closeMatch:
                    id_type = 2;
                    break;
                case SKOSProperty.exactMatch:
                    id_type = 1;
                    break;
                case SKOSProperty.broadMatch:
                    id_type = 3;
                    break;
                case SKOSProperty.narrowMatch:
                    id_type = 5;
                    break;
                case SKOSProperty.relatedMatch:
                    id_type = 4;
                    break;
            }
            nodeAlignment.setId_author(idUser);
            nodeAlignment.setConcept_target("");
            nodeAlignment.setThesaurus_target("");
            nodeAlignment.setUri_target(match.getValue());
            nodeAlignment.setInternal_id_concept(acs.concept.getIdConcept());
            nodeAlignment.setInternal_id_thesaurus(idTheso);
            nodeAlignment.setAlignement_id_type(id_type);
            acs.nodeAlignments.add(nodeAlignment);
        }
    }

    private void addMessages(AddConceptsStruct acs) {
        for (SKOSDiscussion discussion : acs.conceptResource.getMessages()) {
            MessageDto messageDto = new MessageDto();
            messageDto.setDate(discussion.getDate());
            messageDto.setIdUser(discussion.getIdUser());
            messageDto.setMsg(discussion.getMsg());
            acs.messages.add(messageDto);
        }
    }

    private void addVotes(AddConceptsStruct acs) {
        for (SKOSVote vote : acs.conceptResource.getVotes()) {
            VoteDto voteDto = new VoteDto();
            voteDto.setTypeVote(vote.getTypeVote());
            voteDto.setIdUser(vote.getIdUser());
            voteDto.setIdConcept(vote.getIdConcept());

            if (!StringUtils.isEmpty(vote.getIdNote())) {
                String str = formatLinkToHtmlTag(vote.getIdNote());
                str = str.replaceAll("'", "''");
                NodeNote nodeNote = new NoteHelper().getNoteByValue(ds, str);
                if (nodeNote != null ) {
                    voteDto.setIdNote(nodeNote.getId_note()+"");
                }
            } else {
                voteDto.setIdNote(null);
            }

            acs.votes.add(voteDto);
        }
    }

    private void addNotation(AddConceptsStruct acs) {
        acs.concept.setNotation("");
        for (SKOSNotation notation : acs.conceptResource.getNotationList()) {
            if (notation.getNotation() != null) {
                acs.concept.setNotation(notation.getNotation());
            }
        }
    }

    private void addImages(AddConceptsStruct acs) {
        for (String imageUri : acs.conceptResource.getImageUris()) {
            if (imageUri != null && (!imageUri.isEmpty())) {
                acs.nodeImages.add(imageUri);
            }
        }
    }    

    private void addGPSCoordinates(AddConceptsStruct acs) {
        SKOSGPSCoordinates gPSCoordinates = acs.conceptResource.getGPSCoordinates();
        try {
            acs.nodeGps.setLatitude(Double.parseDouble(gPSCoordinates.getLat()));
            acs.nodeGps.setLongitude(Double.parseDouble(gPSCoordinates.getLon()));

        } catch (Exception e) {
            acs.nodeGps.setLatitude(null);
            acs.nodeGps.setLongitude(null);
        }

    }

    private void addLabel(AddConceptsStruct acs) {
        NodeTermTraduction nodeTermTraduction;

        for (SKOSLabel label : acs.conceptResource.getLabelsList()) {
            if (label.getProperty() == SKOSProperty.prefLabel) {
                nodeTermTraduction = new NodeTermTraduction();
                nodeTermTraduction.setLexicalValue(label.getLabel());
                nodeTermTraduction.setLang(label.getLanguage());
                acs.nodeTermTraductionList.add(nodeTermTraduction);
            } else {
                NodeEM nodeEM = new NodeEM();
                String status = "";
                boolean hiden = false;
                if (label.getProperty() == SKOSProperty.altLabel) {
                    status = "USE";

                } else if (label.getProperty() == SKOSProperty.hiddenLabel) {
                    status = "Hidden";
                    hiden = true;
                }
                nodeEM.setLexical_value(label.getLabel());
                nodeEM.setLang(label.getLanguage());
                nodeEM.setSource("" + idUser);
                nodeEM.setStatus(status);
                nodeEM.setHiden(hiden);
                acs.nodeEMList.add(nodeEM);
            }
            appendNewLang(label.getLanguage());
        }
    }

    private void appendNewLang(String idLang) {
        if(idLang == null || idLang.isEmpty()) return;
        if(idLangsFound.contains(idLang)) return;
        idLangsFound.add(idLang);
    }
    
    private void addDocumentation(AddConceptsStruct acs) {
        NodeNote nodeNote;
        for (SKOSDocumentation documentation : acs.conceptResource.getDocumentationsList()) {
            String noteTypeCode = "";
            int prop = documentation.getProperty();
            nodeNote = new NodeNote();
            switch (prop) {
                case SKOSProperty.definition:
                    noteTypeCode = "definition";
                    break;
                case SKOSProperty.scopeNote:
                    noteTypeCode = "scopeNote";
                    break;
                case SKOSProperty.example:
                    noteTypeCode = "example";
                    break;
                case SKOSProperty.historyNote:
                    noteTypeCode = "historyNote";
                    break;
                case SKOSProperty.editorialNote:
                    noteTypeCode = "editorialNote";
                    break;
                case SKOSProperty.changeNote:
                    noteTypeCode = "changeNote";
                    break;
                case SKOSProperty.note:
                    noteTypeCode = "note";
                    break;
            }
            nodeNote.setLang(documentation.getLanguage());
            nodeNote.setLexicalvalue(documentation.getText());
            nodeNote.setNotetypecode(noteTypeCode);
            acs.nodeNotes.add(nodeNote);
        }
    }

    private void addDate(AddConceptsStruct acs) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate);
            for (SKOSDate date : acs.conceptResource.getDateList()) {
                if (date.getProperty() == SKOSProperty.created) {
                    acs.concept.setCreated(simpleDateFormat.parse(date.getDate()));
                } else if ((date.getProperty() == SKOSProperty.modified)) {
                    acs.concept.setModified(simpleDateFormat.parse(date.getDate()));
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(ImportRdf4jHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

 
    private void addRelation(AddConceptsStruct acs, String idTheso) {
        HierarchicalRelationship hierarchicalRelationship;
        int prop;
        String idConcept2;
        acs.isTopConcept = true;
        for (SKOSRelation relation : acs.conceptResource.getRelationsList()) {
            prop = relation.getProperty();

            hierarchicalRelationship = new HierarchicalRelationship();
            String role;

            switch (prop) {
                case SKOSProperty.narrower:
                    role = "NT";
                    break;
                case SKOSProperty.narrowerGeneric:
                    role = "NTG";
                    break;
                case SKOSProperty.narrowerPartitive:
                    role = "NTP";
                    break;
                case SKOSProperty.narrowerInstantial:
                    role = "NTI";
                    break;
                case SKOSProperty.broader:
                    acs.isTopConcept = false;
                    role = "BT";
                    break;
                case SKOSProperty.broaderGeneric:
                    acs.isTopConcept = false;                    
                    role = "BTG";
                    break;
                case SKOSProperty.broaderInstantial:
                    acs.isTopConcept = false;                    
                    role = "BTI";
                    break;
                case SKOSProperty.broaderPartitive:
                    acs.isTopConcept = false;                    
                    role = "BTP";
                    break;
                case SKOSProperty.related:
                    role = "RT";
                    break;
                case SKOSProperty.relatedHasPart:
                    role = "RHP";
                    break;
                case SKOSProperty.relatedPartOf:
                    role = "RPO";
                    break;
                default:
                    role = "";
            }

            if (!role.equals("")) {
                hierarchicalRelationship.setIdConcept1(acs.concept.getIdConcept());

                // option cochée
                //if(identifierType.equalsIgnoreCase("sans")){
                //    idConcept2 = getIdFromUri(relation.getTargetUri());
                //} else {
                // Récupération des Id Ark ou Handle
                idConcept2 = getOriginalId(relation.getTargetUri());
                //}
                hierarchicalRelationship.setIdConcept2(idConcept2);
                hierarchicalRelationship.setIdThesaurus(idTheso);
                hierarchicalRelationship.setRole(role);
                acs.hierarchicalRelationships.add(hierarchicalRelationship);

            } else if (prop == SKOSProperty.inScheme) {
                // ?
                /*} else if (prop == SKOSProperty.memberOf) {
                acs.idGrps.add(getIdFromUri(relation.getTargetUri()));
                //addIdGroupToVector(uri);    ????
                 */
            } else if (prop == SKOSProperty.topConceptOf) {
                acs.isTopConcept = true;

            } else if (prop == SKOSProperty.memberOf) {
                acs.collectionToAdd = getIdFromUri(relation.getTargetUri());
            } if (hasTopConcceptList.contains(acs.conceptResource.getUri())) {
                acs.isTopConcept = true;
            }
    /*        String uri = acs.conceptResource.getUri();
            String idPere = memberHashMap.get(uri);

            if (idPere != null) {
                acs.idGrps.add(idPere);
                memberHashMap.remove(uri);
            }*/
        }
    }

    private static String getIdFromUri(String uri) {
        if (uri.contains("idg=")) {
            if (uri.contains("&")) {
                uri = uri.substring(uri.indexOf("idg=") + 4, uri.indexOf("&"));
            } else {
                uri = uri.substring(uri.indexOf("idg=") + 4, uri.length());
            }
        } else {
            if (uri.contains("idc=")) {
                if (uri.contains("&")) {
                    uri = uri.substring(uri.indexOf("idc=") + 4, uri.indexOf("&"));
                } else {
                    uri = uri.substring(uri.indexOf("idc=") + 4, uri.length());
                }
            } else {
                if (uri.contains("#")) {
                    uri = uri.substring(uri.indexOf("#") + 1, uri.length());
                } else {
                    uri = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
                }
            }
        }

        StringPlus stringPlus = new StringPlus();
        uri = stringPlus.normalizeStringForIdentifier(uri);
        return uri;
    }

    /**
     * Permet de retourner l'id d'origine d'un concept SKOS en s'appuyant sur le
     * DC:identifier sauvegardé à l'import Si l'identifiant d'origine n'a pas
     * été trouvé, c'est l'ID de l'URI qui est récupéré
     *
     * @return #MR
     */
    private String getOriginalId(String uri) {
        String originalId;
        if (skosXmlDocument.getEquivalenceUriArkHandle().isEmpty()
                || skosXmlDocument.getEquivalenceUriArkHandle().get(uri) == null) {
            return getIdFromUri(uri);
        }

        originalId = skosXmlDocument.getEquivalenceUriArkHandle().get(uri).toString();
        if (originalId == null) {
            if (message.length() != 0) {
                message.append(System.getProperty("line.separator"));
            }
            message.append("Identifiant (DC:Identifier) non détecté pour l'URL:");
            message.append(uri);
            originalId = getIdFromUri(uri);
            return originalId;
        }
        return originalId;
    }

    private String getIdArkFromUri(String uri) {
        // URI de type Ark
        String id = null;
        if (uri.contains("ark:/")) {
            id = uri.substring(uri.indexOf("ark:/") + 5, uri.length());
        }
        return id;
    }

    private String getIdHandleFromUri(String uri) {
        // URI de type Handle
        String id = null;
        if (uri.contains(prefixHandle)) {
            id = uri.substring(uri.indexOf(prefixHandle), uri.length());
        }
        return id;
    }

    public void addLangsToThesaurus(HikariDataSource ds, String idTheso) {

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        for (String idLang : idLangsFound) {
            if (!thesaurusHelper.isLanguageExistOfThesaurus(ds, idTheso, idLang)) {
                Thesaurus thesaurus1 = new Thesaurus();
                thesaurus1.setId_thesaurus(idTheso);
                thesaurus1.setContributor("");
                thesaurus1.setCoverage("");
                thesaurus1.setCreator("");
                thesaurus1.setDescription("");
                thesaurus1.setFormat("");
                thesaurus1.setLanguage(idLang);
                thesaurus1.setPublisher("");
                thesaurus1.setRelation("");
                thesaurus1.setRights("");
                thesaurus1.setSource("");
                thesaurus1.setSubject("");
                thesaurus1.setTitle("theso_" + idTheso + "_" + idLang);
                thesaurus1.setType("");
                thesaurusHelper.addThesaurusTraduction(ds, thesaurus1);
            }            
        }
/*        ArrayList<String> tabListLang = thesaurusHelper.getAllUsedLanguagesOfThesaurus(ds, idThesaurus); 
        for (int i = 0; i < tabListLang.size(); i++) {
            if (!thesaurusHelper.isLanguageExistOfThesaurus(ds, idThesaurus, tabListLang.get(i).trim())) {
                Thesaurus thesaurus1 = new Thesaurus();
                thesaurus1.setId_thesaurus(idThesaurus);
                thesaurus1.setContributor("");
                thesaurus1.setCoverage("");
                thesaurus1.setCreator("");
                thesaurus1.setDescription("");
                thesaurus1.setFormat("");
                thesaurus1.setLanguage(tabListLang.get(i));
                thesaurus1.setPublisher("");
                thesaurus1.setRelation("");
                thesaurus1.setRights("");
                thesaurus1.setSource("");
                thesaurus1.setSubject("");
                thesaurus1.setTitle("theso_" + idThesaurus);
                thesaurus1.setType("");
                thesaurusHelper.addThesaurusTraduction(ds, thesaurus1);
            }
        }*/

    }

    /*    private String getNewGroupId(HikariDataSource ds) {
        GroupHelper groupHelper = new GroupHelper();
        ToolsHelper toolsHelper = new ToolsHelper();
        String id = toolsHelper.getNewId(10);
        while (groupHelper.isIdGroupExiste(ds.getConnection(), id)) {
            id = toolsHelper.getNewId(10);
        }
        return id;
    }*/
    public SKOSXmlDocument getRdf4jThesaurus() {
        return skosXmlDocument;
    }

    public void setRdf4jThesaurus(SKOSXmlDocument rdf4jThesaurus) {
        this.skosXmlDocument = rdf4jThesaurus;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public StringBuilder getMessage() {
        return message;
    }

    public void setMessage(StringBuilder message) {
        this.message = message;
    }

    public String getSelectedIdentifier() {
        return selectedIdentifier;
    }

    public void setSelectedIdentifier(String selectedIdentifier) {
        this.selectedIdentifier = selectedIdentifier;
    }



    public String getPrefixHandle() {
        return prefixHandle;
    }

    public void setPrefixHandle(String prefixHandle) {
        this.prefixHandle = prefixHandle;
    }

}
