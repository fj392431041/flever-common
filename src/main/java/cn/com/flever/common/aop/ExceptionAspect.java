package cn.com.flever.common.aop;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author 冯健
 * @date 2019/2/14 23:18
 * @descride
 */
@Aspect
public class ExceptionAspect {


  // 匹配cn.com.flever包下所有类的、
  // 所有方法的执行作为切入点
  @AfterThrowing(throwing = "ex", pointcut = "execution(* cn.com.flever..*.*(..))")
  // 声明ex时指定的类型会限制目标方法必须抛出指定类型的异常
  // 此处将ex的类型声明为Throwable，意味着对目标方法抛出的异常不加限制
  public void doRecoveryActions(Throwable ex) {
  /*  log.error("系统发生异常：", ex);*/
  }
}
