package fr.cnrs.opentheso.ws.handle;

import java.util.ArrayList;
import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.utils.ToolsHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Data
@Service
public class HandleHelper {

    @Autowired
    private HandleClient handleClient;

    private String message;
    
    /**
     * Permet d'ajouter un identifiant Handle
     * @param privateUri
     * @return
     */
    public String addIdHandle(String privateUri, Preferences nodePreference) {
        if (nodePreference == null) {
            return null;
        }
        if (!nodePreference.isUseHandle()) {
            return null;
        }

        String newId = getNewHandleId(nodePreference);
        newId = nodePreference.getPrefixHandle() + "/" + nodePreference.getPrivatePrefixHandle() + newId;

        log.info("avant l'appel à HandleClient");
        String jsonData = handleClient.getJsonData(nodePreference.getCheminSite() + privateUri);//"?idc=" + idConcept + "&idt=" + idThesaurus);
        log.info("avant le put ");
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
     */
    private String getNewHandleId(Preferences nodePreference) {

        boolean duplicateId = true;
        String idHandle = null;

        while (duplicateId) {
            idHandle = ToolsHelper.getNewId(10, false, false);
            if (!handleClient.isHandleExist(nodePreference.getUrlApiHandle(),
                    nodePreference.getPrefixHandle() + "/" + idHandle)) {
                duplicateId = false;
            }
            if (!handleClient.isHandleExist(
                    " https://hdl.handle.net/",
                    nodePreference.getPrefixHandle() + "/" + idHandle)) {
                duplicateId = false;
            }
        }
        return idHandle;
    }    
 
    /**
     * Permet de supprimer un identifiant Handle de la
     * plateforme (handle.net) via l'API REST
     */
    public boolean deleteIdHandle(String idHandle, Preferences nodePreference) {
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
    public boolean deleteAllIdHandle(ArrayList<String> tabIdHandle, Preferences nodePreference) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }

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
