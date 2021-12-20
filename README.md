# Kache - 将持久化操作推向极致

<p align="left">
  <a href="https://search.maven.org/artifact/io.gitee.kould/Kache/1.6.0/jar">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.gitee.kould/Kache.svg?style=flat-square">
  </a><a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

#### 概要 | synopsis

**仅通过注解即可完成缓存操作**

**缓存与业务逻辑分离**

**开箱即用**

**极致优化响应速度与网络IO**

**支持分布式**

#### 优势 | advantage

- **0.3毫秒响应时间**：Kache拥有进程间缓存与远程缓存的二级缓存设计，默认提供为Guava的进程间缓存实现与Redis的远程缓存实现；在默认的配置下，id搜索平均为0.2毫秒，条件搜索平均为0.4毫秒的单次响应速度

- **极低侵入**：若使用MyBatis-Plus的IService和BaseServiceImpl，**则仅需要一个注解即可完成缓存操作**

- **降低数据库的IO消耗**：Kache为Key-Value的缓存结构，缓存后可避免重复的数据库检索的冗余消耗

- **溅射查询**：条件查询会被解析为多个元数据，提高缓存命中率

- **散列PO级缓存**：与MySQL的非聚合索引和回表查询概念类似，Kache的远程缓存形式为key -> po的id列表 -> po，增强PO数据的一致性并提高修改数据的效率，并且默认的实现使用Lua脚本完成了一次网络io完成上述操作，且提高缓存命中率（详情见原理）

- **基于注解**：Kache的实现原理是基于Service层与Dao层的代理与使用注解的形式获取摘要编码进行缓存，控制存取的渠道；

  仅需在对应的方法及类上添加注释而不修改本身的业务代码，不会影响本身的业务流程稳定性

- **并发同步读，并行异步写**：使用同步读写锁与消息队列，将读操作以：进程间缓存 -> 远程缓存 -> 数据库的次序同步读取，尽可能的减少对网络io的消耗并提高响应时间，而增删改操作则将三个数据源同时操作，并不会因为阻塞而较大地影响写入响应时间（默认提供该策略）

- **支持策略拓展**：针对不同应用场景，可以自己通过策略实现延时双删、Write Back、Read/Write Through等策略（默认提供异步基于AMQP的异步处理（即上条）与同步的数据库优先处理）

- **高拓展性**：提供进程间缓存、远程缓存与缓存调度器的接口，允许使用者通过实现对应的抽象类兼容所需的NoSQL、进程间缓存框架以更好的兼容使用者的项目

- **IO消耗低**：积极使用Lua脚本进行IO优化并用GuavaCache辅助缓存，最大化减少网络IO带来的性能影响

- **可读性强**：代码量大约为2400行（含注释和空换行）

- **高速读取**：通过类似HashMap的编码形式进行对应数据的去除，读取Lua脚本为静态常量，仅写入Lua脚本为动态拼接

- **支持自定义监听器**：允许通过自定义监听器进行缓存动作的额外业务处理，默认提供StatisticsListener统计监听器

- **内置Web信息端点**：允许通过Resutful路径动态观测缓存命中情况，详情在示例中

#### 使用 | Use

使用流程：

**1、Kache依赖引入**

**2、Service层写入注解**

**3、Dao层写入注解**

**4、根据策略提供所需组件（默认为异步删除需要提供对应RabbitMQ实例）**

**5、配置文件参考(详情见说明，可跳过)**

##### 示例：

**1**.pom文件引入:

```xml
<dependency>
  <groupId>io.gitee.kould</groupId>
  <artifactId>Kache</artifactId>
</dependency>
```

**2**.**Service**的实现类或接口上添加@CachebeanClass(clazz = PO.class)注解并填入对应的PO类类型

**若使用IService类似的模板ServiceImpl，则在对应的ServiceImpl上添加@CacheImpl即可**

**（若使用MyBatis-Plus的默认ServiceImpl可跳过）**

```java
//该标签用于声明Service对应持久类
@CacheBean(clazz = ConfigIndexPO.class)
@Service
public class ConfigIndexServiceImpl extends BaseServiceImpl<ConfigIndexPO, ConfigIndexMapper> implements IConfigIndexService {

    @Override
    public List<BlogPO> findByBlogTitle(BlogBaseDTO blogBaseDTO) {
        Page<BlogPO> page = new Page<>(blogBaseDTO.getIndex(), blogBaseDTO.getStepSize()) ;
        QueryWrapper<BlogPO> wrapper = new QueryWrapper<>() ;
        wrapper.eq("title",blogBaseDTO.getTitle()) ;
        return this.blogMapper.selectPage(page, wrapper).getRecords() ;
    }
    
    @Override
    public int edit(BlogPO blogPO) {
        return this.blogMapper.updateById(blogPO);
    }
}
```

**3**.其对应的**Dao层**的Dao方法添加注释：

- 搜索方法：@DaoSelect
- 其对应的@DaoSelect的status默认为Status.BY_Field：

  - status = Status.BY_FIELD : 业务查询方法
  - status = Status.BY_ID : ID查询方法
- 插入方法：@DaoInsert
- 更新方法：@DaoUpdate
- 删除方法：@DaoDelete
- **(若使用MyBatis-Plus的默认Mapper可跳过)**

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
    @DaoSelect(Status.BY_ID)
    T selectById(Serializable id);

    //该标签声明该方法为持久类数据查找
    @DaoSelect(Status.BY_FIELD)
    IPage<T> selectPage(IPage<T> page, @Param("ew") Wrapper<T> queryWrapper);
}
```

**4**.提供消息队列源（此处略）

**5**.可选配置（默认值）：

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
   listener:
   	   enable: true //监听器是否开启，默认为true
```

**其他说明：**

- **Dao方法**不允许针对某一业务而**业务化**、否则与Service并无本质上的区分可能导致缓存出现问题
- 内部默认提供本地锁LocalLock用于单机环境，同时提供分布式读写锁**RessionLock实现**，需要手动提供。用于提供该框架下在**分布式环境**下的**缓存穿透处理**的缓存读写安全
- src/resource/other目录下有一份JedisPool配置的文件可供参考
- - 搜索方法：select*(..)
  - 插入方法：insert*(..)
  - 更新方法：update*(..)
  - 删除方法：delete*(..)

**Web端点**：**/kache/details**：下为例子，参数为：

- 总命中次数：sumHit
- 总未命中次数：sumNotHit
- 命中service方法：
  - 命中的key名：
    - 命中次数：hit
    - 未命中次数：notHit

```json
{
    "com.kould.listener.impl.StatisticsListener": {
        "statisticMap": {
            "com.kould.service.impl.FounderTeamServiceImpl.findByFieldLike": {
                "key_set": [
                    "KACHE:NO_ID_selectPage7242089337059975123METHOD_SERVICE_BY_FIELDfindByFieldLikecom.kould.po.FounderTeamPO",
                    "KACHE:NO_ID_selectPage4902055363543254289METHOD_SERVICE_BY_FIELDfindByFieldLikecom.kould.po.FounderTeamPO"
                ],
                "hit": 1500,
                "notHit": 4
            },
            "com.kould.service.impl.FounderTeamServiceImpl.findById": {
                "key_set": [
                    "KACHE:1427152189974417410",
                    "KACHE:1456098452778844161"
                ],
                "hit": 1511,
                "notHit": 2
            }
        },
        "sumHit": 3011,
        "sumNotHit": 6
    }
}
```

#### 架构 | framework

Java标准MVC架构如图：

![](https://www.hualigs.cn/image/6127d7294d06c.jpg)

Kache架构：

![Kache结构图](https://s3.bmp.ovh/imgs/2021/11/20a75b43c86cff1f.jpg)

**（溢出方框外表示允许拓展）**

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

##### 缓存结构：

![Kache缓存结构](https://s3.bmp.ovh/imgs/2021/11/4ec53d620c952a95.jpg)



#### 测试 | Test

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

​	开始：

​	![](https://s3.bmp.ovh/imgs/2021/11/e677bc3a4d56d171.png)

​	结束：

​	![](https://s3.bmp.ovh/imgs/2021/11/38b6dfd492b29a71.png)

###### Kache组：

- **图形结果**

![](https://s3.bmp.ovh/imgs/2021/11/712a28313bbf0615.png)

- **汇总报告**

![](https://s3.bmp.ovh/imgs/2021/11/123b087ffde32502.png)

**Arthas的dashboard检测情况：**

​	开始：

​	![](https://s3.bmp.ovh/imgs/2021/11/16e27216b18fd98b.png)

​	结束：

​	![](https://s3.bmp.ovh/imgs/2021/11/7abfcb1943f50df1.png)

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

###### 正在使用

- 安徽鼎信：安徽招采平台

###### 感谢名单

**感谢@z_k_y耐心的测试使用和Bug反馈**
