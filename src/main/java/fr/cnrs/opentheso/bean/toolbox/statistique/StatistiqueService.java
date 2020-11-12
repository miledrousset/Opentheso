package fr.cnrs.opentheso.bean.toolbox.statistique;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.faces.application.FacesMessage;


public class StatistiqueService {


    public List<GenericStatistiqueData> searchAllCollectionsByThesaurus(Connect connect, String idTheso, String idLang) {

        List<GenericStatistiqueData> result = new ArrayList<>();
        GroupHelper groupHelper = new GroupHelper();

        ArrayList<NodeGroup> listGroup = groupHelper.getListConceptGroup(connect.getPoolConnexion(), idTheso, idLang);

        listGroup.stream().forEach(group -> {
            GenericStatistiqueData data = new GenericStatistiqueData();
            data.setIdCollection(group.getConceptGroup().getIdgroup());
            data.setCollection(group.getLexicalValue());

            data.setNotesNbr(new NoteHelper().getNbrNoteByGroup(connect.getPoolConnexion(),
                    group.getConceptGroup().getIdgroup(), idTheso, idLang));

            data.setSynonymesNbr(new StatisticHelper().getNbSynonymesByGroup(connect.getPoolConnexion(), idTheso,
                    group.getConceptGroup().getIdgroup(), idLang));

            data.setConceptsNbr(new ConceptHelper().getCountOfConceptsOfGroup(connect.getPoolConnexion(), idTheso,
                    group.getConceptGroup().getIdgroup()));

            data.setTermesNonTraduitsNbr(
                    data.getConceptsNbr() - 
                    new StatisticHelper().getNbTradOfGroup(
                    connect.getPoolConnexion(), idTheso, group.getConceptGroup().getIdgroup(), idLang));//getNbrTermNonTraduit(connect.getPoolConnexion(), group, idTheso, idLang));

            result.add(data);
        });

        // Ajout de la dernier ligne réservé aux concepts sans collection
        GenericStatistiqueData data = new GenericStatistiqueData();
        data.setCollection("Sans collection");
        data.setConceptsNbr(new ConceptHelper().getCountOfConceptsSansGroup(connect.getPoolConnexion(), idTheso));
        data.setNotesNbr(new NoteHelper().getNbrNoteSansGroup(connect.getPoolConnexion(), idTheso, idLang));
        data.setSynonymesNbr(new StatisticHelper().getNbDesSynonimeSansGroup(connect.getPoolConnexion(), idTheso, idLang));
        data.setTermesNonTraduitsNbr(data.getConceptsNbr() - new StatisticHelper().getNbTradWithoutGroup(connect.getPoolConnexion(), idTheso, idLang));
        result.add(data);

        return result;

    }

    private int getNbrTermNonTraduit(HikariDataSource ds, NodeGroup group, String idTheso, String idLang) {
        TermHelper termHelper = new TermHelper();
        ArrayList<NodeUri> concepts = new ConceptHelper().getListConceptsOfGroup(ds, idTheso, group.getConceptGroup().getIdgroup());
        AtomicInteger nbrTermNonTraduit = new AtomicInteger();
        concepts.forEach(concept -> {
            if(!termHelper.isTraductionExistOfConcept(ds, concept.getIdConcept(), idTheso, idLang)) {
                nbrTermNonTraduit.getAndIncrement();
            }
        });
        return nbrTermNonTraduit.get();
    }

    private int getNbrTermTraduitWithoutGroup(HikariDataSource ds, String idTheso, String idLang) {
        TermHelper termHelper = new TermHelper();
        ArrayList<NodeUri> concepts = new ConceptHelper().getListConceptsWithoutGroup(ds, idTheso);
        AtomicInteger nbrTermNonTraduit = new AtomicInteger();
        concepts.forEach(concept -> {
            if(!termHelper.isTraductionExistOfConcept(ds, concept.getIdConcept(), idTheso, idLang)) {
                nbrTermNonTraduit.getAndIncrement();
            }
        });
        return nbrTermNonTraduit.get();
    }

    public List<ConceptStatisticData> searchAllConceptsByThesaurus(StatistiqueBean statistiqueBean, Connect connect,
                         String idTheso, String idLang, Date dateDebut, Date dateFin, String collectionId, String nbrResultat) {

        List<ConceptStatisticData> result = new ArrayList<>();

        if (dateDebut == null && dateFin != null) {
            statistiqueBean.showMessage(FacesMessage.SEVERITY_ERROR, "Il faut préciser la date de fin !");
            return result;
        }

        if (dateDebut != null && dateFin != null && dateDebut.after(dateFin)) {
            statistiqueBean.showMessage(FacesMessage.SEVERITY_ERROR, "La date de début est plus récente que la date de fin !");
            return result;
        }

        if (dateDebut != null && dateFin == null) {
            dateFin = new Date();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String debut = dateDebut == null ? null : formatter.format(dateDebut); 
        String fin = dateFin == null ? null : formatter.format(dateFin); 
        int limit;
        try {
            limit = Integer.parseInt(nbrResultat);
        } catch (Exception e) {
            limit = 100;
        }
        if(dateDebut == null || dateFin == null) {
            if(collectionId == null || collectionId.isEmpty()) {
                result = new StatisticHelper().getStatConcept(connect.getPoolConnexion(), idTheso, idLang, limit);                 
            } else {
                result = new StatisticHelper().getStatConceptLimitCollection(connect.getPoolConnexion(), idTheso, collectionId, idLang, limit);
            }
        } else {
            result = new StatisticHelper().getStatConceptByDateAndCollection(
                    connect.getPoolConnexion(), idTheso, collectionId, idLang,
                    debut, fin, limit);
        }
       
        
            // désactivé par Miled, la focntion ne renvoie pas le bon résultat 
   /*         result = new ConceptHelper().searchAllCondidats(connect.getPoolConnexion(), idTheso, 
                    idLang, debut, fin, collectionId, nbrResultat);*/



        /*    TermHelper termHelper = new TermHelper();

            result.forEach(concept -> {
                
                String label = termHelper.getThisTerm(connect.getPoolConnexion(), concept.getIdConcept(),
                        idTheso, idLang).getLexical_value();
                
                concept.setLabel(label);

                if (termHelper.isPrefLabelExist(connect.getPoolConnexion(), label, idTheso, idLang)) {
                    concept.setType("skos:prefLabel");
                } else {
                    concept.setType("skos:altLabel");
                }
            });*/


        return result;
    }

}
