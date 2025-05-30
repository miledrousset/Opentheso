package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.entites.ThesaurusLabel;
import fr.cnrs.opentheso.entites.UserRoleOnlyOn;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.repositories.ExternalResourceRepository;
import fr.cnrs.opentheso.repositories.GraphViewExportedConceptBranchRepository;
import fr.cnrs.opentheso.repositories.LanguageIso639Repository;
import fr.cnrs.opentheso.repositories.NodeLabelRepository;
import fr.cnrs.opentheso.repositories.RoutineMailRepository;
import fr.cnrs.opentheso.repositories.ThesaurusAlignementSourceRepository;
import fr.cnrs.opentheso.repositories.ThesaurusArrayRepository;
import fr.cnrs.opentheso.repositories.ThesaurusDcTermRepository;
import fr.cnrs.opentheso.repositories.ThesaurusHomePageRepository;
import fr.cnrs.opentheso.repositories.ThesaurusLabelRepository;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@AllArgsConstructor
public class ThesaurusService {

    private final ThesaurusRepository thesaurusRepository;
    private final ThesaurusLabelRepository thesaurusLabelRepository;
    private final LanguageIso639Repository languageIso639Repository;
    private final ThesaurusHomePageRepository thesaurusHomePageRepository;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    private final ExternalResourceRepository externalResourceRepository;
    private final ThesaurusDcTermRepository thesaurusDcTermRepository;
    private final ThesaurusArrayRepository thesaurusArrayRepository;
    private final NodeLabelRepository nodeLabelRepository;
    private final RoutineMailRepository routineMailRepository;
    private final ThesaurusAlignementSourceRepository thesaurusAlignementRepository;
    private final GraphViewExportedConceptBranchRepository graphViewExportedConceptBranchRepository;
    private final ThesaurusAlignementSourceRepository thesaurusAlignementSourceRepository;

    private final TermService termService;
    private final GroupService groupService;
    private final ConceptService conceptService;
    private final CandidatService candidatService;
    private final GpsService gpsService;
    private final AlignmentService alignmentService;
    private final PropositionService propositionService;
    private final RelationService relationService;
    private final ImageService imageService;
    private final PreferenceService preferenceService;
    private final NoteService noteService;
    private final UserRoleOnlyOnRepository userRoleOnlyOnRepository;


    public Thesaurus getThesaurusById(String idThesaurus) {

        log.info("Rechercher un thésaurus à partir de son id {}", idThesaurus);
        var thesaurus = thesaurusRepository.findById(idThesaurus);
        if (thesaurus.isEmpty()) {
            log.error("Le thésaurus avec id {} n'existe pas dans la base de données", idThesaurus);
            return null;
        }

        return thesaurus.get();
    }

    public List<String> getIsoLanguagesOfThesaurus(String idThesaurus) {

        log.info("Recherche des langues ISO pour le thésaurus '{}'", idThesaurus);
        var langues = thesaurusLabelRepository.findDistinctLangByIdThesaurus(idThesaurus);

        if (CollectionUtils.isEmpty(langues)) {
            log.warn("Aucune langue trouvée pour le thésaurus '{}'", idThesaurus);
            return Collections.emptyList();
        }

        log.info("{} langue(s) trouvée(s) pour le thésaurus '{}': {}", langues.size(), idThesaurus, langues);
        return langues;
    }

    public List<NodeIdValue> getThesaurusOfProject(int idGroup, String idLang) {

        log.info("Recherche de la liste des thésaurus définis dans le projet {} et avec la langue {}", idGroup, idLang);
        var thesaurusList = userGroupThesaurusRepository.findAllByIdGroup(idGroup);
        if (CollectionUtils.isEmpty(thesaurusList)) {
            log.info("Aucun thésaurus n'est trouvé dans le group {}", idGroup);
            return List.of();
        }

        log.info("{} thésaurus trouvés dans le group {}", thesaurusList.size(), idGroup);
        return thesaurusList.stream()
                .map(thesaurus -> {
                    var idLangTemp = preferenceService.getWorkLanguageOfThesaurus(thesaurus.getIdThesaurus());
                    idLangTemp = StringUtils.isEmpty(idLangTemp) ? idLang : idLangTemp;

                    var thesaurusLabel = thesaurusLabelRepository.findByIdThesaurusAndLang(thesaurus.getIdThesaurus(), idLangTemp);
                    var value = thesaurusLabel.isPresent() ? thesaurusLabel.get().getTitle() : "";

                    var thesaurusDetails = thesaurusRepository.findById(thesaurus.getIdThesaurus());
                    var status = thesaurusDetails.isPresent() && thesaurusDetails.get().getIsPrivate();

                    return NodeIdValue.builder()
                            .id(thesaurus.getIdThesaurus())
                            .value(value)
                            .status(status)
                            .build();
                }
        ).toList();
    }

    public void deleteThesaurusTraduction(String idThesaurus, String idLang) {

        log.info("Suppression de la traduction en {} du thésaurus {}", idLang, idThesaurus);
        idThesaurus = fr.cnrs.opentheso.utils.StringUtils.convertString(idThesaurus);
        thesaurusLabelRepository.deleteByIdThesaurusAndLang(idThesaurus, idLang);
        log.info("Suppression terminé de la traduction en {} du thésaurus {}", idLang, idThesaurus);
    }

    public boolean isLanguageExistOfThesaurus(String idThesaurus, String idLang) {

        log.info("Vérification si la traduction en langue {} est présente pour le thésaurus {}", idLang, idThesaurus);
        var thesaurus = thesaurusLabelRepository.findByIdThesaurusAndLang(idThesaurus, idLang);
        log.info("La traduction en langue {} est présente pour le thésaurus {} : {}", idLang, idThesaurus, thesaurus.isPresent());
        return thesaurus.isEmpty();
    }

    public List<String> getAllUsedLanguagesOfThesaurus(String idThesaurus) {

        log.info("Rechercher les langues utilisées par les concepts du thésaurus {}", idThesaurus);
        var terms = termService.searchDistinctLangInThesaurus(idThesaurus);
        if (CollectionUtils.isEmpty(terms)) {
            log.info("Aucune langue n'est utilisé dans le thésaurus {}", idThesaurus);
            return List.of();
        }

        log.info("{} langues utilisées dans le thésaurus {}", terms.size(), idThesaurus);
        return terms;
    }

    public boolean isThesaurusExiste(String idThesaurus) {

        log.info("Vérifier si le thésaurus {} existe", idThesaurus);
        var thesaurus = thesaurusRepository.findById(idThesaurus);
        log.error("Le thésaurus id {} est trouvé ? {}", idThesaurus, thesaurus.isPresent());
        return thesaurus.isPresent();
    }

    public List<String> getAllIdOfThesaurus(boolean withPrivateThesaurus) {

        log.info("Recherche des id des thésaurus (avec les thésaurus privées : {})", withPrivateThesaurus);
        var thesaurus = withPrivateThesaurus ? thesaurusRepository.findAll() : thesaurusRepository.findAllByIsPrivateFalse();

        if (CollectionUtils.isEmpty(thesaurus)) {
            log.info("Aucun thésaurus trouvé !");
            return List.of();
        }

        log.info("{} thésaurus trouvés", thesaurus.size());
        return thesaurus.stream().map(Thesaurus::getIdThesaurus).toList();
    }

    public boolean updateIdArkOfThesaurus(String idThesaurus, String idArk) {

        log.info("Mise à jour de l'idArk du thésaurus id {} avec la valeur {}", idThesaurus, idArk);
        var thesaurus = thesaurusRepository.findById(idThesaurus);
        if (thesaurus.isEmpty()) {
            log.error("Le thésaurus avec id {} n'existe pas dans la base de données", idThesaurus);
            return true;
        }

        thesaurus.get().setIdArk(idArk);
        thesaurusRepository.save(thesaurus.get());
        log.info("Mise à jour thésaurus {} avec la nouvelle valeur d'idArk {}", idThesaurus, idArk);
        return false;
    }

    public List<NodeIdValue> getAllThesaurus(boolean withPrivateThesaurus) {

        log.info("Rechercher les thésaurus dans la langue source de chaque thésaurus");
        var tabIdThesaurus = getAllIdOfThesaurus(withPrivateThesaurus);
        return tabIdThesaurus.stream().map(idThesaurus ->
            NodeIdValue.builder()
                    .id(idThesaurus)
                    .value(getTitleOfThesaurus(idThesaurus, preferenceService.getWorkLanguageOfThesaurus(idThesaurus)))
                    .build()
        ).toList();
    }

    public String getTitleOfThesaurus(String idThesaurus, String idLang) {

        var thesaurusLabel = thesaurusLabelRepository.findByIdThesaurusAndLang(idThesaurus, idLang);
        if (thesaurusLabel.isEmpty()) {
            log.error("Aucun thésaurus trouvé avec l'id {}", idThesaurus);
            return "";
        }

        log.info("Le titre du thésaurus (id = {}) est {}", idThesaurus, thesaurusLabel.get().getTitle());
        return thesaurusLabel.get().getTitle();
    }

    public String addThesaurusRollBack() {

        log.info("Création d'un nouveau thésaurus");
        var thesaurusSeq = thesaurusRepository.getNextThesaurusSequenceValue();
        var idThesaurus = "th" + thesaurusSeq;
        while (isThesaurusExiste(idThesaurus)) {
            idThesaurus = "th" + ++thesaurusSeq;
        }
        log.info("Le nouveau id du thésaurus est {}", idThesaurus);

        var thesaurus = thesaurusRepository.save(Thesaurus.builder()
                .idThesaurus(idThesaurus)
                .idArk("")
                .isPrivate(false)
                .created(new Date())
                .modified(new Date())
                .build());

        log.info("Enregistrement terminé du nouveau thésaurus {}", thesaurus.getIdThesaurus());
        return thesaurus.getIdThesaurus();
    }

    public void setThesaurusVisibility(String idThesaurus, boolean isPrivateTheso) {

        log.info("Changement de la visibilité du thésaurus id {} en {}", idThesaurus, isPrivateTheso);
        thesaurusRepository.updateVisibility(idThesaurus, isPrivateTheso);
    }

    public List<NodeLangTheso> getAllUsedLanguagesOfThesaurusNode(String idThesaurus, String idLang) {

        log.info("Recherche des langues utilisées dans le thésaurus '{}'", idThesaurus);
        final var langue = StringUtils.isBlank(idLang) ? "fr" : idLang;

        var projections = thesaurusRepository.findAllUsedLanguagesOfThesaurus(idThesaurus);
        if (CollectionUtils.isEmpty(projections)) {
            log.info("Aucune langue est utilisée par le thésaurus {}", idThesaurus);
            return Collections.emptyList();
        }

        log.info("{} langue(s) utilisée(s) trouvée(s) pour le thésaurus '{}'", projections.size(), idThesaurus);
        return projections.stream()
                .map(element -> NodeLangTheso.builder()
                        .id(element.getId())
                        .code(element.getCode())
                        .codeFlag(element.getCodeFlag())
                        .labelTheso(element.getLabelTheso())
                        .value("fr".equalsIgnoreCase(langue) ? element.getFrenchName() : element.getEnglishName())
                        .build())
                .toList();
    }

    public String getIdThesaurusFromArkId(String arkId) {

        log.info("Recherche de l'identifiant du thésaurus à partir d'Ark id {}", arkId);
        var idThesaurus = thesaurusRepository.findIdThesaurusByArkId(arkId).orElse(null);
        log.info("L'id du thésaurus est {}", idThesaurus);
        return idThesaurus;
    }

    public NodeThesaurus getNodeThesaurus(String idThesaurus) {

        log.info("Recherche des détails du thésaurus id {}", idThesaurus);

        log.info("Recherche di thésaurus id {}", idThesaurus);
        var thesaurus = getThesaurusById(idThesaurus);
        if (thesaurus == null) {
            log.error("Aucun thésaurus n'est trouvé avec l'id {}", idThesaurus);
            return null;
        }

        log.info("Recherche des langues utilisées par le thésaurus id {}", idThesaurus);
        var listLangThesaurus = getLanguagesOfThesaurus(idThesaurus);

        return NodeThesaurus.builder()
                .idThesaurus(idThesaurus)
                .idArk(thesaurus.getIdArk())
                .listThesaurusTraduction(getTraductions(listLangThesaurus, idThesaurus))
                .build();
    }

    private List<fr.cnrs.opentheso.models.thesaurus.Thesaurus> getTraductions(List<LanguageIso639> listLangThesaurus, String idThesaurus) {

        log.info("Recherche des traductions du thésaurus id {}", idThesaurus);
        if (CollectionUtils.isEmpty(listLangThesaurus)) {
            log.info("Aucune traduction n'est disponible avec l'id {}", idThesaurus);
            return Collections.emptyList();
        } else {
            return listLangThesaurus.stream()
                    .map(element -> getByIdAndLang(idThesaurus, element.getIso6391()))
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    public fr.cnrs.opentheso.models.thesaurus.Thesaurus getByIdAndLang(String idThesaurus, String idLang) {

        log.info("Recherche du thésaurus id {} avec la langue {}", idThesaurus, idLang);
        var thesaurus = thesaurusRepository.getThesaurusByIdAndLang(idThesaurus, StringUtils.trimToEmpty(idLang));
        if (thesaurus.isEmpty()) {
            log.error("Aucun thésaurus n'existe avec l'id {} et la langue {}", idThesaurus, idLang);
            return null;
        }
        return thesaurus.get();
    }

    public List<LanguageIso639> getLanguagesOfThesaurus(String idThesaurus) {

        log.info("Recherche des langues utilisées dans le thésaurus '{}'", idThesaurus);
        var result = languageIso639Repository.findLanguagesByThesaurusId(idThesaurus);
        if (CollectionUtils.isEmpty(result)) {
            log.warn("Aucune langue trouvée pour le thésaurus '{}'", idThesaurus);
            return Collections.emptyList();
        }

        log.info("{} langue(s) trouvée(s) pour le thésaurus '{}'", result.size(), idThesaurus);
        return result.stream()
                .map(element ->
                    LanguageIso639.builder()
                            .iso6391(element.getIso6391())
                            .iso6392(element.getIso6392())
                            .englishName(element.getEnglishName())
                            .frenchName(element.getFrenchName())
                            .codePays(element.getCodePays())
                            .build()
                )
                .toList();
    }

    public void addThesaurusTraductionRollBack(fr.cnrs.opentheso.models.thesaurus.Thesaurus thesaurus) {

        var thesaurusToSave = addQuotes(thesaurus);
        log.info("Création d'une nouvelle traduction pour le thésaurus {}", thesaurusToSave.getId_thesaurus());
        thesaurusLabelRepository.save(ThesaurusLabel.builder()
                .idThesaurus(thesaurusToSave.getId_thesaurus())
                .lang(thesaurusToSave.getLanguage().trim())
                .title(thesaurusToSave.getTitle())
                .contributor(thesaurusToSave.getContributor())
                .coverage(thesaurusToSave.getCoverage())
                .creator(thesaurusToSave.getCreator())
                .description(thesaurusToSave.getDescription())
                .format(thesaurusToSave.getFormat())
                .publisher(thesaurusToSave.getPublisher())
                .relation(thesaurusToSave.getRelation())
                .rights(thesaurusToSave.getRights())
                .source(thesaurusToSave.getSource())
                .subject(thesaurusToSave.getSubject())
                .type(thesaurusToSave.getType())
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .build());
    }

    public void addThesaurusTraduction(fr.cnrs.opentheso.models.thesaurus.Thesaurus thesaurusToSave) {

        log.info("Ajout d'une traduction au thésaurus {}", thesaurusToSave.getId_thesaurus());
        var thesaurus = addQuotes(thesaurusToSave);
        thesaurusLabelRepository.save(ThesaurusLabel.builder()
                .idThesaurus(thesaurus.getId_thesaurus())
                .contributor(thesaurus.getContributor())
                .coverage(thesaurus.getCoverage())
                .creator(thesaurus.getCreator())
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .description(thesaurus.getDescription())
                .format(thesaurus.getFormat())
                .lang(thesaurus.getLanguage().trim())
                .publisher(thesaurus.getPublisher())
                .relation(thesaurus.getRelation())
                .rights(thesaurus.getRights())
                .source(thesaurus.getSource())
                .subject(thesaurus.getSubject())
                .title(thesaurus.getTitle())
                .type(thesaurus.getType())
                .build());
    }

    public boolean updateThesaurus(fr.cnrs.opentheso.models.thesaurus.Thesaurus thesaurus) {

        var thesaurusToSave = addQuotes(thesaurus);

        var thesaurusLabel = thesaurusLabelRepository.findByIdThesaurusAndLang(thesaurusToSave.getId_thesaurus(), thesaurusToSave.getLanguage());
        if (thesaurusLabel.isEmpty()) {
            log.error("Aucun thésaurus trouvé avec l'id {} et langue {}", thesaurusToSave.getId_thesaurus(), thesaurusToSave.getLanguage());
            return false;
        }

        thesaurusLabel.get().setContributor(thesaurusToSave.getContributor());
        thesaurusLabel.get().setCoverage(thesaurusToSave.getCoverage());
        thesaurusLabel.get().setCreator(thesaurusToSave.getCreator());
        thesaurusLabel.get().setModified(LocalDateTime.now());
        thesaurusLabel.get().setDescription(thesaurusToSave.getDescription());
        thesaurusLabel.get().setFormat(thesaurusToSave.getFormat());
        thesaurusLabel.get().setPublisher(thesaurusToSave.getPublisher());
        thesaurusLabel.get().setRelation(thesaurusToSave.getRelation());
        thesaurusLabel.get().setRights(thesaurusToSave.getRights());
        thesaurusLabel.get().setSource(thesaurusToSave.getSource());
        thesaurusLabel.get().setSubject(thesaurusToSave.getSubject());
        thesaurusLabel.get().setTitle(thesaurusToSave.getTitle());
        thesaurusLabel.get().setType(thesaurusToSave.getType());
        thesaurusLabelRepository.save(thesaurusLabel.get());
        log.info("Mise à jour de la traduction terminée correctement");
        return true;
    }

    private fr.cnrs.opentheso.models.thesaurus.Thesaurus addQuotes(fr.cnrs.opentheso.models.thesaurus.Thesaurus thesaurus) {

        log.info("Ajouter des cotes pour passer des données en JDBC");
        thesaurus.setContributor(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getContributor()));
        thesaurus.setCoverage(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getCoverage()));
        thesaurus.setCreator(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getCreator()));
        thesaurus.setDescription(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getDescription()));
        thesaurus.setFormat(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getFormat()));
        thesaurus.setPublisher(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getPublisher()));
        thesaurus.setRelation(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getRelation()));
        thesaurus.setRights(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getRights()));
        thesaurus.setSource(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getSource()));
        thesaurus.setSubject(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getSubject()));
        thesaurus.setTitle(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getTitle()));
        thesaurus.setType(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getType()));
        return thesaurus;
    }

    @Transactional
    public boolean cleaningThesaurus(String idThesaurus) {

        log.info("Nettoyer du thésaurus {} des espaces et des null", idThesaurus);
        termService.deleteTerm("", idThesaurus);
        groupService.deleteConceptGroupRollBack("", idThesaurus);
        groupService.cleanGroup();
        conceptService.cleanConcept();
        return true;
    }

    @Transactional
    public boolean deleteThesaurus(String idThesaurus) {

        log.info("Suppression du thésaurus id {}", idThesaurus);
        if (!isThesaurusExiste(idThesaurus)) {
            log.error("Le thésaurus id {} n'existe pas", idThesaurus);
            return false;
        }

        idThesaurus = fr.cnrs.opentheso.utils.StringUtils.convertString(idThesaurus);
        thesaurusRepository.deleteById(idThesaurus);
        thesaurusLabelRepository.deleteByIdThesaurus(idThesaurus);
        thesaurusHomePageRepository.deleteAllByIdTheso(idThesaurus);
        userGroupThesaurusRepository.deleteByIdThesaurus(idThesaurus);
        userRoleOnlyOnRepository.deleteByThesaurusIdThesaurus(idThesaurus);
        thesaurusAlignementSourceRepository.deleteAllByIdThesaurus(idThesaurus);
        thesaurusDcTermRepository.deleteAllByIdThesaurus(idThesaurus);
        thesaurusArrayRepository.deleteAllByIdThesaurus(idThesaurus);
        nodeLabelRepository.deleteAllByIdThesaurus(idThesaurus);
        thesaurusAlignementRepository.deleteAllByIdThesaurus(idThesaurus);
        graphViewExportedConceptBranchRepository.deleteAllByTopConceptThesaurusId(idThesaurus);
        routineMailRepository.deleteAllByIdThesaurus(idThesaurus);

        termService.deleteAllTermsInThesaurus(idThesaurus);

        groupService.deleteAllGroupsByThesaurus(idThesaurus);

        candidatService.deleteAllCandidatsByThesaurus(idThesaurus);

        preferenceService.deletePreferenceThesaurus(idThesaurus);

        gpsService.deleteGpsByThesaurus(idThesaurus);

        alignmentService.deleteAllAlignmentsByThesaurus(idThesaurus);

        propositionService.deleteByThesaurus(idThesaurus);

        relationService.deleteAllByThesaurus(idThesaurus);

        imageService.deleteImagesByThesaurus(idThesaurus);

        externalResourceRepository.deleteAllByIdThesaurus(idThesaurus);

        noteService.deleteByThesaurus(idThesaurus);

        conceptService.deleteByThesaurus(idThesaurus);
        return true;
    }

    public boolean changeIdOfThesaurus(String oldIdThesaurus, String newIdThesaurus) {

        log.info("Changement de l'id du thésaurus du {} vers {}", oldIdThesaurus, newIdThesaurus);
        if (isThesaurusExiste(newIdThesaurus)) {
            log.error("Un thésaurus avec l'id {} existe déjà !", newIdThesaurus);
            return false;
        }
        newIdThesaurus = fr.cnrs.opentheso.utils.StringUtils.convertString(newIdThesaurus);

        thesaurusRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        thesaurusLabelRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        thesaurusArrayRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        nodeLabelRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        thesaurusHomePageRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        userGroupThesaurusRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        userRoleOnlyOnRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        thesaurusAlignementSourceRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        thesaurusDcTermRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        thesaurusAlignementRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        graphViewExportedConceptBranchRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        routineMailRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);

        termService.updateThesaurusId(newIdThesaurus, oldIdThesaurus);

        groupService.updateThesaurusId(newIdThesaurus, oldIdThesaurus);

        gpsService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        candidatService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        preferenceService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        gpsService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        alignmentService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        propositionService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        relationService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        imageService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        externalResourceRepository.updateThesaurusId(newIdThesaurus, newIdThesaurus);

        noteService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        conceptService.updateThesaurusId(oldIdThesaurus, newIdThesaurus);

        return true;
    }

    public void deleteDroitByThesaurus(String idThesaurusToDelete) {

        userGroupThesaurusRepository.deleteByIdThesaurus(idThesaurusToDelete);
    }
}
