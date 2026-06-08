export type ColumnType = 'text' | 'number' | 'select' | 'date'

export type TableColumnDef = {
  key: string
  label: string
  type: ColumnType
  required?: boolean
  options?: string[]
}

export type NonconformityTableSchema = {
  version: number
  columns: TableColumnDef[]
}

export function parseNonconformitySchema(json: string): NonconformityTableSchema {
  const obj = JSON.parse(json) as NonconformityTableSchema
  if (!obj || !Array.isArray(obj.columns)) return { version: 1, columns: [] }
  return obj
}

/**
 * Translates a nonconformity field label based on its key or original label text.
 * This allows backend schema labels to be displayed in the user's selected language.
 */
export function translateNonconformityLabel(key: string, originalLabel: string, t: (key: string) => string): string {
  // First try to translate by key (preferred method)
  const keyTranslation = t(`nonconformities.field.${key}`)
  if (keyTranslation && keyTranslation !== `nonconformities.field.${key}`) {
    return keyTranslation
  }
  
  // Fallback: translate by common label text patterns
  const labelMap: Record<string, string> = {
    'Lokasyon': t('nonconformities.field.location'),
    'Kategori': t('nonconformities.field.category'),
    'Risk / Tehlike': t('nonconformities.field.risk'),
    'Risk': t('nonconformities.field.risk'),
    'Tehlike': t('nonconformities.field.risk'),
    'Aksiyon': t('nonconformities.field.action'),
    'Kanıt': t('nonconformities.field.evidence'),
  }
  
  return labelMap[originalLabel] || originalLabel
}


