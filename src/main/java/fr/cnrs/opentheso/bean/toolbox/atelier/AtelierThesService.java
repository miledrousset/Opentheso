package fr.cnrs.opentheso.bean.toolbox.atelier;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;


@Named
@ViewScoped
public class AtelierThesService implements Serializable {
    

    @Inject
    private Connect connect;

    @Inject
    private LanguageBean languageBean;
    
    @Inject
    private CurrentUser currentUser;
    
    
    public ArrayList<ConceptResultNode> comparer(List<Data> datas, NodeIdValue thesoSelected) {
        
        ArrayList<ConceptResultNode> list = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        
        for (Data data : datas) {
            ArrayList<NodeSearchMini> temp = new SearchHelper().searchFullText(connect.getPoolConnexion(), 
                    data.getValue(), languageBean.getIdLangue(), thesoSelected.getId());
            
            //atelierThesBean.setProgressStep(atelierThesBean.getProgressStep() + atelierThesBean.getProgressValue());
            
            temp.forEach(nodeSearchMini -> {
                NodeConcept concept = conceptHelper.getConcept(connect.getPoolConnexion(), nodeSearchMini.getIdConcept(), 
                        thesoSelected.getId(), languageBean.getIdLangue());
                
                ConceptResultNode conceptResultNode = new ConceptResultNode();
                conceptResultNode.setIdOrigine(data.getId());
                conceptResultNode.setPrefLabelOrigine(data.getValue());
                conceptResultNode.setIdConcept(concept.getConcept().getIdConcept());
                conceptResultNode.setPrefLabelConcept(concept.getTerm().getLexical_value());
                //conceptResultNode.setAltLabelConcept(concept.get);
                conceptResultNode.setDefinition(getDefinition(concept.getNodeNotesTerm()));
                conceptResultNode.setUriArk(concept.getConcept().getIdArk());
                list.add(conceptResultNode);
                
            });
        }
        
        return list;
    }
    
    private String getDefinition(ArrayList<NodeNote> notes) {
        String definition = "";
        for (NodeNote note : notes) {
            if ("definition".equals(note.getNotetypecode())) {
                definition = note.getLexicalvalue();
            }
        }
        return definition;
    }
    
    public List<Data> loadCsvFile(FileUploadEvent event, String delimiterCsv) {
        List<Data> values = new ArrayList<>();
        try {
            String line;
            Reader reader = new InputStreamReader(event.getFile().getInputStream());
            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                String[] datas = line.split(delimiterCsv);
                Data data = new Data();
                data.setId(datas[0]);
                data.setValue(datas[1]);
                values.add(data);
            }
            reader.close();
        } catch (IOException e) {

        }
        return values;
    }
    
    public ArrayList<NodeIdValue> searchAllThesaurus() {
        
        List<String> authorizedTheso;
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            authorizedTheso = new ThesaurusHelper().getAllIdOfThesaurus(connect.getPoolConnexion(), true);
        } else {
            authorizedTheso = new UserHelper().getThesaurusOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
        }
        
        if (authorizedTheso == null) {
            return new ArrayList<>();
        }
        
        ArrayList<NodeIdValue> nodeListTheso = new ArrayList<>();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();

        String preferredIdLangOfTheso;
        for (String idTheso1 : authorizedTheso) {
            
            preferredIdLangOfTheso = new PreferencesHelper()
                    .getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
            if (preferredIdLangOfTheso == null) {
                preferredIdLangOfTheso = connect.getWorkLanguage().toLowerCase();
            }

            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), 
                    idTheso1, preferredIdLangOfTheso));
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso1));
            nodeListTheso.add(nodeIdValue);
            
        }
        
        return nodeListTheso;
    }
    
}
