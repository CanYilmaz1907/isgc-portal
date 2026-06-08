import React, { useEffect, useMemo, useRef, useState } from 'react'
import { Badge, Button, Card, Input, Space, Table, Tag, Typography, Upload, message } from 'antd'
import { SearchOutlined, UploadOutlined, DownloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import dayjs from 'dayjs'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'
import type { DisciplineRow } from './disciplineTypes'

const STATUS_COLORS: Record<string, string> = {
  SOZLU_UYARI: 'blue',
  UYARI: 'gold',
  IDARI_CEZA: 'orange',
  SOZLESME_FESHI: 'red'
}

export function DisciplineListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const canEdit = canWrite(user)
  const [rows, setRows] = useState<DisciplineRow[]>([])
  const [loading, setLoading] = useState(false)
  const [searchText, setSearchText] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  const load = async () => {
    setLoading(true)
    try {
      const resp = await http.get<DisciplineRow[]>('/api/discipline-logs')
      setRows(resp.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const filteredRows = useMemo(() => {
    if (!searchText.trim()) return rows
    const s = searchText.toLowerCase().trim()
    return rows.filter((row) =>
      [
        row.sequenceNo,
        row.fullName,
        row.employeeRegistrationNo,
        row.company,
        row.workArea,
        row.violationTypeLabel,
        row.responsiblePerson,
        row.status
      ].some((v) => String(v ?? '').toLowerCase().includes(s))
    )
  }, [rows, searchText])

  const columns = useMemo(
    () => [
      { title: t('discipline.sequenceNo'), dataIndex: 'sequenceNo', key: 'seq', width: 70 },
      {
        title: t('discipline.date'),
        dataIndex: 'occurredAt',
        key: 'occurredAt',
        render: (v: string) => (v ? dayjs(v).format('DD.MM.YYYY') : '-')
      },
      { title: t('discipline.fullName'), dataIndex: 'fullName', key: 'fullName' },
      { title: t('discipline.company'), dataIndex: 'company', key: 'company' },
      {
        title: t('common.category'),
        dataIndex: 'categoryLevel',
        key: 'categoryLevel',
        render: (v: string) => (v ? v.replace('CAT_', 'Kategori ') : '-')
      },
      {
        title: t('discipline.violationType'),
        dataIndex: 'violationTypeLabel',
        key: 'violationTypeLabel',
        ellipsis: true
      },
      {
        title: t('discipline.repeatCount'),
        dataIndex: 'repeatCount',
        key: 'repeatCount',
        render: (v: number, r: DisciplineRow) =>
          r.repeatThresholdReached ? <Badge count={v} color="red" /> : v
      },
      {
        title: t('common.status'),
        dataIndex: 'status',
        key: 'status',
        render: (v: string) => <Tag color={STATUS_COLORS[v] ?? 'default'}>{t(`disciplineStatus.${v}`, v)}</Tag>
      }
    ],
    [t]
  )

  const handleExport = async () => {
    try {
      const resp = await http.get<ArrayBuffer>('/api/discipline-logs/export', { responseType: 'arraybuffer' })
      const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'disiplin-logu.xlsx'
      a.click()
      window.URL.revokeObjectURL(url)
    } catch {
      message.error(t('common.loadError'))
    }
  }

  const handleImport = async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    try {
      const resp = await http.post<{ imported: number; errors: string[] }>('/api/discipline-logs/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      message.success(`${resp.data.imported} ${t('discipline.imported')}`)
      if (resp.data.errors?.length) {
        message.warning(resp.data.errors.slice(0, 3).join('; '))
      }
      await load()
    } catch {
      message.error(t('discipline.importError'))
    }
    return false
  }

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>{t('discipline.title')}</Typography.Title>
        <Space>
          <Button icon={<DownloadOutlined />} onClick={handleExport}>{t('common.export')}</Button>
          {canEdit && (
            <Upload beforeUpload={handleImport} showUploadList={false} accept=".xlsx,.xls">
              <Button icon={<UploadOutlined />}>{t('discipline.importExcel')}</Button>
            </Upload>
          )}
          <Button onClick={() => nav('/discipline/stats')}>{t('discipline.chartsTitle')}</Button>
          {canEdit && (
            <Button type="primary" onClick={() => nav('/discipline/new')}>{t('discipline.newRecord')}</Button>
          )}
        </Space>
      </Space>

      <Card>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Input
            placeholder={t('discipline.searchPlaceholder')}
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
            style={{ maxWidth: 400 }}
          />
          <Table
            rowKey="id"
            loading={loading}
            dataSource={filteredRows}
            columns={columns as any}
            pagination={{ pageSize: 15 }}
            onRow={(record) => ({ onClick: () => nav(`/discipline/${record.id}`) })}
          />
        </Space>
      </Card>
    </div>
  )
}
