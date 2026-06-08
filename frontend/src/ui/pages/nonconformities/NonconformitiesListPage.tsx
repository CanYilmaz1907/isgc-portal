import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Select, Space, Table, Tag, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'
import { parseNonconformitySchema, translateNonconformityLabel, type NonconformityTableSchema } from './schema'

type Row = {
  id: string
  projectId: string | null
  projectName: string | null
  templateId: string
  templateName: string
  title: string
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED'
  dueDate: string | null
  hazardClassName: string | null
  responsibleEmployeeName: string | null
  dataJson: string
}

type Template = { id: string; name: string; tableSchemaJson: string }

export function NonconformitiesListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const [rows, setRows] = useState<Row[]>([])
  const [loading, setLoading] = useState(false)
  const [templates, setTemplates] = useState<Template[]>([])
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null)
  const [schema, setSchema] = useState<NonconformityTableSchema>({ version: 1, columns: [] })

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [resp, t] = await Promise.all([
          http.get<Row[]>('/api/nonconformities'),
          http.get<Template[]>('/api/nonconformities/templates')
        ])
        if (!mounted) return
        setRows(resp.data)
        setTemplates(t.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    const tid = selectedTemplateId ?? templates[0]?.id ?? null
    const t = templates.find((x) => x.id === tid)
    if (t?.tableSchemaJson) setSchema(parseNonconformitySchema(t.tableSchemaJson))
    else setSchema({ version: 1, columns: [] })
  }, [selectedTemplateId, templates])

  const filtered = useMemo(() => {
    if (!selectedTemplateId) return rows
    return rows.filter((r) => r.templateId === selectedTemplateId)
  }, [rows, selectedTemplateId])

  const parsedData = useMemo(() => {
    const map = new Map<string, Record<string, any>>()
    for (const r of filtered) {
      try {
        map.set(r.id, JSON.parse(r.dataJson || '{}'))
      } catch {
        map.set(r.id, {})
      }
    }
    return map
  }, [filtered])

  const columns = useMemo(
    () => [
      { title: t('nonconformities.titleField'), dataIndex: 'title', key: 'title' },
      { title: t('accidents.project'), dataIndex: 'projectName', key: 'projectName', render: (v: string | null) => v ?? '-' },
      { title: t('nonconformities.template'), dataIndex: 'templateName', key: 'templateName' },
      {
        title: t('common.status'),
        dataIndex: 'status',
        key: 'status',
        render: (v: Row['status']) => <Tag color={v === 'CLOSED' ? 'green' : v === 'IN_PROGRESS' ? 'gold' : 'red'}>{v}</Tag>
      },
      { title: t('nonconformities.dueDate'), dataIndex: 'dueDate', key: 'dueDate' },
      { title: t('nonconformities.hazardClass'), dataIndex: 'hazardClassName', key: 'hazard' },
      { title: t('nonconformities.responsible'), dataIndex: 'responsibleEmployeeName', key: 'resp' }
    ],
    [t]
  )

  const dynamicColumns = useMemo(() => {
    return (schema.columns ?? []).slice(0, 6).map((c) => ({
      title: translateNonconformityLabel(c.key, c.label, t),
      key: `dyn-${c.key}`,
      render: (_: any, r: Row) => {
        const obj = parsedData.get(r.id) ?? {}
        const v = obj[c.key]
        if (v === null || v === undefined) return ''
        if (typeof v === 'object') return JSON.stringify(v)
        return String(v)
      }
    }))
  }, [schema.columns, parsedData, t])

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('nonconformities.title')}
        </Typography.Title>
        <Space>
          <Select
            allowClear
            style={{ width: 280 }}
            placeholder={t('nonconformities.templateFilter')}
            value={selectedTemplateId}
            options={templates.map((t) => ({ value: t.id, label: t.name }))}
            onChange={(v) => setSelectedTemplateId(v ?? null)}
          />
          {canWrite(user) && (
            <Button type="primary" onClick={() => nav('/nonconformities/new')}>
              {t('nonconformities.newNonconformity')}
            </Button>
          )}
        </Space>
      </Space>

      <Card>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={filtered}
          columns={[...(columns as any), ...(dynamicColumns as any)]}
          pagination={{ pageSize: 10 }}
          onRow={(record) => ({
            onClick: () => nav(`/nonconformities/${record.id}`)
          })}
        />
      </Card>
    </div>
  )
}


