package br.com.ofisy.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public static User create(String email, String encryptedPassword, String nome, Role role) {
        User usuario = new User();
        usuario.email = new Email(email);
        usuario.password = encryptedPassword;
        usuario.name = nome;
        usuario.role = role;
        usuario.active = true;
        usuario.createdAt = LocalDateTime.now();
        return usuario;
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

    public void remove() {

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
            throw new IllegalArgumentException("Email já cadastrado: " + email);
        }
    }

    public void validateCurrentPassword(boolean currentPassword) {
        if (!currentPassword) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public Collection<String> getAuthorities() {
        return List.of("ROLE_" + this.role.name());
    }
}
