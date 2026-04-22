package br.com.ofisy.interfaces.api;

import br.com.ofisy.infrastructure.config.security.JwtService;
import br.com.ofisy.infrastructure.config.security.OfisyUserDetailsService;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AutoConfigureMockMvc(addFilters = false)
public abstract class ControllerTestBase {

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected OfisyUserDetailsService userDetailsService;
}