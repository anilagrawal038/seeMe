package com.famelive.streamManagement.command

import javax.servlet.http.HttpServletRequest

/**
 * Created by anil on 27/10/14.
 */
class VerifyAccountRequestCO extends RequestCO {
    String email //= "admn.famelive@gmail.com"
    String verificationToken
    HttpServletRequest request
}
