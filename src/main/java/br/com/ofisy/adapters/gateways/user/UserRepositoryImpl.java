package br.com.ofisy.adapters.gateways.user;

import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpa;

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        return UserMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailAddress(String email) {
        return jpa.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmailAddress(String email) {
        return jpa.existsByEmail(email);
    }
}
