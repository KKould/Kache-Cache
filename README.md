![Logo](https://s3.bmp.ovh/imgs/2022/01/2f5f01726cfef8fb.png)

<p align="middle">
  <a href="https://search.maven.org/artifact/io.gitee.kould/Kache/1.6.0/jar">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.gitee.kould/Kache.svg?style=flat-square">
  </a><a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
  <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
      <img src="https://img.shields.io/badge/JDK-1.8+-blue.svg" />
  </a>
</p>

<p align="center">
    <strong>
    Kache - 散列式缓存
    </strong>
</p>

----

### 与其他缓存框架有何差异？

### How is it different from other caching frameworks？

GuavaCache是一个优秀的缓存框架，他出身于IT大头Google，其中对缓存的各种定义和操作都做出了非常合理的诠释与实现。

而类似的缓存框架也有J2Cache、Memcache、Ehcache等充分经受实践拷打的优秀开源框架。

甚至更甚者诸如SpringCache、AutoLoadCache这样的自动化缓存框架让用户脱离了缓存变更而手动处理缓存操作的繁琐业务流程。

而在这众多优先开源缓存框架之中，Kache存在的必要是什么？

> 论证这种缓存结构对于内存空间的最大化利用是否可行

**Kache在缓存结构上对传统的缓存概念作出了“解耦”**

往常的缓存结构基本是将函数的参数作为Key，函数的结果作为Value

![](https://s3.bmp.ovh/imgs/2022/03/0a2b1ef988f419d7.png)

这样的缓存结构既简单，又满足了大多数情况下的使用。

只是在这种情况下产生了新的数据冗余问题：

![](https://s3.bmp.ovh/imgs/2022/03/25263c0411d34243.png)

![](https://s3.bmp.ovh/imgs/2022/03/36d284e947e06742.png)

上图则为这种缓存结构下，不同参数指向同一数据的情况。

在类似进程间缓存时，像Java之中，这样的key-value使用类似HashMap的数据结构作为存储，故value和key为对象时都是以引用的形式存储，在合理的操作下并不会存在有重复的对象占用内存空间。

而在Redis这样常作为缓存的NoSQL数据库则并非如此。

在存入Redis时，数据往往都需要经过序列化；而序列化后的数据即使值是一致，但因为key的不同而导致同一份序列化数据会分别存储，造成数据冗余的占用。不仅在大对象序列化时会占用大量的cpu计算资源，也会导致有限的IO无法得到充分的利用。

**这里引用一篇腾讯在知乎发布的一部有关于缓存文章的一句话：**

> Redis key大小设计： 由于网络的一次传输MTU最大为1500字节，所以为了保证高效的性能，建议单个k-v大小不超过1KB，一次网络传输就能完成，避免多次网络交互； k-v是越小性能越好

这也意味着，若是像结果为List这样的数据集时，避免相同数据重复的序列化而导致序列化结果的空间变大，则能让缓存越小而越好。

**而如何让数据减少重复序列化占用空间的同时保证数据信息的完整性？**

我的想法是，利用像Java进程对对象的引用一样，将单独的数据分别存入Redis之中，将id作为引用信息，使数据集中不包含具体的数据内容，取而代之则为各个数据的id。

这种方式将具体数据与参数进行解耦，将单独且具体的数据分布在Redis之中。

在面对刚才不同参数对应同一参数之间，做到了真正贴合实际引用逻辑。

这样的缓存结构之中，**我称上缓存为索引缓存、下缓存为元数据缓存**

而在Kache实现中更具体一些的逻辑如下：

当数据被**条件**读取时：

- 通过编码器将参数编码，作为索引缓存的Key

- 若对应的索引缓存不存在
  
  - 解析数据集中元数据的个数与主键
  
  - 拼接Echo脚本，用于查询Redis中所需的元数据缓存是否存在
  
  - 若元数据都无或仅有部分：
    
    - 将对应的数据集中元数据解析出，并将Redis中缺少的元数据与该索引数据一起保存在Redis之中
      
      ![](https://s3.bmp.ovh/imgs/2022/03/e81275da3402fe92.png)
  
  - 若元数据都有：
    
    - 直接将索引缓存存入，**不需要对具体数据再序列化且存入Redis之中。从而降低数据变动而导致缓存更新成本**
      
      ![](https://s3.bmp.ovh/imgs/2022/03/4eaac6c6c9c9db8f.png)

- 若对应索引数据存在
  
  - 将对应的参数通过编码器获取索引缓存的Key
  - 通过Lua脚本在获取对应的索引缓存时，使用Redis的mget指令批量获取对应的元数据并进行填充，然后返回加工好的索引缓存（完整的序列化数据）

这样类型的缓存所能带来的特点：

- 数据集与单个数据分离
  
  - 数据集删除的成本降低
    
    - 当数据集被修改时，根据实际情况可能会重复利用之前所存储过的元数据缓存
    
    - 最大化减少序列化的内容，做到不重复序列化一致数据

- 元数据以Id作为Key
  
  - 为FindById方法直接提供对应的Key，可以直接获取对应元缓存

- 索引缓存仅有容器类的状态与元数据id集合
  
  - 删除索引缓存而使缓存不删除元缓存数据
  
  - 多个索引缓存共享一致的元缓存数据，去除数据冗余

#### 因此可得其适用场景：

- 对缓存依赖性较强，且数据变动较为频繁

- Redis的基础建设较弱，服务器配置较低，对缓存的可用空间有限制

- 应用服务器CPU较差且IO密集

- 通过Id获取数据的方法使用较为频繁

- 瓶颈为数据库操作

- 大量重复数据

- 单个数据的内容大

- 数据体量大

**若是满足以上大多数情况，则该缓存结构能给你带来相较于传统框架而言更为理想的性能。**

**该框架仅是此结构的一种实现，未经实际生产环境磨练。欢迎尝鲜**

### 概要 | Synopsis

- **Dao持久层缓冲增强**
- **实验性质的缓存框架**
  - 零重复序列化元数据
  - 元数据高命中率
  - 低缓存变更代价

### 结构 | Structure

```
|- com.kould
    |- annotaion  操作注解
        |- DaoDelete    标记删除注解
        |- DaoInsert    标记新增注解
        |- DaoSelect    标记搜索注解
        |- DaoUpdate    标记更新注解
    |- api        对外调用接口
        |- Kache    控制面板
    |- codec      序列化编码器
        |- KryoRedisCodec   Kryo序列化编码器
        |- ProtostuffRedisCodec    Ptotostuff序列化编码器
    |- core       读写处理逻辑
        |- impl    实现
            |- BaseCacheHandler    基础读写处理逻辑实现
        |- CacheHandler    读写处理逻辑定义接口
    |- encoder    键名编码器
        |- impl    实现
            |- BaseCacheEncoder    基础键名编码器实现
        |- CacheEncoder    键名编码器定义接口
    |- entity     包装实体
        |- KacheMessage    摘要信息封装实体
        |- KeyEntity       方法名摘要匹配实体
        |- MethodPoint     方法代理封装实体
        |- NullValue       空值实体
        |- Status          方法状态枚举
    |- function   函数式接口
        |- KeyFunction     取键函数
        |- ReadFunction    读取函数
        |- SyncCommandCallback 同步命令函数
        |- WriteFunction   写入函数
    |- handler    删改策略
        |- impl    实现
            |- AmqpAsyncHandler  基于AMQP的异步策略实现
            |- DBFristHandler    数据库优先同步策略实现
        |- AsyncHandler    异步策略接口定义
        |- StrategyHandler 策略接口定义
        |- SyncHandler     同步策略接口定义
    |- inerceptor 拦截器
        |- CacheMethodInerceptor 缓存方法代理拦截器
    |- listener   监听器
        |- impl    实现
            |- MethodStatistic    方法统计计数器
            |- StatisticsListener 统计监听器
            |- StatisticsSnapshot 统计计数快照
        |- CacheListener   缓存监听器定义
        |- ListenerHandler 缓存监听处理器
    |- lock       并发锁封装
        |- impl    实现
            |- LocalLock   本地锁实现
            |- RedissonLock   基于Redisson实现分布式锁
        |- KacheLock   并发锁封装定义
    |- logic      更新处理逻辑
        |- impl    实现
            |- BaseCacheLogic    基本更新处理逻辑实现
        |- CacheLogic   更新处理逻辑定义接口
    |- manager    操纵管理器
        |- impl    实现
            |- BaseCacheManagerImpl    基础二级缓存封装操作实现
            |- GuavaCacheManagerImpl   基于GuavaCache的进程缓存实现
            |- RedisCacheManagerImpl   基于Redis的远程缓存实现
        |- IBaseCacheManager         二级缓存封装操作接口定义
        |- InterprocessCacheManager  进程缓存操作接口定义
        |- RemoteCacheManager        远程缓存操作接口定义
    |- properties 配置信息包装
        |- DaoProperties                Dao层配置
        |- DataFieldProperties          数据名定义配置
        |- InterprocessCacheProperties  进程缓存配置
        |- ListenerProperties           监听器配置
    |- service    Redis操作客户端
        |- RedisService    Redis封装实现
    |- type    结构化接口
        |- Builder    建造者接口
    |- utils    工具
        |- FieldUtils      属性反射工具
        |- KryoUtils       Kryo序列化工具
        |- ProtostuffUtils Protostuff序列化工具
```

### 优势 | Advantage

- **极低侵入**：以黑盒的形式作为Dao持久化层的代理提供缓存功能
- **面向数据库操作**：Kache仅服务于Dao持久层，降低MySQL数据库等负担，转移至Redis
- **溅射查询**：单次条件查询会被解析为多个元数据缓存，能够做到简单的缓存预存
- **散列PO级缓存**：与MySQL的非聚合索引和回表查询概念类似，Kache的远程缓存形式为key -> po的id列表 -> po，增强PO数据的一致性并提高修改数据的效率，并且默认的实现使用**Lua脚本完成了一次网络io**完成上述操作，且提高缓存命中率（详情见原理）
- **基于注解**：Kache的实现原理是基于Dao层的动态代理与使用注解的形式获取摘要编码进行缓存。仅需在对应的方法及类上添加注释而不修改本身的业务代码，**不会影响本身的业务流程稳定性**
- **支持策略拓展**：针对不同应用场景，可以自己通过策略实现延时双删、Write Back、Read/Write Through等策略，同时也能够拓展Kafka等其他消息队列（内部已提供同步的数据库优先处理（默认）与异步基于RabbitMQ的异步处理）
- **高拓展性**：提供进程间缓存、远程缓存与缓存调度器的接口，**允许使用者通过实现对应的抽象类兼容所需的NoSQL、进程间缓存框架**以更好的兼容使用者的项目
- **IO消耗低**：积极使用Lua脚本进行IO优化并用GuavaCache辅助进程间缓存，**最大化减少网络IO带来的性能影响
- **高速读取**：通过类似HashMap的编码形式进行对应数据的去除，**读取Lua脚本为静态常量，仅写入Lua脚本为动态拼接**
- **内置Actuator信息端点**：**允许通过URL路径动态观测缓存命中情况**，详情在示例中
- **支持自定义监听器**：允许通过自定义监听器进行缓存动作的额外业务处理，**默认提供StatisticsListener统计缓存监听器**

### 使用 | Use

#### **1、Kache依赖引入**

#### **2、Dao层写入注解**

#### 3、提供Lettuce实例

#### **可选：根据策略提供所需组件（异步策略需要提供对应RabbitMQ、Kafka等实例）**

#### **可选：配置文件参考(详情见说明，可跳过)**

##### 示例：

**1**.pom文件引入:

```xml
<dependency>
  <groupId>io.gitee.kould</groupId>
  <artifactId>Kache</artifactId>
</dependency>
```

**2**.其对应的**Dao层**的Dao方法添加注释：

- 搜索方法：@DaoSelect
  
  - 其对应的@DaoSelect的status默认为Status.BY_Field：
    - status = Status.BY_FIELD : 非ID查询方法
    - status = Status.BY_ID : ID查询方法

- 插入方法：@DaoInsert

- 更新方法：@DaoUpdate

- 删除方法：@DaoDelete

- **(MyBatis-Plus的BaseMapper已经在代码中做了支持，不需要加入注释)**

```java
@Repository
@DaoClass(Tag.class)
public interface TagMapper extends BaseMapper<Tag> {

    @Select("select t.* from klog_article_tag at "
            + "right join klog_tag t on t.id = at.tag_id "
            + "where t.deleted = 0 AND at.deleted = 0 "
            + "group by t.id order by count(at.tag_id) desc limit #{limit}")
    @DaoSelect(status = Status.BY_FIELD)
    List<Tag> listHotTagsByArticleUse(@Param("limit") int limit);
}
```

**3**.提供Lettuce实例：

```java
@Configuration
public class RedisAutoConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.database}")
    private Integer database;

    @Bean
    RedisClient redisClient() {
        RedisURI uri = RedisURI.Builder.redis(this.host, this.port)
                .withPassword(this.password)
                .withDatabase(this.database)
                .build();
        return RedisClient.create(uri);
    }
}
```

**可选**：提供消息队列源（可选，当使用框架内提供的异步策略时），参考：

```java
@Configuration
public class MessageQueueConfig {

    public static final String LOG_QUEUE_NAME = "KLOG_LOG_QUEUE";

    @Bean
    public Queue logQueue() {
        return new Queue(LOG_QUEUE_NAME,false,false,false);
    }

    @Bean
    public Queue kacheInsertQueue() {
        return new Queue(AmqpAsyncHandler.QUEUE_INSERT_CACHE,false,false,false);
    }

    @Bean
    public Queue kacheDeleteQueue() {
        return new Queue(AmqpAsyncHandler.QUEUE_DELETE_CACHE,false,false,false);
    }

    @Bean
    public Queue kacheUpdateQueue() {
        return new Queue(AmqpAsyncHandler.QUEUE_UPDATE_CACHE,false,false,false);
    }
}
```

**可选**：配置（默认值）：

```yaml
#Kache各属性可修改值
kache:
   dao:
       lock-time: 3 //分布式锁持续时间
       base-time: 86400 //缓存基本存活时间
       random-time: 600 //缓存随机延长时间
       poolMaxTotal: 20    //Redis连接池最大连接数
       poolMaxIdle: 5    //Redis连接池最大Idle状态连接数
   interprocess-cache:
       enable: true //进程间缓存是否开启
       size: 50 //进程间缓存数量
   data-field:
       name: records //分页包装类等包装类对持久类的数据集属性名：如MyBatis-Plus中Page的records属性
       declare-type: java.util.List //上述属性名所对应的属性声明类型（全称）
   listener:
          enable: true //监听器是否开启
```

**其他说明：**

- 内部默认提供本地锁LocalLock用于单机环境，同时提供分布式读写锁**RessionLock实现**，需要手动提供。用于提供该框架下在**分布式环境**下的**缓存穿透处理**的缓存读写安全
- - 搜索方法：select*(..)
  - 插入方法：insert*(..)
  - 更新方法：update*(..)
  - 删除方法：delete*(..)

**Actuator端点**：**/kache**：下为例子，参数为：

- 总命中次数：sumHit
- 总未命中次数：sumNotHit
- 命中service方法：
  - 命中的key名：
    - 命中次数：hit
    - 未命中次数：notHit

```json
{
    "com.kould.listener.impl.StatisticsListener":{
        "statisticMap":{
            "com.kould.klog.entity.ArticleAndTag.selectList":{
                "key_set":[
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList7366488776458918380",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList3688230540085275879",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList-6616124714197240509",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList-8825655128112326602",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList-4800593238996224284",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList8175467167130639943",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleAndTagselectList5515765047547306303"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.Comment.selectPage":{
                "key_set":[
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.CommentselectPage-5088645369418484732",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.CommentselectPage-601408742373120668",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.CommentselectPage-3261110861956454308"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.ArticleBody.selectById":{
                "key_set":[
                    "KACHE:1504703525416009729",
                    "KACHE:1487892466958348290",
                    "KACHE:1501379280208101377"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.Article.selectById":{
                "key_set":[
                    "KACHE:1504703525986435074",
                    "KACHE:1487892467822374914",
                    "KACHE:1501379280321347585"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.Article.selectList":{
                "key_set":[
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectList-2737135272387492214",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectList6742038077807145512"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.Tag.listHotTagsByArticleUse":{
                "key_set":[
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.TaglistHotTagsByArticleUse8537847423193657128"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.Category.selectById":{
                "key_set":[
                    "KACHE:1487868148098592770",
                    "KACHE:1487868235843432449",
                    "KACHE:1487868277975216130"
                ],
                "hit":{

                },
                "notHit":{

                }
            },
            "com.kould.klog.entity.Article.selectPage":{
                "key_set":[
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectPage-8185391717994071526",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectPage4874353664547449116",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectPage-512645026620581858",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectPage-5899643717788612832",
                    "KACHE:NO_ID-METHOD_SERVICE_BY_FIELDcom.kould.klog.entity.ArticleselectPage7160101664752907810"
                ],
                "hit":{

                },
                "notHit":{

                }
            }
        },
        "sumHit":23,
        "sumNotHit":19
    }
}
```

注：若想使用端点需要在配置文件中暴露

例子：暴露全部Actuator端点

```yaml
management.endpoints.web.exposure.include=*
```

### 架构 | Framework

Java标准MVC架构如图：

![Kache结构图](https://s3.bmp.ovh/imgs/2022/04/09/685006080e2d0b91.png)

**（溢出方框外表示允许拓展）**

### 对比 | Contrast

#### Spring Cache

- **应用层面**：
  - Spring Cache往往是基于Service进行缓存
  - Kache是基于Dao进行缓存；
  - 也就是说其实两者是允许共同使用的
- **应用架构**：
  - Spring Cache与Kache同样都可以通过内置拓展类的形式完成单机与分布式的应用架构
- **代码侵入与学习成本**：
  - 都基于Aop+注解完成，代码侵入性极低
  - Spring Cache的注解为：
    - @CacheConfig：主要用于配置该类中会用到的一些共用的缓存配置
    - @Cacheable：主要方法返回值加入缓存。同时在查询时，会先从缓存中取，若不存在才再发起对数据库的访问
    - @CachePut:配置于函数上，能够根据参数定义条件进行缓存，与@Cacheable不同的是，每次回真实调用函数
    - @CacheEvict:配置于函数上，通常用在删除方法上，用来从缓存中移除对应数据
    - @Caching:配置于函数上，组合多个Cache注解使用
  - Kache的注解为：
    - @CacheBean：标记Service对应Mapper的Po类类型（即缓存类型）
    - @CacheImpl：标记Service的实现类
    - @DaoSelect：标记为数据搜索的方法
    - @DaoInsert：标记为数据插入的方法
    - @DaoUpdate：标记为数据更新的方法
    - @DaoDelete：标记为数据删除的方法
    - 在应用基于MyBatis-Plus的IService与BaseMapper的情况下，仅需要@CacheBean注解即可完成缓存操作
- **缓存结构**：
  - Spring Cache在基于Redis的情况下，缓存是基于简单的Key—Value对应实现的
  - Kache同样在使用二级缓存（默认为Redis）的情况下，会将缓存分为
    - **索引缓存**
    - **元数据缓存**
  - 并将搜索分为
    - **通过Id进行单个数据搜索**：直接搜索元数据缓存
    - **通过某条件或者无条件等进行数据搜索**：通过解析dao方法以及参数进行特制Hash编码生成一个特殊的key，再通过lua脚本让key获取到索引缓存的同时对索引缓存中的元数据id替换为元数据缓存value
- **缓存增删改逻辑**
  - Spring Cache
    - 增，即@CachePut：通过新增或替换对应key缓存实现，但往往会与@CacheEvict混合使用（因为可能会影响条件搜索）
    - 删改，即@CacheEvict：通过删除单条或多条数据（往往都是将同一Po类型下的所有缓存删除）
  - Kache
    - 增删为同一策略：通过删除该Po类型的所有索引缓存进行条件缓存的清除，若为单条数据删除则会再删除掉对应的Id元数据缓存
    - 改：在删除索引的同时，对对应Id的元数据缓存进行修改
  - 最大差异点：Kache与SpringCache会清空条件搜索的缓存，但Id搜索的缓存基本不会删除，缓存利用率较高
- **缓存命中率**：
  - Spring Cache在简单的Key—Value缓存结构下，缓存的命中基本为同样的参数下返回同样的值，结合**缓存增删改来看**命中率适中偏低
  - Kache则会分为两种情况的命中率
    - **通过Id进行单个数据搜索**：在重复的Id搜索情况下，命中率仍然与Spring Cache保持一致，但是若该应用的缓存被多个不同的方法进行查询（如有条件、无条件、通过id等）交叉进行使用时，在**缓存更新**处所述的**通过某条件或者无条件进行数据搜索**会在单次的条件查询时带来多个元缓存（笔者称为**溅射查询**），这意味着在网页中搜索某一条件时，若之后继续点击其结果的数据时，则第二次是必定命中的；
      - 在**缓存成本**处，也会体现到其中Kache的缓存利用率更高，不会因为数据增删改而类似Spring Cache一样删除所有元数据缓存
      - 故实际情况**id搜索**的缓存命中率会**大大高于**Spring Cache
    - **通过某条件或者无条件等进行数据搜索**：索引缓存的Key是由参数决定的，所以结构和SpringCache的命中率是基本保持一致的，但由于Kache所在的Dao层次相较于SpringCache的Service层次更底层，所以这导致了Dao的方法使用大多数情况下会更加密集，使实际使用的缓存的命中率相较Spring Cache的相较起来更高（或是称为缓存复用率）

### 原理 | Principle

Kache的原型的描述文章：

[基于Redis的DTO应用Service层缓存AOP](https://zhuanlan.zhihu.com/p/395076311)

基于上述文章的主要变化为：

##### 代理对象的转移：

Service层缓存对于Dao层缓存来说产生一个问题：

> - 缓存更新问题：Service支持DTO概念时，针对有一种PO却产生有不同的形态的缓存，最容易导致的问题是缓存删除、更新、新增时带来的一致性问题，对于PO结果有Page类对象封装的缓存更甚，对于缓存的利用率较低

而Dao缓存则能够去弥补上面所述的问题，是高性能的缓存所必不可少的

但于此同时Dao又会导致一系列实际开发上的问题

1. 标准Dao的开发并不偏向业务化、不符合原先缓存Key逻辑
2. Dao层有着一系列持久层框架带来的默认实现，难以对其命名规范的同一化
3. Service层对Dao层耦合大、Dao的修改对系统稳定性是致命性的

可见与原先的缓存设计有着很大的冲突

于是使用了两层的AOP：

- ServiceAop获取Service方法信息摘要、通过ThreadLocal传递给DaoAop
- DaoAop获取Service层方法进行编码为Key，使用Key获取缓存的结果

以此避免同一Service方法下调用同一Dao方法但不同参数而引起的缓存冲突问题

##### Aop转变为框架：

- 主要通过注解的形式+切点的形式提供更好的兼容性
- 注解提高代码的可读性，且可以应对多种不同包名的这种细节性问题
- 一些框架的默认实现无法修改代码，则可以通过默认提供的切点来修改包名而兼容
- 降低对原项目的耦合，使更多第三方项目也能使用上
- 修复原本设计带来的一系列不足点的耦合问题
- 通过注解降低其业务代码侵入性，并简化使用

##### 二级缓存设计：

- 即使是NoSQL所带来的提升也仍然会导致网络IO的占用，为了更多的性能提升以及无用IO损耗则加入了进程间缓存的概念。
- 使用了二级缓存调度器，允许用户通过自定义二级缓存调度器去调度两个缓存的使用。

##### 缓存散列化：

- 通过解析结果的持久类数据集，将持久类数据拆分并分别储存，将原包装类除去数据集后与数据集装填入List类型存入，去除实体数据与缓存索引的直接耦合
  - 使实体数据原子化，加强数据一致性
  - 减少缓存值重复，提高缓存的空间利用率
  - 更直观观察到热点数据
  - 散列开的单一持久化类有利于id查找
- Kache提供有KacheConfig.Status.BY_ID的状态属性用于标记直接id搜索，而实际的查找流程往往为：分页查询-》单一数据查询
  - 如百度中搜索某一关键词后，点击其中提供内容中的一条。
- 散列化可以使在如上种情景下，不存在该关键词的缓存时，获取其分页数据时保存其单一实体缓存，使第二步的单一数据时必定命中缓存（点击其分页内容下），提高其缓存命中率且更加符合实际的应用场景

##### 缓存结构：

![Kache缓存结构](https://s3.bmp.ovh/imgs/2021/11/4ec53d620c952a95.jpg)

### 测试 | Test

##### 一分钟持续并发单一PO类重复操作

- 使用jmeter进行7线程查询+3线程增改
- 环境皆为局域网内，双方环境一致（Kache组使用Redis+MySQL，MySQL组仅为MySQL）
- Kache版本为1.3.2

结果如下

###### **MySQL组：**

- **图形结果**

![](https://s3.bmp.ovh/imgs/2021/11/fa2d2f05caf9268e.png)

- **汇总报告**

![](https://s3.bmp.ovh/imgs/2021/11/eda0920a774d0ae6.png)

**Arthas的dashboard检测情况：**

​    开始：

​    ![](https://s3.bmp.ovh/imgs/2021/11/e677bc3a4d56d171.png)

​    结束：

​    ![](https://s3.bmp.ovh/imgs/2021/11/38b6dfd492b29a71.png)

###### Kache组：

- **图形结果**

![](https://s3.bmp.ovh/imgs/2021/11/712a28313bbf0615.png)

- **汇总报告**

![](https://s3.bmp.ovh/imgs/2021/11/123b087ffde32502.png)

**Arthas的dashboard检测情况：**

​    开始：

​    ![](https://s3.bmp.ovh/imgs/2021/11/16e27216b18fd98b.png)

​    结束：

​    ![](https://s3.bmp.ovh/imgs/2021/11/7abfcb1943f50df1.png)

###### **测试总结**：

- 吞吐量/平均单次数据读取时间降低3倍左右（同为简单条件情况下，理论实际涉及海量数据与复杂查询时，Kache的延迟速度基本不会变动，提升会变为几个数量级）
- 各类读取速度平均时间和最小值时间更为稳定一致
- 标注偏差时间更低更稳定
- 最大值基本持平
- 该场景的GC次数：
  - Kache组：142
  - MySQL组：249
  - 相较起来Kache产生的内存占用更低

**欢迎一起讨论或是提供改善意见与测试结果~~~~**

#### 正在使用

- 安徽鼎信：安徽招采平台

#### 感谢名单

**感谢@z_k_y耐心的测试使用和Bug反馈**
