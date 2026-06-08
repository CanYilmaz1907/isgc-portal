import type { ThemeConfig } from 'antd'

// Color scheme based on RAL codes from Excel file
// Green colors: #059d74 (dark), #2DBF81 (medium), #E7F6ED (light)
// Gray colors: #adaeb0 (light), #808082 (medium)
// Graphic colors: #0ACC86, #343D42, #07A069, #4B585E, #697C84, #D3D3D3
export const appTheme: ThemeConfig = {
  token: {
    // Primary color - Medium Green (#2DBF81) from Green section
    colorPrimary: '#2DBF81',
    colorSuccess: '#2DBF81',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#0ACC86', // Bright teal from Graphic Colors
    
    // Background colors
    colorBgContainer: '#ffffff',
    colorBgElevated: '#ffffff',
    colorBgLayout: '#E7F6ED', // Light mint green from Green section
    
    // Text colors
    colorText: '#343D42', // Dark slate gray from Graphic Colors
    colorTextSecondary: '#808082', // Medium gray
    
    // Border colors
    colorBorder: '#adaeb0', // Light gray
    colorBorderSecondary: '#D3D3D3', // Light gray from Graphic Colors
    
    // Grey tones
    colorFill: '#E7F6ED', // Light mint green
    colorFillSecondary: '#D3D3D3', // Light gray
    colorFillTertiary: '#ffffff',
    
    // Border radius
    borderRadius: 6,
    
    // Font
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    fontSize: 14,
    
    // Sider (sidebar) colors - Dark slate gray from Graphic Colors
    colorBgBase: '#343D42',
  },
  components: {
    Layout: {
      siderBg: '#343D42', // Dark slate gray from Graphic Colors
      headerBg: '#ffffff',
      bodyBg: '#E7F6ED', // Light mint green background
    },
    Menu: {
      darkItemBg: '#343D42',
      darkItemSelectedBg: '#2DBF81', // Medium green for selected menu item
      darkItemHoverBg: '#4B585E', // Dark cool gray from Graphic Colors
      darkSubMenuItemBg: '#343D42',
    },
    Button: {
      primaryColor: '#ffffff',
      borderRadius: 6,
    },
    Card: {
      borderRadius: 8,
      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.09)',
    },
    Input: {
      borderRadius: 6,
      activeBorderColor: '#2DBF81',
      hoverBorderColor: '#0ACC86',
    },
    Select: {
      borderRadius: 6,
      activeBorderColor: '#2DBF81',
    },
  },
  algorithm: undefined, // Will be set in main.tsx
}

