import React, { useMemo } from 'react'
import { Layout, Menu, Typography, Button, Select } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../state/auth/useAuth'
import {
  DashboardOutlined,
  AlertOutlined,
  LineChartOutlined,
  ExclamationCircleOutlined,
  BarChartOutlined,
  FileTextOutlined,
  CheckSquareOutlined,
  FileOutlined,
  FolderOpenOutlined,
  LogoutOutlined,
  GlobalOutlined
} from '@ant-design/icons'

const { Header, Sider, Content } = Layout

// Logo component
function Logo({ size = 'default' }: { size?: 'small' | 'default' | 'large' }) {
  const sizeMap = {
    small: { height: 32, width: 'auto' },
    default: { height: 40, width: 'auto' },
    large: { height: 48, width: 'auto' }
  }
  
  return (
    <img
      src="/logos/east contech.jpg"
      alt="IST KONTEKH"
      style={{
        ...sizeMap[size],
        objectFit: 'contain',
        marginRight: 8
      }}
      onError={(e) => {
        // Fallback if logo not found
        const target = e.target as HTMLImageElement
        target.style.display = 'none'
      }}
    />
  )
}

export function AppLayout() {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const loc = useLocation()
  const { t, i18n } = useTranslation()

  const items = useMemo(
    () => {
      const menuItems = [
        { key: '/', icon: <DashboardOutlined />, label: t('menu.dashboard') },
        { key: '/accidents', icon: <AlertOutlined />, label: t('menu.accidents') },
        { key: '/accidents/reports', icon: <LineChartOutlined />, label: t('menu.accidentReports') },
        { key: '/nonconformities', icon: <ExclamationCircleOutlined />, label: t('menu.nonconformities') },
        { key: '/nonconformance/overview', icon: <BarChartOutlined />, label: t('menu.nonconformityStats') },
        { key: '/ncr', icon: <FileTextOutlined />, label: t('menu.ncr') },
        { key: '/discipline', icon: <FileTextOutlined />, label: t('menu.discipline') },
        { key: '/audits', icon: <CheckSquareOutlined />, label: t('menu.audits') },
        { key: '/checklists', icon: <CheckSquareOutlined />, label: t('menu.checklists') },
        { key: '/audit-archive', icon: <FolderOpenOutlined />, label: t('menu.auditArchive') },
        { key: '/documents', icon: <FileOutlined />, label: t('menu.documents') }
      ]
      
      // Only show users menu for ADMIN
      if (user?.role === 'ADMIN') {
        menuItems.push({ key: '/users', icon: <FileOutlined />, label: t('menu.users') })
      }
      
      return menuItems
    },
    [t, user]
  )

  const languageOptions = [
    { value: 'tr', label: '🇹🇷 Türkçe' },
    { value: 'en', label: '🇬🇧 English' },
    { value: 'ru', label: '🇷🇺 Русский' }
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible={false}>
        <div style={{ padding: 16, color: 'white', display: 'flex', alignItems: 'center', flexWrap: 'wrap' }}>
          <Logo size="default" />
          <div style={{ flex: 1, minWidth: 0 }}>
            <Typography.Text style={{ color: 'white', fontSize: 16, fontWeight: 600, display: 'block' }} strong>
              ISG-C Portal
            </Typography.Text>
            <div style={{ fontSize: 12, opacity: 0.85, marginTop: 4 }}>{user ? t(`roles.${user.role}`) : ''}</div>
          </div>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[loc.pathname]}
          items={items}
          onClick={(e) => nav(e.key)}
        />
      </Sider>

      <Layout>
        <Header style={{ background: 'white', display: 'flex', alignItems: 'center' }}>
          <div style={{ flex: 1 }} />
          <Select
            value={i18n.language}
            onChange={(lang) => {
              i18n.changeLanguage(lang)
              localStorage.setItem('i18nextLng', lang)
            }}
            options={languageOptions}
            style={{ width: 150, marginRight: 16 }}
            suffixIcon={<GlobalOutlined />}
          />
          <Button
            icon={<LogoutOutlined />}
            onClick={async () => {
              await logout()
              nav('/login', { replace: true })
            }}
          >
            {t('auth.logout')}
          </Button>
        </Header>
        <Content style={{ background: '#E7F6ED' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}


