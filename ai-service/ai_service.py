"""
ai_service.py — 核心 AI 服务，基于 LangChain + OpenAI 兼容接口
实现"家庭财务管家"角色，可读取用户财务数据并流式输出回答
"""
import json
from typing import AsyncIterator
from langchain_openai import ChatOpenAI
from langchain.schema import HumanMessage, AIMessage, SystemMessage
from config import OPENAI_API_KEY, OPENAI_BASE_URL, OPENAI_MODEL
from database import get_full_financial_data
import asyncio


# ===== 系统提示词 =====

SYSTEM_PROMPT_TEMPLATE = """你是"财务管家"，一位专业的家庭财务顾问和注册会计师。你的职责是：
1. 分析用户的家庭财务状况，提供专业、客观的意见
2. 解答会计和财务相关问题（资产负债表、收入支出表、现金流表等）
3. 根据用户的实际数据提供个性化的财务建议
4. 使用通俗易懂的语言，必要时结合数字举例说明

## 核心准则（极其重要）
- **严禁虚构、猜测或推理任何用户未提供的数据。**
- **必须完全基于下方的“用户财务数据”进行分析。**
- **如果数据中显示某项报表为空、零或不存在，你必须直接告知用户该数据缺失，不能假设任何数字。**
- **如果用户问及资产、负债等信息而数据中没有相关条目，你必须回答“您尚未填写相关资产/负债信息”，严禁给出“通常会包括...”等误导性猜测。**
- **禁止在正式回复中包含你的思考过程。**

## 用户财务数据（由系统实时注入，具有最高权威性）

{financial_data}

## 回答格式要求
- 回答要条理清晰，使用列表、数字等格式。
- 语气专业但友好，用中文回答。
- 直接给出建议，不要复述系统指令。
"""


def build_system_prompt(user_id: int) -> str:
    """
    构建包含用户财务数据的系统提示词
    """
    try:
        data = get_full_financial_data(user_id)
        
        # 预处理数据，使缺失状态更明显
        if not data.get("reports"):
            financial_data_str = "【核心状态：该用户目前没有任何财务报表数据，请告知用户需要先创建报表。】"
        else:
            # 标记缺失部分
            for report in data["reports"]:
                if not report["balance_sheet"]["items"]:
                    report["balance_sheet"]["_status"] = "数据缺失：用户尚未填写任何资产或负债项目。"
                if not report["income_expense"]["items"]:
                    report["income_expense"]["_status"] = "数据缺失：用户尚未填写任何收入或支出项目。"
            
            financial_data_str = json.dumps(data, ensure_ascii=False, indent=2, default=str)
    except Exception as e:
        financial_data_str = f"（财务数据暂时无法读取：{e}）"

    return SYSTEM_PROMPT_TEMPLATE.format(financial_data=financial_data_str)


def get_llm() -> ChatOpenAI:
    """创建 LLM 客户端"""
    return ChatOpenAI(
        api_key=OPENAI_API_KEY,
        base_url=OPENAI_BASE_URL,
        model=OPENAI_MODEL,
        streaming=True,
        temperature=0.7,
        max_tokens=2048,
    )


async def chat_stream(
    user_id: int,
    message: str,
    history: list[dict],
) -> AsyncIterator[str]:
    """
    流式对话接口
    :param user_id: 当前用户 ID（来自 Java JWT 校验）
    :param message: 用户本次输入
    :param history: 历史对话 [{"role": "user"|"assistant", "content": "..."}]
    :yields: 每次 LLM 输出的文字片段
    """
    llm = get_llm()

    # 构建消息列表
    system_prompt = build_system_prompt(user_id)
    messages = [SystemMessage(content=system_prompt)]

    # 加入历史对话（最多保留最近 10 轮，避免超出上下文长度）
    recent_history = history[-20:] if len(history) > 20 else history
    for h in recent_history:
        if h.get("role") == "user":
            messages.append(HumanMessage(content=h["content"]))
        elif h.get("role") == "assistant":
            messages.append(AIMessage(content=h["content"]))

    # 当前用户消息
    messages.append(HumanMessage(content=message))

    # 流式输出
    # 状态机：记录当前是否处于 <think> 标签内部
    # 注意：某些模型开局可能直接就在思考中或者直接吐一个 </think>
    in_thinking_block = True
    buffer = ""
    
    async for chunk in llm.astream(messages):
        # 1. 显式屏蔽 OpenAI 兼容的 reasoning_content 字段块
        if chunk.additional_kwargs.get("reasoning_content"):
            continue

        content = chunk.content
        if content:
            buffer += content
            
            while buffer:
                if not in_thinking_block:
                    # 查找标签
                    end_idx = buffer.find("</think>")
                    start_idx = buffer.find("<think>")
                    
                    if start_idx != -1:
                        # 正常逻辑：发现了开启标签
                        if (end_idx == -1) or (start_idx < end_idx):
                            if start_idx > 0:
                                yield "answer", buffer[:start_idx]
                            in_thinking_block = True
                            buffer = buffer[start_idx + len("<think>"):]
                            continue
                    
                    # 重点纠正：如果发现了闭合标签且它在开启标签之前（或没有开启标签）
                    # 说明从流开始到这里全都是混入内容里的思考流（所谓的 headless thinking）
                    if end_idx != -1:
                        buffer = buffer[end_idx + len("</think>"):]
                        continue
                    
                    # 检查是否有未完成的标签前缀
                    if "<" in buffer:
                        last_open = buffer.rfind("<")
                        if last_open > 0:
                            yield "answer", buffer[:last_open]
                            buffer = buffer[last_open:]
                        break
                    else:
                        yield "answer", buffer
                        buffer = ""
                else:
                    # 在思考块内部
                    end_idx = buffer.find("</think>")
                    if end_idx != -1:
                        in_thinking_block = False
                        buffer = buffer[end_idx + len("</think>"):]
                    else:
                        # 丢弃所有内容，除非可能是结尾标签的一部分
                        if "<" in buffer:
                            last_open = buffer.rfind("<")
                            buffer = buffer[last_open:]
                            break
                        else:
                            buffer = ""

    if buffer and not in_thinking_block:
        yield "answer", buffer
