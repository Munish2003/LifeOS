from datetime import datetime
from typing import List, Dict, Any, Optional
from app.engine.availability_engine import AvailabilityEngine, minutes_to_time_str, time_to_minutes, parse_time_str
from app.engine.priority_engine import PriorityEngine


class DailyPlanner:
    """
    Core Deterministic Intelligence Engine for Personal AI Life OS.
    Answers:
    'Given my schedule, routines, available time, goals, current progress,
     and remaining tasks, what can I realistically accomplish today, and what should I do next?'
    """

    @classmethod
    def generate_plan(
        cls,
        wake_time: str,
        sleep_time: str,
        routine_blocks: List[Dict[str, Any]],
        calendar_events: List[Dict[str, Any]],
        tasks: List[Dict[str, Any]],
        current_steps: int = 0,
        target_steps: int = 10000,
        current_time_str: Optional[str] = None
    ) -> Dict[str, Any]:
        avail_engine = AvailabilityEngine(wake_time=wake_time, sleep_time=sleep_time)
        capacity = avail_engine.calculate_day_capacity(
            routine_blocks=routine_blocks,
            calendar_events=calendar_events,
            current_time_str=current_time_str
        )

        available_minutes = capacity["available_focused_minutes"]
        free_slots = capacity["free_slots"]

        # Calculate required time for active tasks
        required_task_minutes = 0
        active_tasks = []
        for t in tasks:
            if not t.get("is_completed", False):
                active_tasks.append(t)
                if t.get("measurement_type") == "time_based":
                    rem = max(0, int(t.get("target_value", 0)) - int(t.get("current_value", 0)))
                    required_task_minutes += rem
                else:
                    # Allocate default estimation if non-time based (e.g. 30 mins)
                    required_task_minutes += 30

        # Steps step check
        steps_remaining = max(0, target_steps - current_steps)
        steps_behind = steps_remaining > 3000

        # Rank tasks by priority
        ranked_tasks = PriorityEngine.rank_tasks(
            tasks=active_tasks,
            available_minutes=available_minutes,
            steps_behind=steps_behind
        )

        difference_minutes = available_minutes - required_task_minutes
        is_feasible = difference_minutes >= 0

        conflicts = []
        suggested_adjustments = []

        if not is_feasible:
            shortage = abs(difference_minutes)
            conflicts.append(
                f"You have {len(active_tasks)} pending tasks requiring {required_task_minutes // 60}h {required_task_minutes % 60}m, "
                f"but only {available_minutes // 60}h {available_minutes % 60}m of realistic focused time remaining today. "
                f"Shortage: {shortage} minutes."
            )

            # Suggest adjustments: find lowest priority flexible tasks to move
            flexible_candidates = [
                t for t in reversed(ranked_tasks)
                if t.get("is_flexible", True)
            ]
            trimmed_minutes = 0
            for cand in flexible_candidates:
                cand_rem = max(0, int(cand.get("target_value", 0)) - int(cand.get("current_value", 0)))
                if cand_rem > 0:
                    suggested_adjustments.append(
                        f"Move or postpone '{cand.get('title')}' ({cand_rem}m) to tomorrow/weekend."
                    )
                    trimmed_minutes += cand_rem
                    if trimmed_minutes >= shortage:
                        break

            if not suggested_adjustments:
                suggested_adjustments.append(
                    f"Trim {shortage} minutes proportionally across planned tasks or reduce routine duration."
                )

        # Build timeline blocks by allocating tasks into free slots
        timeline = []
        # First add committed routine and calendar blocks
        for b in capacity["blocked_intervals"]:
            timeline.append({
                "title": b["name"],
                "category": b["category"],
                "start_time": b["start_time"],
                "end_time": b["end_time"],
                "duration_minutes": b["duration_minutes"],
                "is_completed": False,
                "task_id": None
            })

        # Now fill free slots with ranked tasks
        task_idx = 0
        for slot in free_slots:
            slot_curr_m = slot["start_minutes"]
            slot_end_m = slot["end_minutes"]

            while slot_curr_m < slot_end_m and task_idx < len(ranked_tasks):
                curr_task = ranked_tasks[task_idx]
                task_rem = max(0, int(curr_task.get("target_value", 0)) - int(curr_task.get("current_value", 0)))
                if task_rem <= 0:
                    task_idx += 1
                    continue

                slot_avail = slot_end_m - slot_curr_m
                duration_to_schedule = min(slot_avail, task_rem)

                if duration_to_schedule >= 15:  # Minimum 15-minute slot chunk
                    block_end = slot_curr_m + duration_to_schedule
                    timeline.append({
                        "title": curr_task["title"],
                        "category": curr_task.get("category", "learning"),
                        "start_time": minutes_to_time_str(slot_curr_m),
                        "end_time": minutes_to_time_str(block_end),
                        "duration_minutes": duration_to_schedule,
                        "is_completed": curr_task.get("is_completed", False),
                        "task_id": curr_task.get("id")
                    })
                    slot_curr_m = block_end

                task_idx += 1

        timeline.sort(key=lambda x: x["start_time"])

        # Determine "What Should I Do Now?"
        recommended_action = cls._calculate_next_action(
            free_slots=free_slots,
            ranked_tasks=ranked_tasks,
            calendar_events=calendar_events,
            steps_remaining=steps_remaining,
            current_time_str=current_time_str or wake_time
        )

        return {
            "evaluation_time": current_time_str or wake_time,
            "wake_time": wake_time,
            "sleep_time": sleep_time,
            "total_day_minutes": capacity["total_awake_minutes"],
            "routine_committed_minutes": capacity["committed_minutes"],
            "available_focused_minutes": available_minutes,
            "required_task_minutes": required_task_minutes,
            "difference_minutes": difference_minutes,
            "is_feasible": is_feasible,
            "conflicts": conflicts,
            "suggested_adjustments": suggested_adjustments,
            "recommended_next_action": recommended_action,
            "timeline": timeline
        }

    @classmethod
    def _calculate_next_action(
        cls,
        free_slots: List[Dict[str, Any]],
        ranked_tasks: List[Dict[str, Any]],
        calendar_events: List[Dict[str, Any]],
        steps_remaining: int,
        current_time_str: str
    ) -> Optional[Dict[str, Any]]:
        curr_m = time_to_minutes(parse_time_str(current_time_str))

        # Check if an upcoming calendar event is imminent (< 20 minutes)
        for evt in calendar_events:
            s_str = evt["start_time"][-5:] if "T" in evt["start_time"] else evt["start_time"]
            evt_s_m = time_to_minutes(parse_time_str(s_str))
            if 0 <= (evt_s_m - curr_m) <= 20:
                return {
                    "task_id": None,
                    "title": f"Prepare for {evt.get('title', 'Upcoming Event')}",
                    "duration_minutes": evt_s_m - curr_m,
                    "category": "work",
                    "action_type": "break",
                    "reason": f"Don't start deep work yet. You have '{evt.get('title')}' scheduled in {evt_s_m - curr_m} minutes."
                }

        # If behind on steps and currently in evening (> 17:00)
        if steps_remaining > 2500 and curr_m >= time_to_minutes(parse_time_str("17:00")):
            return {
                "task_id": None,
                "title": "Brisk Walk / Steps",
                "duration_minutes": 30,
                "category": "health",
                "action_type": "step_walk",
                "reason": f"You still have {steps_remaining:,} steps remaining for today's target. A 30-minute walk fits well now."
            }

        # Pick highest-priority incomplete task
        for t in ranked_tasks:
            t_rem = max(0, int(t.get("target_value", 0)) - int(t.get("current_value", 0)))
            if t_rem > 0:
                # Find current or next free slot duration
                next_slot_duration = 45
                if free_slots:
                    next_slot_duration = free_slots[0]["duration_minutes"]

                action_duration = min(next_slot_duration, t_rem)
                return {
                    "task_id": t.get("id"),
                    "title": t.get("title"),
                    "duration_minutes": action_duration,
                    "category": t.get("category", "learning"),
                    "action_type": "task_timer",
                    "reason": (
                        f"You have an available block ({next_slot_duration}m). "
                        f"'{t.get('title')}' has {t_rem}m remaining today. {t.get('priority_reason', '')}"
                    )
                }

        return {
            "task_id": None,
            "title": "Rest & Recharge",
            "duration_minutes": 30,
            "category": "personal",
            "action_type": "break",
            "reason": "All planned focus sessions and primary targets for today are successfully completed! 🎉"
        }
