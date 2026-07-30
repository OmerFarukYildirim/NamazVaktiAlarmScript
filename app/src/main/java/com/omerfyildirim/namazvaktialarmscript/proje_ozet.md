# Proje Özeti: Headless Namaz Vakti Alarm Servisi (Android)

Sen tecrübeli bir Senior Android Geliştiricisisin. Benimle birlikte, arayüzü olmayan (headless/invisible) ve arka planda çalışarak her gün namaz vakitleri için sistem alarmı kuran bir Android uygulaması geliştireceğiz. Projeyi GitHub'da açık kaynak olarak paylaşacağım için kodun modüler, modern ve temiz olmasını istiyorum.

**Dil Seçimi:** Projenin modern Android standartlarına uygunluğu, null güvenliği ve Coroutines desteği nedeniyle **Kotlin** ile ilerlenmesi kararlaştırılmıştır.

## Temel İşlevler ve Mimari Gereksinimler

### 1. Görünmez Arayüz (Invisible Activity) ve İzinler
* Uygulamanın bir UI'ı (kullanıcı arayüzü) olmayacak. Kullanıcı uygulama ikonuna tıkladığında `Theme.NoDisplay` kullanan şeffaf bir Activity açılacak.
* Bu Activity sadece şu izinleri isteyecek: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `SET_ALARM`, ve Android 13+ için `POST_NOTIFICATIONS`.
* **Önemli:** Arkaplan konum izni (`ACCESS_BACKGROUND_LOCATION`) Android 10+ cihazlarda öncelikle ön plan konum izinleri alındıktan sonra ayrı bir diyalog/yönlendirme ile istenir.
* Kullanıcıdan ayrıca "Pil Optimizasyonunu Yoksayma" (Ignore Battery Optimizations) izni de istenmeli.
* İzinler verildikten hemen sonra uygulama `WorkManager` görevini zamanlayacak ve `finish()` ile kendini kapatacak.

### 2. Arka Plan Tetikleyicisi (WorkManager)
* `WorkManager` kullanılarak günde 1 kez (örneğin gece 02:00'de) çalışacak bir `PeriodicWorkRequest` oluşturulacak.
* Bu Worker sınıfı uyandığında sırasıyla: Konumu alacak, API isteği atacak ve alarmları kuracak.

### 3. Konum ve Veri Çekme (Location & REST API)
* `FusedLocationProviderClient` kullanılarak cihazın son bilinen veya güncel konumu alınacak.
* `Retrofit` kütüphanesi ile Aladhan API'sine (veya benzeri bir JSON REST API'ye) HTTP GET isteği atılacak. Diyanet hesaplaması için `method=13` parametresi kullanılacak.
* API Yanıtı için `Data Transfer Object (DTO)` sınıfları oluşturulacak.

### 4. Sistem Alarmını Kurma
* API'den dönen namaz vakti saatlerinden (örneğin sabah namazı) 5 dakika öncesi hesaplanacak.
* `AlarmClock.ACTION_SET_ALARM` intent'i kullanılarak, kullanıcıya hiçbir UI göstermeden (`EXTRA_SKIP_UI = true`) Android'in yerel saat uygulamasına alarm kurulacak.
* O günün 5 vakti için de bir döngü kurularak 5 ayrı alarm oluşturulmalı.
* **Not:** Mevcut alarmları temizleme mekanizması (mümkünse) veya üst üste binmemesi için kontrol eklenmeli.

### 5. Cihaz Yeniden Başlatıldığında (BootReceiver)
* `RECEIVE_BOOT_COMPLETED` izniyle çalışan bir `BroadcastReceiver` cihaz açıldığında WorkManager görevini tekrar sisteme kaydettirecek.

## Teknik Yığın (Tech Stack)
* **Language:** Kotlin
* **Async:** Kotlin Coroutines
* **Background Work:** WorkManager
* **Networking:** Retrofit + Gson
* **Location:** Google Play Services Location
* **Architecture:** MVVM (Minimalistic for headless)

## Beklentim
Lütfen bu mimariye uygun olarak:
1. `libs.versions.toml` ve `build.gradle.kts` güncellemelerini,
2. `AndroidManifest.xml` dosyasındaki gerekli düzenlemeleri,
3. Görünmez Ana Activity (İzin yönetimi ve tetikleme) kodunu,
4. Arka planda çalışacak `Worker` sınıfının kodunu (Coroutines kullanarak),
5. `BootReceiver` sınıfının kodunu sırasıyla benimle paylaş.
