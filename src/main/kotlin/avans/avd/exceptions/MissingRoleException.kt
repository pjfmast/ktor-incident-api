package avans.avd.exceptions

import avans.avd.users.Role

class MissingRoleException(expectedRoles: List<Role> = emptyList())
    : IllegalAccessException("Only authorized for role(s) or user who created this incident: ${expectedRoles.joinToString { it.name }}")