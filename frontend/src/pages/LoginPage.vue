<script setup>
import { onUnmounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ArrowRight } from 'lucide-vue-next'
import { authStore as store } from '../stores/auth'
import { api } from '../services/api'
import { assertPassword } from '../services/password'
import { landingPage, setSession } from '../services/auth'

const router = useRouter(),
  route = useRoute()
const mode = ref('login')
const form = ref({ phone: '', password: '', name: '', code: '' })
const error = ref(''),
  smsNote = ref(''),
  busy = ref(false),
  sending = ref(false),
  countdown = ref(0)
let timer
onUnmounted(() => clearInterval(timer))
function selectMode(value) {
  mode.value = value
  error.value = ''
}
async function sendCode() {
  error.value = ''
  smsNote.value = ''
  sending.value = true
  try {
    const data = await api('/auth/sms', { method: 'POST', body: { phone: form.value.phone } })
    smsNote.value = data.devCode
      ? `本地测试模式：验证码 ${data.devCode}（没有发送短信）`
      : '验证码已发送，5 分钟内有效。'
    countdown.value = data.retryAfter
    clearInterval(timer)
    timer = setInterval(() => {
      if (--countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    error.value = e.message
  } finally {
    sending.value = false
  }
}
async function submit() {
  if (busy.value) return
  error.value = ''
  busy.value = true
  try {
    assertPassword(form.value.password)
    const input =
      mode.value === 'register'
        ? form.value
        : { phone: form.value.phone, password: form.value.password }
    setSession((await api(`/auth/${mode.value}`, { method: 'POST', body: input })).user)
    const redirect = route.query.redirect
    await router.push(
      store.user.role === 'government'
        ? '/government'
        : typeof redirect === 'string' &&
            redirect.startsWith('/') &&
            !redirect.startsWith('//') &&
            redirect !== '/login'
          ? redirect
          : landingPage(store.user),
    )
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="login-page">
    <div class="login-visual">
      <div>
        <span class="kicker light">WELCOME TO ZHIQI FUTURE</span>
        <h1>每一次好奇<br />都是未来的起点</h1>
        <p>探索 · 创造 · 成长</p>
      </div>
    </div>
    <div class="login-form-wrap">
      <form class="login-form" @submit.prevent="submit">
        <div class="brand-mini">智启未来</div>
        <h2>{{ mode === 'login' ? '欢迎回来' : '注册学习账号' }}</h2>
        <div class="auth-tabs">
          <button
            type="button"
            :class="{ selected: mode === 'login' }"
            @click="selectMode('login')"
          >
            登录</button
          ><button
            type="button"
            :class="{ selected: mode === 'register' }"
            @click="selectMode('register')"
          >
            注册
          </button>
        </div>
        <label v-if="mode === 'register'"
          >姓名 / 昵称<input
            v-model="form.name"
            required
            maxlength="40"
            autocomplete="nickname"
            placeholder="请输入姓名或昵称"
        /></label>
        <label
          >手机号<input
            v-model="form.phone"
            type="tel"
            required
            pattern="1[3-9][0-9]{9}"
            maxlength="11"
            autocomplete="username"
            placeholder="请输入手机号"
        /></label>
        <label
          >密码<input
            v-model="form.password"
            type="password"
            required
            minlength="8"
            maxlength="72"
            :autocomplete="mode === 'register' ? 'new-password' : 'current-password'"
            placeholder="至少 8 个字符"
        /></label>
        <label v-if="mode === 'register'"
          >短信验证码
          <div class="sms-input">
            <input
              v-model="form.code"
              required
              inputmode="numeric"
              pattern="[0-9]{6}"
              maxlength="6"
              autocomplete="one-time-code"
              placeholder="六位验证码"
            /><button type="button" :disabled="sending || countdown > 0" @click="sendCode">
              {{ sending ? '发送中…' : countdown > 0 ? `${countdown} 秒后重发` : '获取验证码' }}
            </button>
          </div>
        </label>
        <p v-if="smsNote && mode === 'register'" class="notice-bar" role="status">{{ smsNote }}</p>
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <button type="submit" class="btn btn-primary" :disabled="busy">
          {{ busy ? '提交中…' : mode === 'login' ? '登录平台' : '验证并注册' }}
          <ArrowRight :size="17" />
        </button>
        <small v-if="mode === 'register'"
          >自行注册为普通用户，购买学习资料后开通学习权限。机构与政府账号由管理员创建。</small
        >
        <p class="login-information-links">
          <RouterLink to="/privacy">隐私说明</RouterLink><span> · </span
          ><RouterLink to="/site-notice">网站使用说明</RouterLink>
        </p>
      </form>
    </div>
  </section>
</template>

<style scoped>
.login-information-links {
  margin: 8px 0 0;
  color: #687d96;
  font-size: 12px;
  line-height: 1.8;
}
.login-information-links a:hover {
  color: #214f7d;
  text-decoration: underline;
  text-underline-offset: 3px;
}
</style>
