package cn.com.flever.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Redis 配置类
 *
 * @author Leon
 * @version 2018/6/17 17:46
 */
@Configuration
// 必须加，使配置生效
@EnableCaching
@EnableRedisHttpSession
public class RedisConfiguration extends CachingConfigurerSupport {

  /**
   * Logger
   */
  private static final Logger lg = LoggerFactory.getLogger(RedisConfiguration.class);


  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    //  设置自动key的生成规则，配置spring boot的注解，进行方法级别的缓存
    // 使用：进行分割，可以很多显示出层级关系
    // 这里其实就是new了一个KeyGenerator对象，只是这是lambda表达式的写法，我感觉很好用，大家感兴趣可以去了解下
    return (target, method, params) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(target.getClass().getName());
      sb.append(":");
      sb.append(method.getName());
      for (Object obj : params) {
        sb.append(":" + String.valueOf(obj));
      }
      String rsToUse = String.valueOf(sb);
      lg.info("自动生成Redis Key -> [{}]", rsToUse);
      return rsToUse;
    };
  }


  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
    StringRedisTemplate template = new StringRedisTemplate(factory);
    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(
        Object.class);
    ObjectMapper om = new ObjectMapper();
    om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(om);
    template.setValueSerializer(jackson2JsonRedisSerializer);
    template.afterPropertiesSet();
    return template;
  }

  @Override
  @Bean
  public CacheErrorHandler errorHandler() {
    // 异常处理，当Redis发生异常时，打印日志，但是程序正常走
    lg.info("初始化 -> [{}]", "Redis CacheErrorHandler");
    CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        lg.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
      }

      @Override
      public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        lg.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
      }

      @Override
      public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        lg.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
      }

      @Override
      public void handleCacheClearError(RuntimeException e, Cache cache) {
        lg.error("Redis occur handleCacheClearError：", e);
      }
    };
    return cacheErrorHandler;
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofDays(1))//缓存1天
        .disableCachingNullValues()
        .computePrefixWith(cacheName -> "miaosha1".concat(":").concat(cacheName).concat(":"))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new
            GenericJackson2JsonRedisSerializer()));
    // 设置一个初始化的缓存空间set集合
    Set<String> cacheNames = new HashSet<>();
    cacheNames.add("my-redis-cache1");
    cacheNames.add("user");

    // 对每个缓存空间应用不同的配置
    Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
    configMap.put("my-redis-cache1", cacheConfiguration);
    configMap.put("user", cacheConfiguration.entryTtl(Duration.ofSeconds(120)));
    RedisCacheManager cacheManager = RedisCacheManager
        .builder(factory)     // 使用自定义的缓存配置初始化一个cacheManager
        .initialCacheNames(cacheNames)  // 注意这两句的调用顺序，一定要先调用该方法设置初始化的缓存名，再初始化相关的配置
        .withInitialCacheConfigurations(configMap)
        .build();
    return cacheManager;
  }


}
