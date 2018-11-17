package com.lanyage.spring.kafka.userservice.service;

import com.lanyage.spring.kafka.userservice.domain.User;
import com.lanyage.spring.kafka.userservice.event.ApplicationContextHolder;
import com.lanyage.spring.kafka.userservice.event.RemoteApplicationEventPublisher;
import com.lanyage.spring.kafka.userservice.event.UserEvent;
import com.lanyage.spring.kafka.userservice.msg.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private List<User> users;


    @Autowired
    private ApplicationContextHolder holder;

    public UserService() {
        this.users = this.create();
    }

    public List<User> findAll() {
        return this.users;
    }

    public User save(User userDTO) {
        for (User user : this.users) {
            if (user.getId() == userDTO.getId()) {
                user.setName(userDTO.getName());
                break;
            }
        }
        this.users.add(userDTO);

        this.fireEvent(UserEvent.ET_UPDATE, userDTO);   //这是关键，保存用户之后，就发布更新事件

        return userDTO;
    }

    private void fireEvent(String eventAction, User user) {
        logger.info("==> context:{} <==",holder.getApplicationContext().getId());
        UserEvent userEvent = new UserEvent(user,   //源对象
                holder.getApplicationContext().getId(), //上下文ID
                "*:**", //将消息发往所有服务
                eventAction,    //事件
                String.valueOf(user.getId()));  //源对象的唯一标识符
        RemoteApplicationEventPublisher.publishEvent(userEvent,holder);
    }

    private List<User> create() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "兰亚戈"));
        users.add(new User(2, "戴梦晓"));
        return users;
    }
}
