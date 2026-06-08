import React, { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Form, Input, InputNumber, Space, Switch, Table, Typography, message } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'

type Checklist = {
  id: string
  code: string
  title: string
  scope: string
  enabled: boolean
}

type Item = {
  id: string
  checklistId: string
  itemNo: number
  question: string
  weight: string
  maxScore: string
  enabled: boolean
}

export function ChecklistDetailPage() {
  const { id } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const { t } = useTranslation()
  const canEdit = canWrite(user)
  const [checklist, setChecklist] = useState<Checklist | null>(null)
  const [items, setItems] = useState<Item[]>([])
  const [loading, setLoading] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [form] = Form.useForm()

  async function refresh() {
    if (!id) return
    const [itemsResp, checklistsResp] = await Promise.all([
      http.get<Item[]>(`/api/checklists/${id}/items`),
      http.get<Checklist[]>('/api/checklists')
    ])
    setItems(itemsResp.data)
    const found = checklistsResp.data.find((c) => c.id === id)
    if (found) {
      setChecklist(found)
      form.setFieldsValue({
        code: found.code,
        title: found.title,
        scope: found.scope,
        enabled: found.enabled
      })
    }
  }

  useEffect(() => {
    let mounted = true
    ;(async () => {
      if (!id) return
      setLoading(true)
      try {
        const [itemsResp, checklistsResp] = await Promise.all([
          http.get<Item[]>(`/api/checklists/${id}/items`),
          http.get<Checklist[]>('/api/checklists')
        ])
        if (!mounted) return
        setItems(itemsResp.data)
        const found = checklistsResp.data.find((c) => c.id === id)
        if (found) {
          setChecklist(found)
          form.setFieldsValue({
            code: found.code,
            title: found.title,
            scope: found.scope,
            enabled: found.enabled
          })
        }
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [id, form])

  if (!checklist) {
    return (
      <div style={{ padding: 16 }}>
        <Card loading={loading}>{t('common.loading')}</Card>
      </div>
    )
  }

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('audits.checklistDetail')}
        </Typography.Title>
        <Space>
          {canEdit ? (
            <Button onClick={() => setEditMode((x) => !x)}>{editMode ? t('common.view') : t('common.edit')}</Button>
          ) : null}
          <Button onClick={() => nav('/checklists')}>{t('common.back')}</Button>
        </Space>
      </Space>

      <Card loading={loading}>
        {editMode ? (
          <Form
            form={form}
            layout="vertical"
            onFinish={async (values) => {
              if (!id) return
              setLoading(true)
              try {
                await http.put(`/api/checklists/${id}`, {
                  code: values.code,
                  title: values.title,
                  scope: values.scope || 'GENERAL',
                  enabled: values.enabled
                })
                message.success(t('audits.checklistUpdateSuccess'))
                await refresh()
                setEditMode(false)
              } finally {
                setLoading(false)
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
              <Button type="primary" htmlType="submit" loading={loading}>
                {t('common.save')}
              </Button>
              <Button
                onClick={() => {
                  setEditMode(false)
                  form.setFieldsValue({
                    code: checklist.code,
                    title: checklist.title,
                    scope: checklist.scope,
                    enabled: checklist.enabled
                  })
                }}
              >
                {t('common.cancel')}
              </Button>
            </Space>
          </Form>
        ) : (
          <>
            <Descriptions bordered size="small" column={1}>
              <Descriptions.Item label={t('audits.code')}>{checklist.code}</Descriptions.Item>
              <Descriptions.Item label={t('audits.checklistTitle')}>{checklist.title}</Descriptions.Item>
              <Descriptions.Item label={t('audits.scope')}>{checklist.scope}</Descriptions.Item>
              <Descriptions.Item label={t('common.enabled')}>{checklist.enabled ? t('common.yes') : t('common.no')}</Descriptions.Item>
            </Descriptions>

            <div style={{ marginTop: 16 }}>
              <Typography.Paragraph type="secondary" style={{ marginTop: 0 }}>
                {t('audits.addItemsNote')}
              </Typography.Paragraph>

              {canEdit ? (
                <Form
                  layout="inline"
                  onFinish={async (values) => {
                    if (!id) return
                    try {
                      await http.post(`/api/checklists/${id}/items`, {
                        checklistId: id,
                        itemNo: values.itemNo,
                        question: values.question,
                        weight: values.weight,
                        maxScore: values.maxScore,
                        enabled: true
                      })
                      message.success(t('audits.itemAdded'))
                      await refresh()
                    } catch {
                      message.error(t('audits.itemAddError'))
                    }
                  }}
                >
                  <Form.Item name="itemNo" rules={[{ required: true }]} style={{ width: 120 }}>
                    <InputNumber placeholder={t('audits.no')} min={1} />
                  </Form.Item>
                  <Form.Item name="question" rules={[{ required: true }]} style={{ width: 520 }}>
                    <Input placeholder={t('audits.question')} />
                  </Form.Item>
                  <Form.Item name="weight" initialValue={1} rules={[{ required: true }]} style={{ width: 120 }}>
                    <InputNumber placeholder={t('audits.weight')} min={0.01} step={0.25} />
                  </Form.Item>
                  <Form.Item name="maxScore" initialValue={1} rules={[{ required: true }]} style={{ width: 140 }}>
                    <InputNumber placeholder={t('audits.maxScore')} min={0} step={0.5} />
                  </Form.Item>
                  <Button type="primary" htmlType="submit">
                    {t('common.add')}
                  </Button>
                </Form>
              ) : null}

              <div style={{ marginTop: 12 }}>
                <Table
                  rowKey="id"
                  size="small"
                  dataSource={items}
                  pagination={false}
                  columns={[
                    { title: t('audits.no'), dataIndex: 'itemNo', key: 'itemNo' },
                    { title: t('audits.question'), dataIndex: 'question', key: 'question' },
                    { title: t('audits.weight'), dataIndex: 'weight', key: 'weight' },
                    { title: t('audits.maxScore'), dataIndex: 'maxScore', key: 'maxScore' }
                  ]}
                />
              </div>
            </div>
          </>
        )}
      </Card>
    </div>
  )
}


