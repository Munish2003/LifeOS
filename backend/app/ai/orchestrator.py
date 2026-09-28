import re
from datetime import datetime, date, timedelta
from typing import Dict, Any, Optional
import httpx
from app.core.config import settings
from app.ai.food_parser import FoodParser


class AIOrchestrator:
    """
    Handles Level 3 AI interactions:
    - Natural Language Command interpretation
    - Validated structured output generation
    - Safe integration with Hugging Face LLM or deterministic fallback
    """

    @classmethod
    async def process_natural_language(cls, user_text: str) -> Dict[str, Any]:
        text = user_text.lower().strip()

        # 1. Check for Task Time / Study Session ("I studied Python for 1 hour / 45m / 90 mins")
        study_match = re.search(r"(?:studied|coded|worked on|practiced|did)\s+([a-zA-Z0-9_\s]+?)\s+for\s+(\d+(?:\.\d+)?)\s*(hours?|hrs?|h|minutes?|mins?|m)", text)
        if study_match:
            task_name = study_match.group(1).strip().title()
            val = float(study_match.group(2))
            unit = study_match.group(3)
            minutes = int(val * 60) if unit.startswith("h") else int(val)

            return {
                "action_type": "session_logged",
                "message": f"Recorded {minutes} minutes focus session for '{task_name}'.",
                "details": {
                    "task_title": task_name,
                    "duration_minutes": minutes,
                    "duration_seconds": minutes * 60,
                    "notes": user_text
                }
            }

        # 2. Check for Food Logging ("I ate ...", "had breakfast: ...", "lunch was ...")
        food_match = re.search(r"(?:i ate|had|ate|eating|food:?)\s+(.+)", text)
        if food_match or any(w in text for w in ["roti", "paneer", "dal", "rice", "sandwich", "lunch", "dinner", "breakfast"]):
            food_query = food_match.group(1) if food_match else text
            nutrition = FoodParser.parse_text(food_query)
            return {
                "action_type": "food_logged",
                "message": f"Estimated nutrition for '{nutrition['food_name']}': {nutrition['calories']} kcal.",
                "details": nutrition
            }

        # 3. Check for Outing / Availability block ("Tomorrow I have an outing for 4 hours")
        outing_match = re.search(r"(?:outing|trip|event|party|doctor appointment)\s*(?:for)?\s*(\d+(?:\.\d+)?)\s*(hours?|hrs?|h)", text)
        if outing_match or "outing" in text:
            hours = float(outing_match.group(1)) if outing_match else 3.0
            is_tomorrow = "tomorrow" in text
            target_date = (date.today() + timedelta(days=1)).isoformat() if is_tomorrow else date.today().isoformat()

            return {
                "action_type": "outing_created",
                "message": f"Scheduled {hours}h outing block for {target_date}. Day availability adjusted.",
                "details": {
                    "name": "Outing",
                    "category": "outing",
                    "date": target_date,
                    "start_time": "14:00",
                    "end_time": f"{14 + int(hours):02d}:00",
                    "duration_minutes": int(hours * 60),
                    "is_flexible": False
                }
            }

        # 4. Check for Recurring Task ("Make Python 3 hours daily")
        task_create_match = re.search(r"(?:make|add|create task)\s+([a-zA-Z0-9_\s]+?)\s+(\d+(?:\.\d+)?)\s*(hours?|hrs?|h|minutes?|mins?|m)\s*(?:daily)?", text)
        if task_create_match:
            title = task_create_match.group(1).strip().title()
            val = float(task_create_match.group(2))
            unit = task_create_match.group(3)
            minutes = int(val * 60) if unit.startswith("h") else int(val)

            return {
                "action_type": "task_created",
                "message": f"Created daily task '{title}' with target of {minutes} minutes.",
                "details": {
                    "title": title,
                    "category": "learning",
                    "measurement_type": "time_based",
                    "target_value": minutes,
                    "unit": "minutes",
                    "recurrence": "daily",
                    "is_flexible": True
                }
            }

        # 5. Fallback or Hugging Face Query
        if settings.HUGGINGFACE_API_KEY:
            hf_res = await cls._query_huggingface(user_text)
            if hf_res:
                return {
                    "action_type": "ai_insight",
                    "message": hf_res,
                    "details": {"query": user_text}
                }

        return {
            "action_type": "general_response",
            "message": f"Processed note: '{user_text}'. Tell me if you'd like to log a task, record a meal, or block outing hours.",
            "details": {"raw_text": user_text}
        }

    @classmethod
    async def _query_huggingface(cls, prompt: str) -> Optional[str]:
        try:
            url = f"https://api-inference.huggingface.co/models/{settings.AI_MODEL_NAME}"
            headers = {"Authorization": f"Bearer {settings.HUGGINGFACE_API_KEY}"}
            payload = {
                "inputs": f"You are a personal AI life planner assistant. Answer concisely and supportively:\n{prompt}",
                "parameters": {"max_new_tokens": 150}
            }
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(url, headers=headers, json=payload)
                if res.status_code == 200:
                    data = res.json()
                    if isinstance(data, list) and len(data) > 0:
                        return data[0].get("generated_text", "").strip()
        except Exception:
            pass
        return None
