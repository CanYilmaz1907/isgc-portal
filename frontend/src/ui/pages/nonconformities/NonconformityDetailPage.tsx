import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, DatePicker, Descriptions, Divider, Form, Input, Select, Space, Table, Typography, Upload, message } from 'antd'
import dayjs from 'dayjs'
import type { UploadRequestOption } from 'rc-upload/lib/interface'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite, canUpload } from '../../../state/auth/permissions'
import { parseNonconformitySchema, translateNonconformityLabel, type NonconformityTableSchema } from './schema'
import { DynamicNonconformityFields } from './DynamicNonconformityFields'

type Nonconf = {
  id: string
  projectId: string | null
  projectName: string | null
  title: string
  description: string | null
  status: string
  dueDate: string | null
  templateId: string
  templateName: string
  hazardClassId: string | null
  hazardClassName: string | null
  responsibleEmployeeId: string | null
  responsibleEmployeeName: string | null
  severity: string | null
  dataJson: string
}

type Template = { id: string; name: string; tableSchemaJson: string }
type Hazard = { id: string; name: string }
type Employee = { id: string; firstName: string; lastName: string }
type FileObj = { id: string; originalFilename: string; displayName: string | null; contentType?: string | null; sizeBytes: number }

export function NonconformityDetailPage() {
  const { id } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const { t } = useTranslation()
  const canEdit = canWrite(user)
  const canUploadFiles = canUpload(user)

  const [row, setRow] = useState<Nonconf | null>(null)
  const [schema, setSchema] = useState<NonconformityTableSchema>({ version: 1, columns: [] })
  const [files, setFiles] = useState<FileObj[]>([])
  const [loading, setLoading] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [templates, setTemplates] = useState<Template[]>([])
  const [hazards, setHazards] = useState<Hazard[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loadingData, setLoadingData] = useState(false)
  const [formData, setFormData] = useState<Record<string, any>>({})
  const [form] = Form.useForm()

  useEffect(() => {
    if (!id) return
    let mounted = true
    ;(async () => {
      setLoading(true)
      setLoadingData(true)
      try {
        const [r, templates, f, hazardsResp, employeesResp] = await Promise.all([
          http.get<Nonconf>(`/api/nonconformities/${id}`),
          http.get<any[]>(`/api/nonconformities/templates`),
          http.get<FileObj[]>(`/api/nonconformities/${id}/files`),
          http.get<Hazard[]>('/api/nonconformities/hazard-classes'),
          http.get<any[]>('/api/employees')
        ])
        if (!mounted) return
        setRow(r.data)
        setFiles(f.data)
        setTemplates(templates.data || [])
        setHazards(hazardsResp.data || [])
        setEmployees((employeesResp.data || []).map((x) => ({ id: x.id, firstName: x.firstName, lastName: x.lastName })) as Employee[])
        const t = (templates.data as Template[]).find((x) => x.id === r.data.templateId)
        if (t?.tableSchemaJson) {
          setSchema(parseNonconformitySchema(t.tableSchemaJson))
        }
        const parsedData = r.data.dataJson ? JSON.parse(r.data.dataJson) : {}
        setFormData(parsedData)
        form.setFieldsValue({
          title: r.data.title,
          description: r.data.description ?? '',
          hazardClassId: r.data.hazardClassId,
          responsibleEmployeeId: r.data.responsibleEmployeeId,
          dueDate: r.data.dueDate ? dayjs(r.data.dueDate) : null,
          status: r.data.status,
          severity: r.data.severity ?? ''
        })
      } finally {
        if (mounted) {
          setLoading(false)
          setLoadingData(false)
        }
      }
    })()
    return () => {
      mounted = false
    }
  }, [id, form])

  const data = useMemo(() => {
    if (!row?.dataJson) return {}
    try {
      return JSON.parse(row.dataJson) as Record<string, any>
    } catch {
      return {}
    }
  }, [row])

  const employeeOptions = useMemo(
    () => employees.map((e) => ({ value: e.id, label: `${e.firstName} ${e.lastName}` })),
    [employees]
  )

  const uploadRequest = async (opt: UploadRequestOption) => {
    if (!id) return
    const file = opt.file as File
    const form = new FormData()
    form.append('file', file)
    try {
      await http.post(`/api/nonconformities/${id}/files`, form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      message.success(t('nonconformities.uploadSuccess'))
      const refreshed = await http.get<FileObj[]>(`/api/nonconformities/${id}/files`)
      setFiles(refreshed.data)
      opt.onSuccess?.({}, new XMLHttpRequest())
    } catch (e) {
      message.error(t('nonconformities.uploadError'))
      opt.onError?.(e as any)
    }
  }

  async function downloadFile(fileId: string, fileName: string) {
    if (!id) return
    const resp = await http.get<ArrayBuffer>(`/api/nonconformities/${id}/files/${fileId}/download`, {
      responseType: 'arraybuffer'
    })
    const blob = new Blob([resp.data])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    window.URL.revokeObjectURL(url)
  }

  if (!row) {
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
          {t('nonconformities.nonconformityDetail')}
        </Typography.Title>
        <Space>
          {canEdit ? (
            <Button onClick={() => setEditMode((x) => !x)}>{editMode ? t('common.view') : t('common.edit')}</Button>
          ) : null}
          <Button onClick={() => nav('/nonconformities')}>{t('common.back')}</Button>
        </Space>
      </Space>

      <Card loading={loading}>
        {editMode ? (
          <Form
            form={form}
            layout="vertical"
            onFinish={async (values) => {
              if (!id || !row) return
              const missing = (schema.columns ?? [])
                .filter((c) => c.required)
                .filter((c) => {
                  const v = formData[c.key]
                  return v === null || v === undefined || v === ''
                })
                .map((c) => translateNonconformityLabel(c.key, c.label, t))
              if (missing.length) {
                message.error(`${t('common.required')}: ${missing.join(', ')}`)
                return
              }

              setLoading(true)
              try {
                await http.put(`/api/nonconformities/${id}`, {
                  projectId: row.projectId,
                  templateId: row.templateId,
                  hazardClassId: values.hazardClassId || null,
                  responsibleEmployeeId: values.responsibleEmployeeId || null,
                  title: values.title,
                  description: values.description || null,
                  dueDate: values.dueDate ? values.dueDate.format('YYYY-MM-DD') : null,
                  status: values.status,
                  severity: values.severity || null,
                  dataJson: JSON.stringify(formData ?? {})
                })
                message.success(t('nonconformities.updateSuccess'))
                const refreshed = await http.get<Nonconf>(`/api/nonconformities/${id}`)
                setRow(refreshed.data)
                const parsedData = refreshed.data.dataJson ? JSON.parse(refreshed.data.dataJson) : {}
                setFormData(parsedData)
                setEditMode(false)
              } finally {
                setLoading(false)
              }
            }}
          >
            <Form.Item label={t('nonconformities.template')}>
              <Input value={row.templateName} disabled />
            </Form.Item>

            <Form.Item label={t('nonconformities.titleField')} name="title" rules={[{ required: true }]}>
              <Input />
            </Form.Item>

            <Form.Item label={t('nonconformities.description')} name="description">
              <Input.TextArea rows={3} />
            </Form.Item>

            <Form.Item label={t('nonconformities.hazardClass')} name="hazardClassId">
              <Select
                options={hazards.map((h) => ({ value: h.id, label: h.name }))}
                loading={loadingData}
                allowClear
                notFoundContent={loadingData ? t('common.loading') : hazards.length === 0 ? t('common.noData') : t('common.noData')}
              />
            </Form.Item>

            <Form.Item label={t('nonconformities.responsiblePerson')} name="responsibleEmployeeId">
              <Select
                options={employeeOptions}
                loading={loadingData}
                allowClear
                notFoundContent={loadingData ? t('common.loading') : employeeOptions.length === 0 ? t('employees.notFound') : t('common.noData')}
              />
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
            <DynamicNonconformityFields schema={schema} data={formData} onChange={setFormData} />

            <Space style={{ marginTop: 12 }}>
              <Button type="primary" htmlType="submit" loading={loading}>
                {t('common.save')}
              </Button>
              <Button
                onClick={() => {
                  setEditMode(false)
                  form.setFieldsValue({
                    title: row.title,
                    description: row.description ?? '',
                    hazardClassId: row.hazardClassId,
                    responsibleEmployeeId: row.responsibleEmployeeId,
                    dueDate: row.dueDate ? dayjs(row.dueDate) : null,
                    status: row.status,
                    severity: row.severity ?? ''
                  })
                  const parsedData = row.dataJson ? JSON.parse(row.dataJson) : {}
                  setFormData(parsedData)
                }}
              >
                {t('common.cancel')}
              </Button>
            </Space>
          </Form>
        ) : (
          <>
            <Descriptions bordered size="small" column={1}>
              <Descriptions.Item label={t('nonconformities.titleField')}>{row.title}</Descriptions.Item>
              <Descriptions.Item label={t('accidents.project')}>{row.projectName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('nonconformities.template')}>{row.templateName}</Descriptions.Item>
              <Descriptions.Item label={t('common.status')}>{row.status}</Descriptions.Item>
              <Descriptions.Item label={t('nonconformities.dueDate')}>{row.dueDate ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('nonconformities.hazardClass')}>{row.hazardClassName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('nonconformities.responsiblePerson')}>{row.responsibleEmployeeName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label={t('nonconformities.description')}>{row.description ?? '-'}</Descriptions.Item>
            </Descriptions>

            <Divider>{t('nonconformities.dynamicFields')}</Divider>
            <Table
              size="small"
              rowKey="key"
              pagination={false}
              dataSource={(schema.columns ?? []).map((c) => ({ key: c.key, label: translateNonconformityLabel(c.key, c.label, t), value: data[c.key] }))}
              columns={[
                { title: t('nonconformities.fieldLabel'), dataIndex: 'label', key: 'label' },
                { title: t('nonconformities.fieldValue'), dataIndex: 'value', key: 'value', render: (v: any) => v ?? '-' }
              ]}
            />
          </>
        )}

        <Divider>{t('nonconformities.photosFiles')}</Divider>
        <Space direction="vertical" style={{ width: '100%' }}>
          {canUploadFiles ? (
            <Upload customRequest={uploadRequest} showUploadList={false}>
              <Button>{t('nonconformities.upload')}</Button>
            </Upload>
          ) : (
            <Typography.Text type="secondary">{t('nonconformities.noUploadPermission')}</Typography.Text>
          )}
          <Table
            rowKey="id"
            size="small"
            pagination={false}
            dataSource={files}
            columns={[
              {
                title: t('accidents.fileDisplayName'),
                key: 'displayName',
                render: (_, r: FileObj) => (
                  <Input
                    placeholder={t('accidents.fileDisplayNamePlaceholder')}
                    value={r.displayName ?? ''}
                    onChange={(e) => {
                      setFiles((prev) => prev.map((f) => (f.id === r.id ? { ...f, displayName: e.target.value } : f)))
                    }}
                    onBlur={(e) => {
                      const name = (e.target.value || '').trim() || null
                      if (!id) return
                      http.patch(`/api/nonconformities/${id}/files/${r.id}/display-name`, { displayName: name })
                        .then(() => setFiles((prev) => prev.map((f) => (f.id === r.id ? { ...f, displayName: name } : f))))
                        .catch(() => message.error(t('common.saveError')))
                    }}
                  />
                )
              },
              { title: t('nonconformities.fileName'), dataIndex: 'originalFilename', key: 'name' },
              { title: t('nonconformities.fileSize'), dataIndex: 'sizeBytes', key: 'size' },
              {
                title: t('nonconformities.download'),
                key: 'dl',
                render: (_, r: FileObj) => (
                  <a
                    onClick={(e) => {
                      e.preventDefault()
                      downloadFile(r.id, r.displayName || r.originalFilename)
                    }}
                  >
                    {t('nonconformities.download')}
                  </a>
                )
              }
            ]}
          />
        </Space>
      </Card>
    </div>
  )
}


