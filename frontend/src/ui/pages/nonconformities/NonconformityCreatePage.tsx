import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, DatePicker, Divider, Form, Input, Select, Space, Typography, message, Alert } from 'antd'
import dayjs from 'dayjs'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { DynamicNonconformityFields } from './DynamicNonconformityFields'
import { parseNonconformitySchema, translateNonconformityLabel, type NonconformityTableSchema } from './schema'

type Template = { id: string; name: string; tableSchemaJson: string }
type Hazard = { id: string; name: string }
type Employee = { id: string; firstName: string; lastName: string }
type Project = { id: string; code: string; name: string; enabled: boolean }

export function NonconformityCreatePage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const [templates, setTemplates] = useState<Template[]>([])
  const [hazards, setHazards] = useState<Hazard[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [projects, setProjects] = useState<Project[]>([])

  const [templateId, setTemplateId] = useState<string | null>(null)
  const [schema, setSchema] = useState<NonconformityTableSchema>({ version: 1, columns: [] })
  const [data, setData] = useState<Record<string, any>>({})
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      try {
        const [t, h, e, p] = await Promise.all([
          http.get<Template[]>('/api/nonconformities/templates'),
          http.get<Hazard[]>('/api/nonconformities/hazard-classes'),
          http.get<any[]>('/api/employees'),
          http.get<Project[]>('/api/projects')
        ])
        if (!mounted) return
        setTemplates(t.data || [])
        setHazards(h.data || [])
        setEmployees((e.data || []).map((x) => ({ id: x.id, firstName: x.firstName, lastName: x.lastName })) as Employee[])
        setProjects(p.data || [])
      } catch (error: any) {
        console.error('Failed to load data:', error)
        if (error.response?.status === 403) {
          message.error(t('common.accessDenied'))
        } else {
          message.error(t('common.loadError'))
        }
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    const t = templates.find((x) => x.id === templateId)
    if (!t) {
      setSchema({ version: 1, columns: [] })
      setData({})
      return
    }
    setSchema(parseNonconformitySchema(t.tableSchemaJson))
    setData({})
  }, [templateId, templates])

  const employeeOptions = useMemo(
    () => employees.map((e) => ({ value: e.id, label: `${e.firstName} ${e.lastName}` })),
    [employees]
  )

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('nonconformities.newNonconformity')}
        </Typography.Title>
        <Button onClick={() => nav('/nonconformities')}>{t('common.back')}</Button>
      </Space>

      <Card>
        <Alert
          message={t('nonconformities.uploadAfterCreate')}
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
        <Form
          layout="vertical"
          onFinish={async (values) => {
            if (!templateId) return
            const missing = (schema.columns ?? [])
              .filter((c) => c.required)
              .filter((c) => {
                const v = data[c.key]
                return v === null || v === undefined || v === ''
              })
              .map((c) => translateNonconformityLabel(c.key, c.label, t))
            if (missing.length) {
              message.error(`${t('common.required')}: ${missing.join(', ')}`)
              return
            }

            setSaving(true)
            try {
              await http.post('/api/nonconformities', {
                projectId: values.projectId || null,
                templateId,
                hazardClassId: values.hazardClassId || null,
                responsibleEmployeeId: values.responsibleEmployeeId || null,
                title: values.title,
                description: values.description || null,
                dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : null,
                status: values.status,
                severity: values.severity || null,
                dataJson: JSON.stringify(data ?? {})
              })
              nav('/nonconformities', { replace: true })
            } finally {
              setSaving(false)
            }
          }}
          initialValues={{ status: 'OPEN', dueDate: dayjs().add(7, 'day') }}
        >
          <Form.Item label={t('nonconformities.template')} required>
            <Select value={templateId} onChange={setTemplateId} options={templates.map((t) => ({ value: t.id, label: t.name }))} />
          </Form.Item>

          <Form.Item label={t('accidents.project')} name="projectId">
            <Select
              allowClear
              placeholder={t('accidents.project')}
              options={projects.filter((pr) => pr.enabled).map((pr) => ({ value: pr.id, label: `${pr.name} (${pr.code})` }))}
            />
          </Form.Item>

          <Form.Item label={t('nonconformities.titleField')} name="title" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label={t('nonconformities.description')} name="description">
            <Input.TextArea rows={3} />
          </Form.Item>

          <Form.Item label={t('nonconformities.hazardClass')} name="hazardClassId">
            <Select options={hazards.map((h) => ({ value: h.id, label: h.name }))} />
          </Form.Item>

          <Form.Item label={t('nonconformities.responsiblePerson')} name="responsibleEmployeeId">
            <Select options={employeeOptions} />
          </Form.Item>

          <Form.Item label={t('nonconformities.dueDate')} name="dueDate">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label={t('common.status')} name="status" rules={[{ required: true }]}>
            <Select
              options={[
                { value: 'OPEN', label: t('status.OPEN') },
                { value: 'IN_PROGRESS', label: t('status.IN_PROGRESS') },
                { value: 'CLOSED', label: t('status.CLOSED') }
              ]}
            />
          </Form.Item>

          <Form.Item label={t('nonconformities.severity')} name="severity">
            <Input placeholder={t('common.optional')} />
          </Form.Item>

          <Divider>{t('nonconformities.dynamicFields')}</Divider>
          <DynamicNonconformityFields schema={schema} data={data} onChange={setData} />

          <Space style={{ marginTop: 12 }}>
            <Button type="primary" htmlType="submit" loading={saving} disabled={!templateId}>
              {t('common.save')}
            </Button>
            <Button onClick={() => nav('/nonconformities')}>{t('common.cancel')}</Button>
          </Space>
        </Form>
      </Card>
    </div>
  )
}


