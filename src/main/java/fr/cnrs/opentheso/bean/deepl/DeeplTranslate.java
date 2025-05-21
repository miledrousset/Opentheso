package fr.cnrs.opentheso.bean.deepl;

import com.deepl.api.Language;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.DeeplService;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import fr.cnrs.opentheso.services.NoteService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.primefaces.PrimeFaces;
import jakarta.inject.Named;


@Data
@Named(value = "deeplTranslate")
@SessionScoped
public class DeeplTranslate implements Serializable {

    private final NoteService noteService;
    private RoleOnThesoBean roleOnThesoBean;
    private CurrentUser currentUser;
    private SelectedTheso selectedTheso;
    private DeeplService deeplHelper;
    private ConceptView conceptView;

    private String fromLang, fromLangLabel, textToTranslate, existingTranslatedText, sourceTranslatedText, translatingText;
    private String toLang = "en-GB";
    private List<Language> sourceLangs, targetLangs;
    private NodeNote nodeNote;


    public void init() {

        var keyApi = roleOnThesoBean.getNodePreference().getDeeplApiKey();
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
        var nodeNote1= noteService.getNodeNote(identifier, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang(), nodeNoteType);

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
        if(fromLang.equalsIgnoreCase(normalizeIdLang(toLang))) {
            toLang = "fr";
        }
        retrieveExistingTranslatedText();
    }

    private String normalizeIdLang(String idLang){
        switch (idLang) {
            case "en-GB":
                return "en";
            case "en-US":
                return "en";
            case "pt-BR":
                return "pt";
            case "pt-PT":
                return "pt";
        }
        return idLang;
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

        var keyApi = roleOnThesoBean.getNodePreference().getDeeplApiKey();
        translatingText = deeplHelper.translate(keyApi, textToTranslate, fromLang, toLang);
    }

    public void retrieveExistingTranslatedText() {

        var note = noteService.getNodeNote(nodeNote.getIdConcept(), selectedTheso.getCurrentIdTheso(), toLang, nodeNote.getNoteTypeCode());
        if (note != null) {
            existingTranslatedText = note.getLexicalValue();
            sourceTranslatedText = note.getNoteSource();
        }
    }

    public void saveTranslatedText() {
        LocalDate currentDate = LocalDate.now();
        String source = "traduit par Deepl le " + currentDate;
        if(this.nodeNote == null) return;
        if (!noteService.addNote(nodeNote.getIdConcept(), toLang, selectedTheso.getCurrentIdTheso(),
                translatingText, nodeNote.getNoteTypeCode(), source,//nodeNote.getNoteSource(),

                currentUser.getNodeUser().getIdUser())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'opération a échouée");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        retrieveExistingTranslatedText();
        conceptView.getConceptForTree(selectedTheso.getCurrentIdTheso(), nodeNote.getIdConcept(), nodeNote.getLang(), currentUser);
        translatingText = "";
    }

    public void saveExistingTranslatedText() {
        if(this.nodeNote == null) return;
        if (!noteService.addNote(nodeNote.getIdConcept(), toLang,
                selectedTheso.getCurrentIdTheso(),
                existingTranslatedText, nodeNote.getNoteTypeCode(),
                nodeNote.getNoteSource(), currentUser.getNodeUser().getIdUser())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'opération a échouée");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        conceptView.getConceptForTree(selectedTheso.getCurrentIdTheso(), nodeNote.getIdConcept(), nodeNote.getLang(), currentUser);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Note mis à jour avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
