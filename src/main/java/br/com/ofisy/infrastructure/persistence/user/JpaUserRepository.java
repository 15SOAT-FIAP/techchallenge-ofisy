package br.com.ofisy.infrastructure.persistence.user;

import br.com.ofisy.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailEmailAddress(String email);

    boolean existsByEmailEmailAddress(String email);
}
