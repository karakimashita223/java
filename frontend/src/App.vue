<template>
  <div class="min-h-screen bg-monster-black">
    <!-- Header -->
    <header class="bg-monster-dark border-b border-monster-gray sticky top-0 z-50">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="text-4xl font-monster text-monster-green tracking-wider">
              MONSTER
            </div>
            <div class="text-2xl font-monster text-mango-orange">
              LOCO TRACKER
            </div>
          </div>

          <div class="flex items-center gap-4">
            <!-- Cart Button -->
            <button
              @click="cartOpen = true"
              class="relative flex items-center gap-2 px-4 py-3 bg-monster-gray hover:bg-monster-gray/80
                     text-white font-bold rounded-lg transition-all duration-300"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              Cart
              <span
                v-if="cartItemCount > 0"
                class="absolute -top-2 -right-2 w-6 h-6 bg-monster-green text-black text-xs font-bold rounded-full flex items-center justify-center"
              >
                {{ cartItemCount }}
              </span>
            </button>

            <!-- Sync Button -->
            <button
              @click="syncPrices"
              :disabled="syncing"
              class="flex items-center gap-2 px-6 py-3 bg-monster-green hover:bg-monster-green-dark
                     text-black font-bold rounded-lg transition-all duration-300
                     disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <svg
                :class="['w-5 h-5', syncing ? 'animate-spin' : '']"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
              {{ syncing ? 'Syncing...' : 'Sync Prices' }}
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- Sync Status -->
    <div v-if="syncResult" class="container mx-auto px-4 mt-4">
      <div
        :class="[
          'p-4 rounded-lg',
          syncResult.failCount === 0 ? 'bg-green-900/50 border border-green-500' : 'bg-yellow-900/50 border border-yellow-500'
        ]"
      >
        <p class="text-white">
          {{ syncResult.message }} -
          <span class="text-green-400">{{ syncResult.successCount }} successful</span>,
          <span class="text-yellow-400">{{ syncResult.failCount }} failed</span>
        </p>
      </div>
    </div>

    <!-- Error Alert -->
    <div v-if="error" class="container mx-auto px-4 mt-4">
      <div class="p-4 rounded-lg bg-red-900/50 border border-red-500">
        <p class="text-red-400">{{ error }}</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="text-center">
        <div class="w-16 h-16 border-4 border-monster-green border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-monster-green text-xl">Loading drinks...</p>
      </div>
    </div>

    <!-- Drinks Grid -->
    <main v-else class="container mx-auto px-4 py-8">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <DrinkCard
          v-for="drink in drinks"
          :key="drink.drink.id"
          :drink="drink"
          :is-in-cart="drinkIdsInCart.has(drink.drink.id)"
          @show-history="showHistory"
          @open-cart="cartOpen = true"
          @cart-updated="refreshCartStatus"
        />
      </div>
    </main>

    <!-- Price History Modal -->
    <PriceHistoryModal
      v-if="selectedDrink"
      :drink="selectedDrink"
      :history="priceHistory"
      :loading="historyLoading"
      @close="closeHistory"
    />

    <!-- Cart Sidebar -->
    <CartSidebar
      :is-open="cartOpen"
      @close="cartOpen = false"
      @cart-updated="refreshCartStatus"
      ref="cartSidebar"
    />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { drinkService, cartService } from './services/api.js'
import DrinkCard from './components/DrinkCard.vue'
import PriceHistoryModal from './components/PriceHistoryModal.vue'
import CartSidebar from './components/CartSidebar.vue'

export default {
  name: 'App',
  components: {
    DrinkCard,
    PriceHistoryModal,
    CartSidebar
  },
  setup() {
    const drinks = ref([])
    const loading = ref(true)
    const syncing = ref(false)
    const syncResult = ref(null)
    const error = ref(null)
    const selectedDrink = ref(null)
    const priceHistory = ref([])
    const historyLoading = ref(false)
    const cartOpen = ref(false)
    const cartSidebar = ref(null)
    const drinkIdsInCart = ref(new Set())

    const cartItemCount = computed(() => drinkIdsInCart.value.size)

    const fetchDrinks = async () => {
      try {
        loading.value = true
        error.value = null
        drinks.value = await drinkService.getAllDrinks()
      } catch (err) {
        error.value = 'Failed to load drinks. Make sure the backend is running.'
        console.error('Error fetching drinks:', err)
      } finally {
        loading.value = false
      }
    }

    const refreshCartStatus = async () => {
      try {
        const cart = await cartService.getCart()
        drinkIdsInCart.value = new Set(cart.items.map(item => item.cartItem.drink.id))
      } catch (err) {
        console.error('Error refreshing cart status:', err)
        drinkIdsInCart.value = new Set()
      }
    }

    const syncPrices = async () => {
      try {
        syncing.value = true
        syncResult.value = null
        error.value = null
        syncResult.value = await drinkService.syncPrices()
        // Refresh drinks after sync
        await fetchDrinks()
      } catch (err) {
        error.value = 'Failed to sync prices. Please try again.'
        console.error('Error syncing prices:', err)
      } finally {
        syncing.value = false
      }
    }

    const showHistory = async (drink) => {
      selectedDrink.value = drink
      historyLoading.value = true
      try {
        priceHistory.value = await drinkService.getPriceHistory(drink.drink.id)
      } catch (err) {
        console.error('Error fetching price history:', err)
        priceHistory.value = []
      } finally {
        historyLoading.value = false
      }
    }

    const closeHistory = () => {
      selectedDrink.value = null
      priceHistory.value = []
    }

    onMounted(async () => {
      await fetchDrinks()
      await refreshCartStatus()
    })

    return {
      drinks,
      loading,
      syncing,
      syncResult,
      error,
      selectedDrink,
      priceHistory,
      historyLoading,
      cartOpen,
      cartSidebar,
      cartItemCount,
      drinkIdsInCart,
      syncPrices,
      showHistory,
      closeHistory,
      refreshCartStatus
    }
  }
}
</script>
