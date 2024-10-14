package avans.avd

import avans.avd.incidents.Incident
import avans.avd.incidents.Priority
import avans.avd.incidents.Status
import avans.avd.users.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


object Incidents : LongIdTable("incidents") {
    val reportedBy: Column<EntityID<Long>?> = reference("reported_by", Users.id).nullable()
    val category: Column<String> = varchar("category", 255)
    val decription: Column<String> = varchar("description", 255)

    val latitude: Column<Double> = double("latitude")
    val longitude: Column<Double> = double("longitude")

    val priority: Column<Priority> = enumeration("priority", Priority::class)
    val status: Column<Status> = enumeration("status", Status::class)
    val images: Column<List<String>> = array<String>("images")

    val createdAt: Column<LocalDateTime> = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").defaultExpression(CurrentDateTime)
    val completedAt: Column<LocalDateTime> = datetime("completed_at")
}

class IncidentDAO(id: EntityID<Long>) : LongEntity(id) {
    var reportedBy by Incidents.reportedBy
    var category by Incidents.category
    var description by Incidents.decription
    var latitude by Incidents.latitude
    var longitude by Incidents.longitude
    var priority by Incidents.priority
    var status by Incidents.status
    var images by Incidents.images

    var createdAt by Incidents.createdAt
    var updatedAt by Incidents.updatedAt
    var completedAt by Incidents.completedAt

    companion object : LongEntityClass<IncidentDAO>(Incidents)
}

object Users : LongIdTable("users") {
    val username: Column<String> = varchar("username", 255).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val role: Column<Role> = enumeration("role", Role::class)
    val avatar: Column<String?> = varchar("avatar", 255).nullable()
}

class UserDAO(id: EntityID<Long>) : LongEntity(id) {
    var username by Users.username
    var password by Users.password
    var email by Users.email
    var role by Users.role
    var avatar by Users.avatar

    companion object : LongEntityClass<UserDAO>(Users)
}


suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToIncident(dao: IncidentDAO): Incident = Incident(
    dao.reportedBy?.value,
    dao.category,
    dao.description,
    dao.latitude,
    dao.longitude,
    dao.priority,
    dao.status,
    dao.images.toMutableList(),
    dao.createdAt,
    dao.updatedAt,
    dao.completedAt,
)