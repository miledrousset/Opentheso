package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.repositories.GpsHelper;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "gpsBean")
public class GpsBean implements Serializable {

    private ConceptView conceptView;
    private SelectedTheso selectedTheso;
    private GpsHelper gpsRepository;

    private Gps gpsSelected;


    @Inject
    public GpsBean(ConceptView conceptView,
                   SelectedTheso selectedTheso,
                   GpsHelper gpsRepository) {

        this.conceptView = conceptView;
        this.selectedTheso = selectedTheso;
        this.gpsRepository = gpsRepository;
    }

    public void addNewCoordinateGps() {

        gpsRepository.saveNewGps(gpsSelected);

        conceptView.getNodeConcept().setNodeGps(gpsRepository.getGpsByConceptAndThesorus(
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso()));

        conceptView.createMap(selectedTheso.getCurrentIdTheso());

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Coordonnée GPS ajoutée avec succèe");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    /**
     * permet de mettre à jour les coordonnées GPS
     */
    public void updateCoordinateGps(Gps gps) {

        gpsRepository.saveNewGps(Gps.builder()
                .idConcept(conceptView.getNodeConcept().getConcept().getIdConcept())
                .idTheso(selectedTheso.getCurrentIdTheso())
                .latitude(gps.getLatitude())
                .longitude(gps.getLongitude())
                .build());

        conceptView.getNodeConcept().setNodeGps(gpsRepository.getGpsByConceptAndThesorus(
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso()));

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Coordonnée GPS supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
            PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        }
    }


    /**
     * permet de supprimer les coordonnées GPS d'un concept
     */
    public void deleteCoordinateGps(Gps gps) {

        if(gps == null) return;

        gpsRepository.removeGps(gps);

        conceptView.createMap(selectedTheso.getCurrentIdTheso());

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Suppression des coordonnées GPS réussie");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
            PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        }
    }

    public void init() {
        gpsSelected = new Gps();
        gpsSelected.setIdConcept(conceptView.getNodeConcept().getConcept().getIdConcept());
        gpsSelected.setIdTheso(selectedTheso.getCurrentIdTheso());
    }

}
