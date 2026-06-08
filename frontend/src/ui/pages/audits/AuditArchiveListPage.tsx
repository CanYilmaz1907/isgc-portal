import React, { useEffect, useState } from 'react'
import { Button, Card, Space, Table, Tag, Typography } from 'antd'
import { AuditCompareModal } from './AuditCompareModal'
import { complianceTagColor, complianceZone } from './auditCompliance'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'

type Audit = {
  id: string
  projectName: string | null
  auditType: 'INTERNAL' | 'EXTERNAL'
  checklistTitle: string | null
  title: string
  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED'
  calculatedScore: string | null
  finishedAt: string | null
}

export function AuditArchiveListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const [rows, setRows] = useState<Audit[]>([])
  const [loading, setLoading] = useState(false)
  const [compareOpen, setCompareOpen] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<Audit[]>('/api/audits/archive')
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
          {t('audits.archiveTitle')}
        </Typography.Title>
        <Button type="primary" onClick={() => setCompareOpen(true)} disabled={rows.length < 2}>
          {t('audits.compare')}
        </Button>
      </Space>

      <Card>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={rows}
          pagination={{ pageSize: 10 }}
          columns={[
            { title: t('audits.checklistTitle'), dataIndex: 'title', key: 'title' },
            { title: t('accidents.project'), dataIndex: 'projectName', key: 'projectName' },
            { title: t('audits.type'), dataIndex: 'auditType', key: 'auditType' },
            { title: t('audits.checklists'), dataIndex: 'checklistTitle', key: 'checklistTitle' },
            {
              title: t('audits.score'),
              dataIndex: 'calculatedScore',
              key: 'score',
              render: (v: string | null) => {
                if (!v) return '-'
                const pct = Number(v)
                const zone = complianceZone(pct)
                return <Tag color={complianceTagColor(zone)}>{v}%</Tag>
              }
            },
            {
              title: t('common.status'),
              dataIndex: 'status',
              key: 'status',
              render: (v: Audit['status']) => <Tag color={v === 'COMPLETED' ? 'green' : 'red'}>{v}</Tag>
            }
          ]}
          onRow={(r) => ({ onClick: () => nav(`/audit-archive/${r.id}`) })}
        />
      </Card>

      <AuditCompareModal open={compareOpen} audits={rows} onClose={() => setCompareOpen(false)} />
    </div>
  )
}


