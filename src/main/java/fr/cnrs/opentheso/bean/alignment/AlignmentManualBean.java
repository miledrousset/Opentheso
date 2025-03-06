package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentType;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "alignmentManualBean")
@SessionScoped
public class AlignmentManualBean implements Serializable {

    @Autowired @Lazy private AlignmentBean alignmentBean;
    @Autowired @Lazy private ConceptView conceptView;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private CandidatBean candidatBean;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private AlignmentHelper alignmentHelper;
    
    private List<NodeAlignmentType> nodeAlignmentTypes;
    
    // pour l'aligenement manuel
    private String manualAlignmentSource;
    private String manualAlignmentUri;
    private int manualAlignmentType;
    private List<NodeAlignment> nodeAlignments;

    @PreDestroy
    public void destroy(){
        clear();
       // nodeAlignmentTypes = alignmentHelper.getAlignmentsType();
    }  
    public void clear(){
        if(nodeAlignmentTypes!= null){
            nodeAlignmentTypes.clear();
            nodeAlignmentTypes = null;
        }        
        manualAlignmentSource = null;
        manualAlignmentUri = null;
    }
    
    public AlignmentManualBean() {
    }

    public void reset() {
        nodeAlignmentTypes = alignmentHelper.getAlignmentsType();
        manualAlignmentSource = "";
        manualAlignmentUri = "";
        manualAlignmentType = -1;

        nodeAlignments = alignmentHelper.getAllAlignmentOfConcept(conceptView.getNodeFullConcept().getIdentifier(),selectedTheso.getCurrentIdTheso());
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    public void deleteAlignment(NodeAlignment nodeAlignment) {
        
        if(nodeAlignment == null) return;

        if(!alignmentHelper.deleteAlignment(nodeAlignment.getId_alignement(), selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
        reset();
    }

    public void updateAlignement(){
        if(alignmentBean.getAlignementElementSelected() == null) return;

        FacesMessage msg;

        if(!alignmentHelper.updateAlignment(
                alignmentBean.getAlignementElementSelected().getIdAlignment(),
                alignmentBean.getAlignementElementSelected().getConceptTarget(),
                alignmentBean.getAlignementElementSelected().getThesaurus_target(),
                alignmentBean.getAlignementElementSelected().getTargetUri(),
                alignmentBean.getAlignementElementSelected().getAlignement_id_type(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }
        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        alignmentBean.getIdsAndValues(selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateAlignement(NodeAlignment nodeAlignment){

        if(nodeAlignment == null) return;

        if(!alignmentHelper.updateAlignment(
                nodeAlignment.getId_alignement(),
                nodeAlignment.getConcept_target(),
                nodeAlignment.getThesaurus_target(),
                nodeAlignment.getUri_target(),
                nodeAlignment.getAlignement_id_type(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(),
                currentUser);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateAlignementFromConceptInterface(){
        if(!alignmentHelper.updateAlignment(
                alignmentBean.getAlignementElementSelected().getIdAlignment(),
                alignmentBean.getAlignementElementSelected().getConceptTarget(),
                alignmentBean.getAlignementElementSelected().getThesaurus_target(),
                alignmentBean.getAlignementElementSelected().getTargetUri(),
                alignmentBean.getAlignementElementSelected().getAlignement_id_type(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        alignmentBean.getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
    
    public void addManualAlignement(String idConcept, boolean isFromConceptView){

    /*    if(manualAlignmentSource == null || manualAlignmentSource.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }*/
        if(manualAlignmentUri == null || manualAlignmentUri.isEmpty()){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Veuillez saisir une valeur  !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        } 

        if(!fr.cnrs.opentheso.utils.StringUtils.urlValidator(manualAlignmentUri)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL n'est pas valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        if(!alignmentHelper.addNewAlignment(currentUser.getNodeUser().getIdUser(), "", manualAlignmentSource,
                manualAlignmentUri, manualAlignmentType, idConcept, selectedTheso.getCurrentIdTheso(), 0)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), idConcept, currentUser.getNodeUser().getIdUser());

        if (isFromConceptView) {
            conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                    conceptView.getSelectedLang(), currentUser);
        } else {
            candidatBean.getCandidatSelected().setAlignments(alignmentHelper.getAllAlignmentOfConcept(
                    idConcept, selectedTheso.getCurrentIdTheso()));
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        alignmentBean.getIdsAndValues(selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }    
    
    /**
     * 
     * @param idTheso
     * @param idConcept
     * @param idUser 
     */
    private void updateDateOfConcept(String idTheso, String idConcept, int idUser) {

        conceptHelper.updateDateOfConcept(
                idTheso,
                idConcept, idUser);      
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConcept, idTheso);
        ///////////////        
    }
    

    public List<NodeAlignmentType> getNodeAlignmentTypes() {
        return nodeAlignmentTypes;
    }

    public void setNodeAlignmentTypes(List<NodeAlignmentType> nodeAlignmentTypes) {
        this.nodeAlignmentTypes = nodeAlignmentTypes;
    }

    public String getManualAlignmentSource() {
        return manualAlignmentSource;
    }

    public void setManualAlignmentSource(String manualAlignmentSource) {
        this.manualAlignmentSource = manualAlignmentSource;
    }

    public String getManualAlignmentUri() {
        return manualAlignmentUri;
    }

    public void setManualAlignmentUri(String manualAlignmentUri) {
        this.manualAlignmentUri = manualAlignmentUri;
    }

    public int getManualAlignmentType() {
        return manualAlignmentType;
    }

    public void setManualAlignmentType(int manualAlignmentType) {
        this.manualAlignmentType = manualAlignmentType;
    }

    public List<NodeAlignment> getNodeAlignments() {
        return nodeAlignments;
    }

    public void setNodeAlignments(List<NodeAlignment> nodeAlignments) {
        this.nodeAlignments = nodeAlignments;
    }
}
