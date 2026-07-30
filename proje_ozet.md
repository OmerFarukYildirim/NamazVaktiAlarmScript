# Proje Özeti: Headless Namaz Vakti Alarm Servisi (Android)

Sen tecrübeli bir Senior Android Geliştiricisisin. Benimle birlikte, arayüzü olmayan (headless/invisible) ve arka planda çalışarak her gün namaz vakitleri için sistem alarmı kuran bir Android uygulaması geliştireceğiz. Projeyi GitHub'da açık kaynak olarak paylaşacağım için kodun modüler, modern ve temiz olmasını istiyorum. Dili Java olarak seçebilirsin, hangisinde daha stabil bir yapı kuracaksan onunla ilerleyelim.

## Temel İşlevler ve Mimari Gereksinimler

Lütfen bana aşağıda belirttiğim kurguyu hayata geçirecek kod mimarisini ve gerekli sınıfları adım adım sağla.

### 1. Görünmez Arayüz (Invisible Activity) ve İzinler
* Uygulamanın bir UI'ı (kullanıcı arayüzü) olmayacak. Kullanıcı uygulama ikonuna tıkladığında `Theme.NoDisplay` kullanan şeffaf bir Activity açılacak.
* Bu Activity sadece şu izinleri isteyecek: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `SET_ALARM`, ve Android 13+ için `POST_NOTIFICATIONS`.
* Kullanıcıdan ayrıca "Pil Optimizasyonunu Yoksayma" (Ignore Battery Optimizations) izni de istenmeli ki arka plan süreçleri sistem tarafından öldürülmesin.
* İzinler verildikten hemen sonra uygulama `WorkManager` görevini zamanlayacak ve `finish()` ile kendini tamamen kapatacak.

### 2. Arka Plan Tetikleyicisi (WorkManager)
* `WorkManager` kullanılarak günde 1 kez (örneğin gece 02:00'de) çalışacak bir `PeriodicWorkRequest` oluşturulacak.
* Bu Worker sınıfı uyandığında sırasıyla: Konumu alacak, API isteği atacak ve alarmları kuracak.

### 3. Konum ve Veri Çekme (Location & REST API)
* Worker çalıştığında Fused Location Provider kullanılarak cihazın o anki enlem ve boylamı alınacak.
* Alınan koordinatlar ile Aladhan API'sine (veya benzeri bir JSON REST API'ye) HTTP GET isteği atılacak. Diyanet hesaplaması için `method=13` parametresi kullanılacak.

### 4. Sistem Alarmını Kurma
* API'den dönen namaz vakti saatlerinden (örneğin sabah namazı) 5 dakika öncesi hesaplanacak.
* `AlarmClock.ACTION_SET_ALARM` intent'i kullanılarak, kullanıcıya hiçbir UI göstermeden (`EXTRA_SKIP_UI = true`) Android'in yerel saat uygulamasına alarm kurulacak.
* O günün 5 vakti için de bir döngü kurularak 5 ayrı alarm oluşturulmalı.

### 5. Cihaz Yeniden Başlatıldığında (BootReceiver)
* Kullanıcı telefonunu yeniden başlattığında WorkManager görevlerinin iptal olmaması için `RECEIVE_BOOT_COMPLETED` izniyle çalışan bir `BroadcastReceiver` yazılacak.
* Bu receiver cihaz açıldığında WorkManager görevini tekrar sisteme kaydettirecek.

## Beklentim
Lütfen bu mimariye uygun olarak:
1. `AndroidManifest.xml` dosyasındaki gerekli düzenlemeleri,
2. Görünmez Ana Activity (İzin yönetimi ve tetikleme) kodunu,
3. Arka planda çalışacak `Worker` sınıfının iskelet kodunu (Location, API call, Alarm set mantığını içeren),
4. `BootReceiver` sınıfının kodunu sırasıyla benimle paylaş.