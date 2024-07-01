
package fr.cnrs.opentheso.bean.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author miledrousset
 */

@Named
@SessionScoped
public class GraphService implements Serializable{

    private List<GraphObject> graphObjects;


    public void init() {
        graphObjects = new ArrayList<>();
        graphObjects.add(new GraphObject(1000, "f230fheza0g3", "Bamboo Watch", "Product Description", 24,  5));
        graphObjects.add(new GraphObject(1001, "f230fhr0g3", "e Watch", "Product Description", 24,  5));
        graphObjects.add(new GraphObject(1002, "f230fhre0g3", "Bamboeoer Watch", "Product Description", 24,  5));
        graphObjects.add(new GraphObject(1003, "f230fh0rg3", "Bamboo Warertch", "Product Description", 24,  5));

    }

    public List<GraphObject> getProducts() {
        return new ArrayList<>(graphObjects);
    }

    public List<GraphObject> getProducts(int size) {

        if (size > graphObjects.size()) {
            Random rand = new Random();

            List<GraphObject> randomList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int randomIndex = rand.nextInt(graphObjects.size());
                randomList.add(graphObjects.get(randomIndex));
            }

            return randomList;
        }

        else {
            return new ArrayList<>(graphObjects.subList(0, size));
        }

    }


}
