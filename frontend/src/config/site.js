// 只填写已经确认、允许公开的信息。未配置的字段不向访问者展示。
const value = (input) => (input || '').trim()
export const site = {
  name: '智启未来科技教育平台',
  operator: value(import.meta.env.VITE_SITE_OPERATOR),
  email: value(import.meta.env.VITE_SITE_EMAIL),
  phone: value(import.meta.env.VITE_SITE_PHONE),
  address: value(import.meta.env.VITE_SITE_ADDRESS),
  icp: value(import.meta.env.VITE_SITE_ICP),
  policeRecord: value(import.meta.env.VITE_SITE_POLICE_RECORD),
}
export const hasPublicContact = Boolean(site.email || site.phone || site.address)
