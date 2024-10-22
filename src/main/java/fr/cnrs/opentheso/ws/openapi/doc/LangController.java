package fr.cnrs.opentheso.ws.openapi.doc;

import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.LangHelper;
import java.util.Locale;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
@Slf4j
@RestController
//@RequestMapping("/api")
@CrossOrigin(methods = { RequestMethod.GET })
public class LangController {

    private String firstLetterCapital(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    @GetMapping(value = "/lang", consumes = CustomMediaType.APPLICATION_JSON_UTF_8)
    public ResponseEntity<Object> getAvailablesLanguages() {
        
        JsonArrayBuilder builder = Json.createArrayBuilder();

        for (String langCode : new LangHelper().availableLang()) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            Locale locale = new Locale(langCode);
            objectBuilder.add("code", langCode);
            objectBuilder.add("display", firstLetterCapital(locale.getDisplayLanguage(locale)));
            builder.add(objectBuilder.build());
        }
        
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(builder.build().toString());
        
    }
    
}*/
