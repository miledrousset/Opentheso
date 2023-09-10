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
        mapModel.setZoom(13);
        mapModel.setAttribution("©<a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>");
        mapModel.setMiniMap(false);
        mapModel.setLayerControl(false);
        mapModel.setDraggingEnabled(true);
        mapModel.setZoomEnabled(true);
        mapModel.setCenter(calculerCentrePolyline(gpsList));

        String title = StringUtils.isEmpty(term) ? "" : term.replaceAll("'", "_");
        Pulse pule = new Pulse(true, 10, "#F47B2A");
        Layer placesLayer = new Layer();

        if (GpsMode.POINT.equals(gpsMode)) {
            placesLayer.setLabel(title);
            for (Gps gps : gpsList) {
                placesLayer.addMarker(new Marker(new LatLong(gps.getLatitude().toString(), gps.getLongitude().toString()), title, pule));
            }
            mapModel.addLayer(placesLayer);
        } else if (GpsMode.POLYGONE.equals(gpsMode)) {

            List<LatLong> points = gpsList.stream()
                    .map(gps -> new LatLong(gps.getLatitude().toString(), gps.getLongitude().toString()))
                    .collect(Collectors.toList());
            points.add(new LatLong(gpsList.get(0).getLatitude().toString(), gpsList.get(0).getLongitude().toString()));

            Layer polygone = new Layer()
                    .setLabel(title)
                    .addPolyline((new Polyline())
                            .addPoint(points)
                            .setColor("#F47B2A"));
            mapModel.addLayer(polygone);
        } else {

            Layer polygone = new Layer()
                    .setLabel(title)
                    .addPolyline((new Polyline())
                            .addPoint(gpsList.stream()
                                    .map(gps -> new LatLong(gps.getLatitude().toString(), gps.getLongitude().toString()))
                                    .collect(Collectors.toList()))
                            .setColor("#F47B2A"));
            mapModel.addLayer(polygone);
        }

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

}