$(document).ready(function () {

    // Paramètres par défaut
    const DEFAULT_LANGUAGE = 'fr';
    const DEFAULT_BASE_URL = 'https://pactols.frantiq.fr/opentheso';

    const languagesMap = new Map([["fr", "Français"], ["en", "Anglais"], ["it", "Italien"],
        ["es", "Espagnol"], ["ru", "Russian"], ["ar", "Arabe"], ["he", "Hebrew"], ["de", "Allemand"],
        ["nl", "Néerlandais"], ["el", "Grec"]]);

    const thesaurusSelect = document.getElementById('thesaurus-select');
    const languagesSelect = document.getElementById('languages-select');
    const languageSelect = document.getElementById('language-select');
    const collectionsSelect = document.getElementById('collections-select');
    const collectionSelect = document.getElementById('collection-select');
    const thesaurusBaseUrl = document.getElementById('thesaurus-url');
    const demoAutoComplete = document.getElementById('demoAutoComplete');
    const thesaurusSelectedNameContent = document.getElementById('thesaurusSelectedNameContent');
    const errorConfigMessage = document.getElementById('errorConfigMessage');
    const searchInAllCollections = document.getElementById('searchInAllCollections');

    let autoCompleteInstance;
    let thesaurusSelected = undefined;
    let collectionsList, languesList, thesaurusList;

    // Objet pour enregistrer temporairement le dernier paramètre
    const objectToSave = {
        collections: [],
        languages: [],
        thesaurus: [],
        baseUrl: undefined
    };

    //jQuery time
    let current_fs, next_fs, previous_fs, left, opacity, scale, animating;


    // Chargement des paramètres par défaut, utilisé lors du chargement de la page
    function setDefaultComposant() {
        thesaurusBaseUrl.value = DEFAULT_BASE_URL;
        searchInAllCollections.checked = true;
        collectionSelect.disabled = true;
        getThesaurus(thesaurusBaseUrl.value, true);
    }

    // Renvoie le label de la langue à partir d'un code
    function getLanguageLabel(languageCode) {
        let languageLabel = languagesMap.get(languageCode);
        return (languageLabel) ? languageLabel : languageCode
    }

    // Action du click sur le bouton 'Paramètre'
    $(".setting").click(function () {

        /** Début de l'animation pour switcher entre les deux écrans **/
        if (animating) return false;
        animating = true;

        current_fs = $(this).parent();
        next_fs = $(this).parent().next();

        $("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");

        next_fs.show();
        current_fs.animate(
            { opacity: 0 },
            {
                step: function (now, mx) {
                    scale = 1 - (1 - now) * 0.2;
                    left = now * 50 + "%";
                    opacity = 1 - now;
                    current_fs.css({
                        transform: "scale(" + scale + ")",
                        position: "absolute"
                    });
                    next_fs.css({ left: left, opacity: opacity });
                },
                duration: 800,
                complete: function () {
                    current_fs.hide();
                    animating = false;
                }
            }
        );
        /** FIN de l'animation pour switcher entre les deux écrans **/
    });

    // Action lors du click sur le bouton 'annuler'
    $(".previous").click(function () {

        if (objectToSave.baseUrl != undefined) {
            // Chargement du dernier paramètrage
            thesaurusBaseUrl.value = objectToSave.baseUrl;

            thesaurusSelected = objectToSave.thesaurus.filter(thesaurus => thesaurus.selected === true)[0];
            thesaurusSelect.innerHTML = objectToSave.thesaurus
                .map(thesaurus => `'<option value="${thesaurus.id}">${thesaurus.label}</option>'`)
                .join(', ');
            thesaurusSelect.disabled = false;
            for (let i = 0; i < thesaurusSelect.options.length; i++) {
                if (thesaurusSelect.options[i].value === thesaurusSelected.id) {
                    thesaurusSelect.options[i].selected = true;
                }
            }

            collectionsSelect.innerHTML = objectToSave.collections.map(objet => `'<option value="${objet.id}">${objet.label}</option>'`).join(', ');
            collectionsSelect.disabled = false;
            for (let i = 0; i < collectionsSelect.options.length; i++) {
                if (isCollectionSelected(objectToSave.collections, collectionsSelect.options[i].value)) {
                    collectionsSelect.options[i].selected = true;
                }
            }
            collectionSelect.innerHTML = objectToSave.collections.filter(collection => collection.selected === true)
                    .map(objet => `'<option value="${objet.id}">${objet.label}</option>'`)
                    .join(', ');
            collectionSelect.disabled = false;


            languagesSelect.innerHTML = objectToSave.languages.map(longue => `'<option value="${longue.id}">` + getLanguageLabel(longue.id) + `</option>'`).join(', ');
            languagesSelect.disabled = false;
            for (let i = 0; i < languagesSelect.options.length; i++) {
                if (isLangSelected(objectToSave.languages, languagesSelect.options[i].value)) {
                    languagesSelect.options[i].selected = true;
                }
            }

            if (objectToSave.languages != null) {
                languageSelect.innerHTML = '';
                if (objectToSave.languages.length > 1) {
                    languageSelect.innerHTML = "<option value='all'>Toutes les langues</option>";
                }
                languageSelect.innerHTML += objectToSave.languages
                    .filter(langue => langue.selected === true)
                    .map(langue => `'<option value="${langue.id}">` + getLanguageLabel(langue.id) + `</option>'`)
                    .join(', ');
                languageSelect.disabled = false;
            }

            setAutoComplet();
        } else {
            thesaurusBaseUrl.value = DEFAULT_BASE_URL;
            getThesaurus(thesaurusBaseUrl.value, true);
        }

        /** Début de l'animation pour switcher entre les deux écrans **/
        if (animating) return false;
        animating = true;

        current_fs = $(this).parent();
        previous_fs = $(this).parent().prev();

        $("#progressbar li").eq($("fieldset").index(current_fs)).removeClass("active");

        previous_fs.show();
        current_fs.animate(
            { opacity: 0 },
            {
                step: function (now, mx) {
                    scale = 0.8 + (1 - now) * 0.2;
                    left = (1 - now) * 50 + "%";
                    opacity = 1 - now;
                    current_fs.css({ left: left });
                    previous_fs.css({
                        transform: "scale(" + scale + ")",
                        opacity: opacity
                    });
                },
                duration: 800,
                complete: function () {
                    current_fs.hide();
                    animating = false;
                }
            }
        );
        /** FIN de l'animation pour switcher entre les deux écrans **/
    });

    function isCollectionSelected(collections, idCollection) {
        return collections.filter(collection => collection.selected === true && collection.id === idCollection).length > 0;
    }

    function isLangSelected(langues, idLang) {
        return langues.filter(langue => langue.selected === true && langue.id === idLang).length > 0;
    }

    $(".save").click(function () {

        objectToSave.baseUrl = thesaurusBaseUrl.value;

        for (let i = 0; i < thesaurusSelect.options.length; i++) {
            if (thesaurusSelect.options[i].selected) {
                thesaurusSelected = thesaurusList.filter(thesaurus => thesaurus.id === thesaurusSelect.options[i].value)[0]
            }
        }
        thesaurusSelectedNameContent.textContent = thesaurusSelected.label;

        if (thesaurusList != null) {
            thesaurusList.forEach(thesaurus => {
                let thesaurusToSave = { id: thesaurus.id, label: thesaurus.label };
                if (thesaurus.id === thesaurusSelected.id) {
                    thesaurusToSave.selected = true;
                } else {
                    thesaurusToSave.selected = false;
                }
                objectToSave.thesaurus.push(thesaurusToSave);
            });
        }

        collectionSelect.innerHTML = "";
        if (collectionsList != null) {
            collectionsList.forEach(collection => {
                let selected = false;
                for (let i = 0; i < collectionsSelect.options.length; i++) {
                    if (collectionsSelect.options[i].selected && collectionsSelect.options[i].value === collection.id) {
                        selected = true;
                        collectionSelect.innerHTML += `<option value="${collection.id}">${collection.label}</option>`;
                    }
                }
                for (let i = 0; i < collectionSelect.options.length; i++) {
                    collectionSelect.options[i].selected = true;
                }
                objectToSave.collections.push({ id: collection.id, label: collection.label, selected: selected });
            });
        }

        languageSelect.innerHTML = "";
        if (languesList != null) {
            languesList.forEach(langue => {
                let selected = false;
                for (let i = 0; i < languagesSelect.options.length; i++) {
                    if (languagesSelect.options[i].selected && languagesSelect.options[i].value === langue.lang) {
                        selected = true;
                        languageSelect.innerHTML += `, <option value="${langue.lang}">` + getLanguageLabel(langue.lang) + `</option>`;
                    }
                }
                objectToSave.languages.push({ id: langue.lang, selected: selected });
            });

            selectedDefaultLanguage();
        };

        searchInAllCollections.checked = true;
        collectionSelect.disabled = true;
        if (collectionSelect.options.length == 0) {
            searchInAllCollections.disabled = true;
        } else {
            searchInAllCollections.disabled = false;
        }

        setAutoComplet();

        /** Début de l'animation pour switcher entre les deux écrans **/
        if (animating) return false;
        animating = true;

        current_fs = $(this).parent();
        previous_fs = $(this).parent().prev();

        $("#progressbar li").eq($("fieldset").index(current_fs)).removeClass("active");

        previous_fs.show();
        current_fs.animate(
            { opacity: 0 },
            {
                step: function (now, mx) {
                    scale = 0.8 + (1 - now) * 0.2;
                    left = (1 - now) * 50 + "%";
                    opacity = 1 - now;
                    current_fs.css({ left: left });
                    previous_fs.css({
                        transform: "scale(" + scale + ")",
                        opacity: opacity
                    });
                },
                duration: 800,
                complete: function () {
                    current_fs.hide();
                    animating = false;
                }
            }
        );
        /** FIN de l'animation pour switcher entre les deux écrans **/
    });

    thesaurusSelect.addEventListener('change', () => {
        searchCollectionsAndLanguages(thesaurusBaseUrl.value, thesaurusSelect.value, false);
    });

    async function searchCollectionsAndLanguages(url, idThesaurus, isFirstTime) {
        let collectionList = await searchCollections(url, idThesaurus);
        if (collectionList) {
            collectionsList = collectionList.map(function(element) {
                let label = element.labels.find(function(label) {
                    return label.lang === DEFAULT_LANGUAGE;
                });
                return {
                    id: element.idGroup,
                    label: label ? label.title : null
                };
            });
            collectionsSelect.innerHTML = collectionsList.map(function(collection) {
                return '<option value="' + collection.id + '">' + collection.label + '</option>';
            }).join('');

            for (let i = 0; i < collectionsSelect.options.length; i++) {
                collectionsSelect.options[i].selected = true;
            }
            collectionsSelect.disabled = false;
        }

        languesList = await searchLanguages(url, idThesaurus);
        if (languesList) {
            languagesSelect.innerHTML = languesList.map(function(langue) {
                return '<option value="' + langue.lang + '">' + getLanguageLabel(langue.lang) + '</option>';
            }).join('');

            for (let i = 0; i < languagesSelect.options.length; i++) {
                languagesSelect.options[i].selected = true;
            }
            languagesSelect.disabled = false;
        }

        if (isFirstTime) {
            collectionSelect.innerHTML = "";
            if (collectionsList != null) {
                collectionsList.forEach(collection => {
                    for (let i = 0; i < collectionsSelect.options.length; i++) {
                        if (collectionsSelect.options[i].selected && collectionsSelect.options[i].value === collection.id) {
                            collectionSelect.innerHTML += `<option value="${collection.id}">${collection.label}</option>`;
                        }
                    }
                });
                for (let i = 0; i < collectionSelect.options.length; i++) {
                    collectionSelect.options[i].selected = true;
                }
            }

            languageSelect.innerHTML = "";
            if (languesList != null) {
                languesList.forEach(langue => {
                    for (let i = 0; i < languagesSelect.options.length; i++) {
                        if (languagesSelect.options[i].selected && languagesSelect.options[i].value === langue.lang) {
                            languageSelect.innerHTML += `, <option value="${langue.lang}">` + getLanguageLabel(langue.lang) + `</option>`;
                        }
                    }
                });
                selectedDefaultLanguage();
            }
        }
    }

    // Selectionner la langue par défaut
    function selectedDefaultLanguage() {
        let isSelected = false;
        for (let i = 0; i < languageSelect.options.length; i++) {
            if (languageSelect.options[i].value === DEFAULT_LANGUAGE) {
                languageSelect.options[i].selected = true;
                isSelected = true;
            }
        }
        if (!isSelected && languageSelect.options.length() > 0) {
            languageSelect.options[0].selected = true;
        }
    }

    // Listener sur le changement du thésaurus selectionné
    // -> rechercher la liste des langues et des collections lorsque l'utilisateur sélectionne un thésaurus
    thesaurusSelect.addEventListener('change', () => {
        collectionsSelect.disabled = thesaurusSelect.value === '';
        languagesSelect.disabled = thesaurusSelect.value === '';
        thesaurusSelected = thesaurusList.filter(thesaurus => thesaurus.id === thesaurusSelect.value)[0];
    });

    // Listener sur le changement de la collection selectionnée
    collectionSelect.addEventListener('change', () => {
        setAutoComplet();
    });

    // Listener sur la changement de la langue selectionnée
    languageSelect.addEventListener('change', () => {
        setAutoComplet();
    });

    // Configurer le composant autocomplet selon les langues et les collections selectionnées
    function setAutoComplet() {
        let langueSelected = (languageSelect.value && languageSelect.value != 'all') ? languageSelect.value : undefined;

        let collectionSelected = undefined;
        if (!searchInAllCollections.checked && collectionSelect.value) {
            collectionSelected = '';
            for (let i = 0; i < collectionSelect.options.length; i++) {
                if (collectionSelect.options[i].selected) {
                    collectionSelected += collectionSelect.options[i].value + ',';
                }
            }
            if (collectionSelected.length > 0) {
                collectionSelected = collectionSelected.substring(0, collectionSelected.length - 1);
            } else {
                collectionSelected = undefined;
            }
        }

        demoAutoComplete.value = '';

        autoCompleteInstance.destroy();
        autoCompleteInstance = new autoComplete({
            selector: '#demoAutoComplete',
            loading: 'loading',
            output: 'output',
            url: thesaurusBaseUrl.value + '/openapi/v1/concept/',
            thesaurus: thesaurusSelected.id,
            lang: langueSelected,
            groupe: collectionSelected
        });
    }

    // Rechercher la liste des collections
    async function searchCollections(baseUrl, idTheso) {
        try {
            const response = await fetch(`${baseUrl}/api/info/list?theso=${idTheso}&group=all`);
            if (!response.ok) {
                throw new Error(`Erreur HTTP : ${response.status}`);
            }
            const data = await response.json();
            const collections = data.map(collection => ({
                idGroup: collection.idGroup,
                labels: collection.labels
            }));
            return collections;
        } catch (error) {
            showErrorConfigurationMessage('Erreur dans la recherche des collections !');
            return null;
        }
    }

    // Rechercher la liste des langues
    async function searchLanguages(baseUrl, idTheso) {
        try {
            const response = await fetch(`${baseUrl}/api/info/listLang?theso=${idTheso}&group=all`);
            if (!response.ok) {
                throw new Error(`Erreur HTTP : ${response.status}`);
            }
            const data = await response.json();
            const langues = data.map(langue => ({
                lang: langue.lang
            }));
            return langues;
        } catch (error) {
            showErrorConfigurationMessage('Erreur dans la recherche des langues !');
            return null;
        }
    }

    // Listener sur le bouton recherche des thèsaurus
    $(".searchAllThesaurus").click(async () => {
        disableSettingScreen();
        getThesaurus(thesaurusBaseUrl.value, false);
    });

    // Recherche des thésaurus + recherche des langues et collections du 1er thésaurus
    async function getThesaurus(url, isFirstTime) {
        const thesaurusListTmp = await searchAllThesaurus(url);
        if (thesaurusListTmp) {
            // Formater la liste des thèsaurus trouvée
            thesaurusList = thesaurusListTmp.filter(element => element.labels !== undefined)
                .map(element => {
                    let label = undefined;
                    let labelSelected = element.labels.find(label => label.lang === DEFAULT_LANGUAGE);
                    if (labelSelected) {
                        label = labelSelected.title;
                    } else if (element.labels) {
                        label = element.labels[0].title + ' (' +  element.labels[0].lang + ')';
                    }
                    return { id: element.idTheso, label: label };
            });

            // Afficher la liste des thesaurus dans le composant select
            thesaurusSelect.innerHTML = thesaurusList.map(function(thesaurus) {
                return '<option value="' + thesaurus.id + '">' + thesaurus.label + '</option>';
            }).join('');

            // Selectionner le 1er thèsaurus de la liste
            thesaurusSelect.options[0].selected = true;
            thesaurusSelected = thesaurusListTmp[0];

            thesaurusSelect.disabled = false;
            // Recherche des collections et des langues du 1er thésaurus de la liste
            searchCollectionsAndLanguages(url, thesaurusSelect.value, isFirstTime);

            if (isFirstTime) {
                // Si c'est fonction est appelée lors du chargement de la page
                // -> initier le composant autocomplet par des paramètres par défaut
                thesaurusSelectedNameContent.textContent = thesaurusSelected.labels.filter(element => element.lang === DEFAULT_LANGUAGE)[0].title;
                thesaurusSelectedNameContent.style.visibility = 'visible';
                thesaurusSelectedNameContent.style.display = 'block';

                autoCompleteInstance = new autoComplete({
                    selector: '#demoAutoComplete',
                    loading: 'loading',
                    output: 'output',
                    url: thesaurusBaseUrl.value + '/openapi/v1/concept/',
                    thesaurus: thesaurusSelected.idTheso,
                    lang: undefined,
                    groupe: undefined
                });
            } else {
                enableSettingScreen();
            }
        } else {
            showErrorConfigurationMessage('Aucun thésaurus trouvé !')
        }
    }

    // Verrouiller l'écran paramètre
    function disableSettingScreen() {
        $(".save, .previous, .searchAllThesaurus").prop("disabled", true).addClass("disabled");
        thesaurusSelect.disabled = true;
        collectionsSelect.disabled = true;
        languagesSelect.disabled = true;
    }

    // Déverrouiller l'écran paramètre
    function enableSettingScreen() {
        $(".save, .previous, .searchAllThesaurus").prop("disabled", false);
        $(".save, .previous, .searchAllThesaurus").removeClass("disabled");
        thesaurusSelect.disabled = false;
        collectionsSelect.disabled = false;
        languagesSelect.disabled = false;
    }

    // Recherche de la liste des thèsaurus depuis la base URL
    async function searchAllThesaurus(baseUrl) {
        if (isURLValid(`${baseUrl}/api/info/list?theso=all`)) {
            try {
                const response = await fetch(`${baseUrl}/api/info/list?theso=all`);
                if (!response.ok) {
                    throw new Error(`Erreur HTTP : ${response.status}`);
                }
                const data = await response.json();
                const thesaurusList = data.map(thesaurus => ({
                    idTheso: thesaurus.idTheso,
                    labels: thesaurus.labels
                }));
                return thesaurusList;
            } catch (error) {
                showErrorConfigurationMessage('Erreur dans la recherche des thésaurus !');
                return null;
            }
        } else {
            showErrorConfigurationMessage('Votre URL est invalide !');
        }
    }

    // Affichage des messages d'erreur dans l'écran
    function showErrorConfigurationMessage(message) {
        errorConfigMessage.innerHTML = message;
        errorConfigMessage.style.color = "red";
    }


    //Vérification de la validité d'une URL
    function isURLValid(url) {
        let urlRegex = /^(https?):\/\/[^\s\/$.?#].[^\s]*$/i;
        return urlRegex.test(url);
    }

    // Listener sur la case à cocher de recherche dans tous les collections
    searchInAllCollections.addEventListener('change', function() {
        collectionSelect.disabled = searchInAllCollections.checked;
        setAutoComplet();
    });

    window.onload = setDefaultComposant();
});

