import { authStore as store } from '../stores/auth'
import { api } from './api'
export const managementRoles = ['admin', 'principal', 'teacher']
export const landingPage = (user) =>
  user?.role === 'government'
    ? '/government'
    : managementRoles.includes(user?.role)
      ? '/users'
      : '/'
let checkedAt = 0,
  pending = null,
  generation = 0
export function setSession(user) {
  store.sessionError = ''
  generation++
  store.user = user
  checkedAt = Date.now()
}
export async function restoreSession({ force = false } = {}) {
  if (!force && Date.now() - checkedAt < 30000) return store.user
  if (pending) return pending
  const started = generation
  pending = (async () => {
    try {
      const data = await api('/auth/me')
      store.sessionError = ''
      if (started === generation) {
        store.user = data.user
        checkedAt = Date.now()
      }
    } catch (error) {
      if (error.status !== 401) {
        store.sessionError = '无法验证登录状态，请检查网络或后端服务后重试。'
        throw error
      }
      store.sessionError = ''
      if (started === generation) {
        store.user = null
        checkedAt = Date.now()
      }
    }
    return store.user
  })()
  try {
    return await pending
  } finally {
    pending = null
  }
}
export async function logout() {
  await api('/auth/logout', { method: 'POST', body: {} })
  setSession(null)
}
