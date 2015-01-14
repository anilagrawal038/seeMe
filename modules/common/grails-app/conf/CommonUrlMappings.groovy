class CommonUrlMappings {

    static mappings = {


        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }
        "500"(view: '/error')
    }
}
