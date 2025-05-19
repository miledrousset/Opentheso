package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.utils.ToolsHelper;
import fr.cnrs.opentheso.ws.ark.ArkHelper2;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.ArrayList;


@Slf4j
@Service
@AllArgsConstructor
public class ArkService {

    private final PreferenceService preferenceService;
    private final ConceptRepository conceptRepository;
    private final ConceptService conceptService;
    private final TermService termService;
    private final UserRepository userRepository;

    /**
     * Cette fonction regenerer tous les idArk des concepts fournis en paramètre
     * cette action se fait en une seule fois, ne prends en charge que les
     * métadonnées obligatoires traitement rapide
     *
     * @param idTheso
     * @param idConcepts
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> generateArkIdFast(String idTheso, ArrayList<String> idConcepts, String idLang) {

        var nodePreference = preferenceService.getThesaurusPreferences(idTheso);
        if (nodePreference != null && nodePreference.isUseArkLocal()) {
            generateArkIdLocal(idTheso, idConcepts);
            return null;
        }

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur de connexion !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }

        if (nodePreference == null) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur: Veuillez paramétrer les préférences pour ce thésaurus !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }
        if (!nodePreference.isUseArk()) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur: Veuillez activer Ark dans les préférences !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }

        JsonArrayBuilder jsonArrayBuilderMetas = Json.createArrayBuilder();

        JsonObjectBuilder joDatas = Json.createObjectBuilder();
        if (arkHelper2.getToken() == null) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setValue("Erreur: token non fourni");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }

        joDatas.add("token", arkHelper2.getToken());

        for (String idConcept : idConcepts) {
            var concept = conceptService.getConcept(idConcept, idTheso);
            if (concept == null) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(idConcept);
                nodeIdValue.setValue("Erreur: ce concept n'existe pas");
                nodeIdValues.add(nodeIdValue);
                continue;
            }
            JsonObjectBuilder jo = Json.createObjectBuilder();
            jo.add("idConcept", concept.getIdConcept());
            jo.add("ark", concept.getIdArk());

            jo.add("naan", nodePreference.getIdNaan());
            jo.add("type", nodePreference.getPrefixArk());
            jo.add("urlTarget", nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idTheso);
            jo.add("title", termService.getLexicalValueOfConcept(idConcept, idTheso, idLang));

            var creator = userRepository.findById(concept.getCreator());
            jo.add("creator", creator.isPresent() ? creator.get().getUsername() : "");

            jsonArrayBuilderMetas.add(jo.build());
        }
        joDatas.add("arks", jsonArrayBuilderMetas.build());

        String jsonResult = arkHelper2.addBatchArk(joDatas.build().toString());

        JsonArray jsonArray;
        JsonObject jsonObject;
        String idConcept = null;
        String idArk;
        try {
            JsonReader reader = Json.createReader(new StringReader(jsonResult));
            jsonArray = reader.readArray();
            System.out.println("/////////////////// traitement des mises à jour dans Opentheso /////////////////////");
            for (int i = 0; i < jsonArray.size(); ++i) {
                jsonObject = jsonArray.getJsonObject(i);
                try {
                    idConcept = jsonObject.getString("idConcept");
                    idArk = jsonObject.getString("idArk");
                    if (StringUtils.isEmpty(idConcept) || StringUtils.isEmpty(idArk)) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(idConcept);
                        nodeIdValue.setValue("Error: id Ark ou Concept vide : " + idArk);
                        nodeIdValues.add(nodeIdValue);
                    } else {
                        if (StringUtils.contains(idArk, "Error:")) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(idConcept);
                            nodeIdValue.setValue(idArk);
                            nodeIdValues.add(nodeIdValue);
                        } else {
                            if (!updateArkIdOfConcept(idConcept, idTheso, idArk)) {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(idConcept);
                                nodeIdValue.setValue("Error: erreur de mise à jour de Ark dans Opentheso : " + idArk);
                                nodeIdValues.add(nodeIdValue);
                            } else {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(idConcept);
                                nodeIdValue.setValue(idArk);
                                nodeIdValues.add(nodeIdValue);
                            }
                        }
                    }
                } catch (Exception e) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue(e.toString());
                    nodeIdValues.add(nodeIdValue);
                }
            }
        } catch (Exception e) {
        }
        return nodeIdValues;
    }

    public boolean updateArkIdOfConcept(String idConcept, String idThesaurus, String idArk) {

        log.info("Mise à jour de l'id ark (nouvelle valeur {}) du concept id {}", idArk, idConcept);
        var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (concept.isEmpty()) {
            log.info("Aucun concept n'est trouvé avec l'id {} dans le thésaurs id {}", idConcept, idThesaurus);
            return true;
        }

        concept.get().setIdArk(idArk);
        conceptRepository.save(concept.get());
        log.info("Mise à jou de l'id Ark dans le concept id {} est terminée", idConcept);
        return false;
    }

    public boolean updateUriArk(String idThesaurus, ArrayList<String> idConcepts) {

        log.info("Regénération des ids Ark des concepts");
        var preference = preferenceService.getThesaurusPreferences(idThesaurus);
        if (preference == null || !preference.isUseArkLocal()) {
            return false;
        }

        var arkHelper2 = new ArkHelper2(preference);
        if (!arkHelper2.login()) {
            log.error("Erreur de connexion avec le serveur Ark !");;
            return false;
        }

        for (String idConcept : idConcepts) {
            if (idConcept == null || idConcept.isEmpty()) {
                continue;
            }
            // Mise à jour de l'URI
            var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
            if (concept.isEmpty() || StringUtils.isEmpty(concept.get().getIdArk())) {
                continue;
            }

            var privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
            if (!arkHelper2.updateUriArk(concept.get().getIdArk(), privateUri)) {
                log.error("Erreur pendant la mise à jour dans le serveur Ark : " + arkHelper2.getMessage() + "  idConcept = " + idConcept);
                return false;
            }
        }
        return true;
    }

    public boolean generateArkIdLocal(String idThesaurus, ArrayList<String> idConcepts) {

        log.info("Générer les idArk en local");
        var preference = preferenceService.getThesaurusPreferences(idThesaurus);
        if (preference == null || !preference.isUseArkLocal()) {
            return true;
        }

        for (String idConcept : idConcepts) {
            var concept = conceptService.getConcept(idConcept, idThesaurus);
            var idArk = concept.getIdArk();
            if (StringUtils.isEmpty(idArk)) {
                idArk = ToolsHelper.getNewId(preference.getSizeIdArkLocal(), preference.isUppercaseForArk(), true);
                idArk = preference.getNaanArkLocal() + "/" + preference.getPrefixArkLocal() + idArk;
            }
            if (updateArkIdOfConcept(idConcept, idThesaurus, idArk)) {
                return true;
            }
        }
        return false;
    }
}
