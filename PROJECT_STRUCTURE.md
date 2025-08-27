# VanGogh 项目结构文档

## 📁 项目架构概览

```
com.hy.vangogh/
├── data/                           # 数据层
│   ├── model/                      # 数据模型
│   │   ├── ImageFilter.kt         # 滤镜数据模型
│   │   └── Project.kt             # 项目数据模型
│   └── repository/                 # 数据仓库
│       └── ProjectRepository.kt    # 项目数据管理
├── imageprocess/                   # 图像处理层
│   ├── core/                       # 核心接口和主处理器
│   │   ├── ImageProcessorInterface.kt  # 处理器接口定义
│   │   ├── ImageProcessorFactory.kt    # 工厂模式创建器
│   │   └── MainImageProcessor.kt       # 主图像处理器
│   ├── adjustments/                # 颜色调整处理器
│   │   └── ColorAdjustmentProcessor.kt # 亮度/对比度/饱和度/色温
│   └── filters/                    # 特效滤镜处理器
│       ├── BlurProcessor.kt        # 模糊效果
│       ├── SharpenProcessor.kt     # 锐化效果
│       └── VignetteProcessor.kt    # 暗角效果
├── presentation/                   # 表示层
│   └── viewmodel/                  # 视图模型
│       └── ImageEditViewModel.kt   # 图像编辑视图模型
├── ui/                            # UI层
│   ├── screens/                   # 屏幕组件
│   │   ├── HomeScreen.kt          # 项目管理主页
│   │   └── ImageEditScreen.kt     # 图像编辑页面
│   └── theme/                     # 主题配置
├── navigation/                    # 导航管理
│   └── VanGoghNavigation.kt      # 应用导航配置
└── MainActivity.kt               # 主活动
```

## 🏗️ 架构设计原则

### 1. 分层架构
- **数据层 (Data Layer)**: 处理数据存储、网络请求和数据模型
- **业务逻辑层 (Domain Layer)**: 图像处理算法和业务规则
- **表示层 (Presentation Layer)**: ViewModel和UI状态管理
- **UI层 (UI Layer)**: Compose界面组件

### 2. 模块化设计
- **图像处理模块化**: 每个处理功能独立成类，便于维护和扩展
- **接口驱动**: 使用接口定义规范，支持依赖注入和测试
- **工厂模式**: 统一创建和管理处理器实例

### 3. 职责分离
- **ColorAdjustmentProcessor**: 专门处理颜色调整（亮度、对比度、饱和度、色温）
- **BlurProcessor**: 专门处理模糊效果（支持RenderScript和Gaussian算法）
- **SharpenProcessor**: 专门处理锐化效果（卷积核算法）
- **VignetteProcessor**: 专门处理暗角效果（径向渐变）

## 🔧 核心组件说明

### ImageProcessorInterface
定义了图像处理的核心接口，包括：
- `applyFilter()`: 应用滤镜到图像
- `saveBitmap()`: 保存处理后的图像

### MainImageProcessor
主图像处理器，协调各个专门处理器：
- 使用工厂模式创建处理器实例
- 按顺序应用各种效果
- 支持异步处理

### ProjectRepository
项目数据管理：
- 使用SharedPreferences + Gson进行数据持久化
- 支持项目CRUD操作
- 提供StateFlow响应式数据流

## 📦 依赖管理

### 核心依赖
- Jetpack Compose: UI框架
- Navigation Compose: 导航管理
- ViewModel Compose: 状态管理
- Coil: 图像加载
- Gson: JSON序列化

### 图像处理依赖
- RenderScript: 高性能模糊处理（API 17+）
- Canvas + Paint: 基础图像绘制
- ColorMatrix: 颜色变换

## 🚀 扩展指南

### 添加新滤镜
1. 在`imageprocess/filters/`下创建新的处理器类
2. 实现`FilterProcessor`接口
3. 在`ImageProcessorFactory`中添加创建方法
4. 在`MainImageProcessor`中集成新处理器

### 添加新调整功能
1. 在`ColorAdjustmentProcessor`中添加新方法
2. 或创建新的调整处理器类
3. 在`ImageFilter`数据模型中添加对应参数
4. 更新UI控件和ViewModel

## 🎯 性能优化

### 图像处理优化
- 使用RenderScript进行GPU加速（模糊效果）
- 异步处理避免UI阻塞
- 内存管理和Bitmap回收

### 数据存储优化
- 使用StateFlow进行响应式更新
- JSON序列化减少存储空间
- 延迟加载和缓存策略
