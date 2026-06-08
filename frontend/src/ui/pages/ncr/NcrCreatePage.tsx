import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Checkbox, DatePicker, Form, Input, Select, Space, Switch, Typography, message } from 'antd'
import dayjs from 'dayjs'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'

type Project = { id: string; code: string; name: string; enabled: boolean }
type NcrMetadata = {
  rootCauseCategories: { value: string; label: string }[]
  isoStandards: { value: string; label: string }[]
  statuses: { value: string; label: string }[]
}

export function NcrCreatePage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const [projects, setProjects] = useState<Project[]>([])
  const [metadata, setMetadata] = useState<NcrMetadata | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      try {
        const [pResp, mResp] = await Promise.all([
          http.get<Project[]>('/api/projects'),
          http.get<NcrMetadata>('/api/ncr/metadata')
        ])
        if (!mounted) return
        setProjects(pResp.data || [])
        setMetadata(mResp.data)
      } catch {
        message.error(t('common.loadError'))
      }
    })()
    return () => { mounted = false }
  }, [t])

  const projectOptions = useMemo(
    () => projects.filter((p) => p.enabled).map((p) => ({ value: p.id, label: `${p.name} (${p.code})` })),
    [projects]
  )

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>{t('ncr.newNcr')}</Typography.Title>
        <Button onClick={() => nav('/ncr')}>{t('common.back')}</Button>
      </Space>

      <Card>
        <Form
          layout="vertical"
          initialValues={{ ncrDate: dayjs(), status: 'OPEN', followupRequired: false }}
          onFinish={async (values) => {
            setSaving(true)
            try {
              await http.post('/api/ncr', {
                ncrDate: values.ncrDate.format('YYYY-MM-DD'),
                projectId: values.projectId || null,
                responsibleOrganization: values.responsibleOrganization,
                location: values.location || null,
                title: values.title || null,
                description: values.description,
                evidenceReferences: values.evidenceReferences || null,
                proposedCorrectiveAction: values.proposedCorrectiveAction || null,
                executedCorrectiveAction: values.executedCorrectiveAction || null,
                targetCompletionDate: values.targetCompletionDate ? values.targetCompletionDate.format('YYYY-MM-DD') : null,
                completionDate: values.completionDate ? values.completionDate.format('YYYY-MM-DD') : null,
                rootCauseCategories: values.rootCauseCategories || [],
                status: values.status,
                initiatedBy: values.initiatedBy || null,
                approvedBy: values.approvedBy || null,
                verifiedBy: values.verifiedBy || null,
                verificationStatus: values.verificationStatus || null,
                isoStandards: values.isoStandards || [],
                followupRequired: !!values.followupRequired,
                notes: values.notes || null,
                responsibleEmployeeId: null,
                classification: null,
                rootCause: null,
                preventiveAction: values.preventiveAction || null
              })
              message.success(t('ncr.created'))
              nav('/ncr', { replace: true })
            } finally {
              setSaving(false)
            }
          }}
        >
          <Form.Item label={t('ncr.ncrDate')} name="ncrDate" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label={t('accidents.project')} name="projectId">
            <Select allowClear options={projectOptions} />
          </Form.Item>
          <Form.Item label={t('ncr.responsibleOrganization')} name="responsibleOrganization" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item label={t('ncr.location')} name="location"><Input /></Form.Item>
          <Form.Item label={t('ncr.title')} name="title"><Input /></Form.Item>
          <Form.Item label={t('ncr.nonconformanceDescription')} name="description" rules={[{ required: true }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item label={t('ncr.evidenceReferences')} name="evidenceReferences">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item label={t('ncr.rootCauseCategories')} name="rootCauseCategories">
            <Checkbox.Group options={metadata?.rootCauseCategories ?? []} style={{ display: 'flex', flexDirection: 'column', gap: 8 }} />
          </Form.Item>
          <Form.Item label={t('ncr.proposedCorrectiveAction')} name="proposedCorrectiveAction">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item label={t('ncr.targetCompletionDate')} name="targetCompletionDate">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label={t('ncr.isoStandards')} name="isoStandards">
            <Checkbox.Group options={metadata?.isoStandards ?? []} />
          </Form.Item>
          <Form.Item label={t('common.status')} name="status" rules={[{ required: true }]}>
            <Select options={metadata?.statuses ?? []} />
          </Form.Item>
          <Form.Item label={t('ncr.initiatedBy')} name="initiatedBy"><Input /></Form.Item>
          <Form.Item label={t('ncr.followupRequired')} name="followupRequired" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label={t('ncr.notes')} name="notes"><Input.TextArea rows={2} /></Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saving}>{t('common.save')}</Button>
            <Button onClick={() => nav('/ncr')}>{t('common.cancel')}</Button>
          </Space>
        </Form>
      </Card>
    </div>
  )
}
