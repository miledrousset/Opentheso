package fr.cnrs.opentheso.bean.rightbody;

import jakarta.inject.Named;
import java.io.Serializable;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;


@Data
@Named(value = "rightBodySetting")
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RightBodySetting implements Serializable {

    private boolean showConcept;
    private boolean showGroup;
    private boolean showResultSearch;
    private boolean showThesoHome;
    private boolean showhome;
    
    private String index; // pour initialiser la vue du tab


    public void init(){
        showConcept = false;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = false;
        index = "0";
    }
    
    public void setShowGroupToOn() {
        showConcept = false;
        showGroup = true;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = false;        
    }

    public void setShowConceptToOn() {
        showConcept = true;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = false;        
    }
    public void setShowThesoHomeToOn() {
        showConcept = false;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = true;
        showhome = false;        
    }
    public void setShowHomeToOn() {
        showConcept = false;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = true;        
    }
    
    public void setShowResultSearchToOn() {
        showConcept = false;
        showGroup = false;
        showResultSearch = true;    
        showThesoHome = false;
        showhome = false;        
    }
    
}
