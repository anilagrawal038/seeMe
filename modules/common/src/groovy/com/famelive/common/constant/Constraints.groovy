package com.famelive.common.constant

class Constraints {
    //User Management
    static final Integer USERNAME_MAX_SIZE = 40;
    static final Integer FAMENAME_MAX_SIZE = 15;
    static final String INDIA_COUNTRY_CODE = 'IN';
    static final Integer INDIA_MOBILE_NUMBER_SIZE = 10;
    static final Integer WORLD_MOBILE_NUMBER_SIZE = 15;
    static final Integer PASSWORD_MIN_SIZE = 6;
    static final Integer PASSWORD_MAX_SIZE = 15;
    static final Integer BIO_MAX_SIZE = 100;
    static final String MOBILE_NUMBER_PATTERN = /^\d*$/;
    static final String PASSWORD_PATTERN = /^(?=.*\d)(?=.*[a-z])(?!.*\s).{6,10}$/;
    static
    final String EMAIL_PATTERN = /[a-z0-9!#$%&'*+=?^_{|}~-]+(?:.[a-z0-9!#$%&'*+=?^_{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;

    //Slot Management
    static final Integer EVENT_NAME_MAX_SIZE = 50;
    static final Integer EVENT_DESCRIPTION_MAX_SIZE = 200;

}
