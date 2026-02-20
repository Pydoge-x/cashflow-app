import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import legacy from '@vitejs/plugin-legacy'
import { loadEnv } from 'vite'

const env = loadEnv('development', process.cwd())

// https://vite.dev/config/
export default defineConfig({
  base: './',
  plugins: [vue(), legacy({
    targets: ['defaults', 'Android >= 4.4'],
    additionalLegacyPolyfills: ['regenerator-runtime/runtime']
  })],
  server: {
    allowedHosts: env.VITE_SERVER_ALLOWED_HOSTS?.split(',') || [],
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:1800',
        changeOrigin: true
      }
    }
  }
})
