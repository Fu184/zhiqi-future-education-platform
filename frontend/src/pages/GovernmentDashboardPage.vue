<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../services/api'
import { logout } from '../services/auth'
import { authStore as store } from '../stores/auth'

const stats = ref(null),
  error = ref(''),
  loading = ref(false)
const router = useRouter()
async function refresh() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await api('/government/stats')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
async function signOut() {
  try {
    await logout()
    router.push('/login')
  } catch (e) {
    error.value = e.message
  }
}
onMounted(refresh)
</script>

<template>
  <div class="government-dashboard">
    <header class="government-header">
      <div>
        <span class="kicker light">EDUCATION DATA OBSERVATORY</span>
        <h1>科技教育 · 数据概览</h1>
        <p>{{ store.user?.name }} · 只读统计视图</p>
      </div>
      <div class="action-row">
        <button class="btn btn-glass" @click="router.push('/account')">修改密码</button
        ><button class="btn btn-glass" @click="signOut">退出登录</button>
      </div>
    </header>
    <section class="container page-body">
      <div class="section-top">
        <div>
          <h2>平台运行概况</h2>
          <p>汇总平台用户与已确认购买记录。</p>
        </div>
        <button class="btn btn-outline" :disabled="loading" @click="refresh">
          {{ loading ? '更新中…' : '刷新数据' }}
        </button>
      </div>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <template v-if="stats">
        <div class="metric-grid">
          <article>
            <span>用户总量</span><strong>{{ stats.totalUsers }}</strong>
          </article>
          <article>
            <span>启用账号</span><strong>{{ stats.activeUsers }}</strong>
          </article>
          <article>
            <span>已确认购买记录</span><strong>{{ stats.confirmedPurchases }}</strong>
          </article>
        </div>
        <div class="government-panel">
          <h2>用户角色分布</h2>
          <div v-for="item in stats.roles" :key="item.role" class="distribution-row">
            <span>{{ item.label }}</span>
            <div>
              <i
                :style="{
                  width: `${stats.totalUsers ? (item.count / stats.totalUsers) * 100 : 0}%`,
                }"
              ></i>
            </div>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
        <p class="muted">更新时间：{{ new Date(stats.generatedAt).toLocaleString('zh-CN') }}</p>
      </template>
    </section>
  </div>
</template>
