package avans.avd.exceptions

import avans.avd.users.Role

class MissingRoleException(expectedRoles: List<Role> = emptyList()) : IllegalAccessException("Missing role(s): ${expectedRoles.joinToString { it.name }}")