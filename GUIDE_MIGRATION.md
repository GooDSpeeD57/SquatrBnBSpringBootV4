# 🔄 GUIDE DE MIGRATION RAPIDE
## De votre code vers le code corrigé

---

## 📦 CE QUI CHANGE DANS VOS APPELS API

### ❌ AVANT (ancien code)

#### Créer un utilisateur
```bash
POST /user
Content-Type: application/json

{
  "username": "john",
  "nom": "Doe",
  "prenom": "John",
  "email": "john@example.com",
  "dateNaissance": "1990-01-01",
  "password": "123",  # ❌ Pas de validation
  "role": { "id": 1 }
}

# Réponse: 200 OK (même en cas de création)
{
  "id": 1,
  "username": "john",
  "password": "$2a$10$...",  # ❌ MOT DE PASSE EXPOSÉ !
  ...
}
```

### ✅ APRÈS (code corrigé)

#### Créer un utilisateur
```bash
POST /api/users
Content-Type: application/json

{
  "username": "john",
  "nom": "Doe",
  "prenom": "John",
  "email": "john@example.com",
  "dateNaissance": "1990-01-01",
  "password": "Password123!",  # ✅ Validation stricte
  "roleId": 1
}

# Réponse: 201 CREATED
{
  "id": 1,
  "username": "john",
  # ✅ PAS DE MOT DE PASSE dans la réponse
  "email": "john@example.com",
  "role": {
    "id": 1,
    "name": "UTILISATEUR"
  }
}
```

---

## 🔧 CHANGEMENTS DANS LE CODE

### 1. UserController

#### ❌ AVANT
```java
@RestController
public class UserController {
    
    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable Integer id) {
        Optional<User> user = userService.getUser(id);
        return user.orElse(null);  // Retourne null si non trouvé
    }
    
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
}
```

#### ✅ APRÈS
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);  // 200 OK ou exception
    }
    
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);  // 201 CREATED
    }
}
```

### 2. UserService

#### ❌ AVANT
```java
public User saveUser(User user) {
    if (user.getPassword() != null) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    // ❌ Pas de vérification d'unicité email/username
    return userRepository.save(user);
}
```

#### ✅ APRÈS
```java
public UserResponseDTO createUser(UserCreateDTO dto) {
    // ✅ Vérification d'unicité
    if (userRepository.existsByEmail(dto.getEmail())) {
        throw new DataConflictException("Email déjà utilisé");
    }
    
    if (userRepository.existsByUsername(dto.getUsername())) {
        throw new DataConflictException("Username déjà utilisé");
    }
    
    User user = userMapper.toEntity(dto);
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    
    User saved = userRepository.save(user);
    return userMapper.toResponseDTO(saved);  // ✅ Retourne DTO sans mot de passe
}
```

### 3. Entity User

#### ❌ AVANT
```java
@Entity
public class User {
    @Column(name = "password_hash")
    private String password;  // ❌ Exposé dans JSON
}
```

#### ✅ APRÈS
```java
@Entity
public class User {
    @Column(name = "password_hash")
    @JsonIgnore  // ✅ Jamais dans les réponses JSON
    private String password;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;
}
```

---

## 🎯 SCÉNARIOS D'UTILISATION

### Scénario 1: Créer un utilisateur avec mot de passe faible

#### ❌ AVANT
```bash
POST /user
{
  "username": "test",
  "email": "test@test.com",
  "password": "123"  # Accepté sans problème
}

# Réponse: 200 OK (créé avec mot de passe faible)
```

#### ✅ APRÈS
```bash
POST /api/users
{
  "username": "test",
  "email": "test@test.com",
  "password": "123"
}

# Réponse: 400 BAD REQUEST
{
  "timestamp": "2024-02-04T14:30:00",
  "status": 400,
  "error": "Erreur de validation",
  "validationErrors": {
    "password": "Le mot de passe doit contenir au moins 8 caractères",
    "password": "Le mot de passe doit contenir: majuscule, minuscule, chiffre..."
  }
}
```

### Scénario 2: Récupérer un utilisateur inexistant

#### ❌ AVANT
```bash
GET /user/999

# Réponse: 200 OK
null  # ❌ Pas clair du tout
```

#### ✅ APRÈS
```bash
GET /api/users/999

# Réponse: 404 NOT FOUND
{
  "timestamp": "2024-02-04T14:30:00",
  "status": 404,
  "error": "Ressource non trouvée",
  "message": "Utilisateur non trouvé(e) avec id : '999'",
  "path": "/api/users/999"
}
```

### Scénario 3: Créer deux utilisateurs avec le même email

#### ❌ AVANT
```bash
# Premier utilisateur
POST /user { "email": "test@test.com", ... }
# OK

# Deuxième avec même email
POST /user { "email": "test@test.com", ... }
# ❌ Exception SQL non gérée
```

#### ✅ APRÈS
```bash
# Premier utilisateur
POST /api/users { "email": "test@test.com", ... }
# 201 CREATED

# Deuxième avec même email
POST /api/users { "email": "test@test.com", ... }
# 409 CONFLICT
{
  "status": 409,
  "error": "Conflit de données",
  "message": "Un utilisateur avec cet email existe déjà: test@test.com"
}
```

---

## 📋 CHECKLIST DE MIGRATION

Pour migrer votre frontend vers la nouvelle API :

### 1. URLs des endpoints
- [ ] Changer `/user` → `/api/users`
- [ ] Changer `/user/{id}` → `/api/users/{id}`

### 2. Requêtes POST/PUT
- [ ] Utiliser `UserCreateDTO` pour création (avec `roleId` au lieu de `role`)
- [ ] Utiliser `UserUpdateDTO` pour mise à jour

### 3. Gestion des réponses
- [ ] Ne plus chercher le champ `password` dans les réponses
- [ ] Gérer les codes HTTP: 200, 201, 204, 400, 404, 409, 500
- [ ] Parser les erreurs avec le format `ErrorResponse`

### 4. Validation côté client
- [ ] Mot de passe: min 8 caractères + complexité
- [ ] Email: format valide
- [ ] Tous les champs obligatoires

### 5. Gestion des erreurs
- [ ] Afficher `message` du JSON d'erreur
- [ ] Afficher `validationErrors` s'il existe
- [ ] Gérer chaque code d'erreur HTTP appropriément

---

## 🔄 EXEMPLES DE CODE FRONTEND

### React/JavaScript

#### ❌ AVANT
```javascript
// Créer un utilisateur
fetch('http://localhost:8080/user', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'john',
    email: 'john@example.com',
    password: '123',
    role: { id: 1 }
  })
})
.then(res => res.json())
.then(user => {
  console.log(user.password);  // ❌ Mot de passe visible !
});
```

#### ✅ APRÈS
```javascript
// Créer un utilisateur
fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'john',
    nom: 'Doe',
    prenom: 'John',
    email: 'john@example.com',
    dateNaissance: '1990-01-01',
    password: 'Password123!',  // ✅ Validation forte
    roleId: 1
  })
})
.then(async res => {
  if (res.status === 201) {
    const user = await res.json();
    console.log(user);  // ✅ Pas de mot de passe
    return user;
  } else if (res.status === 400) {
    const error = await res.json();
    console.error('Erreur de validation:', error.validationErrors);
    throw new Error(error.message);
  } else if (res.status === 409) {
    const error = await res.json();
    console.error('Conflit:', error.message);
    throw new Error(error.message);
  }
})
.catch(err => {
  console.error('Erreur:', err);
});
```

### Angular/TypeScript

```typescript
// Interfaces
interface UserCreateDTO {
  username: string;
  nom: string;
  prenom: string;
  email: string;
  dateNaissance: string;
  password: string;
  photoPath?: string;
  roleId?: number;
}

interface UserResponseDTO {
  id: number;
  username: string;
  nom: string;
  prenom: string;
  email: string;
  dateNaissance: string;
  photoPath?: string;
  role: {
    id: number;
    name: string;
  };
}

// Service
@Injectable()
export class UserService {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  createUser(dto: UserCreateDTO): Observable<UserResponseDTO> {
    return this.http.post<UserResponseDTO>(this.apiUrl, dto);
  }

  getUserById(id: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.apiUrl}/${id}`);
  }

  updateUser(id: number, dto: Partial<UserCreateDTO>): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.apiUrl}/${id}`, dto);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

## 🎓 POINTS CLÉS À RETENIR

1. **URLs**: Toutes les routes commencent par `/api/users`
2. **DTOs**: Utilisez les DTOs appropriés (Create/Update/Response)
3. **Codes HTTP**: Gérez correctement 200, 201, 204, 400, 404, 409
4. **Sécurité**: Le mot de passe n'est JAMAIS dans les réponses
5. **Validation**: Respectez les règles de validation (surtout mot de passe)
6. **Erreurs**: Utilisez le format `ErrorResponse` pour afficher les erreurs

---

## 📞 EN CAS DE PROBLÈME

### Erreur 400 - Bad Request
→ Vérifiez que toutes les validations sont respectées

### Erreur 404 - Not Found
→ L'utilisateur n'existe pas (normal si vous testez avec un mauvais ID)

### Erreur 409 - Conflict
→ Email ou username déjà utilisé

### Erreur 500 - Internal Server Error
→ Vérifiez les logs du serveur

---

**Dernière mise à jour**: 04/02/2026
