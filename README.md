# SpringBoot 多数据源切换（二）（依旧超级简单）

### 背景：主从架构下，数据库的读写分离

##### 1. 依赖

```java
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.3</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.2.16</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.29</version>
        </dependency>
    </dependencies>
```

##### 2.配置数据源

```java
spring:
  datasource:
    druid:
      type: com.alibaba.druid.pool.DruidDataSource
      master:
        url: jdbc:mysql://127.0.0.1:3307/user1?useUnicode=true&characterEncoding=utf8&useSSL=false
        username: root
        password: root
        driver-class-name: com.mysql.cj.jdbc.Driver
      slave1:
        enabled: true
        url: jdbc:mysql://127.0.0.1:3308/user1?useUnicode=true&characterEncoding=utf8&useSSL=false
        username: root
        password: root
        driver-class-name: com.mysql.cj.jdbc.Driver
      otmstariff:
        enabled: false
        url: jdbc:mysql://127.0.0.1:3306/user1?useUnicode=true&characterEncoding=utf8&useSSL=false
        username: root
        password: root
        driver-class-name: com.mysql.cj.jdbc.Driver
```

##### 3. 注册数据源
> 1）创建一个数据源枚举类
```java
public enum DataSourceType {
    /**
     * 主库
     */
    MASTER,

    /**
     * 从库
     */
    SLAVE1,

    SLAVE2
}
```

>2）我们切换数据库所需要的bean全部交给spring容器中
```java
@Configuration
public class DynamicDataSourceConfig {

    @Bean
    @Qualifier("masterDataSource")
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Qualifier("slave1DataSource")
    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave1")
    // 根据配置文件enabled属性，判断该配置是否生效
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave1", name = "enabled", havingValue = "true")
    public DataSource slave1DataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Qualifier("slave2DataSource")
    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave2")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave2", name = "enabled", havingValue = "true")
    public DataSource slave2DataSource() {
        return DruidDataSourceBuilder.create().build();
    }



    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(DataSource masterDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);
        setDataSource(targetDataSources, DataSourceType.SLAVE1.name(), "slave1DataSource");
        setDataSource(targetDataSources, DataSourceType.SLAVE2.name(), "slave2DataSource");
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        return dynamicDataSource;
    }

    /**
     * 设置数据源
     *
     * @param targetDataSources 备选数据源集合
     * @param sourceName 数据源名称
     * @param beanName bean名称
     */
    public void setDataSource(Map<Object, Object> targetDataSources, String sourceName, String beanName)
    {
        try
        {
            DataSource dataSource = SpringUtils.getBean(beanName);
            targetDataSources.put(sourceName, dataSource);
        }
        catch (Exception e)
        {
        }
    }


}

```

##### 4.切换数据源
```java
public class DynamicDataSourceContextHolder {
    // 线程安全
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
	/**
     * 设置数据源变量
     * @param dataSourceEnum 数据源变量
     */
    public static void setDataSourceType(String type) {
        CONTEXT_HOLDER.set(type);
    }
	/**
     * 获取数据源变量
     * @return 数据源变量
     */
    public static String getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }
	/**
     * 清理数据源
     * @return 数据源变量
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}

```

##### 5.设置数据源
>新建DynamicDataSource类继承AbstractRoutingDataSource类，并实现determineCurrentLookupKey方法，该方法是指定当前默认数据源的方法，该类是实现动态切换数据源的关键

```
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }
}
```

##### 6. 自定义多数据源切换注解
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    DataSourceType value() default DataSourceType.MASTER;
}

```
##### 7. AOP拦截器的实现
```java
@Slf4j
@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(com.example.datasourceprimordialdemo2.datasource.annotation.DataSource)")
    public void doPointCut() {
    }

    @Around("doPointCut()")
    public Object around(ProceedingJoinPoint pointcut) throws Throwable {
        MethodSignature signature = (MethodSignature) pointcut.getSignature();
        Method method = signature.getMethod();
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (Objects.nonNull(dataSource)) {
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        }
        try {
            return pointcut.proceed();
        } finally {
            // 销毁数据源 在执行方法之后
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }
}
```
##### 8. 启动类修改
```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
```
### 9. 使用
>此处为了测试，直接放在controller使用
```java
    @GetMapping("/0")
    @DataSource(DataSourceType.MASTER)
    public ResponseEntity<List<User>> query() {
        return ResponseEntity.ok(this.userService.query());
    }

    @GetMapping("/1")
    @DataSource(DataSourceType.SLAVE1)
    public ResponseEntity<List<User>> query2() {
        return ResponseEntity.ok(this.userService.query());
    }

    @GetMapping("/2")
    @DataSource(DataSourceType.SLAVE2)
    public ResponseEntity<List<User>> query3() {
        return ResponseEntity.ok(this.userService.query());
    }
```

##### 为了区分数据不一样，数据库未做主从同步

> master的数据
>
![在这里插入图片描述](https://img-blog.csdnimg.cn/9d5f398e9fb84c2397b0194e4e16e992.png)


> slave1的数据
>
![在这里插入图片描述](https://img-blog.csdnimg.cn/a915c659fcce421f9f532cf8e75d8b0e.png)
> slave2的数据
>
![在这里插入图片描述](https://img-blog.csdnimg.cn/05c4c0dba92e414aa2734ec1017c6497.png)


##### 5.用postman进行测试

> 获取master的数据
>
![\[外链图片转存失败,源站可能有防盗链机制,建议将图片保存下来直接上传(img-N4UsMMk8-1677832353467)(img/img_3.png)\]](https://img-blog.csdnimg.cn/b6b340325f8e4cb28c3d7f3b8786a6eb.png)
> 获取slave1的数据
>
![在这里插入图片描述](https://img-blog.csdnimg.cn/473c769cb16a4687978a98d7490d0bde.png)

> 获取slave2的数据
>
![在这里插入图片描述](https://img-blog.csdnimg.cn/57be6c9b29a5447c9a96bfeea617920a.png)

### 完成～～～

# 为什么配置了三个数据源


> 是Tz ，想把我遇到的问题都分享给你，想看更多精彩内容，请关注我的wx公众号zhuangtian
