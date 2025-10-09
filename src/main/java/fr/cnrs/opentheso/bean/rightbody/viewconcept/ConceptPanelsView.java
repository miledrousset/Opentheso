package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named(value = "conceptPanelsView")
@SessionScoped
@Data

@RequiredArgsConstructor
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ConceptPanelsView implements Serializable {

    private Map<String, Boolean> panelsVisibility;
    private List<String> selectedPanels;

    @PostConstruct
    public void init() {
        panelsVisibility = new LinkedHashMap<>();

        // Par défaut tous visibles
        panelsVisibility.put("label", true);
        panelsVisibility.put("collections", true);
        panelsVisibility.put("relations", true);
        panelsVisibility.put("traductions", true);
        panelsVisibility.put("notes", true);

        // Initialise la sélection avec les panels visibles
        selectedPanels = panelsVisibility.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** 🔹 Méthode utilisée par <f:selectItems> */
    public List<PanelOption> getPanelOptions() {
        return panelsVisibility.keySet().stream()
                .map(key -> new PanelOption(key, getLabel(key)))
                .collect(Collectors.toList());
    }

    /** 🔹 Met à jour les panels visibles en fonction des cases cochées */
    public void updatePanels() {
        panelsVisibility.replaceAll((key, oldValue) -> selectedPanels.contains(key));
        PrimeFaces.current().ajax().update("conceptForm:contentPanels");
    }

    /** 🔹 Méthode existante conservée (menuButton) */
    public void togglePanel(String key) {
        panelsVisibility.put(key, !panelsVisibility.get(key));
        PrimeFaces.current().executeScript("window.location.reload();");
    }

    /** 🔹 Libellés lisibles */
    public String getLabel(String key) {
        return switch (key) {
            case "label" -> "Libellé";
            case "collections" -> "Collections";
            case "relations" -> "Relations sémantiques";
            case "traductions" -> "Traductions";
            case "notes" -> "Notes";
            default -> key;
        };
    }

    public void selectAllPanels() {
        selectedPanels = new ArrayList<>(panelsVisibility.keySet());
        updatePanels(); // met à jour panelsVisibility en fonction de selectedPanels
    }

    public void deselectAllPanels() {
        selectedPanels.clear();
        updatePanels();
    }

    /** 🔹 Petite classe interne pour simplifier l'affichage */
    @Data
    public static class PanelOption {
        private final String key;
        private final String label;
    }
}