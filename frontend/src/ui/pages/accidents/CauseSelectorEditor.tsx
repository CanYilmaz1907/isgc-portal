import React, { useMemo } from 'react'
import { Badge, Button, Card, Checkbox, Collapse, Space, Tag, Typography } from 'antd'
import { useTranslation } from 'react-i18next'
import type { CauseGroup, CauseSelection } from './accidentMetadata'

type Props = {
  title: string
  sectionTitle?: string
  groups: CauseGroup[]
  value: CauseSelection[]
  onChange: (next: CauseSelection[]) => void
  readOnly?: boolean
}

export function CauseSelectorEditor({ title, sectionTitle, groups, value, onChange, readOnly }: Props) {
  const { t } = useTranslation()
  const selectedCodes = useMemo(() => new Set(value.map((v) => v.code)), [value])

  const toggleItem = (item: CauseSelection, checked: boolean) => {
    if (readOnly) return
    if (checked) {
      if (selectedCodes.has(item.code)) return
      onChange([...value, item])
    } else {
      onChange(value.filter((v) => v.code !== item.code))
    }
  }

  const toggleGroup = (group: CauseGroup, selectAll: boolean) => {
    if (readOnly) return
    const groupCodes = new Set(group.items.map((i) => i.code))
    if (selectAll) {
      const merged = [...value]
      for (const item of group.items) {
        if (!selectedCodes.has(item.code)) merged.push(item)
      }
      onChange(merged)
    } else {
      onChange(value.filter((v) => !groupCodes.has(v.code)))
    }
  }

  const groupedPanels = useMemo(() => {
    const sections = new Map<string, CauseGroup[]>()
    for (const g of groups) {
      const key = g.section || 'default'
      sections.set(key, [...(sections.get(key) || []), g])
    }
    return sections
  }, [groups])

  return (
    <Card size="small" title={title} style={{ marginBottom: 16 }}>
      {[...groupedPanels.entries()].map(([section, sectionGroups]) => (
        <div key={section} style={{ marginBottom: 12 }}>
          {sectionTitle && section !== 'default' ? (
            <Typography.Text strong style={{ display: 'block', marginBottom: 8 }}>
              {section === 'ACTIONS'
                ? t('accidents.causeSectionActions')
                : section === 'CONDITIONS'
                  ? t('accidents.causeSectionConditions')
                  : section === 'PERSONAL'
                    ? t('accidents.causeSectionPersonal')
                    : section === 'JOB'
                      ? t('accidents.causeSectionJob')
                      : section}
            </Typography.Text>
          ) : null}
          <Collapse accordion={false} ghost>
            {sectionGroups.map((group) => {
              const groupSelected = group.items.filter((i) => selectedCodes.has(i.code)).length
              const allSelected = groupSelected === group.items.length && group.items.length > 0
              return (
                <Collapse.Panel
                  key={`${section}-${group.groupCode}`}
                  header={
                    <Space>
                      <span>{group.groupCode}. {group.groupName}</span>
                      {groupSelected > 0 ? <Badge count={groupSelected} /> : null}
                    </Space>
                  }
                  extra={
                    readOnly ? null : (
                      <Button
                        size="small"
                        type="link"
                        onClick={(e) => {
                          e.stopPropagation()
                          toggleGroup(group, !allSelected)
                        }}
                      >
                        {allSelected ? t('accidents.deselectAll') : t('accidents.selectAll')}
                      </Button>
                    )
                  }
                >
                  <Space direction="vertical" style={{ width: '100%' }}>
                    {group.items.map((item) => (
                      <Checkbox
                        key={item.code}
                        checked={selectedCodes.has(item.code)}
                        disabled={readOnly}
                        onChange={(e) => toggleItem(item, e.target.checked)}
                      >
                        {item.code}: {item.label}
                      </Checkbox>
                    ))}
                  </Space>
                </Collapse.Panel>
              )
            })}
          </Collapse>
        </div>
      ))}

      {value.length > 0 && (
        <div style={{ marginTop: 12 }}>
          <Typography.Text type="secondary">{t('accidents.selectedCauses')}:</Typography.Text>
          <div style={{ marginTop: 8 }}>
            {value.map((v) => (
              <Tag
                key={v.code}
                closable={!readOnly}
                onClose={() => toggleItem(v, false)}
                style={{ marginBottom: 4 }}
              >
                {v.code}: {v.label}
              </Tag>
            ))}
          </div>
        </div>
      )}
    </Card>
  )
}
