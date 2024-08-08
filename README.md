# Ktor Incidents api

This project is a demo for a Ktor application.
(Intended as a first step for full-stack Android app using this Ktor api)
### Ktor application

The Ktor application structure is grouped by the following features:

| module                       | description                                                                                                                                                                                                                                                                                       |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [UsersModule](users)         | A user may register and keep informed about report incidents<br/> qualified users may change the status of an incident                                                                                                                                                                            |
| [AuthModule](users)          | a registered user can login to report and manage incidents                                                                                                                                                                                                                                        |
| [IncidentsModule](incidents) | Incidents can be reported anonymously or by a registered user<br/>An incident has location, a status and can be documented with images of the incident<br/>reported incidents can be managed and when an incident report is deleted the related uploaded images of the incident are also deleted. |
