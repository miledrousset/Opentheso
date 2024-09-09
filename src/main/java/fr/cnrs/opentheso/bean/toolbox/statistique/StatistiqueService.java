package fr.cnrs.opentheso.bean.toolbox.statistique;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.StatisticHelper;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StatistiqueService {

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private StatisticHelper statisticHelper;

    @Autowired
    private NoteHelper noteHelper;


    public List<GenericStatistiqueData> searchAllCollectionsByThesaurus(Connect connect, String idTheso, String idLang) {

        List<GenericStatistiqueData> result = new ArrayList<>();
        
        ArrayList<NodeGroup> listGroup = groupHelper.getListConceptGroup(connect.getPoolConnexion(), idTheso, idLang);

        listGroup.stream().forEach(group -> {
            GenericStatistiqueData data = new GenericStatistiqueData();
            data.setIdCollection(group.getConceptGroup().getIdgroup());
            data.setCollection(group.getLexicalValue());

            data.setNotesNbr(noteHelper.getNbrNoteByGroup(connect.getPoolConnexion(),
                    group.getConceptGroup().getIdgroup(), idTheso, idLang));

            data.setSynonymesNbr(statisticHelper.getNbSynonymesByGroup(connect.getPoolConnexion(), idTheso,
                    group.getConceptGroup().getIdgroup(), idLang));

            data.setConceptsNbr(conceptHelper.getCountOfConceptsOfGroup(connect.getPoolConnexion(), idTheso,
                    group.getConceptGroup().getIdgroup()));

            data.setTermesNonTraduitsNbr(
                    data.getConceptsNbr() - 
                    statisticHelper.getNbTradOfGroup(
                    connect.getPoolConnexion(), idTheso, group.getConceptGroup().getIdgroup(), idLang));//getNbrTermNonTraduit(connect.getPoolConnexion(), group, idTheso, idLang));

            data.setWikidataAlignNbr(statisticHelper.getNbAlignWikidata(
                    connect.getPoolConnexion(), idTheso, group.getConceptGroup().getIdgroup()));  
            
            data.setTotalAlignment(statisticHelper.getNbAlign(
                    connect.getPoolConnexion(), idTheso, group.getConceptGroup().getIdgroup()));              
            
            result.add(data);
        });

        // Ajout de la dernier ligne réservé aux concepts sans collection
        GenericStatistiqueData data = new GenericStatistiqueData();
        data.setCollection("Sans collection");
        data.setConceptsNbr(conceptHelper.getCountOfConceptsSansGroup(connect.getPoolConnexion(), idTheso));
        data.setNotesNbr(noteHelper.getNbrNoteSansGroup(connect.getPoolConnexion(), idTheso, idLang));
        data.setSynonymesNbr(statisticHelper.getNbDesSynonimeSansGroup(connect.getPoolConnexion(), idTheso, idLang));
        data.setTermesNonTraduitsNbr(data.getConceptsNbr() - statisticHelper.getNbTradWithoutGroup(connect.getPoolConnexion(), idTheso, idLang));
        
        data.setWikidataAlignNbr(statisticHelper.getNbAlignWikidata(connect.getPoolConnexion(), idTheso, null));  
        data.setTotalAlignment(statisticHelper.getNbAlign(connect.getPoolConnexion(), idTheso, null));          
        
        result.add(data);

        return result;

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
        int limit;
        try {
            limit = Integer.parseInt(nbrResultat);
        } catch (Exception e) {
            limit = 100;
        }
        if(dateDebut == null || dateFin == null) {
            if(collectionId == null || collectionId.isEmpty()) {
                result = statisticHelper.getStatConcept(connect.getPoolConnexion(), idTheso, idLang, limit);
            } else {
                result = statisticHelper.getStatConceptLimitCollection(connect.getPoolConnexion(), idTheso, collectionId, idLang, limit);
            }
        } else {
            result = statisticHelper.getStatConceptByDateAndCollection(
                    connect.getPoolConnexion(), idTheso, collectionId, idLang,
                    dateDebut.toString(), dateFin.toString(), limit);
        }
        return result;
    }

}
