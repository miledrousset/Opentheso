/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.helper;

import com.k_int.IR.IRQuery;
import com.k_int.IR.QueryModels.PrefixString;
import com.k_int.IR.SearchException;
import com.k_int.IR.SearchTask;
import com.k_int.IR.Searchable;
import com.k_int.IR.TimeoutExceededException;
import com.k_int.hss.HeterogeneousSetOfSearchable;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.bdd.tools.AsciiUtils;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author miled.rousset
 */
public class ExportTxtHelper {

    private StringBuffer txtBuff;
    private List<NodeGroup> selectedGroups;
    private String selectedLang;
    private String idTheso;
    private HikariDataSource ds;
    private int indentationConcept = 0;
    private int indentationNT = 0;
    private int indentationBT = 0;
    private int indentationRT = 0;
    private int indentationUF = 0;
    private int indentationGroups = 0;    
    private int indentationTraductions = 0;
    private int indentationNotes = 0;
    private int number = 0; 

    private NodePreference nodePreference;
    //   private String[] selectedOptions;
    private ArrayList<String> selectedOptions;

    public ExportTxtHelper() {
    }

    /**
     * Cette fonction permet de récupérer toutes les données d'un thésaurus puis
     * les charger dans la classe thesaurusDatas en filtrant par langue et
     * Groupe
     *
     * @param ds
     * @param idThesaurus
     * @param selectedLang
     * @param selectedGroups
     * @param nodePreference
     * @param selectedOptions
     */
    public void setThesaurusDatas(HikariDataSource ds,
            String idThesaurus,
            String selectedLang,
            List<NodeGroup> selectedGroups,
            NodePreference nodePreference,
            ArrayList<String> selectedOptions) {

        this.idTheso = idThesaurus;
        this.selectedLang = selectedLang;
        this.selectedGroups = selectedGroups;
        this.ds = ds;
        this.nodePreference = nodePreference;
        this.selectedOptions = selectedOptions;

    }

    /**
     * permet de préparer le thésaurus au format tabulé. Les données sont
     * écrites dans des variables type StringBuffer suivant les options
     * sélectionnées dans le tableau String[] selectedOptions
     *
     * @return
     */
    public boolean exportToTxtCsv() {
        txtBuff = new StringBuffer();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();

        NodeThesaurus nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idTheso);
        if (nodeThesaurus == null) {
            return false;
        }

        // on écrit le label du thésaurus
    //    writeHeader(nodeThesaurus);

        // écriture des groupes
        GroupHelper groupHelper = new GroupHelper();
        NodeGroup nodeGroup;
        for (NodeGroup selectedGroup : selectedGroups) {
            nodeGroup = groupHelper.getThisConceptGroup(ds, selectedGroup.getConceptGroup().getIdgroup(), idTheso, selectedLang);
        //    writeGroup(nodeGroup);

            // écriture récursive des concepts 
            writeConcepts(nodeGroup.getConceptGroup().getIdgroup());

        }
        return true;
    }

    /**
     * permet d'acrire le titre du thésaurus
     *
     * @return
     */
    private void writeHeader(NodeThesaurus nodeThesaurus) {

        for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
            if (thesaurus.getLanguage().equalsIgnoreCase(selectedLang)) {
                txtBuff.append(thesaurus.getTitle());
                txtBuff.append(" (");
                txtBuff.append(idTheso);
                txtBuff.append(" )");
            }
        }
        txtBuff.append("\n");
    }
    
    /**
     * permet d'acrire le titre du thésaurus
     *
     * @return
     */
    private void writeColumnHeader() {
        String headers = "";
        
        headers = headers + "Id" + "\t";
        headers = headers + "idConcept" + "\t";          

        for (int i=1; i <=indentationConcept; i++) {
            headers = headers + "N" + i + "\t";
        }
        
        if (selectedOptions.contains("occ")) {
            headers = headers + "OCC" + "\t";
        }
        
        if (selectedOptions.contains("nt")) {
            for (int i=1; i <=indentationNT; i++) {
                headers = headers + "NT" + i + "\t";
            }
        }
        
        if (selectedOptions.contains("uf")) {
            for (int i=1; i <=indentationUF; i++) {
                headers = headers + "UF" + i + "\t";
            }
        }        

        if (selectedOptions.contains("bt")) {
            for (int i=1; i <=indentationBT; i++) {
                headers = headers + "BT" + i + "\t";
            }
        }

        if (selectedOptions.contains("rt")) {
            for (int i=1; i <=indentationRT; i++) {
                headers = headers + "RT" + i + "\t";
            }
        }

        if (selectedOptions.contains("groups")) {
            for (int i=1; i <=indentationGroups; i++) {
                headers = headers + "MT" + i + "\t";
            }
        }        

        if (selectedOptions.contains("traductions")) {
            for (int i=1; i <=indentationTraductions; i++) {
                headers = headers + "LANG" + i + "\t";
            }
        }

        if (selectedOptions.contains("notes")) {
            for (int i=1; i <=indentationNotes; i++) {
                headers = headers + "NOTE" + i + "\t";
            }
        }        
        txtBuff.insert(0, headers);
        txtBuff.insert(headers.length(), "\n");
    }    

    
    
    /**
     * permet d'acrire le group du thésaurus
     *
     * @return
     */
    private void writeGroup(NodeGroup nodeGroup) {
        if (nodeGroup.getIdLang().equalsIgnoreCase(selectedLang)) {
            txtBuff.append(nodeGroup.getLexicalValue());
            txtBuff.append(" (");
            txtBuff.append(nodeGroup.getConceptGroup().getIdgroup());
            txtBuff.append(" )");
        }
        txtBuff.append("\n\n");
    }

    /**
     * Permet d'écrire les concepts en bouclant par récursivité pour définir les
     * niveaux
     *
     * @param idGroup
     */
    private void writeConcepts(String idGroup) {

        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList<NodeUri> nodeUris;
        // la liste des TopConcepts pour un Groupe
        nodeUris = conceptHelper.getListIdsOfTopConceptsForExport(ds, idGroup, idTheso);
        if (nodeUris == null) {
            return;
        }

        // pour compter le décalage (indentations)
        nodeUris.forEach((nodeUri) -> {
            String indentation = "";
            countIndentation(nodeUri.getIdConcept(), indentation);
        });

        // on écrit les concepts et le décalage par tabulation
        nodeUris.forEach((nodeUri) -> {
            String indentation = "";
            writeConceptsInfo(nodeUri.getIdConcept(), indentation);
            writeConceptRecursive(nodeUri.getIdConcept(), indentation);
        });
        writeColumnHeader();
    }

    /**
     * permet de compter le nombre de décalalge pour chaque type de données
     * concept : pour l'hiérarchie NT : pour le nombre maxi de NT existant dans
     * le thésaurus ou le domaine choisi RT ... UF ....
     *
     * @param idConcept
     * @param indentation
     */
    private void countIndentation(String idConcept, String indentation) {
        ArrayList<NodeNT> childList = new RelationsHelper().getListNT(ds, idConcept, idTheso, selectedLang);
        if (childList == null) {
            return;
        }
        // indentation des concepts par hiérarchie
        indentation += "\t";
        int tot = StringUtils.countMatches(indentation, "\t");
        if (tot > indentationConcept) {
            indentationConcept = tot;
        }

        // indentation des NT nombre maxi
        if (selectedOptions.contains("nt")) {
            int totNT = new RelationsHelper().getCountOfNT(ds, idConcept, idTheso);
            if (totNT > indentationNT) {
                indentationNT = totNT;
            }
        }

        // indentation des BT nombre maxi
        if (selectedOptions.contains("bt")) {
            int totBT = new RelationsHelper().getCountOfBT(ds, idConcept, idTheso);
            if (totBT > indentationBT) {
                indentationBT = totBT;
            }
        }

        // indentation des RT nombre maxi
        if (selectedOptions.contains("rt")) {
            int totRT = new RelationsHelper().getCountOfRT(ds, idConcept, idTheso);
            if (totRT > indentationRT) {
                indentationRT = totRT;
            }
        }

        // indentation des UF nombre maxi
        if (selectedOptions.contains("uf")) {
            int totUF = new RelationsHelper().getCountOfUF(ds, idConcept, idTheso, selectedLang);
            if (totUF > indentationUF) {
                indentationUF = totUF;
            }
        }
        
        // indentation des Groups nombre maxi
        if (selectedOptions.contains("groups")) {
            int totGroup = new GroupHelper().getCountOfGroups(ds, idConcept, idTheso);
            if (totGroup > indentationGroups) {
                indentationGroups = totGroup;
            }
        }        

        // indentation traductions
        if (selectedOptions.contains("traductions")) {
            int totTraductions = new TermHelper().getCountOfTraductions(ds, idConcept, idTheso, selectedLang);
            if (totTraductions > indentationTraductions) {
                indentationTraductions = totTraductions;
            }
        }

        // indentation notes
        if (selectedOptions.contains("notes")) {
            int totNotes = new NoteHelper().getCountOfNotes(ds, idConcept, idTheso, selectedLang);
            if (totNotes > indentationNotes) {
                indentationNotes = totNotes;
            }
        }

        for (NodeNT nodeNT : childList) {
            countIndentation(nodeNT.getIdConcept(), indentation);
        }
    }

    /**
     * fonction recursive qui sert a ecrire tout les fils des term
     *
     * @param id
     * @param indentation
     * @param paragraphs
     * @param idToDoc
     */
    private void writeConceptRecursive(String idConcept, String indentation) {
        ArrayList<NodeNT> childList = new RelationsHelper().getListNT(ds, idConcept, idTheso, selectedLang);
        if (childList == null) {
            return;
        }
        indentation += "\t";
        for (NodeNT nodeNT : childList) {
            writeConceptsInfo(nodeNT.getIdConcept(), indentation);
            writeConceptRecursive(nodeNT.getIdConcept(), indentation);
        }
    }

    /**
     * Permet d'écrire les concepts en bouclant par récursivité pour définir les
     * niveaux
     *
     * @return
     */
    private void writeConceptsInfo(String idConcept, String indentation) {
        ConceptHelper conceptHelper = new ConceptHelper();
        NodeConcept nodeConcept = conceptHelper.getConcept(ds, idConcept, idTheso, selectedLang);
        StringPlus stringPlus = new StringPlus();
        boolean first = true;

        if (nodeConcept == null) {
            return;
        }

        // id numérique séquentiel 
        txtBuff.append(++number);        
        txtBuff.append("\t");
        
        // Id_concept
        txtBuff.append(idConcept);
        txtBuff.append("\t");

        // on écrit le Term_concept
        txtBuff.append(indentation);

        if (nodeConcept.getTerm().getLexical_value().isEmpty()) {
            txtBuff.append("(");
            txtBuff.append(idConcept);
            txtBuff.append(")");
        } else {
            txtBuff.append(nodeConcept.getTerm().getLexical_value());
        }
        int tot = StringUtils.countMatches(indentation, "\t");
        if (tot < indentationConcept) {
            addTabulate(indentationConcept - (tot+1));
        }

        if (selectedOptions.contains("occ")) {
            //on écrit les occurences 
            txtBuff.append("\t");
            txtBuff.append(totalOfNotices(idConcept));
        }

        first = true;
        if (selectedOptions.contains("nt")) {        
        // on écrit la relation NT 
            if (!nodeConcept.getNodeNT().isEmpty()) {
                txtBuff.append("\t");                
                for (NodeNT nodeNT : nodeConcept.getNodeNT()) {
                    if(!first)
                        txtBuff.append("\t");
                //    txtBuff.append("NT: ");
                    txtBuff.append(nodeNT.getTitle());
                    first = false;
                }               
            }
            if(nodeConcept.getNodeNT().size() < indentationNT) {
                addTabulate(indentationNT - nodeConcept.getNodeNT().size());
            }            
        }
        
        first = true;
        // on écrit la relation UF
        if (selectedOptions.contains("uf")) {
            if (!nodeConcept.getNodeEM().isEmpty()) {
                txtBuff.append("\t");                
                for (NodeEM nodeEm : nodeConcept.getNodeEM()) {
                    if (!first)
                        txtBuff.append("\t");
                //    txtBuff.append("UF: ");
                    txtBuff.append(nodeEm.getLexical_value());
                    first = false;
                }                 
            }
            if(nodeConcept.getNodeEM().size() < indentationUF) {
                addTabulate(indentationUF - nodeConcept.getNodeEM().size());
            }             
        }        
        first = true;
        if (selectedOptions.contains("bt")) {
            // on écrit la relation TG
            if (!nodeConcept.getNodeBT().isEmpty()) {
                txtBuff.append("\t");
                for (NodeBT nodeBT : nodeConcept.getNodeBT()) {
                    if(!first)
                        txtBuff.append("\t");
            //        txtBuff.append("BT: ");
                    txtBuff.append(nodeBT.getTitle());
                    first = false;
                }
            }
            if(nodeConcept.getNodeBT().size() < indentationBT) {
                addTabulate(indentationBT - nodeConcept.getNodeBT().size());
            }             
        }

        first = true;
        // on écrit la relation RT 
        if (selectedOptions.contains("rt")) {
            if (!nodeConcept.getNodeRT().isEmpty()) {
                txtBuff.append("\t");                
                for (NodeRT nodeRT : nodeConcept.getNodeRT()) {
                    if (!first)
                        txtBuff.append("\t");
            //        txtBuff.append("RT: ");
                    txtBuff.append(nodeRT.getTitle());
                    first = false;
                }                 
            }
            if(nodeConcept.getNodeRT().size() < indentationRT) {
                addTabulate(indentationRT - nodeConcept.getNodeRT().size());
            }             
        }
        
        first = true;
        // on écrit les Groupes
        if (selectedOptions.contains("groups")) {
            if (!nodeConcept.getNodeConceptGroup().isEmpty()) {
                txtBuff.append("\t");                
                for (NodeGroup nodeGroup : nodeConcept.getNodeConceptGroup()) {
                    if (!first)
                        txtBuff.append("\t");
             //       txtBuff.append("MT: ");
                    txtBuff.append(nodeGroup.getLexicalValue());
                    first = false;
                }                 
            }
            if(nodeConcept.getNodeConceptGroup().size() < indentationGroups) {
                addTabulate(indentationGroups - nodeConcept.getNodeConceptGroup().size());
            }             
        }  
        
        first = true;
        // on écrit les traductions
        if (selectedOptions.contains("traductions")) {
            if (!nodeConcept.getNodeTermTraductions().isEmpty()) {
                txtBuff.append("\t");                
                for (NodeTermTraduction nodeTermTraduction : nodeConcept.getNodeTermTraductions()) {
                    if (!first)
                        txtBuff.append("\t");
        //            txtBuff.append("Tr: ");
                    txtBuff.append(nodeTermTraduction.getLexicalValue());
                    txtBuff.append("(");
                    txtBuff.append(nodeTermTraduction.getLang());
                    txtBuff.append(")");
                    first = false;
                }                 
            }
            if(nodeConcept.getNodeTermTraductions().size() < indentationTraductions) {
                addTabulate(indentationTraductions - nodeConcept.getNodeTermTraductions().size());
            }             
        }
        
        first = true;
        String note;
        // on écrit les notes de type Term
        if (selectedOptions.contains("notes")) {
            if ( (!nodeConcept.getNodeNotesTerm().isEmpty()) || (!nodeConcept.getNodeNotesConcept().isEmpty()) ) {
                txtBuff.append("\t");                
                for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
                    if (!first)
                        txtBuff.append("\t");
            //        txtBuff.append("noteT: ");
                    note = stringPlus.clearNewLine(nodeNote.getLexicalvalue());
                    note = note.replace('\"', ' ');
                    txtBuff.append(note);
                    first = false;
                }
                for (NodeNote nodeNote : nodeConcept.getNodeNotesConcept()) {
                    if (!first)
                        txtBuff.append("\t");
            //        txtBuff.append("noteC: ");
                    note = stringPlus.clearNewLine(nodeNote.getLexicalvalue());
                    note = note.replace('\"', ' ');            
                    txtBuff.append(note);
                    first = false;
                }                 
            }
            if(nodeConcept.getNodeNotesTerm().size() < indentationNotes) {
                addTabulate(indentationNotes - 
                        (nodeConcept.getNodeNotesConcept().size() + nodeConcept.getNodeNotesTerm().size())
                                );
            }             
        }
        
        txtBuff.append("\n");
    }

    /**
     * permet de rajouter les tabulations manquantes
     *
     * @param count
     */
    private void addTabulate(int count) {
        for (int i = 0; i < count; i++) {
            txtBuff.append("\t");
        }
    }

    /**
     * fonction temporaire qui ne marche qu'avec Koha
     */
    private int totalOfNotices(String idConcept) {
        int tot = 0;
        if (nodePreference == null) {
            return 0;
        }

        if (nodePreference.isZ3950actif()) {
            Properties p = new Properties();
            p.put("CollectionDataSourceClassName", "com.k_int.util.Repository.XMLDataSource");
            p.put("RepositoryDataSourceURL", "file:" + nodePreference.getPathNotice1());
            p.put("XSLConverterConfiguratorClassName", "com.k_int.IR.Syntaxes.Conversion.XMLConfigurator");
            p.put("ConvertorConfigFile", nodePreference.getPathNotice2());

            Searchable federated_search_proxy = new HeterogeneousSetOfSearchable();
            federated_search_proxy.init(p);

            try {
                IRQuery e = new IRQuery();
                //   e.collections = new Vector<String>();
                e.collections.add("KOHA/biblios");
                e.hints.put("default_element_set_name", "f");
                e.hints.put("small_set_setname", "f");
                e.hints.put("record_syntax", "unimarc");

                e.query = new PrefixString((new StringBuilder("@attrset bib-1 @attr 1=Koha-Auth-Number \"")).append(AsciiUtils.convertNonAscii("" + idConcept)).append("\"").toString());
                SearchTask st = federated_search_proxy.createTask(e, null);
                st.evaluate(5000);
                tot = st.getTaskResultSet().getFragmentCount();
                st.destroyTask();
                federated_search_proxy.destroy();
            } catch (TimeoutExceededException | SearchException srch_e) {
                // srch_e.printStackTrace();
            }
        }
        return tot;
    }

    public StringBuffer getTxtBuff() {
        return txtBuff;
    }
}
