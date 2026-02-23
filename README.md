# SquArtRbNb - Application Spring Boot Corrigée

## 📋 Table des matières
1. [Corrections apportées](#corrections-apportées)
2. [Architecture du projet](#architecture-du-projet)
3. [Installation et configuration](#installation-et-configuration)
4. [API Endpoints](#api-endpoints)
5. [Tests](#tests)
6. [Bonnes pratiques implémentées](#bonnes-pratiques-implémentées)

---

## 🔧 Corrections apportées

### 1. **Sécurité - Protection du mot de passe**
**Problème**: Le mot de passe était exposé dans les réponses JSON
**Solution**: 
- Ajout de `@JsonIgnore` sur le champ `password` dans l'entité User
- Création de DTOs (Data Transfer Objects) pour séparer les données internes des données exposées
- Le `UserResponseDTO` n'expose jamais le mot de passe

### 2. **DTOs et Mapper**
**Problème**: Exposition directe des entités JPA dans l'API
**Solution**: 
- `UserCreateDTO`: Pour la création d'utilisateurs avec validation du mot de passe
- `UserUpdateDTO`: Pour la mise à jour (tous les champs optionnels)
- `UserResponseDTO`: Pour les réponses (sans données sensibles)
- `UserMapper`: Convertit entre entités et DTOs

### 3. **Validation des données**
**Problème**: Aucune validation des données entrantes
**Solution**: 
- Annotations de validation Jakarta (`@NotBlank`, `@Email`, `@Size`, etc.)
- Validation du mot de passe (minimum 8 caractères, complexité requise)
- Messages d'erreur personnalisés en français

### 4. **Gestion des erreurs**
**Problème**: Retour de `null` en cas d'erreur
**Solution**: 
- Création d'exceptions personnalisées (`ResourceNotFoundException`, `DataConflictException`)
- `GlobalExceptionHandler` avec `@RestControllerAdvice` pour centraliser la gestion des erreurs
- `ErrorResponse` pour structurer les réponses d'erreur
- Codes HTTP appropriés (404, 409, 400, 500)

### 5. **UserController - Codes HTTP et ResponseEntity**
**Problème**: Pas de codes HTTP appropriés
**Solution**: 
- Utilisation de `ResponseEntity<T>` pour tous les endpoints
- `201 CREATED` pour la création
- `200 OK` pour les lectures et mises à jour
- `204 NO CONTENT` pour la suppression
- `404 NOT FOUND` si ressource inexistante

### 6. **UserService - Logique métier améliorée**
**Problème**: Gestion incorrecte du mot de passe lors des mises à jour
**Solution**: 
- Vérification de l'unicité de l'email et du username avant création/mise à jour
- Encodage du mot de passe uniquement s'il est fourni
- Utilisation de `@Transactional` pour la cohérence des données
- Logs structurés avec Log4j2

### 7. **UserRepository**
**Problème**: Méthode `findById` redondante
**Solution**: 
- Suppression de la redondance (déjà dans `JpaRepository`)
- Ajout de méthodes utiles:
  - `existsByEmail(String email)`
  - `existsByUsername(String username)`
  - `findByEmail(String email)`
  - `findByUsername(String username)`

### 8. **Configuration de sécurité**
**Améliorations**:
- Configuration CORS pour les appels cross-origin
- Documentation claire sur les modifications nécessaires pour la production
- Support de plusieurs origines (React, Angular, etc.)

### 9. **Configuration des propriétés**
**Améliorations**:
- Utilisation de variables d'environnement pour les données sensibles
- Profils Spring (dev, prod)
- Configuration HikariCP pour le pool de connexions
- Configuration Jackson pour le format JSON
- Support multipart pour l'upload de fichiers

---

## 🏗️ Architecture du projet

```
src/
├── main/
│   ├── java/training/afpa/cda24060/squartrbnb/
│   │   ├── controller/          # Couche présentation (REST API)
│   │   │   └── UserController.java
│   │   ├── service/             # Couche logique métier
│   │   │   └── UserService.java
│   │   ├── repository/          # Couche accès données
│   │   │   ├── UserRepository.java
│   │   │   └── RoleRepository.java
│   │   ├── entity/              # Entités JPA
│   │   │   ├── User.java
│   │   │   └── Role.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── UserCreateDTO.java
│   │   │   ├── UserUpdateDTO.java
│   │   │   ├── UserResponseDTO.java
│   │   │   └── UserMapper.java
│   │   ├── exception/           # Gestion des exceptions
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── DataConflictException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ErrorResponse.java
│   │   ├── utils/               # Utilitaires et configuration
│   │   │   └── SecurityConfig.java
│   │   └── SquArtRbNbApplication.java
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    └── java/training/afpa/cda24060/squartrbnb/
        └── service/
            └── UserServiceTest.java
```

---

## 🚀 Installation et configuration

### Prérequis
- Java 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven 3.6 ou supérieur

### Configuration de la base de données

1. Créer la base de données:
```sql
CREATE DATABASE squatrbnb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Créer le rôle par défaut:
```sql
USE squatrbnb;
INSERT INTO role (name) VALUES ('UTILISATEUR');
INSERT INTO role (name) VALUES ('ADMINISTRATEUR');
```

### Démarrage de l'application

**En développement**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**En production**:
```bash
# Définir les variables d'environnement
export DATABASE_URL=jdbc:mysql://prod-server:3306/squatrbnb
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=prod_password

# Lancer l'application
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/users
```

### 1. Créer un utilisateur
**POST** `/api/users`

**Body**:
```json
{
  "username": "johndoe",
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "dateNaissance": "1990-01-01",
  "password": "Password123!",
  "roleId": 1
}
```

**Réponse** (201 CREATED):
```json
{
  "id": 1,
  "username": "johndoe",
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "dateNaissance": "1990-01-01",
  "photoPath": null,
  "role": {
    "id": 1,
    "name": "UTILISATEUR"
  }
}
```

### 2. Récupérer tous les utilisateurs
**GET** `/api/users`

**Réponse** (200 OK):
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@example.com",
    "dateNaissance": "1990-01-01",
    "photoPath": null,
    "role": {
      "id": 1,
      "name": "UTILISATEUR"
    }
  }
]
```

### 3. Récupérer un utilisateur par ID
**GET** `/api/users/{id}`

**Réponse** (200 OK) ou (404 NOT FOUND)

### 4. Récupérer un utilisateur par email
**GET** `/api/users/email/{email}`

### 5. Récupérer un utilisateur par username
**GET** `/api/users/username/{username}`

### 6. Mettre à jour un utilisateur
**PUT** `/api/users/{id}`

**Body** (tous les champs sont optionnels):
```json
{
  "email": "newemail@example.com",
  "password": "NewPassword123!"
}
```

**Réponse** (200 OK) ou (404 NOT FOUND)

### 7. Supprimer un utilisateur
**DELETE** `/api/users/{id}`

**Réponse** (204 NO CONTENT) ou (404 NOT FOUND)

---

## 🧪 Tests

### Exécuter les tests
```bash
mvn test
```

### Couverture des tests
Les tests unitaires couvrent:
- Création d'utilisateur avec données valides
- Gestion des erreurs (email/username existant)
- Récupération d'utilisateur (existant/inexistant)
- Suppression d'utilisateur

---

## ✅ Bonnes pratiques implémentées

### 1. **Architecture en couches**
- Séparation claire entre contrôleur, service, repository
- Chaque couche a sa responsabilité

### 2. **Principe DRY (Don't Repeat Yourself)**
- Utilisation de DTOs et Mapper pour éviter la duplication
- Gestion centralisée des erreurs

### 3. **Sécurité**
- Mot de passe jamais exposé dans les réponses
- Encodage BCrypt des mots de passe
- Validation stricte des données entrantes

### 4. **API RESTful**
- Utilisation correcte des verbes HTTP
- Codes de statut appropriés
- URLs sémantiques

### 5. **Gestion des erreurs**
- Exceptions personnalisées
- Réponses d'erreur structurées
- Messages d'erreur clairs en français

### 6. **Documentation**
- Javadoc sur les méthodes importantes
- Commentaires explicatifs
- README détaillé

### 7. **Configuration**
- Profils Spring pour différents environnements
- Variables d'environnement pour les données sensibles
- Configuration claire et commentée

### 8. **Tests**
- Tests unitaires avec Mockito
- Couverture des cas nominaux et d'erreur
- Tests indépendants et reproductibles

---

## 🔐 Remarques de sécurité

### Pour la production
Avant de déployer en production, assurez-vous de:

1. **Activer l'authentification**
   - Implémenter JWT ou OAuth2
   - Configurer les rôles et permissions

2. **Activer HTTPS**
   - Configurer un certificat SSL/TLS
   - Forcer la redirection HTTP vers HTTPS

3. **Activer CSRF** (si vous avez des formulaires web)

4. **Configurer les CORS** selon vos besoins réels

5. **Utiliser des variables d'environnement** pour:
   - Mot de passe de la base de données
   - Clés secrètes
   - Configuration sensible

6. **Activer Spring Boot Actuator** pour le monitoring

7. **Configurer les logs** pour la production

---

## 📝 Notes importantes

### Règles de validation du mot de passe
Le mot de passe doit:
- Contenir au moins 8 caractères
- Contenir au moins une majuscule
- Contenir au moins une minuscule
- Contenir au moins un chiffre
- Contenir au moins un caractère spécial (@#$%^&+=)

### Gestion des rôles
- Le rôle par défaut "UTILISATEUR" est automatiquement attribué si aucun rôle n'est spécifié
- Le rôle doit exister en base de données avant de créer un utilisateur

### Format des dates
- Format ISO 8601: `yyyy-MM-dd`
- Exemple: `1990-01-01`

---

## 🆘 Dépannage

### Erreur de connexion à la base de données
- Vérifiez que MySQL est démarré
- Vérifiez les credentials dans `application.properties`
- Vérifiez que la base de données existe

### Erreur "Rôle UTILISATEUR non trouvé"
- Exécutez le script SQL pour créer les rôles
- Vérifiez que la table `role` contient les données

### Erreur de validation
- Vérifiez que toutes les données requises sont fournies
- Vérifiez le format des données (email, date, mot de passe)

---

## 📧 Contact

Pour toute question ou suggestion, n'hésitez pas à créer une issue sur le projet.
