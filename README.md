# 基于Sa-Token实现单点登录实战



## 一、搭建统一认证中心Sa-Token-SSO-Server

### 1.引入依赖

```
 <properties>
        <java.version>1.8</java.version>
        <sa-token.version>1.35.0.RC</sa-token.version>
 </properties>
 
 
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Sa-Token 权限认证, 在线文档：https://sa-token.cc/ -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- Sa-Token 插件：整合SSO -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-sso</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- Sa-Token 插件：整合redis (使用jackson序列化方式) -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-redis-jackson</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>

        <!-- 视图引擎（在前后端不分离模式下提供视图支持） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Http请求工具（在模式三的单点注销功能下用到，如不需要可以注释掉） -->
        <dependency>
            <groupId>com.dtflys.forest</groupId>
            <artifactId>forest-spring-boot-starter</artifactId>
            <version>1.5.26</version>
        </dependency>

        <!-- 谷歌验证码 -->
        <dependency>
            <groupId>com.github.penggle</groupId>
            <artifactId>kaptcha</artifactId>
            <version>2.3.2</version>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-captcha</artifactId>
            <version>5.8.3</version>
        </dependency>

        <!-- lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.29</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.mybatis.spring.boot/mybatis-spring-boot-starter -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.2.2</version>
        </dependency>

        <!-- 数据连接池 druid-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.2.8</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.51</version>
        </dependency>
```

### 2.导入数据库

创建satoken_admin数据库，登录系统用户名和密码统一存放到该数据表下。

```
-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id，自增主键',
  `dept_id` int(11) NULL DEFAULT NULL COMMENT '部门ID',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户账号',
  `nickname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `pw` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '明文密码',
  `sex` int(255) NULL DEFAULT NULL COMMENT '性别',
  `telephone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电话',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注\r\n',
  `status` int(255) NULL DEFAULT NULL COMMENT '帐号状态（0正常 1停用）',
  `user_sort` int(10) NULL DEFAULT NULL COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'super-admin', '超级管理员', 'e10adc3949ba59abbe56e057f20f883e', '123456', 1, '15946823652', 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif', 0, NULL, NULL, NULL);
INSERT INTO `sys_user` VALUES (2, 2, 'admin', '管理员', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 0, '15946823652', 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif', 0, 0, '2022-12-08 10:52:00', NULL);
INSERT INTO `sys_user` VALUES (3, 2, 'huanyi', '桓一', 'e10adc3949ba59abbe56e057f20f883e', '123456', 0, '15948370464', NULL, 1, 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (5, 4, 'test', '测试', 'e10adc3949ba59abbe56e057f20f883e', '123456', 0, '15554896265', NULL, 0, 3, '2022-12-09 09:31:00', NULL);
INSERT INTO `sys_user` VALUES (6, 1, 'zero', '零', 'e10adc3949ba59abbe56e057f20f883e', '123456', 0, NULL, '2', 0, 5, '2022-12-29 03:43:00', NULL);
```

### 3.创建实体类

```
@Data
public class SysUser implements Serializable {
    /**
     * id，自增主键
     */
    private Integer userId;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 明文密码
     */
    private String pw;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 电话
     */
    private String telephone;

    /**
     * 备注

     */
    private String remark;

    /**
     * 帐号状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者
     */
    private String createBy;

    private static final long serialVersionUID = 1L;
}
```

### 4.创建Mapper

```
@Mapper
public interface SysUserMapper {
    @Select("select * from sys_user where username = #{username}")
    @Results(@Result(property = "userId", column = "user_id"))
    SysUser selectUser(@Param("username") String username);
}
```



### 5.开发认证接口

```
@RestController
public class SsoServerController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SysUserMapper sysUserMapper;

    /*
     * SSO-Server端：处理所有SSO相关请求 (下面的章节我们会详细列出开放的接口)
     */
    @RequestMapping("/sso/*")
    public Object ssoRequest() {
        StringBuffer requestURL = request.getRequestURL();
        if (requestURL.indexOf("doLogin") != -1) {
            String kaptcha_code = (String) request.getSession().getAttribute("kaptcha_code");
            String code = request.getParameter("code");
            //验证是否对，不管大小写
            if (StringUtils.isEmpty(code)) {
                return SaResult.error("验证码不能为空");
            } else if (!kaptcha_code.equalsIgnoreCase(code)) {
                return SaResult.error("验证码错误");
            }
        }
        return SaSsoProcessor.instance.serverDister();
    }

    /**
     * 配置SSO相关参数
     */
    @Autowired
    private void configSso(SaSsoConfig sso) {
        // 配置：未登录时返回的View
        sso.setNotLoginView(() -> {
            return new ModelAndView("sa-login.html");
        });

        // 配置：登录处理函数
        sso.setDoLoginHandle((name, pwd) -> {
            SysUser user = sysUserMapper.selectUser(name);
            if (ObjectUtils.isEmpty(user)) {
                return SaResult.error("用户不存在！");
            } else if (!pwd.equals(user.getPw())) {
                return SaResult.error("密码错误！");
            } else {
                StpUtil.login("userId-"+user.getUserId());
                SaSession session = StpUtil.getTokenSession();
                session.set("user", JSON.toJSON(user));
                return SaResult.ok("登录成功！").setData(StpUtil.getTokenValue());
            }
        });

        // 配置 Http 请求处理器 （在模式三的单点注销功能下用到，如不需要可以注释掉）
        sso.setSendHttp(url -> {
            try {
                // 发起 http 请求
                System.out.println("------ 发起请求：" + url);
                return Forest.get(url).executeAsString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

}
```

### 6.验证码接口

```
@Controller
public class CaptchaController {

    @RequestMapping(value = "/kaptcha", produces = "image/png")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(110, 40, 4, 10);
        OutputStream out = null;
        try {
            response.setHeader("Cache-Control", "no-store");
            response.setContentType("image/png");
            out = response.getOutputStream();
            String code = captcha.getCode();
            request.getSession().setAttribute("kaptcha_code", code);
            captcha.write(out);
            out.flush();
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } finally {
            if (out != null) {
                out.close();
            }
        }

        //验证图形验证码的有效性，返回boolean值
        captcha.verify("1234");
    }

    /**
     * 在SpringMvc中获取到Session
     *
     * @return
     */
    public void writeJSON(HttpServletResponse response, Object object) {
        try {
            //设定编码
            response.setCharacterEncoding("UTF-8");
            //表示是json类型的数据
            response.setContentType("application/json");
            //获取PrintWriter 往浏览器端写数据
            PrintWriter writer = response.getWriter();

            ObjectMapper mapper = new ObjectMapper(); //转换器
            //获取到转化后的JSON 数据
            String json = mapper.writeValueAsString(object);
            //写数据到浏览器
            writer.write(json);
            //刷新，表示全部写完，把缓存数据都刷出去
            writer.flush();

            //关闭writer
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### 7.全局异常处理

```
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 全局异常拦截
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }
}
```

### 8.application.yml配置

```
# 端口
server:
  port: 9000

# Sa-Token 配置
sa-token:
  # token名称 (同时也是cookie名称)
  token-name: satoken
  # token有效期，单位s 默认30天（2592000）, -1代表永不过期
  timeout: 1800
  # 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token)
  is-share: true
  # token 风格
  token-style: uuid
  # 是否输出操作日志
  is-log: false
  # ------- SSO-模式一相关配置
  # cookie:
  # 配置 Cookie 作用域
    # domain: stp.com

  # ------- SSO-模式二相关配置
  sso:
    # Ticket有效期 (单位: 秒)，默认五分钟
    ticket-timeout: 300
    # 所有允许的授权回调地址
    allow-url: "*"
    # 是否打开单点注销功能
    is-slo: true

    # ------- SSO-模式三相关配置 （下面的配置在SSO模式三并且 is-slo=true 时打开）
    # 是否打开模式三
    is-http: true
  sign:
    # 接口调用秘钥（用于SSO模式三的单点注销功能）
    secret-key: kQwIOrYvnXmSDkwEiFngrKidMcdrgKor
    # ---- ????????????? Sa-Token ??http??????????????

spring:
  # jdbc配置
  datasource:
    url: jdbc:mysql://localhost:3306/satoken_admin?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456
    type: com.alibaba.druid.pool.DruidDataSource
  # Redis配置 （SSO模式一和模式二使用Redis来同步会话）
  redis:
    # Redis数据库索引（默认为0）
    database: 1
    # Redis服务器地址
    host: 127.0.0.1
    # Redis服务器连接端口
    port: 6379
    # Redis服务器连接密码（默认为空）
    password:
    # 连接超时时间
    timeout: 10s
    lettuce:
      pool:
        # 连接池最大连接数
        max-active: 200
        # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: -1ms
        # 连接池中的最大空闲连接
        max-idle: 10
        # 连接池中的最小空闲连接
        min-idle: 0

forest:
  # 关闭 forest 请求日志打印
  log-enabled: false
```

### 9.创建启动类

```
@SpringBootApplication
public class SaTokenSsoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaTokenSsoServerApplication.class, args);
        System.out.println("\n------ Sa-Token-SSO 认证中心启动成功");
    }
}
```

## 二、搭建客户端项目Sa-Token-SSO-Client

### 1.引入依赖

```
<properties>
        <java.version>1.8</java.version>
        <sa-token.version>1.35.0.RC</sa-token.version>
</properties>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Sa-Token 权限认证, 在线文档：https://sa-token.cc/ -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- Sa-Token 插件：整合SSO -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-sso</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- Sa-Token 整合redis (使用jackson序列化方式) -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-redis-jackson</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- 提供Redis连接池 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>

        <!-- Http请求工具 -->
        <dependency>
        		<groupId>com.dtflys.forest</groupId>
            <artifactId>forest-spring-boot-starter</artifactId>
            <version>1.5.26</version>
        </dependency>
```

2.创建客户端认证接口

```
@RestController
public class SsoClientController {
    // SSO-Client端：首页
    @RequestMapping("/")
    public String index() {
        String str = "<h2>Sa-Token SSO-Client 应用端</h2>" +
                "<p>当前会话是否登录：" + StpUtil.isLogin() + "</p>" +
                "<p><a href=\"javascript:location.href='/sso/login?back=' + encodeURIComponent(location.href);\">登录</a>" +
                " <a href='/sso/logout?back=self'>注销</a></p>";
        return str;
    }
    /*
     * SSO-Client端：处理所有SSO相关请求
     * 		http://{host}:{port}/sso/login			-- Client端登录地址，接受参数：back=登录后的跳转地址
     * 		http://{host}:{port}/sso/logout			-- Client端单点注销地址（isSlo=true时打开），接受参数：back=注销后的跳转地址
     * 		http://{host}:{port}/sso/logoutCall		-- Client端单点注销回调地址（isSlo=true时打开），此接口为框架回调，开发者无需关心
     */
    @RequestMapping("/sso/*")
    public Object ssoRequest() {
        return SaSsoProcessor.instance.clientDister();
    }

    // 配置SSO相关参数
    @Autowired
    private void configSso(SaSsoConfig sso) {
        // 配置Http请求处理器
        sso.setSendHttp(url -> {
            System.out.println("------ 发起请求：" + url);
            return Forest.get(url).executeAsString();
        });
    }

    // 全局异常拦截
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }
}
```

### 2.创建客户端接口

```
@RestController
public class SsoClientController {
    // SSO-Client端：首页
    @RequestMapping("/")
    public String index() {
        String str = "<h2>Sa-Token SSO-Client 应用端</h2>" +
                "<p>当前会话是否登录：" + StpUtil.isLogin() + "</p>" +
                "<p><a href=\"javascript:location.href='/sso/login?back=' + encodeURIComponent(location.href);\">登录</a>" +
                " <a href='/sso/logout?back=self'>注销</a></p>";
        return str;
    }
    /*
     * SSO-Client端：处理所有SSO相关请求
     * 		http://{host}:{port}/sso/login			-- Client端登录地址，接受参数：back=登录后的跳转地址
     * 		http://{host}:{port}/sso/logout			-- Client端单点注销地址（isSlo=true时打开），接受参数：back=注销后的跳转地址
     * 		http://{host}:{port}/sso/logoutCall		-- Client端单点注销回调地址（isSlo=true时打开），此接口为框架回调，开发者无需关心
     */
    @RequestMapping("/sso/*")
    public Object ssoRequest() {
        return SaSsoProcessor.instance.clientDister();
    }

    // 配置SSO相关参数
    @Autowired
    private void configSso(SaSsoConfig sso) {
        // 配置Http请求处理器
        sso.setSendHttp(url -> {
            System.out.println("------ 发起请求：" + url);
            return Forest.get(url).executeAsString();
        });
    }

    // 全局异常拦截
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }
}
```

### 3.application.yml文件配置

```
# 端口
server:
  port: 9001

# sa-token配置
sa-token:
  # SSO-相关配置
  sso:
    # SSO-Server端 统一认证地址
    auth-url: http://127.0.0.1:9000/sso/auth
    # 使用 Http 请求校验ticket (模式三)
    is-http: true
    # SSO-Server端 ticket校验地址
    check-ticket-url: http://127.0.0.1:9000/sso/checkTicket
    # 单点注销地址
    slo-url: http://127.0.0.1:9000/sso/signout
    # 查询数据地址
    get-data-url: http://127.0.0.1:9000/sso/getData
  sign:
    # API 接口调用秘钥
    secret-key: kQwIOrYvnXmSDkwEiFngrKidMcdrgKor

spring:
  # 配置 Redis 连接 （此处与SSO-Server端连接不同的Redis
  redis:
    # Redis数据库索引
    database: 2
    # Redis服务器地址
    host: 127.0.0.1
    # Redis服务器连接端口
    port: 6379
    # Redis服务器连接密码（默认为空）
    password:
    # 连接超时时间
    timeout: 10s
    lettuce:
      pool:
        # 连接池最大连接数
        max-active: 200
        # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: -1ms
        # 连接池中的最大空闲连接
        max-idle: 10
        # 连接池中的最小空闲连接
        min-idle: 0

forest:
  # 关闭 forest 请求日志打印
  log-enabled: false
```

## 三、测试

依次启动认证中心和客户端。

打包Client，启动多个服务

```
java -jar sa-token-sso-clietn-0.0.1-SNAPSHOT.jar --server.port=9001
java -jar sa-token-sso-clietn-0.0.1-SNAPSHOT.jar --server.port=9002
java -jar sa-token-sso-clietn-0.0.1-SNAPSHOT.jar --server.port=9003
```

依次访问

[客户端1：http://localhost:9001](http://localhost:9001)

[客户端2：http://localhost:9002](http://localhost:9002)

[客户端3：http://localhost:9003](http://localhost:9003)



当系统没有登录则跳转到统一认证中心Sa-Token-SSO-Server进行登录，登录之后剩下的系统都自动登录。

![](/Users/qch/Desktop/Screen Shot 2023-07-12 at 14.43.45.png)

进入其他端口都不用再次登录。

## 四、参考文章

[Sa-Token官方文档](https://sa-token.cc/doc.html#/)

[https://github.com/gitjibl/sa-token-sso](https://github.com/gitjibl/sa-token-sso)

[Sa-Token实现单点登录](https://juejin.cn/spost/7254927062426402853)


## 五、源码

前端文件在源码里，教程里没有具体给出。

[GitHub源码](https://github.com/qinchanghe/sa-token-sso.git)



