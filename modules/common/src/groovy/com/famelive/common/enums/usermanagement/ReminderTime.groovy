package com.famelive.common.enums.usermanagement

public enum ReminderTime {

    START_BROADCAST("When broadcast starts"),
    THIRTY_MINUTES("30 mins in advance"),
    ONE_HOUR("1 hour in advance"),
    ON_EVENT_DAY("On the day of the event")

    String value;

    ReminderTime(String value) {
        this.value = value;
    }

}
