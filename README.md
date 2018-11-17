# `消息总线`

企业服务总线通常在企业消息系统上提供一个抽象层，使得集成架构师能够不用编码而是利用消息的价值完成集成工作。通俗一点来讲就是企业服务总线是架构在消息中间件之上的另外一个抽象层，使得我们可以不用关心消息相关的处理就可以完成业务逻辑的处理。

Spring Cloud Bus是在Stream基础之上再次进行抽象封装,使得我们可以在不用理解消息发送、监听等概念的基础上使用消息来完成业务逻辑的处理。

那么Spring Cloud Bus是如何为我们实现的呢？一句话概括就是事件机制。

### `Spring的事件机制`

在Spring框架中有一个事件机制，该机制是一个观察者模式的实现。

当我们在应用中引入事件机制时需要借助Spring中以下接口或抽象类：

* ApplicationEventPublisher: 这是一个接口，用来发布一个事件;
* ApplicationEvent: 这是一个抽象类，用来定义一个事件;
* ApplicationListener<E extends ApplicationEvent>: 这是一个接口，实现事件的监听。

其中Spring应用的上下文ApplicationContext默认是实现了ApplicationEventPublisher接口，因此在发布事件时我们可以直接使用ApplicationContext.publishEvent()方法来发送。

一个典型的Spring事件发送与监听代码如下。

```
public class UserEvent extends ApplicationEvent {

    private static Logger logger = LoggerFactory.getLogger(UserEvent.class);

    private String action;
    private User user;

    public UserEvent(User user) {
        super(user);
    }

    public UserEvent(String action, User user) {
        super(user);
        this.action = action;
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "action='" + action + '\'' +
                ", user=" + user +
                '}';
    }

    public void fire(ApplicationContextHolder applicationContextHolder) {
        ApplicationContext applicationContext = applicationContextHolder.getApplicationContext();

        if(null != applicationContext) {
            logger.info("==> 发布事件:{} <==", this);
            applicationContext.publishEvent(this);
        }else {
            logger.warn("==> 无法获取ApplicationContext的实例. <==");
        }
    }
}
```

定义监听

```
@Component
public class UserEventListener implements ApplicationListener<UserEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @Override
    public void onApplicationEvent(UserEvent userEvent) {
        logger.debug("收到用户事件{}", userEvent);
        //todo 具体业务代码
    }
}
```

用户事件监听比较简单，只需要实现ApplicationListener接口，进行相应处理即可。

发送消息

发送消息比较简单，我们也可以直接在Event中实现，比如我们将上面UserEvent更改为如下：

```
public void fire(ApplicationContextHolder applicationContextHolder) {
        ApplicationContext applicationContext = applicationContextHolder.getApplicationContext();

        if(null != applicationContext) {
            logger.info("==> 发布事件:{} <==", this);
            applicationContext.publishEvent(this);
        }else {
            logger.warn("==> 无法获取ApplicationContext的实例. <==");
        }
}
```

但是必须自己先自定义ApplicationContextHolder

```
/**
 * 自定义ApplicationContextHolder, 在容器启动的时候就会
 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
```


那么我们就可以在需要的地方通过下面的代码来发布事件了:

```
@Autowired
private ApplicationContextHolder applicationContextHolder;
    
new UserEvent(user, UserEvent.ET_UPDATE).fire(applicationContextHolder);
```
然后演示

```
http://localhost:8081/users/messageinvoke?id=1&name=lanyage
```

<span style="color:red;">`2018-11-16 20:18:04.201  INFO 14119 --- [nio-8081-exec-4] c.l.s.kafka.userservice.event.UserEvent  : ==> 发布事件:UserEvent{action='update', user=User{id=1, name='lanyage'}} <==`</span>


> <span style="color:red;">总结来说，消息机制有三个组件，Action,Listener,Dispatcher,其中Action就是事件，Listener就是监听特定事件的，是范型接口，Dispacher就是ApplicationContext,通过publishEvent(e)来发布事件。</span>

### `Spring Cloud Bus机制`

我们上面了解了Spring的事件机制，那么Spring Cloud Bus又是如何将事件机制和Stream结合在一起的呢？总起来说机制如下：

1. 在需要发布或者监听事件的应用中增加@RemoteApplicationEventScan注解，通过该注解就可以启动Stream中所说的消息通道的绑定；
2. 对于事件发布，则需要继承ApplicationEvent的扩展类 -- RemoteApplicationEvent，当通过ApplicationContext.publishEvent()发布此种类型的事件时，Spring Cloud Bus就会对所要发布的事件进行包装，形成一个我们所熟知的消息，然后通过默认的springCloudBus消息通道发送到消息中间件；
3. 对于事件监听者则不需要进行任何变更，仍旧按照上面的方式就可以实现消息的监听。但，需要注意的一点就是在消费的微服务工程中也必须定义第2步所定义的事件，并且需要保障全类名一致(如果不一致，则需要做一点工作)。

嗯，就是这么简单。通过Bus我们就可以像编写单体架构应用一样进行开发，而不需要关系什么消息中间件、主题、消息、通道呀等等一大堆概念。

你也行在怀疑，是不是这么简单呀。那好，让我们来看看是不是很容易就可以实现Stream中示例。

### `重构Spring-Cloud-Stream-Kafka-Demo中的示例`

#### <span style="color:red">`重构USER-SERVICE微服务`</span>

##### `增加BUS的依赖`
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-kafka</artifactId>
</dependency>
```

##### `定义事件`

```
public class UserEvent extends RemoteApplicationEvent {

    private static Logger logger = LoggerFactory.getLogger(UserEvent.class);
    public static final String ET_UPDATE="USER_UPDATE";
    public static final String ET_DELETE="USER_DELETE";

    private String action;
    private String uniqueKey;

    public UserEvent() {
        super();
    }

    public UserEvent(Object source, String originService, String destinationService, String action, String id) {
        super(source, originService, destinationService);
        this.action = action;
        this.uniqueKey = id;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
```
这里和之前事件构建函数不同的是：在构建一个事件时需要指定<span style="color:red;">`originService`</span>和<span style="color:red;">`destinationService`</span>。对于事件发布者来说<span style="color:red;">`originService`</span>就是自己，而<span style="color:red;">`destinationService`</span>则是指将事件发布到那些微服务实例。<span style="color:red;">`destinationService`</span>配置的格式为：<span style="color:red;">`{serviceId}:{appContextId}`</span>,在配置时<span style="color:red;">`serviceId`</span>和<span style="color:red;">`appContextId`</span>可以使用通配符，如果这两个变量都使用通配符的话<span style="color:red;">`(\*:\*\*)`</span>,则事件将发布到所有的微服务实例。如只省略<span style="color:red;">`appContextId`</span>，则事件只会发布给指定微服务的所有实例，如：<span style="color:red;">`userservice:**`</span>，则只会将事件发布给<span style="color:red;">`userservice`</span>微服务。

##### `实现事件发布`

我们将商品微服务中商品变更中的代码修改为如下：

```
@Service
public class UserService {

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private List<User> users;


    @Autowired
    private ApplicationContextHolder holder;

    public UserService() {
        this.users = this.create();
    }

    public List<User> findAll() {
        return this.users;
    }

    public User save(User userDTO) {
        for (User user : this.users) {
            if (user.getId() == userDTO.getId()) {
                user.setName(userDTO.getName());
                break;
            }
        }
        this.users.add(userDTO);

        this.fireEvent(UserEvent.ET_UPDATE, userDTO);   //这是关键，保存用户之后，就发布更新事件

        return userDTO;
    }

    private void fireEvent(String eventAction, User user) {
        UserEvent userEvent = new UserEvent(user,   //源对象
                holder.getApplicationContext().getId(), //上下文ID
                "*:**", //将消息发往所有服务
                eventAction,    //事件
                String.valueOf(user.getId()));  //源对象的唯一标识符
        RemoteApplicationEventPublisher.publishEvent(userEvent,holder);
    }

    private List<User> create() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "兰亚戈"));
        users.add(new User(2, "戴梦晓"));
        return users;
    }
}
```

封装消息的发送方。

```
public class RemoteApplicationEventPublisher {
    public static final Logger logger = LoggerFactory.getLogger(RemoteApplicationEventPublisher.class);

    public static void publishEvent(RemoteApplicationEvent event, ApplicationContextHolder ach) {
        ApplicationContext cxt = ach.getApplicationContext();
        if(null != cxt) {
            cxt.publishEvent(event);
            logger.info("已经发布事件:{}", event);
        }else {
            logger.warn("无法获取到应用上下文实例，不能发布事件。");
        }
    }
}
```

##### `开启远程消息扫描`

最后，修改微服务启动类，添加@RemoteApplicationEventScan注解：

```
@SpringBootApplication
@EnableDiscoveryClient
@RemoteApplicationEventScan
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```
> 注意： 这里再次声明，<span style="color:red;">远程事件必须定义在`@RemoteApplicationEventScan`注解所注解类的子包中</span>，否则无法实现远程事件发布。

#### <span style="color:red">`重构USER-CONSUMER项目`</span>

##### `1.增加对Bus依赖`
##### `2.拷贝UserEvent到本项目`
##### `3.实现事件监听处理`

```
@Component
public class UserEventListener implements ApplicationListener<UserEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @Override
    public void onApplicationEvent(UserEvent userEvent) {
        logger.debug("==> 收到用户事件{} <==", userEvent);
        //todo 具体业务代码
    }
}
```

##### `4.开启远程消息扫描`

```
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@RemoteApplicationEventScan
public class UserConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserConsumerApplication.class, args);
    }
}
```
#### <span style="color:red">`测试`</span>

重构项目到此结束。

下面依次启动:

* `Kafka`服务器;
	* `zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties`开启`zookeeper`
	* `kafka-server-start /usr/local/etc/kafka/server.properties`开启`Kafka`
* 服务治理服务器: `EUREKA-SERVER`;
* `USER-SERVICE`微服务: `USER-SERVICE`;
* `USER-CONSUMER`微服务: `USER-CONSUMER`;。

然后,使用Postman访问服务。

#### `小结`


从重构后的代码来说的确使用Bus会更容易理解，也更容易上手。这对于当使用场合比较简单会非常好，比如：广播。典型的应用就是Config中的配置刷新，当在项目中同时引入了Config和Bus时，就可以通过/bus/refresh端点实现配置更改的广播，从而让相应的微服务重新加载配置数据。

当然，Bus简便性的另外一层含义就是不够灵活，因此具体是在项目中使用Bug还是直接使用Stream就看你的需要了，总起来一句就是：够用就好。


//问题是现在Consumer接收不到消息。


