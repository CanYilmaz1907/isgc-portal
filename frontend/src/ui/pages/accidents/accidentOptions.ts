import type { TFunction } from 'i18next'

export function accidentClassificationOptions(t: TFunction) {
  const opts = (t('accidents.accidentFormOptions', { returnObjects: true }) as Record<string, string>) || {}
  return Object.entries(opts).map(([_, label]) => ({ value: label, label }))
}

export function hazardSourceOptions(t: TFunction) {
  const hazard = (t('accidents.hazardSourceOptions', { returnObjects: true }) as Record<string, string>) || {}
  const form = (t('accidents.accidentFormOptions', { returnObjects: true }) as Record<string, string>) || {}
  const fromHazard = Object.entries(hazard).map(([value, label]) => ({ value, label }))
  const fromForm = Object.entries(form).map(([_, label]) => ({ value: label, label }))
  const seen = new Set<string>()
  return [...fromHazard, ...fromForm].filter((o) => {
    if (seen.has(o.value)) return false
    seen.add(o.value)
    return true
  })
}
