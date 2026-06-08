import React, { useEffect, useMemo, useState } from 'react'
import { Badge, Button, Card, DatePicker, Descriptions, Divider, Form, Input, InputNumber, Select, Space, Table, Tag, Typography, Upload, message } from 'antd'
import type { UploadRequestOption } from 'rc-upload/lib/interface'
import dayjs from 'dayjs'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite, canUpload } from '../../../state/auth/permissions'
import type { DisciplineCategory, DisciplineMetadata, DisciplineRow } from './disciplineTypes'

type FileObj = {
  id: string
  originalFilename: string
  contentType: string | null
  sizeBytes: number
  createdAt: string
}

const STATUS_COLORS: Record<string, string> = {
  SOZLU_UYARI: 'blue',
  UYARI: 'gold',
  IDARI_CEZA: 'orange',
  SOZLESME_FESHI: 'red'
}

export function DisciplineDetailPage() {
  const { id } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const { t } = useTranslation()
  const canEdit = canWrite(user)
  const canUploadFiles = canUpload(user)
  const [row, setRow] = useState<DisciplineRow | null>(null)
  const [metadata, setMetadata] = useState<DisciplineMetadata | null>(null)
  const [loading, setLoading] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState<DisciplineCategory | null>(null)
  const [files, setFiles] = useState<FileObj[]>([])
  const [form] = Form.useForm()

  useEffect(() => {
    if (!id) return
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [resp, metaResp, fResp] = await Promise.all([
          http.get<DisciplineRow>(`/api/discipline-logs/${id}`),
          http.get<DisciplineMetadata>('/api/discipline-logs/metadata'),
          http.get<FileObj[]>(`/api/discipline-logs/${id}/files`)
        ])
        if (!mounted) return
        setRow(resp.data)
        setMetadata(metaResp.data)
        setFiles(fResp.data || [])
        setSelectedCategory(resp.data.categoryLevel)
        form.setFieldsValue({
          occurredAt: resp.data.occurredAt ? dayjs(resp.data.occurredAt) : null,
          fullName: resp.data.fullName,
          employeeRegistrationNo: resp.data.employeeRegistrationNo,
          company: resp.data.company,
          jobTitle: resp.data.jobTitle,
          workArea: resp.data.workArea,
          categoryLevel: resp.data.categoryLevel,
          violationType: resp.data.violationType,
          violationDescription: resp.data.violationDescription,
          responsiblePerson: resp.data.responsiblePerson,
          status: resp.data.status,
          notes: resp.data.notes,
          penaltyAmount: resp.data.penaltyAmount
        })
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [id, form])

  const violationOptions = useMemo(() => {
    if (!metadata || !selectedCategory) return []
    return metadata.violationTypes[selectedCategory] ?? []
  }, [metadata, selectedCategory])

  const uploadRequest = async (opt: UploadRequestOption) => {
    if (!id) return
    const file = opt.file as File
    const formData = new FormData()
    formData.append('file', file)
    try {
      await http.post(`/api/discipline-logs/${id}/files`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success(t('discipline.uploadSuccess'))
      const refreshed = await http.get<FileObj[]>(`/api/discipline-logs/${id}/files`)
      setFiles(refreshed.data)
      opt.onSuccess?.({}, new XMLHttpRequest())
    } catch (e) {
      message.error(t('discipline.uploadError'))
      opt.onError?.(e as any)
    }
  }

  async function downloadFile(fileId: string, fileName: string) {
    if (!id) return
    try {
      const resp = await http.get<ArrayBuffer>(`/api/discipline-logs/${id}/files/${fileId}/download`, { responseType: 'arraybuffer' })
      const blob = new Blob([resp.data])
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName
      a.click()
      window.URL.revokeObjectURL(url)
    } catch {
      message.error(t('discipline.downloadError'))
    }
  }

  async function downloadPdf() {
    if (!id) return
    try {
      const resp = await http.get<ArrayBuffer>(`/api/discipline-logs/${id}/pdf`, { responseType: 'arraybuffer' })
      const blob = new Blob([resp.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `disiplin-logu-${id.substring(0, 8)}.pdf`
      a.click()
      window.URL.revokeObjectURL(url)
      message.success(t('discipline.pdfDownloaded'))
    } catch (e: any) {
      message.error(e.response?.data?.message || e.message || t('discipline.pdfError'))
    }
  }

  if (!row) {
    return <div style={{ padding: 16 }}><Card loading={loading}>{t('common.loading')}</Card></div>
  }

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('discipline.recordDetail')} #{row.sequenceNo ?? '-'}
        </Typography.Title>
        <Space>
          <Button onClick={downloadPdf}>{t('discipline.downloadPdf')}</Button>
          {canEdit && <Button onClick={() => setEditMode((x) => !x)}>{editMode ? t('common.view') : t('common.edit')}</Button>}
          <Button onClick={() => nav('/discipline')}>{t('common.back')}</Button>
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
                await http.put(`/api/discipline-logs/${id}`, {
                  projectId: row.projectId,
                  occurredAt: values.occurredAt.toISOString(),
                  fullName: values.fullName,
                  employeeRegistrationNo: values.employeeRegistrationNo || null,
                  company: values.company,
                  jobTitle: values.jobTitle,
                  workArea: values.workArea,
                  categoryLevel: values.categoryLevel,
                  violationType: values.violationType,
                  violationDescription: values.violationDescription,
                  responsiblePerson: values.responsiblePerson,
                  status: values.status,
                  notes: values.notes || null,
                  penaltyAmount: values.penaltyAmount ?? null,
                  profession: values.jobTitle || null,
                  violatingEmployeeId: row.violatingEmployeeId,
                  violatingManagerEmployeeId: row.violatingManagerEmployeeId
                })
                message.success(t('discipline.updateSuccess'))
                const refreshed = await http.get<DisciplineRow>(`/api/discipline-logs/${id}`)
                setRow(refreshed.data)
                setEditMode(false)
              } finally {
                setLoading(false)
              }
            }}
          >
            <Form.Item label={t('common.dateTime')} name="occurredAt" rules={[{ required: true }]}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label={t('discipline.fullName')} name="fullName" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item label={t('discipline.employeeId')} name="employeeRegistrationNo"><Input /></Form.Item>
            <Form.Item label={t('discipline.company')} name="company" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item label={t('discipline.jobTitle')} name="jobTitle" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item label={t('discipline.workArea')} name="workArea" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item label={t('common.category')} name="categoryLevel" rules={[{ required: true }]}>
              <Select
                options={metadata?.categories ?? []}
                onChange={(v: DisciplineCategory) => { setSelectedCategory(v); form.setFieldValue('violationType', undefined) }}
              />
            </Form.Item>
            <Form.Item label={t('discipline.violationType')} name="violationType" rules={[{ required: true }]}>
              <Select options={violationOptions} showSearch optionFilterProp="label" />
            </Form.Item>
            <Form.Item label={t('discipline.violationDescription')} name="violationDescription" rules={[{ required: true }]}>
              <Input.TextArea rows={4} />
            </Form.Item>
            <Form.Item label={t('discipline.responsiblePerson')} name="responsiblePerson" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item label={t('common.status')} name="status" rules={[{ required: true }]}>
              <Select options={metadata?.statuses ?? []} />
            </Form.Item>
            <Form.Item label={t('discipline.notes')} name="notes"><Input.TextArea rows={2} /></Form.Item>
            <Form.Item label={t('discipline.penaltyAmount')} name="penaltyAmount"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>{t('common.save')}</Button>
              <Button onClick={() => setEditMode(false)}>{t('common.cancel')}</Button>
            </Space>
          </Form>
        ) : (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label={t('discipline.sequenceNo')}>{row.sequenceNo ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.date')}>{row.occurredAt ? dayjs(row.occurredAt).format('DD.MM.YYYY HH:mm') : '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.fullName')}>{row.fullName ?? row.violatingEmployeeName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.employeeId')}>{row.employeeRegistrationNo ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.company')}>{row.company ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.jobTitle')}>{row.jobTitle ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.workArea')}>{row.workArea ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('common.category')}>{row.categoryLevel ? row.categoryLevel.replace('CAT_', 'Kategori ') : '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.violationType')} span={2}>{row.violationTypeLabel ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.violationDescription')} span={2}>{row.violationDescription ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.responsiblePerson')}>{row.responsiblePerson ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('common.status')}>
                <Tag color={STATUS_COLORS[row.status] ?? 'default'}>{t(`disciplineStatus.${row.status}`, row.status)}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label={t('discipline.repeatCount')}>
                {row.repeatThresholdReached ? <Badge count={row.repeatCount} color="red" /> : row.repeatCount}
              </Descriptions.Item>
              <Descriptions.Item label={t('discipline.penaltyAmount')}>{row.penaltyAmount ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('discipline.notes')} span={2}>{row.notes ?? '-'}</Descriptions.Item>
            </Descriptions>

            <Divider>{t('discipline.photosFiles')}</Divider>
            <Space direction="vertical" style={{ width: '100%' }}>
              {canUploadFiles ? (
                <Upload customRequest={uploadRequest} showUploadList={false}>
                  <Button>{t('discipline.upload')}</Button>
                </Upload>
              ) : (
                <Typography.Text type="secondary">{t('discipline.noUploadPermission')}</Typography.Text>
              )}
              <Table
                rowKey="id"
                size="small"
                pagination={false}
                dataSource={files}
                columns={[
                  { title: t('discipline.fileName'), dataIndex: 'originalFilename', key: 'name' },
                  { title: t('discipline.fileSize'), dataIndex: 'sizeBytes', key: 'size', render: (v: number) => `${(v / 1024).toFixed(2)} KB` },
                  { title: t('common.actions'), key: 'actions', render: (_: any, record: FileObj) => (
                    <Button type="link" onClick={() => downloadFile(record.id, record.originalFilename)}>{t('discipline.download')}</Button>
                  )}
                ]}
                locale={{ emptyText: t('common.noData') }}
              />
            </Space>
          </>
        )}
      </Card>
    </div>
  )
}
