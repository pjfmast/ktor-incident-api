package avans.avd

interface CrudRepository<T, ID> {
    suspend fun findAll(): List<T>
    suspend fun findById(id: ID): T?
    suspend fun save(entity: T): T
    suspend fun saveAll(entities: Iterable<T>): List<T>
    suspend fun delete(entity: T): Boolean
}