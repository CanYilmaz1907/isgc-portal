import React, { useEffect, useMemo, useState } from 'react'
import { Modal, Select, Space, Table, Tag, Typography } from 'antd'
import ReactECharts from 'echarts-for-react'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { complianceTagColor, complianceZone } from './auditCompliance'

type AuditRow = { id: string; title: string; calculatedScore: string | null; finishedAt: string | null }

type CompareResult = {
  leftId: string
  leftTitle: string
  rightId: string
  rightTitle: string
  left: { overallScore: number; categories: { categoryNo: number; label: string; compliancePercent: number; colorZone: string }[] }
  right: { overallScore: number }
  categoryDeltas: { categoryNo: number; label: string; leftPercent: number; rightPercent: number; delta: number }[]
}

type Props = {
  open: boolean
  audits: AuditRow[]
  onClose: () => void
}

export function AuditCompareModal({ open, audits, onClose }: Props) {
  const { t } = useTranslation()
  const [leftId, setLeftId] = useState<string | null>(null)
  const [rightId, setRightId] = useState<string | null>(null)
  const [result, setResult] = useState<CompareResult | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!open) {
      setResult(null)
      setLeftId(null)
      setRightId(null)
    }
  }, [open])

  useEffect(() => {
    if (!leftId || !rightId || leftId === rightId) {
      setResult(null)
      return
    }
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<CompareResult>('/api/nonconformance-overview/audit-compare', {
          params: { leftId, rightId }
        })
        if (mounted) setResult(resp.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [leftId, rightId])

  const radarOpt = useMemo(() => {
    if (!result) return {}
    const labels = result.categoryDeltas.map((d) => String(d.categoryNo))
    return {
      tooltip: {},
      legend: { data: [result.leftTitle, result.rightTitle], bottom: 0 },
      radar: { indicator: labels.map((n) => ({ name: n, max: 100 })), radius: '58%' },
      series: [{
        type: 'radar',
        data: [
          { name: result.leftTitle, value: result.categoryDeltas.map((d) => d.leftPercent) },
          { name: result.rightTitle, value: result.categoryDeltas.map((d) => d.rightPercent) }
        ]
      }]
    }
  }, [result])

  const options = audits.map((a) => ({
    value: a.id,
    label: `${a.title} (${a.calculatedScore ?? '-'}%)`
  }))

  return (
    <Modal
      title={t('audits.compareTitle')}
      open={open}
      onCancel={onClose}
      footer={null}
      width={960}
    >
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          style={{ minWidth: 280 }}
          placeholder={t('audits.compareLeft')}
          options={options}
          value={leftId}
          onChange={setLeftId}
        />
        <Select
          style={{ minWidth: 280 }}
          placeholder={t('audits.compareRight')}
          options={options}
          value={rightId}
          onChange={setRightId}
        />
      </Space>

      {result ? (
        <>
          <Space style={{ marginBottom: 12 }}>
            <Tag color={complianceTagColor(complianceZone(Number(result.left.overallScore)))}>
              {result.leftTitle}: {result.left.overallScore}%
            </Tag>
            <Tag>{result.rightTitle}: {result.right.overallScore}%</Tag>
          </Space>
          <ReactECharts option={radarOpt} style={{ height: 400 }} showLoading={loading} />
          <Table
            size="small"
            rowKey="categoryNo"
            pagination={false}
            dataSource={result.categoryDeltas}
            columns={[
              { title: t('audits.no'), dataIndex: 'categoryNo', width: 70 },
              { title: t('audits.category'), dataIndex: 'label', ellipsis: true },
              { title: t('audits.compareLeft'), dataIndex: 'leftPercent', render: (v: number) => `${v}%` },
              { title: t('audits.compareRight'), dataIndex: 'rightPercent', render: (v: number) => `${v}%` },
              {
                title: t('audits.compareDelta'),
                dataIndex: 'delta',
                render: (v: number) => (
                  <Typography.Text type={v >= 0 ? 'success' : 'danger'}>
                    {v > 0 ? '+' : ''}{Number(v).toFixed(1)}%
                  </Typography.Text>
                )
              }
            ]}
          />
        </>
      ) : (
        <Typography.Text type="secondary">{t('audits.compareHint')}</Typography.Text>
      )}
    </Modal>
  )
}
