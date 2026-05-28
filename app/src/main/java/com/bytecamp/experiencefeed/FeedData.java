package com.bytecamp.experiencefeed;

import java.util.Arrays;
import java.util.List;

final class FeedData {
    private FeedData() {
    }

    static List<ImagePost> posts() {
        return Arrays.asList(
                post("01", "傍晚的城市窗口", "玻璃反光里藏着一整片天空", "2026.05.12", "上海 徐汇", 250),
                post("02", "山路边的风", "绕过弯道后突然安静下来", "2026.05.13", "云南 大理", 310),
                post("03", "旧街口早餐铺", "热气、木桌和刚醒来的街道", "2026.05.14", "广东 广州", 230),
                post("04", "雨后露台", "地面把灯光慢慢铺开", "2026.05.15", "浙江 杭州", 285),
                post("05", "周末书桌", "咖啡放在右手边，灵感也在", "2026.05.16", "北京 朝阳", 245),
                post("06", "海边蓝色时刻", "浪声在天黑之前变得柔软", "2026.05.17", "福建 厦门", 330),
                post("07", "清晨地铁站", "第一班车把城市重新点亮", "2026.05.18", "江苏 南京", 270),
                post("08", "屋顶花园", "风穿过树叶，像一段低声旁白", "2026.05.19", "四川 成都", 240),
                post("09", "博物馆侧门", "石阶、光影和很慢的下午", "2026.05.20", "陕西 西安", 300),
                post("10", "夜色便利店", "路过的人都被灯光短暂收留", "2026.05.21", "重庆 渝中", 260),
                post("11", "湖边慢跑线", "水面把云压得很低", "2026.05.22", "湖北 武汉", 320),
                post("12", "窗台植物", "新叶子总是最会表达春天", "2026.05.23", "天津 和平", 235)
        );
    }

    private static ImagePost post(
            String id,
            String title,
            String description,
            String date,
            String location,
            int height
    ) {
        String url = "https://picsum.photos/seed/bytecamp-" + id + "/720/" + (height * 3);
        return new ImagePost(id, title, description, date, location, url, height);
    }
}
