package fr.cnrs.opentheso.core.imports.rdf4j.nouvelle;

import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.InputStream;


public class ReadRDF4JNewGen {

    public SKOSXmlDocument readRdfFlux(InputStream inputStream, RDFFormat rdfFormat, String defaultLang) throws IOException {

        SKOSXmlDocument skosXmlDocument = new SKOSXmlDocument();
        RDFParser parser = Rio.createParser(rdfFormat);

        if (RDFFormat.RDFJSON.equals(rdfFormat)) {
            parser.setRDFHandler(new ReadJsonFlux(skosXmlDocument, defaultLang));
        } else {
            parser.setRDFHandler(new ReadCommunFlux(skosXmlDocument, defaultLang));
        }

        parser.parse(inputStream, "");

        return skosXmlDocument;
    }
}
