package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.search.NodeSearchMini;

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
    private Connect connect;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private CurrentUser currentUser;
    @Autowired @Lazy
    private CandidatBean candidatBean;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

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
        idConceptsToMove = conceptHelper.getIdsOfBranch(connect.getPoolConnexion(),
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
            if(!conceptHelper.moveConceptToAnotherTheso(connect.getPoolConnexion(), idConcept, idThesoFrom, idThesoTo)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le déplacement a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), idThesoTo, idConcept,
                    currentUser.getNodeUser().getIdUser());

            ///// insert DcTermsData to add contributor
            dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    idConcept, idThesoTo);
            ///////////////
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

        ArrayList<String> lisIdGroup;
        String idArk;
        ArrayList<String> idConcepts = new ArrayList<>();

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), idThesoTo);
        for (String idConcept : idConceptsToMove) {
            if(!conceptHelper.moveConceptToAnotherTheso(connect.getPoolConnexion(), idConcept, idThesoFrom, idThesoTo)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le déplacement a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), idThesoTo, idConcept,
                    currentUser.getNodeUser().getIdUser());

            ///// insert DcTermsData to add contributor
            dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    idConcept, idThesoTo);
            ///////////////


            lisIdGroup = groupHelper.getListIdGroupOfConcept(connect.getPoolConnexion(), idThesoTo, idConcept);
            for (String idGroup : lisIdGroup) {
                groupHelper.deleteRelationConceptGroupConcept(connect.getPoolConnexion(), idGroup, idConcept, idThesoTo, currentUser.getNodeUser().getIdUser());
            }
            idArk = conceptHelper.getIdArkOfConcept(connect.getPoolConnexion(), idConcept, idThesoTo);
            if(!StringUtils.isEmpty(idArk)) {
                idConcepts.clear();
                idConcepts.add(idConcept);
                conceptHelper.setNodePreference(nodePreference);
                conceptHelper.generateArkId(connect.getPoolConnexion(), idThesoTo, idConcepts, selectedTheso.getCurrentLang());
            }
        }

        // suppression des BT du concept de tête à déplacer
        ArrayList<String> listBt = relationsHelper.getListIdBT(connect.getPoolConnexion(), idConceptFrom, idThesoTo);
        for (String idBt : listBt) {
            relationsHelper.deleteRelationBT(connect.getPoolConnexion(), idConceptFrom, idThesoTo, idBt, currentUser.getNodeUser().getIdUser());
        }

        if(nodeSearchSelected != null && !StringUtils.isEmpty(nodeSearchSelected.getIdConcept())) {
            //cas où le déplacement est vers un concept, on attache ce nouveau concept au concept cible
            relationsHelper.addRelationBT(connect.getPoolConnexion(), idConceptFrom, idThesoTo, nodeSearchSelected.getIdConcept(), currentUser.getNodeUser().getIdUser());
            conceptHelper.setNotTopConcept(connect.getPoolConnexion(), idConceptFrom, idThesoTo);
        } else {
            conceptHelper.setTopConcept(connect.getPoolConnexion(), idConceptFrom, idThesoTo);
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
            authorizedThesoAsAdmin = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), true);
        } else {
            authorizedThesoAsAdmin = userHelper.getThesaurusOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
        }
        List<NodeIdValue> nodeIdValues = new ArrayList<>();

        authorizedThesoAsAdmin.remove(selectedTheso.getCurrentIdTheso());
        for (String idTheso : authorizedThesoAsAdmin) {
            var nodeIdValue = NodeIdValue.builder()
                    .id(idTheso)
                    .value(idTheso)
                    .build();
            String idLang = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso);
            String title = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), idTheso, idLang);
            if(StringUtils.isEmpty(title))
                nodeIdValue.setValue("");
            else
                nodeIdValue.setValue(title);
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }

    /// Cette focntion est nécessaire pour activer l'autocomplete
    public void action(){}

    public List<NodeSearchMini> searchConceptsAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (!StringUtils.isEmpty(idThesoTo)) {
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    selectedTheso.getCurrentLang(),
                    idThesoTo, true);
        }
        return liste;
    }
    
}
