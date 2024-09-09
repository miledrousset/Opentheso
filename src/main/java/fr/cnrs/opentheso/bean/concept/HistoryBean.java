package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.HistoryHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;


@Data
@Named(value = "historyBean")
@SessionScoped
public class HistoryBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;

    @Autowired
    private HistoryHelper historyHelper;

    private ArrayList<HistoryHelper.HistoryValue> historyLabels;
    private ArrayList<HistoryHelper.HistoryValue> historySynonyms;
    private ArrayList<HistoryHelper.HistoryValue> historyRelations;      
    private ArrayList<HistoryHelper.HistoryValue> historyNotes;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(historyLabels != null){
            historyLabels.clear();
            historyLabels = null;
        }
        if(historySynonyms != null){
            historySynonyms.clear();
            historySynonyms = null;
        }
        if(historyRelations != null){
            historyRelations.clear();
            historyRelations = null;
        }
        if(historyNotes != null){
            historyNotes.clear();
            historyNotes = null;
        }        
    }

    public void reset() {
        historyLabels = historyHelper.getLabelHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());
        historySynonyms = historyHelper.getSynonymHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());
        historyRelations = historyHelper.getRelationsHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());        
        historyNotes = historyHelper.getNotesHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());             
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour images !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
