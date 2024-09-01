package fr.cnrs.opentheso.models.imports.tabulate;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.ConceptGroupLabel;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTerm;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.models.exports.tabulate.Alignment;
import fr.cnrs.opentheso.models.exports.tabulate.Label;
import fr.cnrs.opentheso.models.exports.tabulate.Note;
import fr.cnrs.opentheso.models.exports.tabulate.TabulateDocument;

public class ImportTabuleIntoBDD {


    public ImportTabuleIntoBDD() {
    }

    /**
     * cette fonction permet de charger dans la BDD toutes les données du thésaurus
     * contenues dans la classe TabulateDocument
     * 
     * @param ds
     * @param idThesaurus
     * @param tabulateDocuments
     * @param idUser
     * @return 
     */
    public boolean insertDatas(HikariDataSource ds,
            String idThesaurus,
            ArrayList<TabulateDocument> tabulateDocuments,
            int idUser) {
        
        for (TabulateDocument tabulateDocument : tabulateDocuments) {
            if(tabulateDocument.getType().equalsIgnoreCase("MT")){
                if(!insertGroup(ds, tabulateDocument, idThesaurus, idUser)){
                    return false;
                }
            }
            if(tabulateDocument.getType().equalsIgnoreCase("TT")){
                if(!insertConcept(ds, tabulateDocument, idThesaurus, true, idUser)){
                    return false;
                }
            }
            if(tabulateDocument.getType().equalsIgnoreCase("DE")){
                if(!insertConcept(ds, tabulateDocument, idThesaurus, false, idUser)){
                    return false;
                }
            }
        }
        addLangsToThesaurus(ds, idThesaurus);
        return true;
    }
    
    private boolean insertGroup(HikariDataSource ds,
            TabulateDocument tabulateDocument,
            String idThesaurus, int idUser) {
        
        GroupHelper conceptGroupHelper = new GroupHelper();
        
        if(!conceptGroupHelper.insertGroup(ds, 
                tabulateDocument.getId(),
                idThesaurus,
                "",
                tabulateDocument.getType(),
                "", 
                "", false, tabulateDocument.getCreated(), tabulateDocument.getModified(), idUser)){
            return false;
        }

        // ajouter les traductions des Groupes
        ConceptGroupLabel conceptGroupLabel = new ConceptGroupLabel();
        for (Label prefLabel : tabulateDocument.getPrefLabels()) {
            conceptGroupLabel.setIdgroup(tabulateDocument.getId());
            conceptGroupLabel.setIdthesaurus(idThesaurus);
            conceptGroupLabel.setLang(prefLabel.getLang());
            conceptGroupLabel.setLexicalvalue(prefLabel.getLabel());
            conceptGroupHelper.addGroupTraduction(ds, conceptGroupLabel, idUser);
        }
        return true;
    }
    
    private boolean insertConcept(
            HikariDataSource ds,
            TabulateDocument tabulateDocument,
            String idThesaurus,
            boolean istopConcept,
            int idUser) {

        // ajout du concept dans la base
        Concept concept = new Concept();
        ConceptHelper conceptHelper = new ConceptHelper();

        concept.setIdConcept(tabulateDocument.getId());

        concept.setIdThesaurus(idThesaurus);
        concept.setNotation("");
        concept.setStatus("");
        concept.setTopConcept(istopConcept);
        concept.setCreated(tabulateDocument.getCreated());
        concept.setModified(tabulateDocument.getModified());

        // cas de plusieurs Domaines (MT)
        if(!tabulateDocument.getInScheme().isEmpty()){
            for (String idGroup : tabulateDocument.getInScheme()) {
                concept.setIdGroup(idGroup);
                conceptHelper.insertConceptInTable(ds,
                        concept,
                        idUser);
            }
        }
        else {
            return false;
        }

        //ajout des termes et traductions
        NodeTerm nodeTerm = new NodeTerm();
        ArrayList <NodeTermTraduction> nodeTermTraductionList = new ArrayList<>();
        
        for (Label prefLabel : tabulateDocument.getPrefLabels()) {
            NodeTermTraduction nodeTermTraduction = new NodeTermTraduction();
            nodeTermTraduction.setLexicalValue(prefLabel.getLabel());
            nodeTermTraduction.setLang(prefLabel.getLang());
            nodeTermTraductionList.add(nodeTermTraduction);
        }
        nodeTerm.setNodeTermTraduction(nodeTermTraductionList);
        nodeTerm.setIdTerm(tabulateDocument.getId());
        nodeTerm.setIdConcept(tabulateDocument.getId());
        nodeTerm.setIdThesaurus(idThesaurus);
        nodeTerm.setSource("");
        nodeTerm.setStatus("");
        nodeTerm.setCreated(tabulateDocument.getCreated());
        nodeTerm.setModified(tabulateDocument.getModified());
        TermHelper termHelper = new TermHelper();
        termHelper.insertTerm(ds, nodeTerm, idUser);

        
        //Enregister les synonymes et traductions (AltLabel)
        ArrayList <NodeEM> nodeEMList = new ArrayList<>();
        
        for (Label altLabel : tabulateDocument.getAltLabels()) {
            NodeEM nodeEM = new NodeEM();
            nodeEM.setLexical_value(altLabel.getLabel());
            nodeEM.setLang(altLabel.getLang());    
            nodeEM.setSource("");
            nodeEM.setStatus("USE");
            nodeEM.setHiden(false);
            nodeEMList.add(nodeEM);
        }
        Term term = new Term();
        for (NodeEM nodeEMList1 : nodeEMList) {
            term.setId_concept(tabulateDocument.getId());
            term.setId_term(tabulateDocument.getId());
            term.setLexical_value(nodeEMList1.getLexical_value());
            term.setLang(nodeEMList1.getLang());
            term.setId_thesaurus(idThesaurus);
            term.setSource(nodeEMList1.getSource());
            term.setStatus(nodeEMList1.getStatus());
            termHelper.addNonPreferredTerm(ds, term, idUser);
        }
        
        // ajouter les notes
        NoteHelper noteHelper = new NoteHelper();

        for (Note note : tabulateDocument.getNote()) {
            noteHelper.addNote(ds,
                    tabulateDocument.getId(),
                    note.getLang(),
                    idThesaurus,
                    note.getNote(),
                    "note",
                    "",
                    idUser);
        }        
        for (Note note : tabulateDocument.getDefinition()) {
            noteHelper.addNote(ds,
                    tabulateDocument.getId(),
                    note.getLang(),
                    idThesaurus,
                    note.getNote(),
                    "definition",
                    "",
                    idUser);
        }
        for (Note note : tabulateDocument.getScopeNote()) {
            noteHelper.addNote(ds,
                    tabulateDocument.getId(),
                    note.getLang(),
                    idThesaurus,
                    note.getNote(),
                    "scopeNote",
                    "",
                    idUser);
        }
        for (Note note : tabulateDocument.getHistoryNote()) {
            noteHelper.addNote(ds,
                    tabulateDocument.getId(),
                    note.getLang(),
                    idThesaurus,
                    note.getNote(),
                    "historyNote",
                    "",
                    idUser);
        }        
        for (Note note : tabulateDocument.getEditorialNote()) {
            noteHelper.addNote(ds,
                    tabulateDocument.getId(),
                    note.getLang(),
                    idThesaurus,
                    note.getNote(),
                    "editorialNote",
                    "",
                    idUser);
        }         
        
        //Enregistrer les relations
        writeRelationsList(ds, tabulateDocument, idThesaurus, idUser);
        

        // Enregistrer les Mappings (alignements)
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        
        for (Alignment alignment : tabulateDocument.getAlignments()) {
            if(alignment.getType().equalsIgnoreCase("exactMatch")){
                alignmentHelper.addNewAlignment(ds,
                    1, // user 
                    "", //concept target
                    "", //thesaurus target
                    alignment.getUri(), // URI
                    1,
                    tabulateDocument.getId(),
                    idThesaurus,0
                );
            }
            if(alignment.getType().equalsIgnoreCase("closeMatch")){
                alignmentHelper.addNewAlignment(ds,
                    1, // user 
                    "", //concept target
                    "", //thesaurus target
                    alignment.getUri(), // URI
                    2,
                    tabulateDocument.getId(),
                    idThesaurus,0
                );
            }
        }
        return true;
    }    
    
    private boolean writeRelationsList(HikariDataSource ds,
            TabulateDocument tabulateDocument,
            String idThesaurus, int idUser) {
        
        ConceptHelper conceptHelper = new ConceptHelper();
        HierarchicalRelationship hierarchicalRelationship = new HierarchicalRelationship();
        
        for (String broader : tabulateDocument.getBroader()) {
            hierarchicalRelationship.setIdConcept1(tabulateDocument.getId());
            hierarchicalRelationship.setIdConcept2(broader);
            hierarchicalRelationship.setIdThesaurus(idThesaurus);
            hierarchicalRelationship.setRole("BT");

            try {
                Connection conn = ds.getConnection();
                conn.setAutoCommit(false);
                if(!conceptHelper.addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
                conn.commit();
                conn.close();
                
            } catch (SQLException ex) {
                Logger.getLogger(ImportTabuleIntoBDD.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
        for (String narrower : tabulateDocument.getNarrower()) {
            hierarchicalRelationship.setIdConcept1(tabulateDocument.getId());
            hierarchicalRelationship.setIdConcept2(narrower);
            hierarchicalRelationship.setIdThesaurus(idThesaurus);
            hierarchicalRelationship.setRole("NT");

            try {
                Connection conn = ds.getConnection();
                conn.setAutoCommit(false);
                if(!conceptHelper.addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
                conn.commit();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ImportTabuleIntoBDD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (String related : tabulateDocument.getRelated()) {
            hierarchicalRelationship.setIdConcept1(tabulateDocument.getId());
            hierarchicalRelationship.setIdConcept2(related);
            hierarchicalRelationship.setIdThesaurus(idThesaurus);
            hierarchicalRelationship.setRole("RT");

            try {
                Connection conn = ds.getConnection();
                conn.setAutoCommit(false);
                if(conceptHelper.addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
                conn.commit();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ImportTabuleIntoBDD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        return true;
    }    


    private void addLangsToThesaurus(HikariDataSource ds, String idThesaurus) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        ArrayList <String> tabListLang = thesaurusHelper.getAllUsedLanguagesOfThesaurus(ds, idThesaurus);
        for (String tabListLang1 : tabListLang) {
            if (!thesaurusHelper.isLanguageExistOfThesaurus(ds, idThesaurus, tabListLang1.trim())) {
                Thesaurus thesaurus = new Thesaurus();
                thesaurus.setId_thesaurus(idThesaurus);
                thesaurus.setContributor("");
                thesaurus.setCoverage("");
                thesaurus.setCreator("");
                thesaurus.setDescription("");
                thesaurus.setFormat("");
                thesaurus.setLanguage(tabListLang1);
                thesaurus.setPublisher("");
                thesaurus.setRelation("");
                thesaurus.setRights("");
                thesaurus.setSource("");
                thesaurus.setSubject("");
                thesaurus.setTitle("");
                thesaurus.setType("");
                thesaurusHelper.addThesaurusTraduction(ds, thesaurus);
            }
        }
    }

}
