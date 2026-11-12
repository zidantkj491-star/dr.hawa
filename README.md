# Dr. Hawa — Android prototype

Project Android menggunakan Kotlin + Jetpack Compose.

## Membuat APK melalui GitHub Actions (tanpa memasang Android Studio)

Paket ini sudah dilengkapi workflow cloud build. Kamu tetap memerlukan akun GitHub dan browser.

1. Ekstrak ZIP ini.
2. Buat repository baru di GitHub.
3. Upload seluruh isi folder `DrHawa` ke repository (termasuk folder tersembunyi `.github`).
4. Buka tab **Actions** pada repository.
5. Pilih workflow **Build Dr. Hawa APK**, lalu tekan **Run workflow**. Jika kamu push ke branch `main`, build juga berjalan otomatis.
6. Tunggu sampai workflow selesai dengan status hijau.
7. Buka hasil run, cari bagian **Artifacts**, lalu unduh `DrHawa-debug-apk`.
8. Ekstrak file artifact dan instal `app-debug.apk` di perangkat Android. Jika diminta, izinkan instalasi dari sumber tersebut.

## Membuat APK dengan Android Studio

1. Ekstrak ZIP.
2. Buka Android Studio → Open → pilih folder `DrHawa`.
3. Gunakan JDK 17 dan tunggu Gradle Sync selesai.
4. Pilih Build → Build Bundle(s) / APK(s) → Build APK(s).
5. APK debug biasanya tersedia di `app/build/outputs/apk/debug/app-debug.apk`.

## Fitur prototipe

- Pilihan suasana hati
- Chat Dr. Hawa dengan respons lokal berbasis kata kunci (belum memakai AI online)
- Input suara melalui layanan pengenalan suara Android
- Text-to-Speech untuk membacakan balasan
- Latihan napas sederhana
- Edukasi kesehatan mental
- Profil dan catatan privasi

## Catatan penting

Ini prototipe edukatif, bukan alat diagnosis atau layanan darurat. Chat belum terhubung ke model AI/API dan percakapan tidak disimpan permanen. Sebelum rilis publik, tambahkan kebijakan privasi, pengamanan data, pengujian, serta jalur bantuan profesional yang tervalidasi.

APK dari workflow ini adalah **debug APK** untuk uji coba dan instalasi manual, bukan paket rilis Play Store.
