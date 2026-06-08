# ISG-C Portal

Kurumsal ölçekte, modüler ve production seviyesinde İş Sağlığı ve Güvenliği (ISG-C) yönetim portalı.

## 🚀 Teknoloji Stack

- **Backend**: Spring Boot 3.3.5 (Java 17)
  - JWT Authentication (Access + Refresh tokens)
  - Role-Based Access Control (RBAC)
  - Spring Security
  - JPA/Hibernate + PostgreSQL
  - Flyway (Database Migrations)
  - Swagger OpenAPI 3
  - AOP-based Audit Logging
  - SMTP Mail Service (HTML Templates)
  - Local File Storage (S3-compatible design)
  - Scheduled Jobs (Spring Scheduling)

- **Frontend**: React 18 + Vite + TypeScript
  - Ant Design 5 (UI Components)
  - ECharts (Data Visualization)
  - React Router (Routing)
  - Axios (HTTP Client)
  - JWT Token Management

- **Database**: PostgreSQL 16

## 📁 Proje Yapısı

```
isc/
├── backend/              # Spring Boot API
│   ├── src/main/java/
│   │   └── com/isgc/portal/
│   │       ├── auth/          # JWT Authentication
│   │       ├── user/          # User & Role Management
│   │       ├── project/       # Project Management
│   │       ├── employee/      # Employee Management
│   │       ├── training/      # Training Records
│   │       ├── accident/      # Accident Management
│   │       ├── nonconformity/ # Nonconformity Tracking
│   │       ├── discipline/    # Discipline Logs
│   │       ├── audit/         # Audits & Checklists
│   │       ├── document/      # Document Management
│   │       ├── auditlog/      # Audit Logging (AOP)
│   │       ├── files/         # File Storage
│   │       ├── mail/          # Email Service
│   │       └── security/      # Security Configuration
│   └── src/main/resources/
│       ├── db/migration/      # Flyway Migrations
│       └── templates/mail/    # Email HTML Templates
├── frontend/             # React Application
│   ├── src/
│   │   ├── api/          # API Client
│   │   ├── routes/       # Route Definitions
│   │   ├── state/        # State Management (Auth)
│   │   └── ui/           # UI Components & Pages
│   └── public/
└── docker-compose.yml     # PostgreSQL Container
```

## 🛠️ Kurulum ve Çalıştırma

### Ön Gereksinimler

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 18+** ve **npm/yarn**
- **Docker** ve **Docker Compose** (PostgreSQL için)
- **PostgreSQL 16** (veya Docker ile)

### 1. Veritabanını Başlat

```bash
docker compose up -d db
```

PostgreSQL şu ayarlarla başlar:
- **Host**: `localhost:5432`
- **Database**: `isgc_portal`
- **User**: `postgres`
- **Password**: `1234`

### 2. Backend'i Çalıştır

```bash
cd backend
mvn spring-boot:run
```

Backend şu adreste çalışır: `http://localhost:8080`

**İlk Çalıştırmada:**
- Flyway otomatik olarak tüm migration'ları çalıştırır
- İlk kullanıcılar otomatik oluşturulur:
  - **Irina** (ADMIN) - username: `irina`, password: `irina123`
  - **Samet** (ADMIN) - username: `samet`, password: `samet123`

### 3. Frontend'i Çalıştır

```bash
cd frontend
npm install
npm run dev
```

Frontend şu adreste çalışır: `http://localhost:5173`

### 4. Production Build

**Backend:**
```bash
cd backend
mvn clean package
java -jar target/isgc-portal-backend-0.1.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Çıktı: frontend/dist/
```

## 🔐 Roller ve Yetkiler

- **ADMIN** (Irina, Samet): Tüm sistem üzerinde tam yetki (ekleme, silme, güncelleme)
- **ISG_C**: Tüm modülleri görüntüleme ve veri girişi
- **YONETICI**: İlişkili projeler ve çalışanları görüntüleme
- **PERSONEL**: Sadece kendi kayıtlarını görüntüleme

## 📋 Modüller

### 1. Kaza Yönetimi
- Dinamik kaza formları (kaza tipine göre)
- Yaralı ve kilit personel seçimi
- Eğitim bilgileri erişimi
- Kaza sınıfı ve potansiyel seviyesi
- Otomatik e-posta bildirimleri
- Kök neden analizi (Human, Equipment, Environment, Management)
- Otomatik diyagram oluşturma

### 2. Uygunsuzluk Takibi
- Proje bazlı takip
- Yetkili kişi seçimi
- Fotoğraf yükleme
- Tehlikeli sınıf bazlı tanımlama
- Otomatik hatırlatma e-postaları
- İstatistikler ve grafikler

### 3. Disiplin Logu
- Çalışan seçimi (otomatik yönetici/profesyon doldurma)
- Uyumsuz çalışan ve yönetici seçimi
- Düzenli e-posta bildirimleri
- İstatistikler ve grafikler

### 4. Denetim & Kontrol Listeleri
- İç/DIş denetimler
- Kontrol listesi yönetimi
- Otomatik skor hesaplama
- Denetim raporları (HTML)
- Tamamlanan denetimler için e-posta bildirimi

### 5. Denetim Arşivi
- Tamamlanan denetimlerin arşivlenmesi
- Görsel ve dosya ekleri
- ISG-C personeli erişimi

### 6. Doküman & Blank Form Yönetimi
- ISG-C blank formlarının yüklenmesi
- PDF erişimi ve indirme
- Versiyon yönetimi
- ADMIN: Yükleme, güncelleme, silme
- ISG_C: Görüntüleme ve indirme

### 7. Kaza Takip & Raporlama
- Otomatik kaza takip tablosu
- Proje bazlı takip
- Trend analizi (günlük/haftalık/aylık)
- İstatistiksel raporlar
- Otomatik periyodik e-posta raporları (abonelik sistemi)

## 🔍 API Dokümantasyonu

Swagger UI: `http://localhost:8080/swagger-ui.html`

Tüm REST API endpoint'leri Swagger'da dokümante edilmiştir. JWT token ile kimlik doğrulama yapabilirsiniz.

## 📧 E-posta Yapılandırması

Production ortamında SMTP ayarlarını environment variable'lar ile yapılandırın:

```bash
export ISGC_SMTP_HOST=smtp.example.com
export ISGC_SMTP_PORT=587
export ISGC_SMTP_USERNAME=your-username
export ISGC_SMTP_PASSWORD=your-password
export ISGC_SMTP_AUTH=true
export ISGC_SMTP_STARTTLS=true
export ISGC_MAIL_FROM=noreply@example.com
export ISGC_MAIL_MANAGEMENT_CC=management@example.com
```

Development için MailHog veya benzeri bir SMTP test sunucusu kullanabilirsiniz.

## 🔒 Güvenlik

- **JWT Tokens**: Access token (15 dk) + Refresh token (14 gün)
- **BCrypt**: Şifre hashleme
- **RBAC**: Spring Security ile rol bazlı erişim kontrolü
- **Audit Log**: Tüm CRUD işlemleri otomatik loglanır (AOP)
- **File Upload Security**: Path traversal koruması
- **CORS**: Frontend için yapılandırılmış

## 📝 Environment Variables

### Backend

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/isgc_portal
DB_USER=postgres
DB_PASSWORD=1234

# JWT
ISGC_JWT_SECRET=your-secret-key-min-32-bytes

# File Storage
ISGC_STORAGE_ROOT=./storage

# SMTP
ISGC_SMTP_HOST=localhost
ISGC_SMTP_PORT=1025
ISGC_SMTP_USERNAME=
ISGC_SMTP_PASSWORD=
ISGC_SMTP_AUTH=false
ISGC_SMTP_STARTTLS=false
ISGC_MAIL_FROM=no-reply@isgc.local
ISGC_MAIL_MANAGEMENT_CC=
```

### Frontend

Frontend API URL'i `frontend/src/api/http.ts` dosyasında yapılandırılır (varsayılan: `http://localhost:8080`).

## 🧪 Test Senaryoları

### 1. Authentication
- Login (ADMIN/ISG_C/YONETICI/PERSONEL)
- Token refresh
- Logout

### 2. Kaza Yönetimi
- Kaza oluşturma (dinamik form)
- Kök neden analizi
- E-posta bildirimi kontrolü

### 3. Uygunsuzluk Takibi
- Uygunsuzluk oluşturma
- Fotoğraf yükleme
- Otomatik hatırlatma e-postaları

### 4. Denetim
- Kontrol listesi oluşturma
- Denetim çalıştırma
- Skor hesaplama ve rapor

### 5. Raporlama
- Kaza istatistikleri
- Trend grafikleri
- Abonelik yönetimi

## 🐛 Sorun Giderme

### Backend başlamıyor
- PostgreSQL'in çalıştığından emin olun: `docker compose ps`
- Port 8080'in boş olduğunu kontrol edin
- `backend/storage/` klasörünün yazılabilir olduğundan emin olun

### Frontend API'ye bağlanamıyor
- Backend'in çalıştığını kontrol edin: `http://localhost:8080/actuator/health`
- CORS ayarlarını kontrol edin
- Browser console'da hata mesajlarını inceleyin

### Database migration hataları
- Flyway migration dosyalarını kontrol edin: `backend/src/main/resources/db/migration/`
- Veritabanını sıfırlamak için: `docker compose down -v` (dikkat: tüm veri silinir)

## 📚 Ek Kaynaklar

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Ant Design Documentation](https://ant.design/)
- [ECharts Documentation](https://echarts.apache.org/)
- [React Router Documentation](https://reactrouter.com/)

## 👥 Geliştiriciler

- **Irina** (ADMIN)
- **Samet** (ADMIN)

## 📄 Lisans

Bu proje kurumsal kullanım içindir.

---

**Not**: Production ortamında mutlaka:
1. `ISGC_JWT_SECRET` değişkenini güçlü bir değerle değiştirin (min 32 byte)
2. SMTP ayarlarını yapılandırın
3. `ISGC_STORAGE_ROOT` için uygun bir disk yolu belirleyin
4. HTTPS kullanın
5. Database şifrelerini güçlendirin
