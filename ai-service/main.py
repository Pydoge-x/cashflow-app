"""
main.py — FastAPI 入口，提供 AI 聊天接口
运行方式： uvicorn main:app --reload --port 8001
"""
import asyncio
import json
import logging
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import Optional
from config import JAVA_BACKEND_ORIGIN, AI_SERVICE_PORT
from ai_service import chat_stream

# ===== 日志配置 =====
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("ai-service")

# ===== FastAPI 应用 =====
app = FastAPI(
    title="家庭财务管家 AI Service",
    description="为智慧家庭财务助手提供 AI 对话能力的 Python 微服务",
    version="1.0.0",
)

# ===== CORS 配置 =====
# 只允许来自 Java 后端的请求（Java 代理后调用此服务）
origins = list(set([JAVA_BACKEND_ORIGIN, "http://localhost:1800", "http://localhost:5173", "https://api.huitomantic.ltd"]))
print(f"DEBUG: CORS Origins: {origins}")
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)


# ===== 请求 / 响应模型 =====

class ChatMessage(BaseModel):
    """单条历史消息"""
    role: str           # "user" 或 "assistant"
    content: str


class ChatRequest(BaseModel):
    """POST /chat 的请求体"""
    userId: int         # 来自 Java 后端 JWT 校验后的用户 ID
    message: str        # 用户本次发送的消息
    history: Optional[list[ChatMessage]] = []   # 历史对话


@app.get("/", summary="根路径响应")
async def root():
    """根路径响应，防止 404 困惑"""
    return {
        "status": "running",
        "service": "家庭财务管家 AI Service",
        "message": "AI 服务已正常启动。请通过 Java 后端或 API 专用接口访问。",
        "endpoints": ["/health", "/chat"]
    }


# ===== 接口 =====

@app.get("/health", summary="健康检查")
async def health():
    """健康检查接口，Java 启动时可 ping 此接口确认 AI 服务已就绪"""
    return {"status": "ok", "service": "家庭财务管家 AI Service"}


@app.post("/chat", summary="AI 聊天（SSE 流式输出）")
async def chat(req: ChatRequest):
    """
    接收用户消息，以 SSE（Server-Sent Events）格式流式返回 AI 回答。
    
    事件格式：
    - data: {"type": "answer", "content": "..."}\n\n   — AI 输出的回答片段（前端可将这些片段拼接为回答）
    - data: {"type": "thinking", "content": "..."}\n\n   — AI 的思考 / 内部推理片段，应在 UI 上标注为“思考”以示区分
    - data: {"type": "done"}\n\n                       — 完成标志
    - data: {"type": "error", "content": "..."}\n\n   — 错误信息
    """
    if not req.message.strip():
        raise HTTPException(status_code=400, detail="消息不能为空")

    logger.info(f"Chat request from userId={req.userId}, message length={len(req.message)}")

    history_dicts = [{"role": m.role, "content": m.content} for m in (req.history or [])]

    async def event_generator():
        try:
            async for token_type, token_content in chat_stream(req.userId, req.message, history_dicts):
                # 将每个片段封装为 SSE 事件，type 可为 "answer" 或 "thinking"
                data = json.dumps({"type": token_type, "content": token_content}, ensure_ascii=False)
                yield f"data: {data}\n\n"
            # 发送完成标志
            yield f"data: {json.dumps({'type': 'done'})}\n\n"
        except Exception as e:
            logger.error(f"Chat stream error for userId={req.userId}: {e}", exc_info=True)
            error_data = json.dumps({"type": "error", "content": f"AI 服务出错：{str(e)}"}, ensure_ascii=False)
            yield f"data: {error_data}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",   # 关闭 Nginx 缓冲，确保实时流式传输
            "Connection": "keep-alive",
        },
    )


# ===== 启动入口 =====
if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting AI Service on port {AI_SERVICE_PORT}")
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=AI_SERVICE_PORT,
        reload=False,
        log_level="info",
    )
