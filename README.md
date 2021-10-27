# Kache缓存分布式框架

#### 概要 | synopsis

增强型 缓存框架

#### 架构 | framework

Java标准MVC架构如图：

![](https://www.hualigs.cn/image/6127d7294d06c.jpg)

接入Kache后的架构：

![](https://www.hualigs.cn/image/6127d72990452.jpg)

#### 优势 | advantage

- **降低响应时间**：Kache拥有进程间缓存与远程缓存的二级缓存设计，默认提供为HashMap的进程间缓存实现与Redis的远程缓存实现
- **负载分发**：分布式架构时，不区分业务模块而完成对远程缓存的删除更新操作，并不集中于同一业务的同一主机上处理，降低单机峰值负载
- **降低数据库的IO消耗**：Kache为Key-Value的缓存结构，缓存后可避免重复的数据库检索的IO消耗
- **散列PO级缓存**：与MySQL的非聚合索引和回表查询概念类似，Kache的远程缓存形式为key -> po的id列表 -> po，增强PO数据的一致性并提高修改数据的效率，并且默认的实现使用Lua脚本完成了一次网络io完成上述操作，且提高缓存命中率（详情见原理）
- **支持主流AMQP协议的消息队列**：基于Spring-Amqp框架开发，仅提供对应协议的消息队列即可使用
- **高兼容性、低代码侵入性**：Kache的实现原理是基于Service层与Dao层的代理与使用注解的形式获取摘要编码进行缓存，控制存取的渠道；仅需在对应的方法及类上添加注释而不修改本身的业务代码，不会影响本身的业务流程稳定性
- **并发同步读，并行异步写**：使用同步读写锁与消息队列，将读操作以：进程间缓存 -> 远程缓存 -> 数据库的次序同步读取，尽可能的减少对网络io的消耗并提高响应时间，而增删改操作则将三个数据源同时操作，并不会因为阻塞而较大地影响写入响应时间
- **高拓展性**：提供进程间缓存、Json序列化、远程缓存与缓存调度器的接口，允许使用者通过实现对应的接口兼容所需的NoSQL、进程间缓存、Json序列化框架以更好的兼容使用者的项目
- **对Redis、Guava、Gson项目支持程度高**：默认提供基于Redis、Guava、Gson的实现
- **IO消耗降低**：使用Lua脚本并使用sha1进行脚本缓存，最大化减少对IO的无用消耗

#### 使用 | Use

使用流程：

**1、Kache依赖引入（最新版为1.1.1）**

**2、Service层写入注解**

**3、Dao层写入注解**

**4、提供对应Redisson、RabbitMQ实例**

**5、配置文件参考**

##### 示例：

**1**.pom文件引入:

```xml
<dependency>
  <groupId>io.gitee.kould</groupId>
  <artifactId>Kache</artifactId>
</dependency>
```

**2**.**Service**的实现类或接口上添加@CachebeanClass(clazz = PO.class)注解并填入对应的PO类类型

然后在**需要缓存**的**读取**方法上添加@ServiceCache注解、**增删改**方法上添加@CacheChange注解

其对应的@ServiceCache的status默认为KacheConfig.Status.IS：

- status = KacheConfig.Status.IS : 准确条件查询方法
- status = KacheConfig.Status.LIKE : 模糊条件查询方法
- status = KacheConfig.Status.NO_ARG : 无参查询方法
- status = KacheConfig.Status.BY_ID : ID查询方法
- status = KacheConfig.Status.ALL : 无条件查询方法

```java
//该标签用于声明Service对应持久类
@CacheBeanClass(clazz = ConfigIndexPO.class)
@Service
public class ConfigIndexServiceImpl extends BaseServiceImpl<ConfigIndexPO, ConfigIndexMapper> implements IConfigIndexService {

    //该标签用于声明该方法需要缓存
    @ServiceCache(status = Status.NO_ARG)
    @Override
    public BaseConfigIndexDTO findAllConfigIndexOrderByCreateTime() {
        QueryWrapper<ConfigIndexPO> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time") ;
        wrapper.last("limit 1") ;
        BaseConfigIndexDTO dto = new BaseConfigIndexDTO() ;
        TransUtil.po2dto(mapper.selectOne(wrapper), dto) ;
        return dto;
    }
    
    //该标签用于声明此方法会导致缓存状态改变
    @CacheChange
    @Override
    public <T extends BaseDTO> int edit(T t) {
        ConfigIndexPO configIndexPO = new ConfigIndexPO();
        TransUtil.dto2po(t,configIndexPO) ;
        ConfigIndexPO result = this.mapper.selectById(configIndexPO.getId());
        BeanUtil.copyProperties(configIndexPO, result,
                true, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        result.setId(null) ;
        return this.mapper.insert(result);
    }
}
```

**3**.其对应的**Dao层**的Dao方法添加注释：

- 搜索方法：@DaoSelect
- 插入方法：@DaoInsert
- 更新方法：@DaoUpdate
- 删除方法：@DaoDelete

```java
public interface BaseMapper<T> {
    //该标签声明该方法为持久类数据插入
    @DaoInsert
    int insert(T entity);
    
    //该标签声明该方法为持久类数据删除
	@DaoDelete
    int deleteById(Serializable id);
    
    //该标签声明该方法为持久类数据修改
	@DaoUpdate
    int update(@Param("et") T entity, @Param("ew") Wrapper<T> updateWrapper);

    //该标签声明该方法为持久类数据查找
    @DaoSelect
    T selectById(Serializable id);

    //该标签声明该方法为持久类数据查找
    @DaoSelect
    IPage<T> selectPage(IPage<T> page, @Param("ew") Wrapper<T> queryWrapper);
}
```

**4**.提供RedissonClient与消息队列（此处略）

```java
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        //Redis地址
        config.useSingleServer().setAddress("redis://123.123.123.123:6379");
        return Redisson.create(config);
    }
}
```

可选配置（默认值）：

```yaml
#Kache各属性可修改值
kache:
   dao:
       lock-time: 3 //分布式锁持续时间，默认为2
       base-time: 300 //缓存基本存活时间，默认为300
       random-time: 120 //缓存随机延长时间，默认为120
   interprocess-cache:
       enable: true //进程间缓存是否开启，默认为true
       size: 50 //进程间缓存数量
   data-field:
   	   name: records //分页包装类等包装类对持久类的数据集属性名：如MyBatis-Plus中Page的records属性
       declare-type: java.util.List //上述属性名所对应的属性声明类型（全称），默认为java.util.List
```

规范说明：

- 基于DTO概念，Service方法允许**无参**和**仅有一个参数**、即所需参数需要使用一个**DTO**进行封装
- Dao方法**不允许**针对某一业务而**业务化**、否则与Service并无本质上的区分可能导致缓存出现问题

- 若Service类或接口无法添加添加注解则可以修改该含有Service类的包名为**service**
- **若Dao类或接口无法加入注解则可以修改Dao的包名为mapper且方法名格式**(默认MyBatis-plus格式)：
- - 搜索方法：select*(..)
  - 插入方法：insert*(..)
  - 更新方法：update*(..)
  - 删除方法：delete*(..)

#### 原理 | principle

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

Key的编码形式则为：

- （是否为单一持久类判断符）+Dao层方法+Dao层方法参数（二进制序列化）+Service注解对应的枚举值+Service层方法+Service层方法参数+PO类名的形式

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

**欢迎一起讨论或是提供改善意见与测试结果~~~~**
