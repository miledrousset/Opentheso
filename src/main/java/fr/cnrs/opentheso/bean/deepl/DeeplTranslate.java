package fr.cnrs.opentheso.bean.deepl;

import com.deepl.api.Language;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.repositories.DeeplHelper;
import fr.cnrs.opentheso.repositories.LanguageHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.models.notes.NodeNote;

import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;


@Data
@Named(value = "deeplTranslate")
@SessionScoped
public class DeeplTranslate implements Serializable {

    
    @Autowired @Lazy private RoleOnThesoBean roleOnThesoBean;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private SelectedTheso selectedTheso;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private DeeplHelper deeplHelper;
    @Autowired
    private ConceptView conceptView;

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
    @Autowired
    private LanguageHelper languageHelper;

    public void init() {
        String keyApi = roleOnThesoBean.getNodePreference().getDeepl_api_key();
        sourceLangs = deeplHelper.getSourceLanguages(keyApi);
        targetLangs = deeplHelper.getTargetLanguages(keyApi);
        existingTranslatedText = null;
        sourceTranslatedText = null;
        translatingText = null;
        textToTranslate = null;
    }

    public void initNoteToTranslate(String identifier, String nodeNoteType) {
        if(nodeNoteType == null) {
            init();
            PrimeFaces.current().executeScript("PF('deeplTranslate').hide();");
            return;
        }
        NodeNote nodeNote1= noteHelper.getNodeNote(
                identifier,
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang(),
                nodeNoteType);
        if(nodeNote1 == null) {
            textToTranslate = null;
            PrimeFaces.current().ajax().update("containerIndex:idDeeplTranslate containerIndex:deeplTranslateForm");
            PrimeFaces.current().executeScript("PF('deeplTranslate').hide();");
            return;
        }
        textToTranslate = nodeNote1.getLexicalValue();
        this.nodeNote = nodeNote1;
        retrieveExistingTranslatedText();
        this.fromLang = nodeNote1.getLang();
        fromLangLabel = getLanguage(fromLang);
    }

    public void initNoteToTranslateByLang(NodeNote nodeNote1, String nodeNoteType) {
        if(nodeNoteType == null) {
            init();
            PrimeFaces.current().executeScript("PF('deeplTranslate').hide();");
            return;
        }
        if(nodeNote1 == null) {
            textToTranslate = null;
            PrimeFaces.current().ajax().update("containerIndex:idDeeplTranslate containerIndex:deeplTranslateForm");
            PrimeFaces.current().executeScript("PF('deeplTranslate').hide();");
            return;
        }
        textToTranslate = nodeNote1.getLexicalValue();
        this.nodeNote = nodeNote1;

        this.fromLang = nodeNote1.getLang();
        fromLangLabel = getLanguage(fromLang);
        if(fromLang.equalsIgnoreCase(languageHelper.normalizeIdLang(toLang))) {
            toLang = "fr";
        }
        retrieveExistingTranslatedText();
    }

    private String getLanguage(String idLang) {
        for (Language sourceLang : sourceLangs) {
            if (idLang.equalsIgnoreCase(sourceLang.getCode())) {
                return sourceLang.getName();
            }
        }
        return idLang;
    }

    public void translate() {

        String keyApi = roleOnThesoBean.getNodePreference().getDeepl_api_key();
        translatingText = deeplHelper.translate(keyApi, textToTranslate, fromLang, toLang);
    }

    public void retrieveExistingTranslatedText() {
        existingTranslatedText = noteHelper.getLabelOfNote(
               // conceptView.getNodeConcept().getConcept().getIdConcept(),
                nodeNote.getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                toLang, nodeNote.getNoteTypeCode());
        sourceTranslatedText = noteHelper.getSourceOfNote(
                //conceptView.getNodeConcept().getConcept().getIdConcept(),
                nodeNote.getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                toLang, nodeNote.getNoteTypeCode());
    }

    public void saveTranslatedText() {
        LocalDate currentDate = LocalDate.now();
        String source = "traduit par Deepl le " + currentDate;
        if(this.nodeNote == null) return;
        if (!noteHelper.addNote(nodeNote.getIdConcept(), toLang,
                selectedTheso.getCurrentIdTheso(),
                translatingText, nodeNote.getNoteTypeCode(),
                source,//nodeNote.getNoteSource(),

                currentUser.getNodeUser().getIdUser())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'opération a échouée");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        retrieveExistingTranslatedText();
        updateConcept();
        translatingText = "";
    }

    private void updateConcept() {
        conceptView.getConceptForTree(selectedTheso.getCurrentIdTheso(),
                nodeNote.getIdConcept(),
                nodeNote.getLang(),
                currentUser);
    }

    public void saveExistingTranslatedText() {
        if(this.nodeNote == null) return;
        if (!noteHelper.addNote(nodeNote.getIdConcept(), toLang,
                selectedTheso.getCurrentIdTheso(),
                existingTranslatedText, nodeNote.getNoteTypeCode(),
                nodeNote.getNoteSource(), currentUser.getNodeUser().getIdUser())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'opération a échouée");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        updateConcept();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Note mis à jour avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
