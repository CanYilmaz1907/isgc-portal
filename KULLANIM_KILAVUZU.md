# 📖 ISG-C Portal Kullanım Kılavuzu

## 🚀 Başlangıç

### 1. Giriş Yapma

1. Tarayıcınızda `http://localhost:5173` adresini açın
2. Login sayfasında:
   - **Kullanıcı Adı**: `irina` veya `samet`
   - **Şifre**: `irina123` veya `samet123`
3. "Giriş" butonuna tıklayın

### 2. Ana Ekran (Dashboard)

Giriş yaptıktan sonra Dashboard'a yönlendirilirsiniz. Burada:
- **Açık Uygunsuzluk** sayısı
- **Bu Ay Kaza** sayısı
- **Tamamlanan Denetim** sayısı

gibi özet istatistikler görüntülenir.

---

## 📋 Modüller ve Kullanım

### 1. 🚨 Kaza Yönetimi

**Yer**: Sol menüden "Kaza Yönetimi" seçeneği

#### Yeni Kaza Oluşturma

1. "Kaza Yönetimi" sayfasında **"Yeni Kaza"** butonuna tıklayın
2. Form alanlarını doldurun:
   - **Kaza Tipi**: Dropdown'dan seçin (örn: "Kayma / Takılma / Düşme")
   - **Proje**: Kaza ile ilişkili projeyi seçin
   - **Lokasyon**: Kaza yerini girin
   - **Tarih/Saat**: Kaza zamanını seçin
   - **Kaza Sınıfı**: MINOR, MAJOR, FATAL
   - **Potansiyel Seviye**: LOW, MEDIUM, HIGH, CRITICAL
3. **Dinamik Form Alanları**: Seçtiğiniz kaza tipine göre ek alanlar görünecektir:
   - Örnek: "Kayma / Takılma / Düşme" seçerseniz → "Zemin Tipi", "KKD Kullanıldı mı?" gibi alanlar
4. **Yaralı Personel**: Birden fazla personel seçebilirsiniz
5. **Kilit Personel**: Kaza ile ilgili önemli personeli seçin
6. **Açıklama**: Detaylı açıklama ekleyin
7. **Kaydet** butonuna tıklayın

**Önemli**: Kaza kaydedildiğinde, kaza sınıfı ve potansiyel seviyesine göre otomatik e-posta bildirimi gönderilir.

#### Kaza Detayları ve Kök Neden Analizi

1. Kaza listesinde bir kazaya tıklayın
2. Detay sayfasında:
   - Tüm kaza bilgileri
   - Yaralı personelin eğitim bilgileri
   - Kilit personelin eğitim bilgileri
   - **Kök Neden Analizi** sekmesi

**Kök Neden Analizi Yapma**:
1. "Kök Neden Analizi" sekmesine gidin
2. 4 kategoriden seçim yapın:
   - **Human (İnsan)**: Eğitim eksikliği, dikkatsizlik, vb.
   - **Equipment (Ekipman)**: Arıza, bakım eksikliği, vb.
   - **Environment (Çevre)**: Kötü hava, zemin koşulları, vb.
   - **Management (Yönetim)**: Prosedür eksikliği, denetim eksikliği, vb.
3. Her kategori için birden fazla neden seçebilirsiniz
4. **Kaydet** butonuna tıklayın
5. Otomatik olarak kök neden diyagramı oluşturulur

#### Kaza Raporları

**Yer**: Sol menüden "Kaza Raporları"

Bu sayfada:
- **Trend Analizi**: Günlük, haftalık, aylık kaza trendleri
- **Dağılım Grafikleri**: Kaza sınıflarına, tiplerine göre dağılım
- **Proje Bazlı Filtreleme**: Belirli projeler için raporlar
- **Tarih Aralığı Filtreleme**: Belirli dönemler için analiz

**Otomatik E-posta Raporları**:
1. "Abonelik Yönetimi" bölümünden yeni abonelik oluşturun
2. Periyot seçin (günlük, haftalık, aylık)
3. Projeleri seçin
4. E-posta adreslerini girin
5. Sistem otomatik olarak seçilen periyotta rapor gönderecektir

---

### 2. ⚠️ Uygunsuzluk Takibi

**Yer**: Sol menüden "Uygunsuzluk Takip"

#### Yeni Uygunsuzluk Oluşturma

1. "Uygunsuzluk Takip" sayfasında **"Yeni Uygunsuzluk"** butonuna tıklayın
2. Form alanlarını doldurun:
   - **Proje**: Uygunsuzluğun olduğu proje
   - **Yetkili Kişi**: Sorumlu personel
   - **Tehlikeli Sınıf**: Çok Tehlikeli, Tehlikeli, Az Tehlikeli
   - **Durum**: AÇIK, KAPALI, vb.
   - **Açıklama**: Detaylı açıklama
3. **Fotoğraf Yükleme**: "Fotoğraf Ekle" butonuna tıklayıp fotoğraf seçin
4. **Dinamik Alanlar**: Tehlikeli sınıfa göre ek alanlar görünebilir
5. **Kaydet** butonuna tıklayın

**Önemli**: 
- Açık uygunsuzluklar için otomatik hatırlatma e-postaları gönderilir
- Yetkili kişiye düzenli hatırlatmalar yapılır

#### Uygunsuzluk Grafikleri

**Yer**: Sol menüden "Uygunsuzluk Grafikler"

Bu sayfada:
- Tehlikeli sınıflara göre dağılım
- Proje bazlı istatistikler
- Durum bazlı grafikler
- Trend analizleri

---

### 3. 📝 Disiplin Logu

**Yer**: Sol menüden "Disiplin Logu"

#### Yeni Disiplin Kaydı Oluşturma

1. "Disiplin Logu" sayfasında **"Yeni Kayıt"** butonuna tıklayın
2. Form alanlarını doldurun:
   - **Çalışan**: Dropdown'dan seçin
   - **Profesyon**: Otomatik doldurulur (çalışanın profesyonu)
   - **Birincil Yönetici**: Otomatik doldurulur (çalışanın yöneticisi)
   - **Uyumsuz Çalışan**: Gerekirse farklı bir çalışan seçebilirsiniz
   - **Uyumsuz Çalışanın Yöneticisi**: Otomatik doldurulur
   - **Tarih**: Olay tarihi
   - **Açıklama**: Detaylı açıklama
3. **Kaydet** butonuna tıklayın

**Önemli**: 
- Sistem otomatik olarak çalışan-yönetici ilişkisini günceller
- İlgili taraflara otomatik e-posta gönderilir
- Düzenli özet e-postaları gönderilir

#### Disiplin İstatistikleri

Disiplin logu sayfasında:
- Çalışan bazlı istatistikler
- Tarih aralığı filtreleme
- Grafikler ve raporlar

---

### 4. ✅ Denetimler ve Kontrol Listeleri

#### Kontrol Listesi Oluşturma

**Yer**: Sol menüden "Kontrol Listeleri"

1. "Kontrol Listeleri" sayfasında **"Yeni Kontrol Listesi"** butonuna tıklayın
2. Form alanlarını doldurun:
   - **Kod**: Benzersiz kod (örn: "ISG-001")
   - **Başlık**: Kontrol listesi adı
   - **Kapsam**: Açıklama
   - **Maddeler**: Her madde için:
     - **Soru/Madde**: Kontrol edilecek konu
     - **Ağırlık**: 1-10 arası önem derecesi
     - **Kategori**: İSG, Kalite, Çevre, vb.
3. **Kaydet** butonuna tıklayın

#### Denetim Oluşturma ve Çalıştırma

**Yer**: Sol menüden "Denetimler"

1. "Denetimler" sayfasında **"Yeni Denetim"** butonuna tıklayın
2. Form alanlarını doldurun:
   - **Denetim Tipi**: İç Denetim veya Dış Denetim
   - **Proje**: Denetim yapılacak proje
   - **Kontrol Listesi**: Daha önce oluşturduğunuz kontrol listesini seçin
   - **Denetim Tarihi**: Tarih seçin
   - **Denetçiler**: Birden fazla denetçi seçebilirsiniz
3. **Kaydet** butonuna tıklayın

#### Denetim Çalıştırma

1. Denetim listesinde bir denetime tıklayın
2. "Denetim Çalıştır" butonuna tıklayın
3. Her kontrol listesi maddesi için:
   - **Uygunluk**: Uygun, Uygunsuz, Kısmen Uygun
   - **Notlar**: Gözlemlerinizi yazın
   - **Kanıt**: Fotoğraf veya dosya ekleyebilirsiniz
4. Tüm maddeleri doldurduktan sonra **"Denetimi Tamamla"** butonuna tıklayın

**Otomatik İşlemler**:
- Sistem otomatik olarak ağırlıklı skor hesaplar
- HTML formatında profesyonel rapor oluşturulur
- Proje yöneticisine ve yönetime e-posta gönderilir (CC)

#### Denetim Arşivi

**Yer**: Sol menüden "Denetim Arşiv"

Tamamlanan denetimler burada arşivlenir:
- Denetim detayları
- Görseller ve dosyalar
- Katılımcı bilgileri
- Raporlar

---

### 5. 📄 Doküman & Blank Form Yönetimi

**Yer**: Sol menüden "Doküman & Blank Formlar"

#### Doküman Yükleme (Sadece ADMIN)

1. "Doküman & Blank Formlar" sayfasında **"Yeni Doküman"** butonuna tıklayın
2. Form alanlarını doldurun:
   - **Kod**: Benzersiz kod (örn: "FORM-001")
   - **Başlık**: Doküman adı
   - **Açıklama**: İsteğe bağlı açıklama
3. **PDF Dosyası**: PDF dosyasını seçin ve yükleyin
4. **Kaydet** butonuna tıklayın

#### Doküman İndirme

1. Doküman listesinde bir dokümana tıklayın
2. **"İndir"** butonuna tıklayın
3. PDF dosyası indirilecektir

**Not**: 
- **ADMIN** (Irina, Samet): Yükleme, güncelleme, silme yetkisi
- **ISG_C**: Sadece görüntüleme ve indirme yetkisi

---

## 🔐 Roller ve Yetkiler

### ADMIN (Irina, Samet)
- ✅ Tüm modüllerde tam yetki
- ✅ Kullanıcı ekleme/silme/güncelleme
- ✅ Doküman yükleme/silme
- ✅ Tüm verileri görüntüleme ve düzenleme

### ISG_C
- ✅ Tüm modülleri görüntüleme
- ✅ Veri girişi yapabilme
- ✅ Raporları görüntüleme
- ❌ Kullanıcı yönetimi yapamaz
- ❌ Doküman yükleyemez/silemez

### YONETICI
- ✅ İlişkili projeleri görüntüleme
- ✅ Proje çalışanlarını görüntüleme
- ✅ Proje bazlı raporları görüntüleme
- ❌ Tüm sistem verilerine erişemez

### PERSONEL
- ✅ Sadece kendi kayıtlarını görüntüleme
- ✅ Kendi kaza kayıtlarını görüntüleme
- ❌ Diğer verilere erişemez

---

## 💡 İpuçları ve Best Practices

### 1. Kaza Yönetimi
- Kaza oluştururken **mutlaka** kaza sınıfı ve potansiyel seviyesini doğru seçin (e-posta bildirimleri buna göre gönderilir)
- Kök neden analizini **mutlaka** yapın - bu, benzer kazaları önlemek için kritiktir
- Yaralı personelin eğitim bilgilerini kontrol edin

### 2. Uygunsuzluk Takibi
- Fotoğraf eklemeyi unutmayın - görsel kanıt önemlidir
- Yetkili kişiyi doğru seçin - hatırlatma e-postaları ona gönderilir
- Açık uygunsuzlukları düzenli kontrol edin

### 3. Denetimler
- Kontrol listesi maddelerine **ağırlık** verirken dikkatli olun (skor hesaplaması etkilenir)
- Denetim sırasında **kanıt** (fotoğraf/dosya) eklemeyi unutmayın
- Denetim tamamlandıktan sonra raporu kontrol edin

### 4. Genel
- **Dashboard**'u düzenli kontrol edin - özet bilgiler burada
- **Swagger UI** (`http://localhost:8080/swagger-ui.html`) ile API'leri test edebilirsiniz
- E-posta bildirimleri için SMTP ayarlarını production'da yapılandırın

---

## 🆘 Sorun Giderme

### Login yapamıyorum
- Kullanıcı adı ve şifrenin doğru olduğundan emin olun
- Backend'in çalıştığını kontrol edin: `http://localhost:8080/actuator/health`
- Admin kullanıcıları oluşturmak için: `POST http://localhost:8080/api/admin/setup/create-users`

### CORS hatası alıyorum
- Backend'in yeniden başlatıldığından emin olun
- CORS yapılandırması `SecurityConfig.java` dosyasında kontrol edin

### E-posta gönderilmiyor
- SMTP ayarlarını kontrol edin (`application.yml` veya environment variables)
- Development için MailHog gibi bir test SMTP sunucusu kullanın

### Dosya yüklenemiyor
- `backend/storage/` klasörünün yazılabilir olduğundan emin olun
- Dosya boyutu limitini kontrol edin (varsayılan: 20MB)

---

## 📞 Destek

Teknik sorular için:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Backend logları: Terminal çıktısını kontrol edin
- Frontend console: Tarayıcı Developer Tools (F12)

---

**İyi çalışmalar! 🚀**

