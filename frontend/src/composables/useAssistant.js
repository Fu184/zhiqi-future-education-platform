import { ref } from 'vue'
export function useAssistant() {
  const aiInput = ref('')
  const aiMessages = ref([
    {
      role: 'assistant',
      text: '你好！我是智启 AI 学习助手。你可以问我课程知识，也可以请我帮你梳理学习思路。',
    },
  ])
  function sendAi(text = aiInput.value) {
    const prompt = text.trim()
    if (!prompt) return
    aiMessages.value.push({ role: 'user', text: prompt })
    const answer = /Python|编程|代码/i.test(prompt)
      ? '可以从一个小目标开始：先明确输入和输出，再用变量保存数据，最后用条件或循环完成逻辑。你想做什么小程序？'
      : /机器人|传感器/.test(prompt)
        ? '设计机器人任务时，可以按“感知 → 判断 → 行动”拆解。先列出要用的传感器，再写下每种情况对应的动作。'
        : /课程|学什么|推荐/.test(prompt)
          ? '如果你刚开始学习，推荐先体验人工智能启蒙和 Python 编程课程；喜欢动手制作的话，可以看看机器人探索与三维设计。'
          : '我们可以先把问题拆成几个小步骤：明确目标、列出已知条件、尝试一个最简单的方案，再观察结果并改进。你可以补充更多细节。'
    aiMessages.value.push({ role: 'assistant', text: answer })
    aiInput.value = ''
  }
  return { aiInput, aiMessages, sendAi }
}
