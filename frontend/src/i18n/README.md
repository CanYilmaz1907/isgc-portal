# 🌍 Çok Dilli Destek (i18n) Kullanım Kılavuzu

## Kurulum

Paketler `package.json`'a eklendi. Yüklemek için:

```bash
cd frontend
npm install
```

## Kullanım

### Component'lerde Kullanım

```tsx
import { useTranslation } from 'react-i18next'

function MyComponent() {
  const { t } = useTranslation()
  
  return (
    <div>
      <h1>{t('menu.dashboard')}</h1>
      <button>{t('common.save')}</button>
    </div>
  )
}
```

### Dil Değiştirme

Dil seçici zaten `AppLayout` ve `LoginPage`'e eklendi. Kullanıcı dil seçtiğinde otomatik olarak localStorage'a kaydedilir.

### Yeni Çeviri Ekleme

1. `frontend/src/i18n/locales/tr.json` dosyasına Türkçe çeviri ekleyin
2. `frontend/src/i18n/locales/en.json` dosyasına İngilizce çeviri ekleyin
3. `frontend/src/i18n/locales/ru.json` dosyasına Rusça çeviri ekleyin

Örnek:
```json
{
  "myModule": {
    "myKey": "Değer"
  }
}
```

Kullanım:
```tsx
{t('myModule.myKey')}
```

## Mevcut Çeviri Anahtarları

- `common.*` - Genel butonlar ve mesajlar
- `auth.*` - Giriş/çıkış
- `menu.*` - Menü öğeleri
- `dashboard.*` - Dashboard
- `accidents.*` - Kaza yönetimi
- `nonconformities.*` - Uygunsuzluk takibi
- `discipline.*` - Disiplin logu
- `audits.*` - Denetimler
- `documents.*` - Dokümanlar
- `roles.*` - Roller
- `status.*` - Durumlar

## Tüm Sayfaları Güncelleme

Tüm sayfalarda hardcoded string'leri `t()` fonksiyonu ile değiştirin:

**Önce:**
```tsx
<Button>Kaydet</Button>
```

**Sonra:**
```tsx
<Button>{t('common.save')}</Button>
```

## Notlar

- Varsayılan dil: Türkçe (tr)
- Dil tercihi localStorage'da saklanır
- Ant Design component'leri otomatik olarak i18n'i destekler (ConfigProvider ile)

