package com.leanpro.leanprover2

import java.io.Serializable
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.toDuration

class Calculation(_name: String): Serializable {
    val name: String = _name
    var settings: Settings = Settings()
    var zones: MutableMap<Int, Zone> = mutableMapOf()
    var NUP: NonUserPreferences = NonUserPreferences()
    var humans: MutableMap<String, Human> = mutableMapOf()

    class Settings: Serializable{
        var workingDays: Double = 0.0
        var yearPlan: Double = 0.0
        var dayPlan: Double = 0.0
        var period: Duration = 1.toDuration(DurationUnit.HOURS)
        var periodCount: Int = 0
        var shutdown: Duration = 0.toDuration(DurationUnit.DAYS)
        var tactTime: Duration = 0.toDuration(DurationUnit.SECONDS)
        var reglamentedBreaks = 0.toDuration(DurationUnit.MINUTES)
        var nonReglamentedBreaks = 0.toDuration(DurationUnit.MINUTES)
        var clearWorkingTime = 0.toDuration(DurationUnit.DAYS)
        var schedule: Schedule = Schedule()
    }

    class Schedule: Serializable{
        var workingDays: Double = 1.0
        var notWorkingDays: Double = 0.0
        var coefficient: Double = 1.0
    }

    inner class Zone: Serializable{
        var id: Int = 0
        var name: String = ""
        var operations: MutableMap<Int, Operation> = mutableMapOf()
        var humans: MutableMap<String, Human> = mutableMapOf()

        inner class Operation(): Serializable{
            var id: Int = 0
            var zoneId: Int = 0
            var name: String = ""
            var profession: String = ""
            var cycles: Int = 0
            var workersCount: Int = 0
            var currentTime: Duration = 0.toDuration(DurationUnit.SECONDS)
        }
    }

    class NonUserPreferences: Serializable{
        var operationsCount = 0
        var zonesCount = 0
        fun getOperationId(): Int{operationsCount += 1; return operationsCount}
        fun getZoneId(): Int{zonesCount += 1; return zonesCount}
        var lastEdit: LocalDate = LocalDate.now()
    }

    class Human: Serializable{
        var profession: String = ""
        var workingTimeForProduct: Duration = 0.toDuration(DurationUnit.SECONDS)
        var edited: Boolean = false
        var calculatedCount: Double = 0.0
        var actualCount: Int = 0
        var operationsIds = mutableListOf<List<Int>>()
    }

    fun calculate(){
        settings.schedule.coefficient =
            settings.schedule.workingDays / (settings.schedule.workingDays + settings.schedule.notWorkingDays)
        settings.workingDays =  ceil((365 - settings.shutdown.inWholeDays) * settings.schedule.coefficient)
        settings.clearWorkingTime = ((settings.period - settings.reglamentedBreaks - settings.nonReglamentedBreaks) * settings.periodCount) * settings.workingDays
        settings.tactTime = (settings.period - settings.reglamentedBreaks - settings.nonReglamentedBreaks) * settings.periodCount / settings.dayPlan
        humans.clear()

        for (human in humans.values) {
            human.workingTimeForProduct = 0.toDuration(DurationUnit.SECONDS)
        }
        for (zone in zones.values) {
            zone.humans.clear()

        }
        for (zone in zones.values) {
            for (operation in zone.operations.values) {
                if (zone.humans[operation.profession] == null) {
                    zone.humans[operation.profession] = Human()
                    zone.humans[operation.profession]!!.profession = operation.profession
                }
                zone.humans[operation.profession]!!.workingTimeForProduct += operation.currentTime * operation.cycles * operation.workersCount
                zone.humans[operation.profession]!!.operationsIds.add(
                    listOf(
                        zone.id,
                        operation.id
                    )
                )
                zone.humans[operation.profession]!!.calculatedCount =
                    zone.humans[operation.profession]!!.workingTimeForProduct / settings.tactTime

                if (humans[operation.profession] == null) {
                    humans[operation.profession] = Human()
                    humans[operation.profession]!!.profession = operation.profession
                }
                humans[operation.profession]!!.workingTimeForProduct += operation.currentTime * operation.cycles * operation.workersCount
                humans[operation.profession]!!.operationsIds.add(
                    listOf(
                        zone.id,
                        operation.id
                    )
                )
                humans[operation.profession]!!.calculatedCount =
                    humans[operation.profession]!!.workingTimeForProduct / settings.tactTime
            }
        }

    }

}
