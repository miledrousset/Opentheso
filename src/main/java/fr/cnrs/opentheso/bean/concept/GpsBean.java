package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.GpsService;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.primefaces.PrimeFaces;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "gpsBean")
public class GpsBean implements Serializable {

    private final ConceptView conceptView;
    private final SelectedTheso selectedTheso;
    private final GpsService gpsService;

    private Gps gpsSelected;


    public void addNewCoordinateGps() {

        gpsService.saveNewGps(gpsSelected);

        conceptView.getNodeConcept().setNodeGps(gpsService.findByIdConceptAndIdThesoOrderByPosition(
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso()));

        conceptView.createMap(selectedTheso.getCurrentIdTheso());

        MessageUtils.showInformationMessage("Coordonnée GPS ajoutée avec sucès");

        PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void updateCoordinateGps(Gps gps) {

        gpsService.saveNewGps(Gps.builder()
                .idConcept(conceptView.getNodeConcept().getConcept().getIdConcept())
                .idTheso(selectedTheso.getCurrentIdTheso())
                .latitude(gps.getLatitude())
                .longitude(gps.getLongitude())
                .build());

        var gpsList = gpsService.findByIdConceptAndIdThesoOrderByPosition(
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        conceptView.getNodeConcept().setNodeGps(gpsList);

        MessageUtils.showInformationMessage("Coordonnée GPS supprimé avec succès");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void deleteCoordinateGps(Gps gps) {

        if(gps == null) return;

        gpsService.deleteGps(gps);

        conceptView.createMap(selectedTheso.getCurrentIdTheso());

        MessageUtils.showInformationMessage("Suppression des coordonnées GPS réussie");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void init() {
        gpsSelected = new Gps();
        gpsSelected.setIdConcept(conceptView.getNodeConcept().getConcept().getIdConcept());
        gpsSelected.setIdTheso(selectedTheso.getCurrentIdTheso());
    }
}
