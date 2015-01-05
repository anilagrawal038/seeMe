import com.famelive.common.CommonBootStrapService
import com.famelive.common.user.User
import com.famelive.common.util.CommonMessagesUtil
import com.famelive.common.util.FameLiveUtil

class CommonBootStrap {
    CommonBootStrapService commonBootStrapService
    def grailsApplication

    def init = { servletContext ->
        FameLiveUtil.grailsApplication = grailsApplication
        if (!CommonMessagesUtil.messageSource) {
            new CommonMessagesUtil().initializeMessageSource(grailsApplication)
        }

        int noOfUsers
        try {
            noOfUsers = User.list().size()
        } catch (Exception e) {
            noOfUsers = -1
        }
        if (noOfUsers < 1) {
            commonBootStrapService.bootstrapUserRoles()
            commonBootStrapService.bootstrapSuperAdmin()
            commonBootStrapService.bootstrapUsers()
//            commonBootStrapService.createGenre()
//            commonBootStrapService.createSocialAccountTemplate()
//            commonBootStrapService.createPushNotificationTemplate()
            commonBootStrapService.bootstrapWowzaChannels()
            commonBootStrapService.bootStrapStreamManagementConstants()
//            commonBootStrapService.bootStrapSlotManagementConstants()
//            commonBootStrapService.cleanAndReinitializeSlotManagement()
//            commonBootStrapService.bootStrapDummyEventData()
//            commonBootStrapService.createCountryEntry()
//            commonBootStrapService.createZoneEntry()
        }
    }

    def destroy = {
    }
}
