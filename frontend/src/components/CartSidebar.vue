<template>
  <!-- Backdrop -->
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black/50 z-40 transition-opacity"
    @click="$emit('close')"
  ></div>

  <!-- Sidebar -->
  <div
    :class="[
      'fixed top-0 right-0 h-full w-96 bg-monster-dark border-l border-monster-gray z-50 transform transition-transform duration-300',
      isOpen ? 'translate-x-0' : 'translate-x-full'
    ]"
  >
    <!-- Header -->
    <div class="flex items-center justify-between p-4 border-b border-monster-gray">
      <h2 class="text-xl font-bold text-monster-green">Shopping Cart</h2>
      <button
        @click="$emit('close')"
        class="text-gray-400 hover:text-white transition-colors"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-10 h-10 border-4 border-monster-green border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Empty Cart -->
    <div v-else-if="cartItems.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <svg class="w-16 h-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
      <p class="text-lg">Your cart is empty</p>
      <p class="text-sm mt-2">Add some Monster Energy drinks!</p>
    </div>

    <!-- Cart Items -->
    <div v-else class="flex flex-col h-full">
      <!-- Items List -->
      <div class="flex-1 overflow-y-auto p-4 space-y-4">
        <div
          v-for="item in cartItems"
          :key="item.cartItem.id"
          class="bg-monster-black/50 rounded-lg p-4 border border-monster-gray"
        >
          <div class="flex items-start justify-between mb-3">
            <div>
              <h3
                :class="[
                  'font-bold',
                  item.cartItem.drink.isMangoLoco ? 'text-mango-orange' : 'text-monster-green'
                ]"
              >
                {{ item.cartItem.drink.flavorName }}
              </h3>
              <p class="text-gray-400 text-sm">{{ item.cartItem.drink.title }}</p>
            </div>
            <button
              @click="removeItem(item.cartItem.id)"
              class="text-red-400 hover:text-red-300 transition-colors"
              title="Remove item"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>

          <!-- Price and Quantity -->
          <div class="flex items-center justify-between">
            <div>
              <p class="text-gray-500 text-xs">Unit Price</p>
              <p class="text-white font-medium">
                {{ item.unitPrice ? `${item.unitPrice} ₴` : 'N/A' }}
              </p>
            </div>

            <!-- Quantity Controls -->
            <div class="flex items-center gap-2">
              <button
                @click="updateQuantity(item.cartItem.id, item.cartItem.quantity - 1)"
                :disabled="item.cartItem.quantity <= 1"
                class="w-8 h-8 rounded bg-monster-gray hover:bg-monster-green hover:text-black disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center"
              >
                -
              </button>
              <span class="w-8 text-center font-bold text-white">{{ item.cartItem.quantity }}</span>
              <button
                @click="updateQuantity(item.cartItem.id, item.cartItem.quantity + 1)"
                class="w-8 h-8 rounded bg-monster-gray hover:bg-monster-green hover:text-black transition-all flex items-center justify-center"
              >
                +
              </button>
            </div>
          </div>

          <!-- Subtotal -->
          <div class="mt-3 pt-3 border-t border-monster-gray">
            <div class="flex justify-between">
              <span class="text-gray-400">Subtotal:</span>
              <span class="text-white font-bold">
                {{ item.subtotal ? `${item.subtotal.toFixed(2)} ₴` : 'N/A' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="border-t border-monster-gray p-4 bg-monster-black/50">
        <!-- Total -->
        <div class="flex justify-between items-center mb-4">
          <span class="text-gray-400">Total ({{ totalItems }} items):</span>
          <span class="text-2xl font-bold text-monster-green">
            {{ totalPrice ? `${totalPrice.toFixed(2)} ₴` : 'N/A' }}
          </span>
        </div>

        <!-- Clear Cart Button -->
        <button
          @click="clearCart"
          class="w-full py-3 bg-red-600 hover:bg-red-700 text-white font-bold rounded-lg transition-colors"
        >
          Clear Cart
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, computed } from 'vue'
import { cartService } from '../services/api.js'

export default {
  name: 'CartSidebar',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'cart-updated'],
  setup(props, { emit }) {
    const cartItems = ref([])
    const loading = ref(false)

    const totalItems = computed(() => {
      return cartItems.value.reduce((sum, item) => sum + item.cartItem.quantity, 0)
    })

    const totalPrice = computed(() => {
      return cartItems.value.reduce((sum, item) => {
        return item.subtotal ? sum + item.subtotal : sum
      }, 0)
    })

    const fetchCart = async () => {
      try {
        loading.value = true
        const data = await cartService.getCart()
        cartItems.value = data.items || []
      } catch (err) {
        console.error('Error fetching cart:', err)
        cartItems.value = []
      } finally {
        loading.value = false
      }
    }

    const updateQuantity = async (cartItemId, newQuantity) => {
      if (newQuantity < 1) return
      try {
        await cartService.updateQuantity(cartItemId, newQuantity)
        await fetchCart()
        emit('cart-updated')
      } catch (err) {
        console.error('Error updating quantity:', err)
      }
    }

    const removeItem = async (cartItemId) => {
      try {
        await cartService.removeFromCart(cartItemId)
        await fetchCart()
        emit('cart-updated')
      } catch (err) {
        console.error('Error removing item:', err)
      }
    }

    const clearCart = async () => {
      try {
        await cartService.clearCart()
        cartItems.value = []
        emit('cart-updated')
      } catch (err) {
        console.error('Error clearing cart:', err)
      }
    }

    // Fetch cart when sidebar opens
    watch(() => props.isOpen, (isOpen) => {
      if (isOpen) {
        fetchCart()
      }
    })

    // Expose fetchCart for parent component
    const refreshCart = () => fetchCart()

    return {
      cartItems,
      loading,
      totalItems,
      totalPrice,
      fetchCart,
      updateQuantity,
      removeItem,
      clearCart,
      refreshCart
    }
  }
}
</script>
