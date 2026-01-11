# AAR 打包说明

## 项目结构

项目已重构为模块化结构：
- **app模块**: 应用入口，仅包含MainActivity
- **engine-lib模块**: 核心功能库，包含所有业务逻辑

## 生成AAR文件

### 方法一：使用Gradle命令

在项目根目录执行以下命令：

```bash
# 生成Debug版本的AAR
./gradlew :engine-lib:assembleDebug

# 生成Release版本的AAR
./gradlew :engine-lib:assembleRelease
```

生成的AAR文件位置：
```
engine-lib/build/outputs/aar/engine-lib-debug.aar
engine-lib/build/outputs/aar/engine-lib-release.aar
```

### 方法二：使用Android Studio

1. 打开Android Studio
2. 在右侧Gradle面板中，展开 `engine-lib` -> `Tasks` -> `build`
3. 双击 `assembleRelease` 或 `assembleDebug`
4. AAR文件将生成在 `engine-lib/build/outputs/aar/` 目录

## 使用AAR

### 在其他项目中引入AAR

1. 将AAR文件复制到项目的 `libs` 目录
2. 在 `build.gradle` 中添加：

```gradle
dependencies {
    implementation(name: 'engine-lib-release', ext: 'aar')
}

// 在android块中添加
repositories {
    flatDir {
        dirs 'libs'
    }
}
```

### 或者发布到Maven仓库

在 `engine-lib/build.gradle.kts` 中添加发布配置：

```kotlin
plugins {
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = "com.devicecontrol"
            artifactId = "engine-lib"
            version = "1.0.0"
        }
    }
}
```

## 注意事项

1. AAR包含所有资源文件（layout、drawable、values等）
2. AAR包含所有Java/Kotlin类文件
3. AAR不包含AndroidManifest.xml中的application标签内容
4. 使用AAR的项目需要自行配置依赖项（Room、ViewModel等）

## 版本信息

- 当前版本: 1.0.0
- 最低SDK: 21
- 目标SDK: 34
- 编译SDK: 34
