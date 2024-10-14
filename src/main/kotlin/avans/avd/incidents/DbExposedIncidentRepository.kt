package avans.avd.incidents

import avans.avd.IncidentDAO
import avans.avd.Incidents
import avans.avd.daoToIncident
import avans.avd.suspendTransaction

class DbExposedIncidentRepository : IncidentRepository<Long> {
    override suspend fun findIncidentsForUser(userID: Long): List<Incident> = suspendTransaction {
        IncidentDAO
            .find { Incidents.reportedBy eq userID }
            .map(::daoToIncident)
    }

    override suspend fun changeStatus(incident: Incident, status: Status): Incident = suspendTransaction {
        TODO("Not yet implemented")
    }

    override suspend fun addImage(id: Long, imageFileName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): List<Incident> {
        TODO("Not yet implemented")
    }

    override suspend fun save(entity: Incident): Incident {
        TODO("Not yet implemented")
    }

    override suspend fun saveAll(entities: Iterable<Incident>): List<Incident> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Long): Incident? {
        TODO("Not yet implemented")
    }
}
