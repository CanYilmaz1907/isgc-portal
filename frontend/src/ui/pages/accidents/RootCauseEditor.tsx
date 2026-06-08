import React from 'react'
import { Card, Checkbox, Collapse, Typography } from 'antd'
import { useTranslation } from 'react-i18next'
import {
  DIRECT_CAUSES_PROCEDURES_OPTIONS,
  DIRECT_CAUSES_EQUIPMENT_OPTIONS,
  DIRECT_CAUSES_PROTECTION_MISSING_OPTIONS,
  DIRECT_CAUSES_ATTENTION_OPTIONS,
  DIRECT_CAUSES_PROTECTION_SYSTEMS_OPTIONS,
  DIRECT_CAUSES_TOOLS_EQUIPMENT_OPTIONS,
  DIRECT_CAUSES_WORKPLACE_OPTIONS,
  ROOT_CAUSES_SKILL_LEVEL_OPTIONS,
  ROOT_CAUSES_MOTIVATION_OPTIONS,
  ROOT_CAUSES_PHYSICAL_CAPABILITY_OPTIONS,
  ROOT_CAUSES_PHYSICAL_INADEQUACY_OPTIONS,
  ROOT_CAUSES_JOB_PLANNING_OPTIONS,
  ROOT_CAUSES_LEADERSHIP_OPTIONS,
  ROOT_CAUSES_PURCHASING_OPTIONS,
  ROOT_CAUSES_ENGINEERING_OPTIONS,
  ROOT_CAUSES_MAINTENANCE_OPTIONS,
  ROOT_CAUSES_TOOLS_EQUIPMENT_ROOT_OPTIONS,
  ROOT_CAUSES_STANDARDS_PROCEDURES_OPTIONS,
  ROOT_CAUSES_PERSONAL_FACTORS_OPTIONS,
  ROOT_CAUSES_JOB_FACTORS_OPTIONS,
  type RootCauseData
} from './rootCause'

type Props = {
  value: RootCauseData
  onChange: (next: RootCauseData) => void
  readOnly?: boolean
}

export function RootCauseEditor({ value, onChange, readOnly }: Props) {
  const { t } = useTranslation()

  const uniq = (arr: string[]) => Array.from(new Set(arr))

  const updateDirectGroup = (
    target: 'unsafeBehavior' | 'unsafeCondition',
    groupOptions: readonly string[],
    selected: string[]
  ) => {
    const groupSet = new Set(groupOptions)
    const nextUnsafeBehavior =
      target === 'unsafeBehavior'
        ? uniq([...value.unsafeBehavior.filter((x) => !groupSet.has(x)), ...selected])
        : value.unsafeBehavior.filter((x) => !groupSet.has(x))
    const nextUnsafeCondition =
      target === 'unsafeCondition'
        ? uniq([...value.unsafeCondition.filter((x) => !groupSet.has(x)), ...selected])
        : value.unsafeCondition.filter((x) => !groupSet.has(x))
    onChange({ ...value, unsafeBehavior: nextUnsafeBehavior, unsafeCondition: nextUnsafeCondition })
  }

  const updateRootGroup = (
    target: 'personalFactors' | 'workFactors',
    groupOptions: readonly string[],
    selected: string[]
  ) => {
    const groupSet = new Set(groupOptions)
    const nextPersonal =
      target === 'personalFactors'
        ? uniq([...value.personalFactors.filter((x) => !groupSet.has(x)), ...selected])
        : value.personalFactors.filter((x) => !groupSet.has(x))
    const nextWork =
      target === 'workFactors'
        ? uniq([...value.workFactors.filter((x) => !groupSet.has(x)), ...selected])
        : value.workFactors.filter((x) => !groupSet.has(x))
    onChange({ ...value, personalFactors: nextPersonal, workFactors: nextWork })
  }

  const selectedFrom = (arr: string[], groupOptions: readonly string[]) => {
    const set = new Set(groupOptions)
    return arr.filter((x) => set.has(x))
  }

  return (
    <div>
      {/* Direkt Sebepler (tam hiyerarşi) */}
      <Card size="small" title={t('accidents.directCauses')} style={{ marginBottom: 16 }}>
        <Collapse defaultActiveKey={['direct-1']} accordion={false} ghost>
          <Collapse.Panel header="1) Prosedürler & Kurallar" key="direct-1">
            <Checkbox.Group
              value={selectedFrom(value.unsafeBehavior, DIRECT_CAUSES_PROCEDURES_OPTIONS)}
              onChange={(v) => updateDirectGroup('unsafeBehavior', DIRECT_CAUSES_PROCEDURES_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={DIRECT_CAUSES_PROCEDURES_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="2) Alet ve Ekipman Kullanımı" key="direct-2">
            <Checkbox.Group
              value={selectedFrom(value.unsafeBehavior, DIRECT_CAUSES_EQUIPMENT_OPTIONS)}
              onChange={(v) => updateDirectGroup('unsafeBehavior', DIRECT_CAUSES_EQUIPMENT_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={DIRECT_CAUSES_EQUIPMENT_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="3) Koruyucu Yöntemlerin Kullanılmaması" key="direct-3">
            <Checkbox.Group
              value={selectedFrom(value.unsafeBehavior, DIRECT_CAUSES_PROTECTION_MISSING_OPTIONS)}
              onChange={(v) =>
                updateDirectGroup('unsafeBehavior', DIRECT_CAUSES_PROTECTION_MISSING_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={DIRECT_CAUSES_PROTECTION_MISSING_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="4) Dikkat ve Farkındalık Eksikliği" key="direct-4">
            <Checkbox.Group
              value={selectedFrom(value.unsafeBehavior, DIRECT_CAUSES_ATTENTION_OPTIONS)}
              onChange={(v) => updateDirectGroup('unsafeBehavior', DIRECT_CAUSES_ATTENTION_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={DIRECT_CAUSES_ATTENTION_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="5) Koruma Sistemleri" key="direct-5">
            <Checkbox.Group
              value={selectedFrom(value.unsafeCondition, DIRECT_CAUSES_PROTECTION_SYSTEMS_OPTIONS)}
              onChange={(v) =>
                updateDirectGroup('unsafeCondition', DIRECT_CAUSES_PROTECTION_SYSTEMS_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={DIRECT_CAUSES_PROTECTION_SYSTEMS_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="6) Alet, Ekipman ve Araç" key="direct-6">
            <Checkbox.Group
              value={selectedFrom(value.unsafeBehavior, DIRECT_CAUSES_TOOLS_EQUIPMENT_OPTIONS)}
              onChange={(v) =>
                updateDirectGroup('unsafeBehavior', DIRECT_CAUSES_TOOLS_EQUIPMENT_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={DIRECT_CAUSES_TOOLS_EQUIPMENT_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="7) Çalışma Ortamı" key="direct-7">
            <Checkbox.Group
              value={selectedFrom(value.unsafeCondition, DIRECT_CAUSES_WORKPLACE_OPTIONS)}
              onChange={(v) =>
                updateDirectGroup('unsafeCondition', DIRECT_CAUSES_WORKPLACE_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={DIRECT_CAUSES_WORKPLACE_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
        </Collapse>
      </Card>

      {/* Kök Sebepler (tam hiyerarşi) */}
      <Card size="small" title={t('accidents.rootCauses')}>
        <Collapse defaultActiveKey={['root-1']} accordion={false} ghost>
          <Collapse.Panel header="1) Beceri Düzeyi" key="root-1">
            <Checkbox.Group
              value={selectedFrom(value.personalFactors, ROOT_CAUSES_SKILL_LEVEL_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('personalFactors', ROOT_CAUSES_SKILL_LEVEL_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_SKILL_LEVEL_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="2) Motivasyon" key="root-2">
            <Checkbox.Group
              value={selectedFrom(value.personalFactors, ROOT_CAUSES_MOTIVATION_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('personalFactors', ROOT_CAUSES_MOTIVATION_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_MOTIVATION_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="3) Fiziksel Kapasite" key="root-3">
            <Checkbox.Group
              value={selectedFrom(value.personalFactors, ROOT_CAUSES_PHYSICAL_CAPABILITY_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('personalFactors', ROOT_CAUSES_PHYSICAL_CAPABILITY_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_PHYSICAL_CAPABILITY_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="4) Fiziksel Yetersizlik" key="root-4">
            <Checkbox.Group
              value={selectedFrom(value.personalFactors, ROOT_CAUSES_PHYSICAL_INADEQUACY_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('personalFactors', ROOT_CAUSES_PHYSICAL_INADEQUACY_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_PHYSICAL_INADEQUACY_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="5) İşin Planlanması" key="root-5">
            <Checkbox.Group
              value={selectedFrom(value.personalFactors, ROOT_CAUSES_JOB_PLANNING_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('personalFactors', ROOT_CAUSES_JOB_PLANNING_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_JOB_PLANNING_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="6) Liderlik & Denetim" key="root-6">
            <Checkbox.Group
              value={selectedFrom(value.personalFactors, ROOT_CAUSES_LEADERSHIP_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('personalFactors', ROOT_CAUSES_LEADERSHIP_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_LEADERSHIP_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="7) Satın Alma" key="root-7">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_PURCHASING_OPTIONS)}
              onChange={(v) => updateRootGroup('workFactors', ROOT_CAUSES_PURCHASING_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={ROOT_CAUSES_PURCHASING_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="8) Mühendislik" key="root-8">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_ENGINEERING_OPTIONS)}
              onChange={(v) => updateRootGroup('workFactors', ROOT_CAUSES_ENGINEERING_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={ROOT_CAUSES_ENGINEERING_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="9) Bakım" key="root-9">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_MAINTENANCE_OPTIONS)}
              onChange={(v) => updateRootGroup('workFactors', ROOT_CAUSES_MAINTENANCE_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={ROOT_CAUSES_MAINTENANCE_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="10) Alet & Ekipman" key="root-10">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_TOOLS_EQUIPMENT_ROOT_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('workFactors', ROOT_CAUSES_TOOLS_EQUIPMENT_ROOT_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_TOOLS_EQUIPMENT_ROOT_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="11) Standartlar & Prosedürler" key="root-11">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_STANDARDS_PROCEDURES_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('workFactors', ROOT_CAUSES_STANDARDS_PROCEDURES_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_STANDARDS_PROCEDURES_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="12) Kişisel Faktörler" key="root-12">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_PERSONAL_FACTORS_OPTIONS)}
              onChange={(v) =>
                updateRootGroup('workFactors', ROOT_CAUSES_PERSONAL_FACTORS_OPTIONS, v as string[])
              }
              disabled={!!readOnly}
              options={ROOT_CAUSES_PERSONAL_FACTORS_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
          <Collapse.Panel header="13) İş Faktörleri" key="root-13">
            <Checkbox.Group
              value={selectedFrom(value.workFactors, ROOT_CAUSES_JOB_FACTORS_OPTIONS)}
              onChange={(v) => updateRootGroup('workFactors', ROOT_CAUSES_JOB_FACTORS_OPTIONS, v as string[])}
              disabled={!!readOnly}
              options={ROOT_CAUSES_JOB_FACTORS_OPTIONS.map((o) => ({ label: `• ${o}`, value: o }))}
            />
          </Collapse.Panel>
        </Collapse>
      </Card>
    </div>
  )
}
