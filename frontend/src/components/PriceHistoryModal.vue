<template>
  <div
    class="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4"
    @click.self="$emit('close')"
  >
    <div class="bg-monster-dark rounded-xl max-w-2xl w-full max-h-[90vh] overflow-hidden border-2 border-monster-green">
      <!-- Header -->
      <div
        :class="[
          'p-4 flex items-center justify-between',
          drink.drink.isMangoLoco ? 'bg-mango-orange' : 'bg-monster-green'
        ]"
      >
        <h2 class="text-xl font-bold text-black">
          {{ drink.drink.flavorName }} - Price History
        </h2>
        <button
          @click="$emit('close')"
          class="text-black hover:text-gray-700 text-2xl font-bold"
        >
          ×
        </button>
      </div>

      <!-- Content -->
      <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-10">
          <div class="w-10 h-10 border-4 border-monster-green border-t-transparent rounded-full animate-spin"></div>
        </div>

        <!-- No Data -->
        <div v-else-if="history.length === 0" class="text-center py-10">
          <p class="text-gray-400 text-lg">No price history available</p>
          <p class="text-gray-500 text-sm mt-2">Click "Sync Prices" to fetch the latest prices</p>
        </div>

        <!-- Chart -->
        <div v-else>
          <!-- Stats -->
          <div class="grid grid-cols-3 gap-4 mb-6">
            <div class="bg-monster-black/50 p-4 rounded-lg text-center">
              <p class="text-gray-400 text-xs mb-1">Current</p>
              <p class="text-white text-xl font-bold">{{ latestPrice }} ₴</p>
            </div>
            <div class="bg-monster-black/50 p-4 rounded-lg text-center">
              <p class="text-gray-400 text-xs mb-1">Lowest</p>
              <p class="text-red-400 text-xl font-bold">{{ lowestPrice }} ₴</p>
            </div>
            <div class="bg-monster-black/50 p-4 rounded-lg text-center">
              <p class="text-gray-400 text-xs mb-1">Highest</p>
              <p class="text-green-400 text-xl font-bold">{{ highestPrice }} ₴</p>
            </div>
          </div>

          <!-- Price Chart -->
          <div class="bg-monster-black/50 p-4 rounded-lg">
            <Line
              :data="chartData"
              :options="chartOptions"
              style="height: 300px"
            />
          </div>

          <!-- History Table -->
          <div class="mt-6">
            <h3 class="text-white font-bold mb-3">Recent Entries</h3>
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="text-gray-400 border-b border-monster-gray">
                    <th class="text-left py-2 px-3">Date</th>
                    <th class="text-right py-2 px-3">Price (₴)</th>
                    <th class="text-right py-2 px-3">Change</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(entry, index) in history.slice().reverse()"
                    :key="entry.id"
                    class="text-white border-b border-monster-gray/50 hover:bg-monster-gray/30"
                  >
                    <td class="py-2 px-3">{{ formatDate(entry.timestamp) }}</td>
                    <td class="text-right py-2 px-3 font-mono">{{ entry.price }}</td>
                    <td class="text-right py-2 px-3">
                      <span
                        v-if="index < history.length - 1"
                        :class="[
                          'font-medium',
                          getPriceChange(index) > 0 ? 'text-red-400' :
                          getPriceChange(index) < 0 ? 'text-green-400' : 'text-gray-400'
                        ]"
                      >
                        {{ getPriceChangeText(index) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

export default {
  name: 'PriceHistoryModal',
  components: {
    Line
  },
  props: {
    drink: {
      type: Object,
      required: true
    },
    history: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close'],
  setup(props) {
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const latestPrice = computed(() => {
      if (props.history.length === 0) return 'N/A'
      return props.history[props.history.length - 1]?.price || 'N/A'
    })

    const lowestPrice = computed(() => {
      if (props.history.length === 0) return 'N/A'
      return Math.min(...props.history.map(h => h.price)).toFixed(2)
    })

    const highestPrice = computed(() => {
      if (props.history.length === 0) return 'N/A'
      return Math.max(...props.history.map(h => h.price)).toFixed(2)
    })

    const chartData = computed(() => {
      const labels = props.history.map(h => formatDate(h.timestamp))
      const data = props.history.map(h => h.price)
      const color = props.drink.drink.isMangoLoco ? '#FF8C00' : '#32CD32'

      return {
        labels,
        datasets: [
          {
            label: 'Price (₴)',
            data,
            borderColor: color,
            backgroundColor: color + '20',
            fill: true,
            tension: 0.4,
            pointBackgroundColor: color,
            pointBorderColor: '#fff',
            pointRadius: 4,
            pointHoverRadius: 6
          }
        ]
      }
    })

    const chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: '#1a1a1a',
          titleColor: '#fff',
          bodyColor: '#32CD32',
          borderColor: '#32CD32',
          borderWidth: 1
        }
      },
      scales: {
        x: {
          ticks: {
            color: '#888',
            maxTicksLimit: 10
          },
          grid: {
            color: '#333'
          }
        },
        y: {
          ticks: {
            color: '#888',
            callback: (value) => value + ' ₴'
          },
          grid: {
            color: '#333'
          }
        }
      }
    }

    const getPriceChange = (index) => {
      const reversedHistory = props.history.slice().reverse()
      if (index >= reversedHistory.length - 1) return 0
      return reversedHistory[index].price - reversedHistory[index + 1].price
    }

    const getPriceChangeText = (index) => {
      const change = getPriceChange(index)
      if (change > 0) return `+${change.toFixed(2)}`
      if (change < 0) return change.toFixed(2)
      return '0.00'
    }

    return {
      formatDate,
      latestPrice,
      lowestPrice,
      highestPrice,
      chartData,
      chartOptions,
      getPriceChange,
      getPriceChangeText
    }
  }
}
</script>
