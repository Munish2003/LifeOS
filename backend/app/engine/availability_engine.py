from datetime import datetime, time, timedelta
from typing import List, Tuple, Dict, Any


def parse_time_str(t_str: str) -> time:
    """Parses 'HH:MM' into datetime.time"""
    parts = t_str.strip().split(":")
    return time(hour=int(parts[0]), minute=int(parts[1]))


def time_to_minutes(t: time) -> int:
    """Converts a datetime.time to minutes since midnight"""
    return t.hour * 60 + t.minute


def minutes_to_time_str(m: int) -> str:
    """Converts minutes since midnight to 'HH:MM'"""
    m = m % (24 * 60)
    hours = m // 60
    minutes = m % 60
    return f"{hours:02d}:{minutes:02d}"


class TimeInterval:
    def __init__(self, start: int, end: int, label: str = "", category: str = "blocked"):
        self.start = start
        self.end = end
        self.label = label
        self.category = category

    @property
    def duration(self) -> int:
        return max(0, self.end - self.start)

    def overlaps_with(self, other: "TimeInterval") -> bool:
        return max(self.start, other.start) < min(self.end, other.end)


class AvailabilityEngine:
    """
    Deterministic calculation of available day time and usable focused time.
    Accounts for:
    - Wake & Sleep time
    - Office & Commute
    - Routine blocks (Bath, Pooja, Meals, etc.)
    - Calendar events
    - Custom commitments/outings
    """

    def __init__(
        self,
        wake_time: str = "07:00",
        sleep_time: str = "23:30",
        min_focus_block_minutes: int = 25
    ):
        self.wake_time_str = wake_time
        self.sleep_time_str = sleep_time
        self.wake_minutes = time_to_minutes(parse_time_str(wake_time))
        self.sleep_minutes = time_to_minutes(parse_time_str(sleep_time))
        self.min_focus_block_minutes = min_focus_block_minutes

    def calculate_day_capacity(
        self,
        routine_blocks: List[Dict[str, Any]],
        calendar_events: List[Dict[str, Any]],
        current_time_str: str = None
    ) -> Dict[str, Any]:
        """
        Calculates:
        1. Total day awake minutes
        2. Committed minutes (routines + events)
        3. Realistically available focused minutes
        4. Free usable time slots
        """
        if self.sleep_minutes > self.wake_minutes:
            total_awake_minutes = self.sleep_minutes - self.wake_minutes
        else:
            # Past midnight sleep
            total_awake_minutes = (24 * 60 - self.wake_minutes) + self.sleep_minutes

        start_eval_minutes = self.wake_minutes
        if current_time_str:
            curr_m = time_to_minutes(parse_time_str(current_time_str))
            start_eval_minutes = max(self.wake_minutes, curr_m)

        # Build list of blocked intervals
        blocked_intervals: List[TimeInterval] = []

        for block in routine_blocks:
            s_m = time_to_minutes(parse_time_str(block["start_time"]))
            e_m = time_to_minutes(parse_time_str(block["end_time"]))
            if e_m > s_m:
                blocked_intervals.append(TimeInterval(s_m, e_m, block["name"], block.get("category", "routine")))

        for evt in calendar_events:
            # evt: start_time and end_time (can be full ISO or HH:MM)
            s_str = evt["start_time"][-5:] if "T" in evt["start_time"] else evt["start_time"]
            e_str = evt["end_time"][-5:] if "T" in evt["end_time"] else evt["end_time"]
            s_m = time_to_minutes(parse_time_str(s_str))
            e_m = time_to_minutes(parse_time_str(e_str))
            if e_m > s_m:
                blocked_intervals.append(TimeInterval(s_m, e_m, evt.get("title", "Event"), "event"))

        # Merge overlapping blocked intervals
        blocked_intervals.sort(key=lambda x: x.start)
        merged_blocked: List[TimeInterval] = []
        for interval in blocked_intervals:
            if not merged_blocked:
                merged_blocked.append(interval)
            else:
                last = merged_blocked[-1]
                if interval.start <= last.end:
                    # Overlap or adjacent
                    merged_blocked[-1] = TimeInterval(
                        last.start,
                        max(last.end, interval.end),
                        f"{last.label} / {interval.label}",
                        "combined"
                    )
                else:
                    merged_blocked.append(interval)

        # Calculate free slots between start_eval_minutes and sleep_minutes
        free_slots: List[Dict[str, Any]] = []
        current_cursor = start_eval_minutes

        for blocked in merged_blocked:
            if blocked.end <= current_cursor:
                continue
            if blocked.start > current_cursor:
                slot_start = current_cursor
                slot_end = min(blocked.start, self.sleep_minutes)
                slot_duration = slot_end - slot_start
                if slot_duration >= self.min_focus_block_minutes:
                    free_slots.append({
                        "start_time": minutes_to_time_str(slot_start),
                        "end_time": minutes_to_time_str(slot_end),
                        "start_minutes": slot_start,
                        "end_minutes": slot_end,
                        "duration_minutes": slot_duration
                    })
            current_cursor = max(current_cursor, blocked.end)
            if current_cursor >= self.sleep_minutes:
                break

        if current_cursor < self.sleep_minutes:
            slot_duration = self.sleep_minutes - current_cursor
            if slot_duration >= self.min_focus_block_minutes:
                free_slots.append({
                    "start_time": minutes_to_time_str(current_cursor),
                    "end_time": minutes_to_time_str(self.sleep_minutes),
                    "start_minutes": current_cursor,
                    "end_minutes": self.sleep_minutes,
                    "duration_minutes": slot_duration
                })

        total_committed_minutes = sum(b.duration for b in merged_blocked if b.end >= start_eval_minutes and b.start <= self.sleep_minutes)
        total_usable_focused_minutes = sum(s["duration_minutes"] for s in free_slots)

        return {
            "wake_time": self.wake_time_str,
            "sleep_time": self.sleep_time_str,
            "evaluation_start_time": minutes_to_time_str(start_eval_minutes),
            "total_awake_minutes": total_awake_minutes,
            "committed_minutes": total_committed_minutes,
            "available_focused_minutes": total_usable_focused_minutes,
            "free_slots": free_slots,
            "blocked_intervals": [
                {
                    "name": b.label,
                    "category": b.category,
                    "start_time": minutes_to_time_str(b.start),
                    "end_time": minutes_to_time_str(b.end),
                    "duration_minutes": b.duration
                }
                for b in merged_blocked
            ]
        }
