package br.com.ofisy.domain.user;

import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import br.com.ofisy.domain.user.exceptions.InactiveUserException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private UUID id;
    private String name;
    private Email email;
    private String password;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static User create(String email, String encryptedPassword, String name, Role role) {
        User user = new User();
        user.email = new Email(email);
        user.password = encryptedPassword;
        user.name = name;
        user.role = role;
        user.active = true;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    public static User reconstruct(UUID id, Email email, String password, String name,
                                   Role role, boolean active,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        User user = new User();
        user.id = id;
        user.email = email;
        user.password = password;
        user.name = name;
        user.role = role;
        user.active = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Usuário já está desativado.");
        }
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Usuário já está ativo.");
        }
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String newEncryptedPassword) {
        this.password = newEncryptedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void modifyRole(Role newRole) {
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }

    public static void validateExistingEmail(boolean emailAlreadyExists, String email) {
        if (emailAlreadyExists) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    public void validateCurrentPassword(boolean currentPassword) {
        if (!currentPassword) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
    }

    public void validateIsActive() {
        if (!this.active) {
            throw new InactiveUserException(this.email.emailAddress());
        }
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public Collection<String> getAuthorities() {
        return List.of("ROLE_" + this.role.name());
    }
}