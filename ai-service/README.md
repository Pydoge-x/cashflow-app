# 家庭财务管家 AI Service

Python 微服务，为"智慧家庭财务助手"提供 AI 问答能力。

## 功能

- 读取用户的全部财务数据（资产负债表、收入支出表、现金流）
- 以"家庭财务管家/注册会计师"角色回答财务问题
- 支持流式输出（SSE），字符实时显示
- 支持对话历史上下文

## 快速开始

### 1. 安装依赖

```bash
cd ai-service
pip install -r requirements.txt
```

### 2. 配置环境变量

复制配置模板并填入你自己的 LLM API Key：

```bash
copy .env.example .env
```

打开 `.env` 文件，修改以下项：

```env
# 必填：你的 LLM API Key（支持 DeepSeek/OpenAI/通义千问等）
OPENAI_API_KEY=your_api_key_here

# API 地址（默认 DeepSeek，可改为 OpenAI 或其他兼容接口）
OPENAI_BASE_URL=https://api.deepseek.com/v1
OPENAI_MODEL=deepseek-chat

# 数据库配置（与 cashflow 后端共用）
DB_PASSWORD=your_mysql_password
```

### 3. 启动服务

```bash
python main.py
```

服务将运行在 `http://localhost:8001`

### 4. 测试

```bash
curl http://localhost:8001/health
```

## API

### `GET /health`

健康检查

### `POST /chat`

AI 聊天（SSE 流式）

请求体：

```json
{
  "userId": 1,
  "message": "分析我的财务状况",
  "history": []
}
```

## 技术栈

- **框架**：FastAPI + Uvicorn
- **AI**：LangChain + OpenAI 兼容接口（DeepSeek / OpenAI / 通义等）
- **数据库**：PyMySQL（只读）
- **流式输出**：SSE (Server-Sent Events)
