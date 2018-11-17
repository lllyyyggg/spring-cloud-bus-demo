package com.lanyage.spring.kafka.userservice.controller;

import com.lanyage.spring.kafka.userservice.domain.User;
import com.lanyage.spring.kafka.userservice.event.ApplicationContextHolder;
import com.lanyage.spring.kafka.userservice.event.UserEvent;
import com.lanyage.spring.kafka.userservice.msg.UserMessage;
import com.lanyage.spring.kafka.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserEndPoint {

    private static final Logger logger = LoggerFactory.getLogger(UserEndPoint.class);

    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    @RequestMapping(value = "/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public User save(@RequestBody User user) {
        logger.info("==> {} method has been invoked <==", "saveOne");
        return userService.save(user);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<User> users() {
        logger.info("==> {} method has been invoked <==", "findAll");
        return userService.findAll();
    }
}
