package fr.cnrs.opentheso.ws.api;

import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DaoResourceHelper;
import fr.cnrs.opentheso.repositories.ExportHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.TermRepository;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.services.PathService;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.concept.Path;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.concept.NodeConceptTree;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.services.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.services.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.utils.JsonHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.faces.context.FacesContext;
import jakarta.json.JsonArray;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class RestRDFHelper {

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ExportRdf4jHelperNew exportRdf4jHelperNew;

    @Autowired
    private PathService pathService;

    @Autowired
    private ExportHelper exportHelper;

    @Autowired
    private AlignmentHelper alignmentHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private DaoResourceHelper daoResourceHelper;


    private enum Choix {
            CONCEPT,
            GROUP,
            THESO
        }

    /**
     * permet de retourner l'URL Opentheso depui un Identifiant ARK
     * ceci pour remplacer la redirection faite par le serveur ARK
     * @param naan
     * @param idArk
     * @return 
     */
    public String getUrlFromIdArk(String naan, String idArk) {
        if (idArk == null || idArk.isEmpty()) {
            return null;
        }
        Choix choix;
        // récupération de l'IdTheso d'après la table Concept
        String idTheso = conceptHelper.getIdThesaurusFromArkId(naan + "/" + idArk);
        choix = Choix.CONCEPT;
        
        if(StringUtils.isEmpty(idTheso)) {
            // cas où c'est l'identifiant d'un thésaurus
            idTheso = thesaurusHelper.getIdThesaurusFromArkId(naan + "/" + idArk);
            choix = Choix.THESO;
        }
        
        if(StringUtils.isEmpty(idTheso)) {
            // cas où c'est l'identifiant d'un groupe
            idTheso = groupHelper.getIdThesaurusFromArkId(naan + "/" + idArk);  
            choix = Choix.GROUP;
        }        
        if(StringUtils.isEmpty(idTheso)){
            return null;
        }
        
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        switch (choix) {
            case CONCEPT:
                String idConcept = conceptHelper.getIdConceptFromArkId(naan + "/" + idArk, idTheso);    
                if(StringUtils.isEmpty(idConcept)){    
                    return null;
                }     
                if (nodePreference == null) {
                    return null;
                }
                return nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idTheso;                  

            case GROUP:
                String idGroup = groupHelper.getIdGroupFromArkId(naan + "/" + idArk, idTheso);    
                if(StringUtils.isEmpty(idGroup)){    
                    return null;
                }     
                if (nodePreference == null) {
                    return null;
                }
                return nodePreference.getCheminSite() + "?idg=" + idGroup + "&idt=" + idTheso;                  
            case THESO:
                // cas où c'est l'identifiant d'un thésaurus
                idTheso = thesaurusHelper.getIdThesaurusFromArkId(naan + "/" + idArk);  
                if(StringUtils.isEmpty(idTheso)){    
                    return null;
                }

                if (nodePreference == null) {
                    return null;
                }
                return nodePreference.getCheminSite() + "?idt=" + idTheso;                
              
            default:
                return null;
        }

    }
    
    public String getAllLinkedConceptsWithOntome__(String idTheso) {
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        ArrayList<NodeIdValue> listLinkedConceptsWithOntome = alignmentHelper.getAllLinkedConceptsWithOntome(idTheso);

        String datasJson;

        JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();
        
        if(!listLinkedConceptsWithOntome.isEmpty()) {
            JsonObjectBuilder jobLabel = Json.createObjectBuilder();
            jobLabel.add("thesaurusLabel", thesaurusHelper.getTitleOfThesaurus(idTheso, nodePreference.getSourceLang()));
            jsonArrayBuilderLang.add(jobLabel.build());
        }
    
        for (NodeIdValue nodeIdValue : listLinkedConceptsWithOntome) {
            JsonObjectBuilder jobLang = Json.createObjectBuilder();
            jobLang.add("uri", getUri(nodePreference, nodeIdValue.getId(), idTheso));
            jobLang.add("class", nodeIdValue.getValue());
            jsonArrayBuilderLang.add(jobLang.build());
        }
        datasJson = jsonArrayBuilderLang.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    public String getLinkedConceptWithOntome__(String idTheso, String cidocClass) {
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        ArrayList<NodeIdValue> listLinkedConceptsWithOntome = alignmentHelper.getLinkedConceptsWithOntome(idTheso, cidocClass);

        String datasJson;

        JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

        if(!listLinkedConceptsWithOntome.isEmpty()) {
            JsonObjectBuilder jobLabel = Json.createObjectBuilder();
            jobLabel.add("thesaurusLabel", thesaurusHelper.getTitleOfThesaurus(idTheso, nodePreference.getSourceLang()));
            jsonArrayBuilderLang.add(jobLabel.build());
        }        
        for (NodeIdValue nodeIdValue : listLinkedConceptsWithOntome) {
            JsonObjectBuilder jobLang = Json.createObjectBuilder();
            jobLang.add("uri", getUri(nodePreference, nodeIdValue.getId(), idTheso));
            jobLang.add("class", nodeIdValue.getValue());
            jsonArrayBuilderLang.add(jobLang.build());
        }
        datasJson = jsonArrayBuilderLang.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    public String getInfosOfConcept(
            String idTheso,
            String idConcept,
            String idLang) {
        if (idTheso == null || idConcept == null || idLang == null) {
            return null;
        }
        String datas = getInfosOfConcept__(
                idTheso, idConcept, idLang);
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * @param idTheso
     * @return
     */
    private String getInfosOfConcept__(String idTheso, String idConcept, String idLang) {

        NodeConcept nodeConcept = conceptHelper.getConcept(idConcept, idTheso, idLang, -1, -1);
        if (nodeConcept == null) {
            return null;
        }

        JsonObjectBuilder job = Json.createObjectBuilder();

        /// Id
        job.add("id", nodeConcept.getConcept().getIdConcept());
        if (nodeConcept.getConcept().getIdArk() != null && !nodeConcept.getConcept().getIdArk().isEmpty()) {
            job.add("ark", nodeConcept.getConcept().getIdArk());
        }
        if (nodeConcept.getConcept().getIdHandle() != null && !nodeConcept.getConcept().getIdHandle().isEmpty()) {
            job.add("handle", nodeConcept.getConcept().getIdHandle());
        }

        // label
        job.add("label", nodeConcept.getTerm().getLexicalValue());

        // synonymes
        JsonArrayBuilder jsonArrayBuilderSyno = Json.createArrayBuilder();
        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
            jsonArrayBuilderSyno.add(nodeEM.getLexicalValue());
        }
        if (jsonArrayBuilderSyno != null) {
            job.add("altLabel", jsonArrayBuilderSyno.build());
        }

        // Associés 
        JsonArrayBuilder jsonArrayBuilderRelate = Json.createArrayBuilder();
        String labelRT;
        for (NodeRT nodeRT : nodeConcept.getNodeRT()) {
            labelRT = conceptHelper.getLexicalValueOfConcept(nodeRT.getIdConcept(), idTheso, idLang);
            if (labelRT != null && !labelRT.isEmpty()) {
                jsonArrayBuilderRelate.add(labelRT);
            }
        }
        if (jsonArrayBuilderRelate != null) {
            job.add("related", jsonArrayBuilderRelate.build());
        }

        // traductions 
        JsonArrayBuilder jsonArrayBuilderTrad = Json.createArrayBuilder();
        for (NodeTermTraduction nodeTermTraduction : nodeConcept.getNodeTermTraductions()) {
            JsonObjectBuilder jobTrad = Json.createObjectBuilder();
            jobTrad.add("lang", nodeTermTraduction.getLang());
            jobTrad.add("label", nodeTermTraduction.getLexicalValue());

            jsonArrayBuilderTrad.add(jobTrad.build());
        }
        if (jsonArrayBuilderTrad != null) {
            job.add("traduction", jsonArrayBuilderTrad.build());
        }

        // Alignements 
        JsonArrayBuilder jsonArrayBuilderAlign = Json.createArrayBuilder();
        for (NodeAlignment nodeAlignment : nodeConcept.getNodeAlignments()) {
            JsonObjectBuilder jobAlign = Json.createObjectBuilder();
            jobAlign.add("type", nodeAlignment.getAlignmentLabelType());
            jobAlign.add("uri", nodeAlignment.getUri_target());

            jsonArrayBuilderAlign.add(jobAlign.build());
        }
        if (jsonArrayBuilderAlign != null) {
            job.add("Alignment", jsonArrayBuilderAlign.build());
        }

        // définitions 
        JsonArrayBuilder jsonArrayBuilderDef = Json.createArrayBuilder();
        for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
            if ("definition".equalsIgnoreCase(nodeNote.getNoteTypeCode())) {
                jsonArrayBuilderDef.add(nodeNote.getLexicalValue());
            }
        }
        if (jsonArrayBuilderDef != null) {
            job.add("definition", jsonArrayBuilderDef.build());
        }

        if (job != null) {
            return job.build().toString();
        } else {
            return null;
        }
    }

    /**
     * Permet de récupérer la liste des topTerms d'un thésaurus
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     * @return
     */
    public String getNarrower(String idTheso, String idConcept, String idLang) {

        if (idTheso == null || idConcept == null || idLang == null) {
            return null;
        }
        String datas = getNarrower__(
                idTheso, idConcept, idLang);
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * recherche par valeur
     *
     * @param idTheso
     * @return
     */
    private String getNarrower__(String idTheso, String idConcept, String idLang) {

        List<NodeConceptTree> nodeConceptTrees = daoResourceHelper.getConceptsNTForTree(idTheso, idConcept, idLang, false, false);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("id", nodeConceptTree.getIdConcept());
            job.add("label", nodeConceptTree.getTitle());
            job.add("haveChildren", nodeConceptTree.isHaveChildren());
            jsonArrayBuilder.add(job.build());
        }
        if (jsonArrayBuilder != null) {
            return jsonArrayBuilder.build().toString();
        } else {
            return null;
        }
    }

    /**
     * Permet de récupérer la liste des topTerms d'un thésaurus
     *
     * @param idTheso
     * @param idLang
     * @return
     */
    public String getTopTerms(String idTheso, String idLang) {

        if (idTheso == null || idLang == null) {
            return null;
        }
        String datas = getTopTerms__(
                idTheso, idLang);
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * recherche par valeur
     *
     * @param idTheso
     * @return
     */
    private String getTopTerms__(String idTheso, String idLang) {

        List<NodeConceptTree> nodeConceptTrees = conceptHelper.getListOfTopConcepts(
                idTheso, idLang, false, false);
        if (nodeConceptTrees == null) {
            return null;
        }

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("id", nodeConceptTree.getIdConcept());
            job.add("label", nodeConceptTree.getTitle());
            job.add("haveChildren", nodeConceptTree.isHaveChildren());
            jsonArrayBuilder.add(job.build());
        }
        if (jsonArrayBuilder != null) {
            return jsonArrayBuilder.build().toString();
        } else {
            return null;
        }

    }

    /**
     * Permet de retourner le prefLabel d'après un idArk avec la langue donnée
     * le résultat est en Json
     *
     * @param naan
     * @param idArk
     * @param idLang
     * @return
     */
    public String getPrefLabelFromArk(String naan, String idArk, String idLang) {

        String datas = getPrefLabelFromArk__(
                naan, idArk, idLang);
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * recherche par valeur
     *
     * @return
     */
    private String getPrefLabelFromArk__(String naan, String idArk, String idLang) {

        if (idArk == null || idLang == null) {
            return null;
        }

        String idTheso = conceptHelper.getIdThesaurusFromArkId(naan + "/" + idArk);
        String idConcept = conceptHelper.getIdConceptFromArkId(naan + "/" + idArk, idTheso);

        if (idTheso == null || idConcept == null) {
            return null;
        }

        String value = conceptHelper.getLexicalValueOfConcept(idConcept, idTheso, idLang);

        if (value == null || value.isEmpty()) {
            return null;
        }

        JsonObject datasJson = Json.createObjectBuilder().add("prefLabel", value).build();

        /*    JsonArrayBuilder jab = Json.createArrayBuilder();        
        jab.add(builder);        
        
        JsonArray datasJson = jab.build();*/
        if (datasJson != null) {
            return datasJson.toString();
        } else {
            return null;
        }
    }

    /**
     * Permet de retourner un concept au format défini en passant par
     * l'identifiant du concept utilisé pour la négociation de contenu
     *
     * @param idConcept
     * @param idTheso
     * @param format
     * @return
     */
    public String exportConceptFromId(String idConcept, String idTheso, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = getConceptFromId(idConcept, idTheso);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    private WriteRdf4j getConceptFromId(
            String idConcept, String idTheso) {
        if (idConcept == null || idTheso == null) {
            return null;
        }
        if (idConcept.isEmpty() || idTheso.isEmpty()) {
            return null;
        }
        idConcept = idConcept.replaceAll("\"", "");
        idTheso = idTheso.replaceAll("\"", "");
        idConcept = idConcept.replaceAll("'", "");
        idTheso = idTheso.replaceAll("'", "");

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        return new WriteRdf4j(skosXmlDocument);

    }

    public String getIdConceptFromDate(
            String idTheso, String fromDate, String format) {

        ArrayList<String> ids = conceptHelper.getIdConceptFromDate(idTheso, fromDate);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        for (String idConcept : ids) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        }

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = new WriteRdf4j(skosXmlDocument);

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * Permet de retourner un concept au format défini en passant par un
     * identifiant Ark utilisé pour la négociation de contenu
     *
     * @param idArk
     * @param format
     * @return
     */
    public String exportConcept(String idArk, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = getConceptFromArk(idArk);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    private WriteRdf4j getConceptFromArk(String idArk) {

        String idTheso = conceptHelper.getIdThesaurusFromArkId(idArk);        
        String idConcept = conceptHelper.getIdConceptFromArkId(idArk, idTheso);


        if (idConcept == null || idTheso == null) {
            return null;
        }

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        exportRdf4jHelperNew.setInfos(nodePreference);

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Permet de retourner un concept au format défini en passant par un
     * identifiant Ark et id thesaurus et filtré par langue et pour récupérer
     * les labels des relations BT et NT
     *
     * @param idArk
     * @param idTheso
     * @param showLabels
     * @param idLang
     * @param format
     * @return
     */
    public String exportConceptFromArkWithLang(String idArk, String idTheso, String idLang, boolean showLabels, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = exportConceptFromArkWithLang__(idArk, idTheso, idLang, showLabels);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    private WriteRdf4j exportConceptFromArkWithLang__(
            String idArk, String idTheso, String idLang, boolean showLabels) {

        String idConcept = conceptHelper.getIdConceptFromArkId(idArk, idTheso);
        if (idTheso == null || idTheso.isEmpty()) {
            idTheso = conceptHelper.getIdThesaurusFromArkId(idArk);
        }

        if (idConcept == null || idTheso == null) {
            return null;
        }

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addconcept(exportRdf4jHelperNew.addSingleConceptByLangV2(idTheso, idConcept, idLang, showLabels));
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Permet de retourner un concept au format défini en passant par un
     * identifiant DOI utilisé pour la négociation de contenu
     *
     * @param doi
     * @param format
     * @return
     */
    public String exportConceptDoi(String doi, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = getConceptFromDoi(doi);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    private WriteRdf4j getConceptFromDoi(String doi) {

        String idConcept = doi;//conceptHelper.getIdConcept FromHandleId(doi);
        String idTheso = conceptHelper.getIdThesaurusFromIdConcept(idConcept);

        if (doi == null || idTheso == null) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Permet de retourner un concept au format défini en passant par un
     * identifiant Handle utilisé pour la négociation de contenu
     *
     * @param handleId
     * @param format
     * @return
     */
    public String exportConceptHdl(String handleId, String format) {
        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = getConceptFromHandle(handleId);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    private WriteRdf4j getConceptFromHandle(
            String handleId) {

        String idConcept = conceptHelper.getIdConceptFromHandleId(handleId);
        String idTheso = conceptHelper.getIdThesaurusFromHandleId(handleId);

        if (idConcept == null || idTheso == null) {
            return null;
        }

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * permet de retourner le format du RDF en utilisant le paramètre d'entrée
     *
     * @param format
     * @return
     */
    private RDFFormat getRDFFormat(String format) {
        RDFFormat rDFFormat = RDFFormat.RDFJSON;
        switch (format) {
            case "application/rdf+xml":
                rDFFormat = RDFFormat.RDFXML;
                break;
            case "application/ld+json":
                rDFFormat = RDFFormat.JSONLD;
                break;
            case "text/turtle":
                rDFFormat = RDFFormat.TURTLE;
                break;
            case "application/json":
                rDFFormat = RDFFormat.RDFJSON;
                break;
                
            case "application/rdf+xml;charset=utf-8":
                rDFFormat = RDFFormat.RDFXML;
                break;
            case "application/ld+json;charset=utf-8":
                rDFFormat = RDFFormat.JSONLD;
                break;
            case "text/turtle;charset=utf-8":
                rDFFormat = RDFFormat.TURTLE;
                break;
            case "application/json;charset=utf-8":
                rDFFormat = RDFFormat.RDFJSON;
                break;                
        }
        return rDFFormat;
    }

    /**
     * Permet de retourner un concept au format défini en passant par un
     * identifiant Ark utilisé pour la négociation de contenu
     *
     * @param idTheso
     * @param lang
     * @param groups
     * @param format
     * @param value
     * @param match
     * @return
     */
    public String findConcepts(String idTheso, String lang, String [] groups, String value, String format, String match) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = findConcepts__(
                value, idTheso, lang, groups, match);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * recherche par valeur
     *
     * @param value
     * @param idTheso
     * @param lang
     * @param groups
     * @return
     */
    private WriteRdf4j findConcepts__(
            
            String value, String idTheso, String lang,  String [] groups, String match) {

        if (value == null || idTheso == null) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        ArrayList<String> idConcepts = null;
        if(StringUtils.isEmpty(match)){
            idConcepts = searchHelper.searchAutoCompletionWSForWidget(value, lang, groups, idTheso);
        } else {
            if(match.equalsIgnoreCase("exact")) {
                idConcepts = searchHelper.searchAutoCompletionWSForWidgetMatchExact(value, lang, groups, idTheso);               
            }
            if(match.equalsIgnoreCase("exactone")) {
                idConcepts = searchHelper.searchAutoCompletionWSForWidgetMatchExactForOneLabel(value, lang, groups, idTheso);            
            }
        }

        if(idConcepts == null) return null;
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        for (String idConcept : idConcepts) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        }
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Permet de retourner les concepts qui correspondent à la notation utilisé
     * pour la négociation de contenu
     *
     * @param idTheso
     * @param format
     * @param value
     * @return
     */
    public String findNotation(String idTheso, String value, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = findNotation__(
                value, idTheso);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * recherche par Notation
     *
     * @param value
     * @param idTheso
     * @return
     */
    private WriteRdf4j findNotation__(String value, String idTheso) {

        if (value == null || idTheso == null) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        ArrayList<String> idConcepts = searchHelper.searchNotationId(value, idTheso);

        if (idConcepts == null || idConcepts.isEmpty()) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        for (String idConcept : idConcepts) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        }
        return new WriteRdf4j(skosXmlDocument);
    }

    public String getChildsArkId(String idArk){

        ArrayList<String> childsIdArks = conceptHelper.getListChildrenOfConceptByArk(idArk);
        
        if(childsIdArks.isEmpty()) return null;
        
        JsonArrayBuilder jsonArrayBuilderChildsArk = Json.createArrayBuilder();
        JsonObjectBuilder job = Json.createObjectBuilder();
        int count = childsIdArks.size();
        job.add("count", count);
        for (String childsIdArk : childsIdArks) {
            jsonArrayBuilderChildsArk.add(childsIdArk);
        }
        job.add("arks", jsonArrayBuilderChildsArk.build());
        return job.build().toString();
    }

    /**
     * Permet de retourner les concepts au format Json avec valeur et URI (pour
     * les programmes qui utilisent l'autocomplétion)
     *
     * @param idTheso
     * @param lang
     * @param groups
     * @param value
     * @param withNotes
     * @return
     */
    public String findAutocompleteConcepts(String idTheso, String lang, String[] groups, String value, boolean withNotes) {

        String datas = findAutocompleteConcepts__(
                value, idTheso, lang, groups, withNotes);
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * recherche par valeur
     *
     * @param value
     * @param idTheso
     * @param lang
     * @return
     */
    public String findAutocompleteConcepts__(String value, String idTheso, String lang, String[] groups, boolean withNotes) {

        if (value == null || idTheso == null) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        JsonHelper jsonHelper = new JsonHelper();
        String uri;
        ArrayList<NodeAutoCompletion> nodeAutoCompletion;

        // recherche de toutes les valeurs
        nodeAutoCompletion = searchHelper.searchAutoCompletionWS(value, lang, groups, idTheso, withNotes);

        if (nodeAutoCompletion == null || nodeAutoCompletion.isEmpty()) {
            return null;
        }

        for (NodeAutoCompletion nodeAutoCompletion1 : nodeAutoCompletion) {
            uri = getUri(nodePreference, nodeAutoCompletion1, idTheso);
            if (withNotes) {
                jsonHelper.addJsonDataFull(uri, nodeAutoCompletion1.getPrefLabel(),
                        nodeAutoCompletion1.getDefinition(), nodeAutoCompletion1.isAltLabel(), nodeAutoCompletion1.getIdConcept());
            } else {
                jsonHelper.addJsonData(uri, nodeAutoCompletion1.getPrefLabel(), nodeAutoCompletion1.getIdConcept());
            }
        }
        JsonArray datasJson = jsonHelper.getBuilder();
        if (datasJson != null) {
            return datasJson.toString();
        } else {
            return null;
        }
    }

    /**
     * Permet de retourner les concepts au format Json avec valeur et URI (pour
     * les programmes qui utilisent l'autocomplétion) mais aussi la branche
     * complète vers la racine
     *
     * @param lang
     * @param idArks
     * @param format
     * @return
     */
    public JsonArrayBuilder findDatasForWidgetByArk(String lang, String[] idArks, String format) {

        return findDatasForWidgetByArk__(lang, idArks, format);
    }

    /**
     * recherche par valeur
     *
     * @param lang
     * @return
     */
    private JsonArrayBuilder findDatasForWidgetByArk__(String lang, String[] idArks, String format) {
        
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idArk : idArks) {
            var idTheso = conceptHelper.getIdThesaurusFromArkId(idArk);
            if(idTheso == null) continue;
            
            var idConcept = conceptHelper.getIdConceptFromArkId(idArk, idTheso);
            if(idConcept == null) continue;
            
            var nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
            if (nodePreference == null) continue;

            var paths = pathService.getPathOfConcept(idConcept, idTheso);
            if (paths != null && !paths.isEmpty()) {
                var element = pathService.getPathWithLabelAsJson(paths, jsonArrayBuilder, idTheso, lang, format);
               // jsonArrayBuilder.add(element);
            }
        }

        return jsonArrayBuilder;
        
    }

    /**
     * Permet de retourner les concepts au format Json avec valeur et URI (pour
     * les programmes qui utilisent l'autocomplétion) mais aussi la branche
     * complète vers la racine
     *
     * @param idTheso
     * @param lang
     * @param groups
     * @param value
     * @param format
     * @param match
     * @return
     */
    public String findDatasForWidget(String idTheso, String lang, String[] groups, String value, String format, boolean match) {

        return findDatasForWidget__(value, idTheso, lang, groups, format, match);
    }

    /**
     * recherche par valeur
     *
     * @param value
     * @param idTheso
     * @param lang
     * @return
     */
    private String findDatasForWidget__(String value, String idTheso, String lang, String[] groups, String format, boolean match) {

        if (idTheso == null) {
            return null;
        }

        var nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        // recherche de toutes les valeurs
        if(StringUtils.isNotEmpty(value)) {
            value = value.trim();
            // si la valeur est entourée de ("), on fait alors une recherche exacte
            if (StringUtils.startsWith(value, "\"") && StringUtils.endsWith(value, "\"")) {
                value = value.replaceAll("\"", "");
                match = true;
            }
        } else
            value = "";

        ArrayList<String> nodeIds;
        if(match) {
            nodeIds = searchHelper.searchAutoCompletionWSForWidgetMatchExact(value, lang, groups, idTheso);
        } else {
            nodeIds = searchHelper.searchAutoCompletionWSForWidget(value, lang, groups, idTheso);
        }

        if (CollectionUtils.isEmpty(nodeIds)) {
            return null;
        }

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idConcept : nodeIds) {
            var paths = pathService.getPathOfConcept(idConcept, idTheso);
            if (CollectionUtils.isNotEmpty(paths)) {
                pathService.getPathWithLabelAsJson(paths, jsonArrayBuilder, idTheso, lang, format);
            }
        }
        return jsonArrayBuilder != null ? jsonArrayBuilder.build().toString() : null;
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la racine (vers le haut)
     *
     * @param idConcept
     * @param idTheso
     * @param format
     * @return skos
     */
    public String brancheOfConceptsTop(String idConcept, String idTheso, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = brancheOfConceptsTop__(
                idConcept, idTheso);
        if (writeRdf4j == null) {
            return messageEmptyRdfXml();
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la racine (vers le haut)
     *
     * @param idConcept
     * @param idTheso
     * @return skos
     */
    private WriteRdf4j brancheOfConceptsTop__(String idConcept, String idTheso) {

        if (idConcept == null || idTheso == null) {
            return null;
        }
        if(!conceptHelper.isIdExiste(idConcept, idTheso)) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        ArrayList<String> path = new ArrayList<>();
        ArrayList<ArrayList<String>> branchs = new ArrayList<>();

        path.add(idConcept);
        branchs = conceptHelper.getPathOfConceptWithoutGroup(idConcept, idTheso, path, branchs);
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        for (ArrayList<String> branch : branchs) {
            for (String idc : branch) {
                skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idc, false));
            }
        }
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la fin (vers le bas)
     *
     * @param idConcept
     * @param idTheso
     * @param format
     * @return skos
     */
    public String brancheOfConceptsDown(String idConcept, String idTheso, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = brancheOfConceptsDown__(
                idConcept, idTheso);
        if (writeRdf4j == null) {
            return messageEmptyRdfXml();
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la fin (vers le bas)
     *
     * @param idConcept
     * @param idTheso
     * @return skos
     */
    private WriteRdf4j brancheOfConceptsDown__(String idConcept, String idTheso) {

        if (idConcept == null || idTheso == null) {
            return null;
        }
        if(!conceptHelper.isIdExiste(idConcept, idTheso)) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        ArrayList<String> path = conceptHelper.getIdsOfBranch(idConcept, idTheso);
        for (String idC : path) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idC, false));
        }
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Fonction qui permet de récupérer toute la branche d'un groupe en partant
     * d'un identifiant d'un group/domaine
     *
     * @param groups
     * @param idTheso
     * @param format
     * @return skos
     */
    public String brancheOfGroup(String[] groups, String idTheso, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = brancheOfGroup__(
                groups, idTheso);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la racine (vers le haut)
     *
     * @param idTheso
     * @return skos
     */
    private WriteRdf4j brancheOfGroup__(String[] groups, String idTheso) {

        if (groups == null || idTheso == null) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        var skosXmlDocument = new SKOSXmlDocument();

        ////////// nouvelle méthode par Miled pour optimiser la récupération des Branches
        String baseUrl;
        if(StringUtils.isEmpty(nodePreference.getCheminSite())) {
            String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
            String serverAdress = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
            String protocole = FacesContext.getCurrentInstance().getExternalContext().getRequestScheme();
            HttpServletRequest request = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            baseUrl = protocole + "://" + serverAdress + ":" + request.getLocalPort() + contextPath;
        } else {
            baseUrl = nodePreference.getCheminSite();
        }
        List<SKOSResource> concepts;
        try {
            for(String idGroup : groups) {
                concepts = exportHelper.getAllConcepts(idTheso, baseUrl, idGroup,
                        nodePreference.getOriginalUri(), nodePreference, false);
                for (SKOSResource concept : concepts) {
                    skosXmlDocument.addconcept(concept);
                }
            }
        } catch (Exception ex){}

        /* ArrayList<String> branchs = conceptHelper.getAllIdConceptOfThesaurusByMultiGroup(idTheso, groups);
         exportRdf4jHelperNew.setInfos(nodePreference);
        for (String idConcept : branchs) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        }*/

         return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Fonction qui permet de récupérer toute la branche d'un groupe en partant
     * d'un identifiant d'un group/domaine
     *
     * @param groups
     * @param idTheso
     * @param lang
     * @return skos
     */
    public String brancheOfGroupAsTree(String[] groups, String idTheso, String lang) {

        String datas = brancheOfGroupAsTree__(
                groups, idTheso, lang);
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la racine (vers le haut)
     *
     * @param idTheso
     * @return skos
     */
    private String brancheOfGroupAsTree__(
            
            String[] groups, String idTheso, String lang) {

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        ArrayList<String> branchs = conceptHelper.getAllIdConceptOfThesaurusByMultiGroup(idTheso, groups);
        
        // construire le tableau JSON avec le chemin vers la racine pour chaque Id
        List<Path> paths;

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (String idConcept : branchs) {
            paths = pathService.getPathOfConcept(idConcept, idTheso);
            if (paths != null && !paths.isEmpty()) {
                pathService.getPathWithLabelAsJson(paths, jsonArrayBuilder, idTheso, lang, null);
            }
        }
        if (jsonArrayBuilder != null) {
            return jsonArrayBuilder.build().toString();
        } else {
            return null;
        }        
    }    
    
    
    
    
    /**
     * Fonction qui permet de récupérer un thésaurus entier uniquement Id et
     * Value en Json
     *
     * @param idTheso
     * @param lang
     * @return skos
     */
    public String getThesoIdValue(String idTheso, String lang) {

        ArrayList<String> idConcepts = conceptHelper.getAllIdConceptOfThesaurus(idTheso);

        String datasJson = null;
        JsonArrayBuilder jsonArrayBuilderLine = Json.createArrayBuilder();

        if (lang != null) {
            for (String idConcept : idConcepts) {
                JsonObjectBuilder jobLine = Json.createObjectBuilder();
                jobLine.add("conceptId", idConcept);
                jobLine.add("arkId", conceptHelper.getIdArkOfConcept(idConcept, idTheso));
                jobLine.add("handleId", conceptHelper.getIdHandleOfConcept(idConcept, idTheso));
                jobLine.add("notation", conceptHelper.getNotationOfConcept(idConcept, idTheso));
                jobLine.add("prefLabel", conceptHelper.getLexicalValueOfConcept(idConcept, idTheso, lang));
                jsonArrayBuilderLine.add(jobLine.build());
            }
        } else {
            List<NodeTermTraduction> termTraductions;
            for (String idConcept : idConcepts) {
                JsonObjectBuilder jobLine = Json.createObjectBuilder();
                jobLine.add("conceptId", idConcept);
                jobLine.add("arkId", conceptHelper.getIdArkOfConcept(idConcept, idTheso));
                jobLine.add("handleId", conceptHelper.getIdHandleOfConcept(idConcept, idTheso));                
                jobLine.add("notation", conceptHelper.getNotationOfConcept(idConcept, idTheso));

                termTraductions = termRepository.findAllTraductionsOfConcept(idConcept, idTheso);

                // traductions 
                JsonArrayBuilder jsonArrayBuilderTrad = Json.createArrayBuilder();

                for (NodeTermTraduction termTraduction : termTraductions) {
                    JsonObjectBuilder jobLang = Json.createObjectBuilder();
                    jobLang.add("lang", termTraduction.getLang());
                    jobLang.add("value", termTraduction.getLexicalValue());
                    jsonArrayBuilderTrad.add(jobLang.build());
                }
                jobLine.add("traduction", jsonArrayBuilderTrad.build());
                jsonArrayBuilderLine.add(jobLine.build());

            }
        }
        if (jsonArrayBuilderLine != null) {
            datasJson = jsonArrayBuilderLine.build().toString();
        }

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    /**
     * Fonction qui permet de récupérer un thésaurus entier
     *
     * @param idTheso
     * @param format
     * @return skos
     */
    public String getTheso(String idTheso, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = null;
        try {
            writeRdf4j = getTheso2__(idTheso);
        } catch (Exception ex) {
            Logger.getLogger(RestRDFHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la racine (vers le haut)
     *
     * @param idTheso
     * @return skos
     */
    private WriteRdf4j getTheso__(String idTheso) {

        if (idTheso == null) {
            return null;
        }
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(idTheso);
        allConcepts.forEach(idConcept -> {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        });
        return new WriteRdf4j(skosXmlDocument);
    }
    
    /**
     * Fonction qui permet de récupérer une branche complète en partant d'un
     * concept et en allant jusqu'à la racine (vers le haut)
     *
     * @param idTheso
     * @return skos
     */
    private WriteRdf4j getTheso2__ (String idTheso) throws Exception{
        if (idTheso == null) return null;
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) return null;

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(idTheso, nodePreference));
        
        String baseUrl = "https" + "://" + nodePreference.getCheminSite();
        
        var groups = exportRdf4jHelperNew.exportCollectionsV2(idTheso);
        for (SKOSResource group : groups) {
            skosXmlDocument.addGroup(group);
        }
        
        List<SKOSResource> concepts = exportHelper.getAllConcepts(idTheso,
                    baseUrl, null, nodePreference.getOriginalUri(), nodePreference, false);

        List<SKOSResource> facettes = exportHelper.getAllFacettes(idTheso, baseUrl,
                nodePreference.getOriginalUri(), nodePreference);
        for (SKOSResource facette : facettes) {
            skosXmlDocument.addFacet(facette);
        }

        for (SKOSResource concept : concepts) {
            skosXmlDocument.addconcept(concept);
        }

        var facetList = exportRdf4jHelperNew.exportFacettesV2(idTheso);
        for (SKOSResource facette : facetList) {
            skosXmlDocument.addFacet(facette);
        }
        return new WriteRdf4j(skosXmlDocument);
    }    
  

    /**
     * Permet de retourner un group au format défini en passant par un
     * identifiant Ark utilisé pour la négociation de contenu
     *
     * @param idTheso
     * @param idGroup
     * @param format
     * @return
     */
    public String exportGroup(String idTheso, String idGroup, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = getGroupFromId(idTheso, idGroup);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    /**
     * Permet de retourner un group au format défini en passant par un
     * identifiant Ark utilisé pour la négociation de contenu
     *
     * @param idArk
     * @param format
     * @return
     */
    public String exportGroup(
            String idArk, String format) {

        RDFFormat rDFFormat = getRDFFormat(format);
        WriteRdf4j writeRdf4j = getGroupFromArk(idArk);
        if (writeRdf4j == null) {
            return null;
        }

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, rDFFormat);
        return out.toString();
    }

    private WriteRdf4j getGroupFromArk(String idArk) {

        String idTheso = groupHelper.getIdThesaurusFromArkId(idArk);        
        String idGroup = groupHelper.getIdGroupFromArkId(idArk, idTheso);


        if (idGroup == null || idTheso == null) {
            return null;
        }

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addGroup(exportRdf4jHelperNew.addSingleGroupV2(idTheso, idGroup));
        return new WriteRdf4j(skosXmlDocument);
    }

    private WriteRdf4j getGroupFromId(
            String idTheso, String idGroup) {
        if (idGroup == null || idTheso == null) {
            return null;
        }

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        exportRdf4jHelperNew.setInfos(nodePreference);
        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.addGroup(exportRdf4jHelperNew.addSingleGroupV2(idTheso, idGroup));
        return new WriteRdf4j(skosXmlDocument);
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * ou Handle si renseignés, sinon l'URL du Site
     *
     * @return
     */
    private String getUri(NodePreference nodePreference,
            NodeAutoCompletion nodeAutoCompletion1, String idTheso) {
        String uri = "";
        if (nodeAutoCompletion1 == null) {
            return uri;
        }
        if (nodeAutoCompletion1.getIdConcept() == null) {
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if (nodeAutoCompletion1.getIdArk() != null) {
            if (!nodeAutoCompletion1.getIdArk().trim().isEmpty()) {
                uri = nodePreference.getOriginalUri() + "/" + nodeAutoCompletion1.getIdArk();
                return uri;
            }
        }
        // URI de type Handle
        if (nodeAutoCompletion1.getIdHandle() != null) {
            if (!nodeAutoCompletion1.getIdHandle().trim().isEmpty()) {
                uri = "https://hdl.handle.net/" + nodeAutoCompletion1.getIdHandle();
                return uri;
            }
        }
        // si on ne trouve pas ni Handle, ni Ark
        // http://localhost:8083/opentheso/?idc=66&idt=1
        uri = nodePreference.getCheminSite() + "?idc=" + nodeAutoCompletion1.getIdConcept()
                + "&idt=" + idTheso;
        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept suivant le type
     * d'identifiant précisé dans les préférences
     *
     * @param nodePreference
     * @param idConcept
     * @return
     */
    public String getUri(NodePreference nodePreference, String idConcept, String idTheso) {
        String uri;

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        String identifier;
        if (nodePreference.isOriginalUriIsArk()) {
            identifier = conceptHelper.getIdArkOfConcept(idConcept, idTheso);
            if (identifier != null && !identifier.isEmpty()) {
                uri = nodePreference.getUriArk() + identifier;
                return uri;
            }
        }

        if (nodePreference.isOriginalUriIsHandle()) {
            // URI de type Handle
            identifier = conceptHelper.getIdHandleOfConcept(idConcept, idTheso);
            if (identifier != null && !identifier.isEmpty()) {
                uri = "https://hdl.handle.net/" + identifier;
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        if (nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idc=" + idConcept
                    + "&idt=" + idTheso;
            return uri;
        }
        return idConcept;
    }

    private String messageEmptyRdfXml()
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "</rdf:RDF>";
    }
}
