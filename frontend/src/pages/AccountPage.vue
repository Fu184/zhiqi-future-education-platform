<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authStore as store } from '../stores/auth'
import { api } from '../services/api'
import { assertPassword } from '../services/password'
const router = useRouter()
const form = ref({ oldPassword: '', newPassword: '' }),
  error = ref(''),
  message = ref(''),
  busy = ref(false)
async function submit() {
  busy.value = true
  error.value = ''
  message.value = ''
  try {
    assertPassword(form.value.newPassword)
    message.value = (await api('/auth/password', { method: 'POST', body: form.value })).message
    form.value = { oldPassword: '', newPassword: '' }
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <section class="container page-body">
    <button
      class="btn btn-outline"
      @click="
        router.push(
          store.user?.role === 'government'
            ? '/government'
            : ['admin', 'principal', 'teacher'].includes(store.user?.role)
              ? '/users'
              : '/',
        )
      "
    >
      返回
    </button>
    <h1>账户设置</h1>
    <p>{{ store.user?.name }} · {{ store.user?.roleLabel }}</p>
    <form class="account-form" @submit.prevent="submit">
      <label class="form-field"
        >原密码<input
          v-model="form.oldPassword"
          type="password"
          required
          minlength="8"
          maxlength="72"
          autocomplete="current-password" /></label
      ><label class="form-field"
        >新密码<input
          v-model="form.newPassword"
          type="password"
          required
          minlength="8"
          maxlength="72"
          autocomplete="new-password"
      /></label>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <p v-if="message" role="status">{{ message }}</p>
      <button class="btn btn-primary" :disabled="busy">保存密码</button>
    </form>
  </section>
</template>
