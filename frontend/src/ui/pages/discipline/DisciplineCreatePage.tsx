import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Button, Card, DatePicker, Form, Input, InputNumber, Select, Space, Typography, message } from 'antd'
import dayjs from 'dayjs'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import type { DisciplineCategory, DisciplineMetadata } from './disciplineTypes'

type Employee = {
  id: string
  firstName: string
  lastName: string
  employeeNo: string | null
  jobTitle: string | null
  profession: string | null
  projectName: string | null
  primaryManagerEmployeeId: string | null
  primaryManagerName: string | null
}

export function DisciplineCreatePage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const [employees, setEmployees] = useState<Employee[]>([])
  const [metadata, setMetadata] = useState<DisciplineMetadata | null>(null)
  const [loading, setLoading] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState<DisciplineCategory | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    let mounted = true
    ;(async () => {
      try {
        const [empResp, metaResp, projResp] = await Promise.all([
          http.get<any[]>('/api/employees'),
          http.get<DisciplineMetadata>('/api/discipline-logs/metadata'),
          http.get<any[]>('/api/projects')
        ])
        if (!mounted) return
        const projects = new Map((projResp.data || []).map((p) => [p.id, p.name]))
        setMetadata(metaResp.data)
        setEmployees(
          (empResp.data || []).map((e) => ({
            id: e.id,
            firstName: e.firstName,
            lastName: e.lastName,
            employeeNo: e.employeeNo ?? null,
            jobTitle: e.jobTitle ?? null,
            profession: e.profession ?? null,
            projectName: e.projectId ? projects.get(e.projectId) ?? null : null,
            primaryManagerEmployeeId: e.primaryManagerEmployeeId ?? null,
            primaryManagerName: null
          }))
        )
      } catch {
        message.error(t('discipline.loadError'))
      }
    })()
    return () => { mounted = false }
  }, [t])

  const employeeOptions = useMemo(
    () => employees.map((e) => ({ value: e.id, label: `${e.firstName} ${e.lastName}` })),
    [employees]
  )

  const violationOptions = useMemo(() => {
    if (!metadata || !selectedCategory) return []
    return metadata.violationTypes[selectedCategory] ?? []
  }, [metadata, selectedCategory])

  const onEmployeeChange = (employeeId: string | undefined) => {
    const emp = employees.find((e) => e.id === employeeId)
    if (!emp) return
    const manager = employees.find((e) => e.id === emp.primaryManagerEmployeeId)
    form.setFieldsValue({
      fullName: `${emp.firstName} ${emp.lastName}`,
      employeeRegistrationNo: emp.employeeNo,
      jobTitle: emp.jobTitle || emp.profession,
      company: emp.projectName,
      responsiblePerson: manager ? `${manager.firstName} ${manager.lastName}` : undefined,
      violatingManagerEmployeeId: emp.primaryManagerEmployeeId
    })
  }

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>{t('discipline.newRecord')}</Typography.Title>
        <Button onClick={() => nav('/discipline')}>{t('common.back')}</Button>
      </Space>

      <Card>
        <Alert message={t('discipline.uploadAfterCreate')} type="info" showIcon style={{ marginBottom: 16 }} />
        <Form
          form={form}
          layout="vertical"
          initialValues={{ occurredAt: dayjs(), status: 'UYARI' }}
          onFinish={async (values) => {
            setLoading(true)
            try {
              const resp = await http.post<{ id: string }>('/api/discipline-logs', {
                projectId: null,
                occurredAt: values.occurredAt.toISOString(),
                fullName: values.fullName,
                employeeRegistrationNo: values.employeeRegistrationNo || null,
                company: values.company,
                jobTitle: values.jobTitle,
                workArea: values.workArea,
                categoryLevel: values.categoryLevel,
                violationType: values.violationType,
                violationDescription: values.violationDescription,
                responsiblePerson: values.responsiblePerson,
                status: values.status,
                notes: values.notes || null,
                penaltyAmount: values.penaltyAmount ?? null,
                profession: values.jobTitle || null,
                violatingEmployeeId: values.violatingEmployeeId || null,
                violatingManagerEmployeeId: values.violatingManagerEmployeeId || null
              })
              message.success(t('discipline.recordCreated'))
              nav(`/discipline/${resp.data.id}`, { replace: true })
            } finally {
              setLoading(false)
            }
          }}
        >
          <Form.Item label={t('common.dateTime')} name="occurredAt" rules={[{ required: true }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label={t('discipline.nonCompliantEmployee')} name="violatingEmployeeId">
            <Select options={employeeOptions} allowClear onChange={onEmployeeChange} />
          </Form.Item>

          <Form.Item label={t('discipline.fullName')} name="fullName" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label={t('discipline.employeeId')} name="employeeRegistrationNo">
            <Input />
          </Form.Item>

          <Form.Item label={t('discipline.company')} name="company" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label={t('discipline.jobTitle')} name="jobTitle" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label={t('discipline.workArea')} name="workArea" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label={t('common.category')} name="categoryLevel" rules={[{ required: true }]}>
            <Select
              options={metadata?.categories ?? []}
              onChange={(v: DisciplineCategory) => {
                setSelectedCategory(v)
                form.setFieldValue('violationType', undefined)
              }}
            />
          </Form.Item>

          <Form.Item label={t('discipline.violationType')} name="violationType" rules={[{ required: true }]}>
            <Select options={violationOptions} disabled={!selectedCategory} showSearch optionFilterProp="label" />
          </Form.Item>

          <Form.Item label={t('discipline.violationDescription')} name="violationDescription" rules={[{ required: true }]}>
            <Input.TextArea rows={4} />
          </Form.Item>

          <Form.Item label={t('discipline.responsiblePerson')} name="responsiblePerson" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label={t('common.status')} name="status" rules={[{ required: true }]}>
            <Select options={metadata?.statuses ?? []} />
          </Form.Item>

          <Form.Item label={t('discipline.notes')} name="notes">
            <Input.TextArea rows={2} />
          </Form.Item>

          <Form.Item label={t('discipline.penaltyAmount')} name="penaltyAmount">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Space style={{ marginTop: 12 }}>
            <Button type="primary" htmlType="submit" loading={loading}>{t('common.save')}</Button>
            <Button onClick={() => nav('/discipline')}>{t('common.cancel')}</Button>
          </Space>
        </Form>
      </Card>
    </div>
  )
}
