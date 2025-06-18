package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;



@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "notationBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NotationBean implements Serializable {

    private final Tree tree;
    private final ConceptView conceptBean;
    private final CurrentUser currentUser;
    private final SelectedTheso selectedTheso;
    private final ConceptService conceptService;
    
    private String notation;


    public void reset() {
        notation = conceptBean.getNodeConcept().getConcept().getNotation();
    }

    /**
     * permet d'ajouter ou modifier la Notation
     */
    public void updateNotation(String idThesaurus) {
        
        if(!notation.isEmpty()) {
            if(conceptService.isNotationExist(idThesaurus, notation)) {
                MessageUtils.showErrorMessage("La notation existe déjà dans le thésaurus !!");
                return;
            }
        }
        
        if(!conceptService.updateNotation(conceptBean.getNodeConcept().getConcept().getIdConcept(), idThesaurus, notation.trim())) {
            MessageUtils.showErrorMessage("Erreur de cohérence de BDD !!");
            return;
        } 

        conceptBean.getConcept(idThesaurus, conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        MessageUtils.showInformationMessage("La notation a bien été modifiée");
        
        // mettre à jour le label dans l'arbre
        if(selectedTheso.isSortByNotation() && tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idThesaurus, conceptBean.getSelectedLang());
            }
            ((TreeNodeData) tree.getSelectedNode().getData()).setNotation(notation);
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:tree");
        }

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        reset();
    }
    
}
