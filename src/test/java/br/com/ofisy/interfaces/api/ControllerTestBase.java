package br.com.ofisy.interfaces.api;

import br.com.ofisy.shared.jwt.JwtService;
import br.com.ofisy.shared.securityfilter.OfisyUserDetailsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestBase.MetricsTestConfig.class)
public abstract class ControllerTestBase {

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected OfisyUserDetailsService userDetailsService;

    @TestConfiguration
    static class MetricsTestConfig {

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}