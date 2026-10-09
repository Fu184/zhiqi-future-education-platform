<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ChevronDown, ChevronRight, Menu, Search, Sparkles, UserRound } from 'lucide-vue-next'
import { authStore as store } from '../stores/auth'
import { nav } from '../config/portal'
import { useUi } from '../composables/useUi'
import { landingPage, logout } from '../services/auth'
const { route, page, mobileOpen, go, notify, isNavActive } = useUi()
const openNav = ref(null)
const header = ref(null)
function closeMenus() {
  openNav.value = null
  mobileOpen.value = false
}
function navigate(path) {
  closeMenus()
  return go(path)
}
function dismissOutside(event) {
  if (!header.value?.contains(event.target)) closeMenus()
}
function dismissEscape(event) {
  if (event.key !== 'Escape') return
  if (openNav.value) header.value?.querySelector('[aria-expanded="true"]')?.focus()
  closeMenus()
}
watch(() => route.fullPath, closeMenus)
onMounted(() => {
  document.addEventListener('pointerdown', dismissOutside)
  document.addEventListener('keydown', dismissEscape)
})
onUnmounted(() => {
  document.removeEventListener('pointerdown', dismissOutside)
  document.removeEventListener('keydown', dismissEscape)
})
async function signOut() {
  try {
    await logout()
    go('/login')
  } catch (error) {
    notify(error.message)
  }
}
</script>
<template>
  <div :class="['site-shell', { 'home-layout': page === '/' }]">
    <header v-if="store.user?.role !== 'government'" ref="header" class="site-header">
      <div class="header-inner container">
        <button class="brand" @click="navigate('/')" aria-label="返回首页">
          <span class="brand-mark"><span></span><span></span><span></span><span></span></span>
          <span class="brand-copy"><strong>智启未来</strong><small>科技教育云平台</small></span>
        </button>
        <nav class="desktop-nav" aria-label="主导航">
          <div
            v-for="item in nav"
            :key="item.path"
            :class="[
              'nav-item',
              { 'has-children': item.children, 'is-open': openNav === item.path },
            ]"
            @mouseenter="openNav = item.children ? item.path : null"
            @mouseleave="openNav = null"
          >
            <button
              :class="['nav-link', { active: isNavActive(item) }]"
              @click="navigate(item.path)"
            >
              {{ item.label }}
            </button>
            <button
              v-if="item.children"
              class="nav-disclosure"
              :aria-label="`展开${item.label}子菜单`"
              :aria-expanded="openNav === item.path"
              :aria-controls="`nav-${item.path.slice(1)}`"
              @click="openNav = openNav === item.path ? null : item.path"
            >
              <ChevronDown :size="14" />
            </button>
            <div
              v-if="item.children"
              :id="`nav-${item.path.slice(1)}`"
              class="nav-dropdown"
              :inert="openNav !== item.path"
            >
              <button
                v-for="child in item.children"
                :key="child.path"
                @click="navigate(child.path)"
              >
                {{ child.label }}
                <ChevronRight :size="14" />
              </button>
            </div>
          </div>
        </nav>
        <div class="header-actions">
          <button class="icon-button header-search" title="搜索课程" @click="navigate('/courses')">
            <Search :size="19" />
          </button>
          <button
            class="login-action"
            @click="navigate(store.user ? landingPage(store.user) : '/login')"
          >
            <UserRound :size="17" /><span>{{ store.user?.name || '登录 / 注册' }}</span>
          </button>
          <button v-if="store.user" class="login-action" @click="navigate('/account')">账户</button>
          <button v-if="store.user" class="login-action" @click="signOut">退出</button>
          <button
            class="icon-button menu-button"
            @click="mobileOpen = !mobileOpen"
            aria-label="菜单"
            :aria-expanded="mobileOpen"
            aria-controls="mobile-navigation"
          >
            <Menu :size="23" />
          </button>
        </div>
      </div>
      <div v-if="mobileOpen" id="mobile-navigation" class="mobile-menu">
        <div v-for="item in nav" :key="item.path" class="mobile-nav-group">
          <button class="mobile-nav-main" @click="navigate(item.path)">
            {{ item.label }}
            <ChevronRight :size="16" />
          </button>
          <button
            v-for="child in item.children"
            :key="child.path"
            class="mobile-nav-child"
            @click="navigate(child.path)"
          >
            {{ child.label }}
          </button>
        </div>
      </div>
    </header>

    <main>
      <p v-if="store.sessionError" class="container form-error" role="alert">
        {{ store.sessionError }}
      </p>
      <slot />
    </main>

    <footer v-if="page !== '/login' && store.user?.role !== 'government'" class="footer">
      <div class="container footer-main">
        <div>
          <div class="footer-brand">
            <span class="brand-mark"><span></span><span></span><span></span><span></span></span
            ><strong>智启未来</strong>
          </div>
          <p>科技赋能教育，创意点亮未来。<br />和每一位探索者一起，拥抱无限可能。</p>
        </div>
        <div class="footer-links">
          <div>
            <strong>探索平台</strong><button @click="navigate('/courses')">教学中心</button
            ><button @click="navigate('/create')">创作中心</button
            ><button @click="navigate('/works')">作品展示</button>
          </div>
          <div>
            <strong>关于我们</strong><button @click="navigate('/about')">平台介绍</button
            ><button @click="navigate('/news')">新闻资讯</button
            ><button @click="navigate('/contact')">联系我们</button>
          </div>
          <div>
            <strong>更多服务</strong><button @click="navigate('/organization')">学校与机构</button
            ><button @click="navigate('/shop')">商品与课程</button
            ><button @click="navigate('/admin')">管理后台</button>
          </div>
        </div>
      </div>
      <div class="container footer-bottom">
        <span>© 2026 智启未来科技教育平台. 前端演示版</span
        ><span
          >让想象力成为创造力
          <Sparkles :size="15" />
        </span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.site-shell {
  --portal-header-height: 78px;
}
.site-header {
  position: sticky;
  top: 0;
}
.header-inner {
  height: var(--portal-header-height);
}
.home-layout .site-header {
  position: fixed;
  inset: 0 0 auto;
}
.nav-item:not(.is-open) .nav-dropdown {
  transition: none;
}
.nav-disclosure {
  display: grid;
  place-items: center;
  padding: 8px 4px;
  border: 0;
  background: transparent;
  color: #777;
  border-radius: 5px;
}
.nav-disclosure svg {
  transition: transform 0.2s ease;
}
.nav-disclosure:focus-visible {
  outline: 2px solid #cf627b;
  outline-offset: 2px;
}
.nav-item.has-children .nav-link {
  padding-right: 4px;
}
.mobile-menu {
  max-height: calc(100dvh - var(--portal-header-height));
  overflow-y: auto;
}
@media (max-width: 700px) {
  .site-shell {
    --portal-header-height: 70px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .nav-disclosure svg,
  .nav-dropdown {
    transition: none;
  }
}
</style>
