import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../services/api'
import { assertPassword } from '../services/password'
import { authStore as store } from '../stores/auth'

export function useUserManagement() {
  const router = useRouter()
  const users = ref([]),
    search = ref(''),
    error = ref(''),
    message = ref(''),
    busy = ref(false),
    loading = ref(false)
  const page = ref(1),
    total = ref(0),
    parents = ref([]),
    parentSearch = ref('')
  let searchTimer,
    parentTimer,
    listRequest = 0,
    parentRequest = 0,
    listAbort,
    parentAbort
  const labels = {
    ordinary: '普通用户',
    learner: '可学习用户',
    principal: '校长',
    teacher: '老师',
    admin: '最大管理员',
    government: '政府账号',
  }
  const roles = computed(() =>
    store.user?.role === 'admin'
      ? Object.keys(labels)
      : store.user?.role === 'principal'
        ? ['teacher']
        : ['learner'],
  )
  const form = ref(emptyForm())
  const editor = ref(null)
  function emptyForm() {
    return {
      name: '',
      phone: '',
      password: '',
      role: roles.value[0],
      parentId: null,
      teacherQuota: 0,
      studentQuota: 0,
    }
  }
  const visible = users
  const parentLabel = (user) =>
    user.parentName || (user.parent_id ? `账号 #${user.parent_id}` : '独立账号')
  async function refresh() {
    const request = ++listRequest
    listAbort?.abort()
    listAbort = new AbortController()
    loading.value = true
    error.value = ''
    try {
      const query = new URLSearchParams({
        page: page.value,
        pageSize: 20,
        search: search.value.trim(),
      })
      const data = await api(`/users?${query}`, { signal: listAbort.signal })
      if (request !== listRequest) return
      users.value = data.users
      total.value = data.total
      store.user = data.me
      const lastPage = Math.max(1, Math.ceil(data.total / 20))
      if (page.value > lastPage) page.value = lastPage
    } catch (e) {
      if (e.name !== 'AbortError' && request === listRequest) error.value = e.message
    } finally {
      if (request === listRequest) loading.value = false
    }
  }
  async function refreshParents() {
    const request = ++parentRequest
    parentAbort?.abort()
    parents.value = []
    if (
      store.user?.role !== 'admin' ||
      !['teacher', 'ordinary', 'learner'].includes(form.value.role)
    )
      return
    parentAbort = new AbortController()
    try {
      const query = new URLSearchParams({
        role: form.value.role === 'teacher' ? 'principal' : 'teacher',
        search: parentSearch.value.trim(),
      })
      const data = await api(`/users/parents?${query}`, { signal: parentAbort.signal })
      if (request === parentRequest) parents.value = data
    } catch (e) {
      if (e.name !== 'AbortError' && request === parentRequest) error.value = e.message
    }
  }
  watch(page, refresh)
  watch(search, () => {
    clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
      if (page.value !== 1) page.value = 1
      else refresh()
    }, 250)
  })
  watch(
    [() => form.value.role, parentSearch],
    () => {
      form.value.parentId = null
      clearTimeout(parentTimer)
      parentTimer = setTimeout(refreshParents, 250)
    },
    { immediate: true },
  )
  onUnmounted(() => {
    clearTimeout(searchTimer)
    clearTimeout(parentTimer)
    listAbort?.abort()
    parentAbort?.abort()
  })
  async function mutate(action, success) {
    if (busy.value) return
    busy.value = true
    error.value = ''
    message.value = ''
    try {
      await action()
      message.value = success
      await refresh()
      return true
    } catch (e) {
      error.value = e.message
      return false
    } finally {
      busy.value = false
    }
  }
  async function create() {
    const data = {
      name: form.value.name,
      phone: form.value.phone,
      password: form.value.password,
      role: form.value.role,
    }
    if (store.user.role === 'admin') {
      if (form.value.role === 'teacher' || ['ordinary', 'learner'].includes(form.value.role))
        data.parentId = form.value.parentId
      if (form.value.role === 'principal') data.teacherQuota = form.value.teacherQuota
    }
    if (['principal', 'teacher'].includes(form.value.role))
      data.studentQuota = form.value.studentQuota
    if (
      await mutate(() => {
        assertPassword(data.password)
        return api('/users', { method: 'POST', body: data })
      }, '账号创建成功，请将初始密码交给本人并提醒修改。')
    )
      form.value = emptyForm()
  }
  function edit(user) {
    editor.value = {
      id: user.id,
      name: user.name,
      phone: user.phone,
      role: user.role,
      originalRole: user.role,
      active: Boolean(user.active),
      password: '',
      teacherQuota: user.teacher_quota,
      studentQuota: user.student_quota,
      reference: '',
    }
  }
  async function save() {
    const item = editor.value
    const data = { name: item.name, active: item.active }
    if (item.password) data.password = item.password
    if (store.user.role === 'admin') {
      data.phone = item.phone
      if (item.role !== item.originalRole) data.role = item.role
      if (item.originalRole === 'principal') data.teacherQuota = item.teacherQuota
    }
    if (
      ['principal', 'teacher'].includes(item.originalRole) &&
      ['admin', 'principal'].includes(store.user.role)
    )
      data.studentQuota = item.studentQuota
    if (
      await mutate(() => {
        if (data.password) assertPassword(data.password)
        return api(`/users/${item.id}`, { method: 'PATCH', body: data })
      }, '用户信息已更新')
    )
      editor.value = null
  }
  async function confirmPurchase() {
    const item = editor.value
    if (
      await mutate(
        () =>
          api(`/users/${item.id}/purchase`, {
            method: 'POST',
            body: { reference: item.reference },
          }),
        '购买已确认，学习权限已开通',
      )
    )
      editor.value = null
  }
  onMounted(refresh)
  return {
    router,
    users,
    search,
    error,
    message,
    busy,
    loading,
    page,
    total,
    parents,
    parentSearch,
    labels,
    roles,
    form,
    editor,
    visible,
    parentLabel,
    refresh,
    create,
    edit,
    save,
    confirmPurchase,
  }
}
