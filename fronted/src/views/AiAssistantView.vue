<template>
  <div class="ai-assistant-container">
    <!-- 头部装饰 -->
    <div class="chat-header glass-card">
      <div class="header-content">
        <div class="bot-info">
          <div class="bot-avatar shimmer-btn">
            <BotIcon :size="24" color="#fff" />
          </div>
          <div class="bot-titles">
            <h2 class="gold-text">AI 财务管家</h2>
            <p class="status-text">
              <span class="status-dot" :class="{ online: isOnline }"></span>
              {{ isOnline ? '在线 · 随时为你分析财务状况' : '离线 · 服务正在启动中' }}
            </p>
          </div>
        </div>
        <el-button class="clear-btn" link @click="clearHistory">
          <Trash2 :size="16" />
          <span>清空对话</span>
        </el-button>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-messages" ref="messageBox">
      <div v-if="messages.length === 0" class="welcome-view">
        <div class="welcome-card glass-card">
          <div class="welcome-icon">
            <Sparkles :size="48" class="gold-text" />
          </div>
          <h3>你好！我是你的智慧财务管家</h3>
          <p>我可以帮你分析收支、评估财务健康、解答会计疑问，或者为你制定存钱计划。</p>
          
          <div class="quick-prompts">
            <button 
              v-for="p in suggestions" 
              :key="p" 
              class="prompt-chip shimmer-btn"
              @click="sendMessage(p)"
            >
              {{ p }}
            </button>
          </div>
        </div>
      </div>

      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        class="message-wrapper"
        :class="msg.role"
      >
        <div class="message-bubble" :class="{ 'glass-card': msg.role === 'assistant' }">
          <div v-if="msg.role === 'assistant'" class="bot-tag">
            <Bot :size="14" />
            <span>智能建议</span>
          </div>

          <!-- 正文回答 -->
          <div class="message-content" v-html="formatContent(msg.content)"></div>
        </div>
      </div>

      <div v-if="isLoading" class="message-wrapper assistant">
        <div class="message-bubble glass-card loading-bubble">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area glass-card">
      <div class="input-container">
        <textarea 
          v-model="userInput" 
          placeholder="问问我：目前的财务风险有哪些？" 
          @keydown.enter.prevent="sendMessage()"
          :disabled="isLoading"
          rows="1"
          ref="inputField"
        ></textarea>
        <button 
          class="send-btn shimmer-btn" 
          :disabled="!userInput.trim() || isLoading"
          @click="sendMessage()"
        >
          <Send :size="20" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { 
  Bot as BotIcon, 
  Bot,
  Send, 
  Trash2, 
  Sparkles,
  User 
} from 'lucide-vue-next';
import { aiApi } from '../api/ai';
import { ElMessage } from 'element-plus';
import { marked } from 'marked';

const messages = ref([]);
const userInput = ref('');
const isLoading = ref(false);
const isOnline = ref(true);
const messageBox = ref(null);
const inputField = ref(null);

const suggestions = [
  "根据我的数据分析财务健康度",
  "我这个月的现金流正常吗？",
  "什么是资产负债表？",
  "如何提高我的投资资产占比？"
];

// 保存到本地存储
const loadHistory = () => {
  const saved = localStorage.getItem('ai_chat_history');
  if (saved) {
    try {
      messages.value = JSON.parse(saved);
    } catch (e) {
      console.error('Failed to load history', e);
    }
  }
};

const saveHistory = () => {
  localStorage.setItem('ai_chat_history', JSON.stringify(messages.value));
};

const clearHistory = () => {
  messages.value = [];
  localStorage.removeItem('ai_chat_history');
};

const scrollToBottom = async () => {
  await nextTick();
  if (messageBox.value) {
    messageBox.value.scrollTop = messageBox.value.scrollHeight;
  }
};

const formatContent = (content) => {
  return marked.parse(content || '');
};

const sendMessage = async (preset) => {
  const text = preset || userInput.value;
  if (!text.trim() || isLoading.value) return;

  userInput.value = '';
  const userMsg = { role: 'user', content: text };
  messages.value.push(userMsg);
  isLoading.value = true;
  
  await scrollToBottom();

  // 准备一个空的回复节点
  const aiMsg = { role: 'assistant', content: '' };
  messages.value.push(aiMsg);
  const aiIndex = messages.value.length - 1;

  // 调用 API
  aiApi.chatStream(
    text,
    messages.value.slice(0, -1), // 传递之前的历史，不包含当前的空回复
    (token) => {
      // 收到普通回答 Token
      messages.value[aiIndex].content += token;
      scrollToBottom();
    },
    () => {
      // 完成
      isLoading.value = false;
      saveHistory();
    },
    (error) => {
      // 报错
      isLoading.value = false;
      messages.value[aiIndex].content = `⚠️ **出错了**：${error}`;
      ElMessage.error('AI 响应中断');
    }
  );
};

onMounted(async () => {
  loadHistory();
  scrollToBottom();
  
  // 检查状态
  const health = await aiApi.checkHealth();
  isOnline.value = health.status === 'ok';
});

// 自动调整输入框高度
watch(userInput, () => {
  if (inputField.value) {
    inputField.value.style.height = 'auto';
    inputField.value.style.height = Math.min(inputField.value.scrollHeight, 120) + 'px';
  }
});
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  max-width: 900px;
  margin: 0 auto;
  gap: 16px;
  position: relative;
}

/* Header */
.chat-header {
  padding: 16px 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bot-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bot-avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.bot-titles h2 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 800;
}

.status-text {
  margin: 2px 0 0;
  font-size: 0.75rem;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #ccc;
  border-radius: 50%;
}

.status-dot.online {
  background: #52c41a;
  box-shadow: 0 0 8px #52c41a;
  animation: pulse 2s infinite;
}

/* Messages Area */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scrollbar-width: thin;
}

.welcome-view {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.welcome-card {
  max-width: 500px;
  padding: 40px;
  text-align: center;
  border-radius: 30px;
}

.welcome-icon {
  margin-bottom: 20px;
}

.welcome-card h3 {
  font-size: 1.4rem;
  margin-bottom: 12px;
  color: #B8860B;
}

.welcome-card p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.prompt-chip {
  background: rgba(212, 175, 55, 0.08);
  border: 1px solid rgba(212, 175, 55, 0.2);
  padding: 8px 16px;
  border-radius: 12px;
  font-size: 0.85rem;
  color: #B8860B;
  cursor: pointer;
  transition: all 0.2s;
}

.prompt-chip:hover {
  background: rgba(212, 175, 55, 0.15);
  transform: translateY(-2px);
}

/* Bubbles */
.message-wrapper {
  display: flex;
  width: 100%;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 80%;
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 0.95rem;
  line-height: 1.6;
  position: relative;
}

.user .message-bubble {
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.2);
}

.assistant .message-bubble {
  background: rgba(255, 255, 255, 0.9);
  border-bottom-left-radius: 4px;
}

.bot-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.7rem;
  font-weight: 700;
  color: #B8860B;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.message-content :deep(p) { margin: 0 0 10px; }
.message-content :deep(p:last-child) { margin-bottom: 0; }
.message-content :deep(code) {
  background: rgba(212, 175, 55, 0.1);
  padding: 2px 4px;
  border-radius: 4px;
  color: #B8860B;
}

/* Input Area */
.chat-input-area {
  padding: 16px;
  border-radius: 20px;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: rgba(255, 255, 255, 0.5);
  padding: 8px 12px;
  border-radius: 16px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

textarea {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  padding: 8px 4px;
  font-size: 0.95rem;
  resize: none;
  max-height: 120px;
  font-family: inherit;
  line-height: 1.5;
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: none;
  cursor: pointer;
  flex-shrink: 0;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Animations */
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
  100% { transform: scale(1); opacity: 1; }
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #B8860B;
  border-radius: 50%;
  opacity: 0.4;
  animation: typing 1s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-4px); opacity: 1; }
}

@media (max-width: 768px) {
  .ai-assistant-container {
    height: calc(100vh - 160px);
  }
  .message-bubble {
    max-width: 90%;
  }
}
</style>
