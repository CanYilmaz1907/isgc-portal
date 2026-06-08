import React, { useEffect, useMemo, useState } from 'react'
import {
  App,
  Button,
  Card,
  Descriptions,
  DatePicker,
  Divider,
  Drawer,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  Upload,
  Checkbox
} from 'antd'
import type { UploadRequestOption } from 'rc-upload/lib/interface'
import dayjs from 'dayjs'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getErrorMessage, http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'
import { canWrite, canUpload } from '../../../state/auth/permissions'
import { emptyRootCause, safeParseRootCause, type RootCauseData } from './rootCause'
import { RootCauseEditor } from './RootCauseEditor'
import { classificationLabel, useAccidentMetadata } from './accidentMetadata'
import { CauseSelectorEditor } from './CauseSelectorEditor'

type PersonRef = { employeeId: string; firstName: string; lastName: string }
type NullableString = string | null | undefined

type Accident = {
  id: string
  projectId: string | null
  projectName: string | null
  accidentTypeId: string
  accidentTypeName: string
  occurredAt: string | null
  location: string | null
  accidentClass: string
  potentialLevel: string
  description: string | null
  formDataJson: string
  rootCauseDataJson: string
  status: string
  injured: PersonRef[]
  keyPeople: PersonRef[]
  // Excel-based fields
  area: string | null
  hazardSource: string | null
  injuredBodyPart: string | null
  injuryType: string | null
  employeeRegistrationNo: string | null
  supervisorEmployee: PersonRef | null
  timePeriod: string | null
  // New report template fields
  groupCompanyName: string | null
  responsiblePerson: string | null
  estimatedCost: string | null
  workRelated: boolean | null
  workDuringAccident: string | null
  injuredPersonAge: number | null
  injuredPersonProfession: string | null
  injuredPersonGender: string | null
  injuredPersonNationality: string | null
  injuredPersonCompany: string | null
  actionsTakenJson: string | null
  preparedBy: PersonRef | null
  preparedAt: string | null
  incidentNo: number | null
  classification: string | null
  personName: string | null
  durationOnProject: string | null
  durationInRole: string | null
  workSupervisor: string | null
  emergencyNotificationSent: boolean | null
  vehiclePlate: string | null
  vehicleType: string | null
  directCauses: { code: string; label: string }[]
  rootCauses: { code: string; label: string }[]
}

function formatTimePeriod(value: NullableString) {
  if (!value) return '-'
  const m = /^(\d{2})(\d{2})\*(\d{2})(\d{2})$/.exec(value)
  if (m) return `${m[1]}:${m[2]} - ${m[3]}:${m[4]}`
  return value.replace('*', ' - ')
}

type FileObj = {
  id: string
  originalFilename: string
  displayName: string | null
  contentType: string | null
  sizeBytes: number
  createdAt: string
}

type Training = {
  id: string
  employeeId: string
  trainingName: string
  provider: string | null
  completedOn: string | null
  validUntil: string | null
}

type ActionItem = { injuredEmployeeId?: string; injuredLabel?: string; action: string; responsiblePerson: string; targetDate: string | null }

export function AccidentDetailPage() {
  const { id } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const { t, i18n } = useTranslation()
  const { message } = App.useApp()
  const { metadata } = useAccidentMetadata()
  const canEdit = canWrite(user)
  const canUploadFiles = canUpload(user)

  const [accident, setAccident] = useState<Accident | null>(null)
  const [files, setFiles] = useState<FileObj[]>([])
  const [loading, setLoading] = useState(false)

  const [trainingDrawerOpen, setTrainingDrawerOpen] = useState(false)
  const [trainingEmployee, setTrainingEmployee] = useState<PersonRef | null>(null)
  const [trainingRows, setTrainingRows] = useState<Training[]>([])
  const [trainingLoading, setTrainingLoading] = useState(false)

  const [editMode, setEditMode] = useState(false)
  const [formData, setFormData] = useState<Record<string, any>>({})
  const [rootCause, setRootCause] = useState<RootCauseData>(emptyRootCause())
  const [injuredIds, setInjuredIds] = useState<string[]>([])
  const [keyPersonIds, setKeyPersonIds] = useState<string[]>([])
  const [employees, setEmployees] = useState<PersonRef[]>([])
  const [supervisorEmployeeId, setSupervisorEmployeeId] = useState<string | null>(null)
  const [actions, setActions] = useState<ActionItem[]>([])

  const [basicForm] = Form.useForm()

  useEffect(() => {
    if (!id) return
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const [a, f, types, emp] = await Promise.all([
          http.get<Accident>(`/api/accidents/${id}`),
          http.get<FileObj[]>(`/api/accidents/${id}/files`),
          http.get<any[]>(`/api/accidents/types`),
          http.get<any[]>(`/api/employees`)
        ])
        if (!mounted) return
        setAccident(a.data)
        setFiles(f.data)

        const parsedFormData = JSON.parse(a.data.formDataJson || '{}')
        setFormData(parsedFormData)
        setRootCause(safeParseRootCause(a.data.rootCauseDataJson))
        setInjuredIds((a.data.injured ?? []).map((p) => p.employeeId))
        setKeyPersonIds((a.data.keyPeople ?? []).map((p) => p.employeeId))
        setSupervisorEmployeeId(a.data.supervisorEmployee?.employeeId || null)
        const empList = (emp.data || []).map((x: any) => ({
          employeeId: x.id,
          firstName: x.firstName,
          lastName: x.lastName
        })) as PersonRef[]
        setEmployees(empList)
        
        // Parse actionsTakenJson
        try {
          const actionsData = JSON.parse(a.data.actionsTakenJson || '[]')
          setActions(Array.isArray(actionsData) ? actionsData : [])
        } catch {
          setActions([])
        }
        basicForm.setFieldsValue({
          occurredAt: a.data.occurredAt ? dayjs(a.data.occurredAt) : null,
          location: a.data.location ?? '',
          accidentClassification: parsedFormData.accidentClassification || a.data.accidentClass || '',
          potentialLevel: a.data.potentialLevel,
          description: a.data.description ?? '',
          area: a.data.area ?? null,
          hazardSource: a.data.hazardSource ?? null,
          injuredBodyPart: a.data.injuredBodyPart ?? null,
          injuryType: a.data.injuryType ?? null,
          employeeRegistrationNo: a.data.employeeRegistrationNo ?? null,
          timePeriod: a.data.timePeriod ?? null,
          // New report template fields
          groupCompanyName: a.data.groupCompanyName ?? '',
          projectName: parsedFormData.projectName || '',
          responsiblePerson: a.data.responsiblePerson ?? '',
          estimatedCost: a.data.estimatedCost ?? '',
          workRelated: a.data.workRelated ?? true,
          workDuringAccident: a.data.workDuringAccident ?? '',
          injuredPersonAge: a.data.injuredPersonAge ?? null,
          injuredPersonProfession: a.data.injuredPersonProfession ?? '',
          injuredPersonGender: a.data.injuredPersonGender ?? null,
          injuredPersonNationality: a.data.injuredPersonNationality ?? '',
          injuredPersonCompany: a.data.injuredPersonCompany ?? ''
        })
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [id, basicForm])

  const trainingCols = useMemo(
    () => [
      { title: t('training.trainingName'), dataIndex: 'trainingName', key: 'trainingName' },
      { title: t('training.provider'), dataIndex: 'provider', key: 'provider' },
      { title: t('training.completedOn'), dataIndex: 'completedOn', key: 'completedOn' },
      { title: t('training.validUntil'), dataIndex: 'validUntil', key: 'validUntil' }
    ],
    [t]
  )

  async function openTraining(person: PersonRef) {
    setTrainingEmployee(person)
    setTrainingDrawerOpen(true)
    setTrainingLoading(true)
    try {
      const resp = await http.get<Training[]>(`/api/training-records/by-employee/${person.employeeId}`)
      setTrainingRows(resp.data)
    } finally {
      setTrainingLoading(false)
    }
  }

  async function downloadFile(fileId: string, fileName: string) {
    if (!id) return
    const resp = await http.get<ArrayBuffer>(`/api/accidents/${id}/files/${fileId}/download`, {
      responseType: 'arraybuffer'
    })
    const blob = new Blob([resp.data])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    window.URL.revokeObjectURL(url)
  }

  const uploadRequest = async (opt: UploadRequestOption) => {
    if (!id) return
    const file = opt.file as File
    const form = new FormData()
    form.append('file', file)
    try {
      await http.post(`/api/accidents/${id}/files`, form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      message.success(t('accidents.uploadSuccess'))
      const refreshed = await http.get<FileObj[]>(`/api/accidents/${id}/files`)
      setFiles(refreshed.data)
      opt.onSuccess?.({}, new XMLHttpRequest())
    } catch (e) {
      message.error(t('accidents.uploadError'))
      opt.onError?.(e as any)
    }
  }

  if (!accident) {
    return (
      <div style={{ padding: 16 }}>
        <Card loading={loading}>{t('common.recordLoading')}</Card>
      </div>
    )
  }

  const rootCauseValue = editMode ? rootCause : safeParseRootCause(accident.rootCauseDataJson)

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('accidents.accidentDetail')}
        </Typography.Title>
        <Space>
          <Button type="primary" icon={<span>📄</span>} onClick={async () => {
            if (!id) return
            try {
              const currentLang = i18n.language || 'tr'
              const resp = await http.get<ArrayBuffer>(`/api/accidents/${id}/pdf?lang=${currentLang}`, {
                responseType: 'arraybuffer'
              })
              const blob = new Blob([resp.data], { type: 'application/pdf' })
              const url = window.URL.createObjectURL(blob)
              const a = document.createElement('a')
              const filenamePrefix = currentLang === 'tr' ? 'kaza-raporu' : 
                                     currentLang === 'en' ? 'accident-report' : 
                                     'otchet-o-neschastnom-sluchae'
              a.download = `${filenamePrefix}-${id.substring(0, 8)}.pdf`
              a.href = url
              a.click()
              window.URL.revokeObjectURL(url)
              message.success(t('accidents.pdfDownloaded'))
            } catch (e: any) {
              console.error('PDF download error:', e)
              message.error(getErrorMessage(e, t('accidents.pdfError')))
            }
          }}>
            {t('accidents.downloadPdf')}
          </Button>
          {canEdit ? (
            <Button onClick={() => setEditMode((x) => !x)}>{editMode ? t('common.view') : t('common.edit')}</Button>
          ) : null}
          <Button onClick={() => nav('/accidents')}>{t('common.back')}</Button>
        </Space>
      </Space>

      <Card loading={loading}>
        <Form form={basicForm} layout="vertical" style={{ display: editMode ? 'block' : 'none' }}>
            <Form.Item label={t('accidents.groupCompanyName')} name="groupCompanyName">
              <Input />
            </Form.Item>

            <Form.Item label={t('common.dateTime')} name="occurredAt">
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item label={t('accidents.location')} name="location">
              <Input />
            </Form.Item>

            <Form.Item label={t('accidents.responsiblePerson')} name="responsiblePerson">
              <Input />
            </Form.Item>

            <Form.Item label={t('accidents.estimatedCost')} name="estimatedCost">
              <Input />
            </Form.Item>

            <Form.Item label={t('accidents.workRelated')} name="workRelated">
              <Select
                options={[
                  { value: true, label: t('accidents.workRelatedYes') },
                  { value: false, label: t('accidents.workRelatedNo') }
                ]}
                placeholder={t('accidents.workRelated')}
              />
            </Form.Item>

            <Form.Item label={t('accidents.workDuringAccident')} name="workDuringAccident">
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder={t('accidents.workDuringAccident')}
                options={Object.entries((t('accidents.workDuringAccidentOptions', { returnObjects: true }) as Record<string, string>) || {}).map(([_, label]) => ({ value: label, label }))}
              />
            </Form.Item>
            <Form.Item label={t('accidents.accidentClassification')} name="accidentClassification" rules={[{ required: true }]}>
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder={t('accidents.accidentClassification')}
                options={Object.entries((t('accidents.accidentFormOptions', { returnObjects: true }) as Record<string, string>) || {}).map(([_, label]) => ({ value: label, label }))}
              />
            </Form.Item>
            <Form.Item label={t('accidents.potentialLevel')} name="potentialLevel" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item label={t('accidents.description')} name="description">
              <Input.TextArea rows={4} />
            </Form.Item>

            <Divider>{t('accidents.excelFields')}</Divider>
            
            <Form.Item label={t('accidents.area')} name="area">
              <Select
                options={[
                  { value: 'Saha İçinde', label: t('accidents.areaInside') },
                  { value: 'Saha Dışında', label: t('accidents.areaOutside') },
                  { value: 'Yaşam alanı', label: t('accidents.areaLiving') }
                ]}
                placeholder={t('accidents.area')}
              />
            </Form.Item>

            <Form.Item label={t('accidents.hazardSource')} name="hazardSource">
              <Select
                options={Object.entries({
                  VEHICLE_USE: t('accidents.hazardSourceOptions.VEHICLE_USE'),
                  HOT_OBJECT_CONTACT: t('accidents.hazardSourceOptions.HOT_OBJECT_CONTACT'),
                  NAIL_PUNCTURE: t('accidents.hazardSourceOptions.NAIL_PUNCTURE'),
                  OTHER: t('accidents.hazardSourceOptions.OTHER'),
                  SMOKE_GAS: t('accidents.hazardSourceOptions.SMOKE_GAS'),
                  FALLING_MATERIALS: t('accidents.hazardSourceOptions.FALLING_MATERIALS'),
                  HAND_TOOL_USE: t('accidents.hazardSourceOptions.HAND_TOOL_USE'),
                  ELECTRIC_SHOCK: t('accidents.hazardSourceOptions.ELECTRIC_SHOCK'),
                  FOREIGN_OBJECT_EYE: t('accidents.hazardSourceOptions.FOREIGN_OBJECT_EYE'),
                  MOVING_OBJECT_HIT: t('accidents.hazardSourceOptions.MOVING_OBJECT_HIT'),
                  CRUSHED_BETWEEN: t('accidents.hazardSourceOptions.CRUSHED_BETWEEN'),
                  SCAFFOLD_COLLAPSE: t('accidents.hazardSourceOptions.SCAFFOLD_COLLAPSE'),
                  LIFTING_CARRYING: t('accidents.hazardSourceOptions.LIFTING_CARRYING'),
                  SLIP_TRIP_FALL: t('accidents.hazardSourceOptions.SLIP_TRIP_FALL'),
                  CHEMICALS: t('accidents.hazardSourceOptions.CHEMICALS'),
                  SPARK: t('accidents.hazardSourceOptions.SPARK'),
                  MACHINE_EQUIPMENT_USE: t('accidents.hazardSourceOptions.MACHINE_EQUIPMENT_USE'),
                  MATERIAL_TRANSPORT: t('accidents.hazardSourceOptions.MATERIAL_TRANSPORT'),
                  STAIR_FALL: t('accidents.hazardSourceOptions.STAIR_FALL'),
                  RADIATION: t('accidents.hazardSourceOptions.RADIATION'),
                  FIXED_OBJECT_HIT: t('accidents.hazardSourceOptions.FIXED_OBJECT_HIT'),
                  COLD: t('accidents.hazardSourceOptions.COLD'),
                  FIRE_EXPLOSION: t('accidents.hazardSourceOptions.FIRE_EXPLOSION'),
                  FALL_FROM_HEIGHT: t('accidents.hazardSourceOptions.FALL_FROM_HEIGHT')
                }).map(([key, label]) => ({ value: key, label }))}
                placeholder={t('accidents.hazardSource')}
              />
            </Form.Item>

            <Form.Item label={t('accidents.injuredBodyPart')} name="injuredBodyPart">
              <Select
                options={Object.entries({
                  HAND_FINGER: t('accidents.bodyPartOptions.HAND_FINGER'),
                  WRIST: t('accidents.bodyPartOptions.WRIST'),
                  ARM_SHOULDER: t('accidents.bodyPartOptions.ARM_SHOULDER'),
                  HEAD: t('accidents.bodyPartOptions.HEAD'),
                  EYE: t('accidents.bodyPartOptions.EYE'),
                  FACE: t('accidents.bodyPartOptions.FACE'),
                  THROAT: t('accidents.bodyPartOptions.THROAT'),
                  NECK: t('accidents.bodyPartOptions.NECK'),
                  BACK: t('accidents.bodyPartOptions.BACK'),
                  SPINE: t('accidents.bodyPartOptions.SPINE'),
                  TOOTH: t('accidents.bodyPartOptions.TOOTH'),
                  CHEST: t('accidents.bodyPartOptions.CHEST'),
                  EAR: t('accidents.bodyPartOptions.EAR'),
                  FOOT_TOE: t('accidents.bodyPartOptions.FOOT_TOE'),
                  LEG: t('accidents.bodyPartOptions.LEG'),
                  INTERNAL_ORGANS: t('accidents.bodyPartOptions.INTERNAL_ORGANS'),
                  GENITAL: t('accidents.bodyPartOptions.GENITAL'),
                  PSYCHOLOGICAL: t('accidents.bodyPartOptions.PSYCHOLOGICAL'),
                  OTHER: t('accidents.bodyPartOptions.OTHER')
                }).map(([key, label]) => ({ value: key, label }))}
                placeholder={t('accidents.injuredBodyPart')}
              />
            </Form.Item>

            <Form.Item label={t('accidents.injuryType')} name="injuryType">
              <Select
                options={Object.entries({
                  CUT_LACERATION: t('accidents.injuryTypeOptions.CUT_LACERATION'),
                  FRACTURE_CRACK: t('accidents.injuryTypeOptions.FRACTURE_CRACK'),
                  DISLOCATION: t('accidents.injuryTypeOptions.DISLOCATION'),
                  AMPUTATION: t('accidents.injuryTypeOptions.AMPUTATION'),
                  BURN: t('accidents.injuryTypeOptions.BURN'),
                  ABRASION: t('accidents.injuryTypeOptions.ABRASION'),
                  CRUSH_BRUISE: t('accidents.injuryTypeOptions.CRUSH_BRUISE'),
                  PUNCTURE_NAIL: t('accidents.injuryTypeOptions.PUNCTURE_NAIL'),
                  SPRAIN_STRAIN: t('accidents.injuryTypeOptions.SPRAIN_STRAIN'),
                  FOREIGN_OBJECT_EYE: t('accidents.injuryTypeOptions.FOREIGN_OBJECT_EYE'),
                  WELDING_UV_EYE: t('accidents.injuryTypeOptions.WELDING_UV_EYE'),
                  ELECTRIC_SHOCK: t('accidents.injuryTypeOptions.ELECTRIC_SHOCK'),
                  IRRITATION: t('accidents.injuryTypeOptions.IRRITATION'),
                  ALLERGIC_REACTION: t('accidents.injuryTypeOptions.ALLERGIC_REACTION'),
                  OTHER: t('accidents.injuryTypeOptions.OTHER')
                }).map(([key, label]) => ({ value: key, label }))}
                placeholder={t('accidents.injuryType')}
              />
            </Form.Item>

            <Divider>{t('accidents.injuredPersonInfo')}</Divider>

            <Form.Item label={t('accidents.injuredPersonAge')} name="injuredPersonAge">
              <InputNumber min={0} max={150} style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item label={t('accidents.injuredPersonProfession')} name="injuredPersonProfession">
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder={t('accidents.injuredPersonProfession')}
                options={Object.entries((t('accidents.professionOptions', { returnObjects: true }) as Record<string, string>) || {}).map(([_, label]) => ({ value: label, label }))}
              />
            </Form.Item>

            <Form.Item label={t('accidents.injuredPersonGender')} name="injuredPersonGender">
              <Select
                options={[
                  { value: 'Erkek', label: t('accidents.gender.male') },
                  { value: 'Kadın', label: t('accidents.gender.female') }
                ]}
              />
            </Form.Item>

            <Form.Item label={t('accidents.injuredPersonNationality')} name="injuredPersonNationality">
              <Select
                allowClear
                placeholder={t('accidents.injuredPersonNationality')}
                options={Object.entries((t('accidents.nationalityOptions', { returnObjects: true }) as Record<string, string>) || {}).map(([_, label]) => ({ value: label, label }))}
              />
            </Form.Item>

            <Form.Item label={t('accidents.injuredPersonCompany')} name="injuredPersonCompany">
              <Input />
            </Form.Item>

            <Divider>{t('accidents.actionsTaken')}</Divider>
            <ActionsTable
              value={actions}
              onChange={setActions}
              employees={employees.map((e) => ({ id: e.employeeId, firstName: e.firstName, lastName: e.lastName }))}
              injuredList={(accident?.injured ?? []).map((p) => ({ id: p.employeeId, label: `${p.firstName} ${p.lastName}` }))}
              t={t}
            />

            <Form.Item label={t('accidents.employeeRegistrationNo')} name="employeeRegistrationNo">
              <Input placeholder={t('accidents.employeeRegistrationNo')} />
            </Form.Item>

            <Form.Item label={t('accidents.supervisorEmployee')}>
              <Select
                value={supervisorEmployeeId}
                options={employees.map((e) => ({ value: e.employeeId, label: `${e.firstName} ${e.lastName}` }))}
                onChange={setSupervisorEmployeeId}
                allowClear
                placeholder={t('accidents.supervisorEmployee')}
              />
            </Form.Item>

            <Form.Item label={t('accidents.timePeriod')} name="timePeriod">
              <Select
                options={Object.entries({
                  '0800_1200': t('accidents.timePeriodOptions.0800_1200'),
                  '1200_1600': t('accidents.timePeriodOptions.1200_1600'),
                  '1600_2000': t('accidents.timePeriodOptions.1600_2000'),
                  '2000_2400': t('accidents.timePeriodOptions.2000_2400'),
                  '2400_0400': t('accidents.timePeriodOptions.2400_0400'),
                  '0400_0800': t('accidents.timePeriodOptions.0400_0800')
                }).map(([key, label]) => ({ value: key.replace('_', '*'), label }))}
                placeholder={t('accidents.timePeriod')}
              />
            </Form.Item>

            <Space>
              <Button
                type="primary"
                onClick={async () => {
                  if (!id) return
                  const values = await basicForm.validateFields()

                  // Rapor PDF'inde ad soyad görünsün: formData'daki injuredPersonName UUID ise isme çevir
                  const rawName = formData?.injuredPersonName
                  const looksLikeUuid = typeof rawName === 'string' && /^[0-9a-f-]{20,}$/i.test(rawName)
                  const firstInjuredName = accident.injured?.length
                    ? `${accident.injured[0].firstName} ${accident.injured[0].lastName}`
                    : null
                  const empById = rawName ? employees.find((e) => e.employeeId === rawName) : null
                  const resolvedInjuredName =
                    looksLikeUuid && (firstInjuredName || empById)
                      ? (firstInjuredName ?? `${empById!.firstName} ${empById!.lastName}`)
                      : rawName

                  await http.put(`/api/accidents/${id}`, {
                    projectId: accident.projectId,
                    accidentTypeId: accident.accidentTypeId,
                    occurredAt: values.occurredAt ? values.occurredAt.toISOString() : null,
                    location: values.location || null,
                    accidentClass: accident.accidentClass, // Keep backend enum value
                    potentialLevel: values.potentialLevel,
                    description: values.description || null,
                    formDataJson: JSON.stringify({
                      ...formData,
                      projectName: values.projectName || null,
                      accidentClassification: values.accidentClassification || null,
                      injuredPersonName: resolvedInjuredName ?? formData?.injuredPersonName
                    }),
                    rootCauseDataJson: JSON.stringify(rootCause ?? emptyRootCause()),
                    injuredEmployeeIds: injuredIds,
                    keyPersonEmployeeIds: keyPersonIds,
                    status: accident.status,
                    // Excel-based fields
                    area: values.area || null,
                    hazardSource: values.hazardSource || null,
                    injuredBodyPart: values.injuredBodyPart || null,
                    injuryType: values.injuryType || null,
                    employeeRegistrationNo: values.employeeRegistrationNo || null,
                    supervisorEmployeeId: supervisorEmployeeId || null,
                    timePeriod: values.timePeriod || null,
                    // New report template fields
                    groupCompanyName: values.groupCompanyName || null,
                    responsiblePerson: values.responsiblePerson || null,
                    estimatedCost: values.estimatedCost || null,
                    workRelated: values.workRelated !== undefined ? values.workRelated : true,
                    workDuringAccident: values.workDuringAccident || null,
                    injuredPersonAge: values.injuredPersonAge || null,
                    injuredPersonProfession: values.injuredPersonProfession || null,
                    injuredPersonGender: values.injuredPersonGender || null,
                    injuredPersonNationality: values.injuredPersonNationality || null,
                    injuredPersonCompany: values.injuredPersonCompany || null,
                    actionsTakenJson: JSON.stringify(actions),
                    preparedByUserId: user?.userId || null,
                    preparedAt: dayjs().toISOString()
                  })
                  message.success(t('accidents.updateSuccess'))
                  const refreshed = await http.get<Accident>(`/api/accidents/${id}`)
                  setAccident(refreshed.data)
                  setEditMode(false)
                }}
              >
                {t('common.save')}
              </Button>
              <Button
                onClick={() => {
                  setEditMode(false)
                  setFormData(JSON.parse(accident.formDataJson || '{}'))
                  setRootCause(safeParseRootCause(accident.rootCauseDataJson))
                  try {
                    const actionsData = JSON.parse(accident.actionsTakenJson || '[]')
                    setActions(Array.isArray(actionsData) ? actionsData : [])
                  } catch {
                    setActions([])
                  }
                }}
              >
                {t('common.cancel')}
              </Button>
            </Space>
        </Form>
        {!editMode && (() => {
          const parsedFormData = JSON.parse(accident.formDataJson || '{}')
          const projectName = accident.projectName || parsedFormData.projectName || '-'
          const classification = accident.classification
            || parsedFormData.accidentClassification
            || accident.accidentClass
          const displayLocation = accident.location || accident.area || '-'
          const actions = (() => {
            try {
              const data = JSON.parse(accident.actionsTakenJson || '[]')
              return Array.isArray(data) ? data : []
            } catch {
              return []
            }
          })()

          return (
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label={t('accidents.incidentNo')}>{accident.incidentNo ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.groupCompanyName')}>{accident.groupCompanyName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.project')}>{projectName}</Descriptions.Item>
            <Descriptions.Item label={t('common.dateTime')}>
              {accident.occurredAt ? dayjs(accident.occurredAt).format('YYYY-MM-DD HH:mm') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label={t('accidents.timePeriod')}>
              {accident.timePeriod ? formatTimePeriod(accident.timePeriod) : '-'}
            </Descriptions.Item>
            <Descriptions.Item label={t('accidents.location')}>{displayLocation}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.area')}>{accident.area ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.responsiblePerson')}>{accident.responsiblePerson ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.estimatedCost')}>{accident.estimatedCost ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.workRelated')}>
              {accident.workRelated !== null && accident.workRelated !== undefined
                ? (accident.workRelated ? t('accidents.workRelatedYes') : t('accidents.workRelatedNo'))
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label={t('accidents.workDuringAccident')}>{accident.workDuringAccident ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.hazardSource')}>{accident.hazardSource ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.accidentClassification')}>
              {classificationLabel(metadata, classification) || classification || '-'}
            </Descriptions.Item>

            <Divider>{t('accidents.injuredPersonInfo')}</Divider>
            <Descriptions.Item label={t('accidents.employeeRegistrationNo')}>{accident.employeeRegistrationNo ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.personName')}>{accident.personName ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuredPersonAge')}>{accident.injuredPersonAge ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuredPersonProfession')}>{accident.injuredPersonProfession ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuredPersonGender')}>{accident.injuredPersonGender ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuredPersonNationality')}>{accident.injuredPersonNationality ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuredPersonCompany')}>{accident.injuredPersonCompany ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.workSupervisor')}>{accident.workSupervisor ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.durationOnProject')}>{accident.durationOnProject ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.durationInRole')}>{accident.durationInRole ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuryType')}>{accident.injuryType ?? '-'}</Descriptions.Item>
            <Descriptions.Item label={t('accidents.injuredBodyPart')}>{accident.injuredBodyPart ?? '-'}</Descriptions.Item>
            {accident.emergencyNotificationSent != null && (
              <Descriptions.Item label={t('accidents.emergencyNotificationSent')}>
                {accident.emergencyNotificationSent ? t('common.yes') : t('common.no')}
              </Descriptions.Item>
            )}
            {accident.vehiclePlate && <Descriptions.Item label={t('accidents.vehiclePlate')}>{accident.vehiclePlate}</Descriptions.Item>}
            {accident.vehicleType && <Descriptions.Item label={t('accidents.vehicleType')}>{accident.vehicleType}</Descriptions.Item>}
            {accident.supervisorEmployee && (
              <Descriptions.Item label={t('accidents.supervisorEmployee')}>
                {accident.supervisorEmployee.firstName} {accident.supervisorEmployee.lastName}
              </Descriptions.Item>
            )}

            <Divider>{t('accidents.description')}</Divider>
            <Descriptions.Item label={t('accidents.description')}>
              <Typography.Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>
                {accident.description ?? '-'}
              </Typography.Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label={t('accidents.potentialLevel')}>
              <Tag color="volcano">{accident.potentialLevel}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label={t('common.status')}>{accident.status}</Descriptions.Item>

            <Divider>{t('accidents.actionsTaken')}</Divider>
            <Descriptions.Item label={t('accidents.actionsTaken')}>
              <ActionsTableReadOnly value={actions} t={t} />
            </Descriptions.Item>

            {(accident.preparedBy || accident.preparedAt) && (
              <>
                {accident.preparedBy && (
                  <Descriptions.Item label={t('accidents.preparedBy')}>
                    {accident.preparedBy.firstName} {accident.preparedBy.lastName}
                  </Descriptions.Item>
                )}
                {accident.preparedAt && (
                  <Descriptions.Item label={t('accidents.preparedAt')}>
                    {dayjs(accident.preparedAt).format('YYYY-MM-DD HH:mm')}
                  </Descriptions.Item>
                )}
              </>
            )}
          </Descriptions>
          )
        })()}


        <Divider>{t('accidents.injuredPersonnel')}</Divider>
        <Space wrap>
          {(accident.injured ?? []).map((p) => (
            <Tag key={`inj-${p.employeeId}`} color="red">
              {p.firstName} {p.lastName}{' '}
              <a
                onClick={(e) => {
                  e.preventDefault()
                  openTraining(p)
                }}
              >
                ({t('training.title')})
              </a>
            </Tag>
          ))}
          {accident.injured?.length ? null : <Typography.Text type="secondary">{t('common.noData')}</Typography.Text>}
        </Space>

        <Divider>{t('accidents.keyPersonnel')}</Divider>
        <Space wrap>
          {(accident.keyPeople ?? []).map((p) => (
            <Tag key={`key-${p.employeeId}`} color="gold">
              {p.firstName} {p.lastName}{' '}
              <a
                onClick={(e) => {
                  e.preventDefault()
                  openTraining(p)
                }}
              >
                ({t('training.title')})
              </a>
            </Tag>
          ))}
          {accident.keyPeople?.length ? null : <Typography.Text type="secondary">{t('common.noData')}</Typography.Text>}
        </Space>

        <Divider>{t('accidents.rootCauseAnalysis')}</Divider>
        {(accident.directCauses?.length || accident.rootCauses?.length) && metadata ? (
          <>
            <CauseSelectorEditor
              title={t('accidents.directCauses')}
              groups={metadata.directCauseGroups}
              value={accident.directCauses || []}
              onChange={() => {}}
              readOnly
            />
            <CauseSelectorEditor
              title={t('accidents.rootCauses')}
              groups={metadata.rootCauseGroups}
              value={accident.rootCauses || []}
              onChange={() => {}}
              readOnly
            />
          </>
        ) : (
          <RootCauseEditor value={rootCauseValue} onChange={setRootCause} readOnly={!editMode} />
        )}

        <Divider>{t('accidents.photosFiles')}</Divider>
        <Space direction="vertical" style={{ width: '100%' }}>
          {canUploadFiles ? (
            <Upload customRequest={uploadRequest} showUploadList={false}>
              <Button>{t('accidents.upload')}</Button>
            </Upload>
          ) : (
            <Typography.Text type="secondary">{t('accidents.noUploadPermission')}</Typography.Text>
          )}

          <Table
            rowKey="id"
            size="small"
            dataSource={files}
            pagination={false}
            columns={[
              {
                title: t('accidents.fileDisplayName'),
                key: 'displayName',
                render: (_, r: FileObj) => (
                  <Input
                    placeholder={t('accidents.fileDisplayNamePlaceholder')}
                    value={r.displayName ?? ''}
                    onChange={(e) => {
                      const next = files.map((f) => (f.id === r.id ? { ...f, displayName: e.target.value } : f))
                      setFiles(next)
                    }}
                    onBlur={(e) => {
                      const name = (e.target.value || '').trim() || null
                      if (!id) return
                      http.patch(`/api/accidents/${id}/files/${r.id}/display-name`, { displayName: name }).then(() => {
                        setFiles((prev) => prev.map((f) => (f.id === r.id ? { ...f, displayName: name } : f)))
                      }).catch(() => message.error(t('common.saveError')))
                    }}
                  />
                )
              },
              { title: t('accidents.fileName'), dataIndex: 'originalFilename', key: 'name' },
              { title: t('accidents.fileSize'), dataIndex: 'sizeBytes', key: 'size' },
              {
                title: t('accidents.download'),
                key: 'dl',
                render: (_, r: FileObj) => (
                  <a
                    onClick={(e) => {
                      e.preventDefault()
                      downloadFile(r.id, r.displayName || r.originalFilename)
                    }}
                  >
                    {t('accidents.download')}
                  </a>
                )
              }
            ]}
          />
        </Space>
      </Card>

      <Drawer
        title={
          trainingEmployee ? `${t('training.info')}: ${trainingEmployee.firstName} ${trainingEmployee.lastName}` : t('training.title')
        }
        open={trainingDrawerOpen}
        onClose={() => setTrainingDrawerOpen(false)}
        width={720}
      >
        <Table
          rowKey="id"
          loading={trainingLoading}
          dataSource={trainingRows}
          columns={trainingCols as any}
          pagination={{ pageSize: 10 }}
        />
      </Drawer>
    </div>
  )
}

type Employee = { id: string; firstName: string; lastName: string }

type InjuredOption = { id: string; label: string }

function ActionsTable({ value, onChange, employees, injuredList = [], t }: { value: ActionItem[]; onChange: (v: ActionItem[]) => void; employees: Employee[]; injuredList?: InjuredOption[]; t: any }) {
  const [newAction, setNewAction] = useState<ActionItem>({ action: '', responsiblePerson: '', targetDate: null })

  const injuredOptions = useMemo(
    () => injuredList.map((x) => ({ value: x.id, label: x.label })),
    [injuredList]
  )

  const addAction = () => {
    if (!newAction.action.trim()) return
    onChange([...value, { ...newAction }])
    setNewAction({ action: '', responsiblePerson: '', targetDate: null })
  }

  const removeAction = (index: number) => {
    onChange(value.filter((_, i) => i !== index))
  }

  const updateAction = (index: number, field: keyof ActionItem, val: any) => {
    const updated = [...value]
    updated[index] = { ...updated[index], [field]: val }
    if (field === 'injuredEmployeeId') {
      const opt = injuredList.find((x) => x.id === val)
      updated[index].injuredLabel = opt ? opt.label : undefined
    }
    onChange(updated)
  }

  const columns = [
    ...(injuredList.length > 0
      ? [{
          title: t('accidents.injuredPersonnel'),
          dataIndex: 'injuredEmployeeId',
          key: 'injured',
          render: (id: string | undefined, record: ActionItem, index: number) => (
            <Select
              allowClear
              placeholder={t('accidents.injuredPersonnel')}
              value={id || undefined}
              onChange={(v) => updateAction(index, 'injuredEmployeeId', v || undefined)}
              options={injuredOptions}
              style={{ width: '100%', minWidth: 140 }}
            />
          )
        }]
      : []),
    {
      title: t('accidents.action'),
      dataIndex: 'action',
      key: 'action',
      render: (text: string, record: ActionItem, index: number) => (
        <Input
          value={text}
          onChange={(e) => updateAction(index, 'action', e.target.value)}
          placeholder={t('accidents.action')}
        />
      )
    },
    {
      title: t('accidents.responsiblePerson'),
      dataIndex: 'responsiblePerson',
      key: 'responsiblePerson',
      render: (text: string, record: ActionItem, index: number) => (
        <Input
          value={text}
          onChange={(e) => updateAction(index, 'responsiblePerson', e.target.value)}
          placeholder={t('accidents.responsiblePerson')}
        />
      )
    },
    {
      title: t('accidents.targetDate'),
      dataIndex: 'targetDate',
      key: 'targetDate',
      render: (date: string | null, record: ActionItem, index: number) => (
        <DatePicker
          value={date ? dayjs(date) : null}
          onChange={(d) => updateAction(index, 'targetDate', d ? d.toISOString() : null)}
          style={{ width: '100%' }}
        />
      )
    },
    {
      title: t('common.actions'),
      key: 'actions',
      render: (_: any, record: ActionItem, index: number) => (
        <Button type="link" danger onClick={() => removeAction(index)}>
          {t('common.delete')}
        </Button>
      )
    }
  ]

  return (
    <div>
      <Table
        dataSource={value.map((item, idx) => ({ ...item, key: idx }))}
        columns={columns}
        pagination={false}
        size="small"
      />
      <Space style={{ marginTop: 12, width: '100%' }}>
        <Input
          value={newAction.action}
          onChange={(e) => setNewAction({ ...newAction, action: e.target.value })}
          placeholder={t('accidents.action')}
          style={{ flex: 1 }}
        />
        <Input
          value={newAction.responsiblePerson}
          onChange={(e) => setNewAction({ ...newAction, responsiblePerson: e.target.value })}
          placeholder={t('accidents.responsiblePerson')}
          style={{ width: 200 }}
        />
        <DatePicker
          value={newAction.targetDate ? dayjs(newAction.targetDate) : null}
          onChange={(d) => setNewAction({ ...newAction, targetDate: d ? d.toISOString() : null })}
          placeholder={t('accidents.targetDate')}
        />
        <Button type="primary" onClick={addAction}>
          {t('common.add')}
        </Button>
      </Space>
    </div>
  )
}

function ActionsTableReadOnly({ value, t }: { value: ActionItem[]; t: any }) {
  const columns = [
    ...(value.some((a) => a.injuredLabel) ? [{ title: t('accidents.injuredPersonnel'), dataIndex: 'injuredLabel', key: 'injured' }] : []),
    {
      title: t('accidents.action'),
      dataIndex: 'action',
      key: 'action'
    },
    {
      title: t('accidents.responsiblePerson'),
      dataIndex: 'responsiblePerson',
      key: 'responsiblePerson'
    },
    {
      title: t('accidents.targetDate'),
      dataIndex: 'targetDate',
      key: 'targetDate',
      render: (date: string | null) => (date ? dayjs(date).format('YYYY-MM-DD') : '-')
    }
  ]

  if (!value || value.length === 0) {
    return <Typography.Text type="secondary">{t('common.noData')}</Typography.Text>
  }

  return (
    <Table
      dataSource={value.map((item, idx) => ({ ...item, key: idx }))}
      columns={columns}
      pagination={false}
      size="small"
    />
  )
}


