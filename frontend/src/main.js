import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

const routes = [
  '/', '/news', '/courses', '/tasks', '/create', '/works', '/assistant',
  '/shop', '/organization', '/admin', '/about', '/contact', '/login',
].map(path => ({ path, component: App }))

const router = createRouter({ history: createWebHashHistory(), routes })
createApp(App).use(router).mount('#app')
