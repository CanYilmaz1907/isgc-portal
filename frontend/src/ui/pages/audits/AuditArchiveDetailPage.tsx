import React, { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Divider, Space, Table, Tag, Typography, Upload, message } from 'antd'
import type { UploadRequestOption } from 'rc-upload/lib/interface'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite, canUpload } from '../../../state/auth/permissions'
import { AuditAnalysisPanel, type AuditAnalysis } from './AuditAnalysisPanel'
import { complianceTagColor } from './auditCompliance'

type Participant = { employeeId: string | null; employeeName: string | null; role: string | null }
type Audit = {
  id: string
  title: string
  projectName: string | null
  auditType: string
  checklistTitle: string | null
  calculatedScore: string | null
  finishedAt: string | null
  reportHtml: string | null
  participants: Participant[]
}

type FileObj = { id: string; originalFilename: string; sizeBytes: number; createdAt: string }

export function AuditArchiveDetailPage() {
  const { id } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const { t } = useTranslation()
  const canUploadFiles = canUpload(user)

  const [audit, setAudit] = useState<Audit | null>(null)
  const [analysis, setAnalysis] = useState<AuditAnalysis | null>(null)
  const [files, setFiles] = useState<FileObj[]>([])
  const [loading, setLoading] = useState(false)

  async function refreshFiles(auditId: string) {
    const f = await http.get<FileObj[]>(`/api/audits/${auditId}/files`)
    setFiles(f.data)
  }

  useEffect(() => {
    if (!id) return
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const a = await http.get<Audit>(`/api/audits/${id}`)
        if (!mounted) return
        setAudit(a.data)
        try {
          const an = await http.get<AuditAnalysis>(`/api/audits/${id}/analysis`)
          if (mounted) setAnalysis(an.data)
        } catch {
          if (mounted) setAnalysis(null)
        }
        await refreshFiles(id)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [id])

  const uploadRequest = async (opt: UploadRequestOption) => {
    if (!id) return
    const file = opt.file as File
    const form = new FormData()
    form.append('file', file)
    try {
      await http.post(`/api/audits/${id}/files`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success(t('accidents.uploadSuccess'))
      await refreshFiles(id)
      opt.onSuccess?.({}, new XMLHttpRequest())
    } catch (e) {
      message.error(t('accidents.uploadError'))
      opt.onError?.(e as any)
    }
  }

  async function download(fileId: string, name: string) {
    if (!id) return
    const resp = await http.get<ArrayBuffer>(`/api/audits/${id}/files/${fileId}/download`, { responseType: 'arraybuffer' })
    const blob = new Blob([resp.data])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = name
    a.click()
    window.URL.revokeObjectURL(url)
  }

  if (!audit) {
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
          {t('audits.archiveDetail')}
        </Typography.Title>
        <Button onClick={() => nav('/audit-archive')}>{t('common.back')}</Button>
      </Space>

      <Card loading={loading}>
        <Descriptions bordered size="small" column={1}>
          <Descriptions.Item label={t('audits.checklistTitle')}>{audit.title}</Descriptions.Item>
          <Descriptions.Item label={t('accidents.project')}>{audit.projectName ?? '-'}</Descriptions.Item>
          <Descriptions.Item label={t('audits.type')}>{audit.auditType}</Descriptions.Item>
          <Descriptions.Item label={t('audits.checklists')}>{audit.checklistTitle ?? '-'}</Descriptions.Item>
          <Descriptions.Item label={t('audits.score')}>
            {audit.calculatedScore ? (
              <Tag color={complianceTagColor((analysis?.overallColorZone ?? 'red') as any)}>
                {audit.calculatedScore}%
              </Tag>
            ) : '-'}
          </Descriptions.Item>
        </Descriptions>

        <Divider>{t('audits.participants')}</Divider>
        <Table
          rowKey={(r) => `${r.employeeId ?? 'x'}-${r.role ?? ''}`}
          size="small"
          pagination={false}
          dataSource={audit.participants ?? []}
          columns={[
            { title: t('audits.person'), dataIndex: 'employeeName', key: 'employeeName' },
            { title: t('common.role'), dataIndex: 'role', key: 'role' }
          ]}
        />

        <Divider>{t('audits.attachments')}</Divider>
        <Space direction="vertical" style={{ width: '100%' }}>
          {canUploadFiles ? (
            <Upload customRequest={uploadRequest} showUploadList={false}>
              <Button>{t('accidents.upload')}</Button>
            </Upload>
          ) : (
            <Typography.Text type="secondary">{t('accidents.noUploadPermission')}</Typography.Text>
          )}

          <Table
            rowKey="id"
            size="small"
            pagination={false}
            dataSource={files}
            columns={[
              { title: t('accidents.fileName'), dataIndex: 'originalFilename', key: 'name' },
              { title: t('accidents.fileSize'), dataIndex: 'sizeBytes', key: 'size' },
              {
                title: t('accidents.download'),
                key: 'dl',
                render: (_, r) => (
                  <a
                    onClick={(e) => {
                      e.preventDefault()
                      download(r.id, r.originalFilename)
                    }}
                  >
                    {t('accidents.download')}
                  </a>
                )
              }
            ]}
          />
        </Space>

        {analysis ? <AuditAnalysisPanel analysis={analysis} loading={loading} /> : null}

        {audit.reportHtml ? (
          <>
            <Divider>{t('audits.report')}</Divider>
            <div style={{ background: 'white', padding: 12, border: '1px solid #eee' }} dangerouslySetInnerHTML={{ __html: audit.reportHtml }} />
          </>
        ) : null}
      </Card>
    </div>
  )
}


