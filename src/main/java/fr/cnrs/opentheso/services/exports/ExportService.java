package fr.cnrs.opentheso.services.exports;
import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.SkosConceptProjection;
import fr.cnrs.opentheso.models.SkosFacetProjection;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.repositories.ExportRepository;

import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.FacetService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private static final String SEPARATOR = "##";
    private static final String SUB_SEPARATOR = "@@";

    private final ExportRepository exportRepository;
    private final FacetService facetService;
    private final ConceptService conceptService;


    public List<SKOSResource> getAllFacettes(String idThesaurus, String baseUrl, String originalUri, Preferences nodePreference) throws Exception {

        var projections = exportRepository.getAllFacettes(idThesaurus, baseUrl);
        List<SKOSResource> result = new ArrayList<>();

        for (SkosFacetProjection p : projections) {
            SKOSResource resource = new SKOSResource(getUriForFacette(p.getId_facet(), idThesaurus, originalUri), SKOSProperty.FACET);
            resource.setIdentifier(p.getId_facet());
            resource.addRelation(p.getId_facet(), p.getUri_value(), SKOSProperty.SUPER_ORDINATE);

            List<String> members = facetService.getAllMembersOfFacet(p.getId_facet(), idThesaurus);
            for (String idConcept : members) {
                NodeUri nodeUri = conceptService.getNodeUriOfConcept(idConcept, idThesaurus);
                resource.addRelation(nodeUri.getIdConcept(), buildUri(nodeUri, idThesaurus, idConcept, originalUri, nodePreference), SKOSProperty.MEMBER);
            }

            resource.addLabel(p.getLexicalvalue(), p.getLang(), SKOSProperty.PREF_LABEL);
            resource.addDate(p.getCreated(), SKOSProperty.CREATED);
            resource.addDate(p.getModified(), SKOSProperty.MODIFIED);

            addDoc(p.getDefinition(), resource, SKOSProperty.DEFINITION);
            addDoc(p.getNote(), resource, SKOSProperty.NOTE);
            addDoc(p.getEditorialnote(), resource, SKOSProperty.EDITORIAL_NOTE);
            addDoc(p.getSecopenote(), resource, SKOSProperty.SCOPE_NOTE);
            addDoc(p.getHistorynote(), resource, SKOSProperty.HISTORY_NOTE);
            addDoc(p.getExample(), resource, SKOSProperty.EXAMPLE);
            addDoc(p.getChangenote(), resource, SKOSProperty.CHANGE_NOTE);

            result.add(resource);
        }
        return result;
    }

    public List<SKOSResource> getAllConcepts(String idThesaurus, String baseUrl, String idGroup, String originalUri,
                                             Preferences nodePreference, boolean filterHtmlCharacter) {

        List<SkosConceptProjection> projections = StringUtils.isEmpty(idGroup)
                ? exportRepository.getAllConcepts(idThesaurus, baseUrl)
                : exportRepository.getAllConceptsByGroup(idThesaurus, baseUrl, idGroup);

        List<SKOSResource> result = new ArrayList<>();

        for (SkosConceptProjection p : projections) {
            SKOSResource resource = new SKOSResource();
            resource.setProperty(SKOSProperty.CONCEPT);
            resource.setUri(p.getUri());
            resource.setLocalUri(p.getLocal_uri());
            resource.addIdentifier(p.getIdentifier(), SKOSProperty.IDENTIFIER);

            if (StringUtils.isNotEmpty(p.getArk_id())) resource.setArkId(p.getArk_id());

            if ("ca".equalsIgnoreCase(p.getType())) resource.setStatus(SKOSProperty.CANDIDATE);
            else if ("dep".equalsIgnoreCase(p.getType())) resource.setStatus(SKOSProperty.DEPRECATED);
            else resource.setStatus(SKOSProperty.CONCEPT);

            addLabels(p.getPrefLab(), resource, SKOSProperty.PREF_LABEL);
            addLabels(p.getAltLab_hiden(), resource, SKOSProperty.HIDDEN_LABEL);
            addLabels(p.getAltLab(), resource, SKOSProperty.ALT_LABEL);

            if (StringUtils.isEmpty(p.getBroader())) {
                resource.addRelation(idThesaurus, getUriThesoFromId(idThesaurus, originalUri, nodePreference), SKOSProperty.TOP_CONCEPT_OF);
            }

            addDoc(p.getDefinition(), resource, SKOSProperty.DEFINITION);
            addDoc(p.getNote(), resource, SKOSProperty.NOTE);
            addDoc(p.getEditorialnote(), resource, SKOSProperty.EDITORIAL_NOTE);
            addDoc(p.getSecopenote(), resource, SKOSProperty.SCOPE_NOTE);
            addDoc(p.getHistorynote(), resource, SKOSProperty.HISTORY_NOTE);
            addDoc(p.getExample(), resource, SKOSProperty.EXAMPLE);
            addDoc(p.getChangenote(), resource, SKOSProperty.CHANGE_NOTE);

            resource.addRelation(idThesaurus, getUriThesoFromId(idThesaurus, originalUri, nodePreference), SKOSProperty.INSCHEME);

            if (StringUtils.isNotEmpty(p.getNotation())) resource.addNotation(p.getNotation());

            // addGps, addAlignement, addMembres, addFacets, addExternalResources etc. peuvent être appelés ici

            result.add(resource);
        }
        return result;
    }

    private void addLabels(String labelsRaw, SKOSResource resource, int type) {
        if (StringUtils.isNotEmpty(labelsRaw)) {
            for (String tab : labelsRaw.split(SEPARATOR)) {
                String[] parts = tab.split(SUB_SEPARATOR);
                if (parts.length == 2) {
                    resource.addLabel(parts[0], parts[1], type);
                }
            }
        }
    }

    private String getUriForFacette(String idFacet, String idTheso, String originalUri) {
        return originalUri + "/?idf=" + idFacet + "&idt=" + idTheso;
    }

    private void addDoc(String content, SKOSResource resource, int type) {
        if (StringUtils.isNotEmpty(content)) {
            String[] tabs = content.split(SEPARATOR);
            for (String tab : tabs) {
                String[] parts = tab.split(SUB_SEPARATOR);
                if (parts.length == 2) {
                    String cleanText = Jsoup.parse(parts[0]).text();
                    resource.addDocumentation(cleanText, parts[1], type);
                }
            }
        }
    }

    private String buildUri(NodeUri nodeUri, String idTheso, String idConcept, String originalUri, Preferences pref) {
        if (pref.isOriginalUriIsArk() && StringUtils.isNotEmpty(nodeUri.getIdArk())) {
            return originalUri + '/' + nodeUri.getIdArk();
        } else if (pref.isOriginalUriIsHandle() && StringUtils.isNotEmpty(nodeUri.getIdHandle())) {
            return "https://hdl.handle.net/" + nodeUri.getIdHandle();
        } else if (pref.isOriginalUriIsDoi() && StringUtils.isNotEmpty(nodeUri.getIdDoi())) {
            return "https://doi.org/" + nodeUri.getIdDoi();
        } else {
            return originalUri + "/?idc=" + idConcept + "&idt=" + idTheso;
        }
    }

    private String getUriThesoFromId(String id, String originalUri, Preferences pref) {
        if (pref.isOriginalUriIsArk()) return pref.getOriginalUri() + "/" + pref.getIdNaan() + "/" + id;
        if (pref.isOriginalUriIsHandle()) return "https://hdl.handle.net/" + id;
        if (StringUtils.isNotEmpty(originalUri)) return originalUri + "/?idt=" + id;
        return originalUri + "/?idt=" + id;
    }
}