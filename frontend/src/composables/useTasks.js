import { computed, ref } from 'vue'
import { demoStore as store } from '../stores/demo'
import { useUi } from './useUi'
export function useTasks() {
  const taskFilter = ref('全部任务'),
    { notify } = useUi()
  const filteredTasks = computed(() =>
    store.tasks.filter(
      (item) => taskFilter.value === '全部任务' || item.status === taskFilter.value,
    ),
  )
  function markTask(task) {
    task.status = task.status === '已完成' ? '进行中' : '已完成'
    notify(task.status === '已完成' ? '任务已标记为完成' : '任务已重新开始')
  }
  return { taskFilter, filteredTasks, markTask }
}
