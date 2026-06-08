import React, { useEffect, useState } from 'react'
import { Button, Card, Space, Table, Typography, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'

type Checklist = { id: string; code: string; title: string; scope: string; enabled: boolean }

export function ChecklistsListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const [rows, setRows] = useState<Checklist[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<Checklist[]>('/api/checklists')
        if (mounted) setRows(resp.data || [])
      } catch (error: any) {
        console.error('Failed to load checklists:', error)
        if (mounted) {
          if (error.response?.status === 403) {
            message.error(t('common.accessDenied'))
          } else {
            message.error(t('audits.loadError'))
          }
        }
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
          {t('audits.checklists')}
        </Typography.Title>
        {canWrite(user) && (
          <Button type="primary" onClick={() => nav('/checklists/new')}>
            {t('audits.newChecklist')}
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
            { title: t('audits.code'), dataIndex: 'code', key: 'code' },
            { title: t('audits.checklistTitle'), dataIndex: 'title', key: 'title' },
            { title: t('audits.scope'), dataIndex: 'scope', key: 'scope' },
            { title: t('common.enabled'), dataIndex: 'enabled', key: 'enabled', render: (v) => (v ? t('common.yes') : t('common.no')) }
          ]}
          onRow={(r) => ({ onClick: () => nav(`/checklists/${r.id}`) })}
        />
      </Card>
    </div>
  )
}


