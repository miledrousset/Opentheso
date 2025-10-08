package fr.cnrs.opentheso.bean.menu;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import java.io.Serializable;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "thesoMenuBean")

public class thesoMenuBean implements Serializable {
    private MenuModel model;

    @PostConstruct
    public void init() {
        model = new DefaultMenuModel();


        // 1. Menu Accueil
        DefaultMenuItem home = DefaultMenuItem.builder()
                .icon("pi pi-home ")
                .title("Accueil")
                .outcome("index2")

                .build();

        // 2. Menu Rechercher
        DefaultMenuItem recherche = DefaultMenuItem.builder()
                .icon("pi pi-search text-white")
                .title("Recherche")
                .outcome("/theso_search/search")

                .build();



        // 4. Ajouter tout au modèle
        model.getElements().add(home);
        model.getElements().add(recherche);
    }

    public MenuModel getModel() {
        return model;
    }
    
}
