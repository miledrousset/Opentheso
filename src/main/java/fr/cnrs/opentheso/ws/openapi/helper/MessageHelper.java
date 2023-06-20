package fr.cnrs.opentheso.ws.openapi.helper;

/**
 *
 * @author julie
 */
public class MessageHelper {
    public static String errorMessage(String message, String typeContent) {
        typeContent = HeaderHelper.removeCharset(typeContent);
        switch (typeContent) {
            case CustomMediaType.APPLICATION_JSON:
            case CustomMediaType.APPLICATION_JSON_LD:
                return "{\"error\": \"" + message + "\"}";
            case CustomMediaType.APPLICATION_RDF:
                return "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description rdf:about=\"#error\"><error>" + message + "</error></rdf:Description></rdf:RDF>";
            case CustomMediaType.APPLICATION_TURTLE:
                return "@prefix : <#> .\n\n:error a :Error ;\n    :message \"" + message + "\" .";
            default:
                return message;
        }
    }

    public static String emptyMessage(String typeContent) {
        typeContent = HeaderHelper.removeCharset(typeContent);
        switch (typeContent) {
            case CustomMediaType.APPLICATION_JSON:
            case CustomMediaType.APPLICATION_JSON_LD:
                return "{}";
            case CustomMediaType.APPLICATION_RDF:
                return "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"></rdf:RDF>";
            case CustomMediaType.APPLICATION_TURTLE:
                return "@prefix : <#> .\n\n:";
            default:
                return "";
        }
    }
}
