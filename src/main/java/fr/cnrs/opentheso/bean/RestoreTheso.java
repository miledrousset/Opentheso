package fr.cnrs.opentheso.bean;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.relations.NodeRelation;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.PreferredTermRepository;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.TermRepository;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.RelationService;
import fr.cnrs.opentheso.utils.ToolsHelper;
import fr.cnrs.opentheso.utils.DateUtils;

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
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Data
@Named(value = "restoreTheso")
@RequestScoped
public class RestoreTheso implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private ThesaurusRepository thesaurusRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private RelationService relationService;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private PreferredTermRepository preferredTermRepository;

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
        conceptIds = conceptHelper.getAllIdConceptOfThesaurus(idTheso);

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
        if(!groupService.deleteConceptsWithEmptyRelation(idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations vides"));
        }
        if(!groupService.deleteConceptsHavingRelationShipWithDeletedGroup(idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations interdites"));
        }
        if(!groupService.deleteConceptsHavingRelationShipWithDeletedConcept(idTheso)) {
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

        ArrayList<String> listIds = conceptHelper.getIdsOfBranchWithoutLoop(idConcept, idTheso);

        for (String id : listIds) {
            NodeRelation nodeRelation = relationsHelper.getLoopRelation(idTheso, idConcept);
            if(nodeRelation!= null){
                relationsHelper.deleteThisRelation(nodeRelation.getIdConcept2(), idTheso, "BT", nodeRelation.getIdConcept1());
                relationsHelper.deleteThisRelation(nodeRelation.getIdConcept1(), idTheso, "NT", nodeRelation.getIdConcept2());
            }
        }
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Correction terminée"));

        PrimeFaces.current().executeScript("window.location.reload();");
    }

    public void reorganizing(String idTheso) {
        if(idTheso == null || idTheso.isEmpty()) return;

        FacesContext fc = FacesContext.getCurrentInstance();
        // nettoyage des null et d'espaces
        if (!thesaurusHelper.cleaningTheso(idTheso)) {
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
        return reorganizingTopTermInThesaurus(idTheso);
    }

    private boolean reorganizingTopTermInThesaurus(String idThesaurus) {

        // récupération des TopTerms en tenant compte des éventuelles erreurs donc par déduction
        ArrayList<String> listIds = relationsHelper.getListIdOfTopTermForRepair(idThesaurus);

        for (String idConcept : listIds) {
            conceptRepository.setTopConceptTag(true, idConcept, idThesaurus);
        }
        return true;
    }

    private boolean reorganizingTheso(String idTheso) {
        return reorganizingThesaurus(idTheso);
    }

    private boolean removeTopTermForConceptWithBT(String idThesaurus){
        // récupération de tous les Id TT du thésaurus
        var thesaurus = thesaurusRepository.findById(idThesaurus);
        var tabIdTT = conceptRepository.findAllByThesaurusAndTopConceptAndStatusNotLike(thesaurus.get(), true, "CA");
        for (var concept : tabIdTT) {
            if (relationsHelper.isConceptHaveRelationBT(concept.getIdConcept(), idThesaurus)) {
                conceptRepository.setTopConceptTag(false, concept.getIdConcept(), idThesaurus);
            }
        }
        return true;
    }

    private boolean removeSameRelations(String idTheso) {
        if(!removeSameRelations("BT", idTheso))
            return false;
        if(!removeSameRelations("NT", idTheso))
            return false;
        return removeSameRelations("RT", idTheso);
    }

    public void switchRolesFromTermToConcept(String idTheso) {

        String lang = workLanguage;
        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(idTheso);
        for (String idConcept : allConcepts) {

            var preferredTerm = preferredTermRepository.findByIdThesaurusAndIdConcept(idTheso, idConcept);

            if (preferredTerm.isPresent()) {
                if(!conceptHelper.isHaveCreator(idTheso, idConcept)) {
                    var term = termRepository.findByIdTermAndIdThesaurusAndLang(preferredTerm.get().getIdTerm(), idTheso, lang);
                    if (term.isPresent()) {
                        if(term.get().getCreator() != -1)
                            conceptHelper.setCreator(idTheso, idConcept, term.get().getCreator());
                    }
                }
                if(!conceptHelper.isHaveContributor(idTheso, idConcept)) {
                    var term = termRepository.findByIdTermAndIdThesaurusAndLang(preferredTerm.get().getIdTerm(), idTheso, lang);
                    if (term.isPresent()) {
                        if(term.get().getContributor() != -1)
                            conceptHelper.setContributor(idTheso, idConcept, term.get().getContributor());
                    }
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

        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(idTheso);

        for (String conceptId : allConcepts) {
            if(!overwrite) {
                if(!conceptHelper.isHaveIdArk(idTheso, conceptId)) {
                    conceptHelper.updateArkIdOfConcept(conceptId, idTheso, naan + "/" + prefix + conceptId);
                    count++;
                }
            } else {
                conceptHelper.updateArkIdOfConcept(conceptId, idTheso, naan + "/" + prefix + conceptId);
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
    public void generateArkLacal(String idTheso, Preferences nodePreference) {

        int count = 0;

        if(nodePreference == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de paramètres !! "));
            return;
        }
        if(nodePreference.getNaanArkLocal() == null || nodePreference.getNaanArkLocal().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de Naan !! "));
            return;
        }

        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(idTheso);

        String idArk;

        for (String conceptId : allConcepts) {
            if(!overwriteLocalArk) {
                if(!conceptHelper.isHaveIdArk(idTheso, conceptId)) {
                    idArk = ToolsHelper.getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercaseForArk(), true);
                    conceptHelper.updateArkIdOfConcept(conceptId, idTheso,
                            nodePreference.getNaanArkLocal() + "/" +
                                    nodePreference.getPrefixArkLocal() + idArk);
                    count++;
                }
            } else {
                idArk = ToolsHelper.getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercaseForArk(), true);
                conceptHelper.updateArkIdOfConcept(conceptId, idTheso,
                        nodePreference.getNaanArkLocal() + "/" +
                                nodePreference.getPrefixArkLocal() + idArk);
                count++;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concepts changés: " + count));
    }

    private boolean removeSameRelations(String role, String idThesaurus) {

        // récupération des relations en Loop
        ArrayList<HierarchicalRelationship> tabRelations = relationsHelper.getListLoopRelations(role, idThesaurus);
        if (!tabRelations.isEmpty()) {
            for (HierarchicalRelationship relation : tabRelations) {
                relationsHelper.deleteThisRelation(relation.getIdConcept1(), idThesaurus,
                        role, relation.getIdConcept2());
            }
        }
        return true;
    }

    /**
     * Fonction qui permet de restructurer le thésaurus en ajoutant les NT et les BT qui manquent
     * elle permet aussi de sortir les termes orphelins si nécessaire
     */
    @Transactional
    public boolean reorganizingThesaurus(String idThesaurus) {

        ArrayList<String> idBT;
        ArrayList<String> idConcept1WhereIsNT;

        // récupération de tous les Id concepts du thésaurus
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurus(idThesaurus);

        for (String idConcept : tabIdConcept) {
            idBT = relationsHelper.getListIdBT(idConcept, idThesaurus);
            idConcept1WhereIsNT = relationsHelper.getListIdWhichHaveNt(idConcept, idThesaurus);
            if (idBT.isEmpty() && idConcept1WhereIsNT.isEmpty()) {
                if (!conceptHelper.isTopConcept(idConcept, idThesaurus)) {
                    // le concept est orphelin
                    conceptRepository.setTopConceptTag(true, idConcept, idThesaurus);
                }
            } else {
                if (!(idBT.containsAll(idConcept1WhereIsNT))) {
                    //alors il manque des BT
                    ArrayList<String> BTmiss = new ArrayList<>(idConcept1WhereIsNT);
                    BTmiss.removeAll(idBT);
                    //on ajoute la différence
                    for (String miss : BTmiss) {
                        relationService.addHierarchicalRelation(idConcept, idThesaurus, "BT", miss);
                    }
                }
                if (!(idConcept1WhereIsNT.containsAll(idBT))) {
                    //il manque des NT pour certain idBT
                    ArrayList<String> NTmiss = new ArrayList<>(idBT);
                    NTmiss.removeAll(idConcept1WhereIsNT);
                    //on jaoute la différence
                    for (String miss : NTmiss) {
                        relationService.addHierarchicalRelation(miss, idThesaurus, "NT", idConcept);
                    }
                }
            }
        }
        return false;
    }
}
