/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.ark;

import java.util.Properties;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeMetaData;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;

/**
 *
 * @author miled.rousset
 */
public class ArkHelper {
    
    private NodePreference nodePreference;
    private String message;
    
    private String idArk;
    private String idHandle;
    private ArkClientRest arkClientRest;
    
    public ArkHelper(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }
    
    public boolean login() {
        //initialisation des valeurs 
        arkClientRest = new ArkClientRest();

        Properties propertiesArk = new Properties();
        propertiesArk.setProperty("serverHost", "https://ark.mom.fr/Arkeo");//nodePreference.getServeurArk());
        propertiesArk.setProperty("idNaan", nodePreference.getIdNaan());
        propertiesArk.setProperty("user", nodePreference.getUserArk());
        propertiesArk.setProperty("password", nodePreference.getPassArk());
        arkClientRest.setPropertiesArk(propertiesArk);        
        return arkClientRest.login();
    }
    
    public boolean isArkExistOnServer(String idArk) {
        return  arkClientRest.isArkExist(idArk);
    }
    
    
    public boolean isHandleExistOnServer(String idHandle) {
        return arkClientRest.isHandleExist(idHandle);
    }
    
    /**
     * Permet de supprimer un Handle en local et sur handle.net
     * @param idHandle
     * @param privateUri
     * @param nodeMetaData
     * @return
     */
    public boolean deleteHandle(
            String idHandle,
            String privateUri,
            NodeMetaData nodeMetaData) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }
        
        NodeJson nodeJson = new NodeJson();
        nodeJson.setArk("");
        
        /// c'est le seul argument qui compte pour la suppression 
        nodeJson.setHandle(idHandle);
        
        
        nodeJson.setUrlTarget(nodePreference.getCheminSite() + privateUri); //"?idc=" + idConcept + "&idt=" + idThesaurus);
        nodeJson.setTitle(nodeMetaData.getTitle());
        nodeJson.setCreator(nodeMetaData.getCreator());
        nodeJson.setDcElements(nodeMetaData.getDcElementsList()); // n'est pas encore exploité
        nodeJson.setType(nodePreference.getPrefixArk());  //pcrt : p= pactols, crt=code DCMI pour collection
        nodeJson.setLanguage(nodePreference.getSourceLang());
        nodeJson.setNaan(nodePreference.getIdNaan());
        nodeJson.setHandle_prefix("20.500.11859");
       
        // création de l'identifiant Ark et Handle 
        if(!arkClientRest.deleteHandle(nodeJson.getJsonString())) {
            message = arkClientRest.getMessage();
            return false;
        }
        message = arkClientRest.getMessage();
        return true;
    }        
    
    /**
     * Permet de créer d'ajouter un identifiant ARK sur le serveur 
     * en pamaramètre l'Id Ark qu'on souhaite créer + identifiant Handle (serveur MOM)
     * @param idArkProvided
     * @param privateUri
     * @param nodeMetaData
     * @return
     */
    public boolean addArkWithProvidedId(
            String idArkProvided,
            String privateUri,
            NodeMetaData nodeMetaData) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }

        idHandle = null;
        

        
        NodeJson nodeJson = new NodeJson();
        nodeJson.setArk(idArkProvided);
        nodeJson.setUrlTarget(nodePreference.getCheminSite() + privateUri); //"?idc=" + idConcept + "&idt=" + idThesaurus);
        nodeJson.setTitle(nodeMetaData.getTitle());
        nodeJson.setCreator(nodeMetaData.getCreator());
        nodeJson.setDcElements(nodeMetaData.getDcElementsList()); // n'est pas encore exploité
        nodeJson.setType(nodePreference.getPrefixArk());  //pcrt : p= pactols, crt=code DCMI pour collection
        nodeJson.setLanguage(nodePreference.getSourceLang());
        nodeJson.setNaan(nodePreference.getIdNaan());
        nodeJson.setHandle_prefix("20.500.11859");
       
        // création de l'identifiant Ark et Handle 
        if(!arkClientRest.addArk(nodeJson.getJsonString())) return false;

        idArk = arkClientRest.getIdArk();
        idHandle = arkClientRest.getIdHandle();
        if (idArk == null) {
            message = "La connexion Ark a échouée";
            return false;
        }
        if (idHandle == null) {
            message = "La connexion Handle a échouée";
            return false;
        }
        return true;
    }    
    
    
    public boolean getArkFromArkeo(String ark){
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }
        idArk = null;
        idHandle = null;
        
        if(!arkClientRest.getArk(ark)) return false;

        idArk = arkClientRest.getIdArk();
        idHandle = arkClientRest.getIdHandle();
        if (idArk == null) {
            message = "Erreur Ark !!";
            return false;
        }
        if (idHandle == null) {
            message = "Erreur Handle !!";
            return false;
        }
        return true;        
    }
    
    
    /**
     * Permet de créer un identifiant ARK et un identifiant Handle (serveur MOM)
     * @param privateUri
     * @param nodeMetaData
     * @return
     */
    public boolean addArk(String privateUri,
            NodeMetaData nodeMetaData) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }
        idArk = null;
        idHandle = null;
        

        
        NodeJson nodeJson = new NodeJson();
        nodeJson.setUrlTarget(nodePreference.getCheminSite() + privateUri); //"?idc=" + idConcept + "&idt=" + idThesaurus);
        nodeJson.setTitle(nodeMetaData.getTitle());
        nodeJson.setCreator(nodeMetaData.getCreator());
        nodeJson.setDcElements(nodeMetaData.getDcElementsList()); // n'est pas encore exploité
        nodeJson.setType(nodePreference.getPrefixArk());  //pcrt : préfixe pour sous distinguer une sous entité:  p= pactols, crt= code DCMI pour collection
        nodeJson.setLanguage(nodePreference.getSourceLang());
        nodeJson.setNaan(nodePreference.getIdNaan());
        nodeJson.setHandle_prefix("20.500.11859");
       
        // création de l'identifiant Ark et Handle 
        String jsonDatas = nodeJson.getJsonString();
        if(jsonDatas == null) return false;
        if(!arkClientRest.addArk(jsonDatas)) return false;

        idArk = arkClientRest.getIdArk();
        idHandle = arkClientRest.getIdHandle();
        if (idArk == null) {
            message = "La connexion Ark a échouée";
            return false;
        }
        if (idHandle == null) {
            message = "La connexion Handle a échouée";
            return false;
        }
        return true;
    }
    
    /**
     * Permet de mettre à jour un objet ARK suivant l'Id Ark (serveur MOM)
     * @param idArk
     * @param privateUri
     * @param nodeMetaData
     * @return
     */
    public boolean updateArk(
            String idArk,
            String privateUri,
            NodeMetaData nodeMetaData) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }
        idHandle = null;
     
        NodeJson nodeJson = new NodeJson();
        nodeJson.setUrlTarget(nodePreference.getCheminSite() + privateUri); //"?idc=" + idConcept + "&idt=" + idThesaurus);
        nodeJson.setArk(idArk);
        nodeJson.setTitle(nodeMetaData.getTitle());
        nodeJson.setCreator(nodeMetaData.getCreator());
        nodeJson.setDcElements(nodeMetaData.getDcElementsList()); // n'est pas encore exploité
        nodeJson.setType(nodePreference.getPrefixArk());  //pcrt : p= pactols, crt=code DCMI pour collection
        nodeJson.setLanguage(nodePreference.getSourceLang());
        nodeJson.setNaan(nodePreference.getIdNaan());
        nodeJson.setHandle_prefix("20.500.11859");
       
//        if(!arkClientRest.login()) return false;
        // création de l'identifiant Ark et Handle 
        if(!arkClientRest.updateArk(nodeJson.getJsonString())) return false;

        idHandle = arkClientRest.getIdHandle();

        if (idHandle == null) {
            message = "La connexion Handle a échouée";
            return false;
        }
        return true;
    }    
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIdArk() {
        return idArk;
    }

    public void setIdArk(String idArk) {
        this.idArk = idArk;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }
    
    
    
}
