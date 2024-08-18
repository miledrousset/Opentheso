/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import java.util.ArrayList;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author miledrousset
 */
public class JsonWriteTest {
    
    public JsonWriteTest() {
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void writeJson() {
/*
        String label = "test";
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
  
        for (Path path1 : paths) {
            JsonArrayBuilder jsonArrayBuilderPath = Json.createArrayBuilder();            
            for (String idConcept : path1.getPath()) {
                JsonObjectBuilder job = Json.createObjectBuilder();

                label = conceptHelper.getLexicalValueOfConcept(
                        ds,
                        idConcept,
                        idTheso, idLang);
                if(label.isEmpty())
                    label = "("+ idConcept+")";
                job.add("id", idConcept);
                job.add("label", label);
                jsonArrayBuilderPath.add(job.build());
            }
            jsonArrayBuilder.add(jsonArrayBuilderPath.build());//.toString());
        }
        datasJson = jsonArrayBuilder.build().toString();    */
    }
}
