package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.entites.PreferredTerm;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.NonPreferredTermRepository;
import fr.cnrs.opentheso.repositories.PreferredTermRepository;
import fr.cnrs.opentheso.repositories.TermRepository;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.NonPreferredTermService;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author miledrousset
 */
@Data
@Slf4j
@SessionScoped
@RequiredArgsConstructor
@Named(value = "synonymBean")
public class SynonymBean implements Serializable {

    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final PropositionBean propositionBean;
    private final CurrentUser currentUser;
    private final ConceptHelper conceptHelper;
    private final ConceptDcTermRepository conceptDcTermRepository;
    private final NonPreferredTermRepository nonPreferredTermRepository;
    private final NonPreferredTermService nonPreferredTermService;
    private final PreferredTermRepository preferredTermRepository;
    private final TermRepository termRepository;
    private final ThesaurusService thesaurusService;
    private final ConceptService conceptService;

    private NodeEM nodeEM;
    private List<NodeLangTheso> nodeLangs;
    private List<NodeEM> nodeEMs, nodeEMsForEdit;
    private String selectedLang, selectedValue, value;
    private boolean hidden, duplicate;


    public void reset() {
        hidden = false;
        selectedLang = conceptBean.getSelectedLang();
        nodeLangs = thesaurusService.getAllUsedLanguagesOfThesaurusNode(selectedTheso.getCurrentIdTheso(), selectedLang);

        nodeEMs = conceptBean.getNodeConcept().getNodeEM();
        
        prepareNodeEMForEdit();
        
        value = "";
        duplicate = false;
        this.nodeEM = null;
    }

    private void init() {
        value = "";
        duplicate = false;
        this.nodeEM = null;
    }
   
    public void prepareNodeEMForEdit() {

        log.info("Charger la liste des synonymes disponibles pour le concept {}", conceptBean.getNodeConcept().getConcept().getIdConcept());
        nodeEMsForEdit = new ArrayList<>();
        if (!CollectionUtils.isEmpty(conceptBean.getNodeConcept().getNodeEM())) {
            for (var nodeEM1 : conceptBean.getNodeConcept().getNodeEM()) {
                nodeEM1.setOldValue(nodeEM1.getLexicalValue());
                nodeEM1.setOldHiden(nodeEM1.isHiden());
                nodeEMsForEdit.add(nodeEM1);
            }
        }
    }

    public void infos() {
        MessageUtils.showInformationMessage("Rédiger une aide ici pour Add Concept !");
    }

    /**
     * permet d'ajouter un synonyme sans controler le nom en doublon
     *
     * @param idUser
     */
    public void addForced(int idUser) {

        log.info("Recherche de preferred term");
        var preferredTerm = preferredTermRepository.findByIdThesaurusAndIdConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept());

        var idTerm = preferredTerm.map(PreferredTerm::getIdTerm).orElse(null);
        log.info("Id du term {}", idTerm);

        log.info("Enregistrement du synonyme dans la base de données");
        nonPreferredTermService.addNonPreferredTerm(Term.builder()
                .idTerm(idTerm)
                .lexicalValue(value)
                .lang(selectedLang)
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .hidden(hidden)
                .status(hidden ? "Hidden" : "USE")
                .build(), idUser);

        refreshConceptDatas(idUser);

        MessageUtils.showInformationMessage("Synonyme ajouté avec succès");

        init();

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
    }

    /**
     * permet de modifier un synonyme
     *
     * @param nodeEMLocal
     * @param idUser
     */
    public void updateSynonym(NodeEM nodeEMLocal, int idUser) {

        log.info("Début de la modification du synonyme {}", nodeEMLocal.getLexicalValue());
        if (nodeEMLocal == null) {
            MessageUtils.showErrorMessage("Aucune sélection !");
            return;
        }

        // save de la valeur pour une modification forcée
        this.nodeEM = nodeEMLocal;

        if (!nodeEMLocal.getOldValue().equals(nodeEMLocal.getLexicalValue())) {
            log.info("Modification de la valeur de la synonyme");
            if (termRepository.existsPrefLabel(nodeEMLocal.getLexicalValue(), nodeEMLocal.getLang(), selectedTheso.getCurrentIdTheso())) {
                MessageUtils.showWarnMessage("Un label identique existe déjà !");
                duplicate = true;
                return;
            }
            if (nonPreferredTermRepository.isAltLabelExist(nodeEMLocal.getLexicalValue(), selectedTheso.getCurrentIdTheso(), nodeEMLocal.getLang())) {
                MessageUtils.showErrorMessage("Un label identique existe déjà !");
                duplicate = true;
                return;
            }
            updateSynonymForced(idUser);
        } else {
            if (nodeEMLocal.isOldHiden() != nodeEMLocal.isHiden()) {
                updateStatus(nodeEMLocal, idUser);
            }
        }

        reset();
        prepareNodeEMForEdit();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    /**
     * permet de modifier un synonyme sans controle avec doublon
     *
     * @param idUser
     */
    public void updateSynonymForced(int idUser) {

        duplicate = false;

        if (nonPreferredTermService.updateNonPreferredTerm(nodeEM.getOldValue(), nodeEM.getLexicalValue(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(), nodeEM.getLang(), selectedTheso.getCurrentIdTheso(),
                nodeEM.isHiden(), idUser)) {
            MessageUtils.showErrorMessage("La modification a échoué !");
            return;
        }

        refreshConceptDatas(idUser);

        MessageUtils.showInformationMessage("Synonyme modifié avec succès");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");;
    }

    /**
     * permet d'ajouter une nouvelle traduction au concept
     *
     * @param idUser
     */
    public void addNewSynonym(int idUser) {

        duplicate = false;

        if (checkData()) {
            addForced(idUser);
        }
    }

    private boolean checkData() {

        if (StringUtils.isEmpty(value)) {
            MessageUtils.showErrorMessage("La valeur est obligatoire !");
            return false;
        }

        if (StringUtils.isEmpty(selectedLang)) {
            MessageUtils.showErrorMessage("Pas de langue choisie !");
            return false;
        }

        if (termRepository.existsPrefLabel(value, selectedLang, selectedTheso.getCurrentIdTheso())) {
            MessageUtils.showErrorMessage("Un label identique existe déjà !");
            duplicate = true;
            return false;
        }

        if (nonPreferredTermRepository.isAltLabelExist(value, selectedTheso.getCurrentIdTheso(), selectedLang)) {
            MessageUtils.showErrorMessage("Un label identique existe déjà !");
            duplicate = true;
            return false;
        }

        return true;
    }

    /**
     * permet d'ajouter une nouvelle traduction au concept
     */
    public void addPropSynonym() {

        log.info("Ajout d'une proposition synonyme !");
        checkData();

        if (CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp())) {
            propositionBean.getProposition().setSynonymsProp(new ArrayList<>());
        }

        for (SynonymPropBean synonym : propositionBean.getProposition().getSynonymsProp()) {
            if (synonym.getLexicalValue().equalsIgnoreCase(value)
                    && synonym.getLang().equalsIgnoreCase(selectedLang)) {
                MessageUtils.showErrorMessage("Un label identique existe déjà !");
                return;
            }
        }

        SynonymPropBean synonymPropBean = new SynonymPropBean();
        synonymPropBean.setToAdd(true);
        synonymPropBean.setHiden(hidden);
        synonymPropBean.setLang(selectedLang);
        synonymPropBean.setLexicalValue(value);
        synonymPropBean.setOldValue(value);
        propositionBean.getProposition().getSynonymsProp().add(synonymPropBean);
        propositionBean.setVarianteAccepted(true);
    }

    public boolean isVarianteMenuDisable() {
        return CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp());
    }

    /**
     * permet de modifier un synonyme sans controle avec doublon
     *
     * @param nodeEM
     * @param idUser
     */
    public void updateStatus(NodeEM nodeEM, int idUser) {

        log.info("Modification du status de la synonyme {}", nodeEM.getLexicalValue());
        if (!nonPreferredTermService.updateStatusNonPreferredTerm(conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeEM.getLexicalValue(), nodeEM.getLang(), selectedTheso.getCurrentIdTheso(), nodeEM.isHiden(), idUser)) {
            MessageUtils.showErrorMessage("La modification a échoué !");
            return;
        }

        refreshConceptDatas(idUser);

        MessageUtils.showInformationMessage("Synonyme modifié avec succès");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    /**
     * permet de modifier tous les synonymes
     *
     * @param idUser
     */
    public void updateAllSynonyms(int idUser) {

        if (nodeEMsForEdit == null) {
            MessageUtils.showInformationMessage("Aucune sélection !");
            return;
        }

        log.info("Début de la modification de la liste des synonyms !");
        for (NodeEM nodeEM1 : nodeEMsForEdit) {
            updateSynonym(nodeEM1, idUser);
        }

        reset();

        prepareNodeEMForEdit();
    }


    public void updateSynonymProp(SynonymPropBean synonymPropBean) {

        if (synonymPropBean == null) {
            MessageUtils.showErrorMessage("Aucune sélection !");
            return;
        }

        if (StringUtils.isEmpty(synonymPropBean.getLexicalValue())) {
            MessageUtils.showErrorMessage("Le champs valeur est obligatoire !");
            return;
        }

        if (synonymPropBean.isToUpdate() && synonymPropBean.getLexicalValue().equals(synonymPropBean.getOldValue())) {
            synonymPropBean.setToUpdate(false);
            return;
        }

        if (!synonymPropBean.getOldValue().equals(synonymPropBean.getLexicalValue())) {
            if (termRepository.existsPrefLabel(synonymPropBean.getLexicalValue(), synonymPropBean.getLang(), selectedTheso.getCurrentIdTheso())) {
                MessageUtils.showErrorMessage("Un label identique existe déjà !");
                return;
            }

            if (nonPreferredTermRepository.isAltLabelExist(synonymPropBean.getLexicalValue(), selectedTheso.getCurrentIdTheso(), synonymPropBean.getLang())) {
                MessageUtils.showErrorMessage("Un label identique existe déjà !");
                return;
            }

            if (synonymPropBean.isToRemove()) {
                synonymPropBean.setLexicalValue(synonymPropBean.getOldValue());
            } else if (!synonymPropBean.isToAdd()) {
                synonymPropBean.setToUpdate(true);
            }
        } else {
            if (synonymPropBean.isOldHiden() != synonymPropBean.isHiden()) {
                if (synonymPropBean.isToRemove()) {
                    synonymPropBean.setLexicalValue(synonymPropBean.getOldValue());
                } else if (!synonymPropBean.isToAdd()) {
                    synonymPropBean.setToUpdate(true);
                }
            }
        }

        propositionBean.checkSynonymPropositionStatus();
    }


    /**
     * permet de supprimer un synonyme
     */
    public void deleteSynonym(NodeEM nodeEM, int idUser) {

        if (nodeEM == null) {
            MessageUtils.showErrorMessage("Aucune sélection !");
            return;
        }

        nonPreferredTermService.deleteNonPreferredTerm(conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeEM.getLang(), nodeEM.getLexicalValue(), selectedTheso.getCurrentIdTheso(), idUser);

        refreshConceptDatas(idUser);

        MessageUtils.showInformationMessage("Synonyme supprimé avec succès");

        reset();

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("conceptForm:listSynonimesToDelete");
    }

    public void deleteSynonymPropo(SynonymPropBean spb) {

        if (spb == null) {
            MessageUtils.showErrorMessage("Aucune sélection !");
            return;
        }

        if (!CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp())) {
            for (SynonymPropBean synonym : propositionBean.getProposition().getSynonymsProp()) {
                if (spb.getLexicalValue().equals(synonym.getLexicalValue())) {
                    if (synonym.isToAdd()) {
                        propositionBean.getProposition().getSynonymsProp().remove(synonym);
                    } else if (synonym.isToUpdate()) {
                        synonym.setToRemove(true);
                        synonym.setToUpdate(false);
                        synonym.setLexicalValue(synonym.getOldValue());
                    } else {
                        synonym.setToRemove(!synonym.isToRemove());
                    }
                }
            }
        } else {
            log.error("Aucune proposition d'un synonyme n'est présent !");
        }

        propositionBean.checkSynonymPropositionStatus();
    }

    public void cancel() {
        duplicate = false;
        this.nodeEM = null;
    }

    private void refreshConceptDatas(int idUser) {

        log.info("Recherche des données du concept {}", conceptBean.getNodeConcept().getConcept().getIdConcept());
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        log.info("Mise à jour de la date de modification du concept {}", conceptBean.getNodeConcept().getConcept().getIdConcept());
        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());
    }
}
