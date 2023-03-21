### 业务开发-后台系统登陆功能
**修改静态资源映射**：

**web拦截器拦截未登录请求**：
```Java
@Component
// 设定拦截路径
@WebFilter(value = "loginFilter", urlPatterns = {"/"})
public class LoginFilter implements Filter {

    // 路径正则匹配器
    private PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(...) {
        // 1. 获取请求URI

        // 2. 设置未登录可以被访问的默认URL

        // 3. 使用路径匹配器判断当前路径是否可以直接访问

        // 4. 若当前路径无法直接被放行，则判断用户登陆状态

        // 5. 若用户未处于登陆状态，则返回错误码
    }
}

```


### 新增员工
**全局异常处理器创建**：

第一步：创建全局异常处理器
```Java
// 为controller类进行aop增强
@ControllerAdvice(annotations = {RestController.class})
// 将返回结果转换为json格式
@ResponseBody
@Slf4j
public class ControllerExceptionHandler {
    // 拦截某一特定异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<ExceptionDef> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.info(e.toString());
        if (e.getMessage().contains("Duplicate entry")) {
            return R.error(ExceptionDef.EMPLOYEE_CREATE_DUPLICATE_ERR.getErrMsg());
        }

        return R.error(ExceptionDef.UNKNOWN_ERR.getErrMsg());
    }
}

```

第二步：定义业务异常信息
```Java
public enum ExceptionDef {
    /***
     * EMPLOYEE相关异常，错误码范围：[100000~101000)
     */
    EMPLOYEE_CREATE_DUPLICATE_ERR(10_00_00, "员工用户名重复错误"),

    UNKNOWN_ERR(-1, "未知错误");

    ExceptionDef(Integer errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private Integer errCode;
    private String errMsg;

    public Integer getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
```

### 员工信息分页查询
**需求分析**：
+ 数据分页展示；
+ 页面可以跳转；
+ 可以设置一页数据数量；
+ 可以对数据进行搜索；

**Mybatis-Plus 插件拦截器**：
```Java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
```

**使用Page进行分页查询**：
```Java
    @GetMapping("/page")
    public R<Page<Employee>> pageQuery(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {

        log.info("员工分页查询，参数：page {}, pageSize {}, name {}", page, pageSize, name);

        // 1.始化页面信息
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        // 2.配置查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        // 3.mapper查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
```

### 禁用员工账号
**mvc 自定义消息转换器**：解决Java Long类型精度与JavaScript Long类型精度不匹配问题；

第一步：WebMvcConfigurer配置新的消息转换器
```Java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 1.创建http消息json转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 2.设置自定义json映射器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 3.将自定义消息转换器放置到converters链的第一个，会被优先使用
        converters.add(0, messageConverter);
    }
}
```

第二步：自定义ObjectMapper映射器 **（没学会）**
```Java
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```

### 公共字段属性填充
**问题分析**：类似`create_time` `update_time` `create_user` `update_user`这类字段为公共字段，需要在不同的业务代码进行维护，因此需要公共字段自动填充技术简化开发；

**快速入门**：

第一步：为实体类增添Mybatis-Plus字段填充约束
```Java
@Data
public class Employee implements Serializable {

    // 插入时进行字段填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 插入或更新时进行字段填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}

```

第二步：编写自定义属性填充处理器（`MetaObjectHandler`）
```Java
@Component
public class CommonSegFillingHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 怎么为不同的实体自动填充不同的字段？
        // 自动填充时间
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        // 自动填充操作用户
        Long operateUserId = BaseContext.getSessionUserId();
        metaObject.setValue("createUser", operateUserId);
        metaObject.setValue("updateUser", operateUserId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        Long operateUserId = BaseContext.getSessionUserId();
        metaObject.setValue("updateUser", operateUserId);
    }
}
```

第三步：编写上下文工具类存储线程上下文信息
```Java
/**
 * 工具类，使用ThreadLocal存储每一个线程独有的上下文信息
 */
public class BaseContext {
    private static ThreadLocal<Long> sessionUserId = new ThreadLocal<>();

    public static void setSessionUserId(Long id) {
        sessionUserId.set(id);
    }

    public static Long getSessionUserId() {
        return sessionUserId.get();
    }
}

```

### 分类相关
**需求说明**：
+ 分页查询分类：要按照sort进行升序排序
+ 新增分类；
+ 删除分类：需要检查相关联菜品与套餐，有的话则抛出异常；

### 菜品相关
**需求说明**：

**文件上传**：

第一步：浏览器遵循相关请求规范
+ 需要为Post请求；
+ 请求格式需要为MultipartFile；
+ 请求栏名称为"file"；

第二步：服务端编写相应请求，可使用web mvc提供的multipartFile参数，会自动注入；其他就注意一下细节（基本路径设置与创建、文件名的生成、文件的转储、io流复制与关闭）
```Java
@RestController
@RequestMapping("/common")
@ResponseBody
@Slf4j
public class CommonController {
    @Value("${images.basePath}")
    String basePath;

    /**
     * 接收上传文件并进行转储，返还文件相对路径
     * @param file
     * @return 存储文件相对路径
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // 1.为文件起自定义别名（要求全局唯一）
        String filename = generateNameForFile(file.getOriginalFilename());

        // 2.若基本路径文件夹不存在，则创建文件夹
        Path path = Paths.get(basePath);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error(e.getMessage());
                return R.error("创建文件夹失败");
            }
        }

        // 3.将基本路径与文件名结合，进行转储
        Path filepath = path.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filepath);
        } catch (IOException e) {
            log.error(e.getMessage());
            return R.error("转储文件失败");
        }

        // 4.关闭文件输入流
        return R.success(filename);
    }

    private String generateNameForFile(String filename) {
        // 1.取出文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));

        // 2.生成UUID
        String id = UUID.randomUUID().toString();

        // 3.结合UUID与文件后缀得到文件名
        String name = id + suffix;
        return name;
    }

}

```

**文件下载**：返回格式不再是json格式，而是使用字节流进行传输；
```Java
public void download(@RequestParam String name, ServletResponse response) {
    // 1.根据文件名获取文件绝对路径
    Path path = Paths.get(basePath).resolve(Paths.get(name));

    // 2.获取文件的输入流与response的输出流
    try (InputStream input = new FileInputStream(path.toString())) {
        // 3.不断拷贝文件字节流
        int len = 1024;
        byte[] bytes = new byte[len];
        while ((len = input.read(bytes)) > 0) {
            response.getOutputStream().write(bytes);
        }
    } catch (IOException e) {
        log.error("文件字节流拷贝失败");
    }
}
```

### 菜品相关
**新增菜品-多张表进行更新操作**：
+ 应当使用`@Transactional`注解保证多张表插入的事务特性；
+ 应当为数据传输新建一个实体类`Dto`；
+ 使用`bactchSave`进行批量插入操作；

**循环依赖问题**：`CategoryService` 依赖 `DishService` （删除分类时约束性检查）；同时`DishService` 依赖 `CategoryService` （查找菜品列表时显示分类），开启springboot依赖循环模式：
```yml
spring:
  main:
    allow-circular-references: true
```

**启用事务解决多张表的同时插入问题**：
```Java
    @Transactional
    public boolean saveWithFlavors(DishDto dishDto) {
        // 1.dish表中插入新菜品
        this.save(dishDto);

        // 2.口味表中插入菜品对应的口味
        // 2.1为口味实体添加dish外键信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor :flavors) {
            flavor.setDishId(dishDto.getId());
        }
        // 2.2批量插入口味表
        dishFlavorService.saveBatch(flavors);

        return true;
    }

```
**使用Dto对象封装业务层到表象层的数据传输**：
```Java
@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors;
    private String categoryName;
}
```

```Java
    @GetMapping("/page")
    public R<Page<DishDto>> page(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {

        // 1.构造分页信息
        Page<Dish> pageInfo = new Page(page, pageSize);

        // 2.构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = (new LambdaQueryWrapper<Dish>())
                .like(StringUtils.isNotEmpty(name), Dish::getName, name)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);

        // 3.获取dish表分页查询结果
        dishService.page(pageInfo, queryWrapper);

        // 4.将实体类与Dto类进行转换
        Page<DishDto> dtoPage = new Page<>();
        // 4.1将pageInfo的信息拷贝到dtoPage中，不过应当排除records项
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        // 4.2获取DishDto列表的拷贝
        List<DishDto> dishDtoList = pageInfo.getRecords().stream().map((dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);

            // 根据categoryId查询categoryName
            Category category = categoryService.getById(dishDto.getCategoryId());
            dishDto.setCategoryName(category.getName());

            return dishDto;
        })).collect(Collectors.toList());
        // 4.3设置pageDto的records项
        dtoPage.setRecords(dishDtoList);

        // 5.返还查询结果
        return R.success(dtoPage);
    }

```

### 套餐相关

### 短信发送与验证码登陆
**使用spiringboot整合JavaMail**：

第一步：引入javamail依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

第二步：设置mail默认属性
```yml
spring:
  mail:
    host: "smtp.qq.com"
    username: "1776331069@qq.com"
    password: "iiejhlvzzkweehie"
```

第三步：创建默认工具MailSender
```Java
@Configuration
public class EmailConfig {
    @Bean
    public JavaMailSender javaMailSender(EmailProperties properties){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(properties.getHost());
        javaMailSender.setUsername(properties.getUsername());
        javaMailSender.setPassword(properties.getPassword());

        return javaMailSender;
    }
}
```

第四步：使用MailSender
```Java
    public boolean sendMail(String to, String head, String text) {
        // 1.构造消息内容
        SimpleMailMessage message = new SimpleMailMessage();
        // 1.1设置发送方，需要与配置中的用户名相同
        message.setFrom(emailProperties.getUsername());
        // 1.2设置接收方
        message.setTo(to);
        // 1.3设置标题与正文
        message.setSubject(head);
        message.setText(text);

        // 2.发送简单短信
        mailSender.send(message);
        return true;
    }
```

**用户验证码获取**：
```Java
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody Map<String, String> map, HttpSession session) {
        String email = map.get("email");
        // 1.随机生成四位数验证码
        Integer code = ValidateCodeUtils.generateValidateCode(validateCodeLen);

        // 2.将验证码保存到session域，供登陆时验证
        session.setAttribute(VALIDATE_CODE, code.toString());

        // 3.设置验证码过期时间到session域
        session.setAttribute(EXPIRE_TIME, DateUtils.addMinutes(new Date(), validate_code_expire_minutes));

        // 4.拼凑邮件正文
        String text = String.format(emailTemplate, code.toString());

        // 5.发送邮件
        mailService.sendMail(email, emailSubject, text);
        return R.success("发送验证码成功");
    }
```

**用户登陆-对验证码进行校验-创建新用户**：
```Java
@PostMapping("/login")
    public R<String> login(HttpSession session, @RequestBody Map map) {
        // 1.获取用户验证码，与session进行比对
        // 1.1如果验证码不一致，抛出异常
        String code = (String) map.get("code");
        String tCode = (String) session.getAttribute(VALIDATE_CODE);
        if (!code.equals(tCode) ) {
            throw new UserException(ExceptionDef.VALIDATE_CODE_DIFFER_ERR);
        }
        // 1.2如果验证码已经过期，抛出异常
        Date expireTime = (Date) session.getAttribute(EXPIRE_TIME);
        if (expireTime.compareTo(new Date()) < 0) {
            throw new UserException(ExceptionDef.VALIDATE_CODE_EXPIRE_ERR);
        }

        // 2.判断当前用户是否在数据库已存在，不存在则创建用户
        // 2.1构造查询条件，根据邮件查询
        String email = (String) map.get("email");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        // 2.2查询用户是否存在
        User user = userService.getOne(queryWrapper);
        // 2.3用户不存在，则根据邮箱创建新用户
        if (user == null) {
            User newUser = new User();
            newUser.setEmail(email);
            userService.save(newUser);
        }

        // 3.将当前用户邮箱信息存储到session域
        session.setAttribute("user_email", email);

        // 4.返还用户邮箱信息
        return R.success(email);
    }
```

### 菜品展示与购物车

### 订单


**问题一览**：
1. 前端多次重复的请求调用都会在后端产生一条数据（幂等性如何保证？）

### 缓存优化

**使用缓存优化查询结果**：

第一步：导入`spring-cache`和`starter-data-redis`依赖
```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

第二步：配置redis服务信息

第三步：启用全局`@EnableCaching`注解

第四步：使用`@Cacheable`缓存用户端请求列表数据
```Java
    @GetMapping("/list")
    @Cacheable(value = "dishCache", key = "#categoryId")
    public R<List<DishDto>> list(@RequestParam(required = false) Long categoryId) {
        log.info("查询菜品列表：categoryId {}", categoryId);
        // ...
    }
```

**使用`@CacheEvict`删除无效缓存**：
```Java
    @PostMapping
    @CacheEvict(value = "dishCache", key = "#dishDto.categoryId")
    public R<String> create(@RequestBody DishDto dishDto) {
        //...
    }

    @PutMapping
    @CacheEvict(value = "dishCache", key = "#dishDto.categoryId")
    public R<String> update(@RequestBody DishDto dishDto) {
        //...
    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true) {
        //...
    }
```

### Mysql优化
**MySQL主从复制介绍**：
+ master将改动记录到二进制日志（binlog）；
+ slave使用I/O thread将master的binlog拷贝到自己的中继日志（relay log）；
+ slave使用sql thread重做relay log中的事件，将改变应用到自己数据库中；

**读写分离介绍**：当系统同一时刻具有大量并发读操作和较少并发写操作的时候，由于写操作会给数据上行锁，导致读操作不能进行，从而导致系统查询性能大大降低，采用读写分离方案可以极大提升数据库的查询性能；

**Sharding-JDBC**：可以理解为升级版的JDBC驱动，可以在程序中轻松的完成数据库的读写分离；

**mysql主从复制快速入门**：[debug文章参考](https://blog.csdn.net/hexf9632/article/details/103890046)

第一步：修改主库配置文件（/etc/.../my.cnf），并重启mysql服务，并创建新用户赋予复制权限
```bash
log-bin=mysql-bin #启用二进制binlog文件
server-id=100 #设置服务id
```

```bash
systemctl restart mysqld #重启mysql服务
```

```sql
grant replication slave on "." to 'xiaoming'@'%' identified by 'Root@hatsune39'
```

第二步：登陆主库，使用`show master status`获取主库bin文件信息

第三步：配置从库，并进行重启，并配置主库（指定host、用户名、密码、binlog名称、位置）
```sql
-- 配置主库
change master to master_host='192.168.213.128',master_user='xiaowang',master_password='123456',master_log_file='mysql-bin.000003',master_log_pos=154;
```

```sql
-- 开启主从复制
start slave;
```

第四步：使用`show slave status`查看从数据库状态

**SpringBoot整合Sharding-JDBC**：

第一步：引入sharding-jdbc依赖
```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>4.1.1</version>
</dependency>
```

第二步：配置数据源
```yml
  shardingsphere:
    datasource:
      names: master,slave

      master:
       type: com.alibaba.druid.pool.DruidDataSource
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://192.168.213.128:3306/reggie
       username: springboot
       password: 123456

      slave:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.213.129:3306/reggie
        username: springboot
        password: 123456

    masterslave:
      load-balance-algorithm-type: round_robin
      name: ms
      master-data-source-name: master
      slave-data-source-names: slave
    props:
      sql:
        show: true
```