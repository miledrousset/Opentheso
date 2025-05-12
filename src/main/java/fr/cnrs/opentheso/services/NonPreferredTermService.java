package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.NonPreferredTerm;
import fr.cnrs.opentheso.entites.NonPreferredTermHistorique;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.NonPreferredTermHistoriqueRepository;
import fr.cnrs.opentheso.repositories.NonPreferredTermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class NonPreferredTermService {

    private final NonPreferredTermRepository nonPreferredTermRepository;
    private final NonPreferredTermHistoriqueRepository nonPreferredTermHistoriqueRepository;


    @Transactional
    public void addNonPreferredTerm(Term term, int idUser) {

        log.info("Enregistrement du nouveau synonyme");
        nonPreferredTermRepository.save(NonPreferredTerm.builder()
                .idTerm(term.getIdTerm())
                .lexicalValue(term.getLexicalValue())
                .lang(term.getLang())
                .idThesaurus(term.getIdThesaurus())
                .source(term.getSource())
                .status(term.getStatus())
                .hiden(term.isHidden())
                .build());

        saveTrace(term.getIdTerm(), term.getLexicalValue(), term.getIdTerm(), term.getLang(), idUser, term.isHidden(), "ADD");
    }

    public void deleteNonPreferredTerm(String idTerm, String idLang, String lexicalValue, String idTheso, int idUser) {

        log.info("Suppression du synonyme du term " + idTerm);
        nonPreferredTermRepository.deleteByIdThesaurusAndIdTermAndLexicalValueAndLang(idTheso, idTerm, lexicalValue, idLang);

        saveTrace(idTerm, lexicalValue, idTheso, idLang, idUser, false, "delete");
    }

    public List<String> getNonPreferredTermValue(String idThesaurus, String idTerm, String lang) {
        var nonPreferredTerms = nonPreferredTermRepository.findAllByIdThesaurusAndIdTermAndLangAndHidenNot(idThesaurus, idTerm, lang, true);
        if (CollectionUtils.isNotEmpty(nonPreferredTerms)) {
            return nonPreferredTerms.stream().map(NonPreferredTerm::getLexicalValue).toList();
        } else {
            return List.of();
        }
    }

    public boolean updateNonPreferredTerm(String oldValue, String newValue, String idTerm, String idLang, String idTheso, boolean isHidden, int idUser) {

        log.info("Début du mise à jour du synonyme " + idTerm);
        var nonPreferredTerm = nonPreferredTermRepository.findByIdTermAndLexicalValueAndLangAndIdThesaurus(idTerm, oldValue, idLang, idTheso);

        if (nonPreferredTerm.isPresent()) {
            log.info("Mise à jour du synonyme");
            nonPreferredTerm.get().setLexicalValue(newValue);
            nonPreferredTerm.get().setHiden(isHidden);
            nonPreferredTerm.get().setModified(new Date());
            nonPreferredTermRepository.save(nonPreferredTerm.get());

            saveTrace(idTerm, newValue, idTheso, idLang, idUser, isHidden, "update");
            return true;
        }
        return false;
    }


    public boolean updateStatusNonPreferredTerm(String idTerm, String value, String idLang, String idTheso, boolean isHidden, int idUser) {
        log.info("Début du mise à jour du status du synonyme " + idTerm);
        var nonPreferredTerm = nonPreferredTermRepository.findByIdTermAndLexicalValueAndLangAndIdThesaurus(idTerm, value, idLang, idTheso);

        if (nonPreferredTerm.isPresent()) {
            log.info("Mise à jour du status synonyme");
            nonPreferredTerm.get().setHiden(isHidden);
            nonPreferredTerm.get().setModified(new Date());
            nonPreferredTermRepository.save(nonPreferredTerm.get());

            saveTrace(idTerm, value, idTheso, idLang, idUser, isHidden, "update");
            return true;
        }
        return false;
    }

    private void saveTrace(String idTerm, String lexicalValue, String idTheso, String idLang, int idUser, boolean isHidden, String action) {
        log.info("Enregistrement du trace de l'action du mise à jour");
        nonPreferredTermHistoriqueRepository.save(NonPreferredTermHistorique.builder()
                .idTerm(idTerm)
                .lexicalValue(lexicalValue)
                .idThesaurus(idTheso)
                .lang(idLang)
                .idUser(idUser)
                .hiden(isHidden)
                .action(action)
                .status("")
                .source("")
                .build());
    }

    public List<NodeEM> getAllNonPreferredTerms(String idConcept, String idThesaurus) {
        try {
            return nonPreferredTermRepository.findAllNodeEMByConcept(idConcept, idThesaurus);
        } catch (Exception e) {
            log.error("Error while getting NonPreferredTerms for concept: " + idConcept, e);
            return new ArrayList<>();
        }
    }

    public List<NodeEM> getNonPreferredTerms(String idConcept, String idThesaurus, String idLang) {
        try {
            return nonPreferredTermRepository.findNodeEMByConceptAndLang(idConcept, idThesaurus, idLang);
        } catch (Exception e) {
            log.error("Error while getting NonPreferredTerm of Term: " + idConcept, e);
            return new ArrayList<>();
        }
    }

}
