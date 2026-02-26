package training.afpa.cda24060.squartrbnb.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Endpoint dédié à l'upload de photos utilisateur.
 *
 * POST /api/users/photo  → reçoit un fichier, le sauvegarde, retourne le chemin relatif
 * GET  /api/users/photo/{filename} → sert le fichier (optionnel si déjà servi en static)
 */
@RestController
@RequestMapping("/api/users/photo")
@Log4j2
public class PhotoController {

    // Dossier de stockage — configurable via application.properties
    @Value("${app.upload.dir:uploads/users}")
    private String uploadDir;

    /**
     * Upload d'une photo.
     * Retourne le chemin relatif : "uploads/users/uuid.jpg"
     */
    @PostMapping
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Aucun fichier reçu.");
        }

        // Vérification du type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Le fichier doit être une image.");
        }

        try {
            // Créer le dossier si nécessaire
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Dossier uploads créé : {}", uploadPath.toAbsolutePath());
            }

            // Générer un nom unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Sauvegarder le fichier
            Path targetPath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner uniquement le chemin public (ex: "uploads/users/uuid.jpg")
            // On extrait la partie après "static/" pour avoir l'URL accessible par le navigateur
            String publicPath;
            if (uploadDir.contains("static/")) {
                publicPath = uploadDir.substring(uploadDir.indexOf("static/") + "static/".length())
                             + "/" + newFilename;
            } else {
                publicPath = uploadDir + "/" + newFilename;
            }
            log.info("Photo uploadée : {}", publicPath);

            return ResponseEntity.ok(publicPath);

        } catch (IOException e) {
            log.error("Erreur upload photo : {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erreur lors de l'upload : " + e.getMessage());
        }
    }
}
