/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class JsonReadTest {

    public JsonReadTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void readJson() {
        String json = "{\n"
                + "  \"https://ark.mom.fr/ark:/26678/pcrtxi0KtisSHN\" : {\n"
                + "    \"http://purl.org/dc/terms/created\" : [\n"
                + "      {\n"
                + "        \"value\" : \"2007-02-08\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"datatype\" : \"http://www.w3.org/2001/XMLSchema#date\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://purl.org/dc/terms/description\" : [\n"
                + "      {\n"
                + "        \"value\" : \"12276\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"datatype\" : \"http://www.w3.org/2001/XMLSchema#string\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://purl.org/dc/terms/identifier\" : [\n"
                + "      {\n"
                + "        \"value\" : \"12276\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"datatype\" : \"http://www.w3.org/2001/XMLSchema#string\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://purl.org/dc/terms/modified\" : [\n"
                + "      {\n"
                + "        \"value\" : \"2013-05-22\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"datatype\" : \"http://www.w3.org/2001/XMLSchema#date\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://purl.org/umu/uneskos#memberOf\" : [\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.frantiq.fr/ark:/?idg=3&idt=TH_1\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" : [\n"
                + "      {\n"
                + "        \"value\" : \"http://www.w3.org/2004/02/skos/core#Concept\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://www.w3.org/2004/02/skos/core#inScheme\" : [\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.frantiq.fr/ark:/TH_1\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://www.w3.org/2004/02/skos/core#narrower\" : [\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrt6YAjOsQDeM\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrt9vV0Zk6kfA\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrtEz49cz3dAg\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrtTEK0OAy1JV\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrtaEVNwbclnn\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrtpZTMSKPHbL\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/pcrtqjiJozZ1eX\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://www.w3.org/2004/02/skos/core#prefLabel\" : [\n"
                + "      {\n"
                + "        \"value\" : \"منجزات فنية\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"ar\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"Kunstwerke\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"de\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"Works of art\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"en\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"Obras artísticas\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"es\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"œuvres artistiques\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"fr\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"Opere artistiche\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"it\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"value\" : \"kunstwerken\",\n"
                + "        \"type\" : \"literal\",\n"
                + "        \"lang\" : \"nl\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"http://www.w3.org/2004/02/skos/core#related\" : [\n"
                + "      {\n"
                + "        \"value\" : \"https://ark.mom.fr/ark:/26678/crtkHyGTQPhE2\",\n"
                + "        \"type\" : \"uri\"\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        
        
        
        
        
    }
}
