import asyncio
from ai_service import get_llm
from langchain.schema import HumanMessage

async def main():
    llm = get_llm()
    async for c in llm.astream([HumanMessage(content='你好呀')]):
        if c.content:
            print(repr(c.content))

asyncio.run(main())
