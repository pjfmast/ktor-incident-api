package avans.avd.users

import avans.avd.CrudRepository

interface UserRepository<ID>: CrudRepository<User, ID> {
    suspend fun findByUsername(username: String): User?
}