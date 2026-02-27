import asyncio
from typing import AsyncIterator
from dataclasses import dataclass

@dataclass
class Chunk:
    content: str
    additional_kwargs: dict

async def mock_astream() -> AsyncIterator[Chunk]:
    # 模拟那个“无头”思考流的情况
    yield Chunk(content="你好", additional_kwargs={})
    yield Chunk(content="！我现在要帮他分析...", additional_kwargs={})
    yield Chunk(content="</think>\n\n你好呀！", additional_kwargs={})

async def test_stripper():
    in_thinking_block = False
    buffer = ""
    messages = [] # Dummy
    
    # 模拟 ai_service.py 里的逻辑
    async def process():
        nonlocal in_thinking_block, buffer
        async for chunk in mock_astream():
            if chunk.additional_kwargs.get("reasoning_content"):
                continue
            content = chunk.content
            if content:
                buffer += content
                while buffer:
                    if not in_thinking_block:
                        end_idx = buffer.find("</think>")
                        start_idx = buffer.find("<think>")
                        if start_idx != -1:
                            if (end_idx == -1) or (start_idx < end_idx):
                                if start_idx > 0:
                                    print(f"OUTPUT: {buffer[:start_idx]}")
                                in_thinking_block = True
                                buffer = buffer[start_idx + len("<think>"):]
                                continue
                        if end_idx != -1:
                            buffer = buffer[end_idx + len("</think>"):]
                            continue
                        if "<" in buffer:
                            last_open = buffer.rfind("<")
                            if last_open > 0:
                                print(f"OUTPUT: {buffer[:last_open]}")
                                buffer = buffer[last_open:]
                            break
                        else:
                            print(f"OUTPUT: {buffer}")
                            buffer = ""
                    else:
                        end_idx = buffer.find("</think>")
                        if end_idx != -1:
                            in_thinking_block = False
                            buffer = buffer[end_idx + len("</think>"):]
                        else:
                            if "<" in buffer:
                                last_open = buffer.rfind("<")
                                buffer = buffer[last_open:]
                                break
                            else:
                                buffer = ""
        if buffer and not in_thinking_block:
            print(f"OUTPUT: {buffer}")

    await process()

if __name__ == "__main__":
    asyncio.run(test_stripper())
