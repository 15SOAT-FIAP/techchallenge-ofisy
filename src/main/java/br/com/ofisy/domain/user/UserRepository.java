package br.com.ofisy.domain.user;

import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmailEmailAddress(String email);

    boolean existsByEmailEmailAddress(String email);

    void deleteById(@NonNull UUID id);
}