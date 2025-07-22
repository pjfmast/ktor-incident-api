package avans.avd.users

import avans.avd.core.BaseInMemoryRepository

object FakeUserRepository : BaseInMemoryRepository<User>(), UserRepository<Long> {
    override val items = mutableListOf<User>()
    override var currentId: Long = User.NEW_USER_ID
    
    override fun copyWithNewId(entity: User, id: Long): User = entity.copy(id = id)
    override fun getId(entity: User): Long = entity.id

    // Seed the fake repository with some fake data
    init {
        create(User(
            username = "admin",
            password = "password",
            email = "admin@avans.nl",
            role = Role.ADMIN
        ))
        create(User(
            username = "Henk",
            password = "pwd",
            email = "henk@heusdenhout.nl",
            role = Role.USER
        ))
        create(User(
            username = "Anne",
            password = "pwd",
            email = "anne@brabantpark.nl",
            role = Role.USER
        ))
        create(User(
            username = "Kees",
            password = "pwd",
            email = "kees@boeimeer.nl",
            role = Role.USER
        ))
        create(User(
            username = "Sophie",
            password = "pwd",
            email = "sophie@gemeentebreda.nl",
            role = Role.OFFICIAL
        ))
        create(User(
            username = "Ron",
            password = "pwd",
            email = "ron@gemeentebreda.nl",
            role = Role.OFFICIAL
        ))
    }

    // UserRepository-specific functionality
    override suspend fun findByUsername(username: String): User? = items.find { it.username == username }
}