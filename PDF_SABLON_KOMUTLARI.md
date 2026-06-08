# PDF Şablon Oluşturma Komutları - Yapay Zeka İçin

## Genel Gereksinimler
- A4 boyutunda (210mm x 297mm)
- Profesyonel, kurumsal görünüm
- Türkçe, İngilizce ve Rusça dil desteği
- Şirket logosu ve başlık alanı
- Sayfa numaralandırması
- Modern, temiz tasarım

---

## 1. BAŞLIK BÖLÜMÜ (Header Section)

**Komut:**
```
Bir PDF şablonu için başlık bölümü tasarla. Bu bölüm:
- Sayfanın en üstünde yer almalı
- Sol tarafta şirket logosu için 50x50mm alan bırak
- Sağ tarafta "KAZA RAPORU" (Türkçe), "ACCIDENT REPORT" (İngilizce), "ОТЧЕТ О НЕСЧАСТНОМ СЛУЧАЕ" (Rusça) başlığı
- Başlık fontu: Bold, 18-20pt
- Başlık altında ince bir çizgi (1pt, gri renk)
- Arka plan: Beyaz veya çok açık gri (#F5F5F5)
- Yükseklik: 60-70mm
- Padding: Üst ve yanlardan 10mm boşluk
```

---

## 2. TEMEL BİLGİLER BÖLÜMÜ (Basic Information Section)

**Komut:**
```
PDF şablonu için "Temel Bilgiler" bölümü tasarla. Bu bölüm:
- Başlık: "TEMEL BİLGİLER" (TR), "BASIC INFORMATION" (EN), "ОСНОВНАЯ ИНФОРМАЦИЯ" (RU)
- İki sütunlu tablo formatında:
  Sol sütun: Etiketler (Bold, 10pt, gri renk)
  Sağ sütun: Değerler (Normal, 10pt, siyah)
- Her satır arasında 8mm boşluk
- Etiket genişliği: 60mm
- Değer genişliği: 120mm
- İçerik:
  * Tarih (Date/Дата)
  * Konum (Location/Место)
  * Kaza Tipi (Accident Type/Тип несчастного случая)
  * Kaza Sınıfı (Accident Class/Класс несчастного случая)
  * Potansiyel Seviye (Potential Level/Уровень потенциальной опасности)
  * Durum (Status/Статус)
  * Alan (Area/Область) - opsiyonel
  * Tehlike Kaynağı (Hazard Source/Источник опасности) - opsiyonel
  * Yaralanan Vücut Bölgesi (Injured Body Part/Пораженная часть тела) - opsiyonel
  * Yaralanma Türü (Injury Type/Тип травмы) - opsiyonel
  * Sicil No (Registration No/Регистрационный номер) - opsiyonel
  * Supervisor - opsiyonel
  * Saat (Time/Время) - opsiyonel
- Bölüm başlığı altında ince çizgi
- Arka plan: Beyaz
- Padding: Tüm kenarlardan 10mm
```

---

## 3. AÇIKLAMA BÖLÜMÜ (Description Section)

**Komut:**
```
PDF şablonu için "Açıklama" bölümü tasarla. Bu bölüm:
- Başlık: "AÇIKLAMA" (TR), "DESCRIPTION" (EN), "ОПИСАНИЕ" (RU)
- Başlık: Bold, 12pt
- İçerik alanı: Çok satırlı metin için geniş alan
- Metin: Normal, 10pt, siyah
- Satır aralığı: 1.5
- Metin uzunluğuna göre otomatik sayfa geçişi
- Her satır maksimum 80 karakter
- Kelime taşması durumunda otomatik satır altına geçiş
- Padding: Tüm kenarlardan 10mm
- Başlık altında ince çizgi
- Arka plan: Beyaz
```

---

## 4. YARALI KİŞİLER BÖLÜMÜ (Injured People Section)

**Komut:**
```
PDF şablonu için "Yaralı Kişiler" bölümü tasarla. Bu bölüm:
- Başlık: "YARALI KİŞİLER" (TR), "INJURED PEOPLE" (EN), "ПОСТРАДАВШИЕ" (RU)
- Başlık: Bold, 12pt, büyük harfler
- Liste formatında:
  * Her kişi için numaralı liste (1., 2., 3. vb.)
  * Kişi adı: Normal, 10pt
  * Her kişi arasında 6mm boşluk
- Eğer yaralı kişi yoksa bu bölüm gösterilmemeli
- Başlık altında ince çizgi
- Padding: Tüm kenarlardan 10mm
- Arka plan: Beyaz
```

---

## 5. KİLİT KİŞİLER BÖLÜMÜ (Key People Section)

**Komut:**
```
PDF şablonu için "Kilit Kişiler" bölümü tasarla. Bu bölüm:
- Başlık: "KİLİT KİŞİLER" (TR), "KEY PEOPLE" (EN), "КЛЮЧЕВЫЕ ЛИЦА" (RU)
- Başlık: Bold, 12pt, büyük harfler
- Liste formatında:
  * Her kişi için numaralı liste (1., 2., 3. vb.)
  * Kişi adı: Normal, 10pt
  * Her kişi arasında 6mm boşluk
- Eğer kilit kişi yoksa bu bölüm gösterilmemeli
- Başlık altında ince çizgi
- Padding: Tüm kenarlardan 10mm
- Arka plan: Beyaz
```

---

## 6. FORM VERİLERİ BÖLÜMÜ (Form Data Section)

**Komut:**
```
PDF şablonu için "Form Verileri" bölümü tasarla. Bu bölüm:
- Başlık: "FORM VERİLERİ" (TR), "FORM DATA" (EN), "ДАННЫЕ ФОРМЫ" (RU)
- Başlık: Bold, 12pt, büyük harfler
- İki sütunlu format:
  Sol sütun: Alan adı + ":" (Bold, 10pt)
  Sağ sütun: Değer (Normal, 10pt)
- Her form alanı için ayrı satır
- Alan adı genişliği: 80mm
- Değer genişliği: 100mm
- Her satır arasında 6mm boşluk
- Eğer form verisi yoksa bu bölüm gösterilmemeli
- Başlık altında ince çizgi
- Padding: Tüm kenarlardan 10mm
- Arka plan: Beyaz
```

---

## 7. KÖK NEDEN ANALİZİ BÖLÜMÜ (Root Cause Analysis Section)

**Komut:**
```
PDF şablonu için "Kök Neden Analizi" bölümü tasarla. Bu bölüm:
- Başlık: "KÖK NEDEN ANALİZİ" (TR), "ROOT CAUSE ANALYSIS" (EN), "АНАЛИЗ ПЕРВОПРИЧИН" (RU)
- Başlık: Bold, 12pt, büyük harfler
- Hiyerarşik liste formatı:
  * Ana kategori (Bold, 10pt)
  * Alt kategori - "Evet" (Normal, 10pt, yeşil renk veya ✓ işareti)
  * Sadece "Evet" olan alt kategoriler gösterilmeli
- Her kategori arasında 8mm boşluk
- Alt kategoriler 20mm içeriden başlamalı (girinti)
- Format: "Ana Kategori - Alt Kategori: Evet"
- Eğer kök neden analizi yoksa bu bölüm gösterilmemeli
- Başlık altında ince çizgi
- Padding: Tüm kenarlardan 10mm
- Arka plan: Beyaz
```

---

## 8. EK FOTOĞRAFLAR/DOSYALAR BÖLÜMÜ (Photos/Files Section)

**Komut:**
```
PDF şablonu için "Ek Fotoğraflar/Dosyalar" bölümü tasarla. Bu bölüm:
- Başlık: "EK FOTOĞRAFLAR/DOSYALAR" (TR), "ATTACHED PHOTOS/FILES" (EN), "ПРИЛОЖЕННЫЕ ФОТО/ФАЙЛЫ" (RU)
- Başlık: Bold, 12pt, büyük harfler
- Fotoğraflar için:
  * Her fotoğraf maksimum 200mm yükseklikte
  * Genişlik orantılı olarak ayarlanmalı (en fazla sayfa genişliği - 20mm)
  * Fotoğraf altında dosya adı (Normal, 9pt, italik, gri)
  * Her fotoğraf arasında 10mm boşluk
- Dosyalar için (fotoğraf değilse):
  * "Dosya:" etiketi (Bold, 10pt)
  * Dosya adı (Normal, 10pt)
  * Her dosya arasında 6mm boşluk
- Eğer ek dosya yoksa bu bölüm gösterilmemeli
- Başlık altında ince çizgi
- Padding: Tüm kenarlardan 10mm
- Arka plan: Beyaz
- Sayfa taşması durumunda yeni sayfaya geçiş
```

---

## 9. ALT BİLGİ BÖLÜMÜ (Footer Section)

**Komut:**
```
PDF şablonu için alt bilgi (footer) bölümü tasarla. Bu bölüm:
- Sayfanın en altında
- Yükseklik: 20mm
- İçerik:
  * Sol tarafta: "Oluşturulma Tarihi: [Tarih]" (TR), "Created Date: [Date]" (EN), "Дата создания: [Дата]" (RU)
  * Sağ tarafta: "Sayfa X / Y" (TR), "Page X / Y" (EN), "Страница X / Y" (RU)
- Font: Normal, 8pt, gri renk
- Üstte ince çizgi (1pt, gri)
- Arka plan: Beyaz veya çok açık gri (#F5F5F5)
- Padding: Alt ve yanlardan 5mm boşluk
```

---

## 10. GENEL TASARIM KURALLARI

**Komut:**
```
PDF şablonu için genel tasarım kuralları:
- Renk paleti:
  * Ana renk: #2DBF81 (yeşil)
  * İkincil renk: #343D42 (koyu gri)
  * Metin: #000000 (siyah)
  * Etiketler: #666666 (orta gri)
  * Çizgiler: #CCCCCC (açık gri)
  * Arka plan: #FFFFFF (beyaz) veya #F5F5F5 (çok açık gri)
- Fontlar:
  * Başlıklar: Helvetica Bold veya Arial Bold
  * Metin: Helvetica veya Arial
  * Boyutlar: 8pt (küçük), 10pt (normal), 12pt (alt başlık), 18-20pt (ana başlık)
- Boşluklar:
  * Sayfa kenarları: 10mm margin
  * Bölümler arası: 15mm boşluk
  * Satırlar arası: 6-8mm
- Çizgiler:
  * Kalınlık: 1pt
  * Renk: #CCCCCC (açık gri)
  * Stil: Solid (düz)
- Sayfa numaralandırması:
  * Her sayfanın altında
  * Format: "Sayfa X / Y"
  * Sağ hizalı
- Logo alanı:
  * Sol üst köşe
  * Boyut: 50x50mm
  * Arka plan: Şeffaf veya beyaz
```

---

## 11. ÇOK DİLLİ DESTEK

**Komut:**
```
PDF şablonu için çok dilli destek:
- Türkçe (TR): Varsayılan dil
- İngilizce (EN): İkinci dil
- Rusça (RU): Üçüncü dil
- Tüm başlıklar ve etiketler seçilen dile göre değişmeli
- Tarih formatları:
  * TR: dd.MM.yyyy HH:mm
  * EN: MM/dd/yyyy HH:mm
  * RU: dd.MM.yyyy HH:mm
- Font desteği:
  * Türkçe karakterler (İ, ı, Ğ, ğ, Ü, ü, Ş, ş, Ö, ö, Ç, ç)
  * Rusça Kiril karakterler (tüm alfabe)
  * Karakterler desteklenmiyorsa transliterasyon kullanılmalı
```

---

## 12. SAYFA YÖNETİMİ

**Komut:**
```
PDF şablonu için sayfa yönetimi:
- İçerik uzunluğuna göre otomatik sayfa ekleme
- Her sayfada:
  * Üst bilgi (header) tekrarlanmalı
  * Alt bilgi (footer) tekrarlanmalı
  * Sayfa numarası güncellenmeli
- Sayfa geçişi:
  * Minimum 50mm boşluk kaldığında yeni sayfaya geç
  * Bölüm başlıkları sayfa sonunda kalırsa bir sonraki sayfaya taşı
  * Fotoğraflar bölünmemeli, tam sayfa geçişi yapılmalı
- Sayfa boyutu: A4 (210mm x 297mm)
- Yönlendirme: Dikey (Portrait)
```

---

## KULLANIM NOTLARI

Bu komutları yapay zekaya (ChatGPT, Claude, vb.) verirken:
1. Her bölümü ayrı ayrı kopyalayıp yapıştırın
2. "Bu komutlara göre bir PDF şablonu tasarla" diye ekleyin
3. Örnek: "1. BAŞLIK BÖLÜMÜ komutuna göre bir PDF başlık tasarımı oluştur"
4. Tüm bölümler hazır olduktan sonra "Tüm bölümleri birleştirerek tam bir PDF şablonu oluştur" diyebilirsiniz

