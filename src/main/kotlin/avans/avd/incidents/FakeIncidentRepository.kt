package avans.avd.incidents

import avans.avd.core.BaseInMemoryRepository

object FakeIncidentRepository : BaseInMemoryRepository<Incident>(), IncidentRepository<Long> {
    override val items = mutableListOf<Incident>()
    override var currentId: Long = 0L
    private var imageId: Long = 0L

    override fun copyWithNewId(entity: Incident, id: Long): Incident = entity.copy(id = id)
    override fun getId(entity: Incident): Long = entity.id

    // Seed the fake repository with some fake data
    init {
        create(
            Incident(
                3, /*Anne*/
                Category.TRAFFIC,
                "Sink hole here. Dangerous situation! Quick fix needed.",
                51.58677130730741,
                4.808487370673,
                Priority.HIGH,
                Status.ASSIGNED,
            )
        )
        create(
            Incident(
                2 /*Henk*/,
                Category.COMMUNAL,
                "At this corner 2 lights are broken",
                51.59051650746655,
                4.812002566502519,
                Priority.NORMAL
            )
        )
        create(
            Incident(
                2 /*Henk*/,
                Category.COMMUNAL,
                "noise disturbance from illegal party",
                51.58218477578439,
                4.835727885428926,
                Priority.NORMAL
            )
        )
        create(
            Incident(
                3 /*Anne*/,
                Category.ENVIRONMENT,
                "Some xtc lab dumped chemicals. ",
                51.58907773104348,
                4.80552621192238,
                Priority.HIGH
            )
        )
    }
    // IncidentRepository-specific functionality:
    override suspend fun findIncidentsForUser(userID: Long): List<Incident> =
        items.filter { it.reportedBy == userID }

    override suspend fun findIncidentsInBoundingBox(
        latMin: Double,
        latMax: Double,
        lngMin: Double,
        lngMax: Double
    ): List<Incident> = items.filter { it.isCoordinateInArea(latMin, latMax, lngMin, lngMax) }

}