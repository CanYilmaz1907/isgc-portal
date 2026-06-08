import React, { useEffect, useMemo, useState } from 'react'

import { Button, Card, Collapse, Divider, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd'

import { useNavigate, useParams } from 'react-router-dom'

import { useTranslation } from 'react-i18next'

import { http } from '../../../api/http'

import { useAuth } from '../../../state/auth/useAuth'

import { canWrite } from '../../../state/auth/permissions'

import { AuditAnalysisPanel, type AuditAnalysis } from './AuditAnalysisPanel'

import { complianceTagColor, scoreOptionFromResult } from './auditCompliance'



type Audit = {

  id: string

  title: string

  auditType: 'INTERNAL' | 'EXTERNAL'

  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED'

  checklistId: string | null

  checklistTitle: string | null

  calculatedScore: string | null

  reportHtml: string | null

  summary: string | null

}



type Item = { id: string; itemNo: number; categoryNo: number; question: string; maxScore: string; weight: string }

type Checklist = { id: string; title: string }



function parentIdsWithChildren(items: Item[]): Set<number> {

  const cats = new Set(items.map((i) => i.categoryNo))

  const parents = new Set<number>()

  for (const c of cats) {

    if (items.some((i) => i.categoryNo === c && i.itemNo !== c)) {

      parents.add(c)

    }

  }

  return parents

}



function scorableItems(items: Item[]): Item[] {

  const parents = parentIdsWithChildren(items)

  return items.filter((i) => !parents.has(i.itemNo))

}



export function AuditRunPage() {

  const { id } = useParams()

  const nav = useNavigate()

  const { user } = useAuth()

  const { t } = useTranslation()

  const canEdit = canWrite(user)



  const [audit, setAudit] = useState<Audit | null>(null)

  const [items, setItems] = useState<Item[]>([])

  const [checklists, setChecklists] = useState<Checklist[]>([])

  const [analysis, setAnalysis] = useState<AuditAnalysis | null>(null)

  const [loading, setLoading] = useState(false)

  const [editMode, setEditMode] = useState(false)

  const [form] = Form.useForm()

  const [auditForm] = Form.useForm()



  async function loadAnalysis(auditId: string) {

    try {

      const resp = await http.get<AuditAnalysis>(`/api/audits/${auditId}/analysis`)

      setAnalysis(resp.data)

    } catch {

      setAnalysis(null)

    }

  }



  async function loadChecklistItems(checklistId: string, auditId: string, auditStatus: Audit['status']) {

    const it = await http.get<any[]>(`/api/checklists/${checklistId}/items`)

    const list = it.data.map((x) => ({

      id: x.id,

      itemNo: x.itemNo,

      categoryNo: x.categoryNo ?? x.itemNo,

      question: x.question,

      maxScore: x.maxScore,

      weight: x.weight

    })) as Item[]

    setItems(list)



    const init: Record<string, any> = {}

    list.forEach((x) => {

      init[`scoreOpt_${x.id}`] = '1'

      init[`note_${x.id}`] = ''

    })



    if (auditStatus === 'COMPLETED' || auditStatus === 'IN_PROGRESS') {

      try {

        const results = await http.get<any[]>(`/api/audits/${auditId}/results`)

        results.data.forEach((r) => {

          init[`scoreOpt_${r.checklistItemId}`] = scoreOptionFromResult(Number(r.score), Number(r.maxScore), r.applicable)

          init[`note_${r.checklistItemId}`] = r.note ?? ''

        })

      } catch {

        // ignore

      }

    }

    form.setFieldsValue(init)

  }



  useEffect(() => {

    if (!id) return

    let mounted = true

    ;(async () => {

      setLoading(true)

      try {

        const [a, cResp] = await Promise.all([

          http.get<Audit>(`/api/audits/${id}`),

          http.get<any[]>('/api/checklists')

        ])

        if (!mounted) return

        setAudit(a.data)

        setChecklists(cResp.data.map((c) => ({ id: c.id, title: c.title })) as Checklist[])

        auditForm.setFieldsValue({

          title: a.data.title,

          auditType: a.data.auditType,

          checklistId: a.data.checklistId,

          summary: a.data.summary ?? ''

        })

        form.setFieldsValue({ summary: a.data.summary ?? '' })

        if (a.data.checklistId) {

          await loadChecklistItems(a.data.checklistId, id, a.data.status)

        }

        if (a.data.status === 'COMPLETED') {

          await loadAnalysis(id)

        }

      } finally {

        if (mounted) setLoading(false)

      }

    })()

    return () => { mounted = false }

  }, [id, form, auditForm])



  const scoreable = useMemo(() => scorableItems(items), [items])



  const grouped = useMemo(() => {

    const map = new Map<number, Item[]>()

    scoreable.forEach((it) => {

      const key = it.categoryNo

      if (!map.has(key)) map.set(key, [])

      map.get(key)!.push(it)

    })

    return [...map.entries()].sort((a, b) => a[0] - b[0])

  }, [scoreable])



  const categoryLabels = useMemo(() => {

    const labels = new Map<number, string>()

    items.forEach((i) => {

      if (i.itemNo === i.categoryNo) labels.set(i.categoryNo, `${i.itemNo} — ${i.question}`)

    })

    grouped.forEach(([catNo]) => {

      if (!labels.has(catNo)) labels.set(catNo, `Kategori ${catNo}`)

    })

    return labels

  }, [items, grouped])



  const columns = useMemo(

    () => [

      { title: t('audits.no'), dataIndex: 'itemNo', key: 'itemNo', width: 70 },

      { title: t('audits.questionField'), dataIndex: 'question', key: 'question' },

      { title: t('audits.maxScore'), dataIndex: 'maxScore', key: 'maxScore', width: 90 },

      {

        title: t('audits.score'),

        key: 'score',

        width: 140,

        render: (_: any, r: Item) => (

          <Form.Item name={`scoreOpt_${r.id}`} style={{ marginBottom: 0 }}>

            <Select

              disabled={!canEdit || audit?.status === 'COMPLETED'}

              style={{ width: '100%' }}

              options={[

                { value: '1', label: '1' },

                { value: '2', label: '2' },

                { value: '3', label: '3' },

                { value: '4', label: '4' },

                { value: '5', label: '5' },

                { value: 'N_A', label: 'N/A' }

              ]}

            />

          </Form.Item>

        )

      },

      {

        title: t('audits.note'),

        key: 'note',

        width: 260,

        render: (_: any, r: Item) => (

          <Form.Item name={`note_${r.id}`} style={{ marginBottom: 0 }}>

            <Input disabled={!canEdit || audit?.status === 'COMPLETED'} />

          </Form.Item>

        )

      }

    ],

    [canEdit, audit?.status, t]

  )



  if (!audit) {

    return (

      <div style={{ padding: 16 }}>

        <Card loading={loading}>{t('common.loading')}</Card>

      </div>

    )

  }



  const overallZone = analysis?.overallColorZone ?? 'red'



  return (

    <div style={{ padding: 16 }}>

      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>

        <Typography.Title level={3} style={{ margin: 0 }}>{t('audits.title')}</Typography.Title>

        <Space>

          {canEdit && audit.status === 'DRAFT' ? (

            <Button onClick={() => setEditMode((x) => !x)}>{editMode ? t('common.view') : t('common.edit')}</Button>

          ) : null}

          <Button onClick={() => nav('/audits')}>{t('common.back')}</Button>

        </Space>

      </Space>



      <Card loading={loading}>

        {editMode ? (

          <Form

            form={auditForm}

            layout="vertical"

            onFinish={async (values) => {

              if (!id) return

              setLoading(true)

              try {

                await http.put(`/api/audits/${id}`, {

                  projectId: null,

                  auditType: values.auditType,

                  checklistId: values.checklistId || null,

                  title: values.title,

                  summary: values.summary || null,

                  participants: []

                })

                message.success(t('audits.updateSuccess'))

                const refreshed = await http.get<Audit>(`/api/audits/${id}`)

                setAudit(refreshed.data)

                if (refreshed.data.checklistId) {

                  await loadChecklistItems(refreshed.data.checklistId, id, refreshed.data.status)

                } else {

                  setItems([])

                }

                setEditMode(false)

              } finally {

                setLoading(false)

              }

            }}

          >

            <Form.Item label={t('audits.checklistTitle')} name="title" rules={[{ required: true }]}><Input /></Form.Item>

            <Form.Item label={t('audits.auditType')} name="auditType" rules={[{ required: true }]}>

              <Select options={[{ value: 'INTERNAL', label: t('audits.internal') }, { value: 'EXTERNAL', label: t('audits.external') }]} />

            </Form.Item>

            <Form.Item label={t('audits.checklists')} name="checklistId">

              <Select allowClear options={checklists.map((c) => ({ value: c.id, label: c.title }))} />

            </Form.Item>

            <Form.Item label={t('audits.notes')} name="summary"><Input.TextArea rows={3} /></Form.Item>

            <Space>

              <Button type="primary" htmlType="submit" loading={loading}>{t('common.save')}</Button>

              <Button onClick={() => setEditMode(false)}>{t('common.cancel')}</Button>

            </Space>

          </Form>

        ) : (

          <>

            <Space wrap>

              <Typography.Text strong>{audit.title}</Typography.Text>

              <Tag>{audit.auditType}</Tag>

              <Tag color={audit.status === 'COMPLETED' ? 'green' : audit.status === 'IN_PROGRESS' ? 'gold' : 'red'}>{audit.status}</Tag>

              {audit.calculatedScore ? (

                <Tag color={complianceTagColor(overallZone as any)}>

                  {t('audits.overallCompliance')}: {audit.calculatedScore}%

                </Tag>

              ) : null}

            </Space>



            <Divider />



            {!audit.checklistId ? (

              <Typography.Text type="secondary">{t('audits.noChecklist')}</Typography.Text>

            ) : (

              <Form form={form} layout="vertical">

                <Collapse

                  defaultActiveKey={grouped.map(([k]) => String(k))}

                  items={grouped.map(([catNo, catItems]) => ({

                    key: String(catNo),

                    label: categoryLabels.get(catNo) ?? `Kategori ${catNo}`,

                    children: <Table rowKey="id" dataSource={catItems} columns={columns as any} pagination={false} size="small" />

                  }))}

                />



                <Divider />

                <Form.Item label={t('audits.notes')} name="summary">

                  <Input.TextArea rows={3} disabled={!canEdit || audit.status === 'COMPLETED'} />

                </Form.Item>



                <Space>

                  {canEdit ? (

                    <>

                      <Button

                        onClick={async () => {

                          if (!id) return

                          await http.post(`/api/audits/${id}/start`, {})

                          message.success(t('audits.started'))

                          const refreshed = await http.get<Audit>(`/api/audits/${id}`)

                          setAudit(refreshed.data)

                        }}

                        disabled={audit.status !== 'DRAFT'}

                      >

                        {t('audits.start')}

                      </Button>

                      <Button

                        type="primary"

                        onClick={async () => {

                          if (!id) return

                          const values = form.getFieldsValue()

                          const itemsPayload = scoreable.map((it) => ({

                            checklistItemId: it.id,

                            score: (() => {

                              const opt = values[`scoreOpt_${it.id}`]

                              if (opt === 'N_A') return 0

                              const puan = Number(opt ?? 1)

                              const max = Number(it.maxScore)

                              return (puan / 5) * max

                            })(),

                            applicable: values[`scoreOpt_${it.id}`] !== 'N_A',

                            note: values[`note_${it.id}`] ?? null

                          }))

                          await http.post(`/api/audits/${id}/complete`, {

                            items: itemsPayload,

                            summary: values.summary ?? null

                          })

                          message.success(t('audits.completed'))

                          const refreshed = await http.get<Audit>(`/api/audits/${id}`)

                          setAudit(refreshed.data)

                          await loadAnalysis(id)

                        }}

                        disabled={audit.status === 'COMPLETED'}

                      >

                        {t('audits.complete')}

                      </Button>

                    </>

                  ) : null}

                </Space>



                {audit.status === 'COMPLETED' && analysis ? (

                  <AuditAnalysisPanel analysis={analysis} loading={loading} />

                ) : null}



                {audit.reportHtml ? (

                  <>

                    <Divider>{t('audits.report')}</Divider>

                    <div style={{ background: 'white', padding: 12, border: '1px solid #eee' }} dangerouslySetInnerHTML={{ __html: audit.reportHtml }} />

                  </>

                ) : null}

              </Form>

            )}

          </>

        )}

      </Card>

    </div>

  )

}


