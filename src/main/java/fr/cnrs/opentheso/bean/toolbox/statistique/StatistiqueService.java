package fr.cnrs.opentheso.bean.toolbox.statistique;

import fr.cnrs.opentheso.entites.Alignement;
import fr.cnrs.opentheso.entites.Concept;
import fr.cnrs.opentheso.models.ConceptGroupProjection;
import fr.cnrs.opentheso.repositories.AlignementRepository;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.ConceptStatusRepository;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Data
@Service
public class StatistiqueService {

    private AlignementRepository alignementRepository;
    private ConceptRepository conceptRepository;
    private ConceptStatusRepository conceptStatusRepository;
    private GroupHelper groupHelper;
    private NoteHelper noteHelper;


    public StatistiqueService(AlignementRepository alignementRepository,
                              GroupHelper groupHelper,
                              ConceptRepository conceptRepository,
                              ConceptStatusRepository conceptStatusRepository,
                              NoteHelper noteHelper) {

        this.alignementRepository = alignementRepository;
        this.groupHelper = groupHelper;
        this.noteHelper = noteHelper;
        this.conceptRepository = conceptRepository;
        this.conceptStatusRepository = conceptStatusRepository;
    }

    public List<GenericStatistiqueData> searchAllCollectionsByThesaurus(String idTheso, String idLang) {

        List<GenericStatistiqueData> result = new ArrayList<>();
        
        var listGroup = groupHelper.getListConceptGroup(idTheso, idLang);

        listGroup.forEach(group -> {

            var noteNbr = noteHelper.getNbrNoteByGroup(group.getConceptGroup().getIdgroup(), idTheso, idLang);
            var conceptNbr = conceptStatusRepository.countConceptsInGroup(idTheso, group.getConceptGroup().getIdgroup());
            var traductionOfGroupNbr = conceptStatusRepository.countNonPreferredTermsByLangAndGroup(idTheso,
                    group.getConceptGroup().getIdgroup(), idLang);
            var WikidataAlignNbr = getNbAlignWikidata(idTheso, group.getConceptGroup().getIdgroup());
            var totalAlignmentNbr = getAlignementsSize(idTheso, group.getConceptGroup().getIdgroup()).size();
            var termesNonTraduitsNbr = conceptNbr - traductionOfGroupNbr;
            
            result.add(GenericStatistiqueData.builder()
                    .idCollection(group.getConceptGroup().getIdgroup())
                    .collection(group.getLexicalValue())
                    .notesNbr(noteNbr)
                    .synonymesNbr(traductionOfGroupNbr)
                    .conceptsNbr(conceptNbr)
                    .termesNonTraduitsNbr(conceptNbr - traductionOfGroupNbr)
                    .wikidataAlignNbr(WikidataAlignNbr)
                    .totalAlignment(totalAlignmentNbr)
                    .termesNonTraduitsNbr(termesNonTraduitsNbr)
                    .build());
        });

        var conceptNbr = conceptStatusRepository.countConceptsWithoutGroup(idTheso);
        result.add(GenericStatistiqueData.builder()
                .collection("Sans collection")
                .conceptsNbr(conceptNbr)
                .notesNbr(noteHelper.getNbrNoteSansGroup(idTheso, idLang))
                .synonymesNbr(conceptStatusRepository.countNonPreferredTermsNotInGroup(idTheso, idLang))
                .termesNonTraduitsNbr(conceptNbr - conceptStatusRepository.countConceptsWithoutGroupByLangAndThesaurus(idTheso, idLang))
                .wikidataAlignNbr(getNbAlignWikidata(idTheso, null))
                .totalAlignment(getAlignementsSize(idTheso, null).size())
                .build());

        return result;
    }

    public int getNbAlignWikidata(String thesaurusId, String groupId) {
        return getAlignementsSize(thesaurusId, groupId).stream()
                .filter(element -> StringUtils.isNotEmpty(element.getUriTarget()) && element.getUriTarget().contains("wikidata.org"))
                .toList()
                .size();
    }

    private List<Alignement> getAlignementsSize(String thesaurusId, String groupId) {

        var alignements = StringUtils.isEmpty(groupId)
                ? alignementRepository.findAlignementsNotInConceptGroup(thesaurusId)
                : alignementRepository.findAlignementsByGroupAndThesaurus(groupId, thesaurusId);

        return CollectionUtils.isNotEmpty(alignements) ? alignements : List.of();
    }

    public List<ConceptStatisticData> searchAllConceptsByThesaurus(StatistiqueBean statistiqueBean, String idTheso,
                                                                   String idLang, Date dateDebut, Date dateFin,
                                                                   String collectionId, String nbrResultat) {

        if (ObjectUtils.isEmpty(dateDebut) && !ObjectUtils.isEmpty(dateFin)) {
            statistiqueBean.showMessage(FacesMessage.SEVERITY_ERROR, "Il faut préciser la date de fin !");
            return List.of();
        }

        if (!ObjectUtils.isEmpty(dateDebut) && !ObjectUtils.isEmpty(dateFin) && dateDebut.after(dateFin)) {
            statistiqueBean.showMessage(FacesMessage.SEVERITY_ERROR, "La date de début est plus récente que la date de fin !");
            return List.of();
        }

        if (!ObjectUtils.isEmpty(dateDebut) && ObjectUtils.isEmpty(dateFin)) {
            dateFin = new Date();
        }

        int limit;
        try {
            limit = Integer.parseInt(nbrResultat);
        } catch (Exception e) {
            limit = 100;
        }

        if(ObjectUtils.isEmpty(dateDebut) || ObjectUtils.isEmpty(dateFin)) {

            var result = StringUtils.isEmpty(collectionId)
                    ? conceptStatusRepository.findRecentConceptsByLangAndThesaurus(idTheso, idLang, limit)
                    : conceptStatusRepository.findConceptsByGroupAndLang(idTheso, idLang, collectionId, limit);
            return conceptStatisticDataMapper(result);
        } else {
            var result = (StringUtils.isEmpty(collectionId))
                ? conceptStatusRepository.findConceptsModifiedBetween(idTheso, idLang, dateDebut, dateFin, limit)
                : conceptStatusRepository.findConceptsByGroupLangDate(idTheso, idLang, collectionId, dateDebut, dateFin, limit);
            return conceptStatisticDataMapper(result);
        }
    }

    public int countValidConceptsByThesaurus(String idThesaurus) {
        return conceptStatusRepository.countValidConceptsByThesaurus(idThesaurus);
    }

    public List<Concept> findAllByThesaurusIdThesaurusAndStatus(String idThesaurus, String status) {
        return conceptRepository.findAllByThesaurusIdThesaurusAndStatus(idThesaurus, status);
    }

    private List<ConceptStatisticData> conceptStatisticDataMapper(List<ConceptGroupProjection> conceptGroupProjectionsList) {

        var dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        return CollectionUtils.isEmpty(conceptGroupProjectionsList)
                ? List.of()
                : conceptGroupProjectionsList.stream()
                    .map(element -> ConceptStatisticData.builder()
                        .idConcept(element.getIdConcept())
                        .dateCreation(ObjectUtils.isEmpty(element.getCreated()) ? null : dataFormat.format(element.getCreated()))
                        .dateModification(ObjectUtils.isEmpty(element.getModified()) ? null : dataFormat.format(element.getModified()))
                        .label(element.getLexicalValue())
                        .utilisateur(element.getUsername())
                        .type("skos:prefLabel")
                        .build())
                    .toList();
    }

}
