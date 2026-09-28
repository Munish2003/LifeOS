import re
from typing import Dict, Any


# Curated nutritional database for common and Indian foods (deterministic fallback and benchmark)
NUTRITION_DB = {
    "roti": {"calories": 110, "protein": 3.0, "carbs": 22.0, "fat": 0.5, "fiber": 2.5, "unit": "piece"},
    "chapati": {"calories": 110, "protein": 3.0, "carbs": 22.0, "fat": 0.5, "fiber": 2.5, "unit": "piece"},
    "paneer": {"calories": 265, "protein": 18.0, "carbs": 3.5, "fat": 20.0, "fiber": 0.0, "unit": "100g / bowl"},
    "dal": {"calories": 150, "protein": 9.0, "carbs": 24.0, "fat": 2.5, "fiber": 4.0, "unit": "bowl"},
    "rice": {"calories": 130, "protein": 2.7, "carbs": 28.0, "fat": 0.3, "fiber": 0.4, "unit": "100g / bowl"},
    "egg": {"calories": 75, "protein": 6.5, "carbs": 0.6, "fat": 5.0, "fiber": 0.0, "unit": "piece"},
    "boiled egg": {"calories": 75, "protein": 6.5, "carbs": 0.6, "fat": 5.0, "fiber": 0.0, "unit": "piece"},
    "sandwich": {"calories": 250, "protein": 8.0, "carbs": 35.0, "fat": 9.0, "fiber": 3.0, "unit": "piece"},
    "paneer sandwich": {"calories": 320, "protein": 14.0, "carbs": 36.0, "fat": 13.0, "fiber": 3.2, "unit": "piece"},
    "apple": {"calories": 95, "protein": 0.5, "carbs": 25.0, "fat": 0.3, "fiber": 4.4, "unit": "piece"},
    "banana": {"calories": 105, "protein": 1.3, "carbs": 27.0, "fat": 0.3, "fiber": 3.1, "unit": "piece"},
    "milk": {"calories": 150, "protein": 8.0, "carbs": 12.0, "fat": 8.0, "fiber": 0.0, "unit": "glass"},
    "curd": {"calories": 98, "protein": 3.5, "carbs": 4.7, "fat": 4.0, "fiber": 0.0, "unit": "bowl"},
    "dosa": {"calories": 168, "protein": 3.9, "carbs": 29.0, "fat": 3.7, "fiber": 1.2, "unit": "piece"},
    "idli": {"calories": 58, "protein": 2.0, "carbs": 12.0, "fat": 0.2, "fiber": 0.5, "unit": "piece"},
    "oats": {"calories": 160, "protein": 5.5, "carbs": 28.0, "fat": 3.0, "fiber": 4.0, "unit": "bowl"},
    "salad": {"calories": 70, "protein": 2.0, "carbs": 12.0, "fat": 1.0, "fiber": 3.5, "unit": "bowl"},
    "chicken breast": {"calories": 165, "protein": 31.0, "carbs": 0.0, "fat": 3.6, "fiber": 0.0, "unit": "100g"}
}


class FoodParser:
    """
    Parses natural language food descriptions into estimated calories and macros.
    Combines rule-based matching with AI estimation.
    """

    @classmethod
    def parse_text(cls, query: str) -> Dict[str, Any]:
        q_lower = query.lower().strip()

        # Extract quantities like "2 rotis", "1 bowl dal", "3 eggs"
        total_calories = 0.0
        total_protein = 0.0
        total_carbs = 0.0
        total_fat = 0.0
        total_fiber = 0.0
        matched_items = []

        words = q_lower.split()

        for food_key, info in NUTRITION_DB.items():
            pattern = rf"(\d+)?\s*(?:pieces?|bowls?|cups?|glasses|plates?)?\s*{re.escape(food_key)}"
            match = re.search(pattern, q_lower)
            if match or food_key in q_lower:
                qty = 1.0
                if match and match.group(1):
                    try:
                        qty = float(match.group(1))
                    except ValueError:
                        qty = 1.0
                elif any(w.isdigit() for w in words):
                    for w in words:
                        if w.isdigit():
                            qty = float(w)
                            break

                cal = info["calories"] * qty
                prot = info["protein"] * qty
                carbs = info["carbs"] * qty
                fat = info["fat"] * qty
                fib = info["fiber"] * qty

                total_calories += cal
                total_protein += prot
                total_carbs += carbs
                total_fat += fat
                total_fiber += fib
                matched_items.append(f"{int(qty) if qty.is_integer() else qty} {food_key}")

        if matched_items:
            return {
                "food_name": ", ".join(matched_items).title(),
                "portion_desc": query,
                "calories": round(total_calories, 1),
                "protein_g": round(total_protein, 1),
                "carbs_g": round(total_carbs, 1),
                "fat_g": round(total_fat, 1),
                "fiber_g": round(total_fiber, 1),
                "confidence": 0.90,
                "is_estimate": True,
                "explanation": f"Calculated based on standard verified portion sizes for: {', '.join(matched_items)}."
            }

        # Fallback heuristic for unknown food
        return {
            "food_name": query.strip().title(),
            "portion_desc": query,
            "calories": 250.0,
            "protein_g": 8.0,
            "carbs_g": 32.0,
            "fat_g": 9.0,
            "fiber_g": 2.0,
            "confidence": 0.60,
            "is_estimate": True,
            "explanation": "Standard meal portion estimate. You can adjust the exact numbers before saving."
        }
