import { reactive, watch } from 'vue'
import { authStore } from './auth'
import { initialNews, initialTasks, initialWorks } from '../data'
function saved(key, fallback) {
  try {
    const value = JSON.parse(localStorage.getItem(key))
    return Array.isArray(value) ? value : structuredClone(fallback)
  } catch {
    return structuredClone(fallback)
  }
}
function persist(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    /* 演示仍可在内存中使用。 */
  }
}
const privateKeys = ['tasks', 'works', 'favorites', 'enrolled', 'cart']
export const demoStore = reactive({
  news: saved('zhiqi-news', initialNews),
  tasks: structuredClone(initialTasks),
  works: structuredClone(initialWorks),
  favorites: [],
  enrolled: [],
  cart: [],
  get user() {
    return authStore.user
  },
  set user(user) {
    authStore.user = user
  },
})
let switching = false
watch(
  () => authStore.user?.id,
  (id) => {
    switching = true
    for (const key of privateKeys) {
      const fallback = key === 'tasks' ? initialTasks : key === 'works' ? initialWorks : []
      demoStore[key] = id ? saved(`zhiqi-user-${id}-${key}`, fallback) : structuredClone(fallback)
    }
    switching = false
  },
  { immediate: true, flush: 'sync' },
)
for (const key of privateKeys) {
  watch(
    () => demoStore[key],
    (value) => {
      if (!switching && authStore.user) persist(`zhiqi-user-${authStore.user.id}-${key}`, value)
    },
    { deep: true, flush: 'sync' },
  )
}
watch(
  () => demoStore.news,
  (value) => persist('zhiqi-news', value),
  { deep: true },
)
// 旧版没有用户 ID 的个人数据不自动归属任何账号，旧键保留以便手动备份。
try {
  localStorage.removeItem('zhiqi-user')
} catch {
  /* 存储不可用时继续运行。 */
}
