import React, { useMemo } from 'react'
import { Checkbox, Form, Input, InputNumber, Select } from 'antd'
import type { AccidentFormSchema } from './schema'

type Props = {
  schema: AccidentFormSchema
  formData: Record<string, any>
  onChange: (next: Record<string, any>) => void
}

export function DynamicAccidentForm({ schema, formData, onChange }: Props) {
  const visibleFields = useMemo(() => {
    return schema.fields.filter((f) => {
      if (!f.visibleWhen) return true
      const v = formData[f.visibleWhen.field]
      return v === f.visibleWhen.equals
    })
  }, [schema.fields, formData])

  return (
    <>
      {visibleFields.map((f) => {
        const rules = f.required ? [{ required: true, message: 'Zorunlu alan' }] : undefined
        const value = formData[f.key]

        if (f.type === 'text') {
          return (
            <Form.Item key={f.key} label={f.label} required={!!f.required} rules={rules}>
              <Input
                value={value ?? ''}
                onChange={(e) => onChange({ ...formData, [f.key]: e.target.value })}
              />
            </Form.Item>
          )
        }
        if (f.type === 'number') {
          return (
            <Form.Item key={f.key} label={f.label} required={!!f.required} rules={rules}>
              <InputNumber
                style={{ width: '100%' }}
                value={value}
                onChange={(v) => onChange({ ...formData, [f.key]: v })}
              />
            </Form.Item>
          )
        }
        if (f.type === 'boolean') {
          return (
            <Form.Item key={f.key} label={f.label} valuePropName="checked">
              <Checkbox
                checked={!!value}
                onChange={(e) => onChange({ ...formData, [f.key]: e.target.checked })}
              >
                {f.label}
              </Checkbox>
            </Form.Item>
          )
        }
        if (f.type === 'select') {
          return (
            <Form.Item key={f.key} label={f.label} required={!!f.required} rules={rules}>
              <Select
                value={value}
                options={(f.options ?? []).map((o) => ({ value: o, label: o }))}
                onChange={(v) => onChange({ ...formData, [f.key]: v })}
              />
            </Form.Item>
          )
        }
        if (f.type === 'multiSelect') {
          return (
            <Form.Item key={f.key} label={f.label} required={!!f.required} rules={rules}>
              <Select
                mode="multiple"
                value={Array.isArray(value) ? value : []}
                options={(f.options ?? []).map((o) => ({ value: o, label: o }))}
                onChange={(v) => onChange({ ...formData, [f.key]: v })}
              />
            </Form.Item>
          )
        }
        return null
      })}
    </>
  )
}


