package fr.cnrs.opentheso.virtuoso;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.primefaces.model.StreamedContent;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;


public class SynchroSparql implements Runnable {

    private final Log LOG = LogFactory.getLog(DownloadBean.class);

    private List<NodeLangTheso> liste_lang;
    private List<NodeGroup> liste_group;
    private Properties properties;

    @Override
    public void run(){

        LOG.info("Début du processes de synchronisation !");

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("hikari.properties");
        if (inputStream != null) {
            LOG.error("Impossible de lire le fichier de paramètrage hikari.properties !");
        }

        VirtGraph graph = null;
        try{

            properties = new Properties();
            properties.load(inputStream);

            graph = new VirtGraph(properties.getProperty("virtuoso.graph"),
                    properties.getProperty("virtuoso.url").replaceAll("http","jdbc:virtuoso").trim()+":1111",
                    properties.getProperty("virtuoso.user"),
                    properties.getProperty("virtuoso.password"));

            LOG.info("Création du graphe !");
            VirtuosoUpdateFactory.create("CLEAR GRAPH <" + properties.getProperty("virtuoso.graph")
                    + ">", graph).exec();

            Model model = ModelFactory.createDefaultModel();

            DownloadBean downloadBean = new DownloadBean();
            downloadBean.setConnect(openConnexionPool());

            LOG.info("Extraction du thésaurus !");
            StreamedContent file = downloadBean.thesoToFile(properties.getProperty("virtuoso.thesorus"), liste_lang, liste_group, 0);

            LOG.info("Envoie du fichier !");
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
            LOG.error("Erreur pendant le processus de synchronisation avec Virtuoso !");
        } finally {
            graph.close();
        }
        LOG.info("Fin du processus de synchronisation avec Virtuoso !");
    }

    private HikariDataSource openConnexionPool() {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("minimumIdle")));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("setMaximumPoolSize")));
        config.setAutoCommit(true);
        config.setIdleTimeout(Integer.parseInt(properties.getProperty("idleTimeout")));
        config.setConnectionTimeout(Integer.parseInt(properties.getProperty("connectionTimeout")));
        config.setConnectionTestQuery(properties.getProperty("connectionTestQuery"));
        config.setDataSourceClassName(properties.getProperty("dataSourceClassName"));

        config.addDataSourceProperty("user", properties.getProperty("dataSource.user"));
        config.addDataSourceProperty("password", properties.getProperty("dataSource.password"));
        config.addDataSourceProperty("databaseName", properties.getProperty("dataSource.databaseName"));
        config.addDataSourceProperty("serverName", properties.getProperty("dataSource.serverName"));
        config.addDataSourceProperty("portNumber", properties.getProperty("dataSource.serverPort"));

        HikariDataSource poolConnexion1 = new HikariDataSource(config);
        try {
            Connection conn = poolConnexion1.getConnection();
            if (conn == null) {
                return null;
            }
            conn.close();

        } catch (SQLException ex) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, ex.getClass().getName(), ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            poolConnexion1.close();
            return null;
        }
        return poolConnexion1;
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
