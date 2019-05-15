## Spring Cloud

* [Spring Cloud Samples - 官方](https://github.com/spring-cloud-samples)

### Eureka - 注册中心

* 显示已注册的服务：`http://localhost:7000/`
* 监控信息：`http://localhost:7000/actuator`
* [Eureka REST operations - 官网](https://github.com/Netflix/eureka/wiki/Eureka-REST-operations) - 
请求`Eureka Server` URL地址时需去掉`/v2/`
  * 返回所有Apps的信息 - `http://localhost:7000/eureka/apps`
  * 返回指定应用的所有实例信息 - `http://localhost:7000/eureka/apps/{svcId}`

#### [Standalone Mode](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#spring-cloud-eureka-server-standalone-mode)

```yaml
spring:
  application:
    name: eureka-server
server:
  port: 7000
eureka:
  client:
    fetch-registry: false
    register-with-eureka: false
```
	
#### [Peer Awareness](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#spring-cloud-eureka-server-peer-awareness)

* 例1
  ```yaml
  spring:
    application:
      name: eureka-server
  management:
    endpoints:
      web:
        exposure:
          # 测试使用 - 开放所有端口
          include: "*"
  eureka:
    client:
      serviceUrl:
        defaultZone: http://peer1:7000/eureka/,http://peer2:7001/eureka/,http://peer3:7002/eureka/
  
  ---
  spring:
    profiles: peer1
  server:
    port: 7000
  eureka:
    instance:
      hostname: peer1
  
  ---
  spring:
    profiles: peer2
  server:
    port: 7001
  eureka:
    instance:
      hostname: peer2
  
  ---
  spring:
    profiles: peer3
  server:
    port: 7002
  eureka:
    instance:
      hostname: peer3
  
  ```
  
  ```
  127.0.0.1 peer1
  127.0.0.1 peer2
  127.0.0.1 peer3
  ```

* 例2

  ```yaml
  spring:
    application:
      name: eureka-server
  server:
    port: 7000
  management:
    endpoints:
      web:
        exposure:
          # 测试使用 - 开放所有端口
          include: "*"
  eureka:
    client:
      serviceUrl:
        defaultZone: http://192.168.100.101:7000/eureka/,http://192.168.100.102:7000/eureka/,http://192.168.100.103:7000/eureka/
    instance:
      # 使用IP
      prefer-ip-address: true
  ```

> 注：`Peer Awareness`模式不一定非得3个实例，2个或大于3个都可以。


### Hystrix - 熔断机制

* [hystrix-javanica -【java注解风格 - 重要】](https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-javanica)
* [How it Works](https://github.com/Netflix/Hystrix/wiki/How-it-Works)
* [How To Use](https://github.com/Netflix/Hystrix/wiki/How-To-Use)
* [监控详解及配置调优(timeouts/threads/semaphores)](https://github.com/Netflix/Hystrix/wiki/Operations)
* [Configuration](https://github.com/Netflix/Hystrix/wiki/Configuration)
* [Metrics and Monitoring](https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring)
* [Plugins](https://github.com/Netflix/Hystrix/wiki/Plugins)
* [Propagating the Security Context or Using Spring Scopes](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#netflix-hystrix-starter)
* `SEMAPHORE`和`THREAD`区别
  * `THREAD`超时后可立即放弃线程执行，`SEMAPHORE`超时后也需要等待Command执行完毕后才能执行后续逻辑（即使配了fallback），
  因为`SEMAPHORE`使用caller线程执行的，并没有另起线程。所以`SEMAPHORE`通常使用于执行时间不长的场景中。
  if a dependency is isolated with a semaphore and then becomes latent, the parent threads will remain blocked 
  until the underlying network calls timeout.

### Hystrix Dashboard

访问地址：`http://{localhost}:{port}/hystrix`

### Turbine

Turbine is a tool for aggregating streams of Server-Sent Event (SSE) JSON data into a single stream. The targeted use case is metrics streams from instances in an SOA being aggregated for dashboards.

* [spring-cloud-netflix-hystrix - 官网](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_turbine)
  * 显示当前可用的clusters - `http://localhost:7300/clusters`
* [Getting Started (1.x)](https://github.com/Netflix/Turbine/wiki/Getting-Started-(1.x))
* [Configuration (1.x)](https://github.com/Netflix/Turbine/wiki/Configuration-(1.x))

* #### 配置1 - 手动配置cluster-config、app-config（一对一）

  ```yaml
  turbine:
    aggregator:
      cluster-config: EASYCODE-DEMO-ORG
    app-config: easycode-demo-org
  ```

  `TurbineClustersProvider`默认使用`ConfigurationBasedTurbineClustersProvider`，此类只会去获取`cluster-config`
  配置项。如果你没有配置`cluster-config`配置项，则获取不到cluster，也就没有监控的应用可显示。
  
  `InstanceDiscovery`默认使用`EurekaInstanceDiscovery`，此类会根据`app-config`配置项，去eureka注册中心获取应用对应的所有实例。
  实例取出来后，默认使用`com.netflix.appinfo.InstanceInfo`的`appName`属性作为`cluster`值。当监控页面需要查看特定`cluster`
  信息时，首先会去`TurbineClustersProvider`查看有没有此值，有则再过滤出`InstanceDiscovery`实例中cluster值相等的应用实例。
  
* #### 配置2 - 自动配置

  ```java
  /**
   * 通过{@link EurekaClient}自动获取当前所有的应用，并把应用名（大写）作为Cluster值
   *
   * @param eurekaClient EurekaClient
   * @return {@link TurbineClustersProvider}
   */
  @Bean
  public TurbineClustersProvider clustersProvider(EurekaClient eurekaClient) {
      return new EurekaBasedTurbineClustersProvider(eurekaClient);
  }
  
  @Bean
  public InstanceDiscovery instanceDiscovery(TurbineProperties turbineProperties, EurekaClient eurekaClient,
      TurbineClustersProvider clustersProvider) {
      return new EurekaInstanceDiscovery(turbineProperties, eurekaClient) {
          /**
           * 重写获取应用名的来源，例如从Eureka获取
           */
          @Override
          protected List<String> getApplications() {
              return clustersProvider.getClusterNames();
          }
      };
  }
  ```
  
  > 注：上述只是例子，需要自行调整（例：通过Eureka返回的数据信息，来判断此应用能否适用于监控）。
  通过上述的例子，就不需要手动配置`cluster-config`/`app-config`。

* #### 配置3 - 混合配置cluster-config、app-config

  ```yaml
  turbine:
    aggregator:
      cluster-config: SYSTEM,USER
    app-config: easycode-demo-org,easycode-demo-shop,easycode-demo-user
    clusterNameExpression: metadata['cluster']
  ```
  
  即从`Eureka`服务中或取`easycode-demo-org`/`easycode-demo-shop`/`easycode-demo-user`三个应用的信息，获取
  `metadata.cluster`(配置于eureka.instance.metadata-map)属性值，与`cluster-config`配置中的值进行匹配，匹配成功则表明该应用属于对应的cluster。
  
  > 注：参考[官方文档](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_turbine)

### Archaius - 动态更新配置

* [Overview](https://github.com/Netflix/archaius/wiki/Overview)
* [Features](https://github.com/Netflix/archaius/wiki/Features)
* [Getting Started](https://github.com/Netflix/archaius/wiki/Getting-Started)
* [Users Guide](https://github.com/Netflix/archaius/wiki/Users-Guide)
  * [Deployment context](https://github.com/Netflix/archaius/wiki/Deployment-context)

### Ribbon - 客户端负载均衡，适用于HTTP、TCP

* Native
  * [官网](https://github.com/Netflix/ribbon/wiki)
  * [Features](https://github.com/Netflix/ribbon/wiki/Features)
  * [Getting Started](https://github.com/Netflix/ribbon/wiki/Getting-Started)
  * [Programmers Guide - 重要](https://github.com/Netflix/ribbon/wiki/Programmers-Guide)
    * client配置
    
      `<clientName>.<nameSpace>.<propertyName>=<value>`
      
      clientName：客户端名。如果集成Eureka，则表示注册至Eureka的服务名。
      
      nameSpace：默认值`ribbon`，可配置（详情参考官方文档）。
      
      propertyName：配置项。默认使用DefaultClientConfigImpl（提供了默认值），可配参数参考CommonClientConfigKey。
      
      > 注：缺少clientName前缀的配置表明是全局配置，适用于所有client。如`ribbon.ReadTimeout=1000`。
      配置格式同样适用于SpringCloud，因为Spring对配置做了桥接。
      
    * 配置namespace
    * 自定义load balancer
    * 集成Eureka
  * [Working with load balancers - 负载均衡相关的类及配置](https://github.com/Netflix/ribbon/wiki/Working-with-load-balancers)
  * [ResourceTemplate related APIs in ribbon module](https://github.com/Netflix/ribbon/wiki/ResourceTemplate-related-APIs-in-ribbon-module)
  * [Ribbon Annotations - 注解定义HTTP请求](https://github.com/Netflix/ribbon/wiki/Ribbon-Annotations)
  * Ribbon的负载均衡熔断算法【重要】请参考：`com.netflix.loadbalancer.ServerStats#isCircuitBreakerTripped()`方法
* Spring Cloud
  * [官网 - 【重要】](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#spring-cloud-ribbon)
  * `IClientConfig`默认实现为`DefaultClientConfigImpl`（参考`RibbonClientConfiguration`类），此类里面提供了`Ribbon Client`的**默认配置**
  * [Using the Ribbon API Directly](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_using_the_ribbon_api_directly)
    ```java
    public class MyClass {
        @Autowired
        private LoadBalancerClient loadBalancer;
    
        public void doStuff() {
            ServiceInstance instance = loadBalancer.choose("stores");
            URI storesUri = URI.create(String.format("http://%s:%s", instance.getHost(), instance.getPort()));
            // ... do something with the URI
        }
    }
    ```
    
    ```java
    @Configuration
    public class MyConfiguration {
    
        @Bean
        @LoadBalanced
        RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
    
    public class MyClass {
        @Autowired
        private RestTemplate restTemplate;
    
        public String doOtherStuff() {
            return restTemplate.getForObject("http://stores/stores", String.class);
        }
    }
    ```
  * Ribbon是否会重试依赖于`AbstractLoadBalancerAwareClient.getRequestSpecificRetryHandler()`实现及是否使用了Spring-retry。
  OpenFeign配置的`Retryer`（`FeignClientsConfiguration`）和Ribbon的重试没有任何关联，两个是完全独立的。
  * [Retrying Failed Requests - 【重要】](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#retrying-failed-requests)
  
> 注：当获取到的服务使用了域名时，当http连接超时设置为1s时，不是指整个操作连接超时为1s，而是域名解析出的所有IP
连接超时都是1s。所以会出现一种现象：当一个域名解析到4个IP时，且请求的connectTimeout、readTimeout都配置为1s，
当这4个IP都不通时，整个请求耗时会是4s。因为尝试了4个IP的连接，且4个IP都连接超时。

### OpenFeign - 简化java http client编程

* [feign官网](https://github.com/OpenFeign/feign)
* [Spring Cloud OpenFeign官网 - 【重要】](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_spring_cloud_openfeign)

> 注：非常重要的组件，此组件的关注程度高于Ribbon和Hystrix。

### Zuul - Router and Filter

Zuul是由Netflix提供的基于JVM路由及服务端负载均衡的组件。Zuul是一个网关服务，提供了dynamic routing、monitoring、
resiliency、security等功能。

* [Zuul官网](https://github.com/Netflix/zuul/wiki)
  * [How It Works](https://github.com/Netflix/zuul/wiki/How-it-Works)
* [Spring Cloud Zuul - 【重要】](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_router_and_filter_zuul)
  * **Spring Cloud Zuul计算HystrixTimeout规则【重要】** - 参考org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommand#getHystrixTimeout()方法
  * `Spring Boot Actuator`启用下面两个endpoints
    * Routes Endpoint（显示所有routes） - `GET /routes`，想显示详细信息则使用`/routes?format=details`。
    `POST /routes`会强制刷新routes。
    * Filters Endpoint（显示所有filters） - `GET /filters`
  * `/actuator/metrics`会统计出路由失败的信息
  * 使用`Zuul RequestContext`在Filters间共享数据，`FilterConstants`包含了filter需要的key
  * [Uploading Files through Zuul](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_uploading_files_through_zuul)
  * [Disable Zuul Filters](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_disable_zuul_filters)
  * [Providing Hystrix Fallbacks For Routes](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#hystrix-fallbacks-for-routes)
  * [Zuul Timeouts](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_zuul_timeouts)
  * [Rewriting the Location header](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#zuul-redirect-location-rewrite)
  * [How Zuul Errors Work](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_how_zuul_errors_work)
  * [Strangulation Patterns and Local Forwards](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#_strangulation_patterns_and_local_forwards)

#### 总结
  
* 当`Spring Cloud Zuul`启用从注册中心获取服务时，获取到的服务在创建`ZuulRoute`对象时，不会设置`sensitiveHeaders`属性，
此时`PreDecorationFilter`会获取默认的`zuul.sensitive-headers`（默认值为：`"Cookie", "Set-Cookie", "Authorization"`）。
**所以此时如果你集成了`Spring OAuth2`，并不会把`OAuth2 Access Token`传递给下游服务，因为`Authorization`被忽略了。**

  解决方案如下，去除Authorization配置项：    
  ```yaml
  zuul:
    # 默认值为："Cookie", "Set-Cookie", "Authorization"
    sensitive-headers: "Cookie, Set-Cookie"
  proxy:
    auth:
      routes:
        server1: oauth2
        server2: passthru
        server3: none
  ```
  
  > 注：此时可以通过`proxy.auth.routes`来配置具体哪个服务需要使用`Authorization` header，哪些直接剔除掉。
  
  * **oauth2** - authorization header值重置为当前OAuth2 Token，传递给下游服务
  * **passthru** - 当前请求有authorization header则传递给下游服务，没有也不会去创建
  * **none** - 忽略authorization header，不会传递给下游服务

* 自定义特定服务的 route sensitiveHeaders，优先级高于sensitiveHeaders配置

  ```yaml
  zuul:
    routes:
      users:
        path: /myusers/**
        # 全局sensitiveHeaders配置项无效
        sensitiveHeaders: Cookie,Set-Cookie,Authorization
        url: https://downstream
  ```
  ```yaml
  zuul:
    routes:
      users:
        path: /myusers/**
        # 全局sensitiveHeaders配置项无效
        sensitiveHeaders:
        url: https://downstream
  ```
  
* `ignore-security-headers: true` - 忽略SpringSecurity安全相关的header，去除downstream response中SpringSecurity安全相关的header。
但你可能会发现浏览器中response还有SpringSecurity安全相关的header，这是由于网关服务自己SpringSecurity后面添加的。


### Spring Cloud Config

* 获取配置的请求格式：
  * `/{application}/{profile}[/{label}]`
  * `/{application}-{profile}.yml`
  * `/{label}/{application}-{profile}.yml`
  * `/{application}-{profile}.properties`
  * `/{label}/{application}-{profile}.properties`
  
  > 注：`application`值为`spring.config.name`（默认值：`application`，也可通过`spring.application.name`来修改）。
  `profile`为激活的`profile`（可以传多个值，以`,`分隔，多个值时最后一个优先级高：`spring.profiles.active`）。
  `label`为可选的git label（可为逗号分隔的多值，默认值：`master`）。
  
  > `spring.application.name`不能以`application-`开始。

  >注：[Locating Remote Configuration Resources - client定位远程配置文件【重要】](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_locating_remote_configuration_resources)

* 通过`/env` endpoint获取环境变量

  ```
  $ curl localhost:8080/env
  {
    "profiles":[],
    "configService:https://github.com/spring-cloud-samples/config-repo/bar.properties":{"foo":"bar"},
    "servletContextInitParams":{},
    "systemProperties":{...},
    ...
  }
  ```
  `configService`属性的PropertySource默认拥有最高优先级。

* 参考文档
  * [Environment Repository - 配置文件加载规则](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_environment_repository)
  * [Git Backend - 支持Git的规则【重要】](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_git_backend)
  * [Sharing Configuration With All Applications - 所有应用共享的配置文件](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_sharing_configuration_with_all_applications)
  * [OAuth 2.0](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_oauth_2_0)
  * [Health Indicator](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_health_indicator_2)
  * [Security](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_security)
  * [Encryption and Decryption - 【重要】](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_encryption_and_decryption_2)
  * [Serving Encrypted Properties - 解密工作由ConfigServer切换至ConfigClient](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_serving_encrypted_properties)
  * [Serving Alternative Formats - 返回的环境变量由JSON格式切换至YML、Properties](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_serving_alternative_formats)
  * [Serving Plain Text - 返回指定的配置文件](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_serving_plain_text)
  
### Spring Cloud Gateway

* [Retrieving route filters](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_retrieving_route_filters)
  * `GET /actuator/gateway/globalfilters` - 返回全局filter
  * `GET /actuator/gateway/routefilters` - 返回GatewayFilter factories
  * `POST /actuator/gateway/refresh` - To clear the routes cache
  * `GET /actuator/gateway/routes` - 返回定义的GatewayFilter
  * `GET /actuator/gateway/routes/{id}` - 返回指定route的详细信息
  * `POST /gateway/routes/{id_route_to_create}` - 创建route，请求体为`GET /actuator/gateway/routes/{id}`JSON格式
  * `DELETE /gateway/routes/{id_route_to_delete}` - 删除route

* 输出Reactor Netty Access Logs - 【重要】

  开启`Reactor Netty Access Logs`的配置项：`-Dreactor.netty.http.server.accessLogEnabled=true`，必须使用`Java System Property`
  而不是`Spring Boot property`。开启后可以把日志分离出单个日志文件（可选），例：

  ```xml
  <appender name="accessLog" class="ch.qos.logback.core.FileAppender">
      <file>access_log.log</file>
      <encoder>
          <pattern>%msg%n</pattern>
      </encoder>
  </appender>
  <appender name="async" class="ch.qos.logback.classic.AsyncAppender">
      <appender-ref ref="accessLog" />
  </appender>
  
  <logger name="reactor.netty.http.server.AccessLog" level="INFO" additivity="false">
      <appender-ref ref="async"/>
  </logger>
  ```


* 参考文档
  * [术语](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#_glossary)
  * [How It Works](https://cloud.spring.io/spring-cloud-static/Greenwich.RELEASE/single/spring-cloud.html#gateway-how-it-works)
  
> 注：当前`Spring Cloud Gateway`版本（`Greenwich.RELEASE`）不支持Ribbon的LoadBalancerRules、retriesNextServer、
隔离无效node等功能，只用到了Ribbon的choose server功能，如果想使用上述功能请切换至`Spring Cloud Zuul`


