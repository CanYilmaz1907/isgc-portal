import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Col, Row, Space, Statistic, Typography } from 'antd'
import ReactECharts from 'echarts-for-react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import type { DisciplineSummaryStats } from './disciplineTypes'

export function DisciplineStatsPage() {
  const { t } = useTranslation()
  const nav = useNavigate()
  const [summary, setSummary] = useState<DisciplineSummaryStats | null>(null)
  const [byViolation, setByViolation] = useState<Record<string, number>>({})
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [s, v] = await Promise.all([
          http.get<DisciplineSummaryStats>('/api/discipline-logs/stats/summary'),
          http.get<Record<string, number>>('/api/discipline-logs/stats/by-severity')
        ])
        if (!mounted) return
        setSummary(s.data)
        setByViolation(v.data)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => { mounted = false }
  }, [])

  const categoryOpt = useMemo(() => {
    const entries = Object.entries(summary?.byCategory ?? {})
    return {
      tooltip: {},
      xAxis: { type: 'category', data: entries.map(([k]) => k.replace('CAT_', 'Kategori ')), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v), itemStyle: { color: '#1677ff' } }]
    }
  }, [summary])

  const monthOpt = useMemo(() => {
    const entries = Object.entries(summary?.byMonth ?? {}).sort(([a], [b]) => a.localeCompare(b))
    return {
      tooltip: {},
      xAxis: { type: 'category', data: entries.map(([k]) => k) },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: entries.map(([, v]) => v), smooth: true }]
    }
  }, [summary])

  const companyOpt = useMemo(() => {
    const entries = Object.entries(summary?.byCompany ?? {}).slice(0, 10)
    return {
      tooltip: {},
      xAxis: { type: 'category', data: entries.map(([k]) => k), axisLabel: { rotate: 25 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v) }]
    }
  }, [summary])

  const responsibleOpt = useMemo(() => {
    const entries = Object.entries(summary?.byResponsiblePerson ?? {}).slice(0, 10)
    return {
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '65%',
        data: entries.map(([name, value]) => ({ name, value }))
      }]
    }
  }, [summary])

  const violationOpt = useMemo(() => {
    const entries = Object.entries(byViolation).sort((a, b) => b[1] - a[1]).slice(0, 8)
    return {
      tooltip: {},
      xAxis: { type: 'category', data: entries.map(([k]) => k.length > 30 ? k.slice(0, 30) + '…' : k), axisLabel: { rotate: 35 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v) }]
    }
  }, [byViolation])

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>{t('discipline.stats')}</Typography.Title>
        <Button onClick={() => nav('/discipline')}>{t('common.back')}</Button>
      </Space>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={6}>
          <Card loading={loading}><Statistic title={t('discipline.totalWarnings')} value={summary?.totalWarnings ?? 0} /></Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card loading={loading}><Statistic title={t('discipline.totalPenalties')} value={summary?.totalPenalties ?? 0} valueStyle={{ color: '#cf1322' }} /></Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Card loading={loading} title={t('discipline.byCategory')}><ReactECharts option={categoryOpt} style={{ height: 360 }} /></Card>
        </Col>
        <Col xs={24} md={12}>
          <Card loading={loading} title={t('discipline.byMonth')}><ReactECharts option={monthOpt} style={{ height: 360 }} /></Card>
        </Col>
        <Col xs={24} md={12}>
          <Card loading={loading} title={t('discipline.byCompany')}><ReactECharts option={companyOpt} style={{ height: 360 }} /></Card>
        </Col>
        <Col xs={24} md={12}>
          <Card loading={loading} title={t('discipline.byResponsible')}><ReactECharts option={responsibleOpt} style={{ height: 360 }} /></Card>
        </Col>
        <Col xs={24}>
          <Card loading={loading} title={t('discipline.byViolationType')}><ReactECharts option={violationOpt} style={{ height: 400 }} /></Card>
        </Col>
      </Row>
    </div>
  )
}
