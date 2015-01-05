package com.famelive.common.slotmanagement

import com.famelive.common.command.notification.FetchReminderCommand
import com.famelive.common.command.notification.FetchReminderIntervalCommand
import com.famelive.common.command.notification.ReminderCommand
import com.famelive.common.command.slotmanagement.*
import com.famelive.common.command.usernamagement.SendInvitationCommand
import com.famelive.common.command.usernamagement.SendInvitationToFollowersCommand
import com.famelive.common.dto.notification.FetchReminderIntervalDto
import com.famelive.common.dto.notification.ReminderDto
import com.famelive.common.dto.slotmanagement.*
import com.famelive.common.enums.SystemPushNotification
import com.famelive.common.enums.slotmanagement.EventStatus
import com.famelive.common.exceptions.CommonException
import com.famelive.common.exceptions.slotmanagement.EventStreamInfoNotFoundException
import com.famelive.common.followermanagement.Follow
import com.famelive.common.notification.PushNotificationService
import com.famelive.common.slotManagement.ChannelSlot
import com.famelive.common.slotManagement.Slot
import com.famelive.common.slotManagement.rule.RuleEngine
import com.famelive.common.streamManagement.*
import com.famelive.common.user.Reminder
import com.famelive.common.user.User
import com.famelive.common.util.DateTimeUtil
import com.famelive.common.util.FameLiveUtil

import static com.famelive.common.util.PlaceHolder.EVENT_NAME
import static com.famelive.common.util.PlaceHolder.getPopulatedContent

class EventService {

    def userService
    def commonMailService
    RuleEngine createEventRuleEngine
    RuleEngine editEventRuleEngine
    RuleEngine checkSlotAvailabilityRuleEngine
    SlotService slotService
    CommonStreamManagementService commonStreamManagementService
    PushNotificationService pushNotificationService

    EventDto createEvent(EventCommand eventCommand) throws CommonException {
        eventCommand.validate()
        User user = userService.findUserById(eventCommand.id)
        createEventRuleEngine.applyRules(eventCommand)
        List<ChannelSlot> channelSlots = slotService.fetchAvailableChannelSlots(new Slot(startTime: eventCommand.startTime, endTime: DateTimeUtil.addMinutesToDate(eventCommand.startTime, (int) eventCommand.duration)))
        ChannelSlot recommendedChannelSlot = slotService.fetchRecommendedChannelSlot(channelSlots)
        BookedChannelSlot bookedChannelSlot = slotService.bookChannelSlot(recommendedChannelSlot)

        Event event = new Event(eventCommand, user, bookedChannelSlot).save(failOnError: true, flush: true)
        event.refresh()
        assignGenreToEvent(eventCommand?.genreIds, event)

        commonStreamManagementService.createEventStreamDetails(event)

        commonMailService.sendCreateEventMail(user)
        sendPushNotificationForEventCreation(event)
        addChannelToEvent(event)
        EventDto eventDto = EventDto.createCommonResponseDto(event)
        return eventDto
    }

    void assignGenreToEvent(List<Long> genreIds, Event event) {
        genreIds.each { Long id ->
            Genre genre = Genre.get(id)
            event.addToGenres(genre)
        }
    }

    void sendPushNotificationForEventCreation(Event event) {
        log.info('********sendPushNotificationForEventCreation*******')
        List<String> followerChannels = Follow.getChannelsForPerformer(event.user)
        pushNotificationService.sendDataToRabbidMQ(getPushMessageForEvent(getMessageForEventCreation(event), followerChannels))
    }

    void sendPushNotificationForEventCancel(Event event) {
        List<String> followerChannels = Follow.getChannelsForPerformer(event.user)
        pushNotificationService.sendDataToRabbidMQ(getPushMessageForEvent(getMessageForEventCancel(event), followerChannels))
    }

    void sendPushNotificationForEventEdit(Event event) {
        List<String> followerChannels = Follow.getChannelsForPerformer(event.user)
        pushNotificationService.sendDataToRabbidMQ(getPushMessageForEvent(getMessageForEventEdit(event), followerChannels))
    }

    String getMessageForEventCreation(Event event) {
        Map placeHoldersMap = [:]
        placeHoldersMap[EVENT_NAME] = event?.name
        String html = getPopulatedContent(placeHoldersMap, SystemPushNotification.CREATE_EVENT.message)
        return html
    }

    String getMessageForEventCancel(Event event) {
        Map placeHoldersMap = [:]
        placeHoldersMap[EVENT_NAME] = event?.name
        String html = getPopulatedContent(placeHoldersMap, SystemPushNotification.CANCEL_EVENT.message)
        return html
    }

    String getMessageForEventEdit(Event event) {
        Map placeHoldersMap = [:]
        placeHoldersMap[EVENT_NAME] = event?.name
        String html = getPopulatedContent(placeHoldersMap, SystemPushNotification.EDIT_EVENT.message)
        return html
    }

    Map getPushMessageForEvent(String message, List<String> channels) {
        Map eventPushMap = [:]
        eventPushMap.message = message
        eventPushMap.channels = channels
        return eventPushMap
    }

    EventDto editEvent(EditEventCommand eventCommand) throws CommonException {
        eventCommand.validate()
        userService.findUserById(eventCommand.id)
        editEventRuleEngine.applyRules(eventCommand)
        Event event = Event.get(eventCommand.eventId)
        updateEvent(event, eventCommand)
        event.refresh()
        removeOldGenre(event)
        assignGenreToEvent(eventCommand?.genreIds, event)
        EventDto eventDto = EventDto.createCommonResponseDto(event)
        sendPushNotificationForEventEdit(event)
        return eventDto
    }

    void removeOldGenre(Event event) {
        event?.genres?.clear()
    }

    void updateEvent(Event event, EditEventCommand eventCommand) {
        event.name = eventCommand.name
        event.description = eventCommand.description
        event.isFeatured = eventCommand.isFeatured
        if (eventCommand.startTime.compareTo(event.startTime) == 0 && eventCommand.endTime.compareTo(event.endTime) == 0) {
            event.save(failOnError: true, flush: true)
        } else {
            event.startTime = eventCommand.startTime
            event.endTime = DateTimeUtil.addMinutesToDate(eventCommand?.startTime, eventCommand.duration as Integer)
            event.duration = eventCommand.duration
            if (slotService.cancelBookedEventChannelSlot(event)) {
                List<ChannelSlot> channelSlots = slotService.fetchAvailableChannelSlots(new Slot(startTime: eventCommand.startTime, endTime: DateTimeUtil.addMinutesToDate(eventCommand.startTime, eventCommand.duration as Integer)))
                ChannelSlot recommendedChannelSlot = slotService.fetchRecommendedChannelSlot(channelSlots)
                BookedChannelSlot bookedChannelSlot = slotService.bookChannelSlot(recommendedChannelSlot)
                event.setBookedChannelSlot(bookedChannelSlot)
                event.save(failOnError: true, flush: true)
                event.refresh()
                commonStreamManagementService.updateEventStreamDetails(event)
            }
        }

    }

    EventImageDto saveEventImage(EventImageCommand eventImageCommand) throws CommonException {
        eventImageCommand.validate()
        userService.findUserById(eventImageCommand.id)
        Event event = Event.get(eventImageCommand?.eventId)
        saveEventImageName(event, eventImageCommand.imageName)
        EventImageDto eventImageDto = EventImageDto.createCommonResponseDto(event)
        return eventImageDto
    }

    void saveEventImageName(Event event, String imageName) {
        event.imageName = imageName
        event.save(flush: true, failOnError: true)
    }

    CancelEventDto cancelEvent(CancelEventCommand cancelEventCommand) throws CommonException {
        cancelEventCommand.validate()
        Event event = Event.get(cancelEventCommand.eventId)
        changeEventStatus(event, EventStatus.CANCELED)
        slotService.cancelBookedEventChannelSlot(event)
        commonStreamManagementService.cancelEventStreamDetails(event)
        commonMailService.sendCancelEventMail(event?.user)
        CancelEventDto cancelEventDto = CancelEventDto.createCommonResponseDto(event)
        sendPushNotificationForEventCancel(event)
        return cancelEventDto
    }

    void changeEventStatus(Event event, EventStatus eventStatus) {
        event.status = eventStatus
        event.save(flush: true, failOnError: true)
    }

    FetchEventsDto fetchAllEventsOfUser(FetchEventsCommand fetchEventsCommand) throws CommonException {
        fetchEventsCommand.validate()
        User user = userService.findUserById(fetchEventsCommand.id)
        List<Event> eventList = Event.findAllByUserAndStatusNotEqual(user, EventStatus.CANCELED, fetchEventsCommand?.properties)
        FetchEventsDto fetchEventsDto = FetchEventsDto.createCommonResponseDto(eventList)
        return fetchEventsDto
    }

    FetchEventsDto fetchAllActiveEvents(FetchEventsCommand fetchEventsCommand) throws CommonException {
        fetchEventsCommand.validate()
        def eventFilteredData = Event.filteredList(fetchEventsCommand)
        FetchEventsDto fetchEventsDto = populateFetchEventDto(fetchEventsCommand, eventFilteredData)
        return fetchEventsDto
    }

    FetchEventsDto fetchAllCancelEvents(FetchEventsCommand fetchEventsCommand) throws CommonException {
        fetchEventsCommand.validate()
        userService.findUserById(fetchEventsCommand.id)
        def eventFilteredData = Event.filteredCancelEventList(fetchEventsCommand)
        FetchEventsDto fetchEventsDto = populateFetchEventDto(fetchEventsCommand, eventFilteredData)
        return fetchEventsDto
    }

    FetchEventsDto populateFetchEventDto(FetchEventsCommand fetchEventsCommand, def eventFilteredData) {
        Integer eventCount = eventFilteredData?.count()
        List<Event> eventList = eventFilteredData.list(fetchEventsCommand?.properties) as List<Event>
        FetchEventsDto fetchEventsDto = FetchEventsDto.createCommonResponseDto(eventList, eventCount)
        return fetchEventsDto
    }

    EventDto fetchEvent(FetchEventCommand fetchEventCommand) throws CommonException {
        fetchEventCommand.validate()
        userService.findUserById(fetchEventCommand.id)
        Event event = Event.get(fetchEventCommand?.eventId)
        EventDto eventDetailDto = EventDto.createCommonResponseDto(event)
        return eventDetailDto
    }

    FetchEventsDto fetchAllEvents(EventListCommand eventListCommand) throws CommonException {
        eventListCommand.validate()
        def eventFilteredData = Event.filteredByStatus(eventListCommand)
        List<Event> eventList = eventFilteredData.list(eventListCommand?.properties) as List<Event>
        FetchEventsDto fetchEventsDto
        if (eventListCommand?.id) {
            User user = userService.findUserById(eventListCommand?.id)
            fetchEventsDto = FetchEventsDto.createCommonResponseDto(eventList, user)
        } else {
            fetchEventsDto = FetchEventsDto.createCommonResponseDto(eventList)
        }
        return fetchEventsDto
    }

    EventDetailsDto fetchEventDetails(EventDetailsCommand eventDetailsCommand) throws CommonException {
        eventDetailsCommand.validate()
        Event event = Event.get(eventDetailsCommand.eventID)
        EventStreamInfo eventStreamInfo = EventStreamInfo.findByEvent(event)
        if (!eventStreamInfo) {
            throw new EventStreamInfoNotFoundException()
        }
        Reminder reminder = null
        if (eventDetailsCommand?.id) {
            User user = userService.findUserById(eventDetailsCommand?.id)
            reminder = Reminder.findByEventAndUserAndIsActive(event, user, true)
        }
        EventDetailsDto eventDetailsDto = EventDetailsDto.createCommonResponseDto(eventStreamInfo, reminder)
        return eventDetailsDto
    }

    void checkSlotAvailability(CheckSlotAvailabilityCommand checkSlotAvailabilityCommand) throws CommonException {
        checkSlotAvailabilityCommand.validate()
        checkSlotAvailabilityRuleEngine.applyRules(checkSlotAvailabilityCommand)
    }

    String addChannelToEvent(Event event) {
        String randomChannel = FameLiveUtil.randomChannel
        event.channels.add(randomChannel)
        event.activeChannel = randomChannel
        event.save()
        return randomChannel
    }

    FetchReminderIntervalDto fetchReminderIntervals(FetchReminderIntervalCommand fetchReminderIntervalCommand) throws CommonException {
        FetchReminderIntervalDto fetchReminderIntervalDto = FetchReminderIntervalDto.createCommonResponseDto()
        return fetchReminderIntervalDto
    }

    ReminderDto reminder(ReminderCommand reminderCommand) throws CommonException {
        reminderCommand.validate()
        User user = userService.findUserById(reminderCommand?.id)
        Reminder reminder = saveReminder(reminderCommand, user)
        ReminderDto reminderDto = ReminderDto.createCommonResponseDto(reminder)
        return reminderDto
    }

    Reminder saveReminder(ReminderCommand reminderCommand, User user) {
        Reminder reminder = reminderCommand?.reminderId ? Reminder.get(reminderCommand?.reminderId) : new Reminder()
        reminder.user = user
        reminder.event = Event.get(reminderCommand?.eventId)
        reminder.reminderTime = reminderCommand?.reminderTime
        reminder.isActive = reminderCommand.isSet
        reminder.save(flush: true, failOnError: true)
    }

    ReminderDto fetchReminder(FetchReminderCommand fetchReminderCommand) throws CommonException {
        fetchReminderCommand.validate()
        User user = userService.findUserById(fetchReminderCommand?.id)
        Event event = Event.get(fetchReminderCommand?.eventId)
        Reminder reminder = Reminder.findByEventAndUserAndIsActive(event, user, true)
        ReminderDto reminderDto = ReminderDto.createCommonResponseDto(reminder)
        return reminderDto
    }

    SendInvitationDto sendInvitation(SendInvitationCommand sendInvatationCommand) throws CommonException {
        sendInvatationCommand.validate()
        commonMailService.sendInvitationMail(sendInvatationCommand.emailIds)
        SendInvitationDto sendInvitationDto = SendInvitationDto.createCommonResponseDto()
        return sendInvitationDto
    }

    SendInvitationToFollowersDto sendInvitationToFollowers(SendInvitationToFollowersCommand sendInvitationToFollowersCommand) throws CommonException {
        sendInvitationToFollowersCommand.validate()
        List<String> followersEmailIds = Follow.getFollowersEmailIds(sendInvitationToFollowersCommand.id)
        commonMailService.sendInvitationToFollowersMail(followersEmailIds)
        SendInvitationToFollowersDto sendInvitationToFollowersDto = SendInvitationToFollowersDto.createCommonResponseDto()
        return sendInvitationToFollowersDto
    }

    EventUrlsDto fetchEventUrls(FetchEventUrlsCommand fetchEventUrlsCommand) {
        fetchEventUrlsCommand.validate()
        Event event = Event.get(fetchEventUrlsCommand?.eventID)
        EventStreamInfo eventStreamInfo = EventStreamInfo.findByEvent(event)
        WowzaChannel wowzaChannel = eventStreamInfo?.wowzaChannel
        WowzaChannel linkedVODChannel = wowzaChannel?.linkedVODChannel
        List<EventUrlDetailDto> eventUrlDetailDtoList = populateEventUrlDetailDtoList(event, wowzaChannel, linkedVODChannel)
        EventUrlsDto eventUrlsDto = EventUrlsDto.createCommonResponseDto(eventUrlDetailDtoList)
        return eventUrlsDto
    }

    List<EventUrlDetailDto> populateEventUrlDetailDtoList(Event event, WowzaChannel wowzaChannel, WowzaChannel linkedVODChannel) {
        List<EventUrlDetailDto> eventUrlDetailDtoList = []
        switch (event.status) {
            case EventStatus.ON_AIR:
                eventUrlDetailDtoList << new EventUrlDetailDto(resolution: '0', url: "http://" + wowzaChannel.fetchLiveStreamPath() + "/playlist.m3u8")
                Set<Integer> resolutions = EventStreamResolutions.findByEvent(event)?.resolutions
                resolutions.each { resolution ->
                    eventUrlDetailDtoList << new EventUrlDetailDto(resolution: "$resolution", url: "http://" + wowzaChannel.fetchLiveStreamPath() + "_" + resolution + "p/playlist.m3u8")
                }
                break
            case EventStatus.UP_COMING:
                EventUpcomingMediaContent eventUpcomingMediaContent = EventUpcomingMediaContent.findByEvent(event)
                eventUrlDetailDtoList = populateEventVODStreamingUrl(linkedVODChannel, eventUpcomingMediaContent)
                break
            case EventStatus.READY:
                EventReadyMediaContent eventReadyMediaContent = EventReadyMediaContent.findByEvent(event)
                eventUrlDetailDtoList = populateEventVODStreamingUrl(linkedVODChannel, eventReadyMediaContent)
                break
            case EventStatus.COMPLETED:
                EventCompletedMediaContent eventCompleteMediaContent = EventCompletedMediaContent.findByEvent(event)
                eventUrlDetailDtoList = populateEventVODStreamingUrl(linkedVODChannel, eventCompleteMediaContent)
        }
        return eventUrlDetailDtoList
    }

    List<EventUrlDetailDto> populateEventVODStreamingUrl(WowzaChannel linkedVODChannel, EventMediaContent eventMediaContent) {
        List<EventUrlDetailDto> eventUrlDetailDtoList = []
        if (linkedVODChannel != null && linkedVODChannel.isVODChannel && linkedVODChannel.isActive) {
            eventUrlDetailDtoList << new EventUrlDetailDto(resolution: '0', url: "http://" + linkedVODChannel?.fetchStreamPath() + "/" + eventMediaContent?.videoPath + ((eventMediaContent?.videoExtension != null && eventMediaContent?.videoExtension?.length() > 0) ? ("." + eventMediaContent?.videoExtension) : "") + "/playlist.m3u8")
            Set<Integer> eventCompleteStreamResolutions = eventMediaContent.videoResolutions
            eventCompleteStreamResolutions.each { resolution ->
                eventUrlDetailDtoList << new EventUrlDetailDto(resolution: resolution, url: "http://" + linkedVODChannel?.fetchStreamPath() + "/" + eventMediaContent?.videoPath + "_" + resolution + "p" + ((eventMediaContent?.videoExtension != null && eventMediaContent?.videoExtension?.length() > 0) ? ("." + eventMediaContent?.videoExtension) : "") + "/playlist.m3u8")
            }
        }
        return eventUrlDetailDtoList
    }

    FetchMostPopularUsersDto fetchMostPopularUsers(FetchMostPopularUsersCommand fetchMostPopularUsersCommand) throws CommonException {
        fetchMostPopularUsersCommand.validate()
        def filteredPerformer = Follow.filteredPerformer(fetchMostPopularUsersCommand)
        List<User> performerList = filteredPerformer.list(fetchMostPopularUsersCommand.properties).collect { it[0] }
        FetchMostPopularUsersDto fetchMostPopularUsersDto = FetchMostPopularUsersDto.createCommonResponseDto(performerList)
        return fetchMostPopularUsersDto
    }
}
