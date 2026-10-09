<script setup>
import { authStore as store } from '../stores/auth'
import { useUserManagement } from '../composables/useUserManagement'
const {
  router,
  search,
  error,
  message,
  busy,
  loading,
  page,
  total,
  parents,
  parentSearch,
  labels,
  roles,
  form,
  editor,
  visible,
  parentLabel,
  refresh,
  create,
  edit,
  save,
  confirmPurchase,
} = useUserManagement()
</script>

<template>
  <section class="container page-body user-management">
    <div class="section-top">
      <div>
        <span class="kicker">USER MANAGEMENT</span>
        <h1>用户管理</h1>
        <p>
          {{ store.user?.roleLabel }} ·
          {{ store.user?.role === 'admin' ? '管理全部平台账号' : '管理自己名下的账号' }}
        </p>
      </div>
      <div class="action-row">
        <button
          v-if="store.user?.role === 'admin'"
          class="btn btn-outline"
          @click="router.push('/admin')"
        >
          内容管理</button
        ><button
          v-if="store.user?.role === 'admin'"
          class="btn btn-outline"
          @click="router.push('/government')"
        >
          数据概览</button
        ><button class="btn btn-outline" @click="router.push('/account')">修改密码</button>
      </div>
    </div>
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <p v-if="message" class="notice-bar" role="status">{{ message }}</p>
    <div v-if="store.user && store.user.role !== 'admin'" class="notice-bar">
      {{
        store.user.role === 'principal'
          ? `老师名额 ${store.user.teacherUsed} / ${store.user.teacher_quota}，学校学生总名额 ${store.user.student_quota}（分配给老师使用）`
          : `学生名额 ${store.user.studentUsed} / ${store.user.student_quota}`
      }}
    </div>
    <div class="management-grid">
      <form class="management-card" @submit.prevent="create">
        <h2>
          创建{{
            store.user?.role === 'principal'
              ? '老师'
              : store.user?.role === 'teacher'
                ? '学生'
                : '用户'
          }}账号
        </h2>
        <label class="form-field"
          >角色<select v-model="form.role" @change="form.parentId = null">
            <option v-for="role in roles" :key="role" :value="role">{{ labels[role] }}</option>
          </select></label
        >
        <label class="form-field"
          >姓名<input v-model="form.name" required maxlength="40" autocomplete="off"
        /></label>
        <label class="form-field"
          >手机号<input
            v-model="form.phone"
            type="tel"
            required
            pattern="1[3-9][0-9]{9}"
            maxlength="11"
            autocomplete="off"
        /></label>
        <label class="form-field"
          >初始密码<input
            v-model="form.password"
            type="password"
            required
            minlength="8"
            maxlength="72"
            autocomplete="new-password"
        /></label>
        <label
          v-if="
            store.user?.role === 'admin' && ['teacher', 'ordinary', 'learner'].includes(form.role)
          "
          class="form-field"
          for="parent-account"
          >{{ form.role === 'teacher' ? '所属校长（必选）' : '所属老师（可选）'
          }}<input
            v-model="parentSearch"
            placeholder="搜索上级姓名或手机号（最多显示 50 项）"
            maxlength="100"
            aria-label="搜索上级账号"
          />
          <select id="parent-account" v-model="form.parentId" :required="form.role === 'teacher'">
            <option :value="null">{{ form.role === 'teacher' ? '请选择校长' : '独立账号' }}</option>
            <option v-for="parent in parents" :key="parent.id" :value="parent.id">
              {{ parent.name }} · {{ parent.phone }}
            </option>
          </select></label
        >
        <label v-if="form.role === 'principal'" class="form-field"
          >允许创建的老师数量<input
            v-model.number="form.teacherQuota"
            type="number"
            required
            min="0"
            max="100000"
        /></label>
        <label v-if="['principal', 'teacher'].includes(form.role)" class="form-field"
          >{{ form.role === 'principal' ? '学校学生总名额' : '分配给该老师的学生名额'
          }}<input v-model.number="form.studentQuota" type="number" required min="0" max="100000"
        /></label>
        <button class="btn btn-primary" :disabled="busy">
          {{ busy ? '提交中…' : '创建账号' }}
        </button>
      </form>
      <div class="management-card user-list">
        <div class="section-top">
          <h2>账号列表</h2>
          <button class="btn btn-outline" :disabled="loading" @click="refresh">刷新</button>
        </div>
        <input
          v-model="search"
          class="user-search"
          placeholder="搜索姓名、手机号、角色"
          aria-label="搜索账号"
        />
        <p v-if="loading" role="status">正在加载…</p>
        <div class="table-scroll">
          <table class="users-table">
            <thead>
              <tr>
                <th>用户</th>
                <th>角色 / 归属</th>
                <th>状态 / 名额</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in visible" :key="user.id">
                <td>
                  <strong>{{ user.name }}</strong
                  ><small>{{ user.phone }}</small>
                </td>
                <td>
                  {{ user.roleLabel }}<small>{{ parentLabel(user) }}</small>
                </td>
                <td>
                  {{ user.active ? '启用' : '停用'
                  }}<small v-if="user.role === 'principal'"
                    >老师 {{ user.teacherUsed }}/{{ user.teacher_quota }} · 学生总名额
                    {{ user.student_quota }}</small
                  ><small v-if="user.role === 'teacher'"
                    >学生 {{ user.studentUsed }}/{{ user.student_quota }}</small
                  >
                </td>
                <td>
                  <button class="btn btn-outline" :disabled="busy" @click="edit(user)">管理</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-if="!loading && !total" class="empty-state">暂无账号</p>
        <div class="action-row pagination">
          <button class="btn btn-outline" :disabled="page <= 1" @click="page--">上一页</button
          ><span>{{ page }} / {{ Math.max(1, Math.ceil(total / 20)) }}</span
          ><button class="btn btn-outline" :disabled="page * 20 >= total" @click="page++">
            下一页
          </button>
        </div>
      </div>
    </div>
    <div v-if="editor" class="modal-backdrop" @click.self="editor = null">
      <div
        class="modal user-editor"
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-editor-title"
      >
        <button class="modal-close" aria-label="关闭" @click="editor = null">×</button>
        <h2 id="user-editor-title">管理用户 #{{ editor.id }}</h2>
        <form @submit.prevent="save">
          <label class="form-field"
            >姓名<input v-model="editor.name" required maxlength="40" /></label
          ><label v-if="store.user?.role === 'admin'" class="form-field"
            >手机号<input
              v-model="editor.phone"
              type="tel"
              required
              pattern="1[3-9][0-9]{9}"
              maxlength="11"
          /></label>
          <label
            v-if="
              store.user?.role === 'admin' && ['ordinary', 'learner'].includes(editor.originalRole)
            "
            class="form-field"
            >学习角色<select v-model="editor.role">
              <option value="ordinary">普通用户</option>
              <option value="learner">可学习用户</option>
            </select></label
          >
          <label class="form-field"
            >重置密码（留空不修改）<input
              v-model="editor.password"
              type="password"
              minlength="8"
              maxlength="72"
              autocomplete="new-password"
          /></label>
          <label
            v-if="store.user?.role === 'admin' && editor.originalRole === 'principal'"
            class="form-field"
            >老师名额<input
              v-model.number="editor.teacherQuota"
              type="number"
              min="0"
              max="100000"
              required
          /></label>
          <label
            v-if="
              ['principal', 'teacher'].includes(editor.originalRole) &&
              ['admin', 'principal'].includes(store.user?.role)
            "
            class="form-field"
            >学生名额<input
              v-model.number="editor.studentQuota"
              type="number"
              min="0"
              max="100000"
              required
          /></label>
          <label class="checkbox-field"
            ><input v-model="editor.active" type="checkbox" />启用账号</label
          >
          <p v-if="error" class="form-error" role="alert">{{ error }}</p>
          <button class="btn btn-primary" :disabled="busy">保存修改</button>
        </form>
        <form
          v-if="
            store.user?.role === 'admin' && ['ordinary', 'learner'].includes(editor.originalRole)
          "
          class="purchase-confirm"
          @submit.prevent="confirmPurchase"
        >
          <h3>确认学习资料购买</h3>
          <label class="form-field"
            >实际购买凭证编号<input
              v-model="editor.reference"
              required
              minlength="3"
              maxlength="100"
              placeholder="已核实的订单或凭证编号" /></label
          ><button class="btn btn-outline" :disabled="busy">确认购买并开通学习</button>
        </form>
      </div>
    </div>
  </section>
</template>
