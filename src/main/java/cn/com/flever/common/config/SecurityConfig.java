package cn.com.flever.common.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author 冯健
 * @date 2018/10/10 22:49
 * @descride 为了解决使用spring  security客户端无法连接服务端得问题，原因是spring security默认开启了csrf检验
 */
@EnableWebSecurity
public class SecurityConfig  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();
  }

}
