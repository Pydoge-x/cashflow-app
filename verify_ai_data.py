import sys
import os

# 将 ai-service 目录加入 python 路径
sys.path.append(r'f:\cashflow-app\ai-service')

from ai_service import build_system_prompt
from database import get_full_financial_data

def verify():
    user_id = 6  # 之前查到的有数据的用户
    print(f"Testing data retrieval for user_id={user_id}...")
    
    try:
        data = get_full_financial_data(user_id)
        if "error" in data:
            print(f"Error: {data['error']}")
            return
            
        print("Data retrieved successfully!")
        print(f"User: {data['user']['username']}")
        print(f"Reports found: {len(data['reports'])}")
        
        prompt = build_system_prompt(user_id)
        
        # 检查是否包含“核心准则”
        if "严禁虚构" in prompt:
            print("SUCCESS: Prompt contains strict rules.")
        else:
            print("FAILED: Strict rules missing from prompt.")
            
        # 检查是否标记了缺失数据 (对于 user 6)
        if "_status" in prompt or "数据缺失" in prompt:
            print("SUCCESS: Prompt explicitly labels missing data sections.")
        else:
            print("FAILED: Prompt does not label missing sections.")
            
    except Exception as e:
        print(f"Crushed with error: {e}")

if __name__ == "__main__":
    verify()
