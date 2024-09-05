/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.handle;

import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import lombok.Data;

/**
 *
 * @author miled.rousset
 */
@Data
public class HandleHelper {
    
    private NodePreference nodePreference;
    private String message;
    
    public HandleHelper(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }
    
    /**
     * Permet d'ajouter un identifiant Handle
     * @param privateUri
     * @return
     */
    public String addIdHandle(String privateUri) {
        if (nodePreference == null) {
            return null;
        }
        if (!nodePreference.isUseHandle()) {
            return null;
        }

        HandleClient handleClient = new HandleClient();
        String newId = getNewHandleId();
        newId = nodePreference.getPrefixIdHandle() + "/"
                + nodePreference.getPrivatePrefixHandle() + newId;

        String jsonData = handleClient.getJsonData(nodePreference.getCheminSite() + privateUri);//"?idc=" + idConcept + "&idt=" + idThesaurus);
        String idHandle = handleClient.putHandle(
                nodePreference.getPassHandle(),
                nodePreference.getPathKeyHandle(),
                nodePreference.getPathCertHandle(),
                nodePreference.getUrlApiHandle(),
                newId,
                jsonData);
        if (idHandle == null) {
            message = handleClient.getMessage();
            return null;
        }
        return idHandle;
    }
    
    /**
     * permet de genérer un identifiant unique pour Handle on controle la
     * présence de l'identifiant sur handle.net si oui, on regénère un autre.
     *
     * @return
     */
    private String getNewHandleId() {
        ToolsHelper toolsHelper = new ToolsHelper();
        boolean duplicateId = true;
        String idHandle = null;
        HandleClient handleClient = new HandleClient();

        while (duplicateId) {
            idHandle = toolsHelper.getNewId(10, false, false);
            if (!handleClient.isHandleExist(
                    nodePreference.getUrlApiHandle(),
                    nodePreference.getPrefixIdHandle() + "/" + idHandle)) {
                duplicateId = false;
            }
            if (!handleClient.isHandleExist(
                    " https://hdl.handle.net/",
                    nodePreference.getPrefixIdHandle() + "/" + idHandle)) {
                duplicateId = false;
            }
        }
        return idHandle;
    }    
 
    /**
     * Permet de supprimer un identifiant Handle de la
     * plateforme (handle.net) via l'API REST
     */
    public boolean deleteIdHandle(String idHandle) {
        /**
         * récupération du code Handle via WebServices
         *
         */
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }

        HandleClient handleClient = new HandleClient();
        boolean status = handleClient.deleteHandle(
                nodePreference.getPassHandle(),
                nodePreference.getPathKeyHandle(),
                nodePreference.getPathCertHandle(),
                nodePreference.getUrlApiHandle(),
                idHandle
        );
        if (!status) {
            message = handleClient.getMessage();
            return false;
        }
        return true;
    }

    /**
     * Permet de supprimer tous les identifiants Handle 
     * de la plateforme (handle.net) via l'API REST pour un thésaurus donné
     * suite à une suppression d'un thésaurus
     */
    public boolean deleteAllIdHandle(ArrayList<String> tabIdHandle) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        HandleClient handleClient = new HandleClient();
        boolean status;
        boolean first = true;

        for (String idHandle : tabIdHandle) {
            status = handleClient.deleteHandle(
                    nodePreference.getPassHandle(),
                    nodePreference.getPathKeyHandle(),
                    nodePreference.getPathCertHandle(),
                    nodePreference.getUrlApiHandle(),
                    idHandle
            );
            if (!status) {
                if (first) {
                    message = "Id handle non supprimé :\n ";
                    first = false;
                }
                message = message + idHandle + " ## ";
            }
        }
        return true;
    }
}
