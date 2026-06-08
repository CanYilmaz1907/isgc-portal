import React, { useEffect, useState } from 'react'
import { Button, Card, List, Space, Typography, Upload, message } from 'antd'
import type { UploadRequestOption } from 'rc-upload/lib/interface'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'

type Version = { id: string; version: number; note: string | null }
type Doc = { id: string; code: string; title: string; description: string | null; enabled: boolean; versions: Version[] }

export function DocumentsPage() {
  const { user } = useAuth()
  const { t } = useTranslation()
  const isAdmin = user?.role === 'ADMIN'
  const [docs, setDocs] = useState<Doc[]>([])
  const [loading, setLoading] = useState(false)

  async function refresh() {
    setLoading(true)
    try {
      const resp = await http.get<Doc[]>('/api/documents')
      setDocs(resp.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refresh()
  }, [])

  async function downloadVersion(versionId: string, fileName: string) {
    const resp = await http.get<ArrayBuffer>(`/api/documents/versions/${versionId}/download`, {
      responseType: 'arraybuffer'
    })
    const blob = new Blob([resp.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    window.URL.revokeObjectURL(url)
  }

  const uploadNewDoc = async (opt: UploadRequestOption) => {
    if (!isAdmin) return
    const file = opt.file as File
    const form = new FormData()
    form.append('code', `DOC_${Date.now()}`)
    form.append('title', file.name.replace(/\.[^.]+$/, ''))
    form.append('description', '')
    form.append('note', 'İlk versiyon')
    form.append('file', file)
    try {
      await http.post('/api/documents', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success(t('documents.uploadSuccess'))
      await refresh()
      opt.onSuccess?.({}, new XMLHttpRequest())
    } catch (e) {
      message.error(t('documents.uploadError'))
      opt.onError?.(e as any)
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('documents.title')}
        </Typography.Title>
        {isAdmin ? (
          <Upload accept="application/pdf" showUploadList={false} customRequest={uploadNewDoc}>
            <Button type="primary">{t('documents.uploadDocument')}</Button>
          </Upload>
        ) : null}
      </Space>

      <Card loading={loading}>
        {docs.length === 0 ? (
          <Typography.Text type="secondary">{t('documents.noVersions')}</Typography.Text>
        ) : (
          <List
            dataSource={docs}
            renderItem={(d) => (
              <List.Item
                actions={[
                  ...(d.versions ?? []).slice(0, 3).map((v) => (
                    <a
                      key={v.id}
                      onClick={(e) => {
                        e.preventDefault()
                        downloadVersion(v.id, `${d.code}_v${v.version}.pdf`)
                      }}
                    >
                      {t('documents.downloadVersion')}{v.version}
                    </a>
                  ))
                ]}
              >
                <List.Item.Meta
                  title={`${d.title} (${d.code})`}
                  description={d.description || t('documents.description')}
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </div>
  )
}


