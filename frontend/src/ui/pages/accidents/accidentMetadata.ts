import { useEffect, useState } from 'react'
import { http } from '../../../api/http'

export type LookupOption = { code: string; label: string }
export type CauseSelection = { code: string; label: string }
export type CauseGroup = {
  section: string
  groupCode: string
  groupName: string
  items: CauseSelection[]
}

export type AccidentMetadata = {
  classifications: LookupOption[]
  areas: LookupOption[]
  timeRanges: LookupOption[]
  hazardSources: LookupOption[]
  injuryTypes: LookupOption[]
  injuredBodyParts: LookupOption[]
  directCauseGroups: CauseGroup[]
  rootCauseGroups: CauseGroup[]
}

export function useAccidentMetadata() {
  const [metadata, setMetadata] = useState<AccidentMetadata | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      try {
        const resp = await http.get<AccidentMetadata>('/api/accidents/metadata')
        if (mounted) setMetadata(resp.data)
      } catch (e: any) {
        if (mounted) setError(e.response?.data?.message || e.message)
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  return { metadata, loading, error }
}

export function classificationLabel(metadata: AccidentMetadata | null, code: string | null | undefined) {
  if (!code) return '-'
  const found = metadata?.classifications.find((c) => c.code === code)
  return found?.label ?? code
}
