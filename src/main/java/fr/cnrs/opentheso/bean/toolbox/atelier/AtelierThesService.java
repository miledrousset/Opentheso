package fr.cnrs.opentheso.bean.toolbox.atelier;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.event.FileUploadEvent;


@Named
@ViewScoped
public class AtelierThesService implements Serializable {
    
    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private LanguageBean languageBean;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private SearchHelper searchHelper;
    
    
    public ArrayList<ConceptResultNode> comparer(List<List<String>> datas, int position, NodeIdValue thesoSelected) {

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), thesoSelected.getId());
        
        ArrayList<ConceptResultNode> list = new ArrayList<>();
        int limit = 5;
        for (List<String> data : datas) {
            if(data.get(position) == null || data.get(position).isEmpty()) continue;
            ArrayList<NodeSearchMini> temp = searchHelper.searchFullText(connect.getPoolConnexion(),
                    data.get(position), languageBean.getIdLangue(), thesoSelected.getId(), limit);
            
            if (!CollectionUtils.isEmpty(temp)) {
                temp.forEach(nodeSearchMini -> {
                    NodeConcept concept = conceptHelper.getConcept(connect.getPoolConnexion(), nodeSearchMini.getIdConcept(), 
                            thesoSelected.getId(), languageBean.getIdLangue(), -1, -1);

                    ConceptResultNode conceptResultNode = new ConceptResultNode();
                    if(data.size() == 1) 
                        conceptResultNode.setIdOrigine("");
                    else
                        conceptResultNode.setIdOrigine(data.get(0));
                    conceptResultNode.setPrefLabelOrigine(data.get(position));
                    conceptResultNode.setIdConcept(concept.getConcept().getIdConcept());
                    conceptResultNode.setPrefLabelConcept(concept.getTerm().getLexicalValue());

                    if(concept.getNodeEM() == null || concept.getNodeEM().isEmpty())
                        conceptResultNode.setAltLabelConcept("");
                    else {
                        for (NodeEM nodeEM : concept.getNodeEM()) {
                            if(conceptResultNode.getAltLabelConcept() == null)
                                conceptResultNode.setAltLabelConcept(nodeEM.getLexicalValue());
                            else 
                                conceptResultNode.setAltLabelConcept(conceptResultNode.getAltLabelConcept() + " ## " + nodeEM.getLexicalValue());
                        }                        
                    }
                    for (NodeBT nodeBT : concept.getNodeBT()) {
                        if(conceptResultNode.getTermGenerique() == null)
                            conceptResultNode.setTermGenerique(nodeBT.getTitle());
                        else 
                            conceptResultNode.setTermGenerique(conceptResultNode.getTermGenerique() + " ## " + nodeBT.getTitle());
                    }                    
                    
                    conceptResultNode.setDefinition(getDefinition(concept.getNodeNotesTerm()));
                    if(concept.getConcept().getIdArk()!= null && !concept.getConcept().getIdArk().isEmpty()) {
                        conceptResultNode.setUriArk(nodePreference.getUriArk() + concept.getConcept().getIdArk());
                    } else
                        conceptResultNode.setUriArk("");
                    list.add(conceptResultNode);

                });
            } else {
                ConceptResultNode conceptResultNode = new ConceptResultNode();
                conceptResultNode.setIdOrigine(data.get(0));
                conceptResultNode.setPrefLabelOrigine(data.get(position));
                list.add(conceptResultNode);    
            }
            
        }
        
        return list;
    }
    
    private String getDefinition(List<NodeNote> notes) {
        String definition = "";
        for (NodeNote note : notes) {
            if ("definition".equals(note.getNoteTypeCode())) {
                definition = note.getLexicalValue();
            }
        }
        return definition;
    }
    
    public List<List<String>> loadCsvFile(FileUploadEvent event, char delimiterCsv) {
        List<List<String>> values = new ArrayList<>();
        try {
            String line;
            Reader reader = new InputStreamReader(event.getFile().getInputStream());
            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                List<String> list = Arrays.asList(line.split(""+delimiterCsv));
                values.add(list);
            }
            br.close();
            reader.close();
        } catch (IOException e) {}
        return values;
    }
    
    public ArrayList<NodeIdValue> searchAllThesaurus() {

        if(currentUser.getNodeUser() == null)
            return new ArrayList<>();

        List<String> authorizedTheso;
        if (currentUser.getNodeUser().isSuperAdmin()) {
            authorizedTheso = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), true);
        } else {
            authorizedTheso = userHelper.getThesaurusOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
        }
        
        if (authorizedTheso == null) {
            return new ArrayList<>();
        }
        
        ArrayList<NodeIdValue> nodeListTheso = new ArrayList<>();

        String preferredIdLangOfTheso;
        for (String idTheso1 : authorizedTheso) {
            
            preferredIdLangOfTheso = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
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
