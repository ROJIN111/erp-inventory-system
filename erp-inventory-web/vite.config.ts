import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000, // 寮€鍙戞湇鍔″櫒绔彛
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 鍚庣鍦板潃
        changeOrigin: true, // 鍏佽璺ㄥ煙
      },
    },
  },
})
