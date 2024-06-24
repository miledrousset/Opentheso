package api;

import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import org.apache.jena.base.Sys;
import org.junit.Test;

public class apiTest {
    String apiKey = "";
    ApiKeyHelper apiKeyHelper = new ApiKeyHelper();
    UserHelper userHelper = new UserHelper();

    @Test
    public void testGenerateApiKey(){
        System.out.println("Test génération: " + apiKeyHelper.generateApiKey("ot_", 64));

    }

    @Test
    public void testCheckApiKey() {

        try {
            System.out.println("Test checkApiKey: " + apiKeyHelper.checkApiKey(apiKey));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testGetGroupId() {

        try {
            System.out.println("Test getGroupId: " + userHelper.getUserGroupId(1, "th2"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testSaveConcept() {
        CandidateHelper candidateHelper = new CandidateHelper();
        String candidateJson = "{"
                + "\"title\": \"Candidat Exemple\","
                + "\"definition\": \"Ceci est une définition d'exemple.\","
                + "\"thesoId\": \"th2\","
                + "\"source\": \"tonton\""
                + "}";

        System.out.println("Test saveConcept: " + candidateHelper.saveCandidat(candidateJson, 1));

    }


}
