package br.com.ofisy.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Page<User> findAll(Pageable pageable);

    User save(User user);

    Optional<User> findByEmailAddress(String email);

    boolean existsByEmailAddress(String email);

}