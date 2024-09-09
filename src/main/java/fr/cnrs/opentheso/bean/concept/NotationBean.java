package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;



@Data
@SessionScoped
@Named(value = "notationBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NotationBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private Tree tree;
    @Autowired @Lazy private SelectedTheso selectedTheso;

    @Autowired
    private ConceptHelper conceptHelper;
    
    private String notation;

    public void reset() {
        notation = conceptBean.getNodeConcept().getConcept().getNotation();
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter ou modifier la Notation
     */
    public void updateNotation(String idTheso) {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        if(!notation.isEmpty()) {
            if(conceptHelper.isNotationExist(connect.getPoolConnexion(),
                    idTheso,
                    notation)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La notation existe déjà dans le thésaurus !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }            
                return;
            }
        }
        
        if(!conceptHelper.updateNotation(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                idTheso,
                notation.trim())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }            
            return;
        } 

        conceptBean.getConcept(idTheso,
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La notation a bien été modifiée");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        // mettre à jour le label dans l'arbre
        if(selectedTheso.isSortByNotation()) {
            if (tree.getSelectedNode() != null) {
                // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
                if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                        conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                    tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idTheso, conceptBean.getSelectedLang());
                }
                ((TreeNodeData) tree.getSelectedNode().getData()).setNotation(notation);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
                }
            }  
        }        
        
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
        reset();
    }
    
}
