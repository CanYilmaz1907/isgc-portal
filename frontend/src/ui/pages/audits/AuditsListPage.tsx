import React, { useEffect, useState } from 'react'
import { Button, Card, Space, Table, Tag, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'

type Audit = {
  id: string
  projectName: string | null
  auditType: 'INTERNAL' | 'EXTERNAL'
  checklistTitle: string | null
  title: string
  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED'
  calculatedScore: string | null
}

export function AuditsListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const [rows, setRows] = useState<Audit[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<Audit[]>('/api/audits')
        if (mounted) setRows(resp.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('audits.title')}
        </Typography.Title>
        {canWrite(user) && (
          <Button type="primary" onClick={() => nav('/audits/new')}>
            {t('audits.newAudit')}
          </Button>
        )}
      </Space>

      <Card>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={rows}
          pagination={{ pageSize: 10 }}
          columns={[
            { title: t('audits.checklistTitle'), dataIndex: 'title', key: 'title' },
            { title: t('audits.type'), dataIndex: 'auditType', key: 'auditType' },
            { title: t('audits.checklists'), dataIndex: 'checklistTitle', key: 'checklistTitle' },
            {
              title: t('common.status'),
              dataIndex: 'status',
              key: 'status',
              render: (v: Audit['status']) => <Tag color={v === 'COMPLETED' ? 'green' : v === 'IN_PROGRESS' ? 'gold' : 'red'}>{v}</Tag>
            },
            { title: t('audits.score'), dataIndex: 'calculatedScore', key: 'score' }
          ]}
          onRow={(r) => ({ onClick: () => nav(`/audits/${r.id}`) })}
        />
      </Card>
    </div>
  )
}


