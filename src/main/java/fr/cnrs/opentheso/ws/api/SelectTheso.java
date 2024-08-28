package fr.cnrs.opentheso.ws.api;

import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.URISyntaxException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/theso")
@CrossOrigin(methods = { RequestMethod.GET })
public class SelectTheso {

    @Autowired
    private Connect connect;

    // Cette fonction permet de se diriger vers le bon thésaurus en passant par son nom VIA REST ceci permet de gérer
    // les noms de domaines et filtrer les thésaurus dans un parc important
    @GetMapping(value = "{theso}", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<Object> getThesoUri(@PathVariable("theso") String name,
                                      @Context UriInfo uriInfo)
            throws URISyntaxException {

        var idTheso = new PreferencesHelper().getIdThesaurusFromName(connect.getPoolConnexion(), name);
        var path = uriInfo.getBaseUriBuilder().toString().replaceAll("/api/", "/") + "?idt=" + idTheso;

        return ResponseEntity.status(307) // 307 corresponds to temporary redirect
                .location(new URI(path))
                .build();
    }
}
