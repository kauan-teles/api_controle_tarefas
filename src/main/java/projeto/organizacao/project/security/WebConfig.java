package projeto.organizacao.project.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/tarefas/**")
                .allowedOrigins("http://localhost:5173") //Observação: fazer alteração da porta caso seja diferente na sua máquina;
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
