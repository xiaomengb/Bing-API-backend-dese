# Bing API

> 一个API开放调用平台，为开发者提供便捷、安全的API调用体验；
>
>Java + React 全栈项目，包括网站前台+后台
> 
>
> 在线体验地址：[Bing API](http://bing-api.cxbym.live)
>
> 项目前端开源地址：[https://github.com/xiaomengb/Bing-API-frontend](https://github.com/xiaomengb/Bing-API-frontend)








## 系统架构
![image-20230912092400984](/image/系统架构-1694576294565-3)






## 技术栈

### 前端技术栈

- 开发框架：React、Umi
- 脚手架：Ant Design Pro
- 组件库：Ant Design、Ant Design Components
- 语法扩展：TypeScript、Less
- 打包工具：Webpack
- 代码规范：ESLint、StyleLint、Prettier



### 后端技术栈

- 主语言：Java
- 框架：SpringBoot、Mybatis-plus、Spring Cloud
- 数据库：Mysql、Redis
- 中间件：RabbitMq
- 注册中心：Nacos
- 服务调用：Dubbo
- 网关：Spring Cloud Gateway



## 项目模块

- Bing-API-backend ：项目前端
- api-common ：公共封装类（如公共实体、公共常量，统一响应实体，统一异常处理、定义rpc接口）
- api-backend ：接口管理平台，主要包括用户服务、接口的在线调用和展示
- api-gateway ：网关服务，**涉及到接口调用的统一鉴权，统一日志处理，接口统计，接口数据一致性处理，异常处理等核心业务**
- api-order ：订单服务，接口订单相关
- api-third-party：第三方服务，阿里云短信、支付宝沙箱支付等功能
- api-interface：模拟接口，真正提供接口服务的地方
- api-sdk：SDK工具包，提供给开发者使用







## 功能模块

- 用户、管理员
  - 登录注册：账号和手机号注册登录
  - 个人主页：包括上传头像、修改个人信息、查看密钥、重新生成密钥
  - 管理员：用户管理
  - 管理员：接口管理、用户管理、接口可视化分析
- 接口
  - 接口商城
  - 在线调用
  - SDK调用接口
  - 购买接口
  - 下载SDK
- 订单
  - 创建订单
  - 订单超时回滚
  - 支付宝沙箱支付


## 快速上手

#### 后端

1. 启动Nacos、Mysql、Redis、RabbitMq
2. 将各模块配置修改成你自己本地的端口、账号、密码
4. 按顺序启动服务

服务启动顺序参考：
1. api-backend
2. api-order
3. api-gateway
4. api-third-party
5. api-interface

#### 前端

环境要求：Node.js >= 16

安装依赖：

```
yarn
```

启动：

```
npm run start:dev
```
**注意：如果想要体验订单和支付业务，需要公网ip,也可以内网穿透**



## SDK使用

### 说明

提供给开发者在代码层面实现远程调用平台所提供api的能力

除了基本的api客户端，还提供了一些封装好的工具类，便于开发者更好的解析调用结果



### 环境要求

JDK版本要在8以上

SpringBoot版本要求2.x



### Maven安装

```xml
<dependency>
    <groupId>com.cxb</groupId>
    <artifactId>api-client-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

- SDK在Bing API平台个人中心中下载，放到maven本地仓库

### 代码示例

依赖成功引入后，需要在`application.yml`配置文件中进行相关配置

```yml
# 开发者签名认证
bing-api:
  client:
    access-key: #在Bing API平台个人中心中查看和申请
    secret-key: #在Bing API平台个人中心中查看和申请
```

配置完成后，就可以启动项目使用客户端去调用接口

```java
@SpringBootTest
class SdkTestApplicationTests {
    
    @Resource
    BingApiClient bingApiClient;

    @Test
    void test() {
        String url = "http://bing-api.cxbym.live/api/interface/test/get/{name}";
        String method = "GET";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name","张三");
        BingApiClient bingApiClient = new BingApiClient(accessKey, secretKey);
        ApiResult apiResult = bingApiClient.invoke(url, method, paramMap);
        System.out.println(apiResult.getCode(),apiResult.getData(),
                          apiResult.getMessage());   
    }
}
```

- 上述就是使用SDK调用接口的一个完整示例，在需要的地方直接引入客户端`BingApiClient`，调用invoke方法即可

- 方法中的三个参数请参照Bing API平台对应接口的说明文档

- 传入参数是`HashMap<String, Object>`结构，传入参数参照接口文档，返回结果是ApiResult包装类

返回参数的统一结构：

```json
{
  "code": "响应码",
  "data": "响应消息",
  "message": "响应结果"
}
```



## 项目展示

- 登录注册

  ![image-20230913113029594](/image/image-20230913113029594-1694576294566-4.png)


- 接口商城

![image-20230913113129399](/image/image-20230913113129399-1694576294566-5.png)

- 接口详情

  ![image-20230913113218043](/image/image-20230913113218043.png)

  

- 接口购买

![image-20230913113244790](/image/image-20230913113244790.png)

- 接口订单

  ![image-20230913113335897](/image/image-20230913113335897.png)

- 接口支付

![image-20230913113355214](/image/image-20230913113355214.png)

- 我的接口

  ![image-20230913113426142](/image/image-20230913113426142.png)

- SDK文档![image-20230913113508237](/image/image-20230913113508237.png)

- 接口管理

![image-20230913113525856](/image/image-20230913113525856.png)

- 接口分析

![image-20230913113557312](/image/image-20230913113557312.png)

- 用户管理

![image-20230913113544360](/image/image-20230913113544360.png)

- 个人中心

![image-20230913113612196](/image/image-20230913113612196.png)

