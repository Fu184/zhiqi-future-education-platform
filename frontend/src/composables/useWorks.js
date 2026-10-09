import { computed } from 'vue'
import { demoStore as store } from '../stores/demo'
export function useWorks() {
  const myWorks = computed(() =>
    store.user ? store.works.filter((work) => work.authorId === store.user.id) : [],
  )
  return { myWorks }
}
