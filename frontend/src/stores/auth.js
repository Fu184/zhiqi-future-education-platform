import { reactive } from 'vue'
// 身份仅由后端 HttpOnly 会话验证；不写入浏览器存储。
export const authStore = reactive({ user: null, sessionError: '' })
