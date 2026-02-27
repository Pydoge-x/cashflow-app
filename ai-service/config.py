"""
config.py — 读取 .env 配置文件，集中管理所有配置项
"""
import os
from dotenv import load_dotenv

# 加载 .env 文件（优先使用 .env，不存在时使用 .env.example）
load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), ".env"))

# ===== LLM 配置 =====
OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
OPENAI_BASE_URL: str = os.getenv("OPENAI_BASE_URL", "https://api.openai.com/v1")
OPENAI_MODEL: str = os.getenv("OPENAI_MODEL", "gpt-4o-mini")

# ===== 数据库配置 =====
DB_HOST: str = os.getenv("DB_HOST", "127.0.0.1")
DB_PORT: int = int(os.getenv("DB_PORT", "3306"))
DB_NAME: str = os.getenv("DB_NAME", "cashflow_db")
DB_USERNAME: str = os.getenv("DB_USERNAME", "root")
DB_PASSWORD: str = os.getenv("DB_PASSWORD", "")

# ===== 服务配置 =====
JAVA_BACKEND_ORIGIN: str = os.getenv("JAVA_BACKEND_ORIGIN", "http://localhost:1800")
AI_SERVICE_PORT: int = int(os.getenv("AI_SERVICE_PORT", "8001"))
