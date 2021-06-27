package fr.cnrs.opentheso.virtuoso;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.primefaces.model.StreamedContent;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SynchroSparql implements Runnable {

    private SparqlStruct sparqlStruct;
    private List<NodeLangTheso> liste_lang;
    private List<NodeGroup> liste_group;

    @Override
    public void run(){
        System.out.println("I'm here !! ");

        VirtGraph graph = new VirtGraph(sparqlStruct.getGraph(),
                sparqlStruct.getAdresseServeur().replaceAll("http","jdbc:virtuoso").trim()+":1111",
                sparqlStruct.getNom_d_utilisateur(),
                sparqlStruct.getMot_de_passe());

        try{
            VirtuosoUpdateFactory.create("CLEAR GRAPH <"+sparqlStruct.getGraph()+">", graph).exec();

            Model model = ModelFactory.createDefaultModel();

            DownloadBean downloadBean = new DownloadBean();
            StreamedContent file = downloadBean.thesoToFile(sparqlStruct.getThesaurus(), liste_lang, liste_group, 0);

            //File outputFile = new File(file.getName());
            //FileUtils.copyInputStreamToFile(file.getStream(), outputFile);

            model.read(file.getStream(),null);
            StmtIterator iter = model.listStatements();
            while(iter.hasNext()){
                Statement stmt=iter.nextStatement();
                Resource subject=stmt.getSubject();
                Property predicate=stmt.getPredicate();
                RDFNode object=stmt.getObject();
                Triple tri = new Triple(subject.asNode(),predicate.asNode(),object.asNode());
                graph.add(tri);
            }
        } catch(Exception e){
            System.out.println(">>>> " + e.getMessage());
        } finally {
            graph.close();
        }
  
    }

    public SparqlStruct getSparqlStruct() {
        return sparqlStruct;
    }

    public void setSparqlStruct(SparqlStruct sparqlStruct) {
        this.sparqlStruct = sparqlStruct;
    }

    public List<NodeLangTheso> getListe_lang() {
        return liste_lang;
    }

    public void setListe_lang(List<NodeLangTheso> liste_lang) {
        this.liste_lang = liste_lang;
    }

    public List<NodeGroup> getListe_group() {
        return liste_group;
    }

    public void setListe_group(List<NodeGroup> liste_group) {
        this.liste_group = liste_group;
    }

}
