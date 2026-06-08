import React, { useEffect, useMemo, useState } from 'react'
import { App, Badge, Button, Card, Checkbox, DatePicker, Descriptions, Divider, Form, Input, Select, Space, Switch, Table, Tag, Typography, Upload } from 'antd'
import type { UploadRequestOption } from 'rc-upload/lib/interface'
import dayjs from 'dayjs'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { TFunction } from 'i18next'
import { getErrorMessage, http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite, canUpload } from '../../../state/auth/permissions'

type Project = { id: string; code: string; name: string; enabled: boolean }

type NcrMetadata = {
  rootCauseCategories: { value: string; label: string }[]
  isoStandards: { value: string; label: string }[]
  statuses: { value: string; label: string }[]
  verificationStatuses: { value: string; label: string }[]
}

type Ncr = {
  id: string
  ncrNumber: string
  ncrDate: string
  projectId: string | null
  projectName: string | null
  responsibleOrganization: string | null
  location: string | null
  title: string | null
  description: string | null
  evidenceReferences: string | null
  proposedCorrectiveAction: string | null
  executedCorrectiveAction: string | null
  targetCompletionDate: string | null
  completionDate: string | null
  rootCauseCategories: string[]
  status: string
  initiatedBy: string | null
  approvedBy: string | null
  verifiedBy: string | null
  verificationStatus: string | null
  isoStandards: string[]
  followupRequired: boolean
  notes: string | null
  preventiveAction: string | null
  overdue: boolean
}

type FileObj = {
  id: string
  originalFilename: string
  displayName: string | null
  contentType: string | null
  sizeBytes: number
  createdAt: string
}

const STATUS_COLORS: Record<string, string> = {
  OPEN: 'red',
  CORRECTIVE_ACTION_PENDING: 'orange',
  VERIFICATION_PENDING: 'gold',
  CLOSED: 'green',
  REJECTED: 'purple'
}

function labelFor(options: { value: string; label: string }[], value: string | null | undefined) {
  if (!value) return '-'
  return options.find((o) => o.value === value)?.label ?? value
}

function labelsFor(options: { value: string; label: string }[], values: string[] | null | undefined) {
  if (!values?.length) return '-'
  return values.map((v) => labelFor(options, v)).join(', ')
}

function openPrintForm(ncr: Ncr, metadata: NcrMetadata | null, t: TFunction) {
  const rc = metadata?.rootCauseCategories ?? []
  const iso = metadata?.isoStandards ?? []
  const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>${ncr.ncrNumber}</title>
<style>
body{font-family:Arial,sans-serif;padding:24px;color:#222}
h1{font-size:18px;margin-bottom:4px}
table{width:100%;border-collapse:collapse;margin-top:12px}
td,th{border:1px solid #ccc;padding:8px;vertical-align:top}
th{background:#f5f5f5;width:30%;text-align:left}
.section{margin-top:16px;font-weight:bold}
@media print{button{display:none}}
</style></head><body>
<h1>NCR - Sistemsel Uyumsuzluk Formu</h1>
<p><strong>${ncr.ncrNumber}</strong> | ${ncr.ncrDate}</p>
<table>
<tr><th>${t('ncr.responsibleOrganization')}</th><td>${ncr.responsibleOrganization ?? '-'}</td></tr>
<tr><th>${t('accidents.project')}</th><td>${ncr.projectName ?? '-'}</td></tr>
<tr><th>${t('ncr.location')}</th><td>${ncr.location ?? '-'}</td></tr>
<tr><th>${t('ncr.title')}</th><td>${ncr.title ?? '-'}</td></tr>
<tr><th>${t('common.status')}</th><td>${t(`ncrStatus.${ncr.status}`, ncr.status)}</td></tr>
<tr><th>${t('ncr.nonconformanceDescription')}</th><td>${(ncr.description ?? '-').replace(/\n/g, '<br>')}</td></tr>
<tr><th>${t('ncr.evidenceReferences')}</th><td>${(ncr.evidenceReferences ?? '-').replace(/\n/g, '<br>')}</td></tr>
<tr><th>${t('ncr.rootCauseCategories')}</th><td>${labelsFor(rc, ncr.rootCauseCategories)}</td></tr>
<tr><th>${t('ncr.proposedCorrectiveAction')}</th><td>${(ncr.proposedCorrectiveAction ?? '-').replace(/\n/g, '<br>')}</td></tr>
<tr><th>${t('ncr.executedCorrectiveAction')}</th><td>${(ncr.executedCorrectiveAction ?? '-').replace(/\n/g, '<br>')}</td></tr>
<tr><th>${t('ncr.targetCompletionDate')}</th><td>${ncr.targetCompletionDate ?? '-'}</td></tr>
<tr><th>${t('ncr.completionDate')}</th><td>${ncr.completionDate ?? '-'}</td></tr>
<tr><th>${t('ncr.isoStandards')}</th><td>${labelsFor(iso, ncr.isoStandards)}</td></tr>
<tr><th>${t('ncr.initiatedBy')}</th><td>${ncr.initiatedBy ?? '-'}</td></tr>
<tr><th>${t('ncr.approvedBy')}</th><td>${ncr.approvedBy ?? '-'}</td></tr>
<tr><th>${t('ncr.verifiedBy')}</th><td>${ncr.verifiedBy ?? '-'}</td></tr>
<tr><th>${t('ncr.verificationStatus')}</th><td>${ncr.verificationStatus ? t(`ncrVerificationStatus.${ncr.verificationStatus}`, ncr.verificationStatus) : '-'}</td></tr>
<tr><th>${t('ncr.followupRequired')}</th><td>${ncr.followupRequired ? t('common.yes') : t('common.no')}</td></tr>
<tr><th>${t('ncr.notes')}</th><td>${(ncr.notes ?? '-').replace(/\n/g, '<br>')}</td></tr>
</table>
<button onclick="window.print()">${t('ncr.print')}</button>
</body></html>`
  const w = window.open('', '_blank')
  if (!w) return
  w.document.write(html)
  w.document.close()
}

export function NcrDetailPage() {
  const { id } = useParams<{ id: string }>()
  const nav = useNavigate()
  const { t } = useTranslation()
  const { message: msg } = App.useApp()
  const { user } = useAuth()
  const canUploadPerm = canUpload(user)
  const canEdit = canWrite(user)

  const [ncr, setNcr] = useState<Ncr | null>(null)
  const [metadata, setMetadata] = useState<NcrMetadata | null>(null)
  const [files, setFiles] = useState<FileObj[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    if (!id) return
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [ncrResp, filesResp, metaResp] = await Promise.all([
          http.get<Ncr>(`/api/ncr/${id}`),
          http.get<FileObj[]>(`/api/ncr/${id}/files`).catch(() => ({ data: [] as FileObj[] })),
          http.get<NcrMetadata>('/api/ncr/metadata')
        ])
        if (mounted) {
          setNcr(ncrResp.data)
          setFiles(filesResp.data)
          setMetadata(metaResp.data)
        }
      } catch (e: any) {
        if (mounted && e.response?.status === 403) msg.error(t('common.accessDenied'))
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [id, t, msg])

  const projectOptions = useMemo(
    () => projects.filter((p) => p.enabled).map((p) => ({ value: p.id, label: `${p.name} (${p.code})` })),
    [projects]
  )

  async function downloadPdf() {
    const lang = 'tr'
    try {
      const resp = await http.get<ArrayBuffer>(`/api/ncr/${id}/pdf?lang=${lang}`, { responseType: 'arraybuffer' })
      const blob = new Blob([resp.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `ncr-${ncr?.ncrNumber?.replace(/[^a-zA-Z0-9.-]/g, '_') ?? id}.pdf`
      a.click()
      window.URL.revokeObjectURL(url)
      msg.success(t('ncr.pdfDownloaded'))
    } catch (e: any) {
      msg.error(getErrorMessage(e, t('common.loadError')))
    }
  }

  async function downloadFile(fileId: string, fileName: string) {
    if (!id) return
    try {
      const resp = await http.get<ArrayBuffer>(`/api/ncr/${id}/files/${fileId}/download`, { responseType: 'arraybuffer' })
      const blob = new Blob([resp.data])
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName || 'file'
      a.click()
      window.URL.revokeObjectURL(url)
    } catch (e: any) {
      msg.error(e.response?.data?.message || e.message || t('common.loadError'))
    }
  }

  const uploadRequest = async (opt: UploadRequestOption) => {
    if (!id) return
    const file = opt.file as File
    const formData = new FormData()
    formData.append('file', file)
    try {
      await http.post(`/api/ncr/${id}/files`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      msg.success(t('ncr.uploadSuccess'))
      const refreshed = await http.get<FileObj[]>(`/api/ncr/${id}/files`)
      setFiles(refreshed.data)
      opt.onSuccess?.({}, new XMLHttpRequest())
    } catch (e) {
      msg.error(t('ncr.uploadError'))
      opt.onError?.(e as any)
    }
  }

  async function startEdit() {
    if (!ncr) return
    try {
      const pResp = await http.get<Project[]>('/api/projects')
      setProjects(pResp.data || [])
      form.setFieldsValue({
        ncrDate: ncr.ncrDate ? dayjs(ncr.ncrDate) : null,
        projectId: ncr.projectId || undefined,
        responsibleOrganization: ncr.responsibleOrganization ?? undefined,
        location: ncr.location ?? undefined,
        title: ncr.title ?? undefined,
        description: ncr.description ?? undefined,
        evidenceReferences: ncr.evidenceReferences ?? undefined,
        proposedCorrectiveAction: ncr.proposedCorrectiveAction ?? undefined,
        executedCorrectiveAction: ncr.executedCorrectiveAction ?? undefined,
        targetCompletionDate: ncr.targetCompletionDate ? dayjs(ncr.targetCompletionDate) : null,
        completionDate: ncr.completionDate ? dayjs(ncr.completionDate) : null,
        rootCauseCategories: ncr.rootCauseCategories ?? [],
        status: ncr.status || 'OPEN',
        initiatedBy: ncr.initiatedBy ?? undefined,
        approvedBy: ncr.approvedBy ?? undefined,
        verifiedBy: ncr.verifiedBy ?? undefined,
        verificationStatus: ncr.verificationStatus ?? undefined,
        isoStandards: ncr.isoStandards ?? [],
        followupRequired: !!ncr.followupRequired,
        notes: ncr.notes ?? undefined,
        preventiveAction: ncr.preventiveAction ?? undefined
      })
      setEditMode(true)
    } catch (e: any) {
      if (e.response?.status === 403) msg.error(t('common.accessDenied'))
      else msg.error(t('common.loadError'))
    }
  }

  async function saveEdit(values: any) {
    if (!id || !ncr) return
    setSaving(true)
    try {
      await http.put(`/api/ncr/${id}`, {
        ncrDate: values.ncrDate ? values.ncrDate.format('YYYY-MM-DD') : ncr.ncrDate,
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
      msg.success(t('ncr.updateSuccess'))
      const resp = await http.get<Ncr>(`/api/ncr/${id}`)
      setNcr(resp.data)
      setEditMode(false)
    } catch (e: any) {
      msg.error(e.response?.data?.message || e.message || t('common.saveError'))
    } finally {
      setSaving(false)
    }
  }

  if (!ncr) return <Card loading={loading} />

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Space>
          <Typography.Title level={3} style={{ margin: 0 }}>{ncr.ncrNumber}</Typography.Title>
          {ncr.overdue && <Badge status="error" text={t('ncr.overdue')} />}
        </Space>
        <Space>
          {canEdit && !editMode && <Button type="primary" onClick={startEdit}>{t('common.edit')}</Button>}
          <Button onClick={() => ncr && openPrintForm(ncr, metadata, t)}>{t('ncr.printForm')}</Button>
          <Button onClick={downloadPdf}>{t('ncr.downloadPdf')}</Button>
          <Button onClick={() => nav('/ncr')}>{t('common.back')}</Button>
        </Space>
      </Space>

      <Card loading={loading}>
        {editMode ? (
          <>
            <Divider orientation="left">{t('common.edit')}</Divider>
            <Form form={form} layout="vertical" onFinish={saveEdit}>
              <Form.Item label={t('ncr.ncrDate')} name="ncrDate" rules={[{ required: true }]}>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item label={t('accidents.project')} name="projectId">
                <Select allowClear placeholder={t('accidents.project')} options={projectOptions} />
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
              <Form.Item label={t('ncr.executedCorrectiveAction')} name="executedCorrectiveAction">
                <Input.TextArea rows={3} />
              </Form.Item>
              <Form.Item label={t('ncr.targetCompletionDate')} name="targetCompletionDate">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item label={t('ncr.completionDate')} name="completionDate">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item label={t('ncr.isoStandards')} name="isoStandards">
                <Checkbox.Group options={metadata?.isoStandards ?? []} />
              </Form.Item>
              <Form.Item label={t('common.status')} name="status" rules={[{ required: true }]}>
                <Select options={metadata?.statuses ?? []} />
              </Form.Item>
              <Form.Item label={t('ncr.initiatedBy')} name="initiatedBy"><Input /></Form.Item>
              <Form.Item label={t('ncr.approvedBy')} name="approvedBy"><Input /></Form.Item>
              <Form.Item label={t('ncr.verifiedBy')} name="verifiedBy"><Input /></Form.Item>
              <Form.Item label={t('ncr.verificationStatus')} name="verificationStatus">
                <Select allowClear options={metadata?.verificationStatuses ?? []} />
              </Form.Item>
              <Form.Item label={t('ncr.followupRequired')} name="followupRequired" valuePropName="checked">
                <Switch />
              </Form.Item>
              <Form.Item label={t('ncr.notes')} name="notes"><Input.TextArea rows={2} /></Form.Item>
              <Form.Item label={t('ncr.preventiveAction')} name="preventiveAction">
                <Input.TextArea rows={2} />
              </Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={saving}>{t('common.save')}</Button>
                <Button onClick={() => setEditMode(false)}>{t('common.cancel')}</Button>
              </Space>
            </Form>
          </>
        ) : (
          <>
            <Descriptions bordered column={1}>
              <Descriptions.Item label={t('ncr.ncrNumber')}>{ncr.ncrNumber}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.ncrDate')}>{ncr.ncrDate}</Descriptions.Item>
              <Descriptions.Item label={t('accidents.project')}>{ncr.projectName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.responsibleOrganization')}>{ncr.responsibleOrganization ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.location')}>{ncr.location ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.title')}>{ncr.title ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('common.status')}>
                <Tag color={STATUS_COLORS[ncr.status] ?? 'default'}>{t(`ncrStatus.${ncr.status}`, ncr.status)}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label={t('ncr.nonconformanceDescription')}>{ncr.description ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.evidenceReferences')}>{ncr.evidenceReferences ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.rootCauseCategories')}>
                {labelsFor(metadata?.rootCauseCategories ?? [], ncr.rootCauseCategories)}
              </Descriptions.Item>
              <Descriptions.Item label={t('ncr.proposedCorrectiveAction')}>{ncr.proposedCorrectiveAction ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.executedCorrectiveAction')}>{ncr.executedCorrectiveAction ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.targetCompletionDate')}>{ncr.targetCompletionDate ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.completionDate')}>{ncr.completionDate ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.isoStandards')}>
                {labelsFor(metadata?.isoStandards ?? [], ncr.isoStandards)}
              </Descriptions.Item>
              <Descriptions.Item label={t('ncr.initiatedBy')}>{ncr.initiatedBy ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.approvedBy')}>{ncr.approvedBy ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.verifiedBy')}>{ncr.verifiedBy ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('ncr.verificationStatus')}>
                {ncr.verificationStatus ? t(`ncrVerificationStatus.${ncr.verificationStatus}`, ncr.verificationStatus) : '-'}
              </Descriptions.Item>
              <Descriptions.Item label={t('ncr.followupRequired')}>
                {ncr.followupRequired ? t('common.yes') : t('common.no')}
              </Descriptions.Item>
              <Descriptions.Item label={t('ncr.notes')}>{ncr.notes ?? '-'}</Descriptions.Item>
              {ncr.preventiveAction && (
                <Descriptions.Item label={t('ncr.preventiveAction')}>{ncr.preventiveAction}</Descriptions.Item>
              )}
            </Descriptions>

            <Typography.Text strong style={{ display: 'block', marginTop: 16 }}>{t('ncr.photosFiles')}</Typography.Text>
            {canUploadPerm ? (
              <Upload customRequest={uploadRequest} showUploadList={false} style={{ marginTop: 8 }}>
                <Button type="primary" style={{ marginBottom: 12, marginTop: 8 }}>{t('ncr.upload')}</Button>
              </Upload>
            ) : (
              <Typography.Text type="secondary" style={{ display: 'block', marginTop: 8 }}>{t('ncr.noUploadPermission')}</Typography.Text>
            )}
            <Table
              rowKey="id"
              size="small"
              dataSource={files}
              pagination={false}
              columns={[
                {
                  title: t('ncr.fileDisplayName'),
                  key: 'displayName',
                  render: (_: unknown, r: FileObj) => (
                    <Input
                      placeholder={t('ncr.fileDisplayNamePlaceholder')}
                      value={r.displayName ?? ''}
                      onChange={(e) => {
                        setFiles((prev) => prev.map((f) => (f.id === r.id ? { ...f, displayName: e.target.value } : f)))
                      }}
                      onBlur={(e) => {
                        const name = (e.target.value || '').trim() || null
                        if (!id) return
                        http.patch(`/api/ncr/${id}/files/${r.id}/display-name`, { displayName: name })
                          .then(() => setFiles((prev) => prev.map((f) => (f.id === r.id ? { ...f, displayName: name } : f))))
                          .catch(() => msg.error(t('common.saveError')))
                      }}
                    />
                  )
                },
                { title: t('ncr.fileName'), dataIndex: 'originalFilename', key: 'name' },
                { title: t('ncr.fileSize'), dataIndex: 'sizeBytes', key: 'size' },
                {
                  title: t('ncr.download'),
                  key: 'dl',
                  render: (_: unknown, r: FileObj) => (
                    <a onClick={(e) => { e.preventDefault(); downloadFile(r.id, r.displayName || r.originalFilename) }}>
                      {t('ncr.download')}
                    </a>
                  )
                }
              ]}
            />
          </>
        )}
      </Card>
    </div>
  )
}
