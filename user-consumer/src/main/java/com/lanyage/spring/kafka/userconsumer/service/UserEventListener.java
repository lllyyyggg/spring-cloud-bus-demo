package com.lanyage.spring.kafka.userconsumer.service;

import com.lanyage.spring.kafka.userconsumer.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserEventListener implements ApplicationListener<UserEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @PostConstruct
    public void dosomething() {
        logger.info("==> do something <==");
    }
    @Override
    public void onApplicationEvent(UserEvent userEvent) {
        logger.debug("==> 收到用户事件{} <==", userEvent);
        //todo 具体业务代码
    }
}
