package fr.cnrs.opentheso.bean.language;

import fr.cnrs.opentheso.models.candidats.LanguageEnum;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;


@Data
@Component("langueBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LanguageBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private String idLangue, currentBundle;


    private ResourceBundle getBundleLangue(String l) {

        var context = FacesContext.getCurrentInstance();
        return context.getApplication().getResourceBundle(context, l);
    }

    @PostConstruct
    public void InitLanguageBean() {
        currentBundle = "langue_" + workLanguage;
        idLangue = workLanguage.toUpperCase();
    }

    public void changeLangue(String l) {

        currentBundle = "langue_" + l;
        idLangue = l.toUpperCase();

        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(l));

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", 
                LanguageEnum.valueOf(l.toUpperCase()).getLanguage() + " !"));

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
        PrimeFaces.current().ajax().update("menuBar");
        PrimeFaces.current().ajax().update("tabViewCandidat");

        PrimeFaces.current().executeScript("window.location.reload();");
    }

    public String getIdLangue() {
        return idLangue.toLowerCase();
    }

    public String getMsg(String msg) {
        return getBundleLangue(currentBundle).getString(msg);
    }

}
