package fr.cnrs.opentheso.ws.handlestandard;

import fr.cnrs.opentheso.entites.Preferences;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;

import lombok.extern.slf4j.Slf4j;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.DeleteHandleRequest;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HandleService {
    @Value("${certificats.admpriv}")
    private String admprivPath;

    @Value("${certificats.cacerts2}")
    private String cacerts2Path;

    @Value("${certificats.key}")
    private String keyPath;

    private int responseMsg;
    private String pathKey;
    private String pass;
    private String adminHandle;
    private String prefix;
    private String privatePrefix;
    
    private String serverHandle;
    private int index;    
    private AdminRecord admin;
    
    private PublicKeyAuthenticationInfo auth;
    private static HandleService hs;
    private HandleResolver resolver;

    private String message;


    // Utilisez le constructeur par défaut (géré automatiquement par Spring)
    public HandleService() {
    }

    public void applyNodePreference(Preferences nodePreference){
        serverHandle = nodePreference.getUrlApiHandle();
        pass = nodePreference.getPassHandle();
        prefix = nodePreference.getPrefixHandle(); // exp : 20.500.11859
        privatePrefix = nodePreference.getPrivatePrefixHandle();
        
        pathKey = admprivPath;//("certificats/admpriv.bin");
        try {
            index = nodePreference.getIndexHandle(); // exp : 300
        } catch (Exception e) {
        }
        adminHandle = nodePreference.getAdminHandle(); // exp : 0.NA/20.500.11859
    }    
    
    public boolean connectHandle(){
        byte[] key = null;
        try {
            File f= new File(admprivPath);
            FileInputStream fs = new FileInputStream(f);
            key = Util.getBytesFromInputStream(fs);
            if (key == null || key.length == 0) {
                throw new RuntimeException("Failed to load private key. Key is null or empty.");
            }
        } catch (Throwable t) {
            message = "Cannot read private key " + admprivPath + ": " + t;
            log.info("Cannot read private key " + admprivPath + ": " + t);
            return false;
        }

        // A HandleResolver object is used not just for resolution, but for
        // all handle operations(including create)
        resolver = new HandleResolver();

        // Check to see if the private key is encrypted.  If so, read in the
        // user's passphrase and decrypt.  Finally, convert the byte[] 
        // representation of the private key into a PrivateKey object.
        PrivateKey privkey = null;
        byte secKey[] = null;
        try {
            if (Util.requiresSecretKey(key)) {
                secKey = pass.getBytes();
            }
            key = Util.decrypt(key, secKey);
            privkey = Util.getPrivateKeyFromBytes(key, 0);
        } catch (Throwable t) {
            log.info("Can't load private key in " + admprivPath + ": " + t);
            return false;
        }

        try {
            // Create a PublicKeyAuthenticationInfo object to pass to HandleResolver.
            // This is constructed with the admin handle, index, and PrivateKey as
            // arguments.
            auth = new PublicKeyAuthenticationInfo(adminHandle.getBytes("UTF8"),
                            index,
                            privkey);

            // We don't want to create a handle without an admin value-- otherwise
            // we would be locked out.  Give ourselves all permissions, even
            // ones that only apply for NA handles.
            admin = new AdminRecord(adminHandle.getBytes("UTF8"), index,
                    true, true, true, true, true, true,
                    true, true, true, true, true, true);

        } catch (Throwable t) {
            log.info("\nError: " + t);
        }
        return true;
    }

    /**
     * peut créer un handle
     * 
     * @param handle
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     * @throws HandleException 
     */
    public boolean createHandle(String handle, String url) throws UnsupportedEncodingException, HandleException {

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            //prefix by http by default
            url = "https://" + url;
        }
        HandleValue[] val = {new HandleValue(100, "URL", url)};

        // Now we can build our CreateHandleRequest object.  As its first
        // parameter it takes the handle we are going to create.  The second
        // argument is the array of initial values the handle should have.
        // The final argument is the authentication object that should be
        // used to gain permission to perform the creation.
        String handleId = prefix + "/" + handle;
        CreateHandleRequest req = new CreateHandleRequest(handleId.getBytes("UTF8"), val, auth);

        AbstractResponse response = resolver.processRequest(req);

        // The responseCode value for a response indicates the status of
        // the request.  A successful resolution will always return
        // RC_SUCCESS.  Failed resolutions could return one of several
        // response codes, including RC_ERROR, RC_INVALID_ADMIN, and
        // RC_INSUFFICIENT_PERMISSIONS.
        if (response.responseCode == AbstractMessage.RC_SUCCESS) {
            //System.out.println("\nGot Response: \n" + response);
            message = response.toString();
            this.setResponseMsg(response.responseCode);
            return true;
        } else {
            //System.out.println("\nGot Error: \n" + response);
            message = response.toString();
            this.setResponseMsg(response.responseCode);
        }
        return false;
    }

    /**
     * peut supprimer un Handle
     * 
     * @param handleId
     * @return
     * @throws HandleException 
     */
    public boolean deleteHandle(String handleId) throws HandleException {
        DeleteHandleRequest req = new DeleteHandleRequest(Util.encodeString(handleId), auth);
        AbstractResponse response = resolver.processRequest(req);
        if (response == null || response.responseCode != AbstractMessage.RC_SUCCESS) {
            System.out.println("error deleting '" + handleId + "': " + response);
            this.setResponseMsg(response.responseCode);
        } else {
            System.out.println("deleted " + handleId);
            return true;
        }
        return false;
    }

    /**
     * @return the responseMsg
     */
    public int getResponseMsg() {
        return responseMsg;
    }

    /**
     * @param responseMsg the responseMsg to set
     */
    public void setResponseMsg(int responseMsg) {
        this.responseMsg = responseMsg;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return if isSuccess
     */
    public boolean isSuccess() {
        return responseMsg == AbstractMessage.RC_SUCCESS;
    }

    public void cleanResponseMsg() {

        responseMsg = 0;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public String getPrivatePrefix() {
        return privatePrefix;
    }

    public void setPrivatePrefix(String privatePrefix) {
        this.privatePrefix = privatePrefix;
    }
   
}
