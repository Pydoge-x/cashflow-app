<template>
  <div class="wealth-particles">
    <span v-for="n in count" :key="n" class="wealth-particle" :style="particleStyle(n)">
      {{ n % 2 === 0 ? '¥' : '$' }}
    </span>
  </div>
</template>

<script setup>
const props = defineProps({
  count: {
    type: Number,
    default: 40
  }
});

function particleStyle(n) {
  const left = Math.random() * 100;
  const duration = 20 + Math.random() * 30;
  const delay = Math.random() * -40;
  const size = 0.5 + Math.random() * 1.2;
  const opacity = 0.08 + Math.random() * 0.15;
  
  return {
    left: `${left}%`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`,
    fontSize: `${size}rem`,
    opacity: opacity,
    '--drift': `${(Math.random() - 0.5) * 80}px`
  };
}
</script>

<style scoped>
.wealth-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

.wealth-particle {
  position: absolute;
  color: #D4AF37;
  font-family: "serif";
  filter: blur(0.3px);
  animation: floatParticle 30s linear infinite;
  text-shadow: 0 0 8px rgba(212, 175, 55, 0.3);
}

@keyframes floatParticle {
  0% { 
    transform: translateY(110vh) translateX(0) rotate(0deg); 
    opacity: 0;
  }
  10% {
    opacity: var(--particle-opacity, 0.15);
  }
  90% {
    opacity: var(--particle-opacity, 0.15);
  }
  100% { 
    transform: translateY(-10vh) translateX(var(--drift, 40px)) rotate(360deg); 
    opacity: 0;
  }
}
</style>
