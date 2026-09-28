from app.engine.availability_engine import AvailabilityEngine
from app.engine.priority_engine import PriorityEngine
from app.engine.daily_planner import DailyPlanner
from app.ai.food_parser import FoodParser


def test_availability_calculation():
    engine = AvailabilityEngine(wake_time="07:00", sleep_time="23:30", min_focus_block_minutes=20)
    routine_blocks = [
        {"name": "Getting Ready", "category": "routine", "start_time": "07:30", "end_time": "08:15"},
        {"name": "Office", "category": "work", "start_time": "09:30", "end_time": "18:00"},
        {"name": "Commute", "category": "commute", "start_time": "18:00", "end_time": "19:00"},
        {"name": "Dinner", "category": "meal", "start_time": "19:30", "end_time": "20:15"},
        {"name": "Pooja", "category": "spiritual", "start_time": "20:15", "end_time": "20:35"},
    ]
    calendar_events = []

    res = engine.calculate_day_capacity(
        routine_blocks=routine_blocks,
        calendar_events=calendar_events,
        current_time_str="19:00"
    )

    # From 19:00 to 23:30 is 4h 30m = 270m.
    # Blocked: Dinner (45m) + Pooja (20m) = 65m.
    # Free slot before dinner: 19:00 - 19:30 (30m)
    # Free slot after pooja: 20:35 - 23:30 (175m)
    # Total available: 30 + 175 = 205m (~3h 25m)
    assert res["available_focused_minutes"] > 150
    assert len(res["free_slots"]) >= 1


def test_conflict_detection_and_adjustment():
    tasks = [
        {"id": 1, "title": "Python", "category": "learning", "measurement_type": "time_based", "target_value": 180, "current_value": 0, "is_flexible": False, "priority": 1},
        {"id": 2, "title": "DSA", "category": "learning", "measurement_type": "time_based", "target_value": 120, "current_value": 0, "is_flexible": True, "priority": 2},
    ]
    routine_blocks = [
        {"name": "Office", "category": "work", "start_time": "09:00", "end_time": "18:00"},
        {"name": "Dinner", "category": "meal", "start_time": "19:30", "end_time": "20:30"},
    ]

    # At 20:30, sleep at 23:00 -> Only 150 minutes available, but tasks need 300 minutes!
    plan = DailyPlanner.generate_plan(
        wake_time="07:00",
        sleep_time="23:00",
        routine_blocks=routine_blocks,
        calendar_events=[],
        tasks=tasks,
        current_time_str="20:30"
    )

    assert plan["is_feasible"] is False
    assert len(plan["conflicts"]) > 0
    assert "Shortage" in plan["conflicts"][0]
    # Flexible task DSA should be suggested for adjustment
    assert any("DSA" in adj for adj in plan["suggested_adjustments"])


def test_food_parser_estimation():
    res = FoodParser.parse_text("2 rotis and paneer")
    assert res["calories"] > 300
    assert res["protein_g"] > 10
    assert "Roti" in res["food_name"] or "Paneer" in res["food_name"]
