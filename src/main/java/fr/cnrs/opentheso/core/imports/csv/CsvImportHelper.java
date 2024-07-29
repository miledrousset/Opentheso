package fr.cnrs.opentheso.core.imports.csv;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bean.importexport.ImportFileBean;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.ConceptGroupLabel;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.ExternalResourcesHelper;
import fr.cnrs.opentheso.bdd.helper.GpsHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.ImagesHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeReplaceValueByValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miled.rousset
 */
public class CsvImportHelper {

    private String message = "";
    private NodePreference nodePreference;

    private String langueSource;
    private int idUser;
    private String formatDate;

    public CsvImportHelper(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public CsvImportHelper() {
    }

    /**
     * initialisation des paramètres d'import
     *
     * @param formatDate
     * @param idUser
     * @param idGroupUser
     * @param langueSource
     * @return
     */
    public boolean setInfos(
            String formatDate, int idUser,
            int idGroupUser,
            String langueSource) {
        this.idUser = idUser;
        this.langueSource = langueSource;
        this.formatDate = formatDate;
        return true;
    }

    public String getMessage() {
        return message;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public String getFormatDate() {
        return formatDate;
    }

    public void setFormatDate(String formatDate) {
        this.formatDate = formatDate;
    }
    

    /**
     * Cette fonction permet de créer un thésaurus avec ses traductions (Import)
     * elle retourne l'identifiant du thésaurus, sinon Null
     *
     * @param ds
     * @param thesoName
     * @param idLang
     * @param idProject
     * @param nodeUser
     * @return
     */
    public String createTheso(HikariDataSource ds, String thesoName, String idLang, int idProject, NodeUser nodeUser) {

        try ( Connection conn = ds.getConnection()) {
            Thesaurus thesaurus = new Thesaurus();

            thesaurus.setCreator(nodeUser.getName());
            thesaurus.setContributor(nodeUser.getName());
            thesaurus.setLanguage(idLang);

            ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
            thesaurusHelper.setIdentifierType("2");
            conn.setAutoCommit(false);

            String idTheso1;
            if ((idTheso1 = thesaurusHelper.addThesaurusRollBack(conn, "", false)) == null) {
                conn.rollback();
                conn.close();
                return null;
            }

            thesaurus.setId_thesaurus(idTheso1);

            if (thesoName.isEmpty()) {
                thesoName = "theso_" + idTheso1;
            }
            thesaurus.setTitle(thesoName);

            if (!thesaurusHelper.addThesaurusTraductionRollBack(conn, thesaurus)) {
                conn.rollback();
                conn.close();
                return null;
            }

            // ajouter le thésaurus dans le group de l'utilisateur
            if (idProject != -1) { // si le groupeUser = - 1, c'est le cas d'un SuperAdmin, alors on n'intègre pas le thésaurus dans un groupUser
                UserHelper userHelper = new UserHelper();
                if (!userHelper.addThesoToGroup(conn, thesaurus.getId_thesaurus(), idProject)) {
                    conn.rollback();
                    conn.close();
                    return null;
                }
            }
            conn.commit();
            return idTheso1;
        } catch (SQLException ex) {
            Logger.getLogger(CsvImportHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void addLangsToThesaurus(HikariDataSource ds, ArrayList<String> langs, String idTheso) {

        for (String idLang : langs) {
            if (!new ThesaurusHelper().isLanguageExistOfThesaurus(ds, idTheso, idLang)) {
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
                new ThesaurusHelper().addThesaurusTraduction(ds, thesaurus1);
            }
        }
    }

    public void addSingleConcept(HikariDataSource ds, String idTheso, String idConceptPere, String idGroup, int idUser,
            CsvReadHelper.ConceptObject conceptObject) {

        boolean first = true;
        String idConcept = null;
        String idTerm = null;

        // ajout du concept
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.setNodePreference(nodePreference);
        Concept concept = new Concept();
        TermHelper termHelper = new TermHelper();

        // On vérifie si le conceptPere est un Groupe, alors il faut ajouter un TopTerm, sinon, c'est un concept avec des relations
        if (idConceptPere == null) {
            if(conceptObject.getBroaders() == null || conceptObject.getBroaders().isEmpty())
                concept.setTopConcept(true);
        } else {
            concept.setTopConcept(false);
        }

        concept.setIdGroup(idGroup);
        concept.setIdThesaurus(idTheso);
        concept.setStatus("");
        concept.setNotation("");
        concept.setIdConcept(conceptObject.getIdConcept());

        Term term = new Term();
        term.setId_thesaurus(idTheso);

        // ajout des PrefLabel
        for (CsvReadHelper.Label prefLabel : conceptObject.getPrefLabels()) {
            if (first) {
                term.setLang(prefLabel.getLang());
                term.setLexical_value(prefLabel.getLabel());
                term.setSource("");
                term.setStatus("");
                idConcept = conceptHelper.addConcept(ds, idConceptPere, "NT", concept, term, idUser);
                if (idConcept == null) {
                    message = message + "\n" + "erreur dans l'intégration du concept " + prefLabel.getLabel();
                    return;
                } else {
                    conceptObject.setIdConcept(idConcept);
                    idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idTheso);
                    conceptObject.setIdTerm(idTerm);

                    // synonymes et cachés
                    addAltLabels(ds, idTheso, conceptObject);

                    // notes
                    addNotes(ds, idTheso, conceptObject);

                    // relations
                    addRelations(ds, idTheso, conceptObject);

                    // alignements
                    addAlignments(ds, idTheso, conceptObject);

                    // géolocalisation
                    addGeoLocalisation(ds, idTheso, conceptObject);
                    
                    

                    first = false;
                }

            } // ajout des traductions
            else {
                if (idConcept != null) {
                    term.setId_thesaurus(idTheso);
                    term.setLang(prefLabel.getLang());
                    term.setLexical_value(prefLabel.getLabel());
                    term.setId_term(idTerm);
                    term.setContributor(idUser);
                    term.setCreator(idUser);
                    term.setSource("");
                    term.setStatus("");
                    if (!conceptHelper.addConceptTraduction(ds, term, idUser)) {
                        message = message + "\n" + "erreur dans l'intégration de la traduction " + prefLabel.getLabel();
                    }
                }
            }
        }

    }

    /**
     * permet d'intégrer le thésaurs dans la base de données (d'après un objet
     * lu d'un fichier CSV)
     *
     * @param ds
     * @param fileBean
     * @param thesoName
     * @param conceptObject
     * @param langs
     * @return
     */
    public boolean addTheso(HikariDataSource ds, ImportFileBean fileBean, String thesoName,
            ArrayList<CsvReadHelper.ConceptObject> conceptObject, ArrayList<String> langs) {
        // création du thésaurus
        String idTheso = "";//createTheso(ds, thesoName);
        if (idTheso == null) {
            return false;
        }
        addLangsToThesaurus(ds, idTheso, thesoName, langs);

        GroupHelper groupHelper = new GroupHelper();
        if (!groupHelper.addGroupDefault(ds, langueSource, idTheso)) {
            return false;
        }
        
        for (CsvReadHelper.ConceptObject conceptObject1 : conceptObject) {
            switch (conceptObject1.getType().trim().toLowerCase()) {
                case "skos:concept":
                    // ajout de concept
                    if (!addConcept(ds, idTheso, conceptObject1)) {
                        return false;
                    }
                    break;
                case "skos:collection":
                    // ajout de groupe
                    if (!addGroup(ds, idTheso, conceptObject1)) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * permet d'ajouter les langues détectées au thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @param langs
     */
    private void addLangsToThesaurus(HikariDataSource ds,
            String idThesaurus,
            String name,
            ArrayList<String> langs) {

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        if (name.isEmpty()) {
            name = "theso_" + idThesaurus;
        }

        for (int i = 0; i < langs.size(); i++) {
            if (!thesaurusHelper.isLanguageExistOfThesaurus(ds, idThesaurus, langs.get(i).trim())) {
                Thesaurus thesaurus1 = new Thesaurus();
                thesaurus1.setId_thesaurus(idThesaurus);
                thesaurus1.setContributor("");
                thesaurus1.setCoverage("");
                thesaurus1.setCreator("");
                thesaurus1.setDescription("");
                thesaurus1.setFormat("");
                thesaurus1.setLanguage(langs.get(i));
                thesaurus1.setPublisher("");
                thesaurus1.setRelation("");
                thesaurus1.setRights("");
                thesaurus1.setSource("");
                thesaurus1.setSubject("");
                thesaurus1.setTitle(name);
                thesaurus1.setType("");
                thesaurusHelper.addThesaurusTraduction(ds, thesaurus1);
            }
        }
    }

    /**
     * Permet d'ajouter les groupes/collections...
     *
     * @param ds
     * @param idTheso
     * @param conceptObject
     * @return
     */
    public boolean addGroup(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {
        // récupération des groups ou domaine
        GroupHelper groupHelper = new GroupHelper();

        String idGroup = conceptObject.getIdConcept();
        if (idGroup == null || idGroup.isEmpty()) {
            message = message + "\n" + "Identifiant Groupe manquant";
            return false;
        }

        // création d'une sous collection en cas d'appertenance à une collection supérieure
   /*     if (!conceptObject.getSubGroups().isEmpty()) {
            for (String idFatherGroup : conceptObject.getMembers()) {
                groupHelper.addSubGroup(ds, idFatherGroup, idGroup, idTheso);
            }
        }        */
        
        // ajout des concepts à la collection
        if (!conceptObject.getMembers().isEmpty()) {
            for (String conceptId : conceptObject.getMembers()) {
                groupHelper.addConceptGroupConcept(ds, idGroup, conceptId, idTheso);
            }
        }
        
        if (StringUtils.isEmpty(formatDate)) {
            formatDate = "yyyy-mm-dd";
        }
        Date created = null;
        Date modified = null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate);        
        try {
            if(conceptObject.getCreated() != null && !conceptObject.getCreated().isEmpty())
                created = simpleDateFormat.parse(conceptObject.getCreated());
            if(conceptObject.getModified() != null && !conceptObject.getModified().isEmpty())
                modified = simpleDateFormat.parse(conceptObject.getModified());            
        } catch (ParseException ex) {
            Logger.getLogger(CsvImportHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        groupHelper.insertGroup(ds, idGroup, idTheso, "", "C", conceptObject.getNotation(),
                "", false, created, modified, idUser);

        ConceptGroupLabel conceptGroupLabel = new ConceptGroupLabel();
        for (CsvReadHelper.Label label : conceptObject.getPrefLabels()) {
            // ajouter les traductions des Groupes
            conceptGroupLabel.setIdgroup(idGroup);
            conceptGroupLabel.setIdthesaurus(idTheso);
            conceptGroupLabel.setLang(label.getLang());
            conceptGroupLabel.setLexicalvalue(label.getLabel());
            groupHelper.addGroupTraduction(ds, conceptGroupLabel, idUser);
        }

        addNotes(ds, idTheso, conceptObject);
        
        return true;
    }

    public boolean addConcept(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        conceptObject.setIdTerm(new TermHelper().getIdTermOfConcept(ds, conceptObject.getIdConcept(), idTheso));

        if (!addPrefLabel(ds, idTheso, conceptObject)) {
            return false;
        }
        // synonymes et cachés
        if (!addAltLabels(ds, idTheso, conceptObject)) {
            return false;
        }
        // notes
        if (!addNotes(ds, idTheso, conceptObject)) {
            return false;
        }
        // relations
        if (!addRelations(ds, idTheso, conceptObject)) {
            return false;
        }
        // alignements
        if (!addAlignments(ds, idTheso, conceptObject)) {
            return false;
        }
        // géolocalisation
        if (!addGeoLocalisation(ds, idTheso, conceptObject)) {
            return false;
        }
        // Membres ou appartenance aux groupes
        if (!addMembers(ds, idTheso, conceptObject)) {
            return false;
        }

        return true;
    }

    private final static String SEPERATEUR = "##";
    private final static String SOUS_SEPERATEUR = "@@";

    public boolean addConceptV2(HikariDataSource ds, String idTheso, 
            CsvReadHelper.ConceptObject conceptObject, int idUser) {

        // Membres ou appartenance aux groupes
        if (!addMembers(ds, idTheso, conceptObject)) {
            return false;
        }

        String conceptStatus = "";
        String conceptType;
        String idHandle = "";
        String idDoi = "";
        boolean isTopConcept = true;
        
        String replacedBy = null;
        
        // le status du concept (déprécié ...)
        if(conceptObject.isDeprecated()) {
            conceptStatus = "DEP";
            if (CollectionUtils.isNotEmpty(conceptObject.getReplacedBy())) {
                for (String replace : conceptObject.getReplacedBy()) {
                    if(StringUtils.isEmpty(replacedBy)) {
                        replacedBy = replace;
                    } else {
                        replacedBy = replacedBy + SEPERATEUR + replace;
                    }
                }
            }            
        }
        else
            conceptStatus= "D";
        
        // concept type
        conceptType = conceptObject.getConceptType();
        if(StringUtils.isEmpty(conceptType)) 
            conceptType = "concept";

        // IMAGES
        //-- 'name1@@copyright1@@url1##name2@@copyright2@@url2'
        String images = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getImages())) {
            images = "";
            for (NodeImage nodeImage : conceptObject.getImages()) {
                if(nodeImage == null) continue;
                if (StringUtils.isEmpty(nodeImage.getUri())) continue;
                
                if(StringUtils.isEmpty(images)) {
                    images = nodeImage.getImageName() + SOUS_SEPERATEUR + nodeImage.getCopyRight() + SOUS_SEPERATEUR + nodeImage.getUri() + SOUS_SEPERATEUR + nodeImage.getCreator();
                }
                else {    
                    images = images + SEPERATEUR + nodeImage.getImageName() + SOUS_SEPERATEUR + nodeImage.getCopyRight() + SOUS_SEPERATEUR + nodeImage.getUri() + SOUS_SEPERATEUR + nodeImage.getCreator();
                }
            }
        }

        // ALIGNEMENT
        //-- 'author@concept_target@thesaurus_target@uri_target@alignement_id_type@internal_id_thesaurus@internal_id_concept'
        String alignements = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getExactMatchs())) {
            alignements = "";
            for (String uri : conceptObject.getExactMatchs()) {
                alignements = alignements + SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + uri
                        + SOUS_SEPERATEUR + 1
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getCloseMatchs())) {
            if (alignements == null) {
                alignements = "";
            }
            for (String uri : conceptObject.getCloseMatchs()) {
                alignements = alignements + SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + uri
                        + SOUS_SEPERATEUR + 2
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getBroadMatchs())) {
            if (alignements == null) {
                alignements = "";
            }
            for (String uri : conceptObject.getBroadMatchs()) {
                alignements = alignements + SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + uri
                        + SOUS_SEPERATEUR + 3
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getRelatedMatchs())) {
            if (alignements == null) {
                alignements = "";
            }
            for (String uri : conceptObject.getRelatedMatchs()) {
                alignements = alignements + SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + uri
                        + SOUS_SEPERATEUR + 4
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getNarrowMatchs())) {
            if (alignements == null) {
                alignements = "";
            }
            for (String uri : conceptObject.getNarrowMatchs()) {
                alignements = alignements + SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + ""
                        + SOUS_SEPERATEUR + uri
                        + SOUS_SEPERATEUR + 5
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (alignements != null && alignements.length() > 0) {
            alignements = alignements.substring(SEPERATEUR.length(), alignements.length());
        }

        String prefTerm = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getPrefLabels())) {
            prefTerm = "";
            for (CsvReadHelper.Label label : conceptObject.getPrefLabels()) {
                prefTerm += SEPERATEUR + label.getLabel() + SOUS_SEPERATEUR + label.getLang();
            }
            if (prefTerm.length() > 0) {
                prefTerm = prefTerm.substring(SEPERATEUR.length(), prefTerm.length());
            }
        }

        //Non Pref Term
        //-- 'id_term@lexical_value@lang@id_thesaurus@source@status@hiden'
        String nonPrefTerm = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getAltLabels())) {
            nonPrefTerm = "";
            for (CsvReadHelper.Label label : conceptObject.getAltLabels()) {
                nonPrefTerm = nonPrefTerm + SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + label.getLabel()
                        + SOUS_SEPERATEUR + label.getLang()
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + "USE"
                        + SOUS_SEPERATEUR + false;
            }
        }

        if (CollectionUtils.isNotEmpty(conceptObject.getAltLabels())) {
            if (nonPrefTerm == null) {
                nonPrefTerm = "";
            }
            for (CsvReadHelper.Label altLabel : conceptObject.getHiddenLabels()) {
                nonPrefTerm = nonPrefTerm + SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + altLabel.getLabel()
                        + SOUS_SEPERATEUR + altLabel.getLang()
                        + SOUS_SEPERATEUR + idTheso
                        + SOUS_SEPERATEUR + idUser
                        + SOUS_SEPERATEUR + "Hiddden"
                        + SOUS_SEPERATEUR + true;
            }
        }
        if (nonPrefTerm != null && nonPrefTerm.length() > 0) {
            nonPrefTerm = nonPrefTerm.substring(SEPERATEUR.length(), nonPrefTerm.length());
        }

        //Relation
        //-- 'id_concept1@role@id_concept2'
        String relations = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getBroaders())) {
            relations = "";
            isTopConcept = false;
            for (String idConcept2 : conceptObject.getBroaders()) {
                relations += SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + "BT"
                        + SOUS_SEPERATEUR + idConcept2;
                relations += SEPERATEUR + idConcept2
                        + SOUS_SEPERATEUR + "NT"
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();                
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getNarrowers())) {
            if (relations == null) {
                relations = "";
            }
            for (String idConcept2 : conceptObject.getNarrowers()) {
                relations += SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + "NT"
                        + SOUS_SEPERATEUR + idConcept2;
                relations += SEPERATEUR + idConcept2
                        + SOUS_SEPERATEUR + "BT"
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();                
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getRelateds())) {
            if (relations == null) {
                relations = "";
            }
            for (String idConcept2 : conceptObject.getRelateds()) {
                relations += SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + "RT"
                        + SOUS_SEPERATEUR + idConcept2;
                relations += SEPERATEUR + idConcept2
                        + SOUS_SEPERATEUR + "RT"
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();                
            }
        }
        if (relations != null && relations.length() > 0) {
            relations = relations.substring(SEPERATEUR.length(), relations.length());
        }

        //CustomRelation
        //-- 'id_concept1@role@id_concept2'
        String customRelations = null;        
        if (CollectionUtils.isNotEmpty(conceptObject.getCustomRelations())) {
            customRelations = "";
            for (NodeIdValue nodeIdValue  : conceptObject.getCustomRelations()) {
                customRelations += SEPERATEUR + conceptObject.getIdConcept()
                        + SOUS_SEPERATEUR + nodeIdValue.getValue()
                        + SOUS_SEPERATEUR + nodeIdValue.getId();
            }
        }    
        if (customRelations != null && customRelations.length() > 0) {
            customRelations = customRelations.substring(SEPERATEUR.length(), customRelations.length());
        }        

        //Notes
        //-- 'value@typeCode@lang@id_term'
        String notes = getNotes(conceptObject);

        String gps = null;
        if (StringUtils.isNotEmpty(conceptObject.getLatitude())) {
            gps = conceptObject.getLatitude() + SOUS_SEPERATEUR + conceptObject.getLongitude();
        }
        if (StringUtils.isNotEmpty(conceptObject.getGps())) {
            if (gps == null) {
                gps = "";
            } else {
                gps += gps + SEPERATEUR;
            }
            var gpsList = ConceptView.readGps(conceptObject.getGps(), "", "");
            if (CollectionUtils.isNotEmpty(gpsList)) {
                for (Gps gpsValue : gpsList) {
                    gps += SEPERATEUR + gpsValue.getLatitude() + SOUS_SEPERATEUR + gpsValue.getLongitude();
                }
            }
        }

        String dcterms = null;
        
        String sql = "";
        try ( Connection conn = ds.getConnection();  Statement stmt = conn.createStatement()) {
            sql = "CALL opentheso_add_new_concept('" + idTheso + "', "
                    + "'" + conceptObject.getIdConcept() + "', "
                    + idUser + ", "
                    + "'" + conceptStatus + "', "
                    + "'" + conceptType + "', "
                    + (conceptObject.getNotation() == null ? null : "'" + conceptObject.getNotation() + "'") + ""
                    + ","
                    + (conceptObject.getArkId() == null ? "''":  "'" + conceptObject.getArkId() + "'") + ", "
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
                    + (replacedBy == null ? null : "'" + replacedBy + "'")  + ", "
                    + (gps != null) + ", "
                    + (gps == null ? null : "'" + gps + "'") + ", "
                    + (conceptObject.getCreated()== null ? null : "'" + conceptObject.getCreated() + "'") + ", "
                    + (conceptObject.getModified()== null ? null : "'" + conceptObject.getModified() + "'") + ", "
                    + (dcterms == null ? null : "'" + dcterms + "'") 
                    + ")";

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("SQL : " + sql);
            System.out.println(e.getMessage());
            System.out.println("--------------------------------");
            message = message + "Erreur concept : " + prefTerm + "(" + conceptObject.getIdConcept() +"(\n";
            return false;
        }
        addExternalResources(ds, idTheso, conceptObject.getIdConcept(), conceptObject.getExternalResources());        
        return true;
    }

    private String getNotes(CsvReadHelper.ConceptObject conceptObject){
        //Notes
        //-- 'value@typeCode@lang@id_term'
        String notes = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getNote())) {
            notes = "";
            for (CsvReadHelper.Label note : conceptObject.getNote()) {
                notes += SEPERATEUR + note.getLabel()
                        + SOUS_SEPERATEUR + "note"
                        + SOUS_SEPERATEUR + note.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getDefinitions())) {
            if (notes == null) {
                notes = "";
            }
            for (CsvReadHelper.Label definition : conceptObject.getDefinitions()) {
                notes += SEPERATEUR + definition.getLabel()
                        + SOUS_SEPERATEUR + "definition"
                        + SOUS_SEPERATEUR + definition.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getChangeNotes())) {
            if (notes == null) {
                notes = "";
            }
            for (CsvReadHelper.Label changeNote : conceptObject.getChangeNotes()) {
                notes += SEPERATEUR + changeNote.getLabel()
                        + SOUS_SEPERATEUR + "changeNote"
                        + SOUS_SEPERATEUR + changeNote.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getEditorialNotes())) {
            if (notes == null) {
                notes = "";
            }
            for (CsvReadHelper.Label editorialNote : conceptObject.getEditorialNotes()) {
                notes += SEPERATEUR + editorialNote.getLabel()
                        + SOUS_SEPERATEUR + "editorialNote"
                        + SOUS_SEPERATEUR + editorialNote.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getHistoryNotes())) {
            if (notes == null) {
                notes = "";
            }
            for (CsvReadHelper.Label historyNote : conceptObject.getHistoryNotes()) {
                notes += SEPERATEUR + historyNote.getLabel()
                        + SOUS_SEPERATEUR + "historyNote"
                        + SOUS_SEPERATEUR + historyNote.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getScopeNotes())) {
            if (notes == null) {
                notes = "";
            }
            for (CsvReadHelper.Label scopeNote : conceptObject.getScopeNotes()) {
                notes += SEPERATEUR + scopeNote.getLabel()
                        + SOUS_SEPERATEUR + "scopeNote"
                        + SOUS_SEPERATEUR + scopeNote.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (CollectionUtils.isNotEmpty(conceptObject.getExamples())) {
            if (notes == null) {
                notes = "";
            }
            for (CsvReadHelper.Label example : conceptObject.getExamples()) {
                notes += SEPERATEUR + example.getLabel()
                        + SOUS_SEPERATEUR + "example"
                        + SOUS_SEPERATEUR + example.getLang()
                        + SOUS_SEPERATEUR + conceptObject.getIdConcept();
            }
        }
        if (notes != null && notes.length() > 0) {
            notes = notes.substring(SEPERATEUR.length(), notes.length());
        } 
        return notes;
    }
    
    private void addExternalResources(HikariDataSource ds, String idTheso, String idConcept, ArrayList<String> externalResources) {
      
        ExternalResourcesHelper externalResourcesHelper = new ExternalResourcesHelper();        
        StringPlus stringPlus = new StringPlus();
        
        for (String externalResource : externalResources) {
            if(externalResource == null || externalResource.isEmpty()) {
                return;
            }
            if(!stringPlus.urlValidator(externalResource)){
                return;            
            }
            if(!externalResourcesHelper.addExternalResource(
                    ds,
                    idConcept,
                    idTheso,
                    "",
                    externalResource,
                    idUser)) {
            }
        }
    }

    
    public void addFacets(HikariDataSource ds, CsvReadHelper.ConceptObject conceptObject, String idTheso) {
        String idFacet = conceptObject.getIdConcept();
        if (idFacet == null) {
            return;
        }
        if (conceptObject.getPrefLabels().isEmpty()) {
            return;
        }

        String idConceptParent = conceptObject.getSuperOrdinate();
        if(StringUtils.isEmpty(idConceptParent)) return;

        String labels = "";
        for (CsvReadHelper.Label prefLabel : conceptObject.getPrefLabels()) {
            if(StringUtils.isEmpty(labels)){
                labels = prefLabel.getLabel() + SOUS_SEPERATEUR + prefLabel.getLang();
            } else {
                labels = labels + SEPERATEUR + prefLabel.getLabel() + SOUS_SEPERATEUR + prefLabel.getLang();
            }
        }


        String membres = null;
        if (CollectionUtils.isNotEmpty(conceptObject.getMembers())) {
            membres = "";
            for (String member : conceptObject.getMembers()) {
                if(StringUtils.isEmpty(membres)){
                    membres = member;
                } else {
                    membres = membres + SEPERATEUR + member;
                }
            }
        }
        
        //Notes
        //-- 'value@typeCode@lang@id_term'
        String notes = getNotes(conceptObject);
        
        String sql = "";
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            sql = "CALL opentheso_add_facet('" + idFacet + "', "
                    + idUser + ", '"
                    + idTheso + "', '"
                    + idConceptParent + "', '"
                    + labels.replaceAll("'", "''") + "', "
                    + (membres == null ? null : "'" + membres + "'") + ", "
                    + (notes == null ? null : "'" + notes.replaceAll("'", "''") + "'")
                    + ")";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("SQL : " + sql);
            System.out.println(e.getMessage());
            System.out.println("--------------------------------");
        }

    }    
    
    private boolean addPrefLabel(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
            message = message + "\n" + "concept sans identifiant : " + conceptObject.getPrefLabels().toString();
            return false;
        }

        // ajout du concept
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();

        conceptHelper.setNodePreference(nodePreference);
        Concept concept = new Concept();

        // On vérifie si le concept a des BT (termes génériques), alors il faut ajouter un TopTerm, sinon, c'est un concept avec des rerlations
        if (conceptObject.getBroaders().isEmpty()) {
            concept.setTopConcept(true);
        } else {
            concept.setTopConcept(false);
        }

        concept.setIdThesaurus(idTheso);
        concept.setStatus("");
        concept.setNotation(conceptObject.getNotation());
        concept.setIdConcept(conceptObject.getIdConcept());
        concept.setIdArk(conceptObject.getArkId());
        
        if (StringUtils.isEmpty(formatDate)) {
            formatDate = "dd-mm-yyyy";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate);        
        try {
            if(conceptObject.getCreated() != null && !conceptObject.getCreated().isEmpty())
                concept.setCreated(simpleDateFormat.parse(conceptObject.getCreated()));
            if(conceptObject.getModified() != null && !conceptObject.getModified().isEmpty())
                concept.setModified(simpleDateFormat.parse(conceptObject.getModified()));            
        } catch (ParseException ex) {
            Logger.getLogger(CsvImportHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        // ajout du concept
        if (!conceptHelper.insertConceptInTable(ds, concept, idUser)) {
            message = message + "\n" + "erreur dans l'intégration du concept " + conceptObject.getIdConcept();
            return false;
        }

        Term term = new Term();
        term.setId_thesaurus(idTheso);
        term.setId_term(conceptObject.getIdConcept());
        conceptObject.setIdTerm(conceptObject.getIdConcept());
        term.setContributor(idUser);
        term.setCreator(idUser);
        term.setSource("");
        term.setStatus("");

        try ( Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            // ajout de la relation entre le concept et le terme
            if (!termHelper.addLinkTerm(conn, term, conceptObject.getIdConcept(), idUser)) {
                message = message + "\n" + "erreur dans l'intégration du concept " + conceptObject.getIdConcept();
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            // ajout des PrefLabel
            for (CsvReadHelper.Label prefLabel : conceptObject.getPrefLabels()) {
                // ajout des traductions
         //       term.setId_thesaurus(idTheso);
                term.setLang(prefLabel.getLang());
                term.setLexical_value(prefLabel.getLabel());
          //      term.setId_term(conceptObject.getIdConcept());
          //      term.setContributor(idUser);
          //      term.setCreator(idUser);
          //      term.setSource("");
          //      term.setStatus("");
                if (!termHelper.addTermTraduction(conn, term, idUser)) {
                    conn.rollback();
                    conn.close();
                    message = message + "\n" + "erreur dans l'intégration du terme " + prefLabel.getLabel();
                    return false;
                }
                conn.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(CsvImportHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * Intègre les synonymes et synonymes cachés
     *
     * @param ds
     * @param idTheso
     * @param conceptObject
     * @return
     */
    private boolean addAltLabels(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {
        Term term = new Term();
        TermHelper termHelper = new TermHelper();
        for (CsvReadHelper.Label altLabel : conceptObject.getAltLabels()) {
            term.setId_term(conceptObject.getIdTerm());
            term.setId_thesaurus(idTheso);
            term.setLang(altLabel.getLang());
            term.setLexical_value(altLabel.getLabel());
            term.setHidden(false);
            term.setStatus("USE");
            term.setSource("");

            if (!termHelper.addNonPreferredTerm(ds, term, idUser)) {
                message = message + "\n" + "erreur dans l'intégration du synonyme : " + altLabel.getLabel();
            }
        }
        for (CsvReadHelper.Label altLabel : conceptObject.getHiddenLabels()) {
            term.setId_term(conceptObject.getIdTerm());
            term.setId_thesaurus(idTheso);
            term.setLang(altLabel.getLang());
            term.setLexical_value(altLabel.getLabel());
            term.setHidden(true);
            term.setStatus("Hiddden");
            term.setSource("");

            if (!termHelper.addNonPreferredTerm(ds, term, idUser)) {
                message = message + "\n" + "erreur dans l'intégration du synonyme : " + altLabel.getLabel();
            }
        }
        return true;
    }

    /**
     * Intègre les notes
     *
     * @param ds
     * @param idTheso
     * @param conceptObject
     * @return
     */
    private boolean addNotes(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        NoteHelper noteHelper = new NoteHelper();
        for (CsvReadHelper.Label note : conceptObject.getNote()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "note","", idUser);
        }
        for (CsvReadHelper.Label note : conceptObject.getDefinitions()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "definition","", idUser);
        }
        for (CsvReadHelper.Label note : conceptObject.getChangeNotes()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "changeNote","", idUser);
        }
        for (CsvReadHelper.Label note : conceptObject.getEditorialNotes()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "editorialNote","", idUser);
        }
        for (CsvReadHelper.Label note : conceptObject.getHistoryNotes()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "historyNote", "",idUser);
        }
        for (CsvReadHelper.Label note : conceptObject.getScopeNotes()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "scopeNote", "",idUser);
        }
        for (CsvReadHelper.Label note : conceptObject.getExamples()) {
            noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                    "example", "",idUser);
        }
        return true;
    }

    private boolean addRelations(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        RelationsHelper relationsHelper = new RelationsHelper();

        for (String idConcept2 : conceptObject.getBroaders()) {
            if (!relationsHelper.insertHierarchicalRelation(ds, conceptObject.getIdConcept(), idTheso, "BT", idConcept2)) {
                message = message + "\n" + "erreur dans de la relation BT: " + conceptObject.getIdConcept();
            }
            // pour créer la relation réciproque si elle n'existe pas
            if (!relationsHelper.insertHierarchicalRelation(ds, idConcept2, idTheso, "NT", conceptObject.getIdConcept())) {
                message = message + "\n" + "erreur dans de la relation BT: " + conceptObject.getIdConcept();
            }
            new ConceptHelper().setNotTopConcept(ds, conceptObject.getIdConcept(), idTheso);
        }

        for (String idConcept2 : conceptObject.getNarrowers()) {
            if (!relationsHelper.insertHierarchicalRelation(ds, conceptObject.getIdConcept(), idTheso, "NT", idConcept2)) {
                message = message + "\n" + "erreur dans de la relation NT: " + conceptObject.getIdConcept();
            }
            // pour créer la relation réciproque si elle n'existe pas
            if (!relationsHelper.insertHierarchicalRelation(ds, idConcept2, idTheso, "BT", conceptObject.getIdConcept())) {
                message = message + "\n" + "erreur dans de la relation NT: " + conceptObject.getIdConcept();
            }
        }

        for (String idConcept2 : conceptObject.getRelateds()) {
            if (!relationsHelper.insertHierarchicalRelation(ds, conceptObject.getIdConcept(), idTheso, "RT", idConcept2)) {
                message = message + "\n" + "erreur dans de la relation RT: " + conceptObject.getIdConcept();
            }
//            // pour créer la relation réciproque si elle n'existe pas
            if (!relationsHelper.insertHierarchicalRelation(ds, idConcept2, idTheso, "RT", conceptObject.getIdConcept())) {
                message = message + "\n" + "erreur dans de la relation RT: " + conceptObject.getIdConcept();
            }
        }
        return true;
    }

    private boolean addAlignments(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        AlignmentHelper alignmentHelper = new AlignmentHelper();
        NodeAlignment nodeAlignment = new NodeAlignment();
        nodeAlignment.setId_author(idUser);
        nodeAlignment.setConcept_target("");
        nodeAlignment.setThesaurus_target("");
        nodeAlignment.setInternal_id_concept(conceptObject.getIdConcept());
        nodeAlignment.setInternal_id_thesaurus(idTheso);

//        exactMatch   = 1;
//        closeMatch   = 2;
//        broadMatch   = 3;
//        relatedMatch = 4;        
//        narrowMatch  = 5;
        for (String uri : conceptObject.getExactMatchs()) {
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(1);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }
        for (String uri : conceptObject.getCloseMatchs()) {
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(2);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }
        for (String uri : conceptObject.getBroadMatchs()) {
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(3);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }
        for (String uri : conceptObject.getRelatedMatchs()) {
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(4);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }
        for (String uri : conceptObject.getNarrowMatchs()) {
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(5);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }

        return true;
    }

    private boolean addGeoLocalisation(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        if (StringUtils.isEmpty(conceptObject.getLatitude())) {
            if (StringUtils.isEmpty(conceptObject.getGps()) || conceptObject.getGps().length() < 3) {
                return true;
            }

            String[] values = conceptObject.getGps().split("##");
            for (String value1 : values) {
                String[] gps = value1.split("@@");
                new GpsHelper().insertCoordonees(ds, conceptObject.getIdConcept(), idTheso,
                        Double.valueOf(gps[1]), Double.valueOf(gps[2]));
            }
            return true;
        } else {
            return addPointGeoLocalisation(ds, idTheso, conceptObject);
        }
    }
    private boolean addPointGeoLocalisation(
            HikariDataSource ds,
            String idTheso,
            CsvReadHelper.ConceptObject conceptObject) {

        Double latitude;
        Double longitude;

        if (conceptObject.getLatitude() == null || conceptObject.getLatitude().isEmpty()) {
            return true;
        }
        if (conceptObject.getLongitude() == null || conceptObject.getLongitude().isEmpty()) {
            return true;
        }
        try {
            latitude = Double.parseDouble(conceptObject.getLatitude());
            longitude = Double.parseDouble(conceptObject.getLongitude());
        } catch (Exception e) {
            return true;
        }
        GpsHelper gpsHelper = new GpsHelper();
        gpsHelper.insertCoordonees(ds, conceptObject.getIdConcept(),
                idTheso,
                latitude, longitude);
        return true;
    }
    
    
    private boolean addMembers(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        if (!conceptObject.getMembers().isEmpty()) {
            for (String member : conceptObject.getMembers()) {
                new GroupHelper().addConceptGroupConcept(ds, member.trim(), conceptObject.getIdConcept(), idTheso);
            }
        }
        return true;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// mise à jour des concepts /////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////    
    
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////    
    //////// Méthodes pour le remplacement des valeurs suivant le type de données ////////    
    //////////////////////////////////////////////////////////////////////////////////////
    /**
     * permet de remplacer un lot de concepts, l'ancienne valeure par la nouvelle
     * @param ds
     * @param idTheso
     * @param nodeReplaceValueByValue
     * @param idUser1
     * @return 
     */
    public boolean updateConceptValueByNewValue(HikariDataSource ds, String idTheso, NodeReplaceValueByValue nodeReplaceValueByValue, int idUser1) {
        message = "";
        switch (nodeReplaceValueByValue.getSKOSProperty()) {
            case SKOSProperty.PREF_LABEL:
                if(!StringUtils.isEmpty(nodeReplaceValueByValue.getNewValue())) {
                    if (!updatePrefLabel(ds, idTheso, nodeReplaceValueByValue, idUser1)) {
                        addMessage("Erreur : ", nodeReplaceValueByValue);
                    }
                }
                break;
            case SKOSProperty.ALT_LABEL:
                if (!updateAltLabel(ds, idTheso, nodeReplaceValueByValue, idUser1)) {
                    addMessage("Erreur : ", nodeReplaceValueByValue);
                }
                break;    
            case SKOSProperty.DEFINITION:
                if (!updateDefinition(ds, idTheso, nodeReplaceValueByValue, idUser1)) {
                    addMessage("Erreur : ", nodeReplaceValueByValue);
                }
                break;                  
                
                
            /*    Action dangereuse, à activer plus tard */
            case SKOSProperty.BROADER:
                if (!updateBroader(ds, idTheso, nodeReplaceValueByValue, idUser1)) {
                    addMessage("Erreur : ", nodeReplaceValueByValue);
                }
                break;                
                
            default:
                break;
        }
        return true;
    }    
    private boolean updatePrefLabel(HikariDataSource ds, String idTheso, NodeReplaceValueByValue nodeReplaceValueByValue, int idUser1) {
        if (nodeReplaceValueByValue.getIdConcept() == null || nodeReplaceValueByValue.getIdConcept().isEmpty()) {
            addMessage("concept sans identifiant :", nodeReplaceValueByValue);
            return false;
        }
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(ds, nodeReplaceValueByValue.getIdConcept(), idTheso);
        if (idTerm == null || idTerm.isEmpty()) {
            return false;
        }

        if(!termHelper.updateTraduction(ds, nodeReplaceValueByValue.getNewValue(), idTerm, nodeReplaceValueByValue.getIdLang(), idTheso, idUser1)) {
            addMessage("Rename error :", nodeReplaceValueByValue);
        }
        return true;
    }
    private boolean updateAltLabel(HikariDataSource ds, String idTheso, NodeReplaceValueByValue nodeReplaceValueByValue, int idUser1) {
        if (nodeReplaceValueByValue.getIdConcept() == null || nodeReplaceValueByValue.getIdConcept().isEmpty()) {
            addMessage("concept sans identifiant :", nodeReplaceValueByValue);
            return false;
        }
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(ds, nodeReplaceValueByValue.getIdConcept(), idTheso);
        if (idTerm == null || idTerm.isEmpty()) {
            return false;
        }
        
        // si l'ancienne valeur et la nouvelle valeur sont présente
        if(!StringUtils.isEmpty(nodeReplaceValueByValue.getOldValue())) {
            if(!StringUtils.isEmpty(nodeReplaceValueByValue.getNewValue())) {
                // on met remplace la valeur du altLabel par la nouvelle valeur
                if(!termHelper.updateTermSynonyme(ds, nodeReplaceValueByValue.getOldValue(),
                        nodeReplaceValueByValue.getNewValue(), idTerm, nodeReplaceValueByValue.getIdLang(),
                        idTheso, false, idUser1)) {
                    addMessage("Rename AltLabel error :", nodeReplaceValueByValue);
                }                
            }
        } else {
            if(!StringUtils.isEmpty(nodeReplaceValueByValue.getNewValue())) {
                // on ajoute un nouvel altLabel 
                if(!termHelper.addNonPreferredTerm(ds, idTerm, nodeReplaceValueByValue.getNewValue(),
                        nodeReplaceValueByValue.getIdLang(), idTheso, "", "", false, idUser1)) {
                    addMessage("Insert AltLabel error :", nodeReplaceValueByValue);
                }                 
            }
        }
        return true;
    }   
    private boolean updateDefinition(HikariDataSource ds, String idTheso, NodeReplaceValueByValue nodeReplaceValueByValue, int idUser1) {
        if (nodeReplaceValueByValue.getIdConcept() == null || nodeReplaceValueByValue.getIdConcept().isEmpty()) {
            addMessage("concept sans identifiant :", nodeReplaceValueByValue);
            return false;
        }
        NoteHelper noteHelper = new NoteHelper();
       
        // si l'ancienne valeur et la nouvelle valeur sont présente
        if(!StringUtils.isEmpty(nodeReplaceValueByValue.getOldValue())) {
            if(!StringUtils.isEmpty(nodeReplaceValueByValue.getNewValue())) {
                // on supprime d'abord l'ancienne note
                int idNote = noteHelper.getNoteByValueAndThesaurus(ds, nodeReplaceValueByValue.getOldValue(),
                        "definition", nodeReplaceValueByValue.getIdLang(), idTheso); 
                if(idNote != -1){
                    // on remplace la valeur du altLabel par la nouvelle valeur
                    if(!noteHelper.updateNote(ds, idNote, nodeReplaceValueByValue.getIdConcept(), nodeReplaceValueByValue.getIdLang(), idTheso, 
                            nodeReplaceValueByValue.getNewValue(), "definition", idUser1)) {
                        addMessage("Rename definition error :", nodeReplaceValueByValue);
                    }                      
                } else {
                    if (!noteHelper.isNoteExist(
                            ds,
                            nodeReplaceValueByValue.getIdConcept(),
                            idTheso,
                            nodeReplaceValueByValue.getIdLang(),
                            nodeReplaceValueByValue.getNewValue(),
                            "definition")) {                    
                        if(!noteHelper.addNote(ds, nodeReplaceValueByValue.getIdConcept(), nodeReplaceValueByValue.getIdLang(), idTheso, 
                                nodeReplaceValueByValue.getNewValue(), "definition", "", idUser1)) {
                            addMessage("add definition error :", nodeReplaceValueByValue);
                        }
                    }                    
                }
            }
        } else {
            if(!StringUtils.isEmpty(nodeReplaceValueByValue.getNewValue())) {
                // on ajoute une nouvelle définition
                if (!noteHelper.isNoteExist(
                        ds,
                        nodeReplaceValueByValue.getIdConcept(),
                        idTheso,
                        nodeReplaceValueByValue.getIdLang(),
                        nodeReplaceValueByValue.getNewValue(),
                        "definition")) {                 
                    if(!noteHelper.addNote(ds, nodeReplaceValueByValue.getIdConcept(), nodeReplaceValueByValue.getIdLang(), idTheso, 
                            nodeReplaceValueByValue.getNewValue(), "definition", "", idUser1)) {
                        addMessage("add definition error :", nodeReplaceValueByValue);
                    }     
                }
            }
        }
        return true;
    }      
    private boolean updateBroader(HikariDataSource ds, String idTheso, NodeReplaceValueByValue nodeReplaceValueByValue, int idUser1) {
        if (nodeReplaceValueByValue.getIdConcept() == null || nodeReplaceValueByValue.getIdConcept().isEmpty()) {
            addMessage("concept sans identifiant :", nodeReplaceValueByValue);
            return false;
        }
        
        RelationsHelper relationsHelper = new RelationsHelper();
        if(!StringUtils.isEmpty( nodeReplaceValueByValue.getOldValue())){
            if(!relationsHelper.deleteRelationBT(ds, nodeReplaceValueByValue.getIdConcept(), idTheso, nodeReplaceValueByValue.getOldValue(), idUser1)) {
                addMessage("Rename error :", nodeReplaceValueByValue);
            }      
        }
        if(!relationsHelper.addRelationBT(ds, nodeReplaceValueByValue.getIdConcept(), idTheso, nodeReplaceValueByValue.getNewValue(), idUser1)) {
            addMessage("Rename error :", nodeReplaceValueByValue);
        }
        new ConceptHelper().setNotTopConcept(ds, nodeReplaceValueByValue.getIdConcept(), idTheso);
        /*
        if(!relationsHelper.deleteRelationNT(ds, nodeReplaceValueByValue.getIdConcept(), idTheso, nodeReplaceValueByValue.getNewValue(), idUser1)) {
            addMessage("Rename error :", nodeReplaceValueByValue);
        } */       
        return true;
    }      
    private void addMessage(String error, NodeReplaceValueByValue nodeReplaceValueByValue) {
        message = message + error + nodeReplaceValueByValue.getIdConcept() + "##" + nodeReplaceValueByValue.getOldValue() + "##" + nodeReplaceValueByValue.getNewValue() + "##" + nodeReplaceValueByValue.getIdLang() + "\n";
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public boolean updateConcept(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject, int idUser1) {

        conceptObject.setIdTerm(new TermHelper().getIdTermOfConcept(ds, conceptObject.getIdConcept(), idTheso));
        updatePrefLabel(ds, idTheso, conceptObject, idUser1);
        updateAltLabel(ds, idTheso, conceptObject, idUser1);
        updateNotes(ds, idTheso, conceptObject, idUser1);
        updateAlignments(ds, idTheso, conceptObject, idUser1);
        updateGeoLocalisation(ds, idTheso, conceptObject);
        updateImages(ds, idTheso, conceptObject, idUser1);
        
        addExternalResources(ds, idTheso, conceptObject.getIdConcept(), conceptObject.getExternalResources()); 
        return true;
    }

    private boolean updatePrefLabel(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject, int idUser1) {
        if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
            message = message + "\n" + "concept sans identifiant : " + conceptObject.getPrefLabels().toString();
            return false;
        }
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(ds, conceptObject.getIdConcept(), idTheso);
        if (idTerm == null || idTerm.isEmpty()) {
            return false;
        }
        String oldLabel;

        for (CsvReadHelper.Label prefLabel : conceptObject.getPrefLabels()) {
            // si le label n'existe pas dans cette langue, on l'ajoute
            if (!termHelper.isTermExistInThisLang(ds, idTerm, prefLabel.getLang(), idTheso)) {
                if (!termHelper.addTraduction(ds, prefLabel.getLabel(), idTerm, prefLabel.getLang(), "import", "", idTheso, idUser1)) {
                    return false;
                }                  
            } else {
                oldLabel = termHelper.getLexicalValue(ds, idTerm, idTheso, prefLabel.getLang());
                
                // si le label est fourni vide, il faut alors supprimer cette traduction
                if (prefLabel.getLabel().isEmpty()) {
                    return termHelper.deleteTraductionOfTerm(ds, idTerm, oldLabel, prefLabel.getLang(), idTheso, idUser1);
                } 
                
                // si le label d'origine est vide (cas rare et normalement impossible)
                if (oldLabel.isEmpty()) {
                    //le terme est alors à mettre à jour
                    return termHelper.updateTraduction(ds, prefLabel.getLabel(), idTerm, prefLabel.getLang(), idTheso, idUser1);
                }        
                
                // on vérifie si le terme est identique, on ne fait rien
                if (oldLabel.trim().equals(prefLabel.getLabel().trim())) {
                    continue;
                }  
                
                // le terme est alors à mettre à jour
                if (!termHelper.updateTraduction(ds, prefLabel.getLabel(), idTerm, prefLabel.getLang(), idTheso, idUser1)) {
                    return false;
                }                   
            }            
        }
        return true;
    }

    private boolean updateAltLabel(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject, int idUser1) {
        if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
            message = message + "\n" + "concept sans identifiant : " + conceptObject.getPrefLabels().toString();
            return false;
        }
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(ds, conceptObject.getIdConcept(), idTheso);
        if (idTerm == null || idTerm.isEmpty()) {
            return false;
        }
        ArrayList<String> oldLabels;

        // suppression des altLabel par langue
        ArrayList<String> langs = getLangs(conceptObject.getAltLabels());
        for (String lang : langs) {
            oldLabels = termHelper.getLexicalValueOfAltLabel(ds, idTerm, idTheso, lang);
            for (String oldLabel : oldLabels) {
                termHelper.deleteNonPreferedTerm(ds, idTerm, lang, oldLabel, idTheso, "", idUser1);
                //return false;
            }
        }

        for (CsvReadHelper.Label altLabel : conceptObject.getAltLabels()) {
            // on ajoute les nouveaux prefLabels dans cette langue
            if (!altLabel.getLabel().isEmpty()) {
                termHelper.addNonPreferredTerm(ds, idTerm, altLabel.getLabel(), altLabel.getLang(), idTheso, "import", "", false, idUser);
                //    return false;
            }
        }
        return true;
    }

    private ArrayList<String> getLangs(ArrayList<CsvReadHelper.Label> labels) {
        ArrayList<String> langs = new ArrayList<>();
        for (CsvReadHelper.Label label : labels) {
            if (!langs.contains(label.getLang())) {
                langs.add(label.getLang());
            }
        }
        return langs;
    }

    /**
     * Intègre les notes
     *
     * @param ds
     * @param idTheso
     * @param conceptObject
     * @return
     */
    private boolean updateNotes(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject, int idUser1) {

        NoteHelper noteHelper = new NoteHelper();
        //    ArrayList<NodeNote> oldNotes;     

        // suppression des Notes  par langue
        ArrayList<String> langs = getLangs(conceptObject.getNote());
        for (String lang : langs) {
            //    oldNotes = noteHelper.getListNotesConcept(ds, conceptObject.getIdConcept(), idTheso, lang);
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "note")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getNote()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "note","", idUser1);
            }
        }
        langs = getLangs(conceptObject.getScopeNotes());
        for (String lang : langs) {
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "scopeNote")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getScopeNotes()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "scopeNote","", idUser1);
            }
        }

        langs = getLangs(conceptObject.getDefinitions());
        for (String lang : langs) {
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "definition")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getDefinitions()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "definition", "",idUser1);
            }
        }

        langs = getLangs(conceptObject.getChangeNotes());
        for (String lang : langs) {
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "changeNote")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getChangeNotes()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "changeNote", "",idUser1);
            }
        }

        langs = getLangs(conceptObject.getEditorialNotes());
        for (String lang : langs) {
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "editorialNote")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getEditorialNotes()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "editorialNote", "",idUser1);
            }
        }

        langs = getLangs(conceptObject.getHistoryNotes());
        for (String lang : langs) {
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "historyNote")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getHistoryNotes()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "historyNote", "",idUser1);
            }
        }

        langs = getLangs(conceptObject.getExamples());
        for (String lang : langs) {
            if (!noteHelper.deleteNoteByLang(ds, conceptObject.getIdConcept(), idTheso, lang, "example")) {
                return false;
            }
        }
        for (CsvReadHelper.Label note : conceptObject.getExamples()) {
            if (!note.getLabel().isEmpty()) {
                noteHelper.addNote(ds, conceptObject.getIdConcept(), note.getLang(), idTheso, note.getLabel(),
                        "example", "",idUser1);
            }
        }
        return true;
    }

    private boolean updateAlignments(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject, int idUser1) {

        AlignmentHelper alignmentHelper = new AlignmentHelper();
        NodeAlignment nodeAlignment = new NodeAlignment();
        nodeAlignment.setId_author(idUser1);
        nodeAlignment.setConcept_target("");
        nodeAlignment.setThesaurus_target("");
        nodeAlignment.setInternal_id_concept(conceptObject.getIdConcept());
        nodeAlignment.setInternal_id_thesaurus(idTheso);

//        exactMatch   = 1;
//        closeMatch   = 2;
//        broadMatch   = 3;
//        relatedMatch = 4;        
//        narrowMatch  = 5;
        /// suppression des alignements 
        if (!conceptObject.getExactMatchs().isEmpty()) {
            alignmentHelper.deleteAlignmentOfConceptByType(ds, conceptObject.getIdConcept(), idTheso, 1);
        }
        for (String uri : conceptObject.getExactMatchs()) {
            if (uri.isEmpty()) {
                continue;
            }
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(1);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                //       message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }

        if (!conceptObject.getCloseMatchs().isEmpty()) {
            alignmentHelper.deleteAlignmentOfConceptByType(ds, conceptObject.getIdConcept(), idTheso, 2);
        }
        for (String uri : conceptObject.getCloseMatchs()) {
            if (uri.isEmpty()) {
                continue;
            }
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(2);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                //        message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }

        if (!conceptObject.getBroadMatchs().isEmpty()) {
            alignmentHelper.deleteAlignmentOfConceptByType(ds, conceptObject.getIdConcept(), idTheso, 3);
        }
        for (String uri : conceptObject.getBroadMatchs()) {
            if (uri.isEmpty()) {
                continue;
            }
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(3);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                //           message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }

        if (!conceptObject.getRelatedMatchs().isEmpty()) {
            alignmentHelper.deleteAlignmentOfConceptByType(ds, conceptObject.getIdConcept(), idTheso, 4);
        }
        for (String uri : conceptObject.getRelatedMatchs()) {
            if (uri.isEmpty()) {
                continue;
            }
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(4);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                //          message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }

        if (!conceptObject.getNarrowMatchs().isEmpty()) {
            alignmentHelper.deleteAlignmentOfConceptByType(ds, conceptObject.getIdConcept(), idTheso, 5);
        }
        for (String uri : conceptObject.getNarrowMatchs()) {
            if (uri.isEmpty()) {
                continue;
            }
            nodeAlignment.setUri_target(uri);
            nodeAlignment.setAlignement_id_type(5);
            if (!alignmentHelper.addNewAlignment(ds, nodeAlignment)) {
                //         message = message + "\n" + "erreur dans l'ajout de l'alignement : " + conceptObject.getIdConcept();
            }
        }

        return true;
    }

    private boolean updateGeoLocalisation(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject) {

        GpsHelper gpsHelper = new GpsHelper();
        if (conceptObject.getLatitude() == null || conceptObject.getLongitude() == null) {
            if(conceptObject.getGps() == null || conceptObject.getGps().isEmpty())
                return true;
        }

        // nettoyege des ancienne valeurs
        gpsHelper.deleteGpsCoordinate(ds, conceptObject.getIdConcept(), idTheso);
       
        
        String gps = null;
        List<NodeGps> nodeGpses = new ArrayList<>();
        
        if (StringUtils.isNotEmpty(conceptObject.getLatitude())) {
            NodeGps nodeGps = new NodeGps();
            try {
                nodeGps.setLatitude(Double.valueOf(conceptObject.getLatitude()));
                nodeGps.setLongitude(Double.valueOf(conceptObject.getLongitude()));
                nodeGps.setPosition(1);
                nodeGpses.add(nodeGps);
            } catch (Exception e) {
                return true;
            }
            gpsHelper.addGpsCoordinates(ds, conceptObject.getIdConcept(), idTheso, nodeGpses);
            
        } else {
            if (StringUtils.isNotEmpty(conceptObject.getGps())) {
                var gpsList = ConceptView.readGps(conceptObject.getGps(), "", "");
                if (CollectionUtils.isNotEmpty(gpsList)) {
                    for (Gps gpsValue : gpsList) {
                        NodeGps nodeGps = new NodeGps();
                        nodeGps.setLatitude(gpsValue.getLatitude());
                        nodeGps.setLongitude(gpsValue.getLongitude());
                        nodeGps.setPosition(gpsValue.getPosition());
                        nodeGpses.add(nodeGps);                                            
                    }
                    gpsHelper.addGpsCoordinates(ds, conceptObject.getIdConcept(), idTheso, nodeGpses);
                }
            }
        }
        return true;
    }

    private boolean updateImages(HikariDataSource ds, String idTheso, CsvReadHelper.ConceptObject conceptObject, int idUser1) {
        ImagesHelper imagesHelper = new ImagesHelper();

        if (conceptObject.getImages() == null || conceptObject.getImages().isEmpty()) {
            return true;
        }

        imagesHelper.deleteAllExternalImage(ds, conceptObject.getIdConcept(), idTheso);
        
        for (NodeImage nodeImage : conceptObject.getImages()) {
            if(nodeImage == null) continue;
            if (StringUtils.isEmpty(nodeImage.getUri())) continue;            

            if (imagesHelper.addExternalImage(ds, conceptObject.getIdConcept(), idTheso, nodeImage.getImageName(), nodeImage.getCopyRight(), nodeImage.getUri(), "", idUser1)) {
                return false;
            }
        }
        return true;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Fin Mise à jour des concepts /////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////     
}
