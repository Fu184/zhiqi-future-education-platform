import { demoStore as store } from '../stores/demo'
import { toRef } from 'vue'
import { useUi } from './useUi'
export function useShop() {
  const cart = toRef(store, 'cart'),
    { notify } = useUi()
  function addCart(id) {
    if (!cart.value.includes(id)) cart.value.push(id)
    notify('已加入选课清单')
  }
  return { cart, addCart }
}
