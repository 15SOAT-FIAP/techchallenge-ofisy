package br.com.ofisy.domain.user;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmailEmailAddress(String email);

    boolean existsByEmailEmailAddress(String email);

    void deleteById(@NonNull UUID id);
}