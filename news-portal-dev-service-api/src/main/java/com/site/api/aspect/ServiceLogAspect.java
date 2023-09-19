package com.site.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLogAspect {

    final static Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * AOP通知：
     * 1. 前置通知：在方法调用之前执行
     * 2. 后置通知：在方法正常调用之后执行
     * 3. 环绕通知：在方法调用之前和之后，都分别可以执行的通知
     * 4. 异常通知：如果在方法调用过程中发生异常，则通知
     * 5. 最终通知：在方法调用之后执行
     */

    /**
     * 切面表达式：
     * execution 代表所要执行的表达式主体
     * 第一处 * 代表方法返回类型 *代表所有类型
     * 第二处 包名代表aop监控的类所在的包
     * 第三处 .*. 代表匹配某一个包，因为我们命名的包都是有一定的规则规范的
     * 第四处 .. 代表该包以及其子包下的所有类方法
     * 第五处 * 代表类名，*代表所有类
     * 第六处 *(..) *代表类中的方法名，(..)表示方法中的任何参数
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */

    @Around("execution(* com.site.*.service.impl..*.*(..))")
    public Object recordTimeOfService(ProceedingJoinPoint joinPoint) throws Throwable {

        logger.info("====== Start of execution {}.{} ======",
                joinPoint.getTarget().getClass(),
                joinPoint.getSignature().getName());

        // Record start time
        long start = System.currentTimeMillis();

        // 执行目标 service
        Object result = joinPoint.proceed();

        // Record end time
        long end = System.currentTimeMillis();
        long takeTime = end - start;

        if (takeTime > 3000) {
            logger.error("====== End of execution. Duration：{} ms ======", takeTime);
        } else if (takeTime > 2000) {
            logger.warn("====== End of execution. Duration：{} ms ======", takeTime);
        } else {
            logger.info("====== End of execution. Duration：{} ms ======", takeTime);
        }

        return result;
    }

}
