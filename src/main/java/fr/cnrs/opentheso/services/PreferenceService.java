package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.repositories.PreferencesRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class PreferenceService {

    private final PreferencesRepository preferencesRepository;


    public void initPreferences(String idThesaurus, String workLanguage) {

        log.info("Initialisation des préférences pour le thésaurus {}", idThesaurus);
        preferencesRepository.save(Preferences.builder()
                .idThesaurus(idThesaurus)
                .sourceLang(workLanguage)
                .preferredName(idThesaurus)
                .build());
    }

    public void addPreference(Preferences preference, String idThesaurus) {

        log.info("Formatage des données de preférence");
        var preferenceToSave = normalizePreferencesDatas(preference);

        log.info("Ajout des préférences du thésaurus {} dans la base de données", idThesaurus);
        preferencesRepository.save(preferenceToSave);
    }

    public void updateAllPreferenceUser(Preferences preference) {

        log.info("Mise à jour des preferences du thésaurus {}", preference.getIdThesaurus());
        preferencesRepository.save(preference);
    }

    private Preferences normalizePreferencesDatas(Preferences preference) {

        if (StringUtils.isNotEmpty(preference.getCheminSite())) {
            log.info("Formatage de la valeur de chemin du site {}", preference.getCheminSite());
            if (!preference.getCheminSite().substring(preference.getCheminSite().length() - 1).equalsIgnoreCase("/")) {
                preference.setCheminSite(preference.getCheminSite() + "/");
            }
        }
        if (StringUtils.isNotEmpty(preference.getServerArk())) {
            log.info("Formatage de la valeur du serveur Ark {}", preference.getServerArk());
            if (!preference.getServerArk().substring(preference.getServerArk().length() - 1).equalsIgnoreCase("/")) {
                preference.setServerArk(preference.getServerArk() + "/");
            }
        }

        return preference;
    }

    public Preferences getThesaurusPreferences(String idThesaurus) {

        log.info("Rechercher des paramètres du thésaurus {}", idThesaurus);
        var preference = preferencesRepository.findByIdThesaurus(idThesaurus);

        if (preference.isEmpty()) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", idThesaurus);
            return null;
        }

        log.info("Paramètres trouvés pour le thésaurus id {}", idThesaurus);
        return preference.get();
    }

    public void setUseArk(String idThesaurus, boolean useArk) {

        log.info("Mise à jour de UseArk pour le thésaurus {}", idThesaurus);
        var preference = preferencesRepository.findByIdThesaurus(idThesaurus);

        if (preference.isEmpty()) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", idThesaurus);
            return;
        }

        preference.get().setUseArk(useArk);
        preferencesRepository.save(preference.get());
        log.info("Mise à jour de UseArk pour le thésaurus {} terminée", idThesaurus);
    }

    public void setUseArkLocal(String idThesaurus, boolean useArk) {

        log.info("Mise à jour de UseArkLocal pour le thésaurus {}", idThesaurus);
        var preference = preferencesRepository.findByIdThesaurus(idThesaurus);

        if (preference.isEmpty()) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", idThesaurus);
            return;
        }

        preference.get().setUseArkLocal(useArk);
        preferencesRepository.save(preference.get());
        log.info("Mise à jour de UseArkLocal pour le thésaurus {} terminée", idThesaurus);
    }


    public String getWorkLanguageOfThesaurus(String idThesaurus) {

        log.info("Rechercher de la langue source du thésaurus {}", idThesaurus);
        var preference = preferencesRepository.findByIdThesaurus(idThesaurus);

        if (preference.isEmpty()) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", idThesaurus);
            return null ;
        }

        log.info("La langue source du thésaurus {}  est {}", idThesaurus, preference.get().getSourceLang());
        return preference.get().getSourceLang();
    }

    public boolean setWorkLanguageOfThesaurus(String idLang, String idThesaurus) {

        log.info("Début de la mise à jour de la source language pour le thésaurus {}", idThesaurus);
        var preference = preferencesRepository.findByIdThesaurus(idThesaurus);

        if (preference.isEmpty()) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", idThesaurus);
            return false;
        }

        preference.get().setSourceLang(idLang);
        preferencesRepository.save(preference.get());
        log.info("Mise à jour de la source language terminé pour le thésaurus {}", idThesaurus);
        return true;
    }

    public void setUseHandle(String idThesaurus, boolean useHandle) {

        log.info("Mise à jour de 'use handle' pour le thésaurus {}", idThesaurus);
        var preference = preferencesRepository.findByIdThesaurus(idThesaurus);
        if (preference.isEmpty()) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", idThesaurus);
            return;
        }

        preference.get().setUseHandle(useHandle);
        preferencesRepository.save(preference.get());
        log.info("Mise à jour terminé de 'use handle' pour le thésaurus {}", idThesaurus);
    }

    public void deletePreferenceThesaurus(String idThesaurus) {

        log.info("Suppression des préférences du thésaurus id {}", idThesaurus);
        preferencesRepository.deleteByIdThesaurus(idThesaurus);
    }

    public void updateThesaurusId(String oldIdThesaurus, String newIdThesaurus) {

        log.info("Mise à jour du thésaurus id pour les préférences du thésaurus id {}", oldIdThesaurus);
        preferencesRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
    }
}
