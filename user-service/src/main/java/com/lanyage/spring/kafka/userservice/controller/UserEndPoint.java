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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    public List<User> users(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin","http://localhost:3000");
        logger.info("==> {} method has been invoked <==", "findAll");
        return userService.findAll();
    }

    @DeleteMapping(value = "/delete/{id}")
    public String delete(@PathVariable("id") String id,HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin","http://localhost:3000");
        return id;
    }
}
