package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.ImagesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "imageBean")
@SessionScoped
public class ImageBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;

    private String uri;
    private String copyright;
    
    private ArrayList<NodeImage> nodeImages;
    private ArrayList<NodeImage> nodeImagesForEdit;    

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeImages != null){
            nodeImages.clear();
            nodeImages = null;
        }
        if(nodeImagesForEdit != null){
            nodeImagesForEdit.clear();
            nodeImagesForEdit = null;
        }
        uri = null;
        copyright = null;
    }      
    
    public ImageBean() {
    }

    public void reset() {
        nodeImages = conceptBean.getNodeConcept().getNodeimages();
        uri = null;
        copyright = null;
        
        prepareImageForEdit();
    }
   
    public void prepareImageForEdit(){
        nodeImagesForEdit = new ArrayList<>();
        for (NodeImage nodeImage : nodeImages) {
            nodeImage.setOldUri(nodeImage.getUri());
            nodeImagesForEdit.add(nodeImage);
        }
    }    
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour images !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet d'ajouter un 
     * @param idUser 
     */
    public void addNewImage(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(uri == null || uri.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ImagesHelper imagesHelper = new ImagesHelper();
        if(!imagesHelper.addExternalImage(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getTerm().getLexical_value(),
                copyright,
                uri,
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant l'ajout de l'image !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void updateImage(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ImagesHelper imagesHelper = new ImagesHelper();
        if(!imagesHelper.updateExternalImage(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeImage.getOldUri(),
                nodeImage.getUri(),
                nodeImage.getCopyRight(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la modification de l'URI de l'image !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image_URI modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }       
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void deleteImage(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ImagesHelper imagesHelper = new ImagesHelper();
        if(!imagesHelper.deleteExternalImage(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeImage.getUri())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la suppression de l'image !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }    

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ArrayList<NodeImage> getNodeImages() {
        return nodeImages;
    }

    public void setNodeImages(ArrayList<NodeImage> nodeImages) {
        this.nodeImages = nodeImages;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public ArrayList<NodeImage> getNodeImagesForEdit() {
        return nodeImagesForEdit;
    }

    public void setNodeImagesForEdit(ArrayList<NodeImage> nodeImagesForEdit) {
        this.nodeImagesForEdit = nodeImagesForEdit;
    }



}
