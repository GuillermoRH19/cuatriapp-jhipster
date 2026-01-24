package com.cuatrimestre.app.web.rest;

import com.cuatrimestre.app.domain.Authority;
import com.cuatrimestre.app.domain.User;
import com.cuatrimestre.app.repository.UserRepository;
import com.cuatrimestre.app.repository.AuthorityRepository;
import com.cuatrimestre.app.security.AuthoritiesConstants;
import com.cuatrimestre.app.security.SecurityUtils;
import com.cuatrimestre.app.service.MailService;
import com.cuatrimestre.app.service.UserService;
import com.cuatrimestre.app.service.dto.AdminUserDTO;
import com.cuatrimestre.app.service.dto.PasswordChangeDTO;
import com.cuatrimestre.app.web.rest.errors.*;
import com.cuatrimestre.app.web.rest.vm.KeyAndPasswordVM;
import com.cuatrimestre.app.web.rest.vm.ManagedUserVM;
import jakarta.validation.Valid;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {
        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;

    public AccountResource(
        UserRepository userRepository, 
        UserService userService, 
        MailService mailService,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) throw new InvalidPasswordException();
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) throw new AccountResourceException("No user was found for this activation key");
    }

    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService.getUserWithAuthorities().map(AdminUserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.orElseThrow().getLogin().equalsIgnoreCase(userLogin))) throw new EmailAlreadyUsedException();
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) throw new AccountResourceException("User could not be found");
        userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getLangKey(), userDTO.getImageUrl());
    }

    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) throw new InvalidPasswordException();
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) {
        Optional<User> user = userService.requestPasswordReset(mail);
        if (user.isPresent()) mailService.sendPasswordResetMail(user.orElseThrow());
        else LOG.warn("Password reset requested for non existing mail");
    }

    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) throw new InvalidPasswordException();
        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());
        if (!user.isPresent()) throw new AccountResourceException("No user was found for this reset key");
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (StringUtils.isEmpty(password) || password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH || password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH);
    }

    // ==========================================
    //  MÉTODO PARA INSERTAR DATOS (BOTÓN)
    // ==========================================
    @PostMapping("/insertar-datos-prueba") // <--- ESTA ES LA RUTA QUE USAREMOS
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void insertarDatosPrueba() {
        // Generamos un identificador aleatorio
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String login = "user_" + randomId;

        User nuevoUsuario = new User();
        nuevoUsuario.setLogin(login);
        nuevoUsuario.setPassword(passwordEncoder.encode("1234")); // Contraseña fija
        nuevoUsuario.setFirstName("Usuario");
        nuevoUsuario.setLastName("Prueba " + randomId);
        nuevoUsuario.setEmail(login + "@test.com");
        nuevoUsuario.setActivated(true);
        nuevoUsuario.setLangKey("es");
        nuevoUsuario.setCreatedBy("system");

        // Asignar rol
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        nuevoUsuario.setAuthorities(authorities);

        // Guardar en SQL
        userRepository.save(nuevoUsuario);
        
        LOG.info("✅ Insertado usuario de prueba: " + login);
    }
}