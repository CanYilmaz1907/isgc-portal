import React, { useState } from 'react'
import { Button, Card, Form, Input, Typography, Select } from 'antd'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../state/auth/useAuth'
import { useNavigate } from 'react-router-dom'
import { GlobalOutlined } from '@ant-design/icons'

// Logo component - tries different logo files
function Logo({ size = 'large' }: { size?: 'small' | 'default' | 'large' }) {
  const sizeMap = {
    small: { height: 40, width: 'auto' },
    default: { height: 60, width: 'auto' },
    large: { height: 180, width: 'auto', maxWidth: '400px' }
  }
  
  const [currentLogoIndex, setCurrentLogoIndex] = useState(0)
  
  // Try logos in order - these should be in frontend/public/logos/
  const logos = [
    '/logos/east contech.jpg',
    '/logos/east contech 2.jpg',
    '/logos/east contech 3.jpg'
  ]
  
  const handleError = () => {
    if (currentLogoIndex < logos.length - 1) {
      setCurrentLogoIndex(currentLogoIndex + 1)
    }
  }
  
  return (
    <img
      key={currentLogoIndex}
      src={logos[currentLogoIndex]}
      alt=""
      onError={handleError}
      style={{
        ...sizeMap[size],
        objectFit: 'contain',
        marginBottom: 16,
        display: 'block',
        width: '100%',
        maxWidth: '300px'
      }}
    />
  )
}

export function LoginPage() {
  const { login } = useAuth()
  const nav = useNavigate()
  const { t, i18n } = useTranslation()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const languageOptions = [
    { value: 'tr', label: '🇹🇷 Türkçe' },
    { value: 'en', label: '🇬🇧 English' },
    { value: 'ru', label: '🇷🇺 Русский' }
  ]

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        padding: 24,
        background: '#E7F6ED' // Light mint green from color scheme
      }}
    >
      <Card style={{ width: 420 }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 24, width: '100%', minHeight: '180px' }}>
            <Logo size="large" />
          </div>
          <Select
            value={i18n.language}
            onChange={(lang) => {
              i18n.changeLanguage(lang)
              localStorage.setItem('i18nextLng', lang)
            }}
            options={languageOptions}
            style={{ width: 150 }}
            suffixIcon={<GlobalOutlined />}
          />
        </div>
        <Typography.Paragraph type="secondary" style={{ textAlign: 'center', marginBottom: 24 }}>
          {t('auth.loginSubtitle')}
        </Typography.Paragraph>

        {error ? (
          <Typography.Paragraph type="danger" style={{ marginTop: 8 }}>
            {error}
          </Typography.Paragraph>
        ) : null}

        <Form
          layout="vertical"
          onFinish={async (values) => {
            setError(null)
            setLoading(true)
            try {
              await login(values.username, values.password)
              nav('/', { replace: true })
            } catch {
              setError(t('auth.loginFailed'))
            } finally {
              setLoading(false)
            }
          }}
        >
          <Form.Item label={t('auth.username')} name="username" rules={[{ required: true }]}>
            <Input autoComplete="username" />
          </Form.Item>
          <Form.Item label={t('auth.password')} name="password" rules={[{ required: true }]}>
            <Input.Password autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>
            {t('auth.login')}
          </Button>
        </Form>
      </Card>
    </div>
  )
}


