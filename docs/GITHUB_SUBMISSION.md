# GitHub 提交说明

## 1. 提交内容

GitHub 仓库需要包含：

- Android 项目完整代码。
- `README.md` 项目说明。
- `docs/TECHNICAL_SOLUTION.md` 技术方案文档。
- `docs/DESIGN_PRESENTATION.md` 作业点评讲解稿。
- `docs/LEARNING_SUMMARY.md` 网络请求问题与优化总结。
- `docs/DEMO_SCRIPT.md` 3 分钟演示录屏脚本。
- `demo/` 目录，用于放置最终演示视频。

不需要提交：

- `build/`
- `.gradle/`
- `.idea/`
- `local.properties`
- `*.apk`
- `*.aab`

这些已经在 `.gitignore` 中排除。

## 2. 首次提交命令

如果还没有创建 Git 仓库，可以在项目根目录执行：

```bash
git init
git add .
git commit -m "Complete Android image feed homework"
git branch -M main
```

然后在 GitHub 新建一个仓库，例如：

```text
experience-feed-android
```

把远程仓库地址添加到本地：

```bash
git remote add origin <你的 GitHub 仓库地址>
git push -u origin main
```

## 3. 提交前检查

提交前建议确认：

- Android Studio 可以正常运行 App。
- 首页双列图片流可以展示。
- 点击卡片可以进入详情页。
- 详情页返回按钮可以回到首页。
- 图片首次加载走网络请求。
- 二次进入可以命中内存缓存或磁盘缓存。
- `docs/` 目录中的文档已经更新。
- 演示录屏已放入 `demo/` 目录。

## 4. 推荐仓库描述

GitHub 仓库描述可以写：

```text
Android homework: Douyin-style two-column image feed with OkHttp image loading and local cache.
```

## 5. 推荐提交说明

最终提交信息可以使用：

```text
Complete Android image feed homework
```
