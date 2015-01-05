package com.famelive.common.cms

import com.famelive.common.enums.cms.MediaCategory
import com.famelive.common.enums.cms.MediaContentType
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Created by anil on 5/12/14.
 */
class MediaContent implements Serializable {
    String name
    String title
    String description
    MediaContentType mediaContentType
    MediaCategory mediaCategory
    MediaContent thumbnail

    boolean equals(other) {
        if (!(other instanceof MediaContent)) {
            return false
        }
        other.name == name && other.title == title && other.mediaCategory == mediaCategory
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append name
        builder.append title
        builder.append mediaCategory
        builder.toHashCode()
    }

    static mapping = { id composite: ['name', 'title', 'mediaCategory'] }

    static constraints = {
        description nullable: true
        thumbnail nullable: true
//        name nullable: true
//        title nullable: true
    }
}
