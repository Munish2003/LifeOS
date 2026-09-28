package com.personal.lifeos.domain.engine

import com.personal.lifeos.data.local.entity.RoutineBlockEntity
import com.personal.lifeos.data.local.entity.TaskEntity
import kotlin.math.max
import kotlin.math.min

data class TimeSlot(
    val startMinutes: Int,
    val endMinutes: Int,
    val durationMinutes: Int
)

data class LocalAvailabilityResult(
    val availableFocusedMinutes: Int,
    val requiredTaskMinutes: Int,
    val differenceMinutes: Int,
    val isFeasible: Boolean,
    val conflictMessage: String?,
    val suggestedAdjustment: String?,
    val recommendedTask: TaskEntity?,
    val recommendedReason: String?
)

class LocalAvailabilityEngine(
    private val wakeMinutes: Int = 7 * 60, // 07:00
    private val sleepMinutes: Int = 23 * 60 + 30 // 23:30
) {

    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun calculate(
        blocks: List<RoutineBlockEntity>,
        tasks: List<TaskEntity>,
        currentMinutes: Int = 19 * 60 // 19:00 default
    ): LocalAvailabilityResult {
        val startCursor = max(wakeMinutes, currentMinutes)

        // Find blocked intervals
        val blockedRanges = blocks.map { b ->
            Pair(parseTimeToMinutes(b.startTime), parseTimeToMinutes(b.endTime))
        }.filter { it.second > startCursor && it.first < sleepMinutes }
            .sortedBy { it.first }

        // Find free slots
        var cursor = startCursor
        var availableMinutes = 0
        val freeSlots = mutableListOf<TimeSlot>()

        for (b in blockedRanges) {
            if (b.first > cursor) {
                val dur = min(b.first, sleepMinutes) - cursor
                if (dur >= 20) {
                    freeSlots.add(TimeSlot(cursor, min(b.first, sleepMinutes), dur))
                    availableMinutes += dur
                }
            }
            cursor = max(cursor, b.second)
            if (cursor >= sleepMinutes) break
        }
        if (cursor < sleepMinutes) {
            val dur = sleepMinutes - cursor
            if (dur >= 20) {
                freeSlots.add(TimeSlot(cursor, sleepMinutes, dur))
                availableMinutes += dur
            }
        }

        // Calculate required minutes
        var requiredMinutes = 0
        val pendingTasks = tasks.filter { !it.isCompleted }
        for (t in pendingTasks) {
            if (t.measurementType == "time_based") {
                requiredMinutes += max(0.0, t.targetValue - t.currentValue).toInt()
            } else {
                requiredMinutes += 25
            }
        }

        val diff = availableMinutes - requiredMinutes
        val isFeasible = diff >= 0

        var conflict: String? = null
        var adjustment: String? = null

        if (!isFeasible) {
            val shortage = kotlin.math.abs(diff)
            conflict = "You have ${pendingTasks.size} tasks requiring ${requiredMinutes}m, but only ${availableMinutes}m available tonight ($shortage minutes shortage)."
            val flexible = pendingTasks.lastOrNull { it.isFlexible }
            if (flexible != null) {
                adjustment = "Move flexible task '${flexible.title}' to tomorrow or weekend to restore balance."
            }
        }

        // Top recommended task
        val topTask = pendingTasks.firstOrNull()
        val reason = if (topTask != null) {
            val rem = (topTask.targetValue - topTask.currentValue).toInt()
            "You have free focus time. '${topTask.title}' has ${rem}m remaining today."
        } else {
            "All primary focus targets for today are completed! 🎉"
        }

        return LocalAvailabilityResult(
            availableFocusedMinutes = availableMinutes,
            requiredTaskMinutes = requiredMinutes,
            differenceMinutes = diff,
            isFeasible = isFeasible,
            conflictMessage = conflict,
            suggestedAdjustment = adjustment,
            recommendedTask = topTask,
            recommendedReason = reason
        )
    }
}
