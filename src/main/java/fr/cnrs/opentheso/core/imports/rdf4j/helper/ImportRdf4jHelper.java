package fr.cnrs.opentheso.core.imports.rdf4j.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.ConceptGroupLabel;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DeprecateHelper;
import fr.cnrs.opentheso.bdd.helper.ExternalResourcesHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTerm;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.tools.StringPlus;

import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.candidat.dto.VoteDto;
import fr.cnrs.opentheso.skosapi.*;
import java.sql.Statement;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author Quincy
 */
public class ImportRdf4jHelper {

    private final static String SEPERATEUR = "##";
    private final static String SOUS_SEPERATEUR = "@@";

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
    private String prefixDoi;

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
     * initialisation des paramètres d'import
     *
     * @param ds
     * @param formatDate
     * @param idGroupUser
     * @param idUser
     * @param langueSource
     * @return
     */
    public boolean setInfos(HikariDataSource ds, String formatDate, int idUser, int idGroupUser, String langueSource) {
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

        SKOSResource conceptScheme = skosXmlDocument.getConceptScheme();
        if (conceptScheme == null) {
            message.append("Erreur SKOS !!! manque balise conceptSheme");
            return null;
        }

        Thesaurus thesaurus = conceptScheme.getThesaurus();

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

        String idTheso1;
        try ( Connection conn = ds.getConnection()) {

            conn.setAutoCommit(false);
            if (thesaurus.getLanguage() == null) {
                thesaurus.setLanguage(langueSource);
            }
            if ((idTheso1 = thesaurusHelper.addThesaurusRollBack(conn, "", false)) == null) {
                conn.rollback();
                conn.close();
                message.append("Erreur lors de la création du thésaurus");
                return null;
            }
            conn.commit();

            // Si le Titre du thésaurus n'est pas detecter, on donne un nom par defaut
            if (skosXmlDocument.getConceptScheme().getLabelsList().isEmpty()) {
                if (thesaurus.getTitle().isEmpty()) {
                    thesaurus.setTitle("theso_" + idTheso1);
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
                conn.commit();
            }

            // ajouter le thésaurus dans le group de l'utilisateur
            if (idGroupUser != -1) { // si le groupeUser = - 1, c'est le cas d'un SuperAdmin, alors on n'intègre pas le thésaurus dans un groupUser
                if (!new UserHelper().addThesoToGroup(conn, thesaurus.getId_thesaurus(), idGroupUser)) {
                    conn.rollback();
                    conn.close();
                    message.append("Erreur lors de l'ajout du thésaurus au projet");
                    return null;
                }
            }
            conn.commit();
        }

        for (SKOSRelation relation : skosXmlDocument.getConceptScheme().getRelationsList()) {
            hasTopConcceptList.add(relation.getTargetUri());
        }
        setPreferences(idTheso1, skosXmlDocument.getTitle());
        return idTheso1;
    }

    private void setPreferences(String idTheso, String uri) {

        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if (nodePreference == null) {
            preferencesHelper.initPreferences(ds, idTheso, langueSource);
            nodePreference = preferencesHelper.getThesaurusPreferences(ds, idTheso);
            nodePreference.setCheminSite(uri);
            nodePreference.setPreferredName(idTheso);
            nodePreference.setOriginalUri(uri);
            if (selectedIdentifier.equalsIgnoreCase("ark")) {
                nodePreference.setOriginalUriIsArk(true);
            }
            if (selectedIdentifier.equalsIgnoreCase("handle")) {
                nodePreference.setOriginalUriIsHandle(true);
            }
            if (selectedIdentifier.equalsIgnoreCase("doi")) {
                nodePreference.setOriginalUriIsDoi(true);
            }
   
        } else {
            nodePreference.setCheminSite(uri);
            nodePreference.setPreferredName(idTheso);
            nodePreference.setOriginalUri(uri);
            if (selectedIdentifier.equalsIgnoreCase("ark")) {
                nodePreference.setOriginalUriIsArk(true);
            }
            if (selectedIdentifier.equalsIgnoreCase("handle")) {
                nodePreference.setOriginalUriIsHandle(true);
            }
            if (selectedIdentifier.equalsIgnoreCase("doi")) {
                nodePreference.setOriginalUriIsDoi(true);
            }
        }
        preferencesHelper.updateAllPreferenceUser(ds, nodePreference, idTheso);
    }

    private void setOriginalUri(String idTheso, String uri) {
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if (nodePreference == null) {
            return;
        }
        if (nodePreference.getCheminSite().isEmpty() && nodePreference.getPreferredName().isEmpty() && nodePreference.getOriginalUri().isEmpty()) {
            nodePreference.setCheminSite(uri);
            nodePreference.setPreferredName(idTheso);
            nodePreference.setOriginalUri(uri);
            preferencesHelper.updateAllPreferenceUser(ds, nodePreference, idTheso);
        }
    }

    public void addFacets(ArrayList<SKOSResource> facetResources, String idTheso) {
        FacetHelper facetHelper = new FacetHelper();

        boolean first = true;
        for (SKOSResource facetSKOSResource : facetResources) {

            String idConceptParent = null;
            String idFacet = getIdFromUri(facetSKOSResource.getUri());
            if (idFacet == null) {
                continue;
            }
            for (SKOSRelation relation : facetSKOSResource.getRelationsList()) {
                if (relation.getProperty() == SKOSProperty.superOrdinate) {
                    idConceptParent = getOriginalId(relation.getTargetUri());
                }
            }
            if (idConceptParent == null) {
                continue;
            }
            if (facetSKOSResource.getLabelsList().isEmpty()) {
                continue;
            }

            for (SKOSLabel sKOSLabel : facetSKOSResource.getLabelsList()) {
                if (first) {
                    facetHelper.addNewFacet(ds,
                            idFacet,
                            idTheso,
                            idConceptParent,
                            sKOSLabel.getLabel(),
                            sKOSLabel.getLanguage(),
                            null);
                    first = false;
                } else {
                    facetHelper.addFacetTraduction(ds, idFacet, idTheso, sKOSLabel.getLabel(), sKOSLabel.getLanguage());
                }
            }
            for (SKOSRelation member : facetSKOSResource.getRelationsList()) {
                if (member.getProperty() == SKOSProperty.member) {
                    facetHelper.addConceptToFacet(ds,
                            idFacet, idTheso, getOriginalId(member.getTargetUri()));
                }
            }
            first = true;
        }

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
            if (idGroup == null || idGroup.isEmpty()) {
                idGroup = group.getUri();
            }

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
            if (!StringUtils.isEmpty(selectedIdentifier)) {
                if (selectedIdentifier.equalsIgnoreCase("ark")) {
                    idArkHandle = getIdArkFromUri(group.getUri());
                }
                if (selectedIdentifier.equalsIgnoreCase("handle")) {
                    idArkHandle = getIdHandleFromUri(group.getUri());
                }
                if (selectedIdentifier.equalsIgnoreCase("doi")) {
                    idArkHandle = getIdDoiFromUri(group.getUri());
                }
            }

            if (idArkHandle == null) {
                idArkHandle = "";
            }
            
            if (StringUtils.isEmpty(formatDate)) {
                formatDate = "dd-mm-yyyy";
            }
            Date created = null;
            Date modified = null;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate);
            for (SKOSDate sKOSDate : group.getDateList()) {
                try {
                    if(!StringUtils.isEmpty(sKOSDate.getDate())) {
                        if(sKOSDate.getProperty() == SKOSProperty.created){
                            created = simpleDateFormat.parse(sKOSDate.getDate());
                        }
                        if(sKOSDate.getProperty() == SKOSProperty.modified){
                            modified = simpleDateFormat.parse(sKOSDate.getDate());
                        }                        
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(ImportRdf4jHelper.class.getName()).log(Level.SEVERE, null, ex);
                }                  
            }
            

            groupHelper.insertGroup(ds, idGroup, idTheso, idArkHandle, type, notationValue, "", false, created, modified, idUser);

            // group/sous_group
            for (SKOSRelation relation : group.getRelationsList()) {
                int prop = relation.getProperty();
                switch (prop) {
                    case SKOSProperty.subGroup:
                        idSubGroup = getIdFromUri(relation.getTargetUri());
                        groupHelper.addSubGroup(ds, idGroup, idSubGroup, idTheso);
                        break;
                    case SKOSProperty.member:
                        // Récupération de l'Id d'origine sauvegardé à l'import (idArk -> identifier)
                        idSubConcept = getOriginalId(relation.getTargetUri());
                        groupSubGroup.put(idSubConcept, idGroup);
                        groupHelper.addConceptGroupConcept(ds, idGroup, idSubConcept, idTheso);
                        break;
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
    }

    /**
     * permet d'ajouter les relations entre les groupes / sousGroupes et les
     * groupes / concepts
     */
    private void addGroupConceptGroup(String idTheso) {
        // groupSubGroup : compositon du HashMap = idSubGroup(ou idConcept) -> idGroup
        // c'est pour séparer les concepts des groupes
        GroupHelper groupHelper = new GroupHelper();
        for (String idSubGroup : groupSubGroup.keySet()) {
            if (idGroups.contains(idSubGroup)) {
                // si la relation member est vers un sous groupe, alors on créé une relation groupe/sousGroupe
                groupHelper.addSubGroup(ds, groupSubGroup.get(idSubGroup), idSubGroup, idTheso);
            } else {
                groupHelper.addConceptGroupConcept(ds, groupSubGroup.get(idSubGroup), idSubGroup, idTheso);
            }
        }
    }

    public void addConcept(SKOSResource conceptResource, String idTheso, boolean isCandidatImport) {
        if (isCandidatImport) {
            if (new ConceptHelper().isIdExiste(ds, conceptResource.getIdentifier())) {
                return;
            }
        }

        AddConceptsStruct acs = new AddConceptsStruct();
        acs.conceptHelper = new ConceptHelper();
        initAddConceptsStruct(acs, conceptResource, idTheso, isCandidatImport);
        addRelation(acs, idTheso);

        if (isCandidatImport) {
            acs.concept.setStatus("CA");
        }

        // envoie du concept à la BDD
        addConceptToBdd(acs, idTheso, isCandidatImport);
    }

    public void addConceptV2(SKOSResource conceptResource, String idTheso) throws SQLException {
        String idConcept;
        if(StringUtils.isEmpty(conceptResource.getIdentifier())){
            idConcept = getOriginalId(conceptResource.getUri());
        } else
            idConcept = conceptResource.getIdentifier();


        String conceptStatus = "";
        
       
        if (conceptResource.getStatus() == SKOSProperty.deprecated) {
            conceptStatus = "dep";
        }
        // concept type
        String conceptType = "concept";// conceptResource.getConceptType();
        //if(StringUtils.isEmpty(conceptType)) 
        //    conceptType = "concept";
        
        
        // option cochée
        String idArk = "";
        if ("ark".equalsIgnoreCase(selectedIdentifier)) {
            idArk = getIdArkFromUri(conceptResource.getUri());
        }

        String idHandle = "";
        if ("handle".equalsIgnoreCase(selectedIdentifier)) {
            idHandle = getIdHandleFromUri(conceptResource.getUri());
        }

        String idDoi = "";
        if ("doi".equalsIgnoreCase(selectedIdentifier)) {
            idDoi = getIdDoiFromUri(conceptResource.getUri());
        }

        boolean isTopConcept = true;

        // IMAGES
        //-- 'url1##url2'
        String images = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getNodeImage())) {
            images = "";
            for (NodeImage nodeImage : conceptResource.getNodeImage()) {
                if (StringUtils.isNotEmpty(nodeImage.getUri())) {
                    images = images + SEPERATEUR + nodeImage.getImageName() + SOUS_SEPERATEUR + nodeImage.getCopyRight() + SOUS_SEPERATEUR + nodeImage.getUri();
                }
            }
            if (images.length() > 0) {
                images = images.substring(SEPERATEUR.length(), images.length());
            }
        }

        // ALIGNEMENT
        //-- 'author@concept_target@thesaurus_target@uri_target@alignement_id_type@internal_id_thesaurus@internal_id_concept'
        String alignements = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getMatchList())) {
            alignements = "";
            for (SKOSMatch match : conceptResource.getMatchList()) {
                int id_type = -1;
                switch (match.getProperty()) {
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

                alignements = alignements + SEPERATEUR + idUser + SOUS_SEPERATEUR + "" + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + match.getValue() + SOUS_SEPERATEUR + id_type
                        + SOUS_SEPERATEUR + idTheso + SOUS_SEPERATEUR + idConcept;
            }
            if (alignements.length() > 0) {
                alignements = alignements.substring(SEPERATEUR.length(), alignements.length());
            }
        }

        boolean isGpsPresent = false;
        String longitude = null, altitude = null;
        if (conceptResource.getGPSCoordinates() != null) {
            isGpsPresent = true;
            altitude = conceptResource.getGPSCoordinates().getLat();
            longitude = conceptResource.getGPSCoordinates().getLon();
        }

        //Non Pref Term
        //-- 'id_term@lexical_value@lang@id_thesaurus@source@status@hiden'
        String nonPrefTerm = null;
        String prefTerm = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getLabelsList())) {
            nonPrefTerm = "";
            prefTerm = "";
            for (SKOSLabel label : conceptResource.getLabelsList()) {
                if (label.getProperty() == SKOSProperty.prefLabel) {
                    prefTerm += SEPERATEUR + label.getLabel() 
                            + SOUS_SEPERATEUR + label.getLanguage();
                } else {
                    String status = null;
                    boolean hiden = false;
                    if (label.getProperty() == SKOSProperty.altLabel) {
                        status = "USE";
                    } else if (label.getProperty() == SKOSProperty.hiddenLabel) {
                        status = "Hidden";
                        hiden = true;
                    }
                    nonPrefTerm += SEPERATEUR + idConcept 
                            + SOUS_SEPERATEUR + label.getLabel()
                            + SOUS_SEPERATEUR + label.getLanguage() 
                            + SOUS_SEPERATEUR + idTheso
                            + SOUS_SEPERATEUR + idUser 
                            + SOUS_SEPERATEUR + status
                            + SOUS_SEPERATEUR + hiden;
                }
                appendNewLang(label.getLanguage());
            }
            if (nonPrefTerm.length() > 0) {
                nonPrefTerm = nonPrefTerm.substring(SEPERATEUR.length(), nonPrefTerm.length());
            }
            if (prefTerm.length() > 0) {
                prefTerm = prefTerm.substring(SEPERATEUR.length(), prefTerm.length());
            }
        }

        //Relation
        //-- 'id_concept1@role@id_concept2'
        String collectionToAdd;
        String relations = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getRelationsList())) {
            relations = "";
            for (SKOSRelation relation : conceptResource.getRelationsList()) {
                String role;
                switch (relation.getProperty()) {
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
                        isTopConcept = false;
                        role = "BT";
                        break;
                    case SKOSProperty.broaderGeneric:
                        isTopConcept = false;
                        role = "BTG";
                        break;
                    case SKOSProperty.broaderInstantial:
                        isTopConcept = false;
                        role = "BTI";
                        break;
                    case SKOSProperty.broaderPartitive:
                        isTopConcept = false;
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
                    relations = relations + SEPERATEUR + idConcept + SOUS_SEPERATEUR + role + SOUS_SEPERATEUR + getOriginalId(relation.getTargetUri());
                } else if (relation.getProperty() == SKOSProperty.memberOf) {
                    collectionToAdd = getIdFromUri(relation.getTargetUri());
                }

                if (hasTopConcceptList.contains(conceptResource.getUri())) {
                    isTopConcept = true;
                }
            }
            if (relations.length() > 0) {
                relations = relations.substring(SEPERATEUR.length(), relations.length());
            }
        }

        //CustomRelation
        //-- 'id_concept1@role@id_concept2'
        String customRelations = null;        
    /*    if (CollectionUtils.isNotEmpty(conceptResource.getCustomRelations())) {
            customRelations = "";
            for (NodeIdValue nodeIdValue  : conceptObject.getCustomRelations()) {
                customRelations += SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + nodeIdValue.getValue()
                        + SOUS_SEPERATEUR + nodeIdValue.getId();
            //    relations += SEPERATEUR + idCostomRelation
            //            + SOUS_SEPERATEUR + "NT"
            //            + SOUS_SEPERATEUR + conceptObject.getIdConcept();                
            //   
            }
        }    
        if (customRelations != null && customRelations.length() > 0) {
            customRelations = customRelations.substring(SEPERATEUR.length(), customRelations.length());
        }   */      
        
        //Notes
        //-- 'value@typeCode@lang@id_term'
        String notes = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getDocumentationsList())) {
            notes = "";
            for (SKOSDocumentation documentation : conceptResource.getDocumentationsList()) {
                String noteTypeCode = "";
                switch (documentation.getProperty()) {
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

                notes += SEPERATEUR + documentation.getText() 
                        + SOUS_SEPERATEUR + noteTypeCode 
                        + SOUS_SEPERATEUR + documentation.getLanguage() 
                        + SOUS_SEPERATEUR + idConcept;
            }
            if (notes.length() > 0) {
                notes = notes.substring(SEPERATEUR.length(), notes.length());
            }
        }

        String notationConcept = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getNotationList())) {
            for (SKOSNotation notation : conceptResource.getNotationList()) {
                notationConcept = notation.getNotation();
            }
        }

        if (isFirst) {
            isFirst = false;
            String uri = conceptResource.getUri().substring(0, conceptResource.getUri().lastIndexOf("/"));
            if (uri == null || uri.isEmpty()) {
                uri = conceptResource.getUri();
            }
            setOriginalUri(idTheso, uri);
        }

        String isReplacedBy = null;
        if (CollectionUtils.isNotEmpty(conceptResource.getsKOSReplaces())) {
            for (SKOSReplaces replace : conceptResource.getsKOSReplaces()) {
                if (SKOSProperty.isReplacedBy == replace.getProperty()) {
                    if (isReplacedBy == null) isReplacedBy = "";
                    isReplacedBy = isReplacedBy + SEPERATEUR + getOriginalId(replace.getTargetUri());
                }
            }
            if (isReplacedBy != null && isReplacedBy.length() > 0) {
                isReplacedBy = isReplacedBy.substring(SEPERATEUR.length(), isReplacedBy.length());
            }
        }

        Date created = null;
        Date modified = null;
        
        if (StringUtils.isEmpty(formatDate)) {
            formatDate = "dd-mm-yyyy";
        }
        try {                
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate);
            for (SKOSDate date : conceptResource.getDateList()) {
                if(date.getDate() != null && !date.getDate().isEmpty()) {
                    if (date.getProperty() == SKOSProperty.created) {
                        created = simpleDateFormat.parse(date.getDate());
                    } 
                    if ((date.getProperty() == SKOSProperty.modified)) {
                        modified = simpleDateFormat.parse(date.getDate());
                    }
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(ImportRdf4jHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        

        String sql = "";
        try ( Connection conn = ds.getConnection();  Statement stmt = conn.createStatement()) {
            sql = "CALL opentheso_add_new_concept('" + idTheso + "', "
                    + "'" + idConcept + "', "
                    + idUser + ", "
                    + "'" + conceptStatus + "', "
                    + "'" + conceptType + "', "
                    + (notationConcept == null ? null : "'" + notationConcept + "'") + ""
                    + ", " 
                    + (idArk == null ? "''":  "'" + idArk + "'") + ", "
                    + isTopConcept + ", "
                    + "'" + idHandle + "', "
                    + "'" + idDoi + "', "
                    + (prefTerm == null ? null : "'" + prefTerm.replaceAll("'", "''") + "'") + ", "
                    + (relations == null ? null : "'" + relations + "'") + ", "
                    + (customRelations == null ? null : "'" + customRelations + "'") + ", "    
                    + (notes == null ? null : "'" + notes.replaceAll("'", "''") + "'") + ", "
                    + (nonPrefTerm == null ? null : "'" + nonPrefTerm.replaceAll("'", "''") + "'") + ", "
                    + (alignements == null ? null : "'" + alignements.replaceAll("'", "''") + "'") + ", "
                    + (images == null ? null : "'" + images + "'") + ", "
                    + (isReplacedBy == null ? null : "'" + isReplacedBy + "'") + ", "
                    + isGpsPresent + ", " 
                    + (altitude == null ? null : Double.parseDouble(altitude)) + ", "
                    + (longitude == null ? null : Double.parseDouble(longitude)) + ", "
                    //+ "'" + created + "', "
                    + (created == null ? null : "'" + created + "'") + ", "

                    //+ "'" + modified + "'"
                     + (modified== null ? null : "'" + modified + "'")                    
                    + ")";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("SQL : " + sql);
            System.out.println(e.getMessage());
            System.out.println("--------------------------------");
        }
        
        addExternalResources(idTheso,idConcept, conceptResource.getDcRelations());
    }
    private void addExternalResources(String idTheso, String idConcept, ArrayList<String> externalRelations) {
        StringPlus stringPlus = new StringPlus();        
        ExternalResourcesHelper externalResourcesHelper = new ExternalResourcesHelper();        
        
        for (String externalRelation : externalRelations) {
            if(externalRelation == null || externalRelation.isEmpty()) {
                return;
            }
            if(!stringPlus.urlValidator(externalRelation)){
                return;            
            }
            if(!externalResourcesHelper.addExternalResource(
                    ds,
                    idConcept,
                    idTheso,
                    "",
                    externalRelation,
                    idUser)) {
            }
        }
    }

    public void addFoafImages(ArrayList<SKOSResource> foafImages, String idTheso){
        String images;        
        for (SKOSResource sKOSResource : foafImages) {
            FoafImage foafImage = sKOSResource.getFoafImage();
            if(foafImage == null) return;
            images = foafImage.getImageName() + SOUS_SEPERATEUR + foafImage.getCopyRight() + SOUS_SEPERATEUR + sKOSResource.getUri();
            if(StringUtils.isEmpty(images)) return;
            String sql = "";
            try ( Connection conn = ds.getConnection();  Statement stmt = conn.createStatement()) {
                sql = "CALL opentheso_add_external_images("
                    + "'" + idTheso + "',"
                    + "'" + sKOSResource.getIdentifier() + "',"
                    + idUser
                    + ",'" + images + "'" 
                    + ")";
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                System.out.println("SQL : " + sql);
                System.out.println(e.getMessage());
                System.out.println("--------------------------------");
            }  
        }
    }
    
    public void addFacetsV2(ArrayList<SKOSResource> facetResources, String idTheso) {

        for (SKOSResource facetSKOSResource : facetResources) {

            String idFacet = getIdFromUri(facetSKOSResource.getUri());
            if (idFacet == null) {
                continue;
            }

            if (facetSKOSResource.getLabelsList().isEmpty()) {
                continue;
            }

            String idConceptParent = null;
            for (SKOSRelation relation : facetSKOSResource.getRelationsList()) {
                if (relation.getProperty() == SKOSProperty.superOrdinate) {
                    idConceptParent = getOriginalId(relation.getTargetUri());
                    break;
                }
            }
            if (idConceptParent == null) {
                continue;
            }

            String labels = "";
            for (SKOSLabel sKOSLabel : facetSKOSResource.getLabelsList()) {
                labels = labels + SEPERATEUR + sKOSLabel.getLabel() + SOUS_SEPERATEUR + sKOSLabel.getLanguage();
            }
            if (labels.length() > 0) {
                labels = labels.substring(2, labels.length());
            }

            String membres = null;
            if (CollectionUtils.isNotEmpty(facetSKOSResource.getRelationsList())) {
                membres = "";
                for (SKOSRelation member : facetSKOSResource.getRelationsList()) {
                    if (member.getProperty() == SKOSProperty.member) {
                        membres = membres + SEPERATEUR + getOriginalId(member.getTargetUri());
                    }
                }
                if (membres.length() > 0) {
                    membres = membres.substring(2, membres.length());
                }
            }

            String sql = "";
            try ( Connection conn = ds.getConnection();  Statement stmt = conn.createStatement()) {
                sql = "CALL opentheso_add_facet('" + idFacet + "', '"
                    + idTheso + "', '"
                    + idConceptParent + "', '"
                    + labels.replaceAll("'", "''") + "', "
                    + (membres == null ? null : "'" + membres + "'") + ")";
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                System.out.println("SQL : " + sql);
                System.out.println(e.getMessage());
                System.out.println("--------------------------------");
            }
        }
    }

    public void initAddConceptsStruct(AddConceptsStruct acs, SKOSResource conceptResource,
            String idTheso, boolean isCandidatImport) {

        acs.conceptResource = conceptResource;
        acs.concept = new Concept();
        String idArk;

        String idConcept = getOriginalId(conceptResource.getUri());
        acs.concept.setIdConcept(idConcept);
        if (conceptResource.getStatus() == SKOSProperty.deprecated) {
            acs.conceptStatus = "dep";
        }

        // option cochée
        if ("ark".equalsIgnoreCase(selectedIdentifier)) {
            //   if(conceptResource.getArkId() != null && !conceptResource.getArkId().isEmpty())
            idArk = getIdArkFromUri(conceptResource.getUri());
            acs.concept.setIdArk(idArk);
        }
        if ("handle".equalsIgnoreCase(selectedIdentifier)) {
            acs.concept.setIdHandle(getIdHandleFromUri(conceptResource.getUri()));
        }
        if ("doi".equalsIgnoreCase(selectedIdentifier)) {
            acs.concept.setIdDoi(getIdDoiFromUri(conceptResource.getUri()));
        }

        acs.concept.setIdThesaurus(idTheso);
        addNotation(acs);
        addGPSCoordinates(acs);
        addLabel(acs);
        addDocumentation(acs);
        addDate(acs);
        addReplaces(acs);

        for (SKOSCreator c : conceptResource.getCreatorList()) {
            if (c.getProperty() == SKOSProperty.creator) {
                acs.concept.setCreatorName(c.getCreator());
            }

            if (c.getProperty() == SKOSProperty.contributor) {
                acs.concept.setContributorName(c.getCreator());
            }
        }

        if (isCandidatImport) {
            addMessages(acs);
            addVotes(acs);
            addStatut(acs, conceptResource.getSkosStatus(), idTheso, idConcept);
        }

        addAlignment(acs, idTheso);
        addImages(acs);

        if (isFirst) {
            isFirst = false;
            String uri = conceptResource.getUri().substring(0, conceptResource.getUri().lastIndexOf("/"));
            if (uri == null || uri.isEmpty()) {
                uri = conceptResource.getUri();
            }
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
        if (skosStatus == null) {
            acs.status.setMessage("");
            acs.status.setIdStatus("1");
        } else {
            acs.status.setMessage(skosStatus.getMessage());
            acs.status.setIdStatus(skosStatus.getIdStatus());
        }
    }

    public void addConceptToBdd(AddConceptsStruct acs, String idTheso, boolean isCandidatImport) {
        if (!acs.conceptHelper.insertConceptInTable(ds, acs.concept, idUser)) {
            System.out.println("Erreur sur le Concept = " + acs.concept.getIdConcept());
        }
        acs.termHelper.insertTerm(ds, acs.nodeTerm, idUser);

        RelationsHelper relationsHelper = new RelationsHelper();

        for (HierarchicalRelationship hierarchicalRelationship : acs.hierarchicalRelationships) {
            switch (hierarchicalRelationship.getRole()) {
                case "NT":
                    if (!relationsHelper.insertHierarchicalRelation(ds,
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2())) {

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

                        message.append(System.getProperty("line.separator"));
                        message.append("Erreur sur la relation = ");
                        message.append(acs.concept.getIdConcept());
                        message.append(" ## ");
                        message.append(hierarchicalRelationship.getRole());
                    }
                    break;
            }
        }

        for (NodeNote nodeNoteList1 : acs.nodeNotes) {

            if (nodeNoteList1.getNotetypecode().equals("customnote") || nodeNoteList1.getNotetypecode().equals("scopeNote") || nodeNoteList1.getNotetypecode().equals("note")) {
                acs.noteHelper.addConceptNote(ds, acs.concept.getIdConcept(), nodeNoteList1.getLang(),
                        idTheso, nodeNoteList1.getLexicalvalue(), nodeNoteList1.getNotetypecode(),"", idUser);
            }

            if (nodeNoteList1.getNotetypecode().equals("definition")
                    || nodeNoteList1.getNotetypecode().equals("historyNote")
                    || nodeNoteList1.getNotetypecode().equals("editorialNote")
                    || nodeNoteList1.getNotetypecode().equals("changeNote")
                    || nodeNoteList1.getNotetypecode().equals("example")) {
                acs.noteHelper.addTermNote(ds, acs.nodeTerm.getIdTerm(), nodeNoteList1.getLang(),
                        idTheso, nodeNoteList1.getLexicalvalue(), nodeNoteList1.getNotetypecode(),"", idUser);
            }

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
            if (acs.nodeGps.getLatitude() != 0.0 && acs.nodeGps.getLongitude() != 0.0) {
                // insertion des données GPS
                acs.gpsHelper.insertCoordonees(ds, acs.concept.getIdConcept(),
                        idTheso,//thesaurus.getId_thesaurus(),
                        acs.nodeGps.getLatitude(), acs.nodeGps.getLongitude());
            }
        }

        if (acs.isTopConcept) {
            if (!acs.conceptHelper.setTopConcept(ds, acs.concept.getIdConcept(), idTheso)) {//thesaurus.getId_thesaurus())) {
                // erreur;
            }
        }

        // ajout des images externes URI
        for (NodeImage nodeImage : acs.nodeImages) {
            acs.imagesHelper.addExternalImage(ds, acs.concept.getIdConcept(), idTheso, nodeImage.getImageName(), nodeImage.getCopyRight() ,nodeImage.getUri(), idUser);
        }

        DeprecateHelper deprecateHelper = new DeprecateHelper();
        if (acs.conceptStatus.equalsIgnoreCase("dep")) {
            deprecateHelper.deprecateConcept(ds, acs.concept.getIdConcept(), idTheso, idUser);
        }
        /// ajout des relations de concepts dépréciés
        for (NodeIdValue nodeIdValue : acs.replacedBy) {
            deprecateHelper.addReplacedBy(ds, acs.concept.getIdConcept(), idTheso, nodeIdValue.getId(), idUser);
        }

        // initialisation des variables
        acs.concept = null;
        acs.concept = new Concept();

        acs.nodeTerm = null;
        acs.nodeTerm = new NodeTerm();

        if (acs.nodeTermTraductionList != null) {
            acs.nodeTermTraductionList.clear();
        }

        if (acs.nodeEMList != null) {
            acs.nodeEMList.clear();
        }

        if (acs.nodeNotes != null) {
            acs.nodeNotes.clear();
        }

        if (acs.nodeAlignments != null) {
            acs.nodeAlignments.clear();
        }

        if (acs.hierarchicalRelationships != null) {
            acs.hierarchicalRelationships.clear();
        }

        if (acs.idGrps != null) {
            acs.idGrps.clear();
        }

        acs.isTopConcept = false;

        acs.nodeGps = null;
        acs.nodeGps = new NodeGps();

        if (acs.nodeImages != null) {
            acs.nodeImages.clear();
        }

        if (acs.replacedBy != null) {
            acs.replacedBy.clear();
        }

        if (acs.replaces != null) {
            acs.replaces.clear();
        }
        acs.conceptStatus = "";

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
                if (nodeNote != null) {
                    voteDto.setIdNote(nodeNote.getId_note() + "");
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
        for (NodeImage nodeImage : acs.conceptResource.getNodeImage()) {
            if (nodeImage != null && (!nodeImage.getUri().isEmpty())) {
                NodeImage nodeImage1 = new NodeImage();
                nodeImage1.setImageName(nodeImage.getImageName());
                nodeImage1.setCopyRight(nodeImage.getCopyRight());
                nodeImage1.setUri(nodeImage.getUri());
                acs.nodeImages.add(nodeImage1);
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
        if (idLang == null || idLang.isEmpty()) {
            return;
        }
        if (idLangsFound.contains(idLang)) {
            return;
        }
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
            if (StringUtils.isEmpty(formatDate)) {
                formatDate = "dd-mm-yyyy";
            }

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

    private void addReplaces(AddConceptsStruct acs) {
        int prop;
        for (SKOSReplaces replace : acs.conceptResource.getsKOSReplaces()) {
            prop = replace.getProperty();
            switch (prop) {
                case SKOSProperty.isReplacedBy:
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(getOriginalId(replace.getTargetUri()));
                    acs.replacedBy.add(nodeIdValue);
                    break;
                case SKOSProperty.replaces:
                    NodeIdValue nodeIdValue2 = new NodeIdValue();
                    nodeIdValue2.setId(getOriginalId(replace.getTargetUri()));
                    acs.replaces.add(nodeIdValue2);
                    break;
                default:
                    break;
            }
        }
    }

    public void addRelation(AddConceptsStruct acs, String idTheso) {
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
                hierarchicalRelationship.setIdConcept2(getOriginalId(relation.getTargetUri()));
                hierarchicalRelationship.setIdThesaurus(idTheso);
                hierarchicalRelationship.setRole(role);
                acs.hierarchicalRelationships.add(hierarchicalRelationship);

            } else if (prop == SKOSProperty.inScheme) {

            } else if (prop == SKOSProperty.topConceptOf) {
                acs.isTopConcept = true;
            } else if (prop == SKOSProperty.memberOf) {
                acs.collectionToAdd = getIdFromUri(relation.getTargetUri());
            }
            if (hasTopConcceptList.contains(acs.conceptResource.getUri())) {
                if (!acs.isTopConcept == false) {
                    acs.isTopConcept = true;
                }
            }
        }
    }

    private String getIdFromUri(String uri) {
        boolean pass = false;

        if (uri.contains("idc=")) {
            if (uri.contains("&")) {
                String str = uri.substring(uri.indexOf("idc="));
                uri = str.substring(4, str.indexOf("&"));
            } else {
                uri = uri.substring(uri.indexOf("idc=") + 4, uri.length());
            }
            pass = true;
        }
        if (!pass) {
            if (uri.contains("idg=")) {
                if (uri.contains("&")) {
                    uri = uri.substring(uri.indexOf("idg=") + 4, uri.indexOf("&"));
                } else {
                    uri = uri.substring(uri.indexOf("idg=") + 4, uri.length());
                }
                pass = true;
            }
        }
        if (!pass) {
            if (uri.contains("idf=")) {
                if (uri.contains("&")) {
                    uri = uri.substring(uri.indexOf("idf=") + 4, uri.indexOf("&"));
                } else {
                    uri = uri.substring(uri.indexOf("idf=") + 4, uri.length());
                }
                pass = true;
            }
        }
        if (!pass) {
            if (uri.contains("#")) {
                uri = uri.substring(uri.indexOf("#") + 1, uri.length());
            } else {
                uri = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
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
        String id = "";
        if (uri.contains("ark:/")) {
            id = uri.substring(uri.indexOf("ark:/") + 5, uri.length());
        }
//      if(id == null) return getIdFromUri(uri);
        return id;
    }

    private String getIdHandleFromUri(String uri) {
        // URI de type Handle
        String id = null;
        if (prefixHandle == null) {
            return getIdFromUri(uri);
        }
        if (uri.contains(prefixHandle)) {
            id = uri.substring(uri.indexOf(prefixHandle), uri.length());
        }
        if (id == null) {
            return getIdFromUri(uri);
        }
        return id;
    }

    private String getIdDoiFromUri(String uri) {
        // URI de type Doi
        String id = null;
        if (prefixDoi == null) {
            return getIdFromUri(uri);
        }
        if (uri.contains(prefixDoi)) {
            id = uri.substring(uri.indexOf(prefixDoi), uri.length());
        }
        if (id == null) {
            return getIdFromUri(uri);
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
    }

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

    public String getPrefixDoi() {
        return prefixDoi;
    }

    public void setPrefixDoi(String prefixDoi) {
        this.prefixDoi = prefixDoi;
    }

    public HikariDataSource getDs() {
        return ds;
    }

    public void setDs(HikariDataSource ds) {
        this.ds = ds;
    }
}
