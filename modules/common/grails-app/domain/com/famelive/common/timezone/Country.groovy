package com.famelive.common.timezone

class Country {

    String code
    String name
    String telephoneCode

    static constraints = {
        code nullable: false
        name nullable: false
        telephoneCode nullable: false
    }
}
