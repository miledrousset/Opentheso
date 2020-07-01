package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.condidat.CandidatBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

@Named(value = "definitionBean")
@SessionScoped
public class DefinitionBean implements Serializable {

    @Inject
    private CandidatBean candidatBean;

    private String definition;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
    public void init() {
        this.definition = "";
    }
    
    public void init(String definition) {
        this.definition = definition;
    }
    
    public void addDefinition() {
        
        if (StringUtils.isEmpty(definition)) {
            return;
        }
        
        for (String str : candidatBean.getCandidatSelected().getDefenitions()) {
            if (str.equals(definition)) {
                definition = "";
                return;
            }
        }
        candidatBean.getCandidatSelected().getDefenitions().add(definition);
        definition = "";
    }
    
    public void deleteDefinition() {
        
        if (StringUtils.isEmpty(definition)) {
            return;
        }
        
        for (String str : candidatBean.getCandidatSelected().getDefenitions()) {
            if (str.equals(definition)) {
                candidatBean.getCandidatSelected().getDefenitions().remove(str);
                definition = "";
                return;
            }
        }
        definition = "";
    }

}
