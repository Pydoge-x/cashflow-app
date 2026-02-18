# CashFlow App (家庭/个人数字财务报表系统)

![Financial Health](https://img.shields.io/badge/Financial-Health-success)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)
![Vue 3](https://img.shields.io/badge/Vue-3.x-blue)
![License](https://img.shields.io/badge/License-MIT-orange)

这是一个基于 **Spring Boot** 和 **Vue 3** 构建的专业级财务管理系统。与普通的记账软件不同，CashFlow 深度融入了**会计学原理**，通过资产负债表、收入支出表与现金流表的“三表联动”，为用户提供真实、精确的财富数字化管理方案。

## 🌟 核心设计理念

项目的灵魂在于**资产与负债的清晰界定**，以及**存量与流量的逻辑闭环**。

### 1. 三大核心报表体系

- **资产负债表 (Balance Sheet)**：反映您的“财务底子”。
  - **资产 (Assets)**：流动资产（手头的钱）、投资资产（生钱的钱）、个人资产（生活用的资产）。
  - **负债 (Liabilities)**：消费负债（高息账单）、投资负债（房贷杠杆）、个人负债。
- **收入支出表 (Income & Expense Statement)**：记录您的“月度盈余”。
  - **联动逻辑**：系统会自动感知资产与负债的状态，将利息支出与资产收益精确呈现在此表中。
- **现金流表 (Cash Flow Statement)**：展现真实的“资金活水”。
  - **精准统计**：自动剔除“资产转移”类支出（如买入股票、定投基金），仅计算真正的现金流消耗（利息、生活费）。

### 2. 精准的利息与本金分离

系统在处理负债（如贷款）时，严格遵循会计准则：

- **本金偿还**：视为负债的减少（资产负债表变动），不计入现金流支出总额。
- **利息支付**：视为纯粹的费用消耗（收入支出表变动），是现金流支出的核心组成部分。

## 🚀 核心功能特性

- **多维报表管理**：支持“个人”与“家庭”双重账户体系，独立核算，互不干扰。
- **三表自动联动**：
  - 在资产负债表修改负债利息，收入支出表实时感知。
  - 在收入支出表同步本金变动，资产负债表自动更新。
  - 现金流表全自动化生成，无需手动计算。
- **可视化数据分析**：内置财务分析图表（ECharts 实现），直观展示各项资产占比、支出构成及月度结余趋势。
- **智慧金融百科**：新增**金融名词解析（Glossary）**功能。通过首页悬浮球即可一键查阅专业财务学术语，帮助非专业用户快速上手。
- **响应式极致体验**：支持暗黑模式，采用玻璃拟态设计，完美适配 PC 与移动端浏览器。

## 🛠 技术深度与实现细节

### 后端技术栈 (Java)

- **核心引擎**: Spring Boot 3.4.1 (Java 17)
- **安全体系**: RESTful API + **JWT (JJWT 0.12.3)** 实现无状态身份认证。
- **数据持久化**: Spring Data JPA + MySQL。
- **业务逻辑**: 实现了复杂的财务计算引擎，支持负债利息的动态同步与现金流自动分类映射。

### 前端技术栈 (Vue)

- **框架架构**: Vue 3 + Vite。
- **状态流转**: Pinia 集中管理财务状态，实现多视图间的数据实时联动。
- **可视化**: ECharts 全量支持财务报表的图形化展示。
- **响应式布局**: 原生 CSS3 变量与 Flex/Grid 布局，提供丝滑的跨设备交互体验。

## 📂 项目结构

```text
cashflow-app/
├── cashflow/              # 后端服务
│   ├── src/main/java/     # 核心 Controller/Entity/Repository
│   └── src/main/resources/# 数据库配置与权限策略
├── fronted/               # 前端应用
│   ├── src/views/         # 仪表盘、三表视图、图表页、百科页
│   ├── src/stores/        # Pinia 状态中心 (Finance/Auth)
│   └── src/components/    # AppLayout.vue 布局组件
└── README.md              # 项目白皮书
```

## 📖 快速上手

### 环境准备

- **JDK 17+**
- **Node.js 18+**
- **MySQL 8.0+**

### 启动步骤

1. **数据库配置**：在 `cashflow/src/main/resources/application.yml` 中配置您的数据库连接。
2. **启动后端**：`cd cashflow && mvn spring-boot:run`
3. **启动前端**：`cd fronted && npm install && npm run dev`
4. **即刻使用**：访问 `http://localhost:5173`。

## 🤝 贡献与反馈

本项目旨在为财富管理提供一种理性的工具。如果您有任何关于会计逻辑或功能优化的建议，欢迎提交 Issue 或 Pull Request。

---

**License**: [MIT](LICENSE)  
_愿每一位用户都能通过清晰的账目，实现财富自由。_
