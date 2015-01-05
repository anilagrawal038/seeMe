package com.famelive.streamManagement.command

import javax.servlet.http.HttpServletRequest

/**
 * Created by anil on 27/10/14.
 */
class ChangePasswordRequestCO extends RequestCO {
    String email //= "admn.famelive@gmail.com"
    String oldPassword
    String newPassword
    String confirmPassword
    HttpServletRequest request
}
