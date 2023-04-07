package fr.cnrs.opentheso;

import fr.cnrs.opentheso.core.imports.rdf4j.nouvelle.ReadRDF4JNewGen;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;

import org.eclipse.rdf4j.rio.RDFFormat;

import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class TestReadRDF4J {

    public static void main(String[] args) throws IOException {

        File inputFile = new File("test.json");
        InputStream inputStream = new FileInputStream(inputFile);

        SKOSXmlDocument skosXmlDocument = new ReadRDF4JNewGen().readRdfFlux(inputStream, RDFFormat.RDFJSON, "fr");

        //SKOSXmlDocument skosXmlDocument = new ReadRdf4j(inputStream, 3, false, "fr").getsKOSXmlDocument();

        Assert.assertNotNull(skosXmlDocument);
    }
}
