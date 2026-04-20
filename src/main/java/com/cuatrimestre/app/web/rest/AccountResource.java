package com.cuatrimestre.app.web.rest;

import com.cuatrimestre.app.domain.Authority;
import com.cuatrimestre.app.domain.User;
import com.cuatrimestre.app.domain.Menu;
import com.cuatrimestre.app.domain.Modulo;
import com.cuatrimestre.app.repository.UserRepository;
import com.cuatrimestre.app.repository.AuthorityRepository;
import com.cuatrimestre.app.repository.MenuRepository;
import com.cuatrimestre.app.repository.ModuloRepository;
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
    private final MenuRepository menuRepository;
    private final ModuloRepository moduloRepository;

    public AccountResource(
        UserRepository userRepository, 
        UserService userService, 
        MailService mailService,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        MenuRepository menuRepository,
        ModuloRepository moduloRepository
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.menuRepository = menuRepository;
        this.moduloRepository = moduloRepository;
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
        try {
            return userService.getUserWithAuthorities()
                .map(user -> {
                    if (!user.isActivated()) {
                        throw new AccountResourceException("User account is suspended");
                    }
                    return new AdminUserDTO(user);
                })
                .orElseThrow(() -> new AccountResourceException("User could not be found"));
        } catch (Exception e) {
            LOG.error("❌ Error en GET /api/account: " + e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        try {
            String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"));
            Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
            if (existingUser.isPresent() && (!existingUser.orElseThrow().getLogin().equalsIgnoreCase(userLogin))) throw new EmailAlreadyUsedException();
            
            LOG.debug("💾 Intentando guardar cuenta para: {}. URL de imagen presente: {}", userLogin, userDTO.getImageUrl() != null);
            
            userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getLangKey(), userDTO.getImageUrl());
            
            LOG.info("✅ Cuenta actualizada con éxito para: {}", userLogin);
        } catch (Exception e) {
            LOG.error("❌ Error al guardar /api/account: " + e.getMessage(), e);
            throw e;
        }
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
    @PostMapping("/insertar-datos-prueba")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void insertarDatosPrueba() {
        // 1. Asegurar Menú de Administración
        Menu adminMenu = menuRepository.findByNombreMenu("Administración")
            .orElseGet(() -> {
                Menu m = new Menu();
                m.setNombreMenu("Administración");
                return menuRepository.save(m);
            });

        // 2. Registrar Módulos Administrativos
        registrarModuloSiFalta("Usuarios y Roles", "/dashboard/admin/user-management", adminMenu);
        registrarModuloSiFalta("Perfiles", "/dashboard/admin/perfil", adminMenu);
        registrarModuloSiFalta("Módulos", "/dashboard/admin/modulo", adminMenu);
        registrarModuloSiFalta("Permisos Perfil", "/dashboard/admin/permisos-perfil", adminMenu);

        // 3. Generar usuario de prueba aleatorio
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String login = "user_" + randomId;

        User nuevoUsuario = new User();
        nuevoUsuario.setLogin(login);
        nuevoUsuario.setPassword(passwordEncoder.encode("1234"));
        nuevoUsuario.setFirstName("Usuario");
        nuevoUsuario.setLastName("Prueba " + randomId);
        nuevoUsuario.setEmail(login + "@test.com");
        nuevoUsuario.setActivated(true);
        nuevoUsuario.setLangKey("es");
        nuevoUsuario.setCreatedBy("system");

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        nuevoUsuario.setAuthorities(authorities);

        userRepository.save(nuevoUsuario);
        
        LOG.info("✅ Insertado usuario de prueba y módulos administrativos verificados.");
    }

    private void registrarModuloSiFalta(String nombre, String ruta, Menu menu) {
        // Buscamos si ya existe por nombre e idMenu
        boolean existe = moduloRepository.findAll().stream()
            .anyMatch(m -> m.getNombreModulo().equals(nombre) && m.getRuta().equals(ruta));
        
        if (!existe) {
            Modulo m = new Modulo();
            m.setNombreModulo(nombre);
            m.setRuta(ruta);
            m.setMenu(menu);
            moduloRepository.save(m);
        }
    }
}