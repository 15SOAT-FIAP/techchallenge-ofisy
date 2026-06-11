package br.com.ofisy.adapters.controllers.user;

import br.com.ofisy.adapters.controllers.user.dto.LoginRequestDTO;
import br.com.ofisy.adapters.controllers.user.dto.LoginResponseDTO;
import br.com.ofisy.application.user.login.LoginUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController implements LoginApi {

    private final LoginUseCase loginUseCase;

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginUseCase.LoginCommand cmd = new LoginUseCase.LoginCommand(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponseDTO(loginUseCase.execute(cmd).token()));
    }
}
