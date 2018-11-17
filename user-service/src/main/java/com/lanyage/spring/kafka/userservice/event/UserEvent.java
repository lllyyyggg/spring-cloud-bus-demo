package com.lanyage.spring.kafka.userservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

public class UserEvent extends RemoteApplicationEvent {

    private static Logger logger = LoggerFactory.getLogger(UserEvent.class);
    public static final String ET_UPDATE="USER_UPDATE";
    public static final String ET_DELETE="USER_DELETE";

    private String action;
    private String uniqueKey;

    public UserEvent() {
        super();
    }

    public UserEvent(Object source, String originService, String destinationService, String action, String id) {
        super(source, originService, destinationService);
        this.action = action;
        this.uniqueKey = id;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "action='" + action + '\'' +
                ", uniqueKey='" + uniqueKey + '\'' +
                '}';
    }
}
