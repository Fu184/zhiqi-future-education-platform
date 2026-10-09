import { ref } from 'vue'
import { demoStore as store } from '../stores/demo'
import { newsTabs } from '../config/portal'
import { useUi } from './useUi'
export function useContentAdmin() {
  const { notify } = useUi()
  const adding = ref(false)
  const adminTab = ref('新闻管理')
  const formTitle = ref('')
  const formDescription = ref('')
  const formCategory = ref('平台动态')
  function addAdminItem() {
    const title = formTitle.value.trim()
    if (!title) return notify('请填写名称')
    if (adminTab.value === '新闻管理')
      store.news.unshift({
        id: Date.now(),
        title,
        category: formCategory.value,
        date: new Date().toISOString().slice(0, 10).replaceAll('-', '.'),
        excerpt: formDescription.value.trim() || '点击查看详细内容。',
      })
    if (adminTab.value === '任务管理')
      store.tasks.unshift({
        id: Date.now(),
        title,
        course: '平台任务',
        due: '待安排',
        status: '未开始',
        description: formDescription.value.trim() || '请查看任务要求。',
      })
    if (adminTab.value === '作品管理')
      store.works.unshift({
        id: Date.now(),
        title,
        author: store.user?.name || '管理员',
        authorId: store.user?.id,
        type: '展示作品',
        color: 'blue',
        icon: 'Sparkles',
        description: formDescription.value.trim() || '创意作品',
      })
    adding.value = false
    formTitle.value = ''
    formDescription.value = ''
    notify('新增成功')
  }
  function removeAdminItem(id) {
    const list =
      adminTab.value === '新闻管理'
        ? store.news
        : adminTab.value === '任务管理'
          ? store.tasks
          : store.works
    const index = list.findIndex((item) => item.id === id)
    if (index >= 0) {
      list.splice(index, 1)
      notify('已删除')
    }
  }
  return {
    adding,
    adminTab,
    formTitle,
    formDescription,
    formCategory,
    newsTabs,
    addAdminItem,
    removeAdminItem,
  }
}
