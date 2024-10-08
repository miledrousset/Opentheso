package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import com.jsf2leaf.model.LatLong;
import com.jsf2leaf.model.Layer;
import com.jsf2leaf.model.Map;
import com.jsf2leaf.model.Marker;
import com.jsf2leaf.model.Polyline;
import com.jsf2leaf.model.Pulse;
import fr.cnrs.opentheso.entites.Gps;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;


public class MapUtils {

    public Map createMap(List<Gps> gpsList, GpsMode gpsMode, String term) {

        Map mapModel = new Map();
        mapModel.setWidth("100%");
        mapModel.setHeight("250px");
        mapModel.setZoom(calculerZoom(gpsList));
        mapModel.setAttribution("©<a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>");
        mapModel.setMiniMap(false);
        mapModel.setLayerControl(false);
        mapModel.setDraggingEnabled(true);
        mapModel.setZoomEnabled(true);
        mapModel.setCenter(calculerCentrePolyline(gpsList));

        String title = StringUtils.isEmpty(term) ? "" : term.replaceAll("'", "_");

        if (GpsMode.POINT.equals(gpsMode)) {
            Pulse pule = new Pulse(true, 10, "#0E53FF");
            mapModel.addLayer(new Layer()
                    .setLabel(title).addMarker(new Marker(new LatLong(gpsList.get(0).getLatitude().toString(),
                            gpsList.get(0).getLongitude().toString()), title, pule)));
        } else {
            mapModel.addLayer(new Layer()
                    .setLabel(title)
                    .addPolyline((new Polyline())
                            .addPoint(gpsList.stream()
                                    .map(gps -> new LatLong(gps.getLatitude().toString(), gps.getLongitude().toString()))
                                    .collect(Collectors.toList()))
                            .setColor("#0E53FF")));
        }

        return mapModel;
    }

    public Map updateMap(List<Gps> gpsList, Map mapModel, GpsMode gpsModeSelected, String title) {
        mapModel.getLayers().removeAll(mapModel.getLayers());
        mapModel.setZoom(calculerZoom(gpsList));
        
        title = StringUtils.isEmpty(title) ? "" : title.replaceAll("'", "_");        
        
        if (GpsMode.POINT.equals(gpsModeSelected)) {
            Pulse pule = new Pulse(true, 10, "#0E53FF");
            mapModel.addLayer(new Layer()
                    .setLabel(title)
                    .addMarker(new Marker(new LatLong(gpsList.get(0).getLatitude().toString(),
                            gpsList.get(0).getLongitude().toString()), title, pule)));
        } else {
            mapModel.addLayer(new Layer()
                    .setLabel(title)
                    .addPolyline((new Polyline())
                            .addPoint(gpsList.stream()
                                    .map(gps -> new LatLong(gps.getLatitude().toString(), gps.getLongitude().toString()))
                                    .collect(Collectors.toList()))
                            .setColor("#0E53FF")));
        }

        mapModel.setCenter(calculerCentrePolyline(gpsList));
        return mapModel;
    }

    private LatLong calculerCentrePolyline(List<Gps> gpsList) {

        Double centreLatitude = gpsList.stream()
                .map(element -> Double.valueOf(element.getLatitude()))
                .mapToDouble(Double::doubleValue)
                .sum() / gpsList.size();

        Double centreLongitude = gpsList.stream()
                .map(element -> Double.valueOf(element.getLongitude()))
                .mapToDouble(Double::doubleValue)
                .sum() / gpsList.size();

        return new LatLong(centreLatitude.toString(), centreLongitude.toString());
    }


    // Calcul du niveau de zoom optimal pour Leaflet JSF Map
    public static int calculerZoom(List<Gps> coordonnees) {

        // Choisissez un facteur de zoom initial en fonction de vos besoins
        final int zoomInitial = 13;
        
        if (coordonnees == null || coordonnees.size() < 2) {
            return zoomInitial;
        }

        // Vous pouvez ajuster ce facteur en fonction de votre application
        final double facteurZoom = 0.12;

        // Calcul de la distance totale entre les points
        double distanceTotale = calculerDistanceTotale(coordonnees);

        // Calcul du niveau de zoom optimal
        int niveauZoom = (int) (zoomInitial - facteurZoom * Math.log(distanceTotale));

        // Assurez-vous que le niveau de zoom est dans une plage valide (ex : entre 1 et 18 pour Leaflet)
        niveauZoom = Math.max(1, Math.min(18, niveauZoom));

        return niveauZoom;
    }

    // Calcul de la distance totale en mètres entre les points GPS
    private static double calculerDistanceTotale(List<Gps> coordonnees) {
        double distanceTotale = 0;

        for (int i = 0; i < coordonnees.size() - 1; i++) {
            Gps point1 = coordonnees.get(i);
            Gps point2 = coordonnees.get(i + 1);
            distanceTotale += distanceEntrePoints(point1, point2);
        }

        return distanceTotale;
    }

    // Calcul de la distance en mètres entre deux points GPS
    private static double distanceEntrePoints(Gps point1, Gps point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Rayon de la Terre en mètres
        double rayonTerre = 6371000;

        // Distance en mètres
        return rayonTerre * c;
    }
}
