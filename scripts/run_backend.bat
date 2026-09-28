@echo off
echo ========================================================
echo   Starting Personal AI Life OS Backend (FastAPI)
echo ========================================================
cd /d "%~dp0..\backend"

set PYTHONPATH=.
echo Installing dependencies if needed...
python -m pip install -r requirements.txt

echo Starting Server on http://0.0.0.0:8000 ...
echo Swagger UI documentation: http://localhost:8000/docs
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause
