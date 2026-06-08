import React, { useEffect, useMemo, useState } from 'react'
import { Badge, Button, Card, Space, Table, Tag, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'

type NcrRow = {
  id: string
  ncrNumber: string
  ncrDate: string
  projectName: string | null
  responsibleOrganization: string | null
  title: string | null
  status: string
  targetCompletionDate: string | null
  overdue: boolean
}

const STATUS_COLORS: Record<string, string> = {
  OPEN: 'red',
  CORRECTIVE_ACTION_PENDING: 'orange',
  VERIFICATION_PENDING: 'gold',
  CLOSED: 'green',
  REJECTED: 'purple'
}

export function NcrListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const [rows, setRows] = useState<NcrRow[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<NcrRow[]>('/api/ncr')
        if (mounted) setRows(resp.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [])

  const columns = useMemo(
    () => [
      { title: t('ncr.ncrNumber'), dataIndex: 'ncrNumber', key: 'ncrNumber' },
      { title: t('ncr.ncrDate'), dataIndex: 'ncrDate', key: 'ncrDate' },
      { title: t('ncr.responsibleOrganization'), dataIndex: 'responsibleOrganization', key: 'org', render: (v: string | null) => v ?? '-' },
      { title: t('accidents.project'), dataIndex: 'projectName', key: 'projectName', render: (v: string | null) => v ?? '-' },
      { title: t('ncr.title'), dataIndex: 'title', key: 'title', render: (v: string | null) => v ?? '-' },
      {
        title: t('common.status'),
        dataIndex: 'status',
        key: 'status',
        render: (v: string, r: NcrRow) => (
          <Space>
            <Tag color={STATUS_COLORS[v] ?? 'default'}>{t(`ncrStatus.${v}`, v)}</Tag>
            {r.overdue && <Badge status="error" text={t('ncr.overdue')} />}
          </Space>
        )
      },
      { title: t('ncr.targetCompletionDate'), dataIndex: 'targetCompletionDate', key: 'target', render: (v: string | null) => v ?? '-' }
    ],
    [t]
  )

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>{t('ncr.titlePage')}</Typography.Title>
        {canWrite(user) && <Button type="primary" onClick={() => nav('/ncr/new')}>{t('ncr.newNcr')}</Button>}
      </Space>
      <Card>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={rows}
          columns={columns}
          pagination={{ pageSize: 10 }}
          onRow={(record) => ({ onClick: () => nav(`/ncr/${record.id}`) })}
        />
      </Card>
    </div>
  )
}
