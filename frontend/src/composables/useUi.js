import { computed, ref, watch, inject, provide, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore as store } from '../stores/auth'
import { navGroups } from '../config/portal'
const uiKey = Symbol('platform-ui')
export function provideUi() {
  const route = useRoute(),
    router = useRouter()
  const page = computed(() => route.path)
  const mobileOpen = ref(false),
    dialog = ref(null),
    toast = ref('')
  let timer
  function go(path) {
    mobileOpen.value = false
    return router.push(path)
  }
  function notify(message) {
    clearTimeout(timer)
    toast.value = message
    timer = setTimeout(() => {
      toast.value = ''
    }, 3000)
  }
  const isNavActive = (item) => (navGroups[item.path] || [item.path]).includes(page.value)
  watch(
    () => route.fullPath,
    () => {
      mobileOpen.value = false
      dialog.value = null
    },
  )
  watch(
    () => store.user?.id,
    () => {
      dialog.value = null
      toast.value = ''
    },
  )
  onUnmounted(() => clearTimeout(timer))
  const ui = { route, page, mobileOpen, dialog, toast, go, notify, isNavActive }
  provide(uiKey, ui)
  return ui
}
export function useUi() {
  const ui = inject(uiKey)
  if (!ui) throw new Error('useUi must be used under App')
  return ui
}
