import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Space, Table, Tag, Typography, Modal, Popconfirm, Form, Input, Select, Switch, App } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { http } from '../../../api/http'
import { useAuth } from '../../../state/auth/useAuth'

type User = {
  id: string
  username: string
  email: string
  role: 'ADMIN' | 'ISG_C' | 'YONETICI' | 'PERSONEL' | 'READ_ONLY'
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export function UsersListPage() {
  const nav = useNavigate()
  const { t } = useTranslation()
  const { user: currentUser } = useAuth()
  const { message } = App.useApp()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [modalVisible, setModalVisible] = useState(false)

  useEffect(() => {
    // Only load if user is ADMIN
    if (currentUser?.role === 'ADMIN') {
      loadUsers()
    } else {
      message.error(t('users.accessDenied'))
      nav('/')
    }
  }, [currentUser, nav, message, t])

  const loadUsers = async () => {
    setLoading(true)
    try {
      const resp = await http.get<User[]>('/api/users')
      setUsers(resp.data)
    } catch (error: any) {
      if (error.response?.status === 403) {
        message.error(t('users.accessDenied'))
        nav('/')
      } else {
        message.error(t('users.loadError'))
      }
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: string) => {
    try {
      await http.delete(`/api/users/${id}`)
      message.success(t('users.deleteSuccess'))
      loadUsers()
    } catch (error) {
      message.error(t('users.deleteError'))
    }
  }

  const columns = useMemo(
    () => [
      { title: t('auth.username'), dataIndex: 'username', key: 'username' },
      { title: t('auth.email'), dataIndex: 'email', key: 'email' },
      {
        title: t('common.role'),
        dataIndex: 'role',
        key: 'role',
        render: (role: User['role']) => <Tag color={getRoleColor(role)}>{t(`roles.${role}`)}</Tag>
      },
      {
        title: t('common.status'),
        dataIndex: 'enabled',
        key: 'enabled',
        render: (enabled: boolean) => (
          <Tag color={enabled ? 'green' : 'red'}>{enabled ? t('common.enabled') : t('common.disabled')}</Tag>
        )
      },
      {
        title: t('common.actions'),
        key: 'actions',
        render: (_: any, record: User) => (
          <Space>
            <Button
              type="link"
              icon={<EditOutlined />}
              onClick={() => {
                setEditingUser(record)
                setModalVisible(true)
              }}
            >
              {t('common.edit')}
            </Button>
            <Popconfirm
              title={t('common.confirmDelete')}
              onConfirm={() => handleDelete(record.id)}
              okText={t('common.yes')}
              cancelText={t('common.no')}
            >
              <Button type="link" danger icon={<DeleteOutlined />}>
                {t('common.delete')}
              </Button>
            </Popconfirm>
          </Space>
        )
      }
    ],
    [t]
  )

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 12 }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {t('users.title')}
        </Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => {
          setEditingUser(null)
          setModalVisible(true)
        }}>
          {t('users.newUser')}
        </Button>
      </Space>

      <Card>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={users}
          columns={columns as any}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <UserModal
        visible={modalVisible}
        user={editingUser}
        onClose={() => {
          setModalVisible(false)
          setEditingUser(null)
        }}
        onSuccess={() => {
          setModalVisible(false)
          setEditingUser(null)
          loadUsers()
        }}
      />
    </div>
  )
}

function getRoleColor(role: string): string {
  switch (role) {
    case 'ADMIN':
      return 'red'
    case 'ISG_C':
      return 'blue'
    case 'YONETICI':
      return 'orange'
    case 'PERSONEL':
      return 'green'
    case 'READ_ONLY':
      return 'default'
    default:
      return 'default'
  }
}

type UserModalProps = {
  visible: boolean
  user: User | null
  onClose: () => void
  onSuccess: () => void
}

function UserModal({ visible, user, onClose, onSuccess }: UserModalProps) {
  const { t } = useTranslation()
  const { message } = App.useApp()
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (visible) {
      if (user) {
        form.setFieldsValue({
          username: user.username,
          email: user.email,
          role: user.role,
          enabled: user.enabled,
          password: '' // Don't prefill password
        })
      } else {
        form.resetFields()
      }
    }
  }, [visible, user, form])

  const handleSubmit = async (values: any) => {
    setSaving(true)
    try {
      if (user) {
        // Update - password is optional
        const updateData = { ...values }
        if (!updateData.password || updateData.password.trim() === '') {
          delete updateData.password
        }
        await http.put(`/api/users/${user.id}`, updateData)
        message.success(t('users.updateSuccess'))
      } else {
        // Create - password is required
        await http.post('/api/users', values)
        message.success(t('users.createSuccess'))
      }
      onSuccess()
    } catch (error: any) {
      message.error(error.response?.data?.message || (user ? t('users.updateError') : t('users.createError')))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      title={user ? t('users.editUser') : t('users.newUser')}
      open={visible}
      onCancel={onClose}
      footer={null}
      width={600}
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item label={t('auth.username')} name="username" rules={[{ required: true, min: 3 }]}>
          <Input />
        </Form.Item>

        <Form.Item label={t('auth.email')} name="email" rules={[{ required: true, type: 'email' }]}>
          <Input type="email" />
        </Form.Item>

        <Form.Item
          label={t('auth.password')}
          name="password"
          rules={user ? [] : [{ required: true, min: 6, message: t('users.passwordMinLength') }]}
        >
          <Input.Password placeholder={user ? t('users.passwordPlaceholder') : ''} />
        </Form.Item>

        <Form.Item label={t('common.role')} name="role" rules={[{ required: true }]}>
          <Select
            options={[
              { value: 'ADMIN', label: t('roles.ADMIN') },
              { value: 'ISG_C', label: t('roles.ISG_C') },
              { value: 'YONETICI', label: t('roles.YONETICI') },
              { value: 'PERSONEL', label: t('roles.PERSONEL') },
              { value: 'READ_ONLY', label: t('roles.READ_ONLY') }
            ]}
          />
        </Form.Item>

        <Form.Item label={t('common.status')} name="enabled" valuePropName="checked">
          <Switch />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saving}>
              {t('common.save')}
            </Button>
            <Button onClick={onClose}>{t('common.cancel')}</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  )
}

