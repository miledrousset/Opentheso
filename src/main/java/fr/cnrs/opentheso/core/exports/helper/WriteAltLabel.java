/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTab2Levels;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;

/**
 *
 * @author Miled
 */
public class WriteAltLabel  {

    StringBuilder stringBuilder;
    public WriteAltLabel() {
        stringBuilder = new StringBuilder();
    }
    
    
    public void setGroup (String idGroup, String value) {
        stringBuilder.append("idGroup = ");
        stringBuilder.append(idGroup);        
        stringBuilder.append("\t");
        stringBuilder.append("Groupe = ");
        stringBuilder.append(value); 
        stringBuilder.append("\t");   
        stringBuilder.append("\t");        
        stringBuilder.append("\n");
    }    
    
    public void setHeader () {
        stringBuilder.append("Id_concept");
        stringBuilder.append("\t");
        stringBuilder.append("prefLabel");
        stringBuilder.append("\t");
        stringBuilder.append("altLabel");
        stringBuilder.append("\t");
        stringBuilder.append("définition");
        stringBuilder.append("\n");
    }


    public void AddAltLabelByGroup(HikariDataSource ds,
            String idTheso, String idGroup, String idLang) {

        boolean passed = false;

        // ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        NoteHelper noteHelper = new NoteHelper();

        //ArrayList<NodeConceptArkId> allIds = conceptHelper.getAllConceptArkIdOfThesaurus(conn, idTheso);

        ArrayList<NodeEM> nodeEMs;
        ArrayList<NodeTab2Levels> nodeConceptTermId = termHelper.getAllIdOfNonPreferredTermsByGroup(ds, idTheso, idGroup);

        Term term;
        ArrayList<NodeNote> nodeNotes;

        for (NodeTab2Levels nodeTab2Levels : nodeConceptTermId) {
            nodeEMs = termHelper.getNonPreferredTerms(ds, nodeTab2Levels.getIdConcept(), idTheso, idLang);

            if (!nodeEMs.isEmpty()) {

                term = termHelper.getThisTerm(ds, nodeTab2Levels.getIdConcept(), idTheso, idLang);
                nodeNotes = noteHelper.getListNotesTerm(ds, nodeTab2Levels.getIdTerm(), idTheso, idLang);

                // écriture dans le fichier
                stringBuilder.append(nodeTab2Levels.getIdConcept());
                stringBuilder.append("\t");
                stringBuilder.append(term.getLexical_value());
                stringBuilder.append("\t");

                for (NodeEM nodeEM : nodeEMs) {
                    if (passed) {
                        stringBuilder.append("##");
                    }
                    stringBuilder.append(nodeEM.getLexical_value());
                    passed = true;

                }
                passed = false;
                stringBuilder.append("\t");
                
                for (NodeNote nodeNote : nodeNotes) {
                    if (nodeNote.getNotetypecode().equalsIgnoreCase("definition")) {
                        if (passed) {
                            stringBuilder.append("##");
                        }
                        stringBuilder.append(nodeNote.getLexicalvalue());
                        passed = true;
                    }
                }
                stringBuilder.append("\n");
            }
            passed = false;
        }
    }

    public StringBuilder getAllAltLabels() {
        return stringBuilder;
    }
    
    

}
