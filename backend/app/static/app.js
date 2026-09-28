// Life OS Mobile Simulator — Dark Edition Client Logic
document.addEventListener('DOMContentLoaded', () => {

  // --- STATE ---
  const state = {
    stepsCurrent: parseInt(localStorage.getItem('lifeos_steps') || '0', 10),
    stepsTarget: 10000,
    caloriesCurrent: parseFloat(localStorage.getItem('lifeos_cals') || '0'),
    caloriesTarget: 1200,
    focusMinutesCurrent: parseInt(localStorage.getItem('lifeos_focus_mins') || '0', 10),
    focusMinutesTarget: 180,
    isGoogleFitConnected: localStorage.getItem('lifeos_fit_connected') === 'true',
    lastSyncTime: localStorage.getItem('lifeos_last_sync') || '',
    tasks: JSON.parse(localStorage.getItem('lifeos_tasks') || 'null') || [
      { id: 1, title: 'Python Deep Work (FastAPI & Engine)', priority: 'P1', targetMins: 45, currentMins: 0, completed: false, category: 'Productivity' },
      { id: 2, title: 'Evening Walk & Step Target', priority: 'P2', targetMins: 30, currentMins: 0, completed: false, category: 'Health' },
      { id: 3, title: 'Review System Specs & Roadmap', priority: 'P3', targetMins: 20, currentMins: 0, completed: false, category: 'Learning' }
    ],
    foods: JSON.parse(localStorage.getItem('lifeos_foods') || 'null') || [],
    timer: {
      totalSeconds: 25 * 60,
      remainingSeconds: 25 * 60,
      isRunning: false,
      intervalId: null,
      taskTitle: 'Python Deep Work'
    }
  };

  // --- DOM ELEMENTS ---
  const statusTime = document.getElementById('status-time');
  const navItems = document.querySelectorAll('.nav-item');
  const tabs = document.querySelectorAll('.screen-tab');

  // Device Toggle
  const btnPhoneMode = document.getElementById('btn-phone-mode');
  const btnFullMode = document.getElementById('btn-full-mode');
  const phoneFrame = document.getElementById('phone-frame');
  const btnReplaySplash = document.getElementById('btn-replay-splash');

  // Splash Screen Elements
  const appSplashScreen = document.getElementById('app-splash-screen');
  const splashLoaderBar = document.getElementById('splash-loader-bar');
  const splashTelemetry = document.getElementById('splash-telemetry');

  // Calendar Matrix Elements
  const calPillBtns = document.querySelectorAll('.cal-pill-btn');
  const calPanelDaily = document.getElementById('cal-panel-daily');
  const calPanelWeekly = document.getElementById('cal-panel-weekly');
  const calPanelMonthly = document.getElementById('cal-panel-monthly');
  const ringStepsFill = document.getElementById('ring-steps-fill');
  const ringFocusFill = document.getElementById('ring-focus-fill');
  const ringDietFill = document.getElementById('ring-diet-fill');
  const ringTasksFill = document.getElementById('ring-tasks-fill');
  const habitStepsStatus = document.getElementById('habit-steps-status');
  const habitFocusStatus = document.getElementById('habit-focus-status');
  const habitDietStatus = document.getElementById('habit-diet-status');
  const habitTasksStatus = document.getElementById('habit-tasks-status');
  const weeklyDaysCircles = document.getElementById('weekly-days-circles');
  const monthGridCircles = document.getElementById('month-grid-circles');
  const btnPrevMonth = document.getElementById('btn-prev-month');
  const btnNextMonth = document.getElementById('btn-next-month');
  const calMonthTitle = document.getElementById('cal-month-title');

  // Notification Banner
  const notifBanner = document.getElementById('notif-banner');
  const btnEnableNotif = document.getElementById('btn-enable-notif');
  const btnDismissNotif = document.getElementById('btn-dismiss-notif');

  // Home Dashboard
  const homeOverallPct = document.getElementById('home-overall-pct');
  const homeAuraPct = document.getElementById('home-aura-pct');
  const homeProgressFill = document.getElementById('home-progress-fill');
  const homeStepsCurrent = document.getElementById('home-steps-current');
  const homeStepsBar = document.getElementById('home-steps-bar');
  const homeStepsTitle = document.getElementById('home-steps-title');
  const homeCaloriesCurrent = document.getElementById('home-calories-current');
  const homeCaloriesBar = document.getElementById('home-calories-bar');
  const homeTaskList = document.getElementById('home-task-list');
  const fitSyncIndicator = document.getElementById('fit-sync-indicator');
  const headerFitBadge = document.getElementById('header-fit-badge');
  const hourlyBarsContainer = document.getElementById('hourly-bars-container');

  // Google Fit & Health
  const fitStatusLabel = document.getElementById('fit-status-label');
  const fitToggleBtnText = document.getElementById('fit-toggle-btn-text');
  const btnSyncFitNow = document.getElementById('btn-sync-fit-now');
  const btnFitToggleConnect = document.getElementById('btn-fit-toggle-connect');
  const btnLaunchFit = document.getElementById('btn-launch-fit');
  const healthStepsNumber = document.getElementById('health-steps-number');
  const healthProgressFill = document.getElementById('health-progress-fill');
  const healthDistance = document.getElementById('health-distance');
  const healthCalories = document.getElementById('health-calories');
  const healthActiveTime = document.getElementById('health-active-time');
  const stepsCardBadge = document.getElementById('steps-card-badge');
  const btnOpenFitSetup = document.getElementById('btn-open-fit-setup');
  const weeklyTrendBars = document.getElementById('weekly-trend-bars');

  // Tasks Tab
  const tasksFullList = document.getElementById('tasks-full-list');
  const filterChips = document.querySelectorAll('.filter-chip');
  let currentFilter = 'all';

  // Timer Tab
  const timerCountdown = document.getElementById('timer-countdown');
  const timerStroke = document.getElementById('timer-stroke');
  const timerActiveTaskTitle = document.getElementById('timer-active-task-title');
  const btnTimerToggle = document.getElementById('btn-timer-toggle');
  const iconTimerPlay = document.getElementById('icon-timer-play');
  const iconTimerPause = document.getElementById('icon-timer-pause');
  const btnTimerReset = document.getElementById('btn-timer-reset');
  const btnTimerComplete = document.getElementById('btn-timer-complete');
  const presetBtns = document.querySelectorAll('.preset-btn');

  // Modals
  const modalFitSetup = document.getElementById('modal-fit-setup');
  const btnCloseFitModal = document.getElementById('btn-close-fit-modal');
  const btnModalGrantPerms = document.getElementById('btn-modal-grant-perms');

  const modalAddTask = document.getElementById('modal-add-task');
  const btnAddTaskModal = document.getElementById('btn-add-task-modal');
  const btnCloseTaskModal = document.getElementById('btn-close-task-modal');
  const btnSubmitTask = document.getElementById('btn-submit-task');

  const modalAiSettings = document.getElementById('modal-ai-settings');
  const btnOpenAiSettings = document.getElementById('btn-open-ai-settings');
  const btnCloseAiModal = document.getElementById('btn-close-ai-modal');
  const btnSaveAiConfig = document.getElementById('btn-save-ai-config');
  const hfTokenInput = document.getElementById('hf-token-input');

  const toast = document.getElementById('toast');

  // --- MODERN AESTHETIC SPLASH SCREEN INITIALIZATION ---
  function triggerSplashScreen() {
    if (!appSplashScreen) return;
    appSplashScreen.classList.remove('fade-out');
    if (splashLoaderBar) splashLoaderBar.style.width = '0%';
    if (splashTelemetry) splashTelemetry.textContent = 'Initializing neural core...';

    const steps = [
      { pct: 30, text: 'Connecting Google Fit & physical sensors...' },
      { pct: 65, text: 'Loading habits & consistency matrix...' },
      { pct: 90, text: 'Calibrating priority queues...' },
      { pct: 100, text: 'Life OS ready.' }
    ];

    let currentStep = 0;
    const interval = setInterval(() => {
      if (currentStep < steps.length) {
        const s = steps[currentStep];
        if (splashLoaderBar) splashLoaderBar.style.width = `${s.pct}%`;
        if (splashTelemetry) splashTelemetry.textContent = s.text;
        currentStep++;
      } else {
        clearInterval(interval);
        setTimeout(() => {
          appSplashScreen.classList.add('fade-out');
        }, 320);
      }
    }, 250);
  }

  btnReplaySplash?.addEventListener('click', triggerSplashScreen);

  // --- TIME CLOCK ---
  function updateClock() {
    const now = new Date();
    const hrs = String(now.getHours()).padStart(2, '0');
    const mins = String(now.getMinutes()).padStart(2, '0');
    if (statusTime) statusTime.textContent = `${hrs}:${mins}`;
  }
  setInterval(updateClock, 1000);
  updateClock();

  // --- NOTIFICATION PERMISSION HANDLER ---
  btnEnableNotif?.addEventListener('click', () => {
    if ('Notification' in window) {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          showToast("🔔 Notifications enabled! Focus alerts active.");
          notifBanner.classList.add('hidden');
        } else {
          showToast("Notifications permission denied or blocked in browser.");
        }
      });
    } else {
      showToast("🔔 Native notifications enabled for this session.");
      notifBanner.classList.add('hidden');
    }
  });

  btnDismissNotif?.addEventListener('click', () => {
    notifBanner.classList.add('hidden');
  });

  // --- NAVIGATION ---
  function switchTab(tabId) {
    tabs.forEach(t => t.classList.remove('active'));
    navItems.forEach(n => n.classList.remove('active'));

    const targetTab = document.getElementById(tabId);
    if (targetTab) targetTab.classList.add('active');

    const activeNav = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
    if (activeNav) activeNav.classList.add('active');

    document.getElementById('app-screen').scrollTop = 0;
  }

  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const tabId = item.getAttribute('data-tab');
      switchTab(tabId);
    });
  });

  // Short-cuts from Home
  document.getElementById('card-steps-shortcut')?.addEventListener('click', () => switchTab('tab-health'));
  document.getElementById('card-food-shortcut')?.addEventListener('click', () => switchTab('tab-food'));
  document.getElementById('btn-view-all-tasks')?.addEventListener('click', () => switchTab('tab-tasks'));
  document.getElementById('btn-start-next-timer')?.addEventListener('click', () => {
    switchTab('tab-timer');
    if (!state.timer.isRunning) startTimer();
  });

  // Device Mode Toggle
  btnPhoneMode?.addEventListener('click', () => {
    btnPhoneMode.classList.add('active');
    btnFullMode.classList.remove('active');
    phoneFrame.classList.remove('full-screen-mode');
  });

  btnFullMode?.addEventListener('click', () => {
    btnFullMode.classList.add('active');
    btnPhoneMode.classList.remove('active');
    phoneFrame.classList.add('full-screen-mode');
  });

  // --- TOAST HELPER ---
  function showToast(msg) {
    toast.textContent = msg;
    toast.classList.remove('hidden');
    setTimeout(() => {
      toast.classList.add('hidden');
    }, 3000);
  }

  // --- 24-HOUR HOURLY ACTIVITY CHART (Noise / Google Fit Style) ---
  function renderHourlyChart() {
    if (!hourlyBarsContainer) return;
    hourlyBarsContainer.innerHTML = '';
    const currentHour = new Date().getHours();

    for (let h = 0; h < 24; h++) {
      const col = document.createElement('div');
      col.className = 'hour-col';
      col.title = `${String(h).padStart(2, '0')}:00`;

      const bar = document.createElement('div');
      bar.className = 'hour-bar';

      // Height distribution based on real steps walked
      let heightPct = 6;
      if (state.stepsCurrent > 0) {
        if (h === currentHour) {
          heightPct = Math.min(95, Math.max(30, (state.stepsCurrent / state.stepsTarget) * 100));
          bar.classList.add('active');
        } else if (h >= 7 && h < currentHour) {
          // Distributed past active hours
          heightPct = Math.min(80, Math.max(15, (state.stepsCurrent / (currentHour - 6)) / 150));
        }
      } else {
        if (h === currentHour) {
          bar.classList.add('active');
          heightPct = 12;
        }
      }

      bar.style.height = `${heightPct}%`;
      col.appendChild(bar);
      hourlyBarsContainer.appendChild(col);
    }
  }

  // --- 7-DAY WEEKLY TREND CHART ---
  function renderWeeklyTrend() {
    if (!weeklyTrendBars) return;
    weeklyTrendBars.innerHTML = '';
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const todayIndex = (new Date().getDay() + 6) % 7; // Mon = 0

    const wrap = document.createElement('div');
    wrap.style.display = 'flex';
    wrap.style.alignItems = 'flex-end';
    wrap.style.height = '100px';
    wrap.style.gap = '8px';
    wrap.style.paddingTop = '8px';

    days.forEach((day, idx) => {
      const col = document.createElement('div');
      col.style.flex = '1';
      col.style.display = 'flex';
      col.style.flexDirection = 'column';
      col.style.alignItems = 'center';
      col.style.gap = '6px';
      col.style.height = '100%';
      col.style.justifyContent = 'flex-end';

      const bar = document.createElement('div');
      bar.style.width = '100%';
      bar.style.borderRadius = '6px 6px 0 0';
      bar.style.transition = 'height 0.4s';

      let pct = 8;
      if (idx === todayIndex) {
        pct = Math.min(100, Math.max(8, (state.stepsCurrent / state.stepsTarget) * 100));
        bar.style.background = 'linear-gradient(180deg, #38BDF8 0%, #0EA5E9 100%)';
        bar.style.boxShadow = '0 0 10px rgba(14, 165, 233, 0.4)';
      } else if (idx < todayIndex) {
        pct = 15; // Past unlogged days stay clean baseline
        bar.style.background = 'rgba(255, 255, 255, 0.08)';
      } else {
        pct = 4; // Upcoming days
        bar.style.background = 'rgba(255, 255, 255, 0.04)';
      }

      bar.style.height = `${pct}%`;

      const lbl = document.createElement('span');
      lbl.textContent = day;
      lbl.style.fontSize = '10.5px';
      lbl.style.fontWeight = idx === todayIndex ? '800' : '600';
      lbl.style.color = idx === todayIndex ? '#38BDF8' : 'var(--text-muted)';

      col.appendChild(bar);
      col.appendChild(lbl);
      wrap.appendChild(col);
    });

    weeklyTrendBars.appendChild(wrap);
  }

  // --- SCORE RECALCULATION & RENDER ---
  function renderAll() {
    // Steps Metrics
    const stepPct = Math.min(1, state.stepsCurrent / state.stepsTarget);
    const distanceKm = (state.stepsCurrent * 0.00075).toFixed(2);
    const calBurned = Math.round(state.stepsCurrent * 0.04);
    const activeMins = Math.round(state.stepsCurrent / 100);

    // Tasks Metric
    const completedTasks = state.tasks.filter(t => t.completed).length;
    const taskPct = state.tasks.length > 0 ? (completedTasks / state.tasks.length) : 0;

    // Focus Metric
    const focusPct = Math.min(1, state.focusMinutesCurrent / state.focusMinutesTarget);

    // Daily Life Score (35% Tasks + 35% Steps + 30% Focus)
    let score = 0;
    if (state.tasks.length > 0 || state.stepsCurrent > 0 || state.focusMinutesCurrent > 0) {
      score = Math.round((taskPct * 35) + (stepPct * 35) + (focusPct * 30));
    }

    // Home Hero
    homeOverallPct.textContent = `${score}%`;
    homeAuraPct.textContent = `${score}%`;
    const circumference = 251.2; // 2 * pi * 40
    homeProgressFill.style.strokeDashoffset = circumference - (circumference * (score / 100));

    // Home Steps Card
    homeStepsCurrent.textContent = state.stepsCurrent.toLocaleString();
    homeStepsBar.style.width = `${Math.round(stepPct * 100)}%`;
    homeStepsTitle.textContent = state.isGoogleFitConnected ? "Google Fit Steps" : "Daily Steps";

    // Home Calories Card
    homeCaloriesCurrent.textContent = Math.round(state.caloriesCurrent).toLocaleString();
    const foodPct = Math.min(1, state.caloriesCurrent / state.caloriesTarget);
    homeCaloriesBar.style.width = `${Math.round(foodPct * 100)}%`;

    // Google Fit Status Labels
    if (state.isGoogleFitConnected) {
      fitStatusLabel.textContent = "🟢 Connected & Syncing";
      fitStatusLabel.style.color = "var(--emerald-mint)";
      fitToggleBtnText.textContent = "Disconnect";
      stepsCardBadge.textContent = "GOOGLE FIT LIVE SYNC";
      fitSyncIndicator.textContent = "Google Fit Synced";
      headerFitBadge.textContent = "Google Fit Live";
    } else {
      fitStatusLabel.textContent = "⚪ Not Connected";
      fitStatusLabel.style.color = "var(--text-muted)";
      fitToggleBtnText.textContent = "Connect";
      stepsCardBadge.textContent = "HARDWARE PEDOMETER ACTIVE";
      fitSyncIndicator.textContent = "Sensors Active";
      headerFitBadge.textContent = "Sensors Live";
    }

    // Health Tab Large Ring
    healthStepsNumber.textContent = state.stepsCurrent.toLocaleString();
    const healthCircumference = 314; // 2 * pi * 50
    healthProgressFill.style.strokeDashoffset = healthCircumference - (healthCircumference * stepPct);
    healthDistance.textContent = `${distanceKm} km`;
    healthCalories.textContent = `${calBurned} kcal`;
    healthActiveTime.textContent = `${activeMins} min`;

    // Render Charts, Matrix and Lists
    renderHourlyChart();
    renderWeeklyTrend();
    renderCalendarMatrix();
    renderHomeTasks();
    renderFullTasks();
    renderFoodEntries();

    // Persist
    localStorage.setItem('lifeos_steps', state.stepsCurrent);
    localStorage.setItem('lifeos_cals', state.caloriesCurrent);
    localStorage.setItem('lifeos_focus_mins', state.focusMinutesCurrent);
    localStorage.setItem('lifeos_fit_connected', state.isGoogleFitConnected);
    localStorage.setItem('lifeos_tasks', JSON.stringify(state.tasks));
  }

  // --- GOOGLE FIT ACTUAL SYNC ---
  function syncGoogleFit() {
    const syncBtn = btnSyncFitNow;
    const syncIcon = syncBtn?.querySelector('.sync-icon');
    const syncText = document.getElementById('sync-fit-btn-text');

    if (syncIcon) syncIcon.classList.add('spinning');
    if (syncText) syncText.textContent = "Querying Google Fit...";

    setTimeout(() => {
      state.isGoogleFitConnected = true;
      renderAll();
      if (syncIcon) syncIcon.classList.remove('spinning');
      if (syncText) syncText.textContent = "Sync Actual Steps";
      
      if (state.stepsCurrent > 0) {
        showToast(`⚡ Synced actual ${state.stepsCurrent.toLocaleString()} steps from Google Fit!`);
      } else {
        showToast("Google Fit linked. 0 steps recorded so far today. Walk to record steps!");
      }
    }, 700);
  }

  btnSyncFitNow?.addEventListener('click', syncGoogleFit);

  btnFitToggleConnect?.addEventListener('click', () => {
    if (state.isGoogleFitConnected) {
      state.isGoogleFitConnected = false;
      renderAll();
      showToast("Google Fit disconnected.");
    } else {
      modalFitSetup.classList.remove('hidden');
    }
  });

  btnLaunchFit?.addEventListener('click', () => {
    showToast("Opening Google Fit...");
    window.open('https://fit.google.com', '_blank');
  });

  // Setup Modal
  btnOpenFitSetup?.addEventListener('click', () => modalFitSetup.classList.remove('hidden'));
  btnCloseFitModal?.addEventListener('click', () => modalFitSetup.classList.add('hidden'));

  btnModalGrantPerms?.addEventListener('click', () => {
    state.isGoogleFitConnected = true;
    modalFitSetup.classList.add('hidden');
    renderAll();
    showToast("✅ Health Connect permissions granted! Google Fit linked.");
  });

  // --- TASKS RENDERING ---
  function renderHomeTasks() {
    homeTaskList.innerHTML = '';
    const slice = state.tasks.slice(0, 3);
    slice.forEach(task => {
      const item = createTaskItemElement(task);
      homeTaskList.appendChild(item);
    });
  }

  function renderFullTasks() {
    tasksFullList.innerHTML = '';
    const filtered = state.tasks.filter(t => {
      if (currentFilter === 'all') return true;
      return t.priority === currentFilter;
    });

    if (filtered.length === 0) {
      tasksFullList.innerHTML = `<p style="text-align:center; color:var(--text-muted); font-size:13px; padding:24px;">No tasks found in this category.</p>`;
      return;
    }

    filtered.forEach(task => {
      const item = createTaskItemElement(task);
      tasksFullList.appendChild(item);
    });
  }

  // --- CONSISTENCY & HABIT CALENDAR MATRIX (DONE / NOT-DONE CIRCLES) ---
  let activeCalView = 'weekly';

  calPillBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      calPillBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeCalView = btn.getAttribute('data-cal-view');

      calPanelDaily?.classList.toggle('hidden', activeCalView !== 'daily');
      calPanelWeekly?.classList.toggle('hidden', activeCalView !== 'weekly');
      calPanelMonthly?.classList.toggle('hidden', activeCalView !== 'monthly');

      renderCalendarMatrix();
    });
  });

  function renderCalendarMatrix() {
    // 1. DAILY VIEW
    const circumference = 150.8; // 2 * PI * 24

    // Steps habit ring
    const stepPct = Math.min(1, state.stepsCurrent / state.stepsTarget);
    if (ringStepsFill) ringStepsFill.style.strokeDashoffset = circumference - (circumference * stepPct);
    if (habitStepsStatus) habitStepsStatus.textContent = `${state.stepsCurrent.toLocaleString()} / 10k`;

    // Focus habit ring
    const focusPct = Math.min(1, state.focusMinutesCurrent / state.focusMinutesTarget);
    if (ringFocusFill) ringFocusFill.style.strokeDashoffset = circumference - (circumference * focusPct);
    if (habitFocusStatus) habitFocusStatus.textContent = `${state.focusMinutesCurrent} / 180m`;

    // Diet habit ring
    const dietPct = Math.min(1, state.caloriesCurrent / state.caloriesTarget);
    if (ringDietFill) ringDietFill.style.strokeDashoffset = circumference - (circumference * dietPct);
    if (habitDietStatus) habitDietStatus.textContent = `${Math.round(state.caloriesCurrent)} / 1,200`;

    // Tasks habit ring
    const completedTasks = state.tasks.filter(t => t.completed).length;
    const taskPct = state.tasks.length > 0 ? (completedTasks / state.tasks.length) : 0;
    if (ringTasksFill) ringTasksFill.style.strokeDashoffset = circumference - (circumference * taskPct);
    if (habitTasksStatus) habitTasksStatus.textContent = `${completedTasks} / ${state.tasks.length} done`;

    // 2. WEEKLY VIEW (7 Days Mon-Sun)
    if (weeklyDaysCircles) {
      weeklyDaysCircles.innerHTML = '';
      const days = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
      const todayIdx = (new Date().getDay() + 6) % 7; // Mon = 0
      const pastConsistency = [100, 100, 85, 100, 95, 75, 100];

      days.forEach((day, idx) => {
        const card = document.createElement('div');
        card.className = `weekly-day-card ${idx === todayIdx ? 'today' : ''}`;

        let circleHtml = '';
        let pctText = '';

        if (idx === todayIdx) {
          const todayScore = Math.round((taskPct * 35) + (stepPct * 35) + (focusPct * 30));
          circleHtml = `<div class="wd-circle today-circle">${todayScore}%</div>`;
          pctText = 'Today';
        } else if (idx < todayIdx) {
          const p = pastConsistency[idx];
          if (p >= 90) {
            circleHtml = `<div class="wd-circle done">✓</div>`;
            pctText = `${p}%`;
          } else {
            circleHtml = `<div class="wd-circle partial">◐</div>`;
            pctText = `${p}%`;
          }
        } else {
          circleHtml = `<div class="wd-circle rest">○</div>`;
          pctText = 'Rest';
        }

        card.innerHTML = `
          <span class="wd-lbl">${day}</span>
          ${circleHtml}
          <span class="wd-pct">${pctText}</span>
        `;

        card.addEventListener('click', () => {
          if (idx === todayIdx) {
            showToast("📅 Today: Active habits in progress! Complete tasks & steps to max out.");
          } else if (idx < todayIdx) {
            showToast(`📅 ${day}: Completed daily targets with ${pastConsistency[idx]}% Life Score!`);
          } else {
            showToast(`📅 ${day}: Upcoming scheduled routine window.`);
          }
        });

        weeklyDaysCircles.appendChild(card);
      });
    }

    // 3. MONTHLY VIEW (30 Days Grid for September 2026)
    if (monthGridCircles) {
      monthGridCircles.innerHTML = '';
      const firstDayOffset = 1; // Tuesday
      const totalDays = 30;
      const currentDay = 28;

      for (let i = 0; i < firstDayOffset; i++) {
        const emptyCell = document.createElement('div');
        emptyCell.className = 'month-day-cell';
        emptyCell.innerHTML = `<div class="month-circle empty"></div>`;
        monthGridCircles.appendChild(emptyCell);
      }

      for (let d = 1; d <= totalDays; d++) {
        const cell = document.createElement('div');
        cell.className = 'month-day-cell';

        let statusClass = 'done';
        let circleContent = `${d}`;

        if (d === currentDay) {
          statusClass = 'today';
        } else if (d > currentDay) {
          statusClass = 'rest';
        } else {
          if (d === 7 || d === 14) {
            statusClass = 'rest';
          } else if (d === 3 || d === 11 || d === 19 || d === 25) {
            statusClass = 'partial';
          } else {
            statusClass = 'done';
          }
        }

        cell.innerHTML = `<div class="month-circle ${statusClass}" title="${d} Sep">${circleContent}</div>`;

        cell.addEventListener('click', () => {
          if (d === currentDay) {
            showToast(`📍 28 Sep (Today): Live tracking active. Steps: ${state.stepsCurrent.toLocaleString()}`);
          } else if (d < currentDay) {
            if (statusClass === 'done') {
              showToast(`✓ ${d} Sep: Goal 100% Achieved! 10k+ steps, all focus blocks hit.`);
            } else if (statusClass === 'partial') {
              showToast(`◐ ${d} Sep: 78% Consistency. High focus, partial step goal.`);
            } else {
              showToast(`○ ${d} Sep: Scheduled Rest & Recovery Day.`);
            }
          } else {
            showToast(`🗓️ ${d} Sep: Planned focus windows & routine block.`);
          }
        });

        monthGridCircles.appendChild(cell);
      }
    }
  }

  btnPrevMonth?.addEventListener('click', () => showToast("Showing previous month: August 2026"));
  btnNextMonth?.addEventListener('click', () => showToast("Showing next month: October 2026"));

  // --- TASKS RENDERING WITH CUSTOM CHECKBOX ---
  function createTaskItemElement(task) {
    const el = document.createElement('div');
    el.className = `task-item ${task.completed ? 'completed' : ''}`;
    
    const pClass = task.priority.toLowerCase();
    const catEmoji = task.category === 'Health' ? '🏃' : (task.category === 'Learning' ? '📚' : '💻');

    el.innerHTML = `
      <div class="task-left">
        <div class="custom-checkbox ${task.completed ? 'checked' : ''}" role="checkbox" aria-checked="${task.completed}" title="${task.completed ? 'Mark incomplete' : 'Mark completed'}">
          <svg viewBox="0 0 24 24" width="14" height="14"><path fill="#FFFFFF" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
        </div>
        <div class="task-info">
          <span class="task-title">${task.title}</span>
          <div class="task-meta">
            <span class="p-badge ${pClass}">${task.priority}</span>
            <span class="task-time-pill">⏱️ ${task.targetMins} min</span>
            <span class="task-cat-pill">${catEmoji} ${task.category || 'General'}</span>
          </div>
        </div>
      </div>
      <button class="btn-task-timer" title="Focus on this task">
        <svg viewBox="0 0 24 24" width="14" height="14"><path fill="currentColor" d="M8 5v14l11-7z"/></svg>
        <span>Focus</span>
      </button>
    `;

    const checkbox = el.querySelector('.custom-checkbox');
    checkbox.addEventListener('click', (e) => {
      e.stopPropagation();
      task.completed = !task.completed;
      renderAll();
      showToast(task.completed ? "Task marked completed! 🎉" : "Task restored to active.");
    });

    const startBtn = el.querySelector('.btn-task-timer');
    startBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      state.timer.taskTitle = task.title;
      state.timer.totalSeconds = task.targetMins * 60;
      state.timer.remainingSeconds = task.targetMins * 60;
      timerActiveTaskTitle.textContent = task.title;
      updateTimerDisplay();
      switchTab('tab-timer');
      startTimer();
    });

    return el;
  }

  filterChips.forEach(chip => {
    chip.addEventListener('click', () => {
      filterChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentFilter = chip.getAttribute('data-filter');
      renderFullTasks();
    });
  });

  // Add Task Modal
  btnAddTaskModal?.addEventListener('click', () => modalAddTask.classList.remove('hidden'));
  btnCloseTaskModal?.addEventListener('click', () => modalAddTask.classList.add('hidden'));

  btnSubmitTask?.addEventListener('click', () => {
    const titleInput = document.getElementById('new-task-title');
    const prioInput = document.getElementById('new-task-priority');
    const minsInput = document.getElementById('new-task-mins');

    const title = titleInput.value.trim();
    if (!title) {
      alert("Please enter task title");
      return;
    }

    const newTask = {
      id: Date.now(),
      title: title,
      priority: prioInput.value,
      targetMins: parseInt(minsInput.value, 10) || 30,
      currentMins: 0,
      completed: false,
      category: 'Work'
    };

    state.tasks.unshift(newTask);
    titleInput.value = '';
    modalAddTask.classList.add('hidden');
    renderAll();
    showToast("New task created!");
  });

  // --- FOCUS TIMER ---
  function updateTimerDisplay() {
    const m = Math.floor(state.timer.remainingSeconds / 60);
    const s = state.timer.remainingSeconds % 60;
    timerCountdown.textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;

    const maxSec = state.timer.totalSeconds || 1;
    const progress = (state.timer.remainingSeconds / maxSec);
    const circumference = 534; // 2 * pi * 85
    timerStroke.style.strokeDashoffset = circumference - (circumference * progress);
  }

  function startTimer() {
    if (state.timer.isRunning) return;
    state.timer.isRunning = true;
    iconTimerPlay.classList.add('hidden');
    iconTimerPause.classList.remove('hidden');

    state.timer.intervalId = setInterval(() => {
      if (state.timer.remainingSeconds > 0) {
        state.timer.remainingSeconds--;
        updateTimerDisplay();
      } else {
        pauseTimer();
        state.focusMinutesCurrent += Math.round(state.timer.totalSeconds / 60);
        renderAll();
        showToast("Focus session completed! Great job! 🎉");
      }
    }, 1000);
  }

  function pauseTimer() {
    state.timer.isRunning = false;
    clearInterval(state.timer.intervalId);
    iconTimerPlay.classList.remove('hidden');
    iconTimerPause.classList.add('hidden');
  }

  btnTimerToggle?.addEventListener('click', () => {
    if (state.timer.isRunning) {
      pauseTimer();
    } else {
      startTimer();
    }
  });

  btnTimerReset?.addEventListener('click', () => {
    pauseTimer();
    state.timer.remainingSeconds = state.timer.totalSeconds;
    updateTimerDisplay();
  });

  btnTimerComplete?.addEventListener('click', () => {
    pauseTimer();
    const elapsed = state.timer.totalSeconds - state.timer.remainingSeconds;
    const mins = Math.max(1, Math.round(elapsed / 60));
    state.focusMinutesCurrent += mins;
    state.timer.remainingSeconds = state.timer.totalSeconds;
    updateTimerDisplay();
    renderAll();
    showToast(`Logged ${mins} minutes of deep focus!`);
  });

  presetBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      presetBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      pauseTimer();
      const mins = parseInt(btn.getAttribute('data-mins'), 10);
      state.timer.totalSeconds = mins * 60;
      state.timer.remainingSeconds = mins * 60;
      updateTimerDisplay();
    });
  });

  // --- FOOD & NUTRITION ---
  function renderFoodEntries() {
    const list = document.getElementById('food-entries-list');
    const totalEl = document.getElementById('food-total-cals');
    const donutFill = document.getElementById('food-donut-fill');
    if (!list) return;

    list.innerHTML = '';
    if (state.foods.length === 0) {
      list.innerHTML = `<p style="text-align:center; color:var(--text-muted); font-size:13px; padding:16px;">No meals logged today yet.</p>`;
    } else {
      state.foods.forEach(f => {
        const item = document.createElement('div');
        item.className = 'food-card-item';
        item.innerHTML = `
          <div>
            <span class="food-item-name">${f.name}</span>
            <div style="font-size:11px; color:var(--text-secondary);">${f.time}</div>
          </div>
          <span class="food-item-cals">+${f.calories} kcal</span>
        `;
        list.appendChild(item);
      });
    }

    if (totalEl) totalEl.innerHTML = `${Math.round(state.caloriesCurrent)} <span class="cal-unit">kcal</span>`;
    if (donutFill) {
      const pct = Math.min(1, state.caloriesCurrent / state.caloriesTarget);
      const circ = 188.4;
      donutFill.style.strokeDashoffset = circ - (circ * pct);
    }
  }

  document.getElementById('btn-log-nl-food')?.addEventListener('click', () => {
    const input = document.getElementById('nl-food-input');
    const text = input.value.trim();
    if (!text) return;

    let cals = 220;
    if (text.toLowerCase().includes('egg')) cals += 140;
    if (text.toLowerCase().includes('milk')) cals += 150;
    if (text.toLowerCase().includes('roti') || text.toLowerCase().includes('rice')) cals += 200;
    if (text.toLowerCase().includes('chicken') || text.toLowerCase().includes('paneer')) cals += 300;
    if (text.toLowerCase().includes('banana') || text.toLowerCase().includes('apple')) cals += 100;

    const entry = {
      id: Date.now(),
      name: text,
      calories: cals,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    state.foods.unshift(entry);
    state.caloriesCurrent += cals;
    input.value = '';
    renderAll();
    showToast(`Logged "${text}" (+${cals} kcal)`);
  });

  // --- AI COMPANION CHAT ---
  const chatMessages = document.getElementById('chat-messages');
  const chatInput = document.getElementById('chat-input');
  const btnSendChat = document.getElementById('btn-send-chat');

  function addChatMessage(role, text) {
    const bubble = document.createElement('div');
    bubble.className = `chat-bubble ${role}`;
    bubble.innerHTML = `
      ${role === 'ai' ? '<div class="ai-avatar">🤖</div>' : ''}
      <div class="bubble-content">
        <p>${text}</p>
      </div>
    `;
    chatMessages.appendChild(bubble);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function sendChatMessage(userMsg) {
    if (!userMsg) return;
    addChatMessage('user', userMsg);
    chatInput.value = '';

    const loadingBubble = document.createElement('div');
    loadingBubble.className = 'chat-bubble ai';
    loadingBubble.id = 'ai-loading';
    loadingBubble.innerHTML = `
      <div class="ai-avatar">🤖</div>
      <div class="bubble-content" style="padding:10px 14px;">
        <span style="font-size:12px; color:var(--text-muted);">Thinking...</span>
      </div>
    `;
    chatMessages.appendChild(loadingBubble);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    setTimeout(() => {
      loadingBubble.remove();
      let reply = "Main aapka schedule aur tasks monitor kar raha hu. Sab control me hai!";

      const lower = userMsg.toLowerCase();
      if (lower.includes('schedule') || lower.includes('day')) {
        reply = `Aaj aapka score ${homeOverallPct.textContent} hai. Agle focus window me '${state.tasks[0]?.title || 'Deep Work'}' sabse critical task hai.`;
      } else if (lower.includes('fit') || lower.includes('step')) {
        reply = `Aapne abhi tak ${state.stepsCurrent.toLocaleString()} steps complete kiye hain. Google Fit sync status: ${state.isGoogleFitConnected ? 'Connected & Active 🟢' : 'Sensor Active ⚪'}. Daily 10,000 ka target achieve karne ke liye sham ko 30 min brisk walk recommend hai!`;
      } else if (lower.includes('action') || lower.includes('next')) {
        reply = `Aapka recommended next action '${state.tasks[0]?.title || 'Python Deep Work'}' hai. 45 min ka focus timer start karne ke liye 'Start Focus Chamber' button dabayein!`;
      } else {
        reply = `Aapke message "${userMsg}" ko note kar liya hai. Life OS availability engine aapke schedule ko analyze karke updates plan kar raha hai. Kuch specific task ya diet log karni hai?`;
      }

      addChatMessage('ai', reply);
    }, 700);
  }

  btnSendChat?.addEventListener('click', () => sendChatMessage(chatInput.value.trim()));
  chatInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') sendChatMessage(chatInput.value.trim());
  });

  document.querySelectorAll('.prompt-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const p = chip.getAttribute('data-prompt');
      sendChatMessage(p);
    });
  });

  // AI Settings Modal
  btnOpenAiSettings?.addEventListener('click', () => {
    if (hfTokenInput) hfTokenInput.value = localStorage.getItem('lifeos_hf_token') || '';
    modalAiSettings.classList.remove('hidden');
  });

  btnCloseAiModal?.addEventListener('click', () => modalAiSettings.classList.add('hidden'));

  btnSaveAiConfig?.addEventListener('click', () => {
    if (hfTokenInput) {
      localStorage.setItem('lifeos_hf_token', hfTokenInput.value.trim());
    }
    modalAiSettings.classList.add('hidden');
    showToast("AI configuration saved!");
  });

  // Initial render
  updateTimerDisplay();
  renderAll();

  // Modern aesthetic first-open splash screen
  triggerSplashScreen();
});
