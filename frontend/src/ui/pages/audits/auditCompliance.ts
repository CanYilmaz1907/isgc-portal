export type ComplianceZone = 'red' | 'orange' | 'yellow' | 'light-green' | 'green'

export function complianceZone(percent: number): ComplianceZone {
  if (percent >= 90) return 'green'
  if (percent >= 80) return 'light-green'
  if (percent >= 65) return 'yellow'
  if (percent >= 50) return 'orange'
  return 'red'
}

export function complianceColor(zone: ComplianceZone): string {
  switch (zone) {
    case 'green': return '#389e0d'
    case 'light-green': return '#52c41a'
    case 'yellow': return '#faad14'
    case 'orange': return '#fa8c16'
    default: return '#cf1322'
  }
}

export function complianceTagColor(zone: ComplianceZone): string {
  switch (zone) {
    case 'green': return 'green'
    case 'light-green': return 'lime'
    case 'yellow': return 'gold'
    case 'orange': return 'orange'
    default: return 'red'
  }
}

export function scoreOptionFromResult(score: number, maxScore: number, applicable: boolean): string {
  if (!applicable) return 'N_A'
  if (maxScore <= 0) return '1'
  const ratio = score / maxScore
  const puan = Math.max(1, Math.min(5, Math.round(ratio * 5)))
  return String(puan)
}

export function categorySeries(itemNo: number): number {
  if (itemNo >= 400) return 400
  if (itemNo >= 300) return 300
  if (itemNo >= 200) return 200
  return 100
}
