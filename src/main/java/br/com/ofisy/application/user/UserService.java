package br.com.ofisy.application.user;

import br.com.ofisy.application.user.dto.CreateUserRequestDTO;
import br.com.ofisy.application.user.dto.ModifyUserRoleRequestDTO;
import br.com.ofisy.application.user.dto.UpdatePasswordRequestDTO;
import br.com.ofisy.application.user.dto.UserResponseDTO;
import br.com.ofisy.application.user.exception.UserNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO create(CreateUserRequestDTO request) {
        User.validateExistingEmail(
                repository.existsByEmailAddress(request.email()),
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
    public UserResponseDTO findById(UUID id) {
        return mapper.toResponse(searchUserByID(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listAllUsers(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public UserResponseDTO modifyUserRole(UUID id, ModifyUserRoleRequestDTO request) {
        User currentUser = searchUserByID(id);
        currentUser.modifyRole(request.role());
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public UserResponseDTO updatePassword(UUID id, UpdatePasswordRequestDTO request) {
        User currentUser = searchUserByID(id);
        currentUser.validateCurrentPassword(passwordEncoder.matches(request.currentPassword(), currentUser.getPassword()));
        currentUser.updatePassword(passwordEncoder.encode(request.newPassword()));
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public UserResponseDTO deactivateUser(UUID id) {
        User currentUser = searchUserByID(id);
        currentUser.deactivate();
        return mapper.toResponse(repository.save(currentUser));
    }

    @Transactional
    public UserResponseDTO activateUser(UUID id) {
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