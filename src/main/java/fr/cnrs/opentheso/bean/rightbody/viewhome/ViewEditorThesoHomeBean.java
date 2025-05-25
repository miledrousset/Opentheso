package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.entites.ThesaurusHomePage;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.ConceptStatusRepository;
import fr.cnrs.opentheso.repositories.ThesaurusDcTermRepository;
import fr.cnrs.opentheso.repositories.ThesaurusHomePageRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.UserService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "viewEditorThesoHomeBean")
public class ViewEditorThesoHomeBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final UserService userService;
    private final ConceptHelper conceptHelper;
    private final UserGroupLabelRepository userGroupLabelRepository;
    private final ThesaurusDcTermRepository thesaurusDcTermRepository;
    private final ThesaurusHomePageRepository thesaurusHomePageRepository;
    private final ConceptRepository conceptRepository;
    private final ConceptStatusRepository conceptStatusRepository;
    private final ConceptService conceptService;

    private boolean isViewPlainText, isInEditing;
    private String text, colorOfHtmlButton, colorOfTextButton;


    public void reset(){
        isInEditing = false;
        isViewPlainText = false;
        text = null;
    }
    
    public void initText(String idLanguage, String idThesaurus) {

        text = getHtmlPage(idLanguage, idThesaurus);
        isInEditing = true;
        isViewPlainText = false;
        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";        
    }

    public String getThesoHomePage(String idLanguage, String idThesaurus){

        var homePage = getHtmlPage(idLanguage, idThesaurus);
        PrimeFaces.current().ajax().update("containerIndex:meta:metadataTheso");
        PrimeFaces.current().ajax().update("containerIndex:thesoHomeData");
        return homePage;
    }
    
    public List<DcElement> meta(String idThesaurus){

        var dcElements = thesaurusDcTermRepository.findAllByIdThesaurus(idThesaurus);
        return CollectionUtils.isNotEmpty(dcElements)
                ? dcElements.stream().map(element -> DcElement.builder()
                    .id(element.getId().intValue())
                    .name(element.getName())
                    .value(element.getValue())
                    .language(element.getLanguage())
                    .type(element.getDataType())
                    .build()).toList()
                : new ArrayList<>();
    }
    /**
     * permet d'ajouter un copyright, s'il n'existe pas, on le créé,sinon, on applique une mise à jour 
     */
    public void updateThesoHomePage(String idLanguage, String idThesaurus) {

        if(StringUtils.isEmpty(idLanguage)) {
            idLanguage = workLanguage;
        }

        var thesaurusHomePage = thesaurusHomePageRepository.findByIdThesoAndLang(idThesaurus, idLanguage);
        ThesaurusHomePage thesaurusToSave;
        if (thesaurusHomePage.isEmpty()) {
            thesaurusToSave = ThesaurusHomePage.builder()
                    .idTheso(idThesaurus)
                    .lang(idLanguage)
                    .htmlCode(text)
                    .build();
        } else {
            thesaurusHomePage.get().setHtmlCode(text);
            thesaurusToSave = thesaurusHomePage.get();
        }

        var result = thesaurusHomePageRepository.save(thesaurusToSave);

        if (ObjectUtils.isEmpty(result)){
            var msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "L'ajout a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;               
        }

        var msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Texte ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        isInEditing = false;
        isViewPlainText = false;
    }

    private String getHtmlPage(String idLanguage, String idThesaurus) {
        idLanguage = StringUtils.isEmpty(idLanguage) ? workLanguage : idLanguage;
        var thesaurusHomePage = thesaurusHomePageRepository.findByIdThesoAndLang(idThesaurus, idLanguage);
        return thesaurusHomePage.isPresent() ? thesaurusHomePage.get().getHtmlCode() : "";
    }

    public String getTotalConceptOfTheso(String idThesaurus){
        return String.valueOf(conceptStatusRepository.countValidConceptsByThesaurus(idThesaurus));
    }

    public String getLastModifiedDate(String idThesaurus){
        var date = userService.getLastModification(idThesaurus);
        return ObjectUtils.isEmpty(date) ? "" : date.toString();
    }

    public String getProjectName(String idThesaurus){
        var idProject = userService.getGroupOfThisThesaurus(idThesaurus);
        return (idProject != -1) ? userGroupLabelRepository.findById(idProject).get().getLabel() : "";
    }

    public List<NodeIdValue> getLastModifiedConcepts(String idThesaurus, String idLanguage){
        return conceptHelper.getLastModifiedConcept(idThesaurus, idLanguage);
    }        
    
    public void setViewPlainTextTo(boolean status){
        if(status) {
            colorOfHtmlButton = "#8C8C8C;";
            colorOfTextButton = "#F49F66;";
        } else {
            colorOfHtmlButton = "#F49F66;";
            colorOfTextButton = "#8C8C8C;";            
        } 
        isViewPlainText = status;
    }
    
}
