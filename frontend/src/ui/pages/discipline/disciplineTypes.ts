export type DisciplineCategory = 'CAT_0' | 'CAT_1' | 'CAT_2' | 'CAT_3'
export type DisciplineStatus = 'SOZLU_UYARI' | 'UYARI' | 'IDARI_CEZA' | 'SOZLESME_FESHI'

export type DisciplineMetadata = {
  categories: { value: string; label: string }[]
  violationTypes: Record<string, { value: string; label: string }[]>
  statuses: { value: string; label: string }[]
}

export type DisciplineRow = {
  id: string
  sequenceNo: number | null
  projectId: string | null
  occurredAt: string
  fullName: string | null
  employeeRegistrationNo: string | null
  company: string | null
  jobTitle: string | null
  workArea: string | null
  categoryLevel: DisciplineCategory | null
  violationType: string | null
  violationTypeLabel: string | null
  violationDescription: string | null
  responsiblePerson: string | null
  status: DisciplineStatus
  notes: string | null
  repeatCount: number
  repeatThresholdReached: boolean
  penaltyAmount: number | null
  severity: number
  profession: string | null
  violatingEmployeeId: string | null
  violatingEmployeeName: string | null
  violatingManagerEmployeeId: string | null
  violatingManagerEmployeeName: string | null
}

export type DisciplineSummaryStats = {
  totalWarnings: number
  totalPenalties: number
  byCategory: Record<string, number>
  byMonth: Record<string, number>
  byResponsiblePerson: Record<string, number>
  byCompany: Record<string, number>
}
