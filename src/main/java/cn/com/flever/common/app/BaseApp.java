package cn.com.flever.common.app;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient  // 在注册中心发现服务
@SpringBootApplication(scanBasePackages = "cn.com.flever")
@MapperScan("cn.com.flever.*.service.mapper")
@EnableAutoConfiguration
public class BaseApp {

}
