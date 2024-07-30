package avans.avd.incidents

import avans.avd.CrudRepository

// https://www.reddit.com/r/androiddev/comments/127excb/concrete_implementation_vs_interface_naming/
interface IncidentRepository<ID>: CrudRepository<Incident, ID> {
    suspend fun findIncidentsForUser(userID: ID): List<Incident>
    suspend fun changeStatus(incident: Incident, status: Status): Incident
}