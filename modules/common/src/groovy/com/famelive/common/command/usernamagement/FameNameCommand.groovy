package com.famelive.common.command.usernamagement

import com.famelive.common.command.RequestCommand
import com.famelive.common.constant.Constraints
import com.famelive.common.exceptions.FameNameMaxLengthException
import com.famelive.common.exceptions.UniqueEmailException
import com.famelive.common.exceptions.UniqueFameNameException
import com.famelive.common.user.User
import grails.validation.Validateable

@Validateable
class FameNameCommand extends RequestCommand {

    String fameName
    String email

    static constraints = {
        fameName nullable: true, validator: { val, obj ->
            if (val) {
                if (val.length() > Constraints.FAMENAME_MAX_SIZE) {
                    throw new FameNameMaxLengthException()
                } else if (User.countByFameName(val)) {
                    throw new UniqueFameNameException()
                }
            }
        }
        email nullable: true, validator: { val, obj ->
            if (val) {
                if (User.countByEmail(val)) {
                    throw new UniqueEmailException()
                }
            }
        }
    }
}
