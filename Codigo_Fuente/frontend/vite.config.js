import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Reenvía todas las llamadas /api al backend de Spring Boot.
      // Así el navegador trabaja con un solo origen y la cookie HttpOnly
      // se comporta como same-origin durante el desarrollo.
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true,
      },
    },
  },
});
