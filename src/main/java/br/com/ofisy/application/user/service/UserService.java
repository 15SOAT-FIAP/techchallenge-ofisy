package br.com.ofisy.application.user.service;

import br.com.ofisy.application.user.exception.UserNotFoundException;
import br.com.ofisy.application.user.mapper.UserMapper;
import br.com.ofisy.application.user.dto.UserDTO.*;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User.validateExistingEmail(
                repository.existsByEmailEmailAddress(request.email()),
                request.email()
        );

        User newUser = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.role()
        );

        return mapper.toResponse(repository.save(newUser));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return mapper.toResponse(searchUserByID(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAllUsers() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse modifyUserRole(UUID id, ModifyUserRoleRequest request) {
        User currentUser = searchUserByID(id);
        currentUser.modifyRole(request.role());
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public UserResponse updatePassword(UUID id, UpdatePasswordRequest request) {
        User currentUser = searchUserByID(id);
        currentUser.validateCurrentPassword(
                passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())
        );
        currentUser.updatePassword(passwordEncoder.encode(request.newPassword()));
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public UserResponse deactivateUser(UUID id) {
        User currentUser = searchUserByID(id);
        currentUser.deactivate();
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public UserResponse activateUser(UUID id) {
        User currentUser = searchUserByID(id);
        currentUser.activate();
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public void removeUser(UUID id) {
        searchUserByID(id);
        repository.deleteById(id);
    }

    private User searchUserByID(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
