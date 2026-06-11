package br.com.ofisy.application.user.listall;

import br.com.ofisy.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListAllUsersUseCase {
    Page<User> execute(Pageable pageable);
}
