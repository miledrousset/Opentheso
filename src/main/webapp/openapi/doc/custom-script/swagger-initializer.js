/**
 * Change le titre dans les méta-données de la page
 * @param {string} title Titre à afficher
 */
const changeTitle = (title) => document.title = title;

/**
 * Génère l'URL du fichier openapi.json en fonction de la version passée en paramètre
 * @param {string} version Version de l'API
 * @param {string} lang Langue souhaité pour la doc
 * @return {string} URL du fichier openapi.json
 */
const generateURL = (version, lang) => {
    return BASE_URL + version + "/" + lang  + "/openapi.json";
};

/**
 * Récupère la version de l'API à partir de l'URL passée en paramètre
 * @param {string} url URL du fichier openapi.json
 * @return {string} Version de l'API
 */
const getVersion = (url) => {
    const selectedParts = url.split("/");
    return selectedParts[selectedParts.length - 1];
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
 * Ajoute un listener sur le selecteur de serveur pour recharger la page avec la selection de la bonne version.<br>
 * La fonction est appelée toutes les 100ms jusqu'à ce que le selecteur soit trouvé, le selecteur du serveur n'étant pas toujours présent au chargement de la page.
 */
const addServerSelectorListener = () => {
    const id = setTimeout(() => {
        const serverSelector = document.querySelectorAll(".servers > label > select")[0];

        if (serverSelector !== undefined) {
            serverSelector.addEventListener("change", () => {
                let langSelector = document.getElementById("lang-selector");
                const version = getVersion(serverSelector.value);
                displayServerDoc("Opentheso2 - API " + version, version, langSelector.value);
            })
            clearTimeout(id);
        }


    }, 100);
};

/**
 * Ajoute un listener sur le selecteur de langue pour recharger la page avec la selection de la bonne langue
 */
const setupLangSelector = () => {
    let selector = document.getElementById("lang-selector");
    selector.addEventListener("change", () => {
        LANG = selector.value;
        // displayServerDoc("Opentheso2 - API",  VERSION, LANG);
        location.href = `http://localhost:8080/opentheso2/openapi/doc/?lang=${LANG}`;
    });
};

/**
 * Change valeur sélectionné par défaut du sélecteur de langue
 * @param {string} langCode Code de la langue choisie
 */
const changeSelector = (langCode) => {
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

const loadQueryLang = () => {
    if (getCurrentURL().indexOf("?") !== -1) {
        const queryString = getCurrentURL().split("?")[1];
        const parameters = new URLSearchParams(queryString);
        if (parameters.has("lang") && (AVAILABLE_LANG.indexOf(parameters.get("lang").toLowerCase()) !== -1)) { 
            const LANG = parameters.get("lang").toLowerCase();
            changeSelector(LANG);
        }
    } 
};

/**
 * Recharge la page avec la documentation de la version passée en paramètre
 * Cette fonction :
 * - Change le titre de la page
 * - Génère l'interface SwaggerUIBundle et l'assigne à la variable globale ui
 * - Cache la barre de recherche
 * - Ajoute un listener sur le selecteur de serveur pour recharger la page avec la selection de la bonne version
 * @param {string} title Titre à afficher dans les méta-données de la page
 * @param {string} version Version de l'API
 * @param {string} lang Langue de la doc
 */
const displayServerDoc = (title, version, lang) => {
    changeTitle(title);
    window.ui = uiBundle(generateURL(version, lang));
    hideSearchBar();
    addServerSelectorListener();
};


/* ========================== Programme principal ========================== */
let VERSION = "v1";
let LANG = "fr";
const BASE_URL = window.location.href.split("/doc")[0]  + "/";
const AVAILABLE_LANG = getAvailableLangages();

window.onload = function() {
    loadQueryLang();
    setupLangSelector();
    displayServerDoc("Opentheso2 - API" ,VERSION, LANG);
};