import React, { useEffect, useState } from 'react'
import { Button, Card, Form, Input, Select, Space, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'

type Checklist = { id: string; title: string }

export function AuditCreatePage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const [checklists, setChecklists] = useState<Checklist[]>([])
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      const resp = await http.get<any[]>('/api/checklists')
      if (!mounted) return
      setChecklists(resp.data.map((c) => ({ id: c.id, title: c.title })) as Checklist[])
    })()
    return () => {
      mounted = false
    }
  }, [])

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('audits.newAudit')}
        </Typography.Title>
        <Button onClick={() => nav('/audits')}>{t('common.back')}</Button>
      </Space>

      <Card>
        <Form
          layout="vertical"
          initialValues={{ auditType: 'INTERNAL' }}
          onFinish={async (values) => {
            setSaving(true)
            try {
              await http.post('/api/audits', {
                projectId: null,
                auditType: values.auditType,
                checklistId: values.checklistId || null,
                title: values.title,
                summary: values.summary || null,
                participants: []
              })
              nav('/audits', { replace: true })
            } finally {
              setSaving(false)
            }
          }}
        >
          <Form.Item label={t('audits.checklistTitle')} name="title" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item label={t('audits.auditType')} name="auditType" rules={[{ required: true }]}>
            <Select options={[{ value: 'INTERNAL', label: t('audits.internal') }, { value: 'EXTERNAL', label: t('audits.external') }]} />
          </Form.Item>
          <Form.Item label={t('audits.checklists')} name="checklistId">
            <Select allowClear options={checklists.map((c) => ({ value: c.id, label: c.title }))} />
          </Form.Item>
          <Form.Item label={t('audits.notes')} name="summary">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saving}>
              {t('common.save')}
            </Button>
            <Button onClick={() => nav('/audits')}>{t('common.cancel')}</Button>
          </Space>
        </Form>
      </Card>
    </div>
  )
}


