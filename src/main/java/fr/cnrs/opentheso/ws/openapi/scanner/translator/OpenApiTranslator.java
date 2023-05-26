package fr.cnrs.opentheso.ws.openapi.scanner.translator;

import com.rits.cloning.Cloner;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.ws.rs.core.UriInfo;

public class OpenApiTranslator {

    private OpenAPI openAPI;
    private UriInfo uriInfo;

    private final ResourceBundle bundle;

    public OpenApiTranslator(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    private void translateInfoDescription() {
        String descriptionCode = openAPI.getInfo().getDescription();
        if (descriptionCode != null) openAPI.getInfo().setDescription(bundle.getString((descriptionCode)));

    }

    private void translateTagsDescription() {
        List<Tag> tags = openAPI.getTags();
        if (tags != null) {
            for (Tag tag : tags) {
                String descriptionCode = tag.getDescription();
                if (descriptionCode != null) tag.setDescription(bundle.getString((descriptionCode)));
            }
        }
    }


    private void translatePathsInfos() {

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            PathItem pathItem = entry.getValue();

            if (pathItem.getSummary() != null) pathItem.setSummary(bundle.getString(pathItem.getSummary()));
            if (pathItem.getDescription() != null) pathItem.setDescription(bundle.getString(pathItem.getDescription()));

            if (pathItem.readOperations() != null) {
                for (Operation operation : pathItem.readOperations()) {
                    if (operation.getSummary() != null) operation.setSummary(bundle.getString(operation.getSummary()));
                    if (operation.getDescription() != null) operation.setDescription(bundle.getString(operation.getDescription()));

                    if (operation.getParameters() != null) {
                        for (Parameter parameter : operation.getParameters()) {
                            if (parameter.getDescription() != null) parameter.setDescription(bundle.getString(parameter.getDescription()));
                        }
                    }

                    if (operation.getResponses() != null) {
                        for (Map.Entry<String, ApiResponse> apiResponseEntry : operation.getResponses().entrySet()) {
                            String translationCode = apiResponseEntry.getValue().getDescription();
                            if (translationCode != null) apiResponseEntry.getValue().setDescription(bundle.getString(translationCode));
                        }
                    }

                }
            }

        }
    }
    
    private void changeServer() {
        for (Server server : openAPI.getServers()) {
            if (server.getUrl().equalsIgnoreCase("BASE_SERVER")) {
                server.setUrl(uriInfo.getBaseUri().toString());
            }
        }
    }

    public OpenAPI translate(OpenAPI openAPI, UriInfo uri) {

        Cloner cloner = new Cloner();

        this.openAPI = cloner.deepClone(openAPI);
        this.uriInfo = uri;

        translateInfoDescription();
        translateTagsDescription();
        translatePathsInfos();
        changeServer();
        return this.openAPI;
    }
}
