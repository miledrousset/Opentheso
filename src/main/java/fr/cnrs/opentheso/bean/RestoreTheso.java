package fr.cnrs.opentheso.bean;

import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.ToolsHelper;
import fr.cnrs.opentheso.utils.DateUtils;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;


@Data
@Named(value = "restoreTheso")
@RequestScoped
public class RestoreTheso implements Serializable {

    @Autowired
    private Connect connect;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private ToolsHelper toolsHelper;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private TermHelper termHelper;

    private boolean overwrite, overwriteLocalArk;
    private String naan, prefix;


    public void generateSitemap(String idTheso, String nameTheso) {
        if(idTheso == null || idTheso.isEmpty()) return;

        /** #MR
         * Le mécanisme de gé,ération du fichier fontionne,
         * il reste seulement à pouvoir l'enregistrer à la racine du site
         */
        URL url = this.getClass().getResource("/");
        String fileName =  idTheso + ".xml";


        ArrayList<String> conceptIds;

        FacesContext fc = FacesContext.getCurrentInstance();
        conceptIds = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);

        Writer writeFile;
        try {
            File file = new File(new URI(url.toString()) + fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writeFile.write(getHeader());
            writeFile.write(getDatas(conceptIds, idTheso));
            writeFile.write(getEnd());
            writeFile.close();
        } catch (IOException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", e.getMessage()));
            return;
        } catch (URISyntaxException ex) {
            Logger.getLogger(RestoreTheso.class.getName()).log(Level.SEVERE, null, ex);
        }
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "l'export du sitemap a réussi !!! " + fileName));
    }

    private String getDatas(ArrayList<String> conceptIds, String idTheso){
        DateUtils fileUtilities = new DateUtils();
        String date = fileUtilities.getDate();
        StringBuilder stringBuilder = new StringBuilder();

        for (String conceptId : conceptIds) {
            stringBuilder.append(getLine(getUri(conceptId, idTheso), date));
        }
        return stringBuilder.toString();
    }

    private String getHeader(){
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        header = header + "\n" + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n";
        return header;
    }

    private String getLine(String url, String date){
        String line = "  <url>\n";
        line = line + "    <loc>" + url + "</loc>\n";
        line = line + "    <lastmod>" + date + "</lastmod>\n";
        line = line + "  </url>\n";
        return line;
    }

    private String getEnd(){
        return "</urlset>";
    }

    private String getUri(String idConcept, String idTheso){
        return  getPath() + "/?idc=" + idConcept + "&amp;idt=" + idTheso;
    }

    /**
     * permet de retourner le Path de l'application
     * exp:  //http://localhost:8082/opentheso2
     * @return
     */
    private String getPath(){
        if(FacesContext.getCurrentInstance() == null) {
            return null;
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        path = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        return path;
    }






    /**
     * permet de corriger l'appartenance d'un cocnept à une collection,
     * si la collection n'existe plus, on supprime l'appartenance de ces concepts à la collection en question
     * @param idTheso
     */
    public void reorganizeConceptsAndCollections(String idTheso) {
        if(idTheso == null || idTheso.isEmpty()) return;

        FacesContext fc = FacesContext.getCurrentInstance();

        // supprimer le concept qui ont une relation vers un groupe vide
        // liste des concepts de la table concept-group-concept qui ont une relation vers un groupe qui n'existe plus
        if(!groupHelper.deleteConceptsWithEmptyRelation(connect.getPoolConnexion(), idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations vides"));
        }
        if(!groupHelper.deleteConceptsHavingRelationShipWithDeletedGroup(connect.getPoolConnexion(), idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations interdites"));
        }
        if(!groupHelper.deleteConceptsHavingRelationShipWithDeletedConcept(connect.getPoolConnexion(), idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations interdites"));
        }

        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Correction réussie !!!"));
    }

    /**
     * Permet de supprimer les relations de type (a -> BT -> b et b -> BT -> a )
     * parcours toute la bracnche en partant du concept en paramètre
     * @param idTheso
     * @param idConcept
     */
    public void deleteLoopRelations(String idTheso, String idConcept){
        FacesContext fc = FacesContext.getCurrentInstance();
        if(StringUtils.isEmpty(idTheso) || StringUtils.isAllEmpty(idConcept)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur manque de paramètres"));
            return;
        }

        ArrayList<String> listIds = conceptHelper.getIdsOfBranchWithoutLoop(connect.getPoolConnexion(), idConcept, idTheso);

        for (String id : listIds) {
            toolsHelper.removeLoopRelation(connect.getPoolConnexion(), idTheso, id);
        }
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Correction terminée"));

        PrimeFaces.current().executeScript("window.location.reload();");
    }

    public void reorganizing(String idTheso) {
        if(idTheso == null || idTheso.isEmpty()) return;

        FacesContext fc = FacesContext.getCurrentInstance();
        // nettoyage des null et d'espaces
        if (!thesaurusHelper.cleaningTheso(connect.getPoolConnexion(), idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des espaces et des null"));
            return;
        }

        // permet de detecter les concepts TT erronnés : si le concept n'a pas de BT, alors, il est forcement TopTerme (en ignorant les candidats)
        if(!reorganizingTopTerm(idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la correction des TT"));
            return;
        }

        // complète le thésaurus par les relations qui manquent NT ou BT
        if(!reorganizingTheso(idTheso)){
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la correction des NT BT"));
            return;
        }

        // permet de detecter si le concept à un BT et il est aussi TopTerm, c'est incohérent, il faut alors supprimer le status TopTerm
        if(!removeTopTermForConceptWithBT(idTheso)){
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des BT pour les topTermes"));
            return;
        }

        // permet de supprimer les relations en boucle (100 -> BT -> 100) ou  (100 -> NT -> 100)ou (100 -> RT -> 100)
        if(!removeSameRelations(idTheso)){
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations en boucle"));
        }
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Correction réussie !!!"));
    }

    private boolean reorganizingTopTerm(String idTheso) {
        return toolsHelper.reorganizingTopTerm(connect.getPoolConnexion(), idTheso);
    }
    private boolean reorganizingTheso(String idTheso) {
        return toolsHelper.reorganizingTheso(connect.getPoolConnexion(), idTheso);
    }

    private boolean removeTopTermForConceptWithBT(String idTheso){
        return toolsHelper.removeTopTermForConceptWithBT(connect.getPoolConnexion(), idTheso);
    }

    private boolean removeSameRelations(String idTheso) {
        if(!toolsHelper.removeSameRelations(connect.getPoolConnexion(), "BT", idTheso))
            return false;
        if(!toolsHelper.removeSameRelations(connect.getPoolConnexion(), "NT", idTheso))
            return false;
        return toolsHelper.removeSameRelations(connect.getPoolConnexion(), "RT", idTheso);
    }

    public void switchRolesFromTermToConcept(String idTheso) {

        String lang = connect.getWorkLanguage();

        String idTerm;
        int idCreator;
        int idContributor;
        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);

        for (String idConcept : allConcepts) {
            if(!conceptHelper.isHaveCreator(connect.getPoolConnexion(), idTheso, idConcept)) {
                idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(), idConcept, idTheso);
                if(idTerm != null) {
                    idCreator = termHelper.getCreator(connect.getPoolConnexion(), idTheso, idTerm, lang);
                    if(idCreator != -1)
                        conceptHelper.setCreator(connect.getPoolConnexion(), idTheso, idConcept, idCreator);
                }
            }
            if(!conceptHelper.isHaveContributor(connect.getPoolConnexion(), idTheso, idConcept)) {
                idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(), idConcept, idTheso);
                if(idTerm != null) {
                    idContributor = termHelper.getContributor(connect.getPoolConnexion(), idTheso, idTerm, lang);
                    if(idContributor != -1)
                        conceptHelper.setContributor(connect.getPoolConnexion(), idTheso, idConcept, idContributor);
                }
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Correction réussie !!!"));
    }

    /**
     * permet de générer les id Ark d'après l'id du concept,
     * si overwrite est activé, on écrase tout et on recommence avec des nouveaus Id Ark
     * @param idTheso
     */
    public void generateArkFromConceptId(String idTheso) {

        int count = 0;
        if(naan == null || naan.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de Naan !! "));
            return;
        }

        if(prefix == null || prefix.isEmpty())
            prefix = "";
        else
            prefix = prefix.trim();

        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);

        for (String conceptId : allConcepts) {
            if(!overwrite) {
                if(!conceptHelper.isHaveIdArk(connect.getPoolConnexion(), idTheso, conceptId)) {
                    conceptHelper.updateArkIdOfConcept(connect.getPoolConnexion(), conceptId, idTheso, naan + "/" + prefix + conceptId);
                    count++;
                }
            } else {
                conceptHelper.updateArkIdOfConcept(connect.getPoolConnexion(), conceptId, idTheso, naan + "/" + prefix + conceptId);
                count++;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concepts changés: " + count));
    }

    /**
     * permet de générer les id Ark en local en se basant au paramètre prédéfini dans Identifiant
     * si overwrite est activé, on écrase tout et on recommence avec des nouveaus Id Ark
     * @param idTheso
     * @param nodePreference
     */
    public void generateArkLacal(String idTheso, NodePreference nodePreference) {

        int count = 0;

        if(nodePreference == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de paramètres !! "));
            return;
        }
        if(nodePreference.getNaanArkLocal() == null || nodePreference.getNaanArkLocal().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de Naan !! "));
            return;
        }

        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);

        String idArk;

        for (String conceptId : allConcepts) {
            if(!overwriteLocalArk) {
                if(!conceptHelper.isHaveIdArk(connect.getPoolConnexion(), idTheso, conceptId)) {
                    idArk = toolsHelper.getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercase_for_ark(), true);
                    conceptHelper.updateArkIdOfConcept(connect.getPoolConnexion(), conceptId, idTheso,
                            nodePreference.getNaanArkLocal() + "/" +
                                    nodePreference.getPrefixArkLocal() + idArk);
                    count++;
                }
            } else {
                idArk = toolsHelper.getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercase_for_ark(), true);
                conceptHelper.updateArkIdOfConcept(connect.getPoolConnexion(), conceptId, idTheso,
                        nodePreference.getNaanArkLocal() + "/" +
                                nodePreference.getPrefixArkLocal() + idArk);
                count++;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concepts changés: " + count));
    }
}
