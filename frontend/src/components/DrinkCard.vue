<template>
  <div
    :class="[
      'bg-monster-dark rounded-xl overflow-hidden border-2 transition-all duration-300 hover:scale-[1.02]',
      drink.drink.isMangoLoco
        ? 'border-mango-orange mango-glow'
        : 'border-monster-gray hover:border-monster-green'
    ]"
  >
    <!-- Badge for Mango Loco -->
    <div v-if="drink.drink.isMangoLoco" class="bg-mango-orange text-black px-3 py-1 text-sm font-bold text-center">
      ⭐ FEATURED ⭐
    </div>

    <!-- Drink Image -->
    <div class="p-6 flex justify-center cursor-pointer" @click="$emit('show-history', drink)">
      <img
        :src="drinkImage"
        :alt="drink.drink.flavorName"
        class="h-36 object-contain drop-shadow-lg hover:scale-110 transition-transform duration-300"
        @error="onImageError"
      />
    </div>

    <!-- Drink Info -->
    <div class="p-4 bg-monster-black/50">
      <!-- Flavor Name -->
      <h3
        :class="[
          'text-xl font-bold mb-2 cursor-pointer',
          drink.drink.isMangoLoco ? 'text-mango-orange' : 'text-monster-green'
        ]"
        @click="$emit('show-history', drink)"
      >
        {{ drink.drink.flavorName }}
      </h3>

      <!-- Title -->
      <p class="text-gray-400 text-sm mb-3 truncate">
        {{ drink.drink.title }}
      </p>

      <!-- Price -->
      <div class="flex items-center justify-between">
        <div>
          <p class="text-gray-500 text-xs">Current Price</p>
          <p
            :class="[
              'text-2xl font-bold',
              drink.latestPrice ? 'text-white' : 'text-gray-600'
            ]"
          >
            {{ drink.latestPrice ? `${drink.latestPrice} ₴` : 'N/A' }}
          </p>
        </div>

        <!-- Product Code -->
        <div class="text-right">
          <p class="text-gray-500 text-xs">Code</p>
          <p class="text-gray-400 text-sm font-mono">
            {{ drink.drink.productCode }}
          </p>
        </div>
      </div>

      <!-- Last Updated -->
      <div v-if="drink.lastUpdated" class="mt-3 pt-3 border-t border-monster-gray">
        <p class="text-gray-500 text-xs">
          Last updated: {{ formatDate(drink.lastUpdated) }}
        </p>
      </div>
    </div>

    <!-- Add to Cart Button -->
    <div class="p-3 bg-monster-gray/50">
      <button
        v-if="!isInCart"
        @click.stop="addToCart"
        :disabled="addingToCart"
        :class="[
          'w-full py-2 px-4 rounded-lg font-bold transition-all duration-300 flex items-center justify-center gap-2',
          drink.drink.isMangoLoco
            ? 'bg-mango-orange hover:bg-mango-orange/80 text-black'
            : 'bg-monster-green hover:bg-monster-green-dark text-black',
          addingToCart ? 'opacity-50 cursor-not-allowed' : ''
        ]"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        {{ addingToCart ? 'Adding...' : 'Add to Cart' }}
      </button>
      <div v-else class="flex items-center justify-between">
        <span
          :class="[
            'text-sm font-medium flex items-center gap-1',
            drink.drink.isMangoLoco ? 'text-mango-orange' : 'text-monster-green'
          ]"
        >
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
          </svg>
          In Cart
        </span>
        <button
          @click.stop="$emit('open-cart')"
          :class="[
            'text-sm font-medium underline',
            drink.drink.isMangoLoco ? 'text-mango-orange hover:text-mango-orange/80' : 'text-monster-green hover:text-monster-green/80'
          ]"
        >
          View Cart
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import { cartService } from '../services/api.js'

export default {
  name: 'DrinkCard',
  props: {
    drink: {
      type: Object,
      required: true
    },
    isInCart: {
      type: Boolean,
      default: false
    }
  },
  emits: ['show-history', 'open-cart', 'cart-updated'],
  setup(props, { emit }) {
    const addingToCart = ref(false)

    const drinkImages = {
      'Mango Loco': '/drinks/mango-loco.png',
      'Juiced Monarch': '/drinks/juiced-monarch.webp',
      'Original': '/drinks/original.webp',
      'Ultra White': '/drinks/ultra-white.webp'
    }

    const drinkImage = computed(() => {
      return drinkImages[props.drink.drink.flavorName] || '/drinks/mango-loco.png'
    })

    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const onImageError = (e) => {
      e.target.style.display = 'none'
    }

    const addToCart = async () => {
      try {
        addingToCart.value = true
        await cartService.addToCart(props.drink.drink.id, 1)
        emit('cart-updated')
      } catch (err) {
        console.error('Error adding to cart:', err)
      } finally {
        addingToCart.value = false
      }
    }

    return {
      addingToCart,
      drinkImage,
      formatDate,
      onImageError,
      addToCart
    }
  }
}
</script>
