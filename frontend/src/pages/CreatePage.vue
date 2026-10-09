<script setup>
import {
  ArrowRight,
  Box,
  Check,
  CheckCircle2,
  Code2,
  Play,
  Plus,
  Rocket,
  WandSparkles,
  X,
} from 'lucide-vue-next'

import { useCreator } from '../composables/useCreator'
import { useUi } from '../composables/useUi'
const { go } = useUi()
const {
  createMode,
  selectedBlocks,
  pythonCode,
  previewText,
  workTitle,
  addBlock,
  runPreview,
  saveWork,
} = useCreator()
function selectMode(mode) {
  createMode.value = mode
  previewText.value = ''
}
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">MAKE IT REAL</span>
      <h1>创作中心</h1>
      <p>每一个想法，都值得被动手实现。</p>
    </div>
    <WandSparkles :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="creator-tabs">
      <button
        v-for="mode in ['Scratch', 'Python', 'CAD / SOLIDWORKS']"
        :key="mode"
        :class="{ selected: createMode === mode }"
        @click="selectMode(mode)"
      >
        {{ mode === 'Scratch' ? '图形化编程' : mode === 'Python' ? 'Python 编程' : '三维设计'
        }}<span>{{ mode }}</span>
      </button>
    </div>
    <div class="creator-shell">
      <div class="creator-top">
        <div>
          <span class="tiny-label">PROJECT / NEW</span
          ><input v-model="workTitle" aria-label="作品名称" />
        </div>
        <div>
          <button class="btn btn-outline" @click="runPreview"><Play :size="16" /> 运行预览</button
          ><button class="btn btn-primary" @click="saveWork"><Check :size="16" /> 保存作品</button>
        </div>
      </div>
      <div v-if="createMode === 'Scratch'" class="creator-workspace">
        <aside class="blocks-panel">
          <h4>积木工具箱</h4>
          <p>点击积木加入工作区</p>
          <button
            v-for="block in [
              '移动 10 步',
              '旋转 15 度',
              '说「你好！」',
              '等待 1 秒',
              '重复执行',
              '如果碰到边缘',
            ]"
            :key="block"
            @click="addBlock(block)"
          >
            {{ block }}
            <Plus :size="15" />
          </button>
        </aside>
        <div class="canvas-panel">
          <div class="canvas-head">
            <span>脚本工作区</span><span>{{ selectedBlocks.length }} 块积木</span>
          </div>
          <div class="block-stack">
            <div v-for="(block, i) in selectedBlocks" :key="i" class="script-block">
              {{ block
              }}<button @click="selectedBlocks.splice(i, 1)" aria-label="移除积木">
                <X :size="13" />
              </button>
            </div>
            <p v-if="!selectedBlocks.length">从左侧添加积木，开始你的创作</p>
          </div>
        </div>
        <div class="stage-panel">
          <div class="stage-head">舞台预览 <span>480 × 360</span></div>
          <div class="stage">
            <Rocket :size="57" :stroke-width="1.2" /><span>你的创意，从这里启航</span>
          </div>
        </div>
      </div>
      <div v-else-if="createMode === 'Python'" class="python-workspace">
        <div class="editor-panel">
          <div class="editor-tab"><Code2 :size="16" /> main.py <span>PYTHON</span></div>
          <textarea
            v-model="pythonCode"
            spellcheck="false"
            aria-label="Python 代码编辑器"
          ></textarea>
        </div>
        <div class="console-panel">
          <div class="editor-tab">控制台 <span>OUTPUT</span></div>
          <p>编写代码后保存作品。在线执行功能需接入 Python 运行服务。</p>
          <pre>{{ previewText || '> 等待运行预览...' }}</pre>
        </div>
      </div>
      <div v-else class="cad-workspace">
        <Box :size="78" :stroke-width="1.1" />
        <h3>为你的三维创意预留空间</h3>
        <p>课程和作品资料可以先在平台展示；在线 CAD 编辑将在集成相关服务后开放。</p>
        <button class="btn btn-outline" @click="go('/courses')">
          查看三维设计课程
          <ArrowRight :size="16" />
        </button>
      </div>
      <div v-if="previewText && createMode !== 'Python'" class="preview-message">
        <CheckCircle2 :size="18" /> {{ previewText }}
      </div>
    </div>
  </section>
</template>
