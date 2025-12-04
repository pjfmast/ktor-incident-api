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
        // Newly added realistic local users (will get ids 7, 8 and 9 respectively)
        create(User(
            username = "Bram",
            password = "pwd",
            email = "bram@ginneken.nl",
            role = Role.USER
        ))
        create(User(
            username = "Fatima",
            password = "pwd",
            email = "fatima@princenhage.nl",
            role = Role.USER
        ))
        create(User(
            username = "Lotte",
            password = "pwd",
            email = "lotte@wierickerschans.nl",
            role = Role.USER
        ))
    }

    // UserRepository-specific functionality
    override suspend fun findByUsername(username: String): User? = items.find { it.username == username }
}