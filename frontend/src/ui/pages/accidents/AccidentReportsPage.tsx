import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Col, DatePicker, Form, Input, InputNumber, Modal, Row, Select, Space, Switch, Table, Typography, message } from 'antd'
import ReactECharts from 'echarts-for-react'
import dayjs from 'dayjs'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite } from '../../../state/auth/permissions'
import { classificationLabel, useAccidentMetadata } from './accidentMetadata'

type Project = { id: string; name: string }
type AccidentType = { id: string; name: string; enabled: boolean }

type Dashboard = {
  kpis: { total: number; fat: number; lti: number; mtc: number; fac: number; nearMiss: number; equipment: number }
  monthlyTrend: { month: string; byClassification: Record<string, number> }[]
  classificationDistribution: Record<string, number>
  hazardSourceDistribution: { label: string; count: number }[]
  directCauseGroups: { groupCode: string; groupName: string; section: string; count: number }[]
  rootCauseGroups: { groupCode: string; groupName: string; section: string; count: number }[]
  bodyPartDistribution: { label: string; count: number }[]
  timeRangeDistribution: Record<string, number>
  areaDistribution: Record<string, number>
  directCauseSummary: { category: string; totalCount: number; topSubCauseCode: string; topSubCauseLabel: string; topSubCauseCount: number }[]
  rootCauseSummary: { category: string; totalCount: number; topSubCauseCode: string; topSubCauseLabel: string; topSubCauseCount: number }[]
}

type Subscription = {
  id: string
  projectId: string | null
  enabled: boolean
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY'
  hourOfDay: number
  minuteOfHour: number
  toEmails: string
  ccEmails: string | null
  filtersJson: string
}

const CLASSIFICATION_ORDER = ['FAT', 'LTI', 'MTC', 'FAC', 'NEAR_MISS', 'EQUIPMENT', 'TRAFFIC', 'FIRE', 'ENVIRONMENT', 'PERMANENT_DISABILITY', 'RWC']

export function AccidentReportsPage() {
  const nav = useNavigate()
  const { user } = useAuth()
  const { t } = useTranslation()
  const { metadata } = useAccidentMetadata()
  const canManageSubs = canWrite(user)
  const [projects, setProjects] = useState<Project[]>([])
  const [accidentTypes, setAccidentTypes] = useState<AccidentType[]>([])
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [loading, setLoading] = useState(false)
  const [exporting, setExporting] = useState(false)
  const [lastFilters, setLastFilters] = useState<Record<string, any> | null>(null)
  const [expandedDirectGroup, setExpandedDirectGroup] = useState<string | null>(null)
  const [expandedRootGroup, setExpandedRootGroup] = useState<string | null>(null)

  const [subs, setSubs] = useState<Subscription[]>([])
  const [subModalOpen, setSubModalOpen] = useState(false)
  const [editingSub, setEditingSub] = useState<Subscription | null>(null)
  const [subForm] = Form.useForm()

  const labelForClass = (code: string) => classificationLabel(metadata, code)

  const loadSubs = async () => {
    if (!canManageSubs) return
    const s = await http.get<Subscription[]>('/api/accidents/report-subscriptions')
    setSubs(s.data)
  }

  useEffect(() => {
    let mounted = true
    ;(async () => {
      const [p, types] = await Promise.all([
        http.get<Project[]>('/api/projects'),
        http.get<AccidentType[]>('/api/accidents/types')
      ])
      if (!mounted) return
      setProjects(p.data)
      setAccidentTypes(types.data || [])
      await loadSubs()
    })()
    return () => {
      mounted = false
    }
  }, [canManageSubs])

  const handleSubSave = async (v: any) => {
    try {
      const payload = {
        projectId: v.projectId || null,
        enabled: v.enabled ?? true,
        frequency: v.frequency,
        hourOfDay: v.hourOfDay,
        minuteOfHour: v.minuteOfHour,
        toEmails: v.toEmails,
        ccEmails: v.ccEmails || null,
        filtersJson: v.filtersJson || '{}'
      }
      if (editingSub) {
        await http.put(`/api/accidents/report-subscriptions/${editingSub.id}`, payload)
        message.success(t('accidents.subscriptionUpdated'))
      } else {
        await http.post('/api/accidents/report-subscriptions', payload)
        message.success(t('accidents.subscriptionCreated'))
      }
      setSubModalOpen(false)
      setEditingSub(null)
      subForm.resetFields()
      await loadSubs()
    } catch (e: any) {
      message.error(e.response?.data?.message || t('accidents.errorOccurred'))
    }
  }

  const handleSubDelete = async (id: string) => {
    Modal.confirm({
      title: t('accidents.deleteSubscription'),
      content: t('accidents.deleteSubscriptionConfirm'),
      okText: t('common.yes'),
      cancelText: t('common.no'),
      onOk: async () => {
        try {
          await http.delete(`/api/accidents/report-subscriptions/${id}`)
          message.success(t('accidents.subscriptionDeleted'))
          await loadSubs()
        } catch (e: any) {
          message.error(e.response?.data?.message || t('accidents.errorOccurred'))
        }
      }
    })
  }

  const openSubModal = (sub?: Subscription) => {
    setEditingSub(sub || null)
    if (sub) {
      subForm.setFieldsValue({
        projectId: sub.projectId,
        enabled: sub.enabled,
        frequency: sub.frequency,
        hourOfDay: sub.hourOfDay,
        minuteOfHour: sub.minuteOfHour,
        toEmails: sub.toEmails,
        ccEmails: sub.ccEmails,
        filtersJson: sub.filtersJson || '{}'
      })
    } else {
      subForm.resetFields()
      subForm.setFieldsValue({ enabled: true, frequency: 'WEEKLY', hourOfDay: 9, minuteOfHour: 0 })
    }
    setSubModalOpen(true)
  }

  const monthlyOpt = useMemo(() => {
    const months = dashboard?.monthlyTrend.map((m) => m.month) || []
    const classSet = new Set<string>()
    dashboard?.monthlyTrend.forEach((m) => Object.keys(m.byClassification).forEach((k) => classSet.add(k)))
    const classes = [...classSet].sort((a, b) => CLASSIFICATION_ORDER.indexOf(a) - CLASSIFICATION_ORDER.indexOf(b))
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: classes.map(labelForClass) },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series: classes.map((cls) => ({
        name: labelForClass(cls),
        type: 'bar',
        stack: 'total',
        data: (dashboard?.monthlyTrend || []).map((m) => m.byClassification[cls] || 0)
      }))
    }
  }, [dashboard, metadata])

  const classDonutOpt = useMemo(() => {
    const entries = Object.entries(dashboard?.classificationDistribution ?? {})
    return {
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        data: entries.map(([code, value]) => ({ name: labelForClass(code), value }))
      }]
    }
  }, [dashboard, metadata])

  const hazardOpt = useMemo(() => {
    const items = dashboard?.hazardSourceDistribution || []
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: 160 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: items.map((i) => i.label), inverse: true },
      series: [{ type: 'bar', data: items.map((i) => i.count) }]
    }
  }, [dashboard])

  const directCauseOpt = useMemo(() => {
    const items = dashboard?.directCauseGroups || []
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: 180 },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: items.map((i) => `${i.groupCode}. ${i.groupName}`),
        inverse: true
      },
      series: [{
        type: 'bar',
        data: items.map((i) => i.count),
        itemStyle: {
          color: (params: any) => (items[params.dataIndex]?.section === 'CONDITIONS' ? '#fa8c16' : '#1890ff')
        }
      }]
    }
  }, [dashboard])

  const rootCauseOpt = useMemo(() => {
    const items = dashboard?.rootCauseGroups || []
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: 180 },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: items.map((i) => `${i.groupCode}. ${i.groupName}`),
        inverse: true
      },
      series: [{
        type: 'bar',
        data: items.map((i) => i.count),
        itemStyle: {
          color: (params: any) => (items[params.dataIndex]?.section === 'JOB' ? '#722ed1' : '#13c2c2')
        }
      }]
    }
  }, [dashboard])

  const bodyPartOpt = useMemo(() => {
    const items = dashboard?.bodyPartDistribution || []
    return {
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: items.map((i) => i.label), axisLabel: { rotate: 35 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: items.map((i) => i.count) }]
    }
  }, [dashboard])

  const timeRangeOpt = useMemo(() => {
    const entries = Object.entries(dashboard?.timeRangeDistribution ?? {})
    return {
      tooltip: { trigger: 'item' },
      radar: {
        indicator: entries.map(([name]) => ({ name, max: Math.max(...entries.map(([, v]) => v), 1) }))
      },
      series: [{
        type: 'radar',
        data: [{ value: entries.map(([, v]) => v), name: t('accidents.timeRange') }]
      }]
    }
  }, [dashboard, t])

  const areaOpt = useMemo(() => {
    const entries = Object.entries(dashboard?.areaDistribution ?? {})
    return {
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: entries.map(([k]) => k) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v) }]
    }
  }, [dashboard])

  const kpiCards = [
    { key: 'total', label: t('accidents.summaryTotal'), value: dashboard?.kpis.total ?? 0, filter: null },
    { key: 'FAT', label: 'FAT', value: dashboard?.kpis.fat ?? 0, filter: 'FAT' },
    { key: 'LTI', label: 'LTI', value: dashboard?.kpis.lti ?? 0, filter: 'LTI' },
    { key: 'MTC', label: 'MTC', value: dashboard?.kpis.mtc ?? 0, filter: 'MTC' },
    { key: 'FAC', label: 'FAC', value: dashboard?.kpis.fac ?? 0, filter: 'FAC' },
    { key: 'NEAR_MISS', label: t('accidents.summaryNearMiss'), value: dashboard?.kpis.nearMiss ?? 0, filter: 'NEAR_MISS' },
    { key: 'EQUIPMENT', label: t('accidents.summaryEquipment'), value: dashboard?.kpis.equipment ?? 0, filter: 'EQUIPMENT' }
  ]

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('accidents.reportsTitle')}
        </Typography.Title>
        <Button
          loading={exporting}
          disabled={!lastFilters}
          onClick={async () => {
            if (!lastFilters) return
            setExporting(true)
            try {
              const from = lastFilters.from ? lastFilters.from.toISOString() : null
              const to = lastFilters.to ? lastFilters.to.toISOString() : null
              const resp = await http.get<ArrayBuffer>('/api/accidents/stats/export', {
                params: {
                  from,
                  to,
                  projectId: lastFilters.projectId || null,
                  area: lastFilters.area || null,
                  accidentTypeId: lastFilters.accidentTypeId || null,
                  bucket: 'MONTH'
                },
                responseType: 'arraybuffer'
              })
              const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
              const url = window.URL.createObjectURL(blob)
              const a = document.createElement('a')
              a.href = url
              a.download = 'kaza-raporu.xlsx'
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
          {t('accidents.exportExcel')}
        </Button>
      </Space>

      <Card style={{ marginBottom: 16 }}>
        <Form
          layout="inline"
          initialValues={{ from: dayjs('2020-01-01'), to: dayjs('2024-12-31') }}
          onFinish={async (v) => {
            setLoading(true)
            try {
              const from = v.from ? v.from.startOf('day').toISOString() : null
              const to = v.to ? v.to.endOf('day').toISOString() : null
              const resp = await http.get<Dashboard>('/api/accidents/stats/dashboard', {
                params: {
                  from,
                  to,
                  projectId: v.projectId || null,
                  area: v.area || null,
                  accidentTypeId: v.accidentTypeId || null
                }
              })
              setDashboard(resp.data)
              setLastFilters(v)
            } catch (e: any) {
              message.error(e.response?.data?.message || t('accidents.errorOccurred'))
              setDashboard(null)
            } finally {
              setLoading(false)
            }
          }}
        >
          <Form.Item name="projectId" label={t('accidents.project')}>
            <Select allowClear style={{ width: 260 }} options={projects.map((p) => ({ value: p.id, label: p.name }))} placeholder={t('accidents.all')} />
          </Form.Item>
          <Form.Item name="area" label={t('accidents.area')}>
            <Select
              allowClear
              style={{ width: 200 }}
              options={(metadata?.areas || []).map((a) => ({ value: a.label, label: a.label }))}
            />
          </Form.Item>
          <Form.Item name="accidentTypeId" label={t('accidents.accidentType')}>
            <Select allowClear style={{ width: 260 }} options={(accidentTypes || []).filter((x) => x.enabled).map((x) => ({ value: x.id, label: x.name }))} />
          </Form.Item>
          <Form.Item name="from" label={t('accidents.startDate')}>
            <DatePicker />
          </Form.Item>
          <Form.Item name="to" label={t('accidents.endDate')}>
            <DatePicker />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading}>
            {t('accidents.fetch')}
          </Button>
        </Form>
      </Card>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        {kpiCards.map((k) => (
          <Col xs={12} sm={8} md={6} lg={3} key={k.key}>
            <Card
              loading={loading}
              hoverable={!!k.filter}
              onClick={() => k.filter && nav(`/accidents?classification=${k.filter}`)}
              style={{ cursor: k.filter ? 'pointer' : 'default' }}
            >
              <Typography.Text type="secondary">{k.label}</Typography.Text>
              <Typography.Title level={3} style={{ margin: 0 }}>{k.value}</Typography.Title>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card title={t('accidents.monthlyTrend')} loading={loading}>
            <ReactECharts option={monthlyOpt} style={{ height: 380 }} />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title={t('accidents.classDistribution')} loading={loading}>
            <ReactECharts option={classDonutOpt} style={{ height: 380 }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={t('accidents.hazardSourceDistribution')} loading={loading}>
            <ReactECharts option={hazardOpt} style={{ height: 380 }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={t('accidents.directCauseAnalysis')} loading={loading}>
            <ReactECharts option={directCauseOpt} style={{ height: 380 }} onEvents={{
              click: (params: any) => {
                const item = dashboard?.directCauseGroups[params.dataIndex]
                if (item) setExpandedDirectGroup(item.groupCode)
              }
            }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={t('accidents.rootCauseAnalysisChart')} loading={loading}>
            <ReactECharts option={rootCauseOpt} style={{ height: 380 }} onEvents={{
              click: (params: any) => {
                const item = dashboard?.rootCauseGroups[params.dataIndex]
                if (item) setExpandedRootGroup(item.groupCode)
              }
            }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={t('accidents.bodyPartDistribution')} loading={loading}>
            <ReactECharts option={bodyPartOpt} style={{ height: 380 }} />
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card title={t('accidents.timeRangeDistribution')} loading={loading}>
            <ReactECharts option={timeRangeOpt} style={{ height: 380 }} />
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card title={t('accidents.areaDistribution')} loading={loading}>
            <ReactECharts option={areaOpt} style={{ height: 380 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title={t('accidents.directCauseSummaryTable')} loading={loading}>
            <Table
              size="small"
              rowKey="category"
              pagination={false}
              dataSource={dashboard?.directCauseSummary || []}
              columns={[
                { title: t('accidents.category'), dataIndex: 'category' },
                { title: t('accidents.totalCount'), dataIndex: 'totalCount' },
                {
                  title: t('accidents.topSubCause'),
                  key: 'top',
                  render: (_, r) => `${r.topSubCauseCode}: ${r.topSubCauseLabel} (${r.topSubCauseCount})`
                }
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={t('accidents.rootCauseSummaryTable')} loading={loading}>
            <Table
              size="small"
              rowKey="category"
              pagination={false}
              dataSource={dashboard?.rootCauseSummary || []}
              columns={[
                { title: t('accidents.category'), dataIndex: 'category' },
                { title: t('accidents.totalCount'), dataIndex: 'totalCount' },
                {
                  title: t('accidents.topSubCause'),
                  key: 'top',
                  render: (_, r) => `${r.topSubCauseCode}: ${r.topSubCauseLabel} (${r.topSubCauseCount})`
                }
              ]}
            />
          </Card>
        </Col>
      </Row>

      {(expandedDirectGroup || expandedRootGroup) && (
        <Card style={{ marginTop: 16 }} title={t('accidents.subCauseDetail')}>
          {(() => {
            const row = expandedDirectGroup
              ? dashboard?.directCauseSummary.find((r) => r.category.startsWith(`${expandedDirectGroup}.`))
              : dashboard?.rootCauseSummary.find((r) => r.category.startsWith(`${expandedRootGroup}.`))
            if (!row) return <Typography.Text type="secondary">{t('common.noData')}</Typography.Text>
            return (
              <Typography.Paragraph>
                {row.category}: {row.totalCount} — {row.topSubCauseCode}: {row.topSubCauseLabel} ({row.topSubCauseCount})
              </Typography.Paragraph>
            )
          })()}
        </Card>
      )}

      {canManageSubs ? (
        <Card
          style={{ marginTop: 16 }}
          title={t('accidents.subscriptionTitle')}
          extra={
            <Button type="primary" onClick={() => openSubModal()}>
              {t('accidents.newSubscription')}
            </Button>
          }
        >
          <Table
            rowKey="id"
            size="small"
            pagination={false}
            dataSource={subs}
            columns={[
              { title: t('common.enabled'), dataIndex: 'enabled', key: 'enabled', render: (v) => (v ? t('common.yes') : t('common.no')) },
              {
                title: t('accidents.project'),
                key: 'project',
                render: (_, r) => {
                  const p = projects.find((p) => p.id === r.projectId)
                  return p ? p.name : t('accidents.projectAll')
                }
              },
              { title: t('accidents.frequency'), dataIndex: 'frequency', key: 'frequency' },
              { title: t('accidents.hour'), key: 'time', render: (_, r) => `${r.hourOfDay}:${String(r.minuteOfHour).padStart(2, '0')}` },
              { title: 'To', dataIndex: 'toEmails', key: 'to' },
              { title: 'CC', dataIndex: 'ccEmails', key: 'cc' },
              {
                title: t('common.actions'),
                key: 'actions',
                render: (_, r) => (
                  <Space>
                    <Button size="small" onClick={() => openSubModal(r)}>
                      {t('common.edit')}
                    </Button>
                    {user?.role === 'ADMIN' && (
                      <Button size="small" danger onClick={() => handleSubDelete(r.id)}>
                        {t('common.delete')}
                      </Button>
                    )}
                  </Space>
                )
              }
            ]}
          />
        </Card>
      ) : null}

      <Modal
        title={editingSub ? t('accidents.editSubscription') : t('accidents.newSubscription')}
        open={subModalOpen}
        onCancel={() => {
          setSubModalOpen(false)
          setEditingSub(null)
          subForm.resetFields()
        }}
        onOk={() => subForm.submit()}
        width={600}
      >
        <Form form={subForm} layout="vertical" onFinish={handleSubSave}>
          <Form.Item name="projectId" label={t('accidents.projectEmpty')}>
            <Select allowClear placeholder={t('accidents.projectAll')} options={projects.map((p) => ({ value: p.id, label: p.name }))} />
          </Form.Item>
          <Form.Item name="enabled" label={t('common.enabled')} valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="frequency" label={t('accidents.frequency')} rules={[{ required: true }]}>
            <Select options={[{ value: 'DAILY', label: t('accidents.daily') }, { value: 'WEEKLY', label: t('accidents.weekly') }, { value: 'MONTHLY', label: t('accidents.monthly') }]} />
          </Form.Item>
          <Form.Item label={t('accidents.sendTime')}>
            <Space>
              <Form.Item name="hourOfDay" rules={[{ required: true }]} style={{ margin: 0 }}>
                <InputNumber min={0} max={23} placeholder={t('accidents.hour')} />
              </Form.Item>
              <Form.Item name="minuteOfHour" rules={[{ required: true }]} style={{ margin: 0 }}>
                <InputNumber min={0} max={59} placeholder={t('accidents.minute')} />
              </Form.Item>
            </Space>
          </Form.Item>
          <Form.Item name="toEmails" label={t('accidents.recipients')} rules={[{ required: true }]}>
            <Input placeholder="email1@example.com, email2@example.com" />
          </Form.Item>
          <Form.Item name="ccEmails" label={t('accidents.cc')}>
            <Input placeholder="cc1@example.com, cc2@example.com" />
          </Form.Item>
          <Form.Item name="filtersJson" label={t('accidents.filters')}>
            <Input.TextArea rows={3} placeholder='{"periodDays": 30}' />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
