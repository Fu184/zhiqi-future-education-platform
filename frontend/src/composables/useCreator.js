import { ref } from 'vue'
import { demoStore as store } from '../stores/demo'
import { useUi } from './useUi'
export function useCreator() {
  const { go, notify } = useUi()
  const createMode = ref('Scratch')
  const selectedBlocks = ref(['当绿旗被点击', '说「你好，未来！」', '等待 1 秒'])
  const pythonCode = ref(
    'print("你好，未来！")\n\nfor step in range(3):\n    print("探索第", step + 1, "步")',
  )
  const previewText = ref('')
  const workTitle = ref('我的创意作品')
  function addBlock(text) {
    selectedBlocks.value.push(text)
    notify('积木已加入工作区')
  }
  function runPreview() {
    if (createMode.value === 'Scratch')
      previewText.value = `已连接 ${selectedBlocks.value.length} 块积木。点击保存可发布你的创作。`
    else if (createMode.value === 'Python')
      previewText.value = '代码已保存在编辑区。在线 Python 运行环境将在接入执行服务后开放。'
    else previewText.value = 'CAD / SOLIDWORKS 在线编辑器将在集成设计服务后开放。'
  }
  function saveWork() {
    if (!store.user) return go('/login')
    const title = workTitle.value.trim()
    if (!title) return notify('请先填写作品名称')
    store.works.unshift({
      id: Date.now(),
      title,
      author: store.user.name,
      authorId: store.user.id,
      type: createMode.value,
      color:
        createMode.value === 'Python'
          ? 'blue'
          : createMode.value === 'Scratch'
            ? 'purple'
            : 'orange',
      icon: createMode.value === 'Python' ? 'Code2' : 'Rocket',
      description:
        createMode.value === 'Python'
          ? pythonCode.value.slice(0, 70)
          : `${selectedBlocks.value.length} 块创意积木组成的作品`,
    })
    notify('作品已保存到本地作品展示')
    go('/works')
  }
  return {
    createMode,
    selectedBlocks,
    pythonCode,
    previewText,
    workTitle,
    addBlock,
    runPreview,
    saveWork,
  }
}
