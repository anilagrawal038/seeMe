import javax.naming.Context
import javax.naming.InitialContext

class UrlMappings {

    static mappings = {

        "/api/$id?"(parseRequest: true) {
            controller = 'api'
            action = 'index'
        }/*

        "/api/$controller/$action" {}*/

        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "500"(view: '/error')

        "/" {
            controller = 'streamingAdmin'
            action = 'index'
            /*String module = System.getProperty('buildModule')
//            println System.getProperty('user.timezone')
            try {
                if (module == null) {
                    module = (String) ((Context) new InitialContext().lookup("java:comp/env")).lookup("buildModule")
                }
            } catch (Exception e) {
                println 'unknown buildModule exp in UrlMappings :' + e
                println 'setting stream as default module for welcome page'
                module="stream"
            }
            if (module.equals("stream")) {
                println(module + ' welome page=> /streamingAdmin/index')
                controller = 'streamingAdmin'
                action = 'index'
            } else if (module.equals("admin")) {
                println(module + ' welome page=> /admin/index')
                controller = 'admin'
                action = 'index'
            } else {
                println(module + ' welome page=> /index.gsp')
                "/index.gsp"
            }*/
        }
    }
}
