package br.com.ofisy.application.user.getidbyemail;

import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetIdByEmailService implements GetIdByEmailUseCase {

    private final UserRepository repository;

    public GetIdByEmailService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UUID execute(String email) {
        User user = repository.findByEmailAddress(email)
                .orElseThrow(() -> new EmailNotFoundException(email));
        return user.getId();
    }
}
