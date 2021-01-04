/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Table;

/**
 *
 * @author antonio.perez
 */

public class ExportStatistiques {

    private String document; //String ou a tout l'information
    private String id_theso = "";
    public String lange = "";

    ArrayList<String> quisontorphan = new ArrayList<>();//les concept qui sont orphan
    public String tete="";
    public String totalconcept=""; 
    public String nondescr =""; 
    public String termesNonTra ="";
    public String notes = "";
    public String ConceptOrphan = "";

    /**
     * cette funtion permet de recuperer tout les fils d'un thesaurus on besoin
     * seulement id_theso et language;
     *
     * @param ds
     * @param idtheso
     * @param lg
     * @param option 1 pour la version txt 2 pour la version PDF (La version TXT
     * il a non_descripteurs, non traduit, et les Orphan)
     * @throws SQLException
     */
    public void recuperatefils(HikariDataSource ds, String idtheso, String lg, int option) throws SQLException {
        
        id_theso = idtheso;
        lange = lg;
        //Declarations de variables contents
        HashMap<String, String> map = new HashMap<>();// pour garder id_grupo et son lexivalvalue de les domaines
        ArrayList<String> candidats = new ArrayList<>();// touts le term du theso
        ArrayList<Integer> combienterm = new ArrayList<>();//combian term il y a dans chacun domain
        ArrayList<Integer> niveaux = new ArrayList<>();//savoir combiens niveaux descen de la racine
        ArrayList<Integer> nonT = new ArrayList<>();
        ArrayList<String> listtraduire = new ArrayList<>();
        ResultSet resultSet, resultSet1, rS;
        //Variables
        boolean first = true;
        int domines = 0;
        int niveauxterm = 0;
        String group__id = "";
        //Connections a la BDD
        Connection conn, conn2 = null;
        Statement stmt, stmt1, stmt2 = null;

        try {
            conn = conn2 = ds.getConnection();
            try {
                stmt = conn.createStatement();
                stmt1 = conn2.createStatement();
                stmt2 = conn2.createStatement();
                try {
                    //ici on recupere les domaines 
                    String query = "SELECT distinct idgroup, lexicalvalue, lang FROM concept_group_label where idthesaurus ='" + id_theso
                            + "'";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        String lexical = "";
                        String lang = "(";
                        String idgroup = resultSet.getString(1);
                        if (group__id == null ? idgroup != null : !group__id.equals(idgroup)) {// c'est unique le domain sans traduction
                            //lexical += resultSet.getString(2);//getString(2) =lexicalvalue.
                            //lang += resultSet.getString(3) + ")";//getString(3) = lang.
                            //lexical += lang;
                            candidats.add(idgroup);
                            niveaux.add(niveauxterm);
                            domines++;
                        } else {// c'est pour si il y a les memme domain en plusieurs lang
                            String change = map.get(group__id);
                            lang = "(";
                            lexical += ", " + resultSet.getString(2);
                            lang += resultSet.getString(3) + ")";
                            lexical += lang;
                            change += lexical;
                            lexical = change;
                            int ou = candidats.size();
                            candidats.remove(ou - 1);//efface le/les ancian pour mettre les nouvelles
                            candidats.add(idgroup);
                        }
                        group__id = resultSet.getString(1);
                        map.put(idgroup, "");//introduir dans le map

                    }
                    for (Map.Entry e : map.entrySet()) {
                        int combien = 0;
                        int cantitad = 0;
                        int orphan = 0;
                        ArrayList<String> conceptOrp = new ArrayList<>();
                        ArrayList<String> id = new ArrayList<>();
                        //ici nous avons le/les fils de chacun domaine
                        String query2 = "Select *  from concept where id_thesaurus = '" + id_theso + "' and id_concept IN  (SELECT idconcept FROM concept_group_concept WHERE idgroup ='" + e.getKey() + "') and top_concept ='true'";
                        //ici nous avos toutes les progéniture
                        String query4 = "Select id_concept from concept where id_concept IN (SELECT id_concept FROM concept_group_concept WHERE idgroup ='" + e.getKey() + "') ";
                        rS = stmt2.executeQuery(query4);
                        while (rS.next()) {
                            cantitad++;//on garde toutes le progénitures d'une domaine e.getkey
                            conceptOrp.add(rS.getString(1));
                        }
                        //appelation a la funtion pour savoir combien de concept orphan ils sont
                        combienterm.add(cantitad - orphan);
                        resultSet = stmt.executeQuery(query2);
                        while (resultSet.next()) {
                            combien++;
                            niveauxterm++;
                            candidats.add(resultSet.getString(1));
                            id.add(resultSet.getString(1));
                            niveaux.add(niveauxterm);
                            //on appele a la funtion recursive
                            genererfils(ds, id_theso, lange, candidats, id.get(id.size() - 1), niveaux, niveauxterm);
                            niveauxterm--;

                        }
                    }

                    listtraduire = copyarray(candidats);
                    candidats = changenames(ds, listtraduire, lange, domines);// change le id_term pour son lexical_value
                    creedocumentatlch(ds, listtraduire, id_theso, lange, niveaux, combienterm, map, domines, candidats);//crée le document 
                    if (option == 1)// cette part c'est seulement por le version .txt
                    {
                        nonT = nontraduit(ds, candidats, combienterm, domines, map);
                        avoirnondescripteur(ds, document, map, listtraduire, nonT, domines);//cherche le non descripteur
                    }
                } finally {
                    stmt2.close();
                    stmt1.close();
                    stmt.close();
                }
            } finally {
                conn.close();
                conn2.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * permet de faire une copy de notre
     *
     * @param candidats
     * @return
     */
    private ArrayList<String> copyarray(ArrayList<String> candidats) {
        ArrayList<String> retorno = new ArrayList<>();
        for (int i = 0; i < candidats.size(); i++) {
            retorno.add(candidats.get(i));
        }
        return retorno;
    }

    /**
     * Avec tout la information que nous avos on va a créer le documen a
     * telecharger
     *
     * @param ds
     * @param idTheso
     * @param langue
     * @param candidats les term que déjà on a garde
     * @param nom le term que on veux savoir son progéniture
     * @param niveaux le niveaux que on descen pour trouver le term "nom"
     * @param niveauxterm le niveaux ou il est le term nom
     */
    private void genererfils(HikariDataSource ds, String idTheso, String langue, ArrayList<String> candidats, String nom, ArrayList<Integer> niveaux, int niveauxterm) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultset;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    //ici on trouve le fils de "nom"
                    String query = "select * from hierarchical_relationship where id_thesaurus ='" + idTheso + "' and id_concept1='" + nom + "' and role ='NT'";
                    resultset = stmt.executeQuery(query);
                    while (resultset.next()) {

                        niveauxterm++;
                        niveaux.add(niveauxterm);
                        candidats.add(resultset.getString(4));//nous ajoutons le id_concept de chacun enfant

                        //on fait l'appelation recursive
                        genererfils(ds, idTheso, langue, candidats, resultset.getString(4), niveaux, niveauxterm);
                        niveauxterm--;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * on cherche dans la BDD le lexical value de chacun id_concept que nous
     * avons et on fait le change
     *
     * @param ds
     * @param candidats
     * @param lang
     */
    private ArrayList changenames(HikariDataSource ds, ArrayList<String> candidats, String lang, int domaines) {
        Connection conn = null;
        Statement stmt = null;
        String complet = "";
        ResultSet resultset, resultset1;
        ArrayList<String> candidattoterm = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    for (int dom = 0; dom < domaines; dom++) {
                        String query = "SELECT lexicalvalue FROM concept_group_label where idthesaurus ='" + id_theso
                                + "' and lang ='" + lange
                                + "' and idgroup ='" + candidats.get(dom) + "'";
                        resultset = stmt.executeQuery(query);
                        if (resultset.next()) {
                            candidattoterm.add(resultset.getString(1));
                            candidats.set(dom, resultset.getString(1));

                        } else {
                            candidattoterm.add(candidats.get(dom));
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    for (int i = domaines; i < candidats.size(); i++) {
                        int pos;
                        String query1 = "select id_term from preferred_term where id_concept ='" + candidats.get(i)
                                + "' and id_thesaurus='" + id_theso + "'";
                        resultset1 = stmt.executeQuery(query1);
                        if (resultset1.next()) {
                            candidattoterm.add(resultset1.getString(1));
                        }

                        //pour chac id on prendre son lexical value dan la lang que nous sommes
                        String query = "select id_term, lexical_value from term where id_term ='" + candidattoterm.get(i)
                                + "' and lang ='" + lang + "'and id_thesaurus='" + id_theso + "'";
                        resultset = stmt.executeQuery(query);
                        if (resultset.next()) {
                            String Idconcetpagarder = "";
                            Idconcetpagarder = candidats.get(i);
                            candidats.remove(i);//efface le id 
                            complet += resultset.getString(2) + " (" + Idconcetpagarder + ")";//on ecrit le lexical value et son id
                            candidats.add(i, complet);
                            complet = "";
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return candidattoterm;
    }

    /**
     * on crée le String que apres serais le fichier a telecharger
     *
     * @param candidat tout les term que nous avons apres de chercher toutes les
     * enfants
     * @param id_theso
     * @param lg langue
     * @param niveaux le niveaux correspondat a chacun term
     * @param cantite cantite de term que il y a dans une domaine
     * @param map le hasMap avec le domaines y son id
     * @param domaines combien domaines on a trouvé
     */
    private void creedocumentatlch(HikariDataSource ds, ArrayList<String> candidat, String id_theso, String lg,
            ArrayList<Integer> niveaux, ArrayList<Integer> cantite, HashMap<String, String> map, int domaines, ArrayList<String> ids) throws SQLException {
        int quelledomaine = 0;// le domaine que on prendre pour commencé a ecrire
        int positionmultipli = 0;//combien de position on besoin avancé pour trouvée le bon term
        boolean dernier = false;
        int domaines2 = domaines;
        document = tete + id_theso + "  \t  " + lg + " \t "+ totalconcept + (candidat.size() - map.size()) + "\r\n";
        //document += langueBean.getMsg("stat.statCpt4")+"\n\n";//ne marche pas et je sais pas pourquoi
        for (int cont = 0; cont < domaines2; cont++) {
            document += candidat.get(cont) + "   " + cantite.get(quelledomaine) + " + " + " Orphan \r\n";//combiens term pour chac domaine
            //on commence aprés le domaines a chercher le term (domaines + positionmultipli)
            //cantite.get(quelledomaine) nous donne combiens term pour chac domain
            //+domaines parceque on commence aprés le domaines
            //+positionmultipli si ce n'est pas le 1° avances tout les term jusca arriver a le bonne
            for (int j = (domaines + positionmultipli); j < ((cantite.get(quelledomaine) + domaines) + positionmultipli); j++) {
                //selon le niveaux du term on fait plus or moins espace en blanche
                for (int i = 0; i < niveaux.get(j); i++) {
                    document += "   ";
                }
                document += candidat.get(j) + " ";// on ecrit le term
                ArrayList<String> notesOfTerm = ajouterNotes(ds, ids.get(j), id_theso);// et on cherche ses notes, et on la garde dans notesOfTerm
                for (int z = 0; z < notesOfTerm.size(); z++) {
                    document += notesOfTerm.get(z);//on ecrite les notes/note si il y a
                }
                document += "\r\n";
                if (quelledomaine == (domaines - 1))//si quelledomaine arrive a ça, c'est parce que lui est le dernier
                {
                    dernier = true;
                }
            }
            if (!dernier)//si ce n'est pas le dernier, change pour savoir ou commence les term de le prochain domaine
            {
                positionmultipli += cantite.get(quelledomaine);
                quelledomaine++;
            }
        }
    }

    /**
     * on va a chercher le term que il ne sont pas descripteur
     *
     * @param ds
     * @param document
     * @param map
     * @return
     */
    private String avoirnondescripteur(HikariDataSource ds, String document, HashMap<String, String> map, ArrayList<String> listnonT, ArrayList<Integer> noTraduit, int domaines) {
        ArrayList<Integer> ouviens = new ArrayList<>();//ArrayList pour savoir combien de term "nondescripteur" il y a dans chac domaine
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultset;
        int ousont = 0;//pour avancé dans ouviens et savoir le/les term  "nondescripteur"
        int combien = 0;//pour conté combien ils sont
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    for (Map.Entry e : map.entrySet()) {
                        //On cherche "nondescripteur" dans la table permuted avec le id_group, la lange, le theso et la option de ispreferredterm a false
                        String query = "Select id_concept from permuted where id_group = '" + e.getKey()
                                + "' and id_lang = '" + lange
                                + "' and ispreferredterm= 'false'"
                                + " and id_thesaurus ='" + id_theso + "'";
                        resultset = stmt.executeQuery(query);
                        while (resultset.next()) {
                            combien++;
                        }
                        ouviens.add(combien);//introduisons combien dans l'ArrayList
                        combien = 0;//reinicialisons le valeur pour le prochaine
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        int conttateur = 0;
        int cont;
        changenames(ds, quisontorphan, lange, combien);
//        for (int h = 0; h < domaines; h++) {
//            this.document += "\r\n\r\n" + listnonT.get(h) + "  "+nondescr + ouviens.get(ousont);
//            this.document += "\r\n\t  "+termesNonTra + noTraduit.get(ousont);//manque faire le languageBean.getMsg 
//            this.document += "\r\n\t  "+notes ;//donne error le getMsg
//            this.document += "\r\n\t  "+ConceptOrphan ;
//            for (cont = conttateur; cont < concept_orphan.get(ousont) + conttateur; cont++) {
//                this.document += quisontorphan.get(cont) + ", ";
//            }
//            conttateur += concept_orphan.get(ousont);
//            ousont++;
//        }
        return this.document;
    }

    /**
     * Cette funtion cherche le/les notes d'un term (ids), que il est dans le
     * thesaurus id_theso
     *
     * @param ds
     * @param ids
     * @param id_theso
     * @return
     * @throws SQLException
     */
    private ArrayList<String> ajouterNotes(HikariDataSource ds, String ids, String id_theso) throws SQLException {
        ArrayList<String> notes = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultset;
        String id_concept = "";// pour garder la transformation de id_concept a id_term
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {//traduction de id_concept a id_term
                    String query = "select id_concept from preferred_term where id_term='" + ids
                            + "' and id_thesaurus= '" + id_theso + "'";
                    resultset = stmt.executeQuery(query);
                    if (resultset.next()) {
                        id_concept = resultset.getString("id_concept");
                    }// chercher toutes le notes de une terme
                    if (id_concept != null) {
                        query = "Select notetypecode, lexicalvalue, lang from note "
                                + " where (id_term ='" + ids + "' OR id_concept='" + id_concept
                                + "') and id_thesaurus= '" + id_theso + "'";
                        resultset = stmt.executeQuery(query);
                        while (resultset.next()) {
                            //on garde le/ les notes dans un Array notes
                            String note = resultset.getString("notetypecode");
                            note += "(" + resultset.getString("lang") + ")";
                            note += "-> " + resultset.getString("lexicalvalue") + ",";
                            notes.add(note);

                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return notes;
    }

    /**
     * ici on cherche toutes le concept que ne sont pas de traduction dans la
     * langue que nous sommes
     *
     * @param ds
     * @param sansnom
     * @param cantite
     * @param domaines
     * @param map
     * @return
     */
    private ArrayList<Integer> nontraduit(HikariDataSource ds, ArrayList<String> sansnom, ArrayList<Integer> cantite, int domaines, HashMap<String, String> map) {

        ArrayList<Integer> noTraduit = new ArrayList<>();
        boolean dernier = false;
        int traduction = 0;
        int positionmultipli = 0;
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultset;
        int alle = domaines;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {

                    for (int quelledomaine = 0; quelledomaine < domaines; quelledomaine++) {
                        for (int i = alle; i < ((cantite.get(quelledomaine) + domaines) + positionmultipli); i++) {
                            String query = "Select id_term from term where "
                                    + " lang = '" + lange
                                    + "'  and id_thesaurus ='" + id_theso + "'"
                                    + "  and id_term='" + sansnom.get(i) + "'";
                            resultset = stmt.executeQuery(query);
                            while (resultset.next()) {
                                traduction++;
                            }
                        }
                        int combien = cantite.get(quelledomaine) - traduction;
                        noTraduit.add(combien);
                        traduction = 0;
                        alle = cantite.get(quelledomaine) + domaines;
                        positionmultipli = cantite.get(quelledomaine);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return noTraduit;

    }
    //getter and setter

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setLange(String lange) {
        this.lange = lange;
    }

    public String getTete() {
        return tete;
    }

    public void setTete(String tete) {
        this.tete = tete;
    }

    public String getTotalconcept() {
        return totalconcept;
    }

    public void setTotalconcept(String totalconcept) {
        this.totalconcept = totalconcept;
    }

    public String getNondescr() {
        return nondescr;
    }

    public void setNondescr(String nondescr) {
        this.nondescr = nondescr;
    }

    public String getTermesNonTra() {
        return termesNonTra;
    }

    public void setTermesNonTra(String termesNonTra) {
        this.termesNonTra = termesNonTra;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getConceptOrphan() {
        return ConceptOrphan;
    }

    public void setConceptOrphan(String ConceptOrphan) {
        this.ConceptOrphan = ConceptOrphan;
    }
    

}
