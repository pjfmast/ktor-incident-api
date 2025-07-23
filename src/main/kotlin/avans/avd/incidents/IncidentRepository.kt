package avans.avd.incidents

import avans.avd.core.CrudRepository

// https://www.reddit.com/r/androiddev/comments/127excb/concrete_implementation_vs_interface_naming/
interface IncidentRepository<ID>: CrudRepository<Incident, ID> {
    suspend fun findIncidentsForUser(userID: ID): List<Incident>
    suspend fun findIncidentsInBoundingBox(latMin: Double, latMax: Double, lngMin: Double, lngMax: Double): List<Incident>
}