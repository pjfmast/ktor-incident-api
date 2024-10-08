package avans.avd.users

object FakeUserRepository : UserRepository<Long> {
    private var userId: Long = User.NEW_USER_ID
    private val users = mutableListOf<User>()

    // Seed the fake repository with some dummy data
    init {
        create(User("admin", "password", "admin@avans.nl", role = Role.ADMIN))
        create(User("Henk", "pwd", "henk@avans.nl", role = Role.USER))
        create(User("Sophie", "pwd", "sophie@breda.nl", role = Role.OFFICIAL))
    }


    override suspend fun findByUsername(username: String): User? = users.find { it.username == username }

    override suspend fun findAll(): List<User> = users.toList()

    override suspend fun findById(id: Long): User? = users.find { it.id == id }

    override suspend fun save(entity: User): User {
        return if (users.any { it.id == entity.id }) {
            update(entity)
        } else {
            create(entity)
        }
    }

    override suspend fun saveAll(entities: Iterable<User>): List<User> = entities.map { save(it) }

    private fun create(user: User): User {
        userId++
        val newUser = user.copy(id = userId)
        users.add(newUser)
        return newUser
    }

    override suspend fun delete(id: Long): Boolean = users.removeIf { it.id == id }

    private fun update(user: User): User {
        check(user.id > 0) { "Id must be greater than 0" }
        require(users.any { it.id == user.id })
        users.removeIf { it.id == user.id }
        users.add(user)
        return user
    }
}