package br.com.ofisy.adapters.controllers.user;

import br.com.ofisy.adapters.controllers.user.dto.ModifyUserRoleRequestDTO;
import br.com.ofisy.adapters.controllers.user.dto.UpdatePasswordRequestDTO;
import br.com.ofisy.adapters.controllers.user.dto.CreateUserRequestDTO;
import br.com.ofisy.adapters.controllers.user.dto.UserResponseDTO;
import br.com.ofisy.adapters.presenters.user.UserPresenter;
import br.com.ofisy.application.user.activateuser.ActivateUserUseCase;
import br.com.ofisy.application.user.createuser.CreateUserUseCase;
import br.com.ofisy.application.user.deactivateuser.DeactivateUserUseCase;
import br.com.ofisy.application.user.findbyid.FindUserByIdUseCase;
import br.com.ofisy.application.user.listall.ListAllUsersUseCase;
import br.com.ofisy.application.user.modifyrole.ModifyUserRoleUseCase;
import br.com.ofisy.application.user.updatepassword.UpdatePasswordUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final CreateUserUseCase createUserUseCase;
    private final ListAllUsersUseCase listAllUsersUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final ModifyUserRoleUseCase modifyUserRoleUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserRequestDTO request) {
        CreateUserUseCase.CreateUserCommand cmd = new CreateUserUseCase.CreateUserCommand(
                request.name(), request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserPresenter.present(createUserUseCase.execute(cmd)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> listAllUsers(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(listAllUsersUseCase.execute(pageable).map(UserPresenter::present));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserPresenter.present(findUserByIdUseCase.execute(id)));
    }

    @PatchMapping("/{id}/modify-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> modifyRole(
            @PathVariable UUID id,
            @RequestBody @Valid ModifyUserRoleRequestDTO request) {
        ModifyUserRoleUseCase.ModifyUserRoleCommand cmd = new ModifyUserRoleUseCase.ModifyUserRoleCommand(request.role());
        return ResponseEntity.ok(UserPresenter.present(modifyUserRoleUseCase.execute(id, cmd)));
    }

    @PatchMapping("/{id}/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> modifyPassword(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePasswordRequestDTO request) {
        UpdatePasswordUseCase.UpdatePasswordCommand cmd = new UpdatePasswordUseCase.UpdatePasswordCommand(
                request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(UserPresenter.present(updatePasswordUseCase.execute(id, cmd)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(UserPresenter.present(deactivateUserUseCase.execute(id)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(UserPresenter.present(activateUserUseCase.execute(id)));
    }
}
