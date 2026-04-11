# 油耗记录 (FuelRecord)

极简油耗记录 Android APP，纯本地存储，无联网、无广告。

## 功能特性

### 加油记录
- **表单字段**：日期、总里程、加油升数、油价、金额、是否加满、备注
- **智能计算**：输入升数和油价后自动计算金额；输入升数和金额后自动计算油价
- **编辑/删除**：点击记录可编辑，支持删除确认

### 油耗计算
- **百公里油耗**：基于相邻两次加满油的里程差自动计算（L/100km）
- **每公里成本**：自动计算每公里花费（元/km）
- **条件说明**：仅在"加满"状态下计算油耗，未加满的记录不计算（因无法准确估算）

### 统计分析
- **总览**：累计加油量、总花费、平均油耗
- **年度统计**：按年汇总加油次数、总里程、加油量、花费、平均油耗、平均成本
- **月度统计**：按月汇总加油量、花费、平均油耗
- **油耗趋势**：简易条形图展示月度油耗变化

### 数据管理
- **导出 CSV**：将所有记录导出为 CSV 文件，方便备份或在电脑上查看
- **导入 CSV**：从 CSV 文件导入记录，支持"追加"和"替换"两种模式
- **升级保留数据**：使用 Room Migration 机制，应用升级时已有数据不会丢失

## 安装说明

### 环境要求
- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 8+
- Android SDK 34
- 最低运行 Android 8.0 (API 26)

### 构建步骤
1. 用 Android Studio 打开项目根目录 `FuelRecord/`
2. 等待 Gradle 同步完成（首次可能需要下载依赖）
3. 连接 Android 设备或启动模拟器
4. 点击 **Run** (▶) 按钮，选择目标设备

### 生成 APK
1. 菜单 → **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. 生成文件位于 `app/build/outputs/apk/debug/app-debug.apk`
3. 将 APK 传输到手机安装即可

## 使用说明

### 首次使用
1. 打开 APP，首页显示"暂无加油记录"
2. 点击右上角 **+** 按钮添加第一条加油记录
3. 填写日期、总里程、加油升数、油价等信息
4. 勾选"已加满"（建议每次加满以获得准确油耗）
5. 点击"添加记录"保存

### 记录管理
- **查看列表**：首页显示所有加油记录，按日期倒序排列
- **编辑记录**：点击任意记录卡片进入编辑页面
- **删除记录**：点击记录右侧的删除图标，或在编辑页面点击右上角删除按钮

### 查看统计
- 点击首页右上角的 **ℹ** 图标进入统计页面
- 查看总体统计数据和月度统计明细
- 油耗趋势图展示近12个月的百公里油耗变化

### 油耗计算说明
- 需要至少2条"已加满"的记录才能计算油耗
- 油耗 = 本次加油升数 ÷ (本次里程 - 上次里程) × 100
- 建议每次加油都加满，以获得最准确的油耗数据

## 技术架构

| 模块 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material3 |
| 数据库 | Room (SQLite) |
| 导航 | Navigation Compose |
| 架构 | MVVM (ViewModel + StateFlow) |

## 项目结构

```
app/src/main/java/com/example/fuelrecord/
├── data/
│   ├── FuelRecord.kt          # 数据实体
│   ├── FuelRecordDao.kt       # 数据访问对象
│   ├── AppDatabase.kt         # Room 数据库
│   └── CsvUtils.kt            # CSV 导入导出工具
├── viewmodel/
│   └── FuelRecordViewModel.kt # 业务逻辑层
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt   # 页面导航
│   ├── screen/
│   │   ├── RecordListScreen.kt    # 记录列表页
│   │   ├── AddEditRecordScreen.kt # 添加/编辑页
│   │   └── StatisticsScreen.kt    # 统计页
│   └── theme/                 # 主题配置
└── MainActivity.kt            # 入口
```
