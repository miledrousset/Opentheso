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
            
            data.setCollection(group.getLexicalValue());

            data.setNotesNbr(new NoteHelper().getNbrNoteByGroupAndThesoAndLang(connect.getPoolConnexion(),
                    group.getConceptGroup().getIdgroup(), idTheso, idLang));

            data.setSynonymesNbr(new StatisticHelper().getNbDescOfGroup(connect.getPoolConnexion(), idTheso,
                    group.getConceptGroup().getIdgroup()));

            data.setConceptsNbr(new ConceptHelper().getCountOfConceptsOfGroup(connect.getPoolConnexion(), idTheso,
                    group.getConceptGroup().getIdgroup()));

            data.setTermesNonTraduitsNbr(getNbrTermNonTraduit(connect.getPoolConnexion(), group, idTheso, idLang));
            
            result.add(data);
        });

        // Ajout de la dernier ligne réservé aux concepts sans collection
        GenericStatistiqueData data = new GenericStatistiqueData();
        data.setCollection("Sans collection");
        data.setConceptsNbr(new ConceptHelper().getCountOfConceptsWithoutGroup(connect.getPoolConnexion(), idTheso));
        data.setNotesNbr(new NoteHelper().getNbrNoteThesoAndLangAndWithoutGroup(connect.getPoolConnexion(), idTheso, idLang));
        data.setSynonymesNbr(new StatisticHelper().getNbDescWithoutGroup(connect.getPoolConnexion(), idTheso));
        data.setTermesNonTraduitsNbr(getNbrTermNonTraduitWithoutGroup(connect.getPoolConnexion(), idTheso, idLang));
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

    private int getNbrTermNonTraduitWithoutGroup(HikariDataSource ds, String idTheso, String idLang) {
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

    public List<CanceptStatistiqueData> searchAllConceptsByThesaurus(StatistiqueBean statistiqueBean, Connect connect,
                         String idTheso, String idLang, Date dateDebut, Date dateFin, String collectionId, String nbrResultat) {

        List<CanceptStatistiqueData> result = new ArrayList<>();

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

        try {
            result = new ConceptHelper().searchAllCondidats(connect.getPoolConnexion(), idTheso, 
                    idLang, debut, fin, collectionId, nbrResultat);

            TermHelper termHelper = new TermHelper();

            result.forEach(concept -> {
                
                String label = termHelper.getThisTerm(connect.getPoolConnexion(), concept.getIdConcept(),
                        idTheso, idLang).getLexical_value();
                
                concept.setLabel(label);

                if (termHelper.isPrefLabelExist(connect.getPoolConnexion(), label, idTheso, idLang)) {
                    concept.setType("skos:prefLabel");
                } else {
                    concept.setType("skos:altLabel");
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
