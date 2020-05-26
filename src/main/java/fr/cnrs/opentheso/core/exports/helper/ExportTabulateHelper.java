/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupLabel;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.core.exports.tabulate.ThesaurusDatas;
import fr.cnrs.opentheso.core.exports.tabulate.FieldsSkos;

/**
 *
 * @author miled.rousset
 */
public class ExportTabulateHelper {

    private ThesaurusDatas thesaurusDatas;
    private StringBuffer tabulateBuff;
    private List<NodeLangTheso> selectedLanguages;

    public ExportTabulateHelper() {
    }

    /**
     * Cette fonction permet de récupérer toutes les données d'un thésaurus puis
     * les charger dans la classe thesaurusDatas
     *
     * @param ds
     * @param idThesaurus
     * @return true or false
     */
    public boolean setThesaurusDatas(HikariDataSource ds,
            String idThesaurus) {

        ExportThesaurus exportThesaurus = new ExportThesaurus();
        if (!exportThesaurus.exportAllDatas(ds, idThesaurus)) {
            return false;
        }
        this.thesaurusDatas = exportThesaurus.getThesaurusDatas();
        return true;
    }

    /**
     * Cette fonction permet de récupérer toutes les données d'un thésaurus puis
     * les charger dans la classe thesaurusDatas en filtrant par langue et
     * Groupe
     *
     * @param ds
     * @param idThesaurus
     * @param selectedLanguages
     * @param selectedGroups
     * @return true or false
     */
    public boolean setThesaurusDatas(HikariDataSource ds,
            String idThesaurus,
            List<NodeLangTheso> selectedLanguages,
            List<NodeGroup> selectedGroups) {

        ExportThesaurus exportThesaurus = new ExportThesaurus();
        if (!exportThesaurus.exportAllDatas(ds, idThesaurus,
                selectedLanguages, selectedGroups)) {
            return false;
        }
        this.thesaurusDatas = exportThesaurus.getThesaurusDatas();
        this.selectedLanguages = selectedLanguages;
        return true;
    }

    /**
     * permet de préparer le thésaurus au format tabulé les données sont écrites
     * dans une variable type StringBuffer
     *
     * @return
     */
    public boolean exportToTabulate() {

        //   System.out.println("Arrive à l'export Tabulé !!! ");
        if (thesaurusDatas == null) {
            return false;
        }

        tabulateBuff = new StringBuffer();
        if (!writeFields()) {
            return false;
        }

        if (!writeGroups()) {
            return false;
        }

        if (!writeConcepts()) {
            return false;
        }

        return true;
    }

    private boolean writeFields() {
        FieldsSkos fieldsSkos = new FieldsSkos();
        for (String field : fieldsSkos.getFields()) {

            tabulateBuff.append(field);
            tabulateBuff.append(";");
        }
        tabulateBuff.append("\n");
        return true;
    }

    private boolean writeGroups() {
        ArrayList<NodeGroupLabel> nodeGroupLabel = thesaurusDatas.getNodeGroupLabels();

        boolean first;

        for (NodeGroupLabel nodeGroupLabel1 : nodeGroupLabel) {
            // idGroup
            tabulateBuff.append(nodeGroupLabel1.getIdGroup());
            tabulateBuff.append(";");

            // idArk
            if (nodeGroupLabel1.getIdArk() == null) {
                tabulateBuff.append("");
            } else {
                tabulateBuff.append(nodeGroupLabel1.getIdArk());
            }
            tabulateBuff.append(";");

            // type
            tabulateBuff.append("MT");
            tabulateBuff.append(";");

            // preflabel
            first = true;
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupLabel1.getNodeGroupTraductionses()) {
                if (!first) {
                    tabulateBuff.append("##");
                }
                tabulateBuff.append(nodeGroupTraduction.getTitle());
                tabulateBuff.append("::");
                tabulateBuff.append(nodeGroupTraduction.getIdLang());
                first = false;
            }
            tabulateBuff.append(";");

            // altLabel
            tabulateBuff.append(";");

            // inScheme
            tabulateBuff.append(";");

            // broader
            tabulateBuff.append(";");

            // narrower
            tabulateBuff.append(";");

            // related
            tabulateBuff.append(";");

            // alignment
            tabulateBuff.append(";");

            // definition
            tabulateBuff.append(";");

            // scopeNote
            tabulateBuff.append(";");

            // historyNote
            tabulateBuff.append(";");

            // editorialNote
            tabulateBuff.append(";");

            // createdDate
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupLabel1.getNodeGroupTraductionses()) {
                tabulateBuff.append(nodeGroupTraduction.getCreated());
            }
            tabulateBuff.append(";");

            // modifiedDdate
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupLabel1.getNodeGroupTraductionses()) {
                tabulateBuff.append(nodeGroupTraduction.getModified());
            }
            tabulateBuff.append("\n");
        }
        return true;
    }

    private boolean writeConcepts() {
        ArrayList<NodeConceptExport> nodeConceptExports = thesaurusDatas.getNodeConceptExports();

        boolean first = true;
        ArrayList<NodeNote> nodeNoteDefinition = new ArrayList<>();
        ArrayList<NodeNote> nodeNoteScope = new ArrayList<>();
        ArrayList<NodeNote> nodeNoteHistory = new ArrayList<>();
        ArrayList<NodeNote> nodeNoteEditorial = new ArrayList<>();

        ArrayList<String> listLangues = new ArrayList<>();

        for (NodeLangTheso selectedLanguage : selectedLanguages) {
            listLangues.add(selectedLanguage.getCode());
        }
        
        for (NodeConceptExport nodeConceptExport : nodeConceptExports) {

            nodeNoteDefinition.clear();
            nodeNoteScope.clear();
            nodeNoteHistory.clear();
            nodeNoteEditorial.clear();

            // id

            tabulateBuff.append(nodeConceptExport.getConcept().getIdConcept());
            tabulateBuff.append(";");

            // idArk
            if (nodeConceptExport.getConcept().getIdArk() == null) {
                tabulateBuff.append("");
            } else {
                tabulateBuff.append(nodeConceptExport.getConcept().getIdArk());
            }
            tabulateBuff.append(";");

            // type
            if (nodeConceptExport.getConcept().isTopConcept()) {
                tabulateBuff.append("TT");
            } else {
                tabulateBuff.append("DE");
            }
            tabulateBuff.append(";");

            // preflabel
            for (NodeTermTraduction nodeTermTraduction : nodeConceptExport.getNodeTermTraductions()) {

                if (listLangues.contains(nodeTermTraduction.getLang())) {
                    if (!first) {
                        tabulateBuff.append("##");
                    }
                    tabulateBuff.append(nodeTermTraduction.getLexicalValue());
                    tabulateBuff.append("::");
                    tabulateBuff.append(nodeTermTraduction.getLang());
                    first = false;
                }
            }

            tabulateBuff.append(";");

            // altLabel
            first = true;
            for (NodeEM nodeEM : nodeConceptExport.getNodeEM()) {
                if (listLangues.contains(nodeEM.getLang())) {
                    if (!first) {
                        tabulateBuff.append("##");
                    }
                    tabulateBuff.append(nodeEM.getLexical_value());
                    tabulateBuff.append("::");
                    tabulateBuff.append(nodeEM.getLang());
                    first = false;
                }
            }

            tabulateBuff.append(";");

            // inScheme
            first = true;
            for (NodeUri nodeUri : nodeConceptExport.getNodeListIdsOfConceptGroup()) {
                if (!first) {
                    tabulateBuff.append("##");
                }
                tabulateBuff.append(nodeUri.getIdConcept());
                first = false;
            }
            tabulateBuff.append(";");

            // broader
            first = true;
            for (NodeHieraRelation node : nodeConceptExport.getNodeListOfBT()) {
                if (!first) {
                    tabulateBuff.append("##");
                }
                tabulateBuff.append(node.getUri().getIdConcept());
                first = false;
            }
            tabulateBuff.append(";");

            // narrower
            first = true;
            for (NodeHieraRelation node : nodeConceptExport.getNodeListOfNT()) {
                if (!first) {
                    tabulateBuff.append("##");
                }
                tabulateBuff.append(node.getUri().getIdConcept());
                first = false;
            }
            tabulateBuff.append(";");

            // related
            first = true;
            for (NodeHieraRelation nodeUri : nodeConceptExport.getNodeListIdsOfRT()) {
                if (!first) {
                    tabulateBuff.append("##");
                }
                tabulateBuff.append(nodeUri.getUri().getIdConcept());
                first = false;
            }
            tabulateBuff.append(";");

            // alignment
            first = true;
            for (NodeAlignmentSmall nodeAlignment : nodeConceptExport.getNodeAlignmentsList()) {
                if (!first) {
                    tabulateBuff.append("##");
                }
                if (nodeAlignment.getAlignement_id_type() == 1) {
                    tabulateBuff.append("exactMatch::");
                    tabulateBuff.append(nodeAlignment.getUri_target());
                }
                if (nodeAlignment.getAlignement_id_type() == 2) {
                    tabulateBuff.append("closeMatch::");
                    tabulateBuff.append(nodeAlignment.getUri_target());
                }
                first = false;
            }
            tabulateBuff.append(";");

            // notes Concept
            // types : definition; editorialNote; historyNote ; scopeNote
            for (NodeNote nodeNote : nodeConceptExport.getNodeNoteConcept()) {
                nodeNote.setLexicalvalue(nodeNote.getLexicalvalue().replace('\r', ' '));
                nodeNote.setLexicalvalue(nodeNote.getLexicalvalue().replace('\n', ' '));
                if (nodeNote.getNotetypecode().equalsIgnoreCase("definition")) {
                    nodeNoteDefinition.add(nodeNote);
                }
                if (nodeNote.getNotetypecode().equalsIgnoreCase("editorialNote")) {
                    nodeNoteEditorial.add(nodeNote);
                }
                if (nodeNote.getNotetypecode().equalsIgnoreCase("historyNote")) {
                    nodeNoteHistory.add(nodeNote);
                }
                if (nodeNote.getNotetypecode().equalsIgnoreCase("scopeNote")) {
                    nodeNoteScope.add(nodeNote);
                }

            }
            // notes Term  
            // types : definition; editorialNote; historyNote ; scopeNote
            for (NodeNote nodeNote : nodeConceptExport.getNodeNoteTerm()) {
                nodeNote.setLexicalvalue(nodeNote.getLexicalvalue().replace('\r', ' '));
                nodeNote.setLexicalvalue(nodeNote.getLexicalvalue().replace('\n', ' '));
                if (nodeNote.getNotetypecode().equalsIgnoreCase("definition")) {
                    nodeNoteDefinition.add(nodeNote);
                }
                if (nodeNote.getNotetypecode().equalsIgnoreCase("editorialNote")) {
                    nodeNoteEditorial.add(nodeNote);
                }
                if (nodeNote.getNotetypecode().equalsIgnoreCase("historyNote")) {
                    nodeNoteHistory.add(nodeNote);
                }
                if (nodeNote.getNotetypecode().equalsIgnoreCase("scopeNote")) {
                    nodeNoteScope.add(nodeNote);
                }

            }

            // definition
            first = true;
            for (NodeNote nodeNote : nodeNoteDefinition) {
                if (listLangues.contains(nodeNote.getLang())) {
                    if (!first) {
                        tabulateBuff.append("##");
                    }
                    tabulateBuff.append(nodeNote.getLexicalvalue());
                    tabulateBuff.append("::");
                    tabulateBuff.append(nodeNote.getLang());
                    first = false;
                }
            }
            tabulateBuff.append(";");

            // scopeNote
            first = true;
            for (NodeNote nodeNote : nodeNoteScope) {
                if (listLangues.contains(nodeNote.getLang())) {
                    if (!first) {
                        tabulateBuff.append("##");
                    }
                    tabulateBuff.append(nodeNote.getLexicalvalue());
                    tabulateBuff.append("::");
                    tabulateBuff.append(nodeNote.getLang());
                    first = false;
                }
            }
            tabulateBuff.append(";");

            // historyNote
            first = true;
            for (NodeNote nodeNote : nodeNoteHistory) {
                if (listLangues.contains(nodeNote.getLang())) {
                    if (!first) {
                        tabulateBuff.append("##");
                    }
                    tabulateBuff.append(nodeNote.getLexicalvalue());
                    tabulateBuff.append("::");
                    tabulateBuff.append(nodeNote.getLang());
                    first = false;
                }
            }
            tabulateBuff.append(";");

            // editorialNote
            first = true;
            for (NodeNote nodeNote : nodeNoteEditorial) {
                if (listLangues.contains(nodeNote.getLang())) {
                    if (!first) {
                        tabulateBuff.append("##");
                    }
                    tabulateBuff.append(nodeNote.getLexicalvalue());
                    tabulateBuff.append("::");
                    tabulateBuff.append(nodeNote.getLang());
                    first = false;
                }
            }
            tabulateBuff.append(";");

            // dates
            tabulateBuff.append(nodeConceptExport.getConcept().getCreated());
            tabulateBuff.append(";");
            tabulateBuff.append(nodeConceptExport.getConcept().getModified());

            tabulateBuff.append("\n");
            first = true;
        }
        return true;
    }

    public StringBuffer getTabulateBuff() {
        return tabulateBuff;
    }

}
