package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.dto.LoginRequestDTO;
import br.com.ofisy.application.user.dto.LoginResponseDTO;
import br.com.ofisy.infrastructure.config.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController implements LoginApi {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String token = jwtService.generateToken(authentication.getName());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}