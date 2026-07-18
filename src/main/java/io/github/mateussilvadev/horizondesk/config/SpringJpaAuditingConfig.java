package io.github.mateussilvadev.horizondesk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class SpringJpaAuditingConfig implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("system_dev");
    }

//    @Override
//    public Optional<String> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        // Verifica se está nulo, se não está autenticado, ou se é um usuário anônimo (rotas públicas)
//        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
//            // Retorna "system" para registros feitos pelo sistema sem login (ex: cadastro inicial)
//            return Optional.of("system");
//        }
//
//        return Optional.of(authentication.getName());
//    }
}