# ifs25034-p5-pbo2026-springboot-cashflow

Aplikasi **Cashflow (Catatan Keuangan)** — Praktikum P5 PBO 2026.
Spring Boot 4.1.2 · Vaadin 25.3.4 · JDBC PostgreSQL · Clean Architecture.

## Kebutuhan
- JDK 25 (sesuai modul) dan Maven 3.9+ (sudah termasuk di IntelliJ IDEA)
- PostgreSQL 14+ (contoh: 17)

## 1. Siapkan database
```sql
CREATE DATABASE pbo2026_cashflow;
```
Windows (PowerShell):
```powershell
$env:PGPASSWORD="postgres"; & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE pbo2026_cashflow;"
```
Tabel `users` dan `transactions` dibuat otomatis saat aplikasi pertama kali berjalan.

## 2. Siapkan `.env`
Salin `.env.example` menjadi `.env` lalu sesuaikan `DB_PASSWORD`, `ROOT_USERNAME`, `ROOT_PASSWORD`.
`.env` tidak ikut Git. Opsional: `SESSION_SECRET` (kunci tanda tangan cookie sesi).

## 3. Jalankan
```bash
mvn spring-boot:run
```
Buka http://localhost:8080 . Login admin memakai `ROOT_USERNAME` / `ROOT_PASSWORD`.
(Jika ingin wrapper: `mvn wrapper:wrapper` lalu gunakan `./mvnw` atau `mvnw.cmd`.)

Build JAR (wajib profil `production`):
```bash
mvn clean package -Pproduction
java -jar target/ifs25034-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

## 4. Pengujian
```bash
mvn clean verify
```
- Unit test memakai repository InMemory; tes JDBC dan `AppTest` memakai H2 (profil `test`).
- Laporan JaCoCo: `target/site/jacoco/index.html` (package `framework/view`, `framework/auth`, dan `App` dikecualikan).
- Gerbang coverage default 80%. Untuk target modul 100%: `mvn clean verify -Djacoco.minimum=1.00`.

## Rute
| Path | Akses |
|---|---|
| `/login`, `/register` | Publik (sudah login -> dialihkan ke `/`) |
| `/uploads/**`, `/icons/**`, `styles.css` | Publik (statis) |
| `/`, `/profile` | USER & ADMIN |
| `/users`, `/user-transactions?userId=ID` | ADMIN saja |

## Struktur
`domain` (entity, exception, repository, PasswordHasher) <- `usecase` <- `adapter` (presenter, repository, storage) & `framework` (auth, view); wiring di `config/AppConfig`.
