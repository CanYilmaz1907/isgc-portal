import React, { useMemo } from 'react'
import { DatePicker, Form, Input, InputNumber, Select } from 'antd'
import dayjs from 'dayjs'
import { useTranslation } from 'react-i18next'
import { translateNonconformityLabel, type NonconformityTableSchema } from './schema'

type Props = {
  schema: NonconformityTableSchema
  data: Record<string, any>
  onChange: (next: Record<string, any>) => void
}

export function DynamicNonconformityFields({ schema, data, onChange }: Props) {
  const { t } = useTranslation()
  const cols = useMemo(() => schema.columns ?? [], [schema])

  return (
    <>
      {cols.map((c) => {
        const translatedLabel = translateNonconformityLabel(c.key, c.label, t)
        const rules = c.required ? [{ required: true, message: t('common.required') }] : undefined
        const v = data[c.key]

        if (c.type === 'text') {
          return (
            <Form.Item key={c.key} label={translatedLabel} required={!!c.required} rules={rules}>
              <Input value={v ?? ''} onChange={(e) => onChange({ ...data, [c.key]: e.target.value })} />
            </Form.Item>
          )
        }
        if (c.type === 'number') {
          return (
            <Form.Item key={c.key} label={translatedLabel} required={!!c.required} rules={rules}>
              <InputNumber
                style={{ width: '100%' }}
                value={typeof v === 'number' ? v : undefined}
                onChange={(nv) => onChange({ ...data, [c.key]: nv })}
              />
            </Form.Item>
          )
        }
        if (c.type === 'select') {
          return (
            <Form.Item key={c.key} label={translatedLabel} required={!!c.required} rules={rules}>
              <Select
                value={v}
                options={(c.options ?? []).map((o) => ({ value: o, label: o }))}
                onChange={(nv) => onChange({ ...data, [c.key]: nv })}
              />
            </Form.Item>
          )
        }
        if (c.type === 'date') {
          return (
            <Form.Item key={c.key} label={translatedLabel} required={!!c.required} rules={rules}>
              <DatePicker
                style={{ width: '100%' }}
                value={v ? dayjs(v) : null}
                onChange={(d) => onChange({ ...data, [c.key]: d ? d.format('YYYY-MM-DD') : null })}
              />
            </Form.Item>
          )
        }
        return null
      })}
    </>
  )
}


