package fr.cnrs.opentheso.bean.session;

import fr.cnrs.opentheso.bean.concept.CopyAndPasteBetweenThesaurus;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewconcepts.TreeConcepts;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewliste.ListIndex;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesaurusBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesaurusHomeBean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlSelectBooleanCheckbox;
import jakarta.faces.component.html.HtmlSelectManyCheckbox;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;


@Getter
@Setter
@SessionScoped
@AllArgsConstructor
@Named(value = "sessionControl")
public class SessionControl implements Serializable {

    private final Tree tree;
    private final ListIndex listIndex;
    private final TreeGroups treeGroups;
    private final CurrentUser currentUser;
    private final TreeConcepts treeConcepts;
    private final IndexSetting indexSetting;
    private final SelectedTheso selectedTheso;
    private final RoleOnThesaurusBean roleOnThesaurusBean;
    private final ViewEditorThesaurusHomeBean viewEditorThesaurusHomeBean;
    private final CopyAndPasteBetweenThesaurus copyAndPasteBetweenThesaurus;

    
    public void isTimeout() throws IOException {

        if(FacesContext.getCurrentInstance()== null) return;

        if (currentUser.getNodeUser() != null) {
            currentUser.disconnect();
        } else {
            tree.reset();
            listIndex.reset();
            treeGroups.reset();
            treeConcepts.reset();
            viewEditorThesaurusHomeBean.reset();
            roleOnThesaurusBean.showListThesaurus(currentUser, selectedTheso.getCurrentIdTheso());
            copyAndPasteBetweenThesaurus.reset();
            indexSetting.setIsThesoActive(true);
            roleOnThesaurusBean.setAndClearThesoInAuthorizedList(selectedTheso);
            PrimeFaces.current().ajax().update("containerIndex");
        }

        //Vider le cache
        clearComponent();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        externalContext.invalidateSession();
        PrimeFaces.current().executeScript("PF('treeWidget').clearCache();");
        PrimeFaces.current().executeScript("PF('groupWidget').clearCache();");
        PrimeFaces.current().executeScript("PF('conceptTreeWidget').clearCache();");
    }

    public void clearComponent() {
        UIViewRoot root = FacesContext.getCurrentInstance().getViewRoot();

        // for JSF 2 getFacetsAndChildren instead of only JSF 1 getChildren
        Iterator<UIComponent> children = root.getFacetsAndChildren();
        clearAllComponentInChilds(children);
    }

    private void clearAllComponentInChilds(Iterator<UIComponent> childrenIt) {

        while (childrenIt.hasNext()) {
            UIComponent component = childrenIt.next();
            if (component != null) {
                if (component instanceof HtmlInputText com) {
                    com.resetValue();
                }
                if (component instanceof HtmlSelectOneMenu com) {
                    com.resetValue();
                }
                if (component instanceof HtmlSelectBooleanCheckbox com) {
                    com.resetValue();
                }
                if (component instanceof HtmlSelectManyCheckbox com) {
                    com.resetValue();
                }

                clearAllComponentInChilds(component.getFacetsAndChildren());
            }
        }
    }

}
