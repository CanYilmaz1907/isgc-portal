import React from 'react'
import { Result, Button } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

export function ForbiddenPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  return (
    <Result
      status="403"
      title="403"
      subTitle={t('common.accessDenied')}
      extra={
        <Button type="primary" onClick={() => nav('/')}>
          {t('common.backToHome')}
        </Button>
      }
    />
  )
}


