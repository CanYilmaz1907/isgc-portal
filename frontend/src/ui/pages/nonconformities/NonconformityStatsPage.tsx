import React, { useEffect, useMemo, useState } from 'react'
import { Card, Col, Row, Select, Space, Typography } from 'antd'
import ReactECharts from 'echarts-for-react'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'

type ProjectStats = { projectId: string; projectName: string; byHazardClass: Record<string, number> }

export function NonconformityStatsPage() {
  const { t } = useTranslation()
  const [totalData, setTotalData] = useState<Record<string, number>>({})
  const [byProject, setByProject] = useState<ProjectStats[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [totalResp, projectResp] = await Promise.all([
          http.get<Record<string, number>>('/api/nonconformities/stats/by-hazard-class'),
          http.get<ProjectStats[]>('/api/nonconformities/stats/by-project')
        ])
        if (!mounted) return
        setTotalData(totalResp.data)
        setByProject(projectResp.data)
        if (projectResp.data.length > 0 && !selectedProjectId) {
          setSelectedProjectId(projectResp.data[0].projectId)
        }
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  const selectedProjectStats = useMemo(
    () => byProject.find((p) => p.projectId === selectedProjectId)?.byHazardClass ?? {},
    [byProject, selectedProjectId]
  )

  const totalOption = useMemo(() => {
    const entries = Object.entries(totalData)
    return {
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      title: { text: t('nonconformities.totalChartTitle'), left: 'center' },
      series: [
        {
          name: t('nonconformities.hazardClassChart'),
          type: 'pie',
          radius: ['35%', '65%'],
          data: entries.map(([name, value]) => ({ name, value }))
        }
      ]
    }
  }, [totalData, t])

  const projectOption = useMemo(() => {
    const entries = Object.entries(selectedProjectStats)
    return {
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      title: {
        text: selectedProjectId
          ? (byProject.find((p) => p.projectId === selectedProjectId)?.projectName ?? t('nonconformities.projectChartTitle'))
          : t('nonconformities.projectChartTitle'),
        left: 'center'
      },
      series: [
        {
          name: t('nonconformities.hazardClassChart'),
          type: 'pie',
          radius: ['35%', '65%'],
          data: entries.map(([name, value]) => ({ name, value }))
        }
      ]
    }
  }, [selectedProjectStats, selectedProjectId, byProject, t])

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('nonconformities.stats')}
        </Typography.Title>
      </Space>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card loading={loading} title={t('nonconformities.totalChartTitle')}>
            <ReactECharts option={totalOption} style={{ height: 400 }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            loading={loading}
            title={
              <Space>
                {t('nonconformities.projectChartTitle')}
                <Select
                  allowClear
                  placeholder={t('accidents.project')}
                  value={selectedProjectId}
                  onChange={(v) => setSelectedProjectId(v ?? null)}
                  options={byProject.map((p) => ({ value: p.projectId, label: p.projectName }))}
                  style={{ minWidth: 180 }}
                />
              </Space>
            }
          >
            <ReactECharts option={projectOption} style={{ height: 400 }} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}


