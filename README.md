[![build status](https://github.com/opengoofy/hippo4j/actions/workflows/ci.yml/badge.svg?event=push)](https://github.com/opengoofy/hippo4j)
[![codecov](https://codecov.io/gh/opengoofy/hippo4j/branch/develop/graph/badge.svg?token=WBUVJN107I)](https://codecov.io/gh/opengoofy/hippo4j)
![maven](https://img.shields.io/maven-central/v/com.alibaba.otter/canal.svg)
[![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![](https://img.shields.io/github/contributors/opengoofy/hippo4j)
[![percentage of issues still open](http://isitmaintained.com/badge/open/opengoofy/hippo4j.svg)](http://isitmaintained.com/project/opengoofy/hippo4j "percentage of issues still open")

## 简介

![](https://oss.open8gu.com/image-20231115133642504.png)

短链接（Short Link）是指将一个原始的长 URL（Uniform Resource Locator）通过特定的算法或服务转化为一个更短、易于记忆的
URL。短链接通常只包含几个字符，而原始的长 URL 可能会非常长。

短链接的原理非常简单，通过一个原始链接生成个相对短的链接，然后通过访问短链接跳转到原始链接。

如果更细节一些的话，那就是：

1. **生成唯一标识符**：当用户输入或提交一个长 URL 时，短链接服务会生成一个唯一的标识符或者短码。
2. **将标识符与长 URL 关联**：短链接服务将这个唯一标识符与用户提供的长 URL 关联起来，并将其保存在数据库或者其他持久化存储中。
3. **创建短链接**：将生成的唯一标识符加上短链接服务的域名（例如：http://nurl.ink ）作为前缀，构成一个短链接。
4. **重定向**：当用户访问该短链接时，短链接服务接收到请求后会根据唯一标识符查找关联的长 URL，然后将用户重定向到这个长 URL。
5. **跟踪统计**：一些短链接服务还会提供访问统计和分析功能，记录访问量、来源、地理位置等信息。

短链接经常出现在咱们日常生活中，大家总是能在某些活动节日里收到各种营销短信，里边就会出现短链接。帮助企业在营销活动中，识别用户行为、点击率等关键信息监控。

![](https://oss.open8gu.com/IMG_9858-20231126.jpg)

主要作用包括但不限于以下几个方面：

- **提升用户体验**：用户更容易记忆和分享短链接，增强了用户的体验。
- **节省空间**：短链接相对于长 URL 更短，可以节省字符空间，特别是在一些限制字符数的场合，如微博、短信等。
- **美化**：短链接通常更美观、简洁，不会包含一大串字符。
- **统计和分析**：可以追踪短链接的访问情况，了解用户的行为和喜好。

## 技术架构

在系统设计中，采用最新 JDK17 + SpringBoot3&SpringCloud 微服务架构，构建高并发、大数据量下仍然能提供高效可靠的短链接生成服务。

通过学习短链接项目，不仅能了解其运作机制，还能接触最新技术体系带来的新特性，从而拓展技术视野并提升自身技术水平。

![](https://oss.open8gu.com/image-20231026132606180.png)

