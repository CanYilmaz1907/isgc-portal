import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Space, Table, Tag, Typography, message } from 'antd'
import { useTranslation } from 'react-i18next'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'
import { classificationLabel, useAccidentMetadata } from './accidentMetadata'

type CauseSelection = { code: string; label: string }

type Accident = {
  id: string
  incidentNo: number | null
  projectId: string | null
  projectName: string | null
  accidentTypeId: string
  accidentTypeName: string
  occurredAt: string | null
  location: string | null
  classification: string | null
  accidentClass: 'NEAR_MISS' | 'MINOR' | 'MAJOR' | 'FATAL'
  potentialLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  status: 'OPEN' | 'INVESTIGATING' | 'CLOSED'
  area: string | null
  hazardSource: string | null
  injuredBodyPart: string | null
  injuryType: string | null
  employeeRegistrationNo: string | null
  workSupervisor: string | null
  timePeriod: string | null
  description: string | null
  directCauses: CauseSelection[]
  rootCauses: CauseSelection[]
}

export function AccidentsListPage() {
  const nav = useNavigate()
  const [searchParams] = useSearchParams()
  const { t } = useTranslation()
  const { user } = useAuth()
  const { metadata } = useAccidentMetadata()
  const [rows, setRows] = useState<Accident[]>([])
  const [loading, setLoading] = useState(false)
  const [exporting, setExporting] = useState(false)
  const classificationFilter = searchParams.get('classification')

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<Accident[]>('/api/accidents')
        if (mounted) setRows(resp.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  const filteredRows = useMemo(() => {
    if (!classificationFilter) return rows
    return rows.filter((r) => r.classification === classificationFilter)
  }, [rows, classificationFilter])

  const columns = useMemo(
    () => [
      { title: t('accidents.incidentNo'), dataIndex: 'incidentNo', key: 'no', width: 80 },
      { title: t('accidents.project'), dataIndex: 'projectName', key: 'projectName', render: (v: string | null) => v ?? '-' },
      {
        title: t('accidents.accidentClassification'),
        dataIndex: 'classification',
        key: 'classification',
        render: (v: string | null) => classificationLabel(metadata, v)
      },
      { title: t('accidents.area'), dataIndex: 'area', key: 'area', render: (v: string | null) => v ?? '-' },
      { title: t('accidents.hazardSource'), dataIndex: 'hazardSource', key: 'hazardSource', render: (v: string | null) => v ?? '-' },
      { title: t('accidents.dateTime'), dataIndex: 'occurredAt', key: 'occurredAt' },
      {
        title: t('accidents.status'),
        dataIndex: 'status',
        key: 'status',
        render: (v: Accident['status']) => <Tag>{v}</Tag>
      }
    ],
    [t, metadata]
  )

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('accidents.title')}
          {classificationFilter ? ` — ${classificationLabel(metadata, classificationFilter)}` : ''}
        </Typography.Title>
        <Space>
          <Button
            loading={exporting}
            onClick={async () => {
              setExporting(true)
              try {
                const resp = await http.get<ArrayBuffer>('/api/accidents/export', { responseType: 'arraybuffer' })
                const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
                const url = window.URL.createObjectURL(blob)
                const a = document.createElement('a')
                a.href = url
                a.download = 'kaza-listesi.xlsx'
                a.click()
                window.URL.revokeObjectURL(url)
                message.success(t('accidents.exportSuccess'))
              } catch (e: any) {
                message.error(e.response?.data?.message || t('accidents.errorOccurred'))
              } finally {
                setExporting(false)
              }
            }}
          >
            {t('accidents.exportListExcel')}
          </Button>
          {canWrite(user) && (
            <Button type="primary" onClick={() => nav('/accidents/new')}>
              {t('accidents.newAccident')}
            </Button>
          )}
        </Space>
      </Space>

      <Card>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={filteredRows}
          columns={columns as any}
          pagination={{ pageSize: 10 }}
          onRow={(record) => ({
            onClick: () => nav(`/accidents/${record.id}`)
          })}
        />
      </Card>
    </div>
  )
}
