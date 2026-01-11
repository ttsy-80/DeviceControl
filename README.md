# 发动机设备控制系统

基于Android平台开发的发动机型号管理和任务控制系统。

## 技术栈

- **语言**: Kotlin
- **架构**: MVVM (Model-View-ViewModel)
- **数据库**: Room
- **UI框架**: XML布局 + ViewBinding
- **最低支持**: Android API 21 (Android 5.0)

## 项目结构

项目采用模块化架构，分为两个模块：

### app模块（应用入口）
```
app/
└── src/main/
    ├── java/com/devicecontrol/app/
    │   └── MainActivity.kt       # 主入口Activity
    └── res/
        └── layout/
            └── activity_main.xml
```

### engine-lib模块（核心功能库，可打包为AAR）
```
engine-lib/
└── src/main/
    ├── java/com/devicecontrol/engine/
    │   ├── data/
    │   │   ├── model/              # 数据模型
    │   │   ├── database/           # Room数据库
    │   │   └── repository/         # 数据仓库层
    │   ├── ui/
    │   │   ├── model/             # 型号管理模块
    │   │   ├── task/              # 任务创建模块
    │   │   └── control/            # 任务控制模块
    │   └── viewmodel/             # ViewModel层
    └── res/                        # 所有资源文件
```

**注意**: 所有Activity已设置为横屏显示（`android:screenOrientation="landscape"`）

## 核心功能

### 1. 型号管理
- 新建型号（必须至少添加一个配置项）
- 添加/删除配置项（变速比、位置、叶片数、点动次数）
- 删除型号（级联删除所有配置项）
- 自动删除空型号（当所有配置项被删除时）

### 2. 任务创建
- 选择型号
- 多选变速比
- 创建任务

### 3. 任务控制
- 启动/暂停/停止任务
- 正转/反转控制
- 点动/连续模式切换
- 力矩和速度调整
- 多任务切换（当选择多个变速比时）

## 数据模型

### EngineModel (型号)
- id: Long
- name: String

### ConfigItem (配置项)
- id: Long
- modelId: Long
- gearRatio: Double (变速比)
- position: String (位置)
- bladeCount: Int (叶片数)
- jogCount: Int (点动次数)

### Task (任务)
- id: Long
- modelId: Long
- modelName: String
- gearRatios: List<Double> (变速比列表)

### TaskExecution (任务执行)
- executionId: Long
- taskId: Long
- currentGearRatioIndex: Int
- status: TaskStatus
- torque: Double
- speed: Double
- rotationDirection: RotationDirection
- operationMode: OperationMode
- progress: Int

## 构建说明

### 运行应用

1. 使用Android Studio打开项目
2. 同步Gradle依赖
3. 连接Android设备或启动模拟器
4. 运行app模块

### 生成AAR文件

engine-lib模块可以打包为AAR文件供其他项目使用：

```bash
# 生成Release版本的AAR
./gradlew :engine-lib:assembleRelease
```

生成的AAR文件位于：`engine-lib/build/outputs/aar/engine-lib-release.aar`

详细说明请参考 [BUILD_AAR.md](BUILD_AAR.md)

## 依赖库

- androidx.core:core-ktx
- androidx.appcompat:appcompat
- com.google.android.material:material
- androidx.lifecycle:lifecycle-viewmodel-ktx
- androidx.lifecycle:lifecycle-livedata-ktx
- androidx.room:room-runtime
- androidx.room:room-ktx
- androidx.recyclerview:recyclerview
- org.jetbrains.kotlinx:kotlinx-coroutines-android
- com.google.code.gson:gson

## 注意事项

### 业务规则
1. 新建型号时必须至少添加一个配置项
2. 删除最后一个配置项时，型号会自动删除
3. 只有包含配置项的型号才能被任务选择
4. 任务创建时必须至少选择一个变速比
5. 多任务切换功能仅在选择多个变速比时启用

### 技术特性
1. **横屏显示**: 所有Activity已设置为横屏模式（`landscape`）
2. **模块化设计**: 核心功能在engine-lib模块，可独立打包为AAR
3. **AAR打包**: engine-lib模块支持生成AAR文件，供其他项目使用

## 开发状态

✅ 已完成所有核心功能开发
- 数据层实现
- UI层实现
- ViewModel层实现
- 业务逻辑实现

## 后续优化建议

1. 数据导入导出功能
2. 任务历史记录
3. 参数预设功能
4. 任务模板功能
5. 数据统计功能
