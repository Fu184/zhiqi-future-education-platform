import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { demoStore as store } from '../stores/demo'
import { newsSections, newsTabs } from '../config/portal'
export function useNews() {
  const route = useRoute(),
    keyword = ref(''),
    newsCategory = ref('全部')
  const filteredNews = computed(() =>
    store.news.filter(
      (item) =>
        (newsCategory.value === '全部' || item.category === newsCategory.value) &&
        `${item.title}${item.excerpt}`.includes(keyword.value.trim()),
    ),
  )
  watch(
    () => route.query.section,
    (section) => {
      newsCategory.value = newsSections[section] || '全部'
    },
    { immediate: true },
  )
  return { keyword, newsCategory, filteredNews, newsTabs }
}
