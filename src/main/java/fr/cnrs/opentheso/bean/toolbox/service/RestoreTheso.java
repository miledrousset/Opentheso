/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.service;

import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
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
    
}
