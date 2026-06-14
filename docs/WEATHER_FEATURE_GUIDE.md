# 实时天气功能说明

## 功能效果

- 经验页在标题和双列图片流之间展示实时天气。
- 首次启动默认查询上海。
- 点击「修改地点」可以输入北京、杭州等城市。
- 成功查询后保存城市，下次启动继续使用。
- 展示温度、天气、体感温度、湿度、风速和更新时间。

## 为什么需要两次请求

用户输入的是“上海”，但天气接口需要的是经纬度。因此完整流程是：

```text
上海
  ↓ 城市搜索接口
纬度 31.22、经度 121.46
  ↓ 实时天气接口
温度、天气代码、湿度、风速
  ↓ Gson 解析与天气代码转换
首页天气卡片
```

## 核心文件

- `WeatherService.java`：使用 Retrofit 注解声明城市搜索和天气请求。
- `WeatherRepository.java`：配置 OkHttp、串联两次请求、处理错误、转换天气代码。
- `MainActivity.java`：创建天气卡片、显示加载状态、弹出修改地点对话框。
- `app/build.gradle`：引入 `converter-gson`，把天气 JSON 自动转换为 Java 对象。

## 技术栈分工

- Retrofit：把 Java 接口方法和参数转换成 HTTP 请求。
- OkHttp：真正建立网络连接并执行请求。
- Gson Converter：把接口返回的 JSON 转成 Java 数据对象。
- SharedPreferences：保存用户最后选择的城市。
- Open-Meteo：提供城市搜索和实时天气数据，不需要 API Key。

## 核心思路

天气接口需要经纬度，但用户输入的是城市名，因此这里使用 Retrofit 串联城市搜索和实时天气两次请求，再用 Gson 把 JSON 转成 Java 对象，并使用 SharedPreferences 保存用户修改后的地点。
