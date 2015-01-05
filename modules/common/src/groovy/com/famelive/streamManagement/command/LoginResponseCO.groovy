package com.famelive.streamManagement.command


import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by anil on 27/10/14.
 */
class LoginResponseCO extends ResponseCO {

    String access_token
    long userId
    boolean isAccountVerified
    String email
    String roles

    JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("success", success)
        jsonObject.put("message", message)
        jsonObject.put('status', status)
        JSONObject data = new JSONObject()
        data.put('email', email)
        data.put('userId', userId)
        data.put('isAccountVerified', isAccountVerified)
        data.put('roles', roles)
        data.put('access_token', access_token)
        jsonObject.put('data', data)
        return jsonObject
    }
}
