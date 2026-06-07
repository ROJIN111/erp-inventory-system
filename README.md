# ERP Inventory System

ERP Inventory System 是一个基于 `Spring Boot + Vue 3 + MySQL` 的中小型企业库存管理系统，支持通过 Docker Compose 一键启动。

系统围绕商品主数据、入库、出库、库存盘点、库存流水、库存预警、用户权限和操作日志等场景，构建了一套可追踪、可审核、可复现的库存管理流程。

联系方式：2812447865@qq.com

## 项目价值

传统仓库管理经常依赖 Excel、纸质单据或人工台账，容易出现库存变化难追踪、审核责任不清晰、库存预警滞后、不同岗位权限边界模糊等问题。

本系统通过前后端分离、单据审核、库存流水、库存预警和角色权限控制，让库存管理从简单记录提升到可追踪、可审核、可分析的业务流程。

## 核心功能

| 模块 | 说明 |
| --- | --- |
| 首页仪表盘 | 展示商品数量、库存预警、入库统计、出库统计和图表汇总。 |
| 商品管理 | 维护商品名称、SKU、分类、单位、价格、当前库存和安全库存上下限。 |
| 入库管理 | 创建入库单，审核通过后自动增加库存并生成库存流水。 |
| 出库管理 | 创建出库单，审核时校验库存，审核通过后扣减库存并生成库存流水。 |
| 库存盘点 | 支持盘点单、盘点明细、库存快照和差异调整。 |
| 库存流水 | 记录每次库存变化的商品、类型、数量、变更前后库存、关联单据和操作人。 |
| 库存预警 | 根据安全库存上下限生成低库存或高库存预警，并支持处理记录。 |
| 用户与权限 | 基于 JWT 和 RBAC 实现登录认证、角色权限和接口访问控制。 |
| 操作日志 | 使用 AOP 记录关键业务操作，方便后续追踪和排查问题。 |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.0.4、Spring Security、MyBatis-Plus、JJWT、EasyExcel、Knife4j |
| 前端 | Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios、ECharts |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose、Nginx、Maven、npm |

## 系统架构

```text
浏览器
  |
  v
Vue 3 前端 / Nginx  (:3000)
  |
  | /api 反向代理
  v
Spring Boot 后端    (:8080)
  |
  v
MySQL 8.0           (:3307 -> 3306)
```

Docker Compose 会启动三个服务：

- `mysql`：提供 MySQL 8.0 数据库，并执行初始化 SQL。
- `backend`：构建并运行 Spring Boot 后端服务。
- `web`：构建 Vue 前端，并通过 Nginx 提供静态资源和接口代理。

## 快速启动

环境要求：

- Docker
- Docker Compose

启动全部服务：

```bash
docker compose up --build
```

启动完成后访问：

```text
http://localhost:3000
```

默认账号：

```text
admin / admin123
```

## 服务端口

| 服务 | 端口 | 说明 |
| --- | --- | --- |
| Web | `3000` | Vue 前端，由 Nginx 提供访问 |
| Backend | `8080` | Spring Boot REST API |
| MySQL | `3307` | 映射到容器内 `3306` 端口 |

如果本机端口已被占用，可以修改 `docker-compose.yml` 中的 `ports` 映射。

## 数据库初始化

MySQL 容器会自动创建 `erp_inventory` 数据库，并按顺序执行：

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/main/resources/demo-data.sql`

数据库数据保存在 Docker volume：`mysql-data`。

如果需要清空数据库并重新初始化：

```bash
docker compose down -v
docker compose up --build
```

## 常用命令

后台启动：

```bash
docker compose up -d --build
```

查看服务状态：

```bash
docker compose ps
```

查看全部日志：

```bash
docker compose logs
```

只查看后端日志：

```bash
docker compose logs backend
```

停止服务：

```bash
docker compose down
```

## 仓库范围

公开仓库保留 Docker 一键部署所需的源码、SQL 脚本和 Docker 配置：

- `docker-compose.yml`
- `Dockerfile`
- `docker/maven-settings.xml`
- `pom.xml`
- `src/main`
- `erp-inventory-web`

## 项目亮点

- 覆盖商品、入库、出库、盘点、流水、预警、权限和日志等完整库存业务流程。
- 入库和出库通过审核后才会影响库存，降低误操作风险。
- 库存变化会生成流水记录，便于追踪库存来源和变化原因。
- 基于 JWT 和 RBAC 实现用户认证与权限控制。
- 使用 Docker Compose 一条命令启动 MySQL、后端和前端，降低部署和运行门槛。
