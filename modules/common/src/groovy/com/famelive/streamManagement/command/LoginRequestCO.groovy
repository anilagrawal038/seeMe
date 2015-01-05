package com.famelive.streamManagement.command

import com.famelive.common.enums.RequestType
import com.famelive.streamManagement.api.command.APIRequestCO

import javax.servlet.http.HttpServletRequest

/**
 * Created by anil on 27/10/14.
 */
class LoginRequestCO extends RequestCO {

    String email //= "admn.famelive@gmail.com"
    String password //= "admin"
    HttpServletRequest request


}
