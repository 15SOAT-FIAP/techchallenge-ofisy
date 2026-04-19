package br.com.ofisy.infrastructure.persistence.user;

import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findById(@NonNull UUID id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaUserRepository.findAll(pageable);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findByEmailAddress(String email) {
        return jpaUserRepository.findByEmailEmailAddress(email);
    }

    @Override
    public boolean existsByEmailAddress(String email) {
        return jpaUserRepository.existsByEmailEmailAddress(email);
    }
}
