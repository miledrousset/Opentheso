package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.services.ConceptAddService;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.ThesaurusService;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


@Data
@SessionScoped
@Named(value = "moveConcept")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MoveConcept implements Serializable {

    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private CurrentUser currentUser;
    @Autowired @Lazy
    private CandidatBean candidatBean;

    @Autowired
    private ThesaurusService thesaurusService;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private PreferenceService preferenceService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private ConceptAddService conceptAddService;

    private String idThesoFrom, idThesoTo;
    
    private String idConceptFrom;
    
    private NodeSearchMini nodeSearchSelected;
    
    private List<String> idConceptsToMove;


    public void initForCandidate(List idConcepts1, String idThesoFrom) {
        idConceptsToMove = new ArrayList<>();
        this.idConceptsToMove = idConcepts1;
        this.idThesoFrom = idThesoFrom;
    }
    
    public void initForConcept(String idConcept, String idThesoFrom) {
        idConceptsToMove = new ArrayList<>();        
        this.idConceptFrom = idConcept;
        idConceptsToMove = conceptHelper.getIdsOfBranch(
                idConcept, selectedTheso.getCurrentIdTheso());
        this.idThesoFrom = idThesoFrom;
        idThesoTo = null;
    }

    /**
     * permet de déplacer des candidats d'un thésaurus vers un autre
     */
    public void moveConceptCA() {
        FacesMessage msg;
        if (StringUtils.isEmpty(idThesoTo) || StringUtils.isEmpty(idThesoFrom) || idConceptsToMove == null || idConceptsToMove.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        for (String idConcept : idConceptsToMove) {
            if(!conceptHelper.moveConceptToAnotherTheso(idConcept, idThesoFrom, idThesoTo)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le déplacement a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            conceptService.updateDateOfConcept(idThesoTo, idConcept, currentUser.getNodeUser().getIdUser());

            conceptDcTermRepository.save(ConceptDcTerm.builder()
                    .name(DCMIResource.CONTRIBUTOR)
                    .value(currentUser.getNodeUser().getName())
                    .idConcept(idConcept)
                    .idThesaurus(idThesoTo)
                    .build());
        }
        PrimeFaces pf = PrimeFaces.current();
        candidatBean.initCandidatModule();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("tabViewCandidat:panelCandidateList");
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le déplacement a réussi");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de déplacer des concepts d'un thésaurus vers un autre
     */
    public void moveConcept() {
        FacesMessage msg;
        if (StringUtils.isEmpty(idThesoTo) || StringUtils.isEmpty(idConceptFrom) || StringUtils.isEmpty(idThesoFrom) || idConceptsToMove == null || idConceptsToMove.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        List<String> lisIdGroup;
        var nodePreference = preferenceService.getThesaurusPreferences(idThesoTo);
        for (String idConcept : idConceptsToMove) {
            if(!conceptHelper.moveConceptToAnotherTheso(idConcept, idThesoFrom, idThesoTo)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le déplacement a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            conceptService.updateDateOfConcept(idThesoTo, idConcept, currentUser.getNodeUser().getIdUser());

            conceptDcTermRepository.save(ConceptDcTerm.builder()
                    .name(DCMIResource.CONTRIBUTOR)
                    .value(currentUser.getNodeUser().getName())
                    .idConcept(idConcept)
                    .idThesaurus(idThesoTo)
                    .build());


            lisIdGroup = groupService.getListIdGroupOfConcept(idThesoTo, idConcept);
            for (String idGroup : lisIdGroup) {
                groupService.deleteRelationConceptGroupConcept(idGroup, idConcept, idThesoTo);
            }

            var concept = conceptService.getConcept(idConcept);
            if (concept != null) {
                if(!StringUtils.isEmpty(concept.getIdArk())) {
                    conceptAddService.generateArkId(idThesoTo, List.of(idConcept), selectedTheso.getCurrentLang(), nodePreference);
                }
            }
        }

        // suppression des BT du concept de tête à déplacer
        ArrayList<String> listBt = relationsHelper.getListIdBT(idConceptFrom, idThesoTo);
        for (String idBt : listBt) {
            relationsHelper.deleteRelationBT(idConceptFrom, idThesoTo, idBt, currentUser.getNodeUser().getIdUser());
        }

        if(nodeSearchSelected != null && !StringUtils.isEmpty(nodeSearchSelected.getIdConcept())) {
            //cas où le déplacement est vers un concept, on attache ce nouveau concept au concept cible
            relationsHelper.addRelationBT(idConceptFrom, idThesoTo, nodeSearchSelected.getIdConcept(), currentUser.getNodeUser().getIdUser());
            conceptHelper.setNotTopConcept(idConceptFrom, idThesoTo);
        } else {
            conceptHelper.setTopConcept(idConceptFrom, idThesoTo);
        }

        try {
            selectedTheso.reloadSelectedTheso();
        } catch (IOException e) {
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le déplacement a réussi");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public List<NodeIdValue> getListThesoAsAdmin(){
        if(currentUser.getNodeUser() == null) return null;
        List<String> authorizedThesoAsAdmin;
        if(currentUser.getNodeUser().isSuperAdmin()) {
            authorizedThesoAsAdmin = thesaurusService.getAllIdOfThesaurus(true);
        } else {
            authorizedThesoAsAdmin = userHelper.getThesaurusOfUserAsAdmin(currentUser.getNodeUser().getIdUser());
        }
        List<NodeIdValue> nodeIdValues = new ArrayList<>();

        return authorizedThesoAsAdmin.stream()
                .filter(idThesaurus -> selectedTheso.getCurrentIdTheso().equalsIgnoreCase(idThesaurus))
                .map(idThesaurus -> NodeIdValue.builder()
                        .id(idThesaurus)
                        .value(thesaurusService.getTitleOfThesaurus(idThesaurus, preferenceService.getWorkLanguageOfThesaurus(idThesaurus)))
                        .build())
                .toList();
    }

    /// Cette focntion est nécessaire pour activer l'autocomplete
    public void action(){}

    public List<NodeSearchMini> searchConceptsAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (!StringUtils.isEmpty(idThesoTo)) {
            liste = searchHelper.searchAutoCompletionForRelation(value, selectedTheso.getCurrentLang(), idThesoTo, true);
        }
        return liste;
    }
    
}
