package br.com.ofisy.infrastructure.config.security;

import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfisyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        //aqui está sendo usado o path da classe junto com o nome pq há conflito com o nosso user se eu adicioná-la ao import
        return new org.springframework.security.core.userdetails.User (
                user.getEmail().emailAddress(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
