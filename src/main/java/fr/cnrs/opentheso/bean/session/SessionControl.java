package fr.cnrs.opentheso.bean.session;

import fr.cnrs.opentheso.bean.concept.CopyAndPasteBetweenTheso;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewconcepts.TreeConcepts;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewliste.ListIndex;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;


@Named(value = "sessionControl")
@SessionScoped
public class SessionControl implements Serializable {

    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private TreeGroups treeGroups;
    @Autowired @Lazy private TreeConcepts treeConcepts;
    @Autowired @Lazy private Tree tree;
    @Autowired @Lazy private ListIndex listIndex;
    @Autowired @Lazy private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Autowired @Lazy private CopyAndPasteBetweenTheso copyAndPasteBetweenTheso;
    @Autowired @Lazy private RoleOnThesoBean roleOnThesoBean;
    @Autowired @Lazy private IndexSetting indexSetting;

    @Autowired
    private SelectedTheso selectedTheso;

    
    public void isTimeout() throws IOException {

        if(FacesContext.getCurrentInstance()== null) return;

        if (currentUser.getNodeUser() != null) {
            currentUser.disconnect();
        } else {
            tree.reset();
            listIndex.reset();
            treeGroups.reset();
            treeConcepts.reset();
            viewEditorThesoHomeBean.reset();
            roleOnThesoBean.showListTheso(currentUser, selectedTheso);
            copyAndPasteBetweenTheso.reset();
            indexSetting.setIsThesoActive(true);
            roleOnThesoBean.setAndClearThesoInAuthorizedList(selectedTheso);
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
                if (component instanceof HtmlInputText) {
                    HtmlInputText com = (HtmlInputText) component;
                    com.resetValue();
                }
                if (component instanceof HtmlSelectOneMenu) {
                    HtmlSelectOneMenu com = (HtmlSelectOneMenu) component;
                    com.resetValue();
                }
                if (component instanceof HtmlSelectBooleanCheckbox) {
                    HtmlSelectBooleanCheckbox com = (HtmlSelectBooleanCheckbox) component;
                    com.resetValue();
                }
                if (component instanceof HtmlSelectManyCheckbox) {
                    HtmlSelectManyCheckbox com = (HtmlSelectManyCheckbox) component;
                    com.resetValue();
                }

                clearAllComponentInChilds(component.getFacetsAndChildren());
            }
        }
    }

}
