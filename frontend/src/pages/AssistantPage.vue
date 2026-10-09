<script setup>
import { ArrowUpRight, Bot, CircleHelp, Send } from 'lucide-vue-next'

import { useAssistant } from '../composables/useAssistant'
const { aiInput, aiMessages, sendAi } = useAssistant()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">AI LEARNING COMPANION</span>
      <h1>AI 学习助手</h1>
      <p>灵感卡住时，换一种方式继续探索。</p>
    </div>
    <Bot :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body assistant-layout">
    <aside class="assistant-intro">
      <div class="assistant-avatar">
        <Bot :size="32" />
      </div>
      <h2>你好，探索者！</h2>
      <p>把问题讲给我听，我们一起梳理思路、寻找下一步。</p>
      <span class="demo-note">
        <CircleHelp :size="16" /> 当前为本地演示问答，正式 AI 服务需接入模型接口。
      </span>
      <div class="suggestions">
        <strong>试试问我</strong
        ><button
          v-for="prompt in [
            '推荐一门适合初学者的课程',
            'Python 编程该怎么入门？',
            '如何设计机器人任务？',
          ]"
          :key="prompt"
          @click="sendAi(prompt)"
        >
          {{ prompt }}
          <ArrowUpRight :size="16" />
        </button>
      </div>
    </aside>
    <div class="chat-panel">
      <div class="chat-head">
        <span class="online-dot"></span><strong>智启 AI 学习助手</strong><small>在线演示</small>
      </div>
      <div class="chat-messages">
        <div v-for="(message, i) in aiMessages" :key="i" :class="['chat-message', message.role]">
          <div class="chat-bubble">{{ message.text }}</div>
        </div>
      </div>
      <form class="chat-input" @submit.prevent="sendAi()">
        <input v-model="aiInput" placeholder="输入你的问题，开始探索..." /><button
          type="submit"
          aria-label="发送消息"
        >
          <Send :size="19" />
        </button>
      </form>
    </div>
  </section>
</template>
