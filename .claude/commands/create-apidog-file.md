# Create Apidog File - Spring Boot Edition

Spring Boot Controller'ından otomatik Apidog JSON dosyası oluştur.

## Kurallar

- Türkçe dökümantasyon olmalı
- Spring Boot projesi (Java/Maven/Gradle)
- JPA Entity ve REST Controller'lardan otomatik döküman oluştur
- Apidog formatında JSON çıktı (OpenAPI değil!)

## Kullanım

```
/create-apidog-file <controller_file_path>
```

**ÖNEMLİ:** Parametre olarak **MUTLAKA Controller dosyasının tam (absolute) path'ini** verin!

✅ **DOĞRU:**

```
/create-apidog-file /Users/fatihdemir/Desktop/diyet-app-backend/src/main/java/com/fatihdemir/diyetappbackend/controller/UserController.java
```

❌ **YANLIŞ:**

```
/create-apidog-file /Users/fatihdemir/Desktop/diyet-app-backend/src/main/java/com/fatihdemir/diyetappbackend/entity/User.java
```

(Entity dosyası değil, Controller dosyası verin!)

## Örnek

```
/create-apidog-file /Users/fatihdemir/Desktop/diyet-app-backend/src/main/java/com/fatihdemir/diyetappbackend/controller/UserController.java
```

## Görev

Verilen Controller dosyasındaki **TÜM endpoint'ler** için Apidog formatında **TEK BİR** JSON dosyası oluştur.

**ÖNEMLİ**: Controller dosyasında kaç endpoint olursa olsun (List, Get, Create, Update, Delete), hepsi için tüm endpoint'leri tek bir Apidog JSON dosyasına ekle.

### Adımlar

1. **Controller Dosyasını Analiz Et**
    - Controller dosyasını oku
    - Tüm @RequestMapping endpoint tanımlarını bul (GET, POST, PUT, DELETE, PATCH)
    - Her endpoint için:
        - HTTP method (@GetMapping, @PostMapping, @PutMapping, @DeleteMapping)
        - Path (@RequestMapping class seviyesi + method seviyesi)
        - Controller method adı
        - Swagger/SpringDoc annotations (@Operation, @ApiResponse, @RequestBody, vb.)

   **Spring Boot Endpoint Örnekleri:**
   ```java
   @RestController
   @RequestMapping("/api/v1/users")
   @Tag(name = "User Management", description = "Kullanıcı Yönetimi")
   public class UserController {
       
       @GetMapping
       @Operation(summary = "Tüm kullanıcıları listele", description = "Sistemdeki tüm kullanıcıları getir")
       @ApiResponse(responseCode = "200", description = "Başarılı")
       public ResponseEntity<Page<UserDTO>> getAllUsers(...) { }
       
       @GetMapping("/{id}")
       @Operation(summary = "Kullanıcı detayını getir")
       public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) { }
       
       @PostMapping
       @Operation(summary = "Yeni kullanıcı oluştur")
       public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) { }
       
       @PutMapping("/{id}")
       @Operation(summary = "Kullanıcı bilgilerini güncelle")
       public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) { }
       
       @DeleteMapping("/{id}")
       @Operation(summary = "Kullanıcıyı sil")
       public ResponseEntity<Void> deleteUser(@PathVariable Long id) { }
   }
   ```

2. **Entity Dosyasını Bul ve Analiz Et**
    - Controller'da kullanılan Entity'i bul (örn: UserController → User.java)
    - Entity dosyasını oku: `/src/main/java/com/fatihdemir/diyetappbackend/entity/User.java`
    - Kod analizi yap:
        - @Entity annotasyonunun bulunduğunu kontrol et
        - @Table annotasyonundan tablo adını al
        - Tüm JPA field'larını bul (@Column, @Id, @GeneratedValue, vb.)
        - Service layer'ından dönüş type'ını belirle (User, UserDTO)

   **Spring Boot Entity Örneği:**
   ```java
   @Entity
   @Table(name = "users")
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class User {
       
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(name = "email", nullable = false, unique = true, length = 255)
       private String email;
       
       @Column(name = "password", nullable = false)
       private String password;
       
       @Column(name = "first_name", length = 100)
       private String firstName;
       
       @Column(name = "last_name", length = 100)
       private String lastName;
       
       @Column(name = "phone_number", length = 20)
       private String phoneNumber;
       
       @Column(name = "is_active", nullable = false)
       private Boolean isActive;
       
       @Column(name = "created_at", nullable = false, updatable = false)
       @Temporal(TemporalType.TIMESTAMP)
       private LocalDateTime createdAt;
       
       @Column(name = "updated_at")
       @Temporal(TemporalType.TIMESTAMP)
       private LocalDateTime updatedAt;
       
       @ManyToOne
       @JoinColumn(name = "role_id")
       private Role role;
       
       @OneToMany(mappedBy = "user")
       private List<UserProfile> profiles;
   }
   ```

3. **DTO/Request/Response Dosyalarını Analiz Et**
    - Controller'da kullanılan DTO'ları bul (CreateUserRequest, UpdateUserRequest, UserDTO)
    - Her bir DTO dosyasını oku
    - Field'lar için şu bilgileri çıkar:
        - Field adı ve JSON property adı (@JsonProperty)
        - Field tipi (String, Long, Integer, Double, LocalDateTime, Enum, vb.)
        - Validation annotations (@NotNull, @NotBlank, @Min, @Max, vb.)
        - **Javadoc comment'ı** (/** ... */)
        - @Schema annotation'ı (description, example, vb.)
    - Description bilgisi için öncelik sırası:
        1. @Schema(description = "...") değeri
        2. @NotBlank(message = "...") gibi validation message'ı
        3. Field üzerindeki Javadoc satırı
        4. Eğer ikisi de yoksa, field adından türet

   **Spring Boot DTO Örnekleri:**
   ```java
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class CreateUserRequest {
       
       @NotBlank(message = "Email gereklidir")
       @Email(message = "Geçerli bir email girin")
       @Schema(description = "Kullanıcının email adresi", example = "user@example.com")
       private String email;
       
       @NotBlank(message = "Şifre gereklidir")
       @Size(min = 8, max = 255)
       @Schema(description = "Kullanıcının şifresi", example = "SecurePass123!")
       private String password;
       
       @NotBlank
       @Schema(description = "Kullanıcının adı", example = "Fatih")
       private String firstName;
       
       @Schema(description = "Kullanıcının soyadı", example = "Demir")
       private String lastName;
       
       @Schema(description = "Telefon numarası", example = "+905551112233")
       private String phoneNumber;
   }
   
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class UserDTO {
       
       @Schema(description = "Kullanıcı ID'si", example = "1")
       private Long id;
       
       @Schema(description = "Email adresi", example = "user@example.com")
       private String email;
       
       @Schema(description = "Kullanıcının adı", example = "Fatih")
       private String firstName;
       
       @Schema(description = "Kullanıcının soyadı", example = "Demir")
       private String lastName;
       
       @Schema(description = "Telefon numarası", example = "+905551112233")
       private String phoneNumber;
       
       @Schema(description = "Aktif durumu", example = "true")
       private Boolean isActive;
       
       @Schema(description = "Oluşturma tarihi", example = "2026-05-19T10:30:00")
       private LocalDateTime createdAt;
       
       @Schema(description = "Güncelleme tarihi", example = "2026-05-19T11:00:00")
       private LocalDateTime updatedAt;
       
       @Schema(description = "Kullanıcı rolü")
       private RoleDTO role;
   }
   ```

4. **JSON Schema Oluştur**
    - Spring Boot DTO'larını JSON Schema'ya dönüştür
    - **Her field için description ekle** (Adım 3'te çıkarılan description bilgisini kullan)
    - Field tipleri dönüşümü:
        - `String` → "type": "string"
        - `Long`, `Integer` → "type": "integer"
        - `Double`, `Float`, `BigDecimal` → "type": "number"
        - `Boolean` → "type": "boolean"
        - `LocalDateTime`, `Date` → "type": "string", "format": "date-time"
        - `LocalDate` → "type": "string", "format": "date"
        - `LocalTime` → "type": "string", "format": "time"
        - Enum → "type": "string", "enum": [...]
        - Nested Object/DTO → "type": "object"
        - List<T> → "type": "array", "items": { ... }

    - Required field'ları belirle (validation annotations'dan bul):
        - @NotNull, @NotBlank, @NotEmpty → required
        - Eğer açıkça nullable değilse → required

    - x-apidog-orders array'ini field sırası ile oluştur (DTO'daki sırayla aynı)

   **Request Schema Örneği (CreateUserRequest):**
   ```json
   {
     "type": "object",
     "required": ["email", "password", "firstName"],
     "properties": {
       "email": {
         "type": "string",
         "format": "email",
         "description": "Kullanıcının email adresi",
         "example": "user@example.com"
       },
       "password": {
         "type": "string",
         "minLength": 8,
         "maxLength": 255,
         "description": "Kullanıcının şifresi",
         "example": "SecurePass123!"
       },
       "firstName": {
         "type": "string",
         "description": "Kullanıcının adı",
         "example": "Fatih"
       },
       "lastName": {
         "type": "string",
         "description": "Kullanıcının soyadı",
         "example": "Demir"
       },
       "phoneNumber": {
         "type": "string",
         "description": "Telefon numarası",
         "example": "+905551112233"
       }
     },
     "x-apidog-orders": ["email", "password", "firstName", "lastName", "phoneNumber"]
   }
   ```

   **Response Schema Örneği (UserDTO):**
   ```json
   {
     "type": "object",
     "properties": {
       "id": {
         "type": "integer",
         "format": "int64",
         "description": "Kullanıcı ID'si",
         "example": 1
       },
       "email": {
         "type": "string",
         "format": "email",
         "description": "Email adresi",
         "example": "user@example.com"
       },
       "firstName": {
         "type": "string",
         "description": "Kullanıcının adı",
         "example": "Fatih"
       },
       "lastName": {
         "type": "string",
         "description": "Kullanıcının soyadı",
         "example": "Demir"
       },
       "phoneNumber": {
         "type": "string",
         "description": "Telefon numarası",
         "example": "+905551112233"
       },
       "isActive": {
         "type": "boolean",
         "description": "Aktif durumu",
         "example": true
       },
       "createdAt": {
         "type": "string",
         "format": "date-time",
         "description": "Oluşturma tarihi",
         "example": "2026-05-19T10:30:00"
       },
       "updatedAt": {
         "type": "string",
         "format": "date-time",
         "description": "Güncelleme tarihi",
         "example": "2026-05-19T11:00:00"
       },
       "role": {
         "type": "object",
         "description": "Kullanıcı rolü",
         "properties": {
           "id": {
             "type": "integer",
             "example": 1
           },
           "name": {
             "type": "string",
             "example": "ADMIN"
           }
         }
       }
     },
     "x-apidog-orders": ["id", "email", "firstName", "lastName", "phoneNumber", "isActive", "createdAt", "updatedAt", "role"]
   }
   ```

5. **Markdown Döküman Oluştur**
   Her endpoint için detaylı markdown döküman:
   ```markdown
   # {Endpoint Türkçe Adı}

   ## Genel Bakış
   {Spring @Operation.description veya JavaDoc}

   ## HTTP Method & Path
   `{METHOD} {PATH}`

   ## Request Parameters

   ### Path Parameters
   - **{paramName}** ({type}): {description}

   ### Query Parameters
   - **{paramName}** ({type}): {description}

   ### Request Body

   #### Gerekli Alanlar
   - **{fieldName}** ({type}): {description}

   #### Opsiyonel Alanlar
   - **{fieldName}** ({type}): {description}

   ## Response

   ### Success (200/201)
   ```json
   {
     "id": 1,
     "email": "user@example.com",
     "firstName": "Fatih",
     "createdAt": "2026-05-19T10:30:00"
   }
   ```

   ### Error (400/500)
   ```json
   {
     "timestamp": "2026-05-19T10:30:00",
     "status": 400,
     "error": "Bad Request",
     "message": "Validation failed",
     "errors": {
       "email": "Email gereklidir"
     }
   }
   ```

   ## Örnek Request
   ```bash
   curl -X POST http://localhost:8080/api/v1/users \
     -H "Content-Type: application/json" \
     -d '{
       "email": "user@example.com",
       "password": "SecurePass123!",
       "firstName": "Fatih",
       "lastName": "Demir",
       "phoneNumber": "+905551112233"
     }'
   ```

   ## Notlar
    - Authentication gereklidir (Bearer Token)
    - Rate limiting: 100 istek/saat
   ```

6. **Apidog JSON Dosyası Oluştur**

   **ÇOK ÖNEMLİ - DOSYA FORMATI UYARISI:**
    - Dosya formatı **KESINLIKLE** Apidog formatında olmalıdır
    - **ASLA** OpenAPI formatı ("openapi": "3.0.1") kullanma!
    - İlk satır **MUTLAKA** `"apidogProject": "1.0.0"` olmalıdır
    - OpenAPI formatı ile Apidog formatı farklıdır, karıştırma!

   Doğru Apidog Yapısı:
   ```json
   {
     "apidogProject": "1.0.0",
     "$schema": {
       "app": "apidog",
       "type": "project",
       "version": "1.2.0"
     },
     "info": {
       "name": "{Entity Name} API",
       "description": "Spring Boot Auto-generated from controller file",
       "mockRule": {
         "rules": [],
         "enableSystemRule": true
       }
     },
     "apiCollection": [
       {
         "name": "Root",
         "id": "root_collection",
         "items": [
           {
             "name": "User Management",
             "id": "user_management_folder",
             "items": [
               {
                 "name": "{Entity Name}",
                 "id": "user_entity_folder",
                 "items": [
                   {
                     "name": "Liste",
                     "ordering": 10,
                     "api": {
                       "id": "{random_id_1}",
                       "method": "get",
                       "path": "/api/v1/users",
                       "auth": null,
                       "requestBody": {
                         "type": "none"
                       },
                       "responses": [
                         {
                           "code": 200,
                           "name": "Success",
                           "contentType": "application/json",
                           "jsonSchema": { ... }
                         },
                         {
                           "code": 500,
                           "name": "Server Error",
                           "contentType": "application/json",
                           "jsonSchema": { ... }
                         }
                       ],
                       "description": "# Tüm Kullanıcıları Listele\n\n... markdown content ..."
                     }
                   },
                   {
                     "name": "Kaydı Getir",
                     "ordering": 15,
                     "api": { ... }
                   },
                   {
                     "name": "Ekle",
                     "ordering": 20,
                     "api": { ... }
                   },
                   {
                     "name": "Güncelle",
                     "ordering": 30,
                     "api": { ... }
                   },
                   {
                     "name": "Sil",
                     "ordering": 40,
                     "api": { ... }
                   }
                 ]
               }
             ]
           }
         ]
       }
     ]
   }
   ```

   **Endpoint Yapı Detayları:**

   **GET Liste Endpoint'i:**
   ```json
   {
     "name": "Liste",
     "ordering": 10,
     "api": {
       "id": "abc123456",
       "method": "get",
       "path": "/api/v1/users",
       "auth": null,
       "parameters": [
         {
           "name": "page",
           "in": "query",
           "required": false,
           "description": "Sayfa numarası (0-indexed)",
           "schema": { "type": "integer", "example": 0 }
         },
         {
           "name": "size",
           "in": "query",
           "required": false,
           "description": "Sayfa başına kayıt sayısı",
           "schema": { "type": "integer", "example": 20 }
         },
         {
           "name": "sort",
           "in": "query",
           "required": false,
           "description": "Sıralama (örn: id,desc)",
           "schema": { "type": "string", "example": "id,desc" }
         }
       ],
       "requestBody": {
         "type": "none"
       },
       "responses": [
         {
           "code": 200,
           "name": "Success",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "content": {
                 "type": "array",
                 "items": {
                   "type": "object",
                   "properties": {
                     "id": { "type": "integer", "example": 1 },
                     "email": { "type": "string", "example": "user@example.com" },
                     "firstName": { "type": "string", "example": "Fatih" },
                     "lastName": { "type": "string", "example": "Demir" },
                     "phoneNumber": { "type": "string", "example": "+905551112233" },
                     "isActive": { "type": "boolean", "example": true },
                     "createdAt": { "type": "string", "format": "date-time", "example": "2026-05-19T10:30:00" },
                     "updatedAt": { "type": "string", "format": "date-time", "example": "2026-05-19T11:00:00" }
                   }
                 }
               },
               "totalElements": { "type": "integer", "example": 100 },
               "totalPages": { "type": "integer", "example": 5 },
               "page": { "type": "integer", "example": 0 }
             }
           }
         }
       ],
       "description": "# Tüm Kullanıcıları Listele\n\n## Genel Bakış\nSistemde kayıtlı tüm kullanıcıları sayfalı olarak getir\n\n## HTTP Method & Path\n`GET /api/v1/users`\n\n## Query Parameters\n- **page** (integer): Sayfa numarası (0-indexed)\n- **size** (integer): Sayfa başına kayıt sayısı\n- **sort** (string): Sıralama parametresi\n\n## Response\n\n### Success (200)\n```json\n{\n  \"content\": [...],\n  \"totalElements\": 100,\n  \"totalPages\": 5,\n  \"page\": 0\n}\n```"
         }
       ]
     }
   }
   ```

   **POST Ekle (Create) Endpoint'i:**
   ```json
   {
     "name": "Ekle",
     "ordering": 20,
     "api": {
       "id": "def456789",
       "method": "post",
       "path": "/api/v1/users",
       "auth": null,
       "requestBody": {
         "type": "application/json",
         "jsonSchema": {
           "type": "object",
           "required": ["email", "password", "firstName"],
           "properties": {
             "email": {
               "type": "string",
               "format": "email",
               "description": "Kullanıcının email adresi",
               "example": "user@example.com"
             },
             "password": {
               "type": "string",
               "minLength": 8,
               "description": "Şifre (minimum 8 karakter)",
               "example": "SecurePass123!"
             },
             "firstName": {
               "type": "string",
               "description": "Kullanıcının adı",
               "example": "Fatih"
             },
             "lastName": {
               "type": "string",
               "description": "Kullanıcının soyadı",
               "example": "Demir"
             },
             "phoneNumber": {
               "type": "string",
               "description": "Telefon numarası",
               "example": "+905551112233"
             }
           },
           "x-apidog-orders": ["email", "password", "firstName", "lastName", "phoneNumber"]
         }
       },
       "responses": [
         {
           "code": 201,
           "name": "Created",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "id": { "type": "integer", "example": 1 },
               "email": { "type": "string", "example": "user@example.com" },
               "firstName": { "type": "string", "example": "Fatih" },
               "lastName": { "type": "string", "example": "Demir" },
               "phoneNumber": { "type": "string", "example": "+905551112233" },
               "isActive": { "type": "boolean", "example": true },
               "createdAt": { "type": "string", "format": "date-time", "example": "2026-05-19T10:30:00" }
             }
           }
         },
         {
           "code": 400,
           "name": "Bad Request",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "timestamp": { "type": "string", "format": "date-time" },
               "status": { "type": "integer", "example": 400 },
               "error": { "type": "string", "example": "Bad Request" },
               "message": { "type": "string", "example": "Validation failed" }
             }
           }
         }
       ],
       "description": "# Yeni Kullanıcı Oluştur\n\n..."
     }
   }
   ```

   **GET by ID (Kaydı Getir) Endpoint'i:**
   ```json
   {
     "name": "Kaydı Getir",
     "ordering": 15,
     "api": {
       "id": "ghi789012",
       "method": "get",
       "path": "/api/v1/users/{id}",
       "auth": null,
       "parameters": [
         {
           "name": "id",
           "in": "path",
           "required": true,
           "description": "Kullanıcı ID'si",
           "schema": { "type": "integer", "example": 1 }
         }
       ],
       "requestBody": {
         "type": "none"
       },
       "responses": [
         {
           "code": 200,
           "name": "Success",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "id": { "type": "integer", "example": 1 },
               "email": { "type": "string", "example": "user@example.com" },
               "firstName": { "type": "string", "example": "Fatih" },
               "lastName": { "type": "string", "example": "Demir" },
               "phoneNumber": { "type": "string", "example": "+905551112233" },
               "isActive": { "type": "boolean", "example": true },
               "createdAt": { "type": "string", "format": "date-time", "example": "2026-05-19T10:30:00" },
               "updatedAt": { "type": "string", "format": "date-time", "example": "2026-05-19T11:00:00" }
             }
           }
         },
         {
           "code": 404,
           "name": "Not Found",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "error": { "type": "string", "example": "User not found" }
             }
           }
         }
       ],
       "description": "# Kullanıcı Detayını Getir\n\n..."
     }
   }
   ```

   **PUT Güncelle (Update) Endpoint'i:**
   ```json
   {
     "name": "Güncelle",
     "ordering": 30,
     "api": {
       "id": "jkl012345",
       "method": "put",
       "path": "/api/v1/users/{id}",
       "auth": null,
       "parameters": [
         {
           "name": "id",
           "in": "path",
           "required": true,
           "description": "Kullanıcı ID'si",
           "schema": { "type": "integer", "example": 1 }
         }
       ],
       "requestBody": {
         "type": "application/json",
         "jsonSchema": {
           "type": "object",
           "properties": {
             "firstName": {
               "type": "string",
               "description": "Kullanıcının adı",
               "example": "Fatih"
             },
             "lastName": {
               "type": "string",
               "description": "Kullanıcının soyadı",
               "example": "Demir"
             },
             "phoneNumber": {
               "type": "string",
               "description": "Telefon numarası",
               "example": "+905551112233"
             }
           },
           "x-apidog-orders": ["firstName", "lastName", "phoneNumber"]
         }
       },
       "responses": [
         {
           "code": 200,
           "name": "Success",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "id": { "type": "integer", "example": 1 },
               "firstName": { "type": "string", "example": "Fatih" },
               "lastName": { "type": "string", "example": "Demir" },
               "updatedAt": { "type": "string", "format": "date-time", "example": "2026-05-19T11:00:00" }
             }
           }
         }
       ],
       "description": "# Kullanıcı Bilgilerini Güncelle\n\n..."
     }
   }
   ```

   **DELETE Sil (Delete) Endpoint'i:**
   ```json
   {
     "name": "Sil",
     "ordering": 40,
     "api": {
       "id": "mno345678",
       "method": "delete",
       "path": "/api/v1/users/{id}",
       "auth": null,
       "parameters": [
         {
           "name": "id",
           "in": "path",
           "required": true,
           "description": "Kullanıcı ID'si",
           "schema": { "type": "integer", "example": 1 }
         }
       ],
       "requestBody": {
         "type": "none"
       },
       "responses": [
         {
           "code": 204,
           "name": "No Content",
           "contentType": "application/json",
           "jsonSchema": {}
         },
         {
           "code": 404,
           "name": "Not Found",
           "contentType": "application/json",
           "jsonSchema": {
             "type": "object",
             "properties": {
               "error": { "type": "string", "example": "User not found" }
             }
           }
         }
       ],
       "description": "# Kullanıcıyı Sil\n\n..."
     }
   }
   ```

7. **Dosyayı Kaydet**
    - Hedef dizin: `/Users/fatihdemir/Desktop/diyet-app-backend/apidog/<entity_name>`
    - Dosya adı: `{entity_name}_api.json` (örn: `user_api.json`, `meal_api.json`, `diet_plan_api.json`)

   **Örnek Output Dizin Yapısı:**
   ```
   /Users/fatihdemir/Desktop/diyet-app-backend/apidog/
   ├── user_api.json
   ├── meal_api.json
   ├── diet_plan_api.json
   ├── nutrition_api.json
   └── ingredient_api.json
   ```

### Endpoint Türkçe İsimlendirme

Spring Boot Mapping'e göre Türkçe isim:

- @GetMapping (liste, @RequestParam ile pagination) → "Liste"
- @GetMapping("/{id}") (by id) → "Kaydı Getir"
- @PostMapping → "Ekle"
- @PutMapping → "Güncelle"
- @PatchMapping → "Kısmen Güncelle"
- @DeleteMapping → "Sil"

### Önemli Notlar - Spring Boot Özellikleri

1. **Spring Boot Annotations**
    - @RestController, @RequestMapping, @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @PatchMapping
    - @PathVariable, @RequestParam, @RequestBody, @ResponseStatus
    - @Operation, @ApiResponse, @Schema (SpringDoc OpenAPI)
    - @Validated, @Valid, @NotNull, @NotBlank, @Email, vb. (Jakarta Validation)

2. **Response Yapısı**
    - Spring Boot standart olarak ResponseEntity<T> döner
    - Error handling için @ExceptionHandler ile custom error response'lar
    - List/Page dönüşleri için Spring Data JPA Page<T> kullan
    - Status codes: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error)

3. **Path Parameters vs Query Parameters**
    - @PathVariable: URL path'te yer alan parametreler (/{id}, /{code})
    - @RequestParam: Query string parametreleri (?page=0&size=20&sort=id,desc)
    - Pagination için @RequestParam(defaultValue = "0") int page gibi kullan

4. **Entity-DTO Mapping**
    - Entity: JPA @Entity sınıfları (veritabanı modeli)
    - DTO: Data Transfer Object (API response modeli)
    - Controller'de DTO döndür, Entity'yi expose etme
    - Mapping için MapStruct veya ModelMapper kullan

5. **Validation**
    - @NotNull, @NotBlank, @NotEmpty (Jakarta Validation)
    - @Email, @Min, @Max, @Size, @Pattern
    - Custom validators oluşturabilir
    - @Valid annotasyonu ile otomatik validation

6. **DateTime Format**
    - Java: LocalDateTime, LocalDate, LocalTime, ZonedDateTime
    - API: ISO 8601 format (2026-05-19T10:30:00)
    - Jackson konfigürasyonunda format belirle

7. **Enum Handling**
   ```java
   public enum UserRole {
       ADMIN("Admin"),
       USER("Kullanıcı"),
       MODERATOR("Moderatör");
       
       private String displayName;
   }
   ```
    - API response: "ADMIN" olarak döner
    - Description'da enum değerlerini ve anlamlarını yaz

8. **Nested Objects ve Relations**
    - @ManyToOne, @OneToMany, @ManyToMany relations
    - DTO'da nested object'ler gül (lazy loading problemine dikkat)
    - Response schema'da inline nested object yapısını belirt

9. **Her endpoint için benzersiz ID üret (random 8 haneli string)**
    - Örn: "abc123456", "def456789", "ghi789012"

10. **JSON Schema'da field sıralamasını koru (x-apidog-orders)**
    - DTO sınıfındaki field sırası korunmalı

11. **Ordering değerleri:**
    - Liste = 10
    - Kaydı Getir = 15
    - Ekle = 20
    - Güncelle = 30
    - Kısmen Güncelle = 25
    - Sil = 40

12. **Auth Bloğu**
    - Spring Security kullanıyorsa, endpoint'lerde @PreAuthorize("hasRole('ADMIN')") gibi kontroller var
    - Apidog'da auth bloğu EKLEME (null bırak) - authentication ayarları inherit olarak yapılacak
    - Description'da "Authentication gereklidir" notu ekle

13. **ASLA $ref kullanma**
    - Tüm response ve request schema'larında inline schema tanımla
    - $ref veya #/definitions/ kullanma
    - Her endpoint'in schema'sı tam olmalı

14. **Response Yapıları**
    - Başarılı Response (200/201): Entity veya DTO döner
    - Hata Response (400/500): Error object döner
    - Delete (204): Empty body

15. **List/Page Response'lar**
    - Liste endpoint'i için Spring Data JPA Page<T> döner
    - Response schema'da content array'ında TÜM model field'larını inline olarak yaz
    - totalElements, totalPages, page, size, last field'larını ekle (PageResponse record'una göre)

16. **KRİTİK — Apidog JSON format kuralları (bunlar yanlış olursa UI'da hiçbir şey görünmez):**

    | Alan | YANLIŞ | DOĞRU |
    |------|--------|-------|
    | Request body tipi | `"type": "json"` | `"type": "application/json"` |
    | Request body şeması | `"schema": { ... }` | `"jsonSchema": { ... }` |
    | Response şeması | `"schema": { ... }` | `"jsonSchema": { ... }` |
    | Response kodu | `"code": "200"` (string) | `"code": 200` (integer) |
    | Response adı | `"message": "Success"` | `"name": "Success"` |
    | Response content type | (eksik) | `"contentType": "application/json"` |
    | Path/Query param listesi | `"requestParameters": [...]` | `"parameters": [...]` |
    | Param tipi/örnek | `"type": "string", "example": "x"` (flat) | `"schema": { "type": "string", "example": "x" }` (nested) |

    **Parametre yapısı (DOĞRU):**
    ```json
    "parameters": [
      {
        "name": "id",
        "in": "path",
        "required": true,
        "description": "Kullanıcı ID'si",
        "schema": { "type": "string", "format": "uuid", "example": "550e8400-e29b-41d4-a716-446655440000" }
      },
      {
        "name": "page",
        "in": "query",
        "required": false,
        "description": "Sayfa numarası",
        "schema": { "type": "integer", "example": 0 }
      }
    ]
    ```

    **Header parametresi (DOĞRU):**
    ```json
    "parameters": [
      {
        "name": "Authorization",
        "in": "header",
        "required": true,
        "description": "Bearer token",
        "schema": { "type": "string", "example": "Bearer eyJhbGci..." }
      }
    ]
    ```

## Komut Örneği

```bash
/create-apidog-file /Users/fatihdemir/Desktop/diyet-app-backend/src/main/java/com/fatihdemir/diyetappbackend/controller/UserController.java

# Çıktı:
# ✅ Controller analizi tamamlandı
# ✅ User Entity'si analiz edildi
# ✅ CreateUserRequest DTO'su analiz edildi
# ✅ UserDTO analiz edildi
# ✅ 5 endpoint bulundu:
#    - GET /api/v1/users (Liste)
#    - GET /api/v1/users/{id} (Kaydı Getir)
#    - POST /api/v1/users (Ekle)
#    - PUT /api/v1/users/{id} (Güncelle)
#    - DELETE /api/v1/users/{id} (Sil)
# ✅ Apidog JSON dosyası oluşturuldu
# 📁 Dosya: /Users/fatihdemir/Desktop/diyet-app-backend/apidog/user_api.json
# 📊 Boyut: 245 KB
```

## Kaynaklar

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- SpringDoc OpenAPI (SpringFox'un yeni versiyonu): https://springdoc.org/
- Jakarta Validation: https://jakarta.ee/specifications/validation/
- JSON Schema: https://json-schema.org/
- Apidog Documentation: https://apidog.com/docs