export type FieldType = 'text' | 'number' | 'boolean' | 'select' | 'multiSelect'

export type VisibleWhen = {
  field: string
  equals: string | number | boolean
}

export type FormField = {
  key: string
  label: string
  type: FieldType
  required?: boolean
  options?: string[]
  visibleWhen?: VisibleWhen
}

export type AccidentFormSchema = {
  version: number
  title?: string
  fields: FormField[]
}

export function parseSchema(json: string): AccidentFormSchema {
  const obj = JSON.parse(json) as AccidentFormSchema
  if (!obj || !Array.isArray(obj.fields)) {
    return { version: 1, fields: [] }
  }
  return obj
}


