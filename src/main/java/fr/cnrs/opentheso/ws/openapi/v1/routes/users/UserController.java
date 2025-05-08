package fr.cnrs.opentheso.ws.openapi.v1.routes.users;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.models.users.NodeUserResource;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.services.UserRoleGroupService;
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

    private final UserRepository userRepository;
    private final ApiKeyHelper apiKeyHelper;
    private final UserHelper userHelper;
    private final UserRoleGroupService userRoleGroupService;


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
        if (StringUtils.isEmpty(user.getApiKey())) {
            var apiKeyValue = apiKeyHelper.generateApiKey("ot_", 64);
            if(!apiKeyHelper.saveApiKey(MD5Password.getEncodedPassword(apiKeyValue), user.getIdUser())){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur pendant la génération du API Key !!!");
            }
            user.setApiKey(apiKeyValue);
        }
        return ResponseEntity.ok().body(user);
    }


    @PostMapping
    @Operation(summary = "Créer un nouveau utilisateur")
    public ResponseEntity createUser(@RequestHeader(value = "API-KEY") String apiKey,
                                     @RequestBody @Valid UserDto userDto) {

        if (getUser(apiKey).getIsSuperAdmin()) {

            var userCreated = userRepository.save(User.builder().username(userDto.getLogin())
                    .mail(userDto.getMail())
                    .password(userDto.getPassword())
                    .isSuperAdmin(userDto.isSuperAdmin())
                    .alertMail(userDto.isAlertMail())
                    .build());

            var apiKeyValue = apiKeyHelper.generateApiKey("ot_", 64);
            if(!apiKeyHelper.saveApiKey(MD5Password.getEncodedPassword(apiKeyValue), userCreated.getId())){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur pendant la génération du API Key !!!");
            }

            if (!ObjectUtils.isEmpty(userDto.getIdRole()) && !ObjectUtils.isEmpty(userDto.getIdThesaurus())) {
                var idGroup = userHelper.getGroupOfThisTheso(userDto.getIdThesaurus());
                userRoleGroupService.addUserRoleOnGroup(userCreated.getId(), userDto.getIdRole(), idGroup);
            }

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
        if (userRequest != null && userRequest.getIsSuperAdmin()) {
            var userToRemove = userRepository.findById(idUser);
            if (userToRemove.isPresent()) {
                userRepository.delete(userToRemove.get());
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
            var user = userRepository.findById(idUser);
            if (user.isPresent()) {

                user.get().setMail(userDto.getMail());
                user.get().setUsername(userDto.getLogin());
                user.get().setPassword(userDto.getPassword());
                user.get().setActive(userDto.isActive());
                user.get().setAlertMail(userDto.isAlertMail());
                userRepository.save(user.get());

                if (!ObjectUtils.isEmpty(userDto.getIdRole()) && !ObjectUtils.isEmpty(userDto.getIdProject())) {
                    var idGroup = userHelper.getGroupOfThisTheso(userDto.getIdThesaurus());
                    userRoleGroupService.updateUserRoleOnGroup(user.get().getId(), userDto.getIdRole(), idGroup);
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

        if (getUser(apiKey).getIsSuperAdmin()) {
            var users = userHelper.searchUserByCriteria(userResource.getMail(), userResource.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }

    private User getUser(String apiKey) {
        var keyState = apiKeyHelper.checkApiKey(apiKey);
        if (keyState != ApiKeyState.VALID){
            return null;
        }
        return userRepository.findByApiKey(apiKey).get();
    }
}
