import React, { useEffect, useMemo, useState } from 'react'
import { Card, Col, Row, Statistic, Typography, Alert, Space, Button, Table, Tag } from 'antd'
import ReactECharts from 'echarts-for-react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../state/auth/useAuth'
import { canWrite } from '../../state/auth/permissions'
import { useNavigate } from 'react-router-dom'
import { http } from '../../api/http'
import dayjs from 'dayjs'

type DashboardStats = {
  accidentsThisMonth: number
  openNonconformities: number
  openNcr: number
  completedAudits: number
  openDisciplineLogs: number
  overdueNcr: number
}

type SeriesPoint = { bucketStart: string; count: number }
type Distribution = { byAccidentClass: Record<string, number>; byPotentialLevel: Record<string, number> }

type RecentAccident = {
  id: string
  occurredAt: string | null
  location: string | null
  accidentClass: string
  potentialLevel: string
}

type RecentNcr = {
  id: string
  ncrNumber: string
  ncrDate: string
  title: string | null
  status: string
  overdue: boolean
}

type RecentDisciplineLog = {
  id: string
  occurredAt: string | null
  category: string | null
  severity: number
  status: string
}

const NCR_STATUS_COLORS: Record<string, string> = {
  OPEN: 'red',
  CORRECTIVE_ACTION_PENDING: 'orange',
  VERIFICATION_PENDING: 'gold',
  CLOSED: 'green',
  REJECTED: 'purple'
}

export function DashboardPage() {
  const { user } = useAuth()
  const { t } = useTranslation()
  const nav = useNavigate()

  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [trend, setTrend] = useState<SeriesPoint[]>([])
  const [distribution, setDistribution] = useState<Distribution | null>(null)
  const [recentAccidents, setRecentAccidents] = useState<RecentAccident[]>([])
  const [recentNcr, setRecentNcr] = useState<RecentNcr[]>([])
  const [recentDisciplineLogs, setRecentDisciplineLogs] = useState<RecentDisciplineLog[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [statsResp, trendResp, distResp, accidentsResp, ncrResp, disciplineResp] = await Promise.all([
          http.get<DashboardStats>('/api/dashboard/stats'),
          http.get<SeriesPoint[]>('/api/dashboard/accident-trend'),
          http.get<Distribution>('/api/dashboard/accident-distribution'),
          http.get<RecentAccident[]>('/api/dashboard/recent-accidents'),
          http.get<RecentNcr[]>('/api/dashboard/recent-ncr'),
          http.get<RecentDisciplineLog[]>('/api/dashboard/recent-discipline-logs')
        ])
        if (!mounted) return
        setStats(statsResp.data)
        setTrend(trendResp.data)
        setDistribution(distResp.data)
        setRecentAccidents(accidentsResp.data)
        setRecentNcr(ncrResp.data)
        setRecentDisciplineLogs(disciplineResp.data)
      } catch (e) {
        console.error('Failed to load dashboard data:', e)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [])

  const trendOption = useMemo(() => {
    const dates = trend.map((p) => dayjs(p.bucketStart).format('MMM YYYY'))
    const counts = trend.map((p) => p.count)
    return {
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: counts, smooth: true, areaStyle: {} }]
    }
  }, [trend])

  const classPieOption = useMemo(() => {
    if (!distribution) return {}
    const entries = Object.entries(distribution.byAccidentClass || {})
    return {
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      series: [{ name: t('accidents.accidentClass'), type: 'pie', radius: ['35%', '65%'], data: entries.map(([name, value]) => ({ name, value })) }]
    }
  }, [distribution, t])

  const potentialPieOption = useMemo(() => {
    if (!distribution) return {}
    const entries = Object.entries(distribution.byPotentialLevel || {})
    return {
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      series: [{ name: t('accidents.potentialLevel'), type: 'pie', radius: ['35%', '65%'], data: entries.map(([name, value]) => ({ name, value })) }]
    }
  }, [distribution, t])

  return (
    <div style={{ padding: 16 }}>
      <Typography.Title level={3} style={{ marginTop: 0 }}>
        {t('dashboard.welcome')}{user ? `, ${user.username}` : ''}
      </Typography.Title>

      {user && user.role === 'ADMIN' && (
        <Alert
          message={t('dashboard.adminUser')}
          description={
            <Space direction="vertical">
              <div>{t('dashboard.adminUserMessage')}</div>
              <Button type="primary" onClick={() => nav('/users')}>{t('menu.users')}</Button>
            </Space>
          }
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      <Card title={t('dashboard.quickAccess')} style={{ marginBottom: 16 }}>
        <Space wrap>
          {canWrite(user) && (
            <>
              <Button onClick={() => nav('/accidents/new')}>{t('accidents.newAccident')}</Button>
              <Button onClick={() => nav('/ncr/new')}>{t('ncr.newNcr')}</Button>
              <Button onClick={() => nav('/discipline/new')}>{t('discipline.newRecord')}</Button>
            </>
          )}
          <Button onClick={() => nav('/audits')}>{t('menu.audits')}</Button>
          <Button onClick={() => nav('/accidents/reports')}>{t('accidents.reports')}</Button>
        </Space>
      </Card>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Statistic title={t('dashboard.accidentsThisMonth')} value={stats?.accidentsThisMonth ?? 0} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Statistic title={t('dashboard.openNcr')} value={stats?.openNcr ?? 0} valueStyle={{ color: '#fa8c16' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Statistic title={t('dashboard.overdueNcr')} value={stats?.overdueNcr ?? 0} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Statistic title={t('dashboard.completedAudits')} value={stats?.completedAudits ?? 0} valueStyle={{ color: '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Statistic title={t('dashboard.openDisciplineLogs')} value={stats?.openDisciplineLogs ?? 0} valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Statistic title={t('dashboard.openNonconformities')} value={stats?.openNonconformities ?? 0} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={12}>
          <Card loading={loading} title={t('dashboard.accidentTrend')}>
            <ReactECharts option={trendOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card loading={loading} title={t('accidents.classDistribution')}>
            <ReactECharts option={classPieOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card loading={loading} title={t('accidents.potentialDistribution')}>
            <ReactECharts option={potentialPieOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card
            loading={loading}
            title={t('dashboard.recentAccidents')}
            extra={<Button type="link" onClick={() => nav('/accidents')}>{t('common.viewAll')}</Button>}
          >
            <Table
              size="small"
              dataSource={recentAccidents}
              rowKey="id"
              pagination={false}
              onRow={(record) => ({ onClick: () => nav(`/accidents/${record.id}`), style: { cursor: 'pointer' } })}
              columns={[
                { title: t('common.dateTime'), dataIndex: 'occurredAt', key: 'occurredAt', render: (text: string) => text ? dayjs(text).format('DD.MM.YYYY HH:mm') : '-' },
                { title: t('accidents.location'), dataIndex: 'location', key: 'location', render: (text: string) => text || '-' },
                { title: t('accidents.accidentClass'), dataIndex: 'accidentClass', key: 'accidentClass', render: (text: string) => <Tag color="blue">{text}</Tag> }
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card
            loading={loading}
            title={t('dashboard.recentNcr')}
            extra={<Button type="link" onClick={() => nav('/ncr')}>{t('common.viewAll')}</Button>}
          >
            <Table
              size="small"
              dataSource={recentNcr}
              rowKey="id"
              pagination={false}
              onRow={(record) => ({ onClick: () => nav(`/ncr/${record.id}`), style: { cursor: 'pointer' } })}
              columns={[
                { title: t('ncr.ncrNumber'), dataIndex: 'ncrNumber', key: 'ncrNumber' },
                { title: t('ncr.ncrDate'), dataIndex: 'ncrDate', key: 'ncrDate' },
                {
                  title: t('common.status'),
                  dataIndex: 'status',
                  key: 'status',
                  render: (v: string, r: RecentNcr) => (
                    <Space>
                      <Tag color={NCR_STATUS_COLORS[v] ?? 'default'}>{t(`ncrStatus.${v}`, v)}</Tag>
                      {r.overdue && <Tag color="red">{t('ncr.overdue')}</Tag>}
                    </Space>
                  )
                }
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card
            loading={loading}
            title={t('dashboard.recentDisciplineLogs')}
            extra={<Button type="link" onClick={() => nav('/discipline')}>{t('common.viewAll')}</Button>}
          >
            <Table
              size="small"
              dataSource={recentDisciplineLogs}
              rowKey="id"
              pagination={false}
              onRow={(record) => ({ onClick: () => nav(`/discipline/${record.id}`), style: { cursor: 'pointer' } })}
              columns={[
                { title: t('common.dateTime'), dataIndex: 'occurredAt', key: 'occurredAt', render: (text: string) => text ? dayjs(text).format('DD.MM.YYYY HH:mm') : '-' },
                { title: t('common.category'), dataIndex: 'category', key: 'category', render: (text: string) => text || '-' },
                { title: t('discipline.severity'), dataIndex: 'severity', key: 'severity', render: (text: number) => <Tag color={text >= 4 ? 'red' : text >= 3 ? 'orange' : 'blue'}>{text}</Tag> }
              ]}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
