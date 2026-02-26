package training.afpa.cda24060.squartrbnb.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configure Spring pour servir les fichiers uploadés depuis le disque.
 *
 * Sans cette config, Spring ne sert que les fichiers dans src/main/resources/static/
 * qui sont compilés dans le JAR — les nouveaux fichiers uploadés ne sont pas accessibles.
 *
 * Avec cette config :
 *   http://localhost:8080/uploads/users/uuid.jpg
 *   → sert le fichier depuis src/main/resources/static/uploads/users/uuid.jpg sur le disque
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:src/main/resources/static/uploads/users}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String uploadAbsolutePath = uploadPath.toUri().toString();

        registry.addResourceHandler("/uploads/users/**")
       .addResourceLocations(uploadAbsolutePath + "/");
    }
}
