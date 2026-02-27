"""
database.py — 直接读取 MySQL 数据库的财务数据，供 AI 分析使用
注意：此模块只读，不写入任何数据
"""
import pymysql
from typing import Optional
from config import DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD


def get_connection():
    """建立 MySQL 连接"""
    return pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        db=DB_NAME,
        user=DB_USERNAME,
        password=DB_PASSWORD,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def get_user_info(user_id: int) -> Optional[dict]:
    """获取用户基本信息"""
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(
                "SELECT id, username, email, phone, gender, age, created_at FROM users WHERE id = %s",
                (user_id,),
            )
            return cursor.fetchone()
    finally:
        conn.close()


def get_user_reports(user_id: int) -> list[dict]:
    """获取用户的所有报表"""
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(
                "SELECT id, name, type, created_at FROM reports WHERE user_id = %s ORDER BY id",
                (user_id,),
            )
            return cursor.fetchall()
    finally:
        conn.close()


def get_balance_sheet_items(report_id: int) -> list[dict]:
    """获取资产负债表条目"""
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(
                """SELECT id, name, amount, category, note, is_interest, interest_amount
                   FROM balance_sheet_items WHERE report_id = %s ORDER BY category, name""",
                (report_id,),
            )
            return cursor.fetchall()
    finally:
        conn.close()


def get_income_expense_items(report_id: int) -> list[dict]:
    """获取收入支出表条目"""
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(
                """SELECT id, name, amount, category, type, note, is_interest, interest_amount
                   FROM income_expense_items WHERE report_id = %s ORDER BY type, category, name""",
                (report_id,),
            )
            return cursor.fetchall()
    finally:
        conn.close()


def compute_cashflow(income_expense_items: list[dict], balance_sheet_items: list[dict]) -> dict:
    """
    根据已获取的收入支出和资产负债数据，计算现金流（复制 Java 端 FinanceController 逻辑）
    """
    items = income_expense_items
    bs_items = balance_sheet_items

    # --- 劳动收入 ---
    labor_items = [
        i for i in items
        if i["type"] == "INCOME" and i["category"] == "LABOR_INCOME"
    ]
    labor_income = sum(i["amount"] or 0 for i in labor_items)

    # --- 资产收入 ---
    asset_income_map: dict[str, float] = {}
    for i in items:
        if i["type"] == "INCOME" and i["category"] == "ASSET_INCOME":
            asset_income_map[i["name"]] = i["amount"] or 0
    asset_income = sum(asset_income_map.values())

    # --- 生活支出 ---
    living_items = [
        i for i in items
        if i["type"] == "EXPENSE" and i["category"] == "LIVING_EXPENSE"
    ]
    living_expense = sum(i["amount"] or 0 for i in living_items)

    # --- 负债名称集合 (来自资产负债表) ---
    debt_categories = {"CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"}
    bs_debts = [b for b in bs_items if b["category"] in debt_categories]
    debt_names = {b["name"] for b in bs_debts}

    # --- 利息支出 ---
    interest_expense_map: dict[str, float] = {}
    for i in items:
        if i["type"] == "EXPENSE" and i.get("is_interest"):
            if i["name"] not in debt_names:
                interest_expense_map[i["name"]] = i["amount"] or 0
    interest_expense = sum(interest_expense_map.values())

    # --- 资产支出 ---
    asset_expense_map: dict[str, float] = {}
    # 1. 资产负债表负债的利息
    for debt in bs_debts:
        if debt.get("is_interest") and debt.get("interest_amount") and debt["interest_amount"] > 0:
            asset_expense_map[debt["name"] + " (利息)"] = debt["interest_amount"]
    # 2. 其他资产支出
    for i in items:
        if i["type"] == "EXPENSE" and i["category"] in ("ASSET_EXPENSE", "LOAN_REPAYMENT"):
            if i["name"] in debt_names:
                if i.get("is_interest") and i.get("interest_amount") and i["interest_amount"] > 0:
                    asset_expense_map[i["name"]] = i["interest_amount"]
            else:
                asset_expense_map[i["name"]] = i["amount"] or 0
    asset_expense = sum(asset_expense_map.values())

    total_income = labor_income + asset_income
    total_expense = living_expense + interest_expense + asset_expense
    net_cash_flow = total_income - total_expense

    return {
        "total_income": total_income,
        "total_expense": total_expense,
        "net_cash_flow": net_cash_flow,
        "labor_income": labor_income,
        "asset_income": asset_income,
        "living_expense": living_expense,
        "interest_expense": interest_expense,
        "asset_expense": asset_expense,
    }


def get_full_financial_data(user_id: int) -> dict:
    """
    获取用户完整的财务数据摘要，供 LLM 分析使用
    返回结构：用户信息 + 所有报表（含资产负债、收入支出、现金流计算结果）
    """
    user = get_user_info(user_id)
    if not user:
        return {"error": "用户不存在"}

    reports = get_user_reports(user_id)
    reports_data = []

    for report in reports:
        report_id = report["id"]
        bs_items = get_balance_sheet_items(report_id)
        ie_items = get_income_expense_items(report_id)
        cashflow = compute_cashflow(ie_items, bs_items)

        # 资产负债汇总
        asset_cats = ["INVESTMENT_ASSET", "CONSUMER_ASSET", "PERSONAL_ASSET"]
        debt_cats = ["INVESTMENT_DEBT", "CONSUMER_DEBT", "PERSONAL_DEBT"]
        total_assets = sum(i["amount"] or 0 for i in bs_items if i["category"] in asset_cats)
        total_debts = sum(i["amount"] or 0 for i in bs_items if i["category"] in debt_cats)

        reports_data.append({
            "report_id": report_id,
            "report_name": report["name"],
            "report_type": "个人" if report["type"] == "PERSONAL" else "家庭",
            "balance_sheet": {
                "total_assets": total_assets,
                "total_debts": total_debts,
                "net_worth": total_assets - total_debts,
                "items": bs_items,
            },
            "income_expense": {
                "items": ie_items,
            },
            "cashflow": cashflow,
        })

    return {
        "user": {
            "username": user.get("username"),
            "email": user.get("email"),
            "gender": user.get("gender"),
            "age": user.get("age"),
        },
        "reports": reports_data,
    }
