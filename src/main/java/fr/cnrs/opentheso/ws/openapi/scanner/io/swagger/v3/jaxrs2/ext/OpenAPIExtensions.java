package fr.cnrs.opentheso.ws.openapi.scanner.io.swagger.v3.jaxrs2.ext;

import io.swagger.v3.jaxrs2.DefaultParameterExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class OpenAPIExtensions {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIExtensions.class);

    private static List<io.swagger.v3.jaxrs2.ext.OpenAPIExtension> extensions = null;

    public static List<io.swagger.v3.jaxrs2.ext.OpenAPIExtension> getExtensions() {
        return extensions;
    }

    public static void setExtensions(List<io.swagger.v3.jaxrs2.ext.OpenAPIExtension> ext) {
        extensions = ext;
    }

    public static Iterator<io.swagger.v3.jaxrs2.ext.OpenAPIExtension> chain() {
        return extensions.iterator();
    }

    static {
        extensions = new ArrayList<>();
        ServiceLoader<io.swagger.v3.jaxrs2.ext.OpenAPIExtension> loader = ServiceLoader.load(io.swagger.v3.jaxrs2.ext.OpenAPIExtension.class);
        for (OpenAPIExtension ext : loader) {
            LOGGER.debug("adding extension {}", ext);
            extensions.add(ext);
        }
        extensions.add(new DefaultParameterExtension());
    }
}