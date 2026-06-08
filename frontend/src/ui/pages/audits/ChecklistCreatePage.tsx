import React, { useState } from 'react'
import { Button, Card, Form, Input, Space, Switch, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'

export function ChecklistCreatePage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const [saving, setSaving] = useState(false)

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('audits.newChecklist')}
        </Typography.Title>
        <Button onClick={() => nav('/checklists')}>{t('common.back')}</Button>
      </Space>

      <Card>
        <Form
          layout="vertical"
          initialValues={{ enabled: true, scope: 'GENERAL' }}
          onFinish={async (values) => {
            setSaving(true)
            try {
              await http.post('/api/checklists', {
                code: values.code,
                title: values.title,
                scope: values.scope,
                enabled: values.enabled
              })
              nav('/checklists', { replace: true })
            } finally {
              setSaving(false)
            }
          }}
        >
          <Form.Item label={t('audits.code')} name="code" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item label={t('audits.checklistTitle')} name="title" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item label={t('audits.scope')} name="scope">
            <Input />
          </Form.Item>
          <Form.Item label={t('common.enabled')} name="enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saving}>
              {t('common.save')}
            </Button>
            <Button onClick={() => nav('/checklists')}>{t('common.cancel')}</Button>
          </Space>
        </Form>
      </Card>
    </div>
  )
}


