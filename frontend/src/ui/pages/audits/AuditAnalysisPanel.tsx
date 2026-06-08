import React, { useMemo } from 'react'
import { Card, Col, Row, Table, Tag, Typography } from 'antd'
import ReactECharts from 'echarts-for-react'
import { useTranslation } from 'react-i18next'
import { complianceColor, complianceTagColor } from './auditCompliance'

export type AuditAnalysis = {
  overallScore: number
  overallColorZone: string
  categories: {
    categoryNo: number
    label: string
    compliancePercent: number
    colorZone: string
    itemCount: number
    applicableCount: number
  }[]
  items: {
    itemNo: number
    categoryNo: number
    question: string
    compliancePercent: number
    colorZone: string
    applicable: boolean
    score: number
    maxScore: number
  }[]
}

type Props = {
  analysis: AuditAnalysis | null
  loading?: boolean
}

export function AuditAnalysisPanel({ analysis, loading }: Props) {
  const { t } = useTranslation()

  const radarOption = useMemo(() => {
    if (!analysis?.items?.length) return {}
    const labels = analysis.items.map((i) => `${i.itemNo}`)
    const values = analysis.items.map((i) => Number(i.compliancePercent))
    return {
      tooltip: {},
      radar: {
        indicator: labels.map((name) => ({ name, max: 100 })),
        radius: '62%'
      },
      series: [{
        type: 'radar',
        data: [{
          value: values,
          name: t('audits.compliance'),
          areaStyle: { opacity: 0.2 }
        }]
      }]
    }
  }, [analysis, t])

  if (!analysis) return null

  return (
    <div style={{ marginTop: 16 }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <Card loading={loading} title={t('audits.radarChart')}>
            <ReactECharts option={radarOption} style={{ height: 380 }} />
          </Card>
        </Col>
        <Col xs={24} lg={14}>
          <Card loading={loading} title={t('audits.categoryCompliance')}>
            <Table
              size="small"
              rowKey="categoryNo"
              pagination={false}
              dataSource={analysis.categories}
              columns={[
                { title: t('audits.no'), dataIndex: 'categoryNo', key: 'no', width: 70 },
                { title: t('audits.category'), dataIndex: 'label', key: 'label', ellipsis: true },
                {
                  title: t('audits.compliance'),
                  dataIndex: 'compliancePercent',
                  key: 'pct',
                  width: 120,
                  render: (v: number, r: AuditAnalysis['categories'][0]) => (
                    <Tag color={complianceTagColor(r.colorZone as any)}>{Number(v).toFixed(1)}%</Tag>
                  )
                },
                {
                  title: t('audits.complianceLevel'),
                  key: 'zone',
                  width: 140,
                  render: (_: unknown, r: AuditAnalysis['categories'][0]) => (
                    <Typography.Text style={{ color: complianceColor(r.colorZone as any) }}>
                      {t(`audits.complianceZone.${r.colorZone}`, r.colorZone)}
                    </Typography.Text>
                  )
                }
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Card style={{ marginTop: 16 }} loading={loading} title={t('audits.itemCompliance')}>
        <Table
          size="small"
          rowKey="itemNo"
          pagination={{ pageSize: 15 }}
          dataSource={analysis.items}
          columns={[
            { title: t('audits.no'), dataIndex: 'itemNo', key: 'no', width: 70 },
            { title: t('audits.questionField'), dataIndex: 'question', key: 'q', ellipsis: true },
            {
              title: t('audits.compliance'),
              dataIndex: 'compliancePercent',
              key: 'pct',
              width: 100,
              render: (v: number, r: AuditAnalysis['items'][0]) => (
                <Tag color={complianceTagColor(r.colorZone as any)}>{Number(v).toFixed(1)}%</Tag>
              )
            }
          ]}
        />
      </Card>
    </div>
  )
}
