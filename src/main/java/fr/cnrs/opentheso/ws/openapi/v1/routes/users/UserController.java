package fr.cnrs.opentheso.ws.openapi.v1.routes.users;

import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserResource;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;



@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/openapi/v1/users")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "User", description = "Gestion des utilisateurs")
public class UserController {

    private final UserHelper userHelper;
    private final ApiKeyHelper apiKeyHelper;


    @PostMapping("/authentification")
    @Operation(summary = "Authentification d'un utilisateur")
    public ResponseEntity createUser(@RequestBody @Valid AuthentificationDto authentificationDto) {

        var user = userHelper.getUserByLoginAndPassword(authentificationDto.getLogin(), authentificationDto.getPassword());
        if (ObjectUtils.isEmpty(user)) {
            return ResponseEntity.notFound().build();
        }

        if (!user.isSuperAdmin()) {
            user.setThesorusList(userHelper.getThesaurusOfUser(user.getIdUser()));
        }
        return ResponseEntity.ok().body(user);
    }


    @PostMapping
    @Operation(summary = "Créer un nouveau utilisateur")
    public ResponseEntity createUser(@RequestHeader(value = "API-KEY") String apiKey,
                                     @RequestBody @Valid UserDto userDto) {

        if (getUser(apiKey).isSuperAdmin()) {
            if(!userHelper.addUser(userDto.getLogin(), userDto.getMail(), userDto.getPassword(), userDto.isSuperAdmin(), userDto.isAlertMail())){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur pendant la création de l'utilisateur !!!");
            }

            var idUser = userHelper.getIdUser(userDto.getLogin(), userDto.getPassword());
            var apiKeyValue = apiKeyHelper.generateApiKey("ot_", 64);
            if(!apiKeyHelper.saveApiKey(MD5Password.getEncodedPassword(apiKeyValue), idUser)){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur pendant la génération du API Key !!!");
            }

            if (!ObjectUtils.isEmpty(userDto.getIdRole()) && !ObjectUtils.isEmpty(userDto.getIdThesaurus())) {
                var idGroup = userHelper.getGroupOfThisTheso(userDto.getIdThesaurus());
                userHelper.addUserRoleOnGroup(idUser, userDto.getIdRole(), idGroup);
            }

            var userCreated = userHelper.getUser(idUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }


    @DeleteMapping("/{idUser}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity deleteUser(@RequestHeader(value = "API-KEY") String apiKey,
                                     @PathVariable("idUser") Integer idUser) {

        var userRequest = getUser(apiKey);
        if (userRequest != null && userRequest.isSuperAdmin()) {
            var userToRemove = userHelper.getUser(idUser);
            if (userToRemove != null) {
                userHelper.deleteUser(userToRemove.getIdUser());
                return ResponseEntity.status(HttpStatus.OK).body("");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }


    @PutMapping("/{idUser}")
    @Operation(summary = "Modifier un utilisateur")
    public ResponseEntity updateUser(@RequestHeader(value = "API-KEY") String apiKey,
                                     @PathVariable("idUser") Integer idUser,
                                     @RequestBody @Valid UserDto userDto) {

        var userRequest = getUser(apiKey);
        if (userRequest != null) {
            var user = userHelper.getUser(idUser);
            if (!ObjectUtils.isEmpty(user)) {
                userHelper.updateUser(user.getIdUser(), userDto.getLogin(), userDto.getMail(), userDto.isActive(), userDto.isAlertMail());

                if (!ObjectUtils.isEmpty(userDto.getPassword())) {
                    userHelper.updatePwd(user.getIdUser(), userDto.getPassword());
                }

                if (!ObjectUtils.isEmpty(userDto.getIdRole()) && !ObjectUtils.isEmpty(userDto.getIdProject())) {
                    var idGroup = userHelper.getGroupOfThisTheso(userDto.getIdThesaurus());
                    userHelper.updateUserRoleOnGroup(user.getIdUser(), userDto.getIdRole(), idGroup);
                }
                return ResponseEntity.status(HttpStatus.OK).body("");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }

    @GetMapping
    @Operation(summary = "Rechercher des utilisateurs")
    public ResponseEntity searchUser(@RequestHeader(value = "API-KEY") String apiKey,
                                     @ParameterObject NodeUserResource userResource) {

        if (getUser(apiKey).isSuperAdmin()) {
            if (StringUtils.isNotEmpty(userResource.getMail())) {
                return ResponseEntity.status(HttpStatus.OK).body(userHelper.searchUserByCriteria(userResource.getMail()));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(userHelper.getAllUsers());
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }

    private NodeUser getUser(String apiKey) {
        var keyState = apiKeyHelper.checkApiKey(apiKey);
        if (keyState != ApiKeyState.VALID){
            return null;
        }
        return userHelper.getUserByApiKey(apiKey);
    }
}
