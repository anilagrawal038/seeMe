package com.famelive.common.slotmanagement

import com.amazonaws.services.s3.model.CannedAccessControlList
import com.famelive.common.command.slotmanagement.EventCommand
import com.famelive.common.command.slotmanagement.EventListCommand
import com.famelive.common.command.slotmanagement.FetchEventsCommand
import com.famelive.common.constant.CommonConstants
import com.famelive.common.enums.slotmanagement.EventStatus
import com.famelive.common.enums.usermanagement.EventLocation
import com.famelive.common.user.User
import com.famelive.common.util.DateTimeUtil

class Event {
    String eventId
    String name
    String description
    Date startTime
    Date endTime
    Long duration = CommonConstants.DEFAULT_EVENT_DURATION
    String imageName
    EventStatus status = EventStatus.UP_COMING
    User user
    BookedChannelSlot bookedChannelSlot
    boolean isHistory = false
    String activeChannel
    Boolean isFeatured = false

    static hasMany = [genres: Genre, channels: String]

    static constraints = {
        eventId nullable: true
        name nullable: false, maxSize: 50
        description nullable: true, validator: { val, obj ->
            if (val) {
                if (val.length() > 200) {
                    return ['event.description.maxSize.error']
                }
            }
        }
        startTime nullable: false
        bookedChannelSlot nullable: true
        imageName nullable: true
        user nullable: true
        activeChannel nullable: true
    }

    static mapping = {
        eventId formula: "CONCAT('E0000000',id)"
    }

    static namedQueries = {
        filteredList { FetchEventsCommand fetchEventsCommand ->
            not {
                eq('status', EventStatus.CANCELED)
            }
            if (fetchEventsCommand.memberName) {
                'user' {
                    ilike("username", "%${fetchEventsCommand.memberName}%")
                }
            }
            if (fetchEventsCommand.email) {
                'user' {
                    ilike("email", "%${fetchEventsCommand.email}%")
                }
            }
            if (fetchEventsCommand.startDateTime) {
                between("startTime", fetchEventsCommand.startDateTime, fetchEventsCommand.endDateTime)
            }
            if (fetchEventsCommand?.genreId) {
                'genres' {
                    eq('id', fetchEventsCommand?.genreId)
                }
            }
            if (fetchEventsCommand?.locationName && fetchEventsCommand.eventLocation == EventLocation.COUNTRY) {
                'user' {
                    'countryCode' {
                        ilike("name", "%${fetchEventsCommand.locationName}%")
                    }
                }
            }
            if (fetchEventsCommand?.eventTitle) {
                ilike("name", "%${fetchEventsCommand.eventTitle}%")
            }
        }
        filteredCancelEventList { FetchEventsCommand fetchEventsCommand ->
            eq('status', EventStatus.CANCELED)
        }
        filteredByStatus { EventListCommand eventListCommand ->
            switch (eventListCommand?.eventStatus) {
                case EventStatus.UP_COMING:
                    if (eventListCommand?.isFeatured) {
                        eq('isFeatured', true)
                    }
                    gt("startTime", DateTimeUtil.currentDate)
                    eq('status', EventStatus.UP_COMING)
                    break
                case EventStatus.ON_AIR:
                    lte("startTime", DateTimeUtil.currentDate)
                    gte("endTime", DateTimeUtil.currentDate)
                    not {
                        eq('status', EventStatus.CANCELED)
                    }
                    break
                case EventStatus.CANCELED:
                    eq('status', EventStatus.CANCELED)
                    break
                case EventStatus.COMPLETED:
                    eq('status', EventStatus.COMPLETED)
                    break
            }
        }
        currentLiveEvents {
            lte("startTime", DateTimeUtil.currentDate)
            gt("endTime", DateTimeUtil.currentDate)
        }
    }

    Event() {}

    Event(EventCommand eventCommand, User user, BookedChannelSlot bookedChannelSlot) {
        this.user = user
        this.name = eventCommand.name
        this.description = eventCommand.description
        this.startTime = eventCommand.startTime
        this.endTime = DateTimeUtil.addMinutesToDate(eventCommand?.startTime, eventCommand.duration as Integer)
        this.duration = eventCommand.duration
        this.bookedChannelSlot = bookedChannelSlot
        this.isFeatured = eventCommand?.isFeatured
    }

    static Integer getTotalEventsBetweenDuration(Date startTime, Date endTime, User user = null) {
        createCriteria().count() {
            'in'('status', [EventStatus.ON_AIR, EventStatus.UP_COMING])
            if (user) {
                eq("user.id", user?.id)
            }
            or {
                and {
                    gte('startTime', startTime)
                    lte('startTime', endTime)
                }
                and {
                    gte('endTime', startTime)
                    lte('endTime', endTime)
                }
                and {
                    gte('startTime', startTime)
                    lte('endTime', endTime)
                }
                and {
                    lte('startTime', startTime)
                    gte('endTime', endTime)
                }
            }
        }
    }

    public void setStartTime(Date startTime) {
        this.startTime = DateTimeUtil.roundOffMinuteInDate(startTime)
    }

    public void setEndTime(Date endTime) {
        this.endTime = DateTimeUtil.roundOffMinuteInDate(endTime)
    }

    public String fetchTrailerPath() {
        return 'user/' + this?.user?.fameId + '/event/' + this?.eventId + '/trailer'
    }

    public String fetchTrailerThumbnailPath() {
        return 'user/' + this?.user?.fameId + '/event/' + this?.eventId + '/trailerThumbnail'
    }

    public String fetchTrailerName(int trailerIndex) {
        return this?.eventId + '_trailer_' + trailerIndex + '.mp4'
    }

    public String fetchTrailerThumbnailName(int trailerIndex) {
        return this?.eventId + '_trailer_' + trailerIndex + '_thumbnail.png'
    }

    public static String fetchTrailerAccessPermission() {
        return CannedAccessControlList.BucketOwnerFullControl
    }

    public static String fetchTrailerThumbnailAccessPermission() {
        return CannedAccessControlList.PublicRead
    }

    public static List<String> getEventChannels(Long eventId) {
        List<String> eventChannels = get(eventId).channels as List<String>
        return eventChannels
    }
}
