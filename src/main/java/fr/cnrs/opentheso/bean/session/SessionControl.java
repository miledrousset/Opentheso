package fr.cnrs.opentheso.bean.session;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import org.primefaces.PrimeFaces;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ResourceBundle;


@Named(value = "sessionControl")
@SessionScoped
public class SessionControl implements Serializable {

    @Inject
    private CurrentUser currentUser;
    @Inject
    private SelectedTheso selectedTheso;

    private final int DEFAULT_TIMEOUT_IN_MIN = 10;
    
    public void isTimeout() throws IOException {
        if(FacesContext.getCurrentInstance()== null) return;
        if (currentUser.getNodeUser() != null) {
            //Déconnexion de l'utilisateur
            currentUser.disconnect();
        }

        //Vider le cache
        clearComponent();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        externalContext.invalidateSession();
        PrimeFaces.current().executeScript("PF('treeWidget').clearCache();");
        PrimeFaces.current().executeScript("PF('groupWidget').clearCache();");
        PrimeFaces.current().executeScript("PF('conceptTreeWidget').clearCache();");
    /*    System.gc ();
        System.runFinalization ();
      */  
        // Rafraîchissement de la page
        externalContext.redirect(((HttpServletRequest) externalContext.getRequest()).getRequestURI());     
    //    selectedTheso.getCurrentIdTheso();
        System.gc();
        System.runFinalization ();
  /*      if (currentUser.getNodeUser() != null) {
            //Déconnexion de l'utilisateur
            currentUser.disconnect();
        }

        //Vider le cache
        clearComponent();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();

        HttpServletResponse response = (HttpServletResponse) ectx.getResponse();

        HttpSession session = (HttpSession) ectx.getSession(false);

        session.invalidate();

        PrimeFaces.current().executeScript("PF('treeWidget').clearCache();");
        PrimeFaces.current().executeScript("PF('groupWidget').clearCache();");
        PrimeFaces.current().executeScript("PF('conceptTreeWidget').clearCache();");
        /*    System.gc ();
        System.runFinalization ();
         */
 /*       selectedTheso.getCurrentIdTheso();
        System.gc();
        // Rafraîchissement de la page
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());*/
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

    public int getTimeout() {
        int minNbr;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            minNbr = Integer.parseInt(bundlePref.getString("timeout_nbr_minute"));
        } catch (Exception e) {
            minNbr = DEFAULT_TIMEOUT_IN_MIN;
        }
        return (minNbr * 60 * 1000);
    }

}
