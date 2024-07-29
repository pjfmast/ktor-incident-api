package avans.avd.repositories

import avans.avd.models.User

interface UserRepository<ID>: CrudRepository<User, ID> {
    suspend fun findByUsername(username: String): User?
}