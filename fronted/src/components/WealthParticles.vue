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
  const duration = 15 + Math.random() * 25;
  const delay = Math.random() * -30;
  const size = 0.6 + Math.random() * 1.5;
  const opacity = 0.15 + Math.random() * 0.35;
  
  return {
    left: `${left}%`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`,
    fontSize: `${size}rem`,
    opacity: opacity,
    // Add a bit of horizontal drift
    '--drift': `${(Math.random() - 0.5) * 100}px`
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
  color: gold;
  font-family: "serif";
  filter: blur(0.5px);
  animation: floatParticle 25s linear infinite;
}

@keyframes floatParticle {
  0% { 
    transform: translateY(110vh) translateX(0) rotate(0deg); 
  }
  100% { 
    transform: translateY(-10vh) translateX(var(--drift, 50px)) rotate(360deg); 
  }
}
</style>
