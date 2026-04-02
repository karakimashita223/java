import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const drinkService = {
  async getAllDrinks() {
    const response = await api.get('/drinks')
    return response.data
  },

  async getDrinkById(id) {
    const response = await api.get(`/drinks/${id}`)
    return response.data
  },

  async getPriceHistory(id) {
    const response = await api.get(`/drinks/${id}/history`)
    return response.data
  },

  async syncPrices() {
    const response = await api.post('/sync')
    return response.data
  },

  async checkHealth() {
    const response = await api.get('/health')
    return response.data
  }
}

export const cartService = {
  async getCart() {
    const response = await api.get('/cart')
    return response.data
  },

  async addToCart(drinkId, quantity = 1) {
    const response = await api.post(`/cart/${drinkId}`, null, {
      params: { quantity }
    })
    return response.data
  },

  async updateQuantity(cartItemId, quantity) {
    const response = await api.put(`/cart/${cartItemId}`, null, {
      params: { quantity }
    })
    return response.data
  },

  async removeFromCart(cartItemId) {
    await api.delete(`/cart/${cartItemId}`)
  },

  async clearCart() {
    await api.delete('/cart')
  },

  async isInCart(drinkId) {
    const response = await api.get(`/cart/contains/${drinkId}`)
    return response.data
  }
}

export default api
