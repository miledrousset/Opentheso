/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.service;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "restoreTheso")
@RequestScoped
public class RestoreTheso implements Serializable {
    @Inject private Connect connect;
    
    @PostConstruct
    public void init() {
    }    
    @PreDestroy
    public void clear(){
    }

    private boolean overwrite = false;
    private String naan;
    private String prefix;
    
    private boolean overwriteLocalArk = false;


    /**
     * Creates a new instance of RestoreTheso
     */
    public RestoreTheso() {
    }
    
    public void reorganizing(String idTheso) {
        if(idTheso == null || idTheso.isEmpty()) return;
        
        FacesContext fc = FacesContext.getCurrentInstance();        
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        // nettoyage des null et d'espaces
        if (!thesaurusHelper.cleaningTheso(connect.getPoolConnexion(), idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des espaces et des null")); 
            return;
        }

        // permet de detecter les concepts TT erronnés : si le concept n'a pas de BT, alors, il est forcement TopTerme (en ignorant les candidtas)
        if(!reorganizingTopTerm(idTheso)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la correction des TT")); 
            return;            
        }

        // complète le thésaurus par les relations qui manquent NT ou BT
        if(!reorganizingTheso(idTheso)){
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la correction des NT BT")); 
            return;              
        }

        // permet de supprimer les BT pour un TopTerm, c'est incohérent
        if(!removeBTofTopTerm(idTheso)){
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des BT pour les topTermes")); 
            return;             
        }

        // permet de supprimer les relations en boucle (100 -> BT -> 100) ou  (100 -> NT -> 100)ou (100 -> RT -> 100)
        if(!removeLoopRelations(idTheso)){
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression des relations en boucle")); 
        }
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Correction réussie !!!"));
    }    
    
    private boolean reorganizingTopTerm(String idTheso) {
        return new ToolsHelper().reorganizingTopTerm(connect.getPoolConnexion(), idTheso);        
    }
    private boolean reorganizingTheso(String idTheso) {
        return new ToolsHelper().reorganizingTheso(connect.getPoolConnexion(), idTheso);
    }        
    private boolean removeBTofTopTerm(String idTheso){
        return new ToolsHelper().removeBTofTopTerm(connect.getPoolConnexion(), idTheso);
    }
    private boolean removeLoopRelations(String idTheso) {
        if(!new ToolsHelper().removeLoopRelations(connect.getPoolConnexion(), "BT", idTheso))
            return false;
        if(!new ToolsHelper().removeLoopRelations(connect.getPoolConnexion(), "NT", idTheso))
            return false;
        return new ToolsHelper().removeLoopRelations(connect.getPoolConnexion(), "RT", idTheso);
    }    
    
    public void switchRolesFromTermToConcept(String idTheso) {
        String lang = connect.getWorkLanguage();
       
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        
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
        ConceptHelper conceptHelper = new ConceptHelper();
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
        ConceptHelper conceptHelper = new ConceptHelper();
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
        
        ToolsHelper toolsHelper = new ToolsHelper();
        String idArk;
        
        for (String conceptId : allConcepts) {
            if(!overwriteLocalArk) {
                if(!conceptHelper.isHaveIdArk(connect.getPoolConnexion(), idTheso, conceptId)) {
                    idArk = toolsHelper.getNewId(nodePreference.getSizeIdArkLocal());              
                    conceptHelper.updateArkIdOfConcept(connect.getPoolConnexion(), conceptId, idTheso,
                            nodePreference.getNaanArkLocal() + "/" +
                            nodePreference.getPrefixArkLocal() + idArk);
                    count++;
                }
            } else {
                idArk = toolsHelper.getNewId(nodePreference.getSizeIdArkLocal());              
                conceptHelper.updateArkIdOfConcept(connect.getPoolConnexion(), conceptId, idTheso,
                        nodePreference.getNaanArkLocal() + "/" +
                        nodePreference.getPrefixArkLocal() + idArk);
                count++;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concepts changés: " + count));
    }      

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getNaan() {
        return naan;
    }

    public void setNaan(String naan) {
        this.naan = naan;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isOverwriteLocalArk() {
        return overwriteLocalArk;
    }

    public void setOverwriteLocalArk(boolean overwriteLocalArk) {
        this.overwriteLocalArk = overwriteLocalArk;
    }
    
    
    
}