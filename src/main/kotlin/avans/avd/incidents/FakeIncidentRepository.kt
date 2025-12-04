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
        // Newly added realistic incidents around Breda (within ~3km radius of 51.5898, 4.7832)
        create(
            Incident(
                7 /*Bram*/,
                Category.COMMUNAL,
                "Loose paving stones causing trips near playground",
                51.5921,
                4.7789,
                Priority.NORMAL,
                Status.REPORTED
            )
        )
        create(
            Incident(
                8 /*Fatima*/,
                Category.TRAFFIC,
                "Pothole forming at intersection; cyclists swerving to avoid it",
                51.5853,
                4.7897,
                Priority.HIGH,
                Status.ASSIGNED
            )
        )
        create(
            Incident(
                9 /*Lotte*/,
                Category.ENVIRONMENT,
                "Illegal dumping: several garbage bags in the bushes",
                51.5945,
                4.7923,
                Priority.NORMAL,
                Status.REPORTED
            )
        )
        create(
            Incident(
                null /*anonymous*/,
                Category.CRIME,
                "Bike theft attempt reported by neighbors last night",
                51.5839,
                4.7752,
                Priority.LOW,
                Status.REPORTED
            )
        )
        create(
            Incident(
                null /*anonymous*/,
                Category.OTHER,
                "Graffiti on underpass wall freshly painted over but still visible",
                51.5884,
                4.8011,
                Priority.LOW,
                Status.RESOLVED
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