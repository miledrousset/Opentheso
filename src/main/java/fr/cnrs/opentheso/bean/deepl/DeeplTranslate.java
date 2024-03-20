package fr.cnrs.opentheso.bean.deepl;

import com.deepl.api.Language;
import fr.cnrs.opentheso.bdd.helper.DeeplHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.jena.sparql.function.library.date;



@Named(value = "deeplTranslate")
@SessionScoped
public class DeeplTranslate implements Serializable {

    @Inject private Connect connect;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private ConceptView conceptView;
    @Inject private CurrentUser currentUser;    
    
    private DeeplHelper deeplHelper;

    private String fromLang;
    private String fromLangLabel;
    private String toLang = "en-GB";
    
    
    private String textToTranslate;
    private String existingTranslatedText;  
    private String sourceTranslatedText;      
    
    private NodeNote nodeNote;
    
    private String translatingText;

    private List<Language> sourceLangs;
    private List<Language> targetLangs;
    
    public void reset() {
        
    }

    public DeeplTranslate() {
    }
    
    public void init(){
        String keyApi = roleOnThesoBean.getNodePreference().getDeepl_api_key();
        deeplHelper = new DeeplHelper(keyApi);
        sourceLangs = deeplHelper.getSourceLanguages();
        targetLangs = deeplHelper.getTargetLanguages();

        translatingText = "";
    }
    
    public void setNoteToTranlate(NodeNote nodeNote){
        textToTranslate = nodeNote.getLexicalvalue();
        this.nodeNote = nodeNote;

        retrieveExistingTranslatedText();

        toLang = "en-GB";
        this.fromLang = nodeNote.getLang();
        fromLangLabel = getLanguage(fromLang);
        
    }
    
    private String getLanguage(String idLang){
        for (Language sourceLang : sourceLangs) {
            if(idLang.equalsIgnoreCase(sourceLang.getCode()))
                return sourceLang.getName();
        }
        return idLang;
    }
    
    public void translate(){
        translatingText = deeplHelper.translate(textToTranslate, fromLang, toLang);
    }
    
    public void retrieveExistingTranslatedText(){
        NoteHelper noteHelper = new NoteHelper();
        switch (nodeNote.getNotetypecode()) {
            case "definition":
                existingTranslatedText = noteHelper.getLabelOfNote(connect.getPoolConnexion(), 
                        conceptView.getNodeConcept().getConcept().getIdConcept(),
                        conceptView.getNodeConcept().getConcept().getIdThesaurus(), toLang, nodeNote.getNotetypecode());
                sourceTranslatedText = noteHelper.getSourceOfNote(connect.getPoolConnexion(), 
                        conceptView.getNodeConcept().getConcept().getIdConcept(),
                        conceptView.getNodeConcept().getConcept().getIdThesaurus(), toLang, nodeNote.getNotetypecode());
                break;
            default:
                break;
        }        
    }
    
    public void saveTranslatedText(){
        LocalDate currentDate = LocalDate.now();
        String source = "traduit par Deepl le " + currentDate;
        
        NoteHelper noteHelper = new NoteHelper();
        if(!noteHelper.addNote(connect.getPoolConnexion(), nodeNote.getId_concept(), toLang,  conceptView.getNodeConcept().getConcept().getIdThesaurus(),
                translatingText, nodeNote.getNotetypecode(), 
                
                source,//nodeNote.getNoteSource(),
                
                currentUser.getNodeUser().getIdUser())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'opération a échouée");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
        
    }
    
    public void saveExistingTranslatedText(){
        NoteHelper noteHelper = new NoteHelper();
        if(!noteHelper.addNote(connect.getPoolConnexion(), nodeNote.getId_concept(), toLang,  conceptView.getNodeConcept().getConcept().getIdThesaurus(),
                existingTranslatedText, nodeNote.getNotetypecode(), nodeNote.getNoteSource(), currentUser.getNodeUser().getIdUser())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'opération a échouée");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Note mis à jour avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }    
    

    public String getTextToTranslate() {
        return textToTranslate;
    }

    public void setTextToTranslate(String textToTranslate) {
        this.textToTranslate = textToTranslate;
    }

    public String getFromLang() {
        return fromLang;
    }

    public void setFromLang(String fromLang) {
        this.fromLang = fromLang;
    }

    public String getToLang() {
        return toLang;
    }

    public void setToLang(String toLang) {
        this.toLang = toLang;
    }

    public List<Language> getSourceLangs() {
        return sourceLangs;
    }

    public void setSourceLangs(List<Language> sourceLangs) {
        this.sourceLangs = sourceLangs;
    }

    public List<Language> getTargetLangs() {
        return targetLangs;
    }

    public void setTargetLangs(List<Language> targetLangs) {
        this.targetLangs = targetLangs;
    }

    public String getTranslatingText() {
        return translatingText;
    }

    public void setTranslatingText(String translatingText) {
        this.translatingText = translatingText;
    }

    public String getExistingTranslatedText() {
        return existingTranslatedText;
    }

    public void setExistingTranslatedText(String existingTranslatedText) {
        this.existingTranslatedText = existingTranslatedText;
    }

    public String getFromLangLabel() {
        return fromLangLabel;
    }

    public void setFromLangLabel(String fromLangLabel) {
        this.fromLangLabel = fromLangLabel;
    }

    public String getSourceTranslatedText() {
        return sourceTranslatedText;
    }

    public void setSourceTranslatedText(String sourceTranslatedText) {
        this.sourceTranslatedText = sourceTranslatedText;
    }
    


}