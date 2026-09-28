from typing import List, Dict, Any


class PriorityEngine:
    """
    Deterministic multi-factor task prioritization with transparent explanations.
    """

    CATEGORY_WEIGHTS = {
        "health": 1.3,     # Health & steps consistency is foundational
        "learning": 1.2,   # High-priority skill growth (e.g. Python, DSA)
        "work": 1.1,
        "personal": 1.0,
        "goals": 1.25,
        "custom": 1.0
    }

    @classmethod
    def calculate_score(
        cls,
        task: Dict[str, Any],
        available_minutes: int,
        steps_behind: bool = False
    ) -> Dict[str, Any]:
        """
        Calculates priority score and reason.
        Higher score = higher priority.
        """
        base_priority = task.get("priority", 3)  # 1 (Highest) to 5 (Lowest)
        # Invert priority so 1 gives highest base score (e.g. 5 - 1 = 4)
        score = (6 - base_priority) * 20.0
        reasons = []

        category = task.get("category", "personal").lower()
        cat_weight = cls.CATEGORY_WEIGHTS.get(category, 1.0)
        score *= cat_weight
        reasons.append(f"{category.capitalize()} category weight: {cat_weight}x")

        # Remaining work calculation
        target_val = float(task.get("target_value", 0.0))
        curr_val = float(task.get("current_value", 0.0))
        remaining_duration = max(0.0, target_val - curr_val)

        if remaining_duration > 0:
            if remaining_duration <= available_minutes:
                score += 15.0
                reasons.append(f"Fits fully within available free time ({int(remaining_duration)}m needed)")
            else:
                score -= 10.0
                reasons.append(f"Exceeds single free slot ({int(remaining_duration)}m > {available_minutes}m)")

        # Deadline urgency
        deadline = task.get("deadline")
        if deadline:
            score += 25.0
            reasons.append("Has upcoming deadline today")

        # Health/Step urgency
        if category == "health" and steps_behind:
            score += 20.0
            reasons.append("Health/step goal is behind daily target")

        # Flexibility penalty
        if not task.get("is_flexible", True):
            score += 15.0
            reasons.append("Marked non-negotiable / inflexible")

        return {
            "score": round(score, 2),
            "remaining_minutes": int(remaining_duration) if task.get("measurement_type") == "time_based" else None,
            "explanation": " • ".join(reasons)
        }

    @classmethod
    def rank_tasks(
        cls,
        tasks: List[Dict[str, Any]],
        available_minutes: int,
        steps_behind: bool = False
    ) -> List[Dict[str, Any]]:
        """
        Ranks tasks in descending order of calculated score.
        """
        scored_tasks = []
        for t in tasks:
            if t.get("is_completed", False):
                continue
            res = cls.calculate_score(t, available_minutes, steps_behind)
            scored = dict(t)
            scored["priority_score"] = res["score"]
            scored["priority_reason"] = res["explanation"]
            scored_tasks.append(scored)

        scored_tasks.sort(key=lambda x: x["priority_score"], reverse=True)
        return scored_tasks
