/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'monster-green': '#32CD32',
        'monster-green-dark': '#28a428',
        'mango-orange': '#FF8C00',
        'mango-orange-dark': '#e67e00',
        'monster-black': '#0a0a0a',
        'monster-dark': '#1a1a1a',
        'monster-gray': '#2a2a2a',
      },
      fontFamily: {
        'monster': ['Impact', 'Arial Black', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
