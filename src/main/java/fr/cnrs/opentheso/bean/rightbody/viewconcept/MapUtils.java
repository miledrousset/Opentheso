package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import fr.cnrs.opentheso.entites.Gps;
import java.util.List;


public class MapUtils {

    private String longitudeCentre, latitudeCentre;

    public String createMap(List<Gps> gpsList, GpsMode gpsMode, String term) {

        StringBuilder script = new StringBuilder();

        calculerCentrePolyline(gpsList);

        script.append("var map = L.map('map').setView([" + longitudeCentre + ", " + latitudeCentre + "], " + calculerZoom(gpsList) + ");" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "maxZoom: 19," +
                "attribution: '© OpenStreetMap contributors'" +
                "}).addTo(map);");
        script.append("var latlngs = [");

        for (Gps gps : gpsList) {
            script.append("[").append(gps.getLongitude()).append(", ").append(gps.getLatitude()).append("],");
        }

        // Supprimer la dernière virgule
        if (!gpsList.isEmpty()) {
            script.deleteCharAt(script.length() - 1);
        }

        script.append("];\n");

        script.append("L.circleMarker(latlngs[0], { radius: 5, color: '#0E53FF', fillColor: '#0E53FF', fillOpacity: 1}).addTo(map);");

        if (!GpsMode.POINT.equals(gpsMode)) {
            if (gpsList.get(0).getLongitude().equals(gpsList.get(gpsList.size()-1).getLongitude())
                    && gpsList.get(0).getLatitude().equals(gpsList.get(gpsList.size()-1).getLatitude()) ) {
                script.append("var polygon = L.polygon(latlngs, { color: 'blue', fillColor: '#0E53FF', fillOpacity: 0.5 }).addTo(map);\n");
            } else {
                script.append("var polygon = L.polyline(latlngs, { color: 'blue', fillColor: '#0E53FF', fillOpacity: 0.5 }).addTo(map);\n");
            }
            script.append("map.fitBounds(polygon.getBounds());");
        }

        return script.toString();
    }

    private void calculerCentrePolyline(List<Gps> gpsList) {

        Double centreLatitude = gpsList.stream()
                .map(element -> Double.valueOf(element.getLatitude()))
                .mapToDouble(Double::doubleValue)
                .sum() / gpsList.size();

        Double centreLongitude = gpsList.stream()
                .map(element -> Double.valueOf(element.getLongitude()))
                .mapToDouble(Double::doubleValue)
                .sum() / gpsList.size();

        latitudeCentre = centreLatitude.toString();
        longitudeCentre = centreLongitude.toString();
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
