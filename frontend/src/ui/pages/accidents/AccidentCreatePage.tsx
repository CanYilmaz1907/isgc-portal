import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Button, Card, Checkbox, DatePicker, Form, Input, InputNumber, Select, Space, Steps, Typography, message } from 'antd'
import dayjs from 'dayjs'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { http } from '../../../api/http'
import { CauseSelectorEditor } from './CauseSelectorEditor'
import { useAccidentMetadata, type CauseSelection } from './accidentMetadata'
import { useAuth } from '../../../state/auth/useAuth'

type AccidentType = { id: string; code: string; name: string; formSchemaJson: string; enabled: boolean }
type Project = { id: string; code: string; name: string; enabled: boolean }

const HIDE_INJURY_CLASSIFICATIONS = new Set(['NEAR_MISS', 'EQUIPMENT'])
const EMERGENCY_CLASSIFICATIONS = new Set(['FAT', 'LTI'])
const TRAFFIC_CLASSIFICATION = 'TRAFFIC'

export function AccidentCreatePage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const { metadata, loading: metaLoading, error: metaError } = useAccidentMetadata()
  const [form] = Form.useForm()
  const [types, setTypes] = useState<AccidentType[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [typeId, setTypeId] = useState<string | null>(null)
  const [projectId, setProjectId] = useState<string | null>(null)
  const [step, setStep] = useState(0)
  const [directCauses, setDirectCauses] = useState<CauseSelection[]>([])
  const [rootCauses, setRootCauses] = useState<CauseSelection[]>([])
  const [saving, setSaving] = useState(false)

  const classification = Form.useWatch('classification', form)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      try {
        const [typesResp, projResp] = await Promise.all([
          http.get<AccidentType[]>('/api/accidents/types'),
          http.get<Project[]>('/api/projects')
        ])
        if (!mounted) return
        const typesList = typesResp.data || []
        setTypes(typesList)
        setProjects(projResp.data || [])
        if (typesList.length > 0) setTypeId(typesList[0].id)
      } catch (e: any) {
        message.error(e.response?.data?.message || t('common.loadError'))
      }
    })()
    return () => {
      mounted = false
    }
  }, [t])

  const lookupOptions = useMemo(
    () => ({
      classification: (metadata?.classifications || []).map((o) => ({ value: o.code, label: o.label })),
      area: (metadata?.areas || []).map((o) => ({ value: o.label, label: o.label })),
      timeRange: (metadata?.timeRanges || []).map((o) => ({ value: o.label, label: o.label })),
      hazardSource: (metadata?.hazardSources || []).map((o) => ({ value: o.label, label: o.label })),
      injuryType: (metadata?.injuryTypes || []).map((o) => ({ value: o.label, label: o.label })),
      injuredBodyPart: (metadata?.injuredBodyParts || []).map((o) => ({ value: o.label, label: o.label }))
    }),
    [metadata]
  )

  const showInjuryFields = classification && !HIDE_INJURY_CLASSIFICATIONS.has(classification)
  const showEmergency = classification && EMERGENCY_CLASSIFICATIONS.has(classification)
  const showVehicle = classification === TRAFFIC_CLASSIFICATION

  const validateStep = async () => {
    if (step === 0) {
      await form.validateFields([
        'classification', 'occurredAt', 'area', 'timePeriod', 'hazardSource',
        'description', 'workSupervisor', ...(showInjuryFields ? ['injuryType', 'injuredBodyPart'] : [])
      ])
    } else if (step === 1) {
      await form.validateFields(['personName'])
    }
  }

  const submit = async () => {
    if (!typeId) return
    const values = form.getFieldsValue(true)
    setSaving(true)
    try {
      await http.post('/api/accidents', {
        projectId: projectId || null,
        accidentTypeId: typeId,
        occurredAt: values.occurredAt ? values.occurredAt.toISOString() : null,
        location: values.location || null,
        accidentClass: 'MINOR',
        potentialLevel: 'LOW',
        description: values.description || null,
        formDataJson: '{}',
        rootCauseDataJson: '{}',
        injuredEmployeeIds: [],
        keyPersonEmployeeIds: [],
        status: 'OPEN',
        area: values.area || null,
        hazardSource: values.hazardSource || null,
        injuredBodyPart: showInjuryFields ? values.injuredBodyPart || null : null,
        injuryType: showInjuryFields ? values.injuryType || null : null,
        employeeRegistrationNo: values.employeeRegistrationNo || null,
        supervisorEmployeeId: null,
        timePeriod: values.timePeriod || null,
        groupCompanyName: null,
        responsiblePerson: null,
        estimatedCost: null,
        workRelated: true,
        workDuringAccident: null,
        injuredPersonAge: values.personAge || null,
        injuredPersonProfession: values.personJobTitle || null,
        injuredPersonGender: null,
        injuredPersonNationality: values.personNationality || null,
        injuredPersonCompany: values.personCompany || null,
        actionsTakenJson: '[]',
        preparedByUserId: user?.userId || null,
        preparedAt: dayjs().toISOString(),
        classification: values.classification || null,
        personName: values.personName || null,
        durationOnProject: values.durationOnProject || null,
        durationInRole: values.durationInRole || null,
        workSupervisor: values.workSupervisor || null,
        emergencyNotificationSent: showEmergency ? values.emergencyNotificationSent ?? null : null,
        vehiclePlate: showVehicle ? values.vehiclePlate || null : null,
        vehicleType: showVehicle ? values.vehicleType || null : null,
        directCauses,
        rootCauses
      })
      message.success(t('accidents.updateSuccess'))
      nav('/accidents', { replace: true })
    } catch (e: any) {
      message.error(e.response?.data?.message || t('accidents.errorOccurred'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('accidents.newAccident')}
        </Typography.Title>
        <Button onClick={() => nav('/accidents')}>{t('common.back')}</Button>
      </Space>

      <Card loading={metaLoading}>
        {metaError && <Alert type="error" message={metaError} style={{ marginBottom: 16 }} />}
        <Steps
          current={step}
          style={{ marginBottom: 24 }}
          items={[
            { title: t('accidents.wizardStep1') },
            { title: t('accidents.wizardStep2') },
            { title: t('accidents.wizardStep3') }
          ]}
        />

        <Form form={form} layout="vertical" initialValues={{ occurredAt: dayjs(), emergencyNotificationSent: false }}>
          {step === 0 && (
            <>
              <Form.Item label={t('accidents.project')}>
                <Select
                  allowClear
                  placeholder={t('accidents.project')}
                  value={projectId}
                  onChange={(v) => setProjectId(v ?? null)}
                  options={projects.filter((p) => p.enabled).map((p) => ({ value: p.id, label: `${p.name} (${p.code})` }))}
                />
              </Form.Item>
              <Form.Item label={t('accidents.accidentClassification')} name="classification" rules={[{ required: true }]}>
                <Select options={lookupOptions.classification} showSearch optionFilterProp="label" />
              </Form.Item>
              <Form.Item label={t('accidents.area')} name="area" rules={[{ required: true }]}>
                <Select options={lookupOptions.area} showSearch optionFilterProp="label" />
              </Form.Item>
              <Form.Item label={t('accidents.dateTime')} name="occurredAt" rules={[{ required: true }]}>
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item label={t('accidents.timeRange')} name="timePeriod" rules={[{ required: true }]}>
                <Select options={lookupOptions.timeRange} />
              </Form.Item>
              <Form.Item label={t('accidents.hazardSource')} name="hazardSource" rules={[{ required: true }]}>
                <Select options={lookupOptions.hazardSource} showSearch optionFilterProp="label" />
              </Form.Item>
              {showInjuryFields && (
                <>
                  <Form.Item label={t('accidents.injuryType')} name="injuryType">
                    <Select allowClear options={lookupOptions.injuryType} showSearch optionFilterProp="label" />
                  </Form.Item>
                  <Form.Item label={t('accidents.injuredBodyPart')} name="injuredBodyPart">
                    <Select allowClear options={lookupOptions.injuredBodyPart} showSearch optionFilterProp="label" />
                  </Form.Item>
                </>
              )}
              <Form.Item label={t('accidents.description')} name="description" rules={[{ required: true }]}>
                <Input.TextArea rows={4} />
              </Form.Item>
              <Form.Item label={t('accidents.workSupervisor')} name="workSupervisor" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              {showEmergency && (
                <Form.Item name="emergencyNotificationSent" valuePropName="checked">
                  <Checkbox>{t('accidents.emergencyNotificationSent')}</Checkbox>
                </Form.Item>
              )}
              {showVehicle && (
                <>
                  <Form.Item label={t('accidents.vehiclePlate')} name="vehiclePlate">
                    <Input />
                  </Form.Item>
                  <Form.Item label={t('accidents.vehicleType')} name="vehicleType">
                    <Input />
                  </Form.Item>
                </>
              )}
              <Form.Item label={t('accidents.location')} name="location">
                <Input />
              </Form.Item>
            </>
          )}

          {step === 1 && (
            <>
              <Form.Item label={t('accidents.personName')} name="personName" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              <Form.Item label={t('accidents.employeeRegistrationNo')} name="employeeRegistrationNo">
                <Input />
              </Form.Item>
              <Form.Item label={t('accidents.injuredPersonAge')} name="personAge">
                <InputNumber min={0} max={150} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item label={t('accidents.injuredPersonProfession')} name="personJobTitle">
                <Input />
              </Form.Item>
              <Form.Item label={t('accidents.injuredPersonNationality')} name="personNationality">
                <Input />
              </Form.Item>
              <Form.Item label={t('accidents.durationOnProject')} name="durationOnProject">
                <Input placeholder={t('accidents.durationPlaceholder')} />
              </Form.Item>
              <Form.Item label={t('accidents.durationInRole')} name="durationInRole">
                <Input placeholder={t('accidents.durationPlaceholder')} />
              </Form.Item>
              <Form.Item label={t('accidents.injuredPersonCompany')} name="personCompany">
                <Input />
              </Form.Item>
            </>
          )}

          {step === 2 && metadata && (
            <>
              <CauseSelectorEditor
                title={t('accidents.directCauses')}
                groups={metadata.directCauseGroups}
                value={directCauses}
                onChange={setDirectCauses}
              />
              <CauseSelectorEditor
                title={t('accidents.rootCauses')}
                groups={metadata.rootCauseGroups}
                value={rootCauses}
                onChange={setRootCauses}
              />
            </>
          )}
        </Form>

        <Space style={{ marginTop: 16 }}>
          {step > 0 && (
            <Button onClick={() => setStep((s) => s - 1)}>{t('accidents.wizardPrev')}</Button>
          )}
          {step < 2 ? (
            <Button
              type="primary"
              disabled={metaLoading || !!metaError}
              onClick={async () => {
                try {
                  await validateStep()
                  setStep((s) => s + 1)
                } catch {
                  /* validation shown by form */
                }
              }}
            >
              {t('accidents.wizardNext')}
            </Button>
          ) : (
            <Button type="primary" loading={saving} disabled={!typeId} onClick={submit}>
              {t('common.save')}
            </Button>
          )}
          <Button onClick={() => nav('/accidents')}>{t('common.cancel')}</Button>
        </Space>
      </Card>
    </div>
  )
}
