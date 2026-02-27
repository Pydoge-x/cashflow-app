/**
 * ai.js — AI 助手相关的 API 调用
 * 使用 Fetch API 以支持流式（Streaming）响应
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.endsWith('/api') 
  ? import.meta.env.VITE_API_BASE_URL 
  : (import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') + '/api');

export const aiApi = {
  /**
   * 发送聊天请求并处理流式响应
   * @param {string} message 用户消息
   * @param {Array} history 历史对话 [{role, content}]
   * @param {Function} onToken 收到 token 时的回调
   * @param {Function} onDone 完成时的回调
   * @param {Function} onError 错误时的回调
   */
  async chatStream(message, history, onToken, onDone, onError) {
    const token = localStorage.getItem('token');
    
    try {
      const response = await fetch(`${API_BASE_URL}/ai/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ message, history })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      
      let buffer = '';
      
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        
        buffer += decoder.decode(value, { stream: true });
        
        // SSE 格式处理：以 \n\n 分隔事件
        const parts = buffer.split('\n\n');
        buffer = parts.pop(); // 最后一部分可能不完整，保留到下次处理
        
        for (const part of parts) {
          if (part.startsWith('data:')) {
            const dataStr = part.replace(/^data:\s*/, '').trim();
            if (!dataStr) continue;
            
            try {
              const data = JSON.parse(dataStr);
              // 后端现在使用 `answer` 表示回答片段
              if (data.type === 'answer') {
                onToken(data.content);
              } else if (data.type === 'token') {
                // 也兼容旧版接口
                onToken(data.content);
              } else if (data.type === 'done') {
                onDone();
              } else if (data.type === 'error') {
                onError(data.content);
              }
            } catch (e) {
              console.warn('Failed to parse SSE data:', dataStr, e);
            }
          }
        }
      }
    } catch (error) {
      console.error('Streaming error:', error);
      if (error.name === 'TypeError' && error.message.includes('network error')) {
        onError('网络连接中断或服务超时，请重试');
      } else {
        onError(error.message);
      }
    }
  },

  /**
   * 检查 AI 服务健康状态
   */
  async checkHealth() {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`${API_BASE_URL}/ai/health`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      return await response.json();
    } catch (error) {
      return { status: 'offline', message: error.message };
    }
  }
};
