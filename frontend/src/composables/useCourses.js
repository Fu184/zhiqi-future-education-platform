import { computed, ref } from 'vue'
import { featuredCourses } from '../data'
import { demoStore as store } from '../stores/demo'
import { api } from '../services/api'
import { useUi } from './useUi'
export function useCourses() {
  const keyword = ref(''),
    courseCategory = ref('全部课程')
  const { dialog, go, notify } = useUi()
  const filteredCourses = computed(() =>
    featuredCourses.filter(
      (item) =>
        (courseCategory.value === '全部课程' || item.type === courseCategory.value) &&
        `${item.title}${item.summary}`.includes(keyword.value.trim()),
    ),
  )
  const currentCourse = computed(() =>
    dialog.value?.kind === 'course' ? featuredCourses.find((c) => c.id === dialog.value.id) : null,
  )
  function openCourse(id) {
    dialog.value = { kind: 'course', id }
  }
  function toggleFavorite(id) {
    const index = store.favorites.indexOf(id)
    if (index >= 0) store.favorites.splice(index, 1)
    else store.favorites.push(id)
    notify(index >= 0 ? '已取消收藏' : '已加入收藏')
  }
  async function enroll(id) {
    try {
      await api('/learning/access')
    } catch (error) {
      notify(error.message)
      if (error.status === 401) go('/login')
      return
    }
    if (!store.enrolled.includes(id)) store.enrolled.push(id)
    dialog.value = null
    notify('已加入我的学习，开始探索吧')
    go('/tasks')
  }
  return {
    keyword,
    courseCategory,
    filteredCourses,
    currentCourse,
    openCourse,
    toggleFavorite,
    enroll,
  }
}
