import React from 'react'
import { Route, Routes, Navigate } from 'react-router-dom'
import { LoginPage } from '../ui/pages/LoginPage'
import { AppLayout } from '../ui/shell/AppLayout'
import { DashboardPage } from '../ui/pages/DashboardPage'
import { ForbiddenPage } from '../ui/pages/ForbiddenPage'
import { RequireAuth } from './RequireAuth'
import { AccidentsListPage } from '../ui/pages/accidents/AccidentsListPage'
import { AccidentCreatePage } from '../ui/pages/accidents/AccidentCreatePage'
import { AccidentDetailPage } from '../ui/pages/accidents/AccidentDetailPage'
import { NonconformitiesListPage } from '../ui/pages/nonconformities/NonconformitiesListPage'
import { NonconformityCreatePage } from '../ui/pages/nonconformities/NonconformityCreatePage'
import { NonconformityDetailPage } from '../ui/pages/nonconformities/NonconformityDetailPage'
import { NonconformanceOverviewPage } from '../ui/pages/overview/NonconformanceOverviewPage'
import { DisciplineListPage } from '../ui/pages/discipline/DisciplineListPage'
import { DisciplineCreatePage } from '../ui/pages/discipline/DisciplineCreatePage'
import { DisciplineDetailPage } from '../ui/pages/discipline/DisciplineDetailPage'
import { DisciplineStatsPage } from '../ui/pages/discipline/DisciplineStatsPage'
import { ChecklistsListPage } from '../ui/pages/audits/ChecklistsListPage'
import { ChecklistCreatePage } from '../ui/pages/audits/ChecklistCreatePage'
import { ChecklistDetailPage } from '../ui/pages/audits/ChecklistDetailPage'
import { AuditsListPage } from '../ui/pages/audits/AuditsListPage'
import { AuditCreatePage } from '../ui/pages/audits/AuditCreatePage'
import { AuditRunPage } from '../ui/pages/audits/AuditRunPage'
import { DocumentsPage } from '../ui/pages/documents/DocumentsPage'
import { AuditArchiveListPage } from '../ui/pages/audits/AuditArchiveListPage'
import { AuditArchiveDetailPage } from '../ui/pages/audits/AuditArchiveDetailPage'
import { AccidentReportsPage } from '../ui/pages/accidents/AccidentReportsPage'
import { NcrListPage } from '../ui/pages/ncr/NcrListPage'
import { NcrCreatePage } from '../ui/pages/ncr/NcrCreatePage'
import { NcrDetailPage } from '../ui/pages/ncr/NcrDetailPage'
import { UsersListPage } from '../ui/pages/users/UsersListPage'
import type { Role } from '../state/auth/types'

const WRITE_ROLES: Role[] = ['ADMIN', 'ISG_C']

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />

      <Route
        path="/"
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="accidents" element={<AccidentsListPage />} />
        <Route
          path="accidents/new"
          element={
            <RequireAuth roles={WRITE_ROLES}>
              <AccidentCreatePage />
            </RequireAuth>
          }
        />
        <Route path="accidents/:id" element={<AccidentDetailPage />} />
        <Route path="accidents/reports" element={<AccidentReportsPage />} />
        <Route path="nonconformities" element={<NonconformitiesListPage />} />
        <Route
          path="nonconformities/new"
          element={
            <RequireAuth roles={WRITE_ROLES}>
              <NonconformityCreatePage />
            </RequireAuth>
          }
        />
        <Route path="nonconformities/:id" element={<NonconformityDetailPage />} />
        <Route path="nonconformities/stats" element={<Navigate to="/nonconformance/overview" replace />} />
        <Route path="nonconformance/overview" element={<NonconformanceOverviewPage />} />
        <Route path="ncr" element={<NcrListPage />} />
        <Route
          path="ncr/new"
          element={
            <RequireAuth roles={WRITE_ROLES}>
              <NcrCreatePage />
            </RequireAuth>
          }
        />
        <Route path="ncr/:id" element={<NcrDetailPage />} />
        <Route path="discipline" element={<DisciplineListPage />} />
        <Route
          path="discipline/new"
          element={
            <RequireAuth roles={WRITE_ROLES}>
              <DisciplineCreatePage />
            </RequireAuth>
          }
        />
        <Route path="discipline/stats" element={<DisciplineStatsPage />} />
        <Route path="discipline/:id" element={<DisciplineDetailPage />} />

        <Route path="checklists" element={<ChecklistsListPage />} />
        <Route
          path="checklists/new"
          element={
            <RequireAuth roles={WRITE_ROLES}>
              <ChecklistCreatePage />
            </RequireAuth>
          }
        />
        <Route path="checklists/:id" element={<ChecklistDetailPage />} />

        <Route path="audits" element={<AuditsListPage />} />
        <Route
          path="audits/new"
          element={
            <RequireAuth roles={WRITE_ROLES}>
              <AuditCreatePage />
            </RequireAuth>
          }
        />
        <Route path="audits/:id" element={<AuditRunPage />} />
        <Route path="audit-archive" element={<AuditArchiveListPage />} />
        <Route path="audit-archive/:id" element={<AuditArchiveDetailPage />} />

        <Route path="documents" element={<DocumentsPage />} />
        <Route
          path="users"
          element={
            <RequireAuth roles={['ADMIN']}>
              <UsersListPage />
            </RequireAuth>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
