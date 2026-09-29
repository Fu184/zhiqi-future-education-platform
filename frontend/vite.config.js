import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: process.env.GITHUB_PAGES === 'true' ? '/zhiqi-future-education-platform/' : '/',
  plugins: [vue()],
  server: { host: '0.0.0.0' },
})
