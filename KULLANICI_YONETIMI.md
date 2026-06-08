# 👥 Kullanıcı Yönetimi Kılavuzu

## 🔐 403 Hatası Çözümü

Eğer `/api/users` endpoint'ine erişirken **403 Forbidden** hatası alıyorsanız:

### 1. ADMIN ile Giriş Yapın

**ÖNEMLİ**: Kullanıcı yönetimi sayfasına sadece **ADMIN** rolüne sahip kullanıcılar erişebilir.

1. Çıkış yapın (sağ üstteki "Çıkış" butonu)
2. ADMIN kullanıcısı ile giriş yapın:
   - **Username**: `irina`
   - **Password**: `irina123`
   - Veya
   - **Username**: `samet`
   - **Password**: `samet123`

### 2. Token Kontrolü

Tarayıcı Console'da (F12) şunu çalıştırın:

```javascript
// Mevcut token ve kullanıcı bilgilerini kontrol edin
const auth = JSON.parse(localStorage.getItem('auth') || '{}')
console.log('User:', auth.username)
console.log('Role:', auth.role)
console.log('Token:', auth.accessToken ? 'Var' : 'Yok')
```

Eğer `role` **ADMIN** değilse, çıkış yapıp tekrar ADMIN ile giriş yapın.

### 3. Debug Endpoint'leri

Backend'de debug endpoint'leri eklendi:

**Mevcut Kullanıcı Bilgisi:**
```bash
GET http://localhost:8080/api/debug/me
Authorization: Bearer YOUR_TOKEN
```

**ADMIN Yetkisi Testi:**
```bash
GET http://localhost:8080/api/debug/admin-test
Authorization: Bearer YOUR_TOKEN
```

### 4. Sayfayı Yenileyin

Bazen token cache'lenmiş olabilir. Sayfayı yenileyin (F5) veya tarayıcıyı kapatıp açın.

---

## 📝 Kullanıcı Oluşturma

### Adım 1: ADMIN ile Giriş

1. `irina` veya `samet` ile giriş yapın
2. Sol menüden **"Kullanıcı Yönetimi"** seçeneğini tıklayın

### Adım 2: Yeni Kullanıcı Oluştur

1. **"Yeni Kullanıcı"** butonuna tıklayın
2. Formu doldurun:
   - **Kullanıcı Adı**: Benzersiz bir kullanıcı adı (örn: `isgc_user`)
   - **E-posta**: Geçerli bir e-posta adresi (örn: `isgc@company.com`)
   - **Şifre**: En az 6 karakter (örn: `isgc123`)
   - **Rol**: Dropdown'dan seçin
     - `ADMIN` - Tam yetki
     - `ISG_C` - ISG-C personeli (veri girişi)
     - `YONETICI` - Yönetici (proje bazlı erişim)
     - `PERSONEL` - Personel (sadece kendi kayıtları)
   - **Durum**: Aktif/Pasif (varsayılan: Aktif)
3. **"Kaydet"** butonuna tıklayın

### Adım 3: Yeni Kullanıcı ile Giriş

1. Çıkış yapın
2. Oluşturduğunuz kullanıcı bilgileriyle giriş yapın
3. Rolüne göre yetkili sayfalara erişebilirsiniz

---

## 🔄 Kullanıcı Düzenleme

1. Kullanıcı listesinde **"Düzenle"** butonuna tıklayın
2. Bilgileri güncelleyin:
   - Kullanıcı adı, e-posta, rol değiştirilebilir
   - **Şifre**: Değiştirmek için yeni şifre girin (boş bırakırsanız değişmez)
   - **Durum**: Aktif/Pasif
3. **"Kaydet"** butonuna tıklayın

---

## 🗑️ Kullanıcı Silme

1. Kullanıcı listesinde **"Sil"** butonuna tıklayın
2. Onay mesajında **"Evet"** seçin
3. Kullanıcı kalıcı olarak silinir

**UYARI**: Silinen kullanıcı geri alınamaz!

---

## 📋 Rol Yetkileri

### ADMIN
- ✅ Tüm modüllerde tam yetki
- ✅ Kullanıcı yönetimi (ekleme, düzenleme, silme)
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

## 🐛 Sorun Giderme

### 403 Forbidden Hatası

**Neden**: Kullanıcı ADMIN rolüne sahip değil veya token geçersiz.

**Çözüm**:
1. Çıkış yapın
2. `irina` veya `samet` ile tekrar giriş yapın
3. Sayfayı yenileyin (F5)

### Kullanıcı Oluşturulamıyor

**Neden**: Kullanıcı adı veya e-posta zaten kullanılıyor.

**Çözüm**: Farklı bir kullanıcı adı veya e-posta kullanın.

### Şifre Değiştirilemiyor

**Neden**: Şifre alanı boş bırakılmış.

**Çözüm**: Şifre değiştirmek için yeni şifre girin (en az 6 karakter).

---

## 💡 İpuçları

1. **İlk Kurulum**: İlk çalıştırmada `irina` ve `samet` kullanıcıları otomatik oluşturulur
2. **Şifre Güvenliği**: Production'da güçlü şifreler kullanın
3. **Rol Seçimi**: Kullanıcıya uygun rolü seçin - rol değiştirilebilir
4. **Durum**: Pasif kullanıcılar giriş yapamaz ama verileri korunur

---

**İyi çalışmalar! 🚀**

