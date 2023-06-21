/**
 * Change le titre dans les méta-données de la page
 * @param {string} title Titre à afficher
 */
const changeTitle = (title) => document.title = title;

/**
 * Génère l'URL du fichier openapi.json en fonction de la version passée en paramètre
 * @param {string} version Version de l'API
 * @param {string} lang Langue souhaité pour la doc
 * @param {string} scheme HTTP ou HTTPS selon le protocole utilisé 
 * @return {string} URL du fichier openapi.json
 */
const generateURL = (version, lang, scheme) => {
    let parameters = "";
    if (scheme !== null && scheme !== "") {
        parameters = "?scheme=" + scheme;
    }
    return BASE_URL + version + "/" + lang  + "/openapi.json" + parameters;
};


/**
 * @returns { string } URL de la page actuelle
 */
function getCurrentURL () {
  return window.location.href;
}

/**
 * @returns { Array<string> } Liste des codes de langues disponibles
 */
const getAvailableLangages = () => {
    let selector = document.getElementById("lang-selector");
    const languages = [];
    selector.childNodes.forEach((elt) => {
        if (elt.localName === "option") {
          languages.push(elt.value);
        }
    });
    return languages;
};

/**
 * Ajoute un listener sur le selecteur de langue pour recharger la page avec la selection de la bonne langue
 */
const addLangSelectorListener = () => {
    let selector = document.getElementById("lang-selector");
    selector.addEventListener("change", () => {
        LANG = selector.value;
        // displayServerDoc("Opentheso2 - API",  VERSION, LANG);
        location.href = `${BASE_URL}doc/?lang=${LANG}&version=${VERSION}`;
    });
};

/**
 * Change valeur sélectionné par défaut du sélecteur de langue
 * @param {string} langCode Code de la langue choisie
 */
const changeLangSelectorDefault = (langCode) => {
    if (AVAILABLE_LANG.indexOf(langCode.toLowerCase()) !== -1) {
        let selector = document.getElementById("lang-selector");
        selector.value = langCode.toLowerCase();
    }
};

/**
 * Génère l'interface SwaggerUIBundle avec les paramètres par défaut et l'URL passée en paramètre
 * @param {string} url URL du fichier openapi.json
 * @return {SwaggerUIBundle} SwaggerUIBundle généré
 */
const uiBundle = (url) => {
    return SwaggerUIBundle({
        url: url,
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
    });
};

/**
 * Cache la barre de recherche, doit être appelée après l'initialisation de l'interface SwaggerUIBundle
 */
const hideSearchBar = () => {
    const downloadWrappers = document.querySelectorAll('.topbar');
    downloadWrappers.forEach(wrapper => {
        wrapper.style.display = 'none';
    });
};

/**
 * Si le query parameter "lang" existe dans l'URL. Charger la langue correspoondante ainsi que la valeur seléctionné par défaut.
 */
const loadQueryLang = () => {
    if (getCurrentURL().indexOf("?") !== -1) {
        const queryString = getCurrentURL().split("?")[1];
        const parameters = new URLSearchParams(queryString);
        if (parameters.has("lang") && (AVAILABLE_LANG.indexOf(parameters.get("lang").toLowerCase()) !== -1)) { 
            LANG = parameters.get("lang").toLowerCase();
        }
    }
    changeLangSelectorDefault(LANG);
};

const loadQueryVersion = () => {
    if (getCurrentURL().indexOf("?") !== -1) {
        const queryString = getCurrentURL().split("?")[1];
        const parameters = new URLSearchParams(queryString);
        if (parameters.has("version") && (AVAILABLE_VERSION.indexOf(parameters.get("version").toLowerCase()) !== -1)) { 
            VERSION = parameters.get("version").toLowerCase();
            // changeVersionSelectorDefault(VERSION);
        }
    } 
};


const fetchAvailableLanguages = () => {
    return fetch(BASE_URL + "doc/config/lang", {
        method: "GET"
    }).then(response => {
        return response.json();
    }).then(data => {
        const selector = document.getElementById("lang-selector");
        for (var i = 0; i < data.length; i++) {
            const option = document.createElement("option");
            option.value = data[i].code;
            option.text = data[i].display;
            
            selector.add(option);
            AVAILABLE_LANG.push(data[i].code);
        }
    });
};

/**
 * Recharge la page avec la documentation de la version passée en paramètre
 * Cette fonction :
 * - Change le titre de la page
 * - Génère l'interface SwaggerUIBundle et l'assigne à la variable globale ui
 * - Cache la barre de recherche
 * - Ajoute un listener sur le selecteur de version pour recharger la page lors du changement de selection d'une version
 * @param {string} title Titre à afficher dans les méta-données de la page
 * @param {string} version Version de l'API
 * @param {string} lang Langue de la doc
 */
const displayServerDoc = (title, version, lang) => {
    changeTitle(title);
    window.ui = uiBundle(generateURL(version, lang, getURLScheme()));
    hideSearchBar();
    // addVersionSelectorListener();
    addLangSelectorListener();
};

const getURLScheme = () => {
    return window.location.href.split("://")[0];
}


/* ========================== Programme principal ========================== */
let VERSION = "v1";
let LANG = "fr";
const BASE_URL = window.location.href.split("/doc")[0]  + "/";
const AVAILABLE_LANG = [];
const AVAILABLE_VERSION = ["v1"];

window.onload = function() {
    fetchAvailableLanguages()
            .then(() => {
                loadQueryLang();
                // loadQueryVersion();
                displayServerDoc("Opentheso2 - API" ,VERSION, LANG);
            });
};