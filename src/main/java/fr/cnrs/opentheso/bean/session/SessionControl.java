package fr.cnrs.opentheso.bean.session;

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

    private final int DEFAULT_TIMEOUT_IN_MIN = 10;


    public void isTimeout() throws IOException {
        if (currentUser.getNodeUser() != null) {
            //Déconnexion de l'utilisateur
            currentUser.disconnect();

            //Vider le cache
            clearComponent();
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            PrimeFaces.current().executeScript("PF('treeWidget').clearCache();");

            // Rafraîchissement de la page
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
        }
    }

    public void clearComponent() {
        UIViewRoot root = FacesContext.getCurrentInstance().getViewRoot();

        // for JSF 2 getFacetsAndChildren instead of only JSF 1 getChildren
        Iterator<UIComponent> children = root.getFacetsAndChildren();
        clearAllComponentInChilds(children);
    }

    private void clearAllComponentInChilds(Iterator<UIComponent> childrenIt) {

        while(childrenIt.hasNext()) {
            UIComponent component = childrenIt.next();
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


    public int getTimeout() {
        int minNbr;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            minNbr = Integer.parseInt(bundlePref.getString("timeout_nbr_minute"));
        } catch(Exception e) {
            minNbr = DEFAULT_TIMEOUT_IN_MIN;
        }
        return (minNbr * 60 * 1000);
    }
    
}
