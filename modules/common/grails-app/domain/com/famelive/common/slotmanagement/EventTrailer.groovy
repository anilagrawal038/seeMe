package com.famelive.common.slotmanagement

import com.famelive.common.cms.MediaContent

class EventTrailer {
    Event event

    static constraints = {
        event unique: true
    }
    static hasMany = [trailers: MediaContent]
}
