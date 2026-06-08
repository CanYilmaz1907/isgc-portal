import React, { useEffect, useMemo, useState } from 'react'
import { Card, Col, Row, Space, Statistic, Table, Tag, Typography } from 'antd'
import ReactECharts from 'echarts-for-react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { http } from '../../../api/http'
import { complianceColor, complianceTagColor, complianceZone } from '../audits/auditCompliance'

type Overview = {
  moduleTotals: Record<string, number>
  openByModule: Record<string, number>
  overdueNcr: number
  avgAuditCompliance: number
  ncrByStatus: Record<string, number>
  monthlyTrend: Record<string, Record<string, number>>
  recentAuditScores: {
    id: string
    title: string
    projectName: string | null
    score: number
    finishedAt: string | null
  }[]
}

const MODULE_LABELS: Record<string, string> = {
  NCR: 'NCR',
  DISCIPLINE: 'Disiplin',
  LEGACY_NC: 'Uygunsuzluk (Eski)',
  AUDITS: 'Denetim'
}

export function NonconformanceOverviewPage() {
  const { t } = useTranslation()
  const nav = useNavigate()
  const [data, setData] = useState<Overview | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const resp = await http.get<Overview>('/api/nonconformance-overview')
        if (mounted) setData(resp.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [])

  const modulePie = useMemo(() => {
    const entries = Object.entries(data?.moduleTotals ?? {})
    return {
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        data: entries.map(([k, v]) => ({ name: t(`overview.module.${k}`, MODULE_LABELS[k] ?? k), value: v }))
      }]
    }
  }, [data, t])

  const trendOpt = useMemo(() => {
    const trend = data?.monthlyTrend ?? {}
    const months = Object.values(trend)[0] ? Object.keys(Object.values(trend)[0]) : []
    const series = Object.entries(trend).map(([mod, counts]) => ({
      name: t(`overview.module.${mod}`, MODULE_LABELS[mod] ?? mod),
      type: 'line',
      smooth: true,
      data: months.map((m) => counts[m] ?? 0)
    }))
    return {
      tooltip: { trigger: 'axis' },
      legend: { top: 'bottom' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series
    }
  }, [data, t])

  const ncrStatusOpt = useMemo(() => {
    const entries = Object.entries(data?.ncrByStatus ?? {})
    return {
      tooltip: {},
      xAxis: { type: 'category', data: entries.map(([k]) => t(`ncrStatus.${k}`, k)), axisLabel: { rotate: 25 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v), itemStyle: { color: '#1677ff' } }]
    }
  }, [data, t])

  const openBar = useMemo(() => {
    const entries = Object.entries(data?.openByModule ?? {})
    return {
      tooltip: {},
      xAxis: { type: 'category', data: entries.map(([k]) => t(`overview.module.${k}`, MODULE_LABELS[k] ?? k)) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v), itemStyle: { color: '#fa8c16' } }]
    }
  }, [data, t])

  return (
    <div style={{ padding: 16 }}>
      <Typography.Title level={3} style={{ marginTop: 0 }}>{t('overview.title')}</Typography.Title>
      <Typography.Paragraph type="secondary">{t('overview.subtitle')}</Typography.Paragraph>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={6}>
          <Card loading={loading}>
            <Statistic title={t('overview.openNcr')} value={data?.openByModule?.NCR ?? 0} valueStyle={{ color: '#fa8c16' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card loading={loading}>
            <Statistic title={t('overview.overdueNcr')} value={data?.overdueNcr ?? 0} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card loading={loading}>
            <Statistic title={t('overview.openLegacy')} value={data?.openByModule?.LEGACY_NC ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card loading={loading}>
            <Statistic
              title={t('overview.avgAuditCompliance')}
              value={data?.avgAuditCompliance ?? 0}
              suffix="%"
              valueStyle={{ color: complianceColor(complianceZone(Number(data?.avgAuditCompliance ?? 0))) }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card loading={loading} title={t('overview.byModule')}>
            <ReactECharts option={modulePie} style={{ height: 340 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card loading={loading} title={t('overview.openByModule')}>
            <ReactECharts option={openBar} style={{ height: 340 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card loading={loading} title={t('overview.ncrByStatus')}>
            <ReactECharts option={ncrStatusOpt} style={{ height: 340 }} />
          </Card>
        </Col>
      </Row>

      <Card style={{ marginTop: 16 }} loading={loading} title={t('overview.monthlyTrend')}>
        <ReactECharts option={trendOpt} style={{ height: 380 }} />
      </Card>

      <Card style={{ marginTop: 16 }} loading={loading} title={t('overview.recentAudits')}>
        <Table
          rowKey="id"
          size="small"
          pagination={false}
          dataSource={data?.recentAuditScores ?? []}
          onRow={(r) => ({ onClick: () => nav(`/audit-archive/${r.id}`), style: { cursor: 'pointer' } })}
          columns={[
            { title: t('audits.checklistTitle'), dataIndex: 'title', key: 'title' },
            { title: t('accidents.project'), dataIndex: 'projectName', key: 'project', render: (v: string | null) => v ?? '-' },
            {
              title: t('audits.compliance'),
              dataIndex: 'score',
              key: 'score',
              render: (v: number) => {
                const zone = complianceZone(Number(v))
                return <Tag color={complianceTagColor(zone)}>{v}%</Tag>
              }
            }
          ]}
        />
      </Card>
    </div>
  )
}
