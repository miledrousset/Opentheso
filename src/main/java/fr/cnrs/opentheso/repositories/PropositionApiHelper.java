package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.models.propositions.PropositionDao;
import fr.cnrs.opentheso.models.propositions.PropositionDetailDao;
import fr.cnrs.opentheso.repositories.propositions.PropositionDetailHelper;
import fr.cnrs.opentheso.repositories.propositions.PropositionHelper;
import fr.cnrs.opentheso.models.propositions.PropositionActionEnum;
import fr.cnrs.opentheso.models.propositions.PropositionCategoryEnum;
import fr.cnrs.opentheso.models.propositions.PropositionFromApi;
import fr.cnrs.opentheso.models.propositions.PropositionStatusEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class PropositionApiHelper {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private PropositionDetailHelper propositionDetailHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private PropositionHelper propositionHelper;



    public void createProposition(HikariDataSource connexion, PropositionFromApi proposition, int userId) {

        var user = userHelper.getUser(connexion, userId);
        var thesaurusLang = preferencesHelper.getWorkLanguageOfTheso(connexion, proposition.getIdTheso());

        int propositionId = saveProposition(connexion, proposition, thesaurusLang, user);

        saveTerme(proposition, connexion, propositionId, thesaurusLang);

        if (CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            saveTraductions(proposition, propositionId, connexion, thesaurusLang);
        }

        if (!CollectionUtils.isEmpty(proposition.getSynonymsProp())) {
            saveSynonymes(proposition, propositionId, connexion);
        }

        if (CollectionUtils.isNotEmpty(proposition.getNotes())) {
            for (NotePropBean note : proposition.getNotes()) {
                noteManagement(connexion, propositionId, note, PropositionCategoryEnum.NOTE.name());
            }
        }

        if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            for (NotePropBean definition : proposition.getDefinitions()) {
                noteManagement(connexion, propositionId, definition, PropositionCategoryEnum.DEFINITION.name());
            }
        }
    }

    private Integer saveProposition(HikariDataSource ds, PropositionFromApi proposition, String thesaurusLang, NodeUser user) {

        var thesaurusName = thesaurusHelper.getTitleOfThesaurus(ds, proposition.getIdTheso(), thesaurusLang);

        var propositionDao = PropositionDao.builder()
                .nom(user.getName())
                .email(user.getMail())
                .commentaire(proposition.getCommentaire())
                .datePublication(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()))
                .idConcept(proposition.getConceptID())
                .idTheso(proposition.getIdTheso())
                .lang(thesaurusLang)
                .status(PropositionStatusEnum.ENVOYER.name())
                .thesoName(thesaurusName)
                .build();
        return propositionHelper.createNewProposition(ds, propositionDao);
    }

    private void saveTerme(PropositionFromApi proposition, HikariDataSource ds, Integer propositionId, String thesaurusLang) {
        var prefLabel = proposition.getTraductionsProp().stream()
                .filter(element -> element.getLang().equals(thesaurusLang))
                .findFirst();
        if (prefLabel.isPresent()) {
            var propositionDetail = PropositionDetailDao.builder()
                    .idProposition(propositionId)
                    .action(PropositionActionEnum.UPDATE.name())
                    .categorie(PropositionCategoryEnum.NOM.name())
                    .lang(prefLabel.get().getLang())
                    .value(prefLabel.get().getLexicalValue())
                    .oldValue(prefLabel.get().getOldValue())
                    .build();
            propositionDetailHelper.createNewPropositionDetail(ds, propositionDetail);
        }
    }

    private void saveTraductions(PropositionFromApi proposition, Integer propositionId, HikariDataSource ds, String thesaurusLang) {

        var traduction = proposition.getTraductionsProp().stream()
                .filter(element -> !element.getLang().equals(thesaurusLang))
                .findFirst();
        if (traduction.isPresent()) {
            if (traduction.get().isToAdd() || traduction.get().isToUpdate() || traduction.get().isToRemove()) {
                var propositionDetail = new PropositionDetailDao();
                PropositionActionEnum action;
                if (traduction.get().isToAdd()) {
                    action = PropositionActionEnum.ADD;
                    propositionDetail.setOldValue(traduction.get().getLexicalValue());
                } else if (traduction.get().isToRemove()) {
                    action = PropositionActionEnum.DELETE;
                    propositionDetail.setOldValue(traduction.get().getOldValue());
                } else {
                    action = PropositionActionEnum.UPDATE;
                    propositionDetail.setOldValue(traduction.get().getOldValue());
                }

                var idTerm = termHelper.getIdTermOfConcept(ds, proposition.getConceptID(), proposition.getIdTheso());
                if (StringUtils.isNotEmpty(idTerm)) {
                    propositionDetail.setIdProposition(propositionId);
                    propositionDetail.setAction(action.name());
                    propositionDetail.setCategorie(PropositionCategoryEnum.TRADUCTION.name());
                    propositionDetail.setLang(traduction.get().getLang());
                    propositionDetail.setValue(traduction.get().getLexicalValue());
                    propositionDetail.setIdTerm(idTerm);
                    propositionDetailHelper.createNewPropositionDetail(ds, propositionDetail);
                }
            }
        }
    }

    private void saveSynonymes(PropositionFromApi proposition, Integer propositionId, HikariDataSource ds) {
        for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
            if (synonymProp.isToAdd() || synonymProp.isToUpdate() || synonymProp.isToRemove()) {
                PropositionActionEnum action;
                if (synonymProp.isToAdd()) {
                    action = PropositionActionEnum.ADD;
                } else if (synonymProp.isToRemove()) {
                    action = PropositionActionEnum.DELETE;
                } else {
                    action = PropositionActionEnum.UPDATE;
                }

                var propositionDetail = PropositionDetailDao.builder()
                        .idProposition(propositionId)
                        .action(action.name())
                        .categorie(PropositionCategoryEnum.SYNONYME.name())
                        .lang(synonymProp.getLang())
                        .value(synonymProp.getLexicalValue())
                        .oldValue(synonymProp.getOldValue())
                        .status(synonymProp.getStatus())
                        .hiden(synonymProp.isHiden())
                        .idTerm(synonymProp.getIdTerm())
                        .build();
                propositionDetailHelper.createNewPropositionDetail(ds, propositionDetail);
            }
        }
    }

    private void noteManagement(HikariDataSource connexion, int propositionID, NotePropBean note, String category) {
        if (note.isToAdd() || note.isToUpdate() || note.isToRemove()) {
            var propositionDetail = new PropositionDetailDao();
            PropositionActionEnum action;
            if (note.isToAdd()) {
                action = PropositionActionEnum.ADD;
                propositionDetail.setOldValue(note.getLexicalValue());
            } else if (note.isToRemove()) {
                action = PropositionActionEnum.DELETE;
                propositionDetail.setOldValue(note.getOldValue());
            } else {
                action = PropositionActionEnum.UPDATE;
                propositionDetail.setOldValue(note.getOldValue());
            }

            propositionDetail.setIdProposition(propositionID);
            propositionDetail.setAction(action.name());
            propositionDetail.setCategorie(category);
            propositionDetail.setLang(note.getLang());
            propositionDetail.setValue(note.getLexicalValue());
            propositionDetail.setIdTerm(note.getIdTerm());
            propositionDetailHelper.createNewPropositionDetail(connexion, propositionDetail);
        }
    }

}
