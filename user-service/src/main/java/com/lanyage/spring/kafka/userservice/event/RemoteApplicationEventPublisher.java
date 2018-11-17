package com.lanyage.spring.kafka.userservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.context.ApplicationContext;

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
