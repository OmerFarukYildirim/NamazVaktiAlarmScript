# Namaz Vakti Alarm Script — Headless Namaz Alarm Uygulaması

> Konum tabanlı, arka planda çalışan, her gün otomatik olarak 5 vakit namaz için sistem alarmı kuran Android uygulaması.

---

## 📋 İçindekiler

- [Proje Hakkında](#proje-hakkında)
- [Özellikler](#özellikler)
- [Ekran Görüntüleri](#ekran-görüntüleri)
- [Teknolojiler](#teknolojiler)
- [Mimari](#mimari)
- [Sınıf Yapısı](#sınıf-yapısı)
- [Kurulum](#kurulum)
- [Kullanım](#kullanım)
- [İzinler](#i̇zinler)
- [API](#api)
- [Katkıda Bulunma](#katkıda-bulunma)
- [Lisans](#lisans)

---

## 🎯 Proje Hakkında

**Namaz Vakti Alarm Script**, kullanıcıya herhangi bir arayüz göstermeden çalışan **headless (görünmez)** bir Android uygulamasıdır. Uygulama ikonuna tıklandığında şeffaf bir Activity açılır, gerekli izinleri ister ve arka planda günlük namaz vakitleri için otomatik alarm kurar.

### Nasıl Çalışır?

```
1. Kullanıcı uygulama ikonuna tıklar
   └─► Şeffaf Activity açılır (Theme.NoDisplay)

2. İzinler kontrol edilir & istenir
   └─► Konum (Fine, Coarse, Background)
   └─► Bildirim (Android 13+)
   └─► Pil optimizasyonunu yoksayma
   └─► Kesin alarm izni (Android 12+)

3. WorkManager görevi zamanlanır
   └─► Her gece 00:05'te çalışacak

4. Worker her gün şunları yapar:
   └─► Konum alır (Fused Location Provider)
   └─► Aladhan API'den vakitleri çeker (method=13 Diyanet)
   └─► Her vakit için 5 dk öncesine alarm kurar

5. Cihaz yeniden başlatıldığında:
   └─► BootReceiver WorkManager'ı yeniden kaydeder
```

---

## ✨ Özellikler

### 🕌 Namaz Vakitleri
- **5 Vakit Alarm** — Sabah (Fajr), Öğle (Dhuhr), İkindi (Asr), Akşam (Maghrib), Yatsı (Isha)
- **Diyanet Hesaplaması** — Aladhan API `method=13` parametresi ile Türkiye Diyanet İşleri hesaplaması
- **5 Dakika Öncesi** — Her vaktin 5 dakika öncesinde alarm çalar
- **Konum Tabanlı** — Cihazın anlık GPS konumuna göre vakit hesaplama

### ⚙️ Arka Plan Çalışma
- **WorkManager** — Güvenilir, batarya dostu periyodik görevler
- **BootReceiver** — Cihaz yeniden başlatıldığında otomatik yeniden kayıt
- **Pil Optimizasyonu** — Sistem tarafından öldürülmemek için "Pil Optimizasyonunu Yoksayma" izni
- **Ağ Bağımlılığı** — İnternet bağlantısı olmadan çalışmaz (Constraints)

### 🎨 Görünmez Arayüz
- **Theme.NoDisplay** — Uygulama ikonuna tıklanınca hiçbir UI göstermeden çalışır
- **Otomatik Kapatma** — İzinler ve WorkManager kaydı tamamlandıktan sonra `finish()` ile kendini kapatır
- **Tek Tıkla Kurulum** — Kullanıcı sadece izinleri verir, gerisini uygulama halleder

---

## 📸 Ekran Görüntüleri

> *(Uygulama headless olduğu için UI ekran görüntüsü yoktur. Sadece izin diyalogları görünür.)*

---

## 🛠️ Teknolojiler

| Teknoloji | Amaç |
|-----------|------|
| Kotlin | Programlama dili |
| Android SDK 35 (API 35) | Target SDK |
| Min SDK 24 (Android 7.0) | Minimum desteklenen sürüm |
| WorkManager | Arka plan periyodik görevler |
| Retrofit 2 | HTTP istekleri (REST API) |
| Gson | JSON parse |
| Google Play Services Location | Fused Location Provider |
| AlarmClock API | Sistem alarmı kurma |

---

## 🏗️ Mimari

```
┌─────────────────────────────────────────────────────────────┐
│                    Kullanıcı                                │
│              (Uygulama İkonuna Tıklar)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              MainActivity (Theme.NoDisplay)                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   İzin      │  │   Pil       │  │   Kesin Alarm       │  │
│  │  Kontrolü   │  │ Optimizasyon│  │   İzni Kontrolü     │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
│         │                │                    │              │
│         └────────────────┴────────────────────┘              │
│                           │                                  │
│                           ▼                                  │
│              ┌─────────────────────┐                        │
│              │   WorkManager       │                        │
│              │   Zamanlama         │                        │
│              └──────────┬──────────┘                        │
│                         │ finish()                          │
└─────────────────────────┼───────────────────────────────────┘
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
┌─────────────────────────┐  ┌─────────────────────────┐
│   OneTimeWorkRequest    │  │  PeriodicWorkRequest    │
│   (Anında çalıştır)     │  │  (Her 24 saatte bir)    │
│   "PrayerWorkImmediate" │  │  "PrayerWork"           │
└───────────┬─────────────┘  └───────────┬─────────────┘
            │                            │
            └────────────┬───────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    PrayerWorker                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Konum     │  │   Aladhan   │  │    Alarm Kurma      │  │
│  │   Alma      │  │    API      │  │   (AlarmClock)      │  │
│  │(Fused Loc.) │  │ (method=13) │  │                     │  │
│  └──────┬──────┘  └──────┬──────┘  └─────────────────────┘  │
│         │                │                                    │
│         └────────────────┴────────────────────────────────────┘
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    BootReceiver                             │
│         (ACTION_BOOT_COMPLETED)                             │
│              WorkManager'ı Yeniden Kaydet                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Sınıf Yapısı

| Sınıf | Açıklama |
|-------|----------|
| **MainActivity.kt** | Şeffaf Activity. İzinleri kontrol eder, WorkManager'ı zamanlar ve kendini kapatır |
| **PrayerWorker.kt** | Arka planda çalışan CoroutineWorker. Konum alır, API'den vakitleri çeker, alarm kurar |
| **BootReceiver.kt** | Cihaz açıldığında WorkManager görevini yeniden kaydeden BroadcastReceiver |
| **AladhanApi.kt** | Retrofit interface. Aladhan API'ye HTTP GET isteği atan metod ve data class'lar |

---

## 🚀 Kurulum

### Gereksinimler

- [Android Studio](https://developer.android.com/studio) (Giraffe veya daha yeni)
- [Android SDK](https://developer.android.com/studio/intro/update) API 24+
- [JDK 11+](https://adoptium.net/)

### Adım Adım Kurulum

```bash
# 1. Repoyu klonla
git clone https://github.com/OmerFarukYildirim/NamazVaktiAlarmScript.git
cd NamazVaktiAlarmScript

# 2. Android Studio ile aç
# File → Open → NamazVaktiAlarmScript klasörünü seç

# 3. Gradle sync yap
# Android Studio'da "Sync Project with Gradle Files" butonuna tıkla

# 4. Cihazda/emülatörde çalıştır
# Run → Run 'app' (Shift+F10)
```

### Derleme (Komut Satırı)

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

---

## 📱 Kullanım

1. **Uygulamayı Yükle** — APK'yı cihaza kur veya Android Studio'dan çalıştır
2. **İkonuna Tıkla** — Uygulama listesindeki ikona dokun
3. **İzinleri Ver** — Konum, bildirim, pil optimizasyonu ve kesin alarm izinlerini onayla
4. **Bitti!** — Uygulama kendini kapatır, her gece 00:05'te otomatik olarak ertesi günün alarmlarını kurar

> 💡 **Not:** Uygulama herhangi bir arayüz göstermez. Alarmlar doğrudan Android'in yerel Saat uygulamasına kurulur.

---

## 🔐 İzinler

| İzin | Açıklama | Android Sürümü |
|------|----------|---------------|
| `ACCESS_FINE_LOCATION` | Hassas konum bilgisi | Tümü |
| `ACCESS_COARSE_LOCATION` | Yaklaşık konum bilgisi | Tümü |
| `ACCESS_BACKGROUND_LOCATION` | Arka planda konum erişimi | 10+ (Q) |
| `POST_NOTIFICATIONS` | Bildirim gösterme | 13+ (Tiramisu) |
| `RECEIVE_BOOT_COMPLETED` | Cihaz açıldığında tetiklenme | Tümü |
| `SCHEDULE_EXACT_ALARM` | Kesin alarm kurma | 12+ (S) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Pil optimizasyonunu yoksayma | Tümü |

---

## 🌐 API

### Aladhan API

Uygulama, namaz vakitlerini [Aladhan API](https://aladhan.com/prayer-times-api)'den çeker:

```
GET https://api.aladhan.com/v1/timings?latitude={lat}&longitude={lon}&method=13
```

| Parametre | Değer | Açıklama |
|-----------|-------|----------|
| `latitude` | Konum enlemi | Cihaz GPS konumu |
| `longitude` | Konum boylamı | Cihaz GPS konumu |
| `method` | `13` | Diyanet İşleri hesaplama metodu |

### Yanıt Formatı

```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "timings": {
      "Fajr": "05:32",
      "Dhuhr": "13:15",
      "Asr": "16:45",
      "Maghrib": "19:52",
      "Isha": "21:20"
    },
    "date": {
      "readable": "15 Aug 2026",
      "timestamp": "1755225600"
    }
  }
}
```

---

## ⚙️ WorkManager Zamanlaması

| Görev | Tip | Periyot | Başlangıç |
|-------|-----|---------|-----------|
| `PrayerWorkImmediate` | OneTimeWork | Tek seferlik | Uygulama açıldığında |
| `PrayerWork` | PeriodicWork | 24 saat | Her gece 00:05 |

> 🔄 **BootReceiver**, cihaz yeniden başlatıldığında her iki görevi de otomatik olarak yeniden zamanlar.

---

## 🤝 Katkıda Bulunma

1. Fork edin
2. Branch oluşturun (`git checkout -b feature/...`)
3. Commit edin (`git commit -m 'feat: ...'`)
4. Push edin (`git push origin feature/...`)
5. Pull Request açın

---

## 📄 Lisans

[MIT](LICENSE)

---

## 👤 Geliştirici

**Ömer Faruk Yıldırım** — [@OmerFarukYildirim](https://github.com/OmerFarukYildirim)

---

> ⚠️ **Not:** Bu uygulama Android'in `AlarmClock.ACTION_SET_ALARM` intent'ini kullanarak sistem saat uygulamasına alarm kurar. Cihazda bir saat/alarma uygulaması yüklü olmalıdır.
