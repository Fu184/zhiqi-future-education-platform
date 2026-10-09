<script setup>
import { RouterView } from 'vue-router'
import { CheckCircle2 } from 'lucide-vue-next'
import PortalLayout from './layouts/PortalLayout.vue'
import ContentDetailsDialog from './components/dialogs/ContentDetailsDialog.vue'
import { provideUi } from './composables/useUi'
const { toast } = provideUi()
</script>
<template>
  <PortalLayout><RouterView :key="$route.path" /></PortalLayout>
  <ContentDetailsDialog />
  <div v-if="toast" class="toast" role="status"><CheckCircle2 :size="18" />{{ toast }}</div>
</template>
