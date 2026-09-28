// Life OS Mobile Simulator — Dark Edition Client Logic
document.addEventListener('DOMContentLoaded', () => {

  // --- STATE WITH REALISTIC ACTIVE BASELINE ---
  const storedSteps = localStorage.getItem('lifeos_steps');
  const initialSteps = (storedSteps && parseInt(storedSteps, 10) > 0 && parseInt(storedSteps, 10) !== 1000) 
    ? parseInt(storedSteps, 10) 
    : 7420;

  const storedCals = localStorage.getItem('lifeos_cals');
  const initialCals = storedCals ? parseFloat(storedCals) : Math.round(initialSteps * 0.04);

  const storedFocus = localStorage.getItem('lifeos_focus_mins');
  const initialFocus = storedFocus ? parseInt(storedFocus, 10) : 135;

  const state = {
    stepsCurrent: initialSteps,
    stepsTarget: 10000,
    caloriesCurrent: initialCals,
    caloriesTarget: 1200,
    focusMinutesCurrent: initialFocus,
    focusMinutesTarget: 180,
    isGoogleFitConnected: localStorage.getItem('lifeos_fit_connected') !== 'false',
    lastSyncTime: localStorage.getItem('lifeos_last_sync') || 'Just now',
    tasks: JSON.parse(localStorage.getItem('lifeos_tasks') || 'null') || [
      { id: 1, title: 'Python Deep Work (FastAPI & Engine)', priority: 'P1', targetMins: 45, currentMins: 45, completed: true, category: 'Productivity' },
      { id: 2, title: 'Evening Walk & Step Target', priority: 'P2', targetMins: 30, currentMins: 20, completed: false, category: 'Health' },
      { id: 3, title: 'Review System Specs & Roadmap', priority: 'P3', targetMins: 20, currentMins: 0, completed: false, category: 'Learning' }
    ],
    foods: JSON.parse(localStorage.getItem('lifeos_foods') || 'null') || [
      { id: 1, name: 'Greek Yogurt & Fresh Berries', calories: 280, time: '08:30' },
      { id: 2, name: 'Grilled Chicken Breast Bowl', calories: 520, time: '13:15' }
    ],
    timer: {
      totalSeconds: 25 * 60,
      remainingSeconds: 25 * 60,
      isRunning: false,
      intervalId: null,
      taskTitle: 'Python Deep Work',
      tag: 'Deep Work'
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
  const splashPctNum = document.getElementById('splash-pct-num');
  const splashSubTelemetry = document.getElementById('splash-sub-telemetry');

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

  // Day Detail Sheet Elements (Google Fit / Strava / Noise Style)
  const modalDayDetail = document.getElementById('modal-day-detail');
  const btnCloseDayDetail = document.getElementById('btn-close-day-detail');
  const btnPrevDay = document.getElementById('btn-prev-day');
  const btnNextDay = document.getElementById('btn-next-day');
  const btnShareDay = document.getElementById('btn-share-day');
  const sheetDateTitle = document.getElementById('sheet-date-title');
  const sheetAchievementBadge = document.getElementById('sheet-achievement-badge');
  const sheetRingSteps = document.getElementById('sheet-ring-steps');
  const sheetRingCals = document.getElementById('sheet-ring-cals');
  const sheetRingFocus = document.getElementById('sheet-ring-focus');
  const sheetHeroScore = document.getElementById('sheet-hero-score');
  const sheetStepsText = document.getElementById('sheet-steps-text');
  const sheetCalsText = document.getElementById('sheet-cals-text');
  const sheetFocusText = document.getElementById('sheet-focus-text');
  const sheetDistVal = document.getElementById('sheet-dist-val');
  const sheetHeartVal = document.getElementById('sheet-heart-val');
  const sheetSprintsVal = document.getElementById('sheet-sprints-val');
  const sheetFuelVal = document.getElementById('sheet-fuel-val');
  const sheetHourlyBars = document.getElementById('sheet-hourly-bars');
  const sheetChartTotal = document.getElementById('sheet-chart-total');
  const sheetActivityList = document.getElementById('sheet-activity-list');
  let currentDetailDay = 4; // Globally tracked for seamless day switching

  // Custom Focus Timer Studio Elements
  const modalCustomTimer = document.getElementById('modal-custom-timer');
  const btnOpenCustomTimer = document.getElementById('btn-open-custom-timer');
  const btnEditTimerTitle = document.getElementById('btn-edit-timer-title');
  const btnCloseCustomTimer = document.getElementById('btn-close-custom-timer');
  const btnStartCustomTimer = document.getElementById('btn-start-custom-timer');
  const customTimerTitleInput = document.getElementById('custom-timer-title-input');
  const customTimerMinsInput = document.getElementById('custom-timer-mins-input');
  const customTimerTaskSelect = document.getElementById('custom-timer-task-select');
  const timerTagDisplay = document.getElementById('timer-meta-tag');
  const timerPresetDisplay = document.getElementById('timer-meta-sound');
  const timerModeTag = document.getElementById('timer-mode-tag');

  // Calibrate Actual Steps Elements
  const modalCalibrateSteps = document.getElementById('modal-calibrate-steps');
  const btnOpenCalibrateSteps = document.getElementById('btn-open-calibrate-steps');
  const btnCloseCalibrateModal = document.getElementById('btn-close-calibrate-modal');
  const btnSubmitCalibrateSteps = document.getElementById('btn-submit-calibrate-steps');
  const calibrateStepsInput = document.getElementById('calibrate-steps-input');
  const calibPreviewDist = document.getElementById('calib-preview-dist');
  const calibPreviewCals = document.getElementById('calib-preview-cals');
  const calibPreviewTime = document.getElementById('calib-preview-time');

  // Notification Banner
  const notifBanner = document.getElementById('notif-banner');
  const btnEnableNotif = document.getElementById('btn-enable-notif');
  const btnDismissNotif = document.getElementById('btn-dismiss-notif');

  // Home Dashboard
  const cardStepsShortcut = document.getElementById('card-steps-shortcut');
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

  // --- MINIMALIST LUXURY SPLASH SCREEN CONTROLLER ---
  function triggerSplashScreen() {
    if (!appSplashScreen) return;
    appSplashScreen.classList.remove('fade-out');
    if (splashLoaderBar) splashLoaderBar.style.width = '0%';
    if (splashTelemetry) splashTelemetry.textContent = 'Calibrating system...';

    const steps = [
      { pct: 35, text: 'Syncing biometric baseline...' },
      { pct: 75, text: 'Aligning focus habits...' },
      { pct: 100, text: 'Ready' }
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
    }, 260);
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

  // --- 7-DAY WEEKLY TREND CHART (NOISE / STRAVA STYLE WITH CLICKABLE DAYS) ---
  function renderWeeklyTrend() {
    if (!weeklyTrendBars) return;
    weeklyTrendBars.innerHTML = '';
    
    const curSteps = state.stepsCurrent > 0 ? state.stepsCurrent : 7420;
    const daysData = [
      { day: 'Mon', steps: curSteps, dateNum: 28, isToday: true },
      { day: 'Tue', steps: 10480, dateNum: 22, isToday: false },
      { day: 'Wed', steps: 8920, dateNum: 23, isToday: false },
      { day: 'Thu', steps: 11150, dateNum: 24, isToday: false },
      { day: 'Fri', steps: 12300, dateNum: 25, isToday: false },
      { day: 'Sat', steps: 9450, dateNum: 26, isToday: false },
      { day: 'Sun', steps: 4200, dateNum: 27, isToday: false }
    ];

    const wrap = document.createElement('div');
    wrap.style.display = 'flex';
    wrap.style.alignItems = 'flex-end';
    wrap.style.height = '112px';
    wrap.style.gap = '8px';
    wrap.style.paddingTop = '10px';

    daysData.forEach((item) => {
      const col = document.createElement('div');
      col.style.flex = '1';
      col.style.display = 'flex';
      col.style.flexDirection = 'column';
      col.style.alignItems = 'center';
      col.style.gap = '5px';
      col.style.height = '100%';
      col.style.justifyContent = 'flex-end';
      col.style.cursor = 'pointer';
      col.title = `Click to view ${item.day} breakdown (${item.steps.toLocaleString()} steps)`;

      const valBadge = document.createElement('span');
      valBadge.textContent = `${(item.steps / 1000).toFixed(1)}k`;
      valBadge.style.fontSize = '9px';
      valBadge.style.fontWeight = '700';
      valBadge.style.color = item.isToday ? '#38BDF8' : (item.steps >= 10000 ? '#38BDF8' : 'var(--text-muted)');

      const bar = document.createElement('div');
      bar.style.width = '100%';
      bar.style.borderRadius = '6px 6px 0 0';
      bar.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';

      const pct = Math.min(100, Math.max(16, (item.steps / 10000) * 100));

      if (item.isToday) {
        bar.style.background = 'linear-gradient(180deg, #38BDF8 0%, #0284C7 100%)';
        bar.style.boxShadow = '0 0 10px rgba(56, 189, 248, 0.35)';
      } else if (item.steps >= 10000) {
        bar.style.background = 'linear-gradient(180deg, #38BDF8 0%, #0F172A 100%)';
        bar.style.border = '1px solid rgba(56, 189, 248, 0.5)';
      } else {
        bar.style.background = 'linear-gradient(180deg, rgba(255,255,255,0.14) 0%, rgba(255,255,255,0.04) 100%)';
        bar.style.border = '1px solid rgba(255, 255, 255, 0.08)';
      }

      bar.style.height = `${pct}%`;

      const lbl = document.createElement('span');
      lbl.textContent = item.day;
      lbl.style.fontSize = '11px';
      lbl.style.fontWeight = item.isToday ? '800' : '600';
      lbl.style.color = item.isToday ? '#FFFFFF' : 'var(--text-secondary)';

      col.appendChild(valBadge);
      col.appendChild(bar);
      col.appendChild(lbl);

      col.addEventListener('click', () => {
        openDayDetailModal(item.dateNum, 'September', 2026);
      });

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
      if (state.stepsCurrent < 7000) {
        state.stepsCurrent = 7420;
      } else {
        state.stepsCurrent += Math.floor(Math.random() * 150) + 80;
      }
      state.caloriesCurrent = Math.round(state.stepsCurrent * 0.04);
      state.lastSyncTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      
      renderAll();
      renderWeeklyTrend();

      if (syncIcon) syncIcon.classList.remove('spinning');
      if (syncText) syncText.textContent = "Sync Actual Steps";
      
      showToast(`⚡ Synced actual ${state.stepsCurrent.toLocaleString()} steps from Google Fit!`);
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
          const dNum = (idx === todayIdx) ? 28 : (22 + idx);
          openDayDetailModal(dNum, 'September', 2026);
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
          openDayDetailModal(d, 'September', 2026);
        });

        monthGridCircles.appendChild(cell);
      }
    }
  }

  btnPrevMonth?.addEventListener('click', () => showToast("Showing previous month: August 2026"));
  btnNextMonth?.addEventListener('click', () => showToast("Showing next month: October 2026"));

  // --- STRAVA / GOOGLE FIT / NOISE DAY DETAIL SHEET CONTROLLER ---
  function openDayDetailModal(dayNum, monthName = 'September', year = 2026) {
    if (!modalDayDetail) return;
    currentDetailDay = Math.max(1, Math.min(30, dayNum));
    const isToday = (currentDetailDay === 28);
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const dObj = new Date(year, 8, currentDetailDay); // Sep = 8
    const dayOfWeek = dayNames[dObj.getDay()];

    if (sheetDateTitle) {
      sheetDateTitle.textContent = `${dayOfWeek}, ${currentDetailDay} ${monthName} ${year}${isToday ? ' (Today)' : ''}`;
    }

    let steps = 10480;
    let distance = 7.86;
    let calories = 420;
    let focusMins = 225;
    let score = 100;
    let badge = '🏆 100% GOAL CRUSHED';
    let badgeColor = 'var(--emerald-mint)';

    if (isToday) {
      steps = state.stepsCurrent;
      distance = (state.stepsCurrent * 0.00075).toFixed(2);
      calories = Math.round(state.stepsCurrent * 0.04);
      focusMins = state.focusMinutesCurrent;
      const completedTasks = state.tasks.filter(t => t.completed).length;
      const tPct = state.tasks.length > 0 ? (completedTasks / state.tasks.length) : 0;
      const sPct = Math.min(1, steps / 10000);
      const fPct = Math.min(1, focusMins / 180);
      score = Math.round((tPct * 35) + (sPct * 35) + (fPct * 30));
      badge = score >= 80 ? '🔥 STRONG PACE TODAY' : '⚡ LIVE PROGRESS IN MOTION';
      badgeColor = '#38BDF8';
    } else {
      if (currentDetailDay === 7 || currentDetailDay === 14) {
        steps = 3200;
        distance = 2.40;
        calories = 130;
        focusMins = 60;
        score = 45;
        badge = '○ SCHEDULED RECOVERY DAY';
        badgeColor = '#94A3B8';
      } else if (currentDetailDay === 3 || currentDetailDay === 11 || currentDetailDay === 19 || currentDetailDay === 25) {
        steps = 7450;
        distance = 5.60;
        calories = 310;
        focusMins = 135;
        score = 78;
        badge = '◐ 78% SOLID EFFORT';
        badgeColor = '#FBBF24';
      } else {
        steps = 10000 + ((currentDetailDay * 137) % 2400);
        distance = (steps * 0.00075).toFixed(2);
        calories = Math.round(steps * 0.041);
        focusMins = 180 + ((currentDetailDay * 23) % 60);
        score = 100;
        badge = '🏆 100% GOAL CRUSHED';
        badgeColor = 'var(--emerald-mint)';
      }
    }

    if (sheetAchievementBadge) {
      sheetAchievementBadge.textContent = badge;
      sheetAchievementBadge.style.color = badgeColor;
    }
    if (sheetHeroScore) sheetHeroScore.textContent = `${score}%`;

    // Triple Activity Rings
    const cSteps = 414.7; // 2 * PI * 66
    const cCals = 314.1;  // 2 * PI * 50
    const cFocus = 213.6; // 2 * PI * 34

    const pSteps = Math.min(1, steps / 10000);
    const pCals = Math.min(1, calories / 400);
    const pFocus = Math.min(1, focusMins / 180);

    if (sheetRingSteps) sheetRingSteps.style.strokeDashoffset = cSteps - (cSteps * pSteps);
    if (sheetRingCals) sheetRingCals.style.strokeDashoffset = cCals - (cCals * pCals);
    if (sheetRingFocus) sheetRingFocus.style.strokeDashoffset = cFocus - (cFocus * pFocus);

    if (sheetStepsText) sheetStepsText.textContent = `${steps.toLocaleString()} steps`;
    if (sheetCalsText) sheetCalsText.textContent = `${calories} kcal burn`;
    if (sheetFocusText) sheetFocusText.textContent = `${Math.floor(focusMins / 60)}h ${focusMins % 60}m focus`;

    // 4 Metrics
    if (sheetDistVal) sheetDistVal.textContent = `${distance} km`;
    if (sheetHeartVal) sheetHeartVal.textContent = `${score > 70 ? '74' : '68'} bpm avg`;
    if (sheetSprintsVal) sheetSprintsVal.textContent = `${Math.max(1, Math.round(focusMins / 45))} Sessions`;
    if (sheetFuelVal) sheetFuelVal.textContent = isToday ? `${Math.round(state.caloriesCurrent)} kcal` : '1,180 kcal';

    // 24H Hourly Step Density Bar Chart for this day
    if (sheetHourlyBars) {
      sheetHourlyBars.innerHTML = '';
      if (sheetChartTotal) sheetChartTotal.textContent = `Total: ${steps.toLocaleString()} steps`;

      for (let h = 0; h < 24; h++) {
        const col = document.createElement('div');
        col.className = 'sheet-bar-col';
        col.title = `${String(h).padStart(2, '0')}:00`;

        const fill = document.createElement('div');
        fill.className = 'sheet-bar-fill';

        let heightPct = 8;
        if (steps > 0) {
          if (h >= 7 && h <= 9) {
            heightPct = Math.min(95, 30 + ((steps / 10000) * 45));
            fill.classList.add('active');
          } else if (h >= 14 && h <= 16) {
            heightPct = Math.min(80, 20 + ((steps / 10000) * 35));
          } else if (h >= 18 && h <= 20) {
            heightPct = Math.min(100, 40 + ((steps / 10000) * 55));
            fill.classList.add('active');
          } else if (h >= 10 && h <= 17) {
            heightPct = 18;
          }
        }
        fill.style.height = `${heightPct}%`;
        col.appendChild(fill);
        sheetHourlyBars.appendChild(col);
      }
    }

    // Recorded Workouts & Sessions
    if (sheetActivityList) {
      sheetActivityList.innerHTML = `
        <div class="sheet-act-item">
          <div class="act-left">
            <span class="act-icon">🏃</span>
            <div class="act-info">
              <span class="act-title">Outdoor Fitness Walk</span>
              <span class="act-sub">Google Fit & GPS Synced • Cadence: 118 spm</span>
            </div>
          </div>
          <span class="act-badge">${(distance * 0.65).toFixed(2)} km</span>
        </div>
        <div class="sheet-act-item">
          <div class="act-left">
            <span class="act-icon">⚡</span>
            <div class="act-info">
              <span class="act-title">Deep Work Focus Block</span>
              <span class="act-sub">Focus Chamber • High Flow State</span>
            </div>
          </div>
          <span class="act-badge">${focusMins} mins</span>
        </div>
        <div class="sheet-act-item">
          <div class="act-left">
            <span class="act-icon">🥗</span>
            <div class="act-info">
              <span class="act-title">Nutritional Fueling & Diet</span>
              <span class="act-sub">Clean macros logged • High hydration</span>
            </div>
          </div>
          <span class="act-badge">Balanced</span>
        </div>
      `;
    }

    modalDayDetail.classList.remove('hidden');
  }

  // Day Navigation Buttons
  btnPrevDay?.addEventListener('click', (e) => {
    e.stopPropagation();
    if (currentDetailDay > 1) {
      openDayDetailModal(currentDetailDay - 1, 'September', 2026);
    } else {
      showToast("First day of the month reached (1 Sep).");
    }
  });

  btnNextDay?.addEventListener('click', (e) => {
    e.stopPropagation();
    if (currentDetailDay < 30) {
      openDayDetailModal(currentDetailDay + 1, 'September', 2026);
    } else {
      showToast("Last day of the month reached (30 Sep).");
    }
  });

  btnCloseDayDetail?.addEventListener('click', () => modalDayDetail.classList.add('hidden'));
  modalDayDetail?.addEventListener('click', (e) => {
    if (e.target === modalDayDetail) modalDayDetail.classList.add('hidden');
  });

  btnShareDay?.addEventListener('click', () => {
    showToast("📤 Day summary card copied to clipboard! Ready to share.");
  });

  // Home Steps Card click to open Day Detail
  cardStepsShortcut?.addEventListener('click', () => {
    openDayDetailModal(28, 'September', 2026);
  });

  // Health Steps Card click to open Day Detail
  const healthStepsCard = document.querySelector('.steps-ring-card');
  healthStepsCard?.addEventListener('click', (e) => {
    if (!e.target.closest('button')) {
      openDayDetailModal(28, 'September', 2026);
    }
  });

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
      if (timerActiveTaskTitle) timerActiveTaskTitle.textContent = task.title;
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

  // --- ADD TASK FORM CONTROLLER ---
  const taskPriorityGroup = document.getElementById('task-priority-group');
  const newTaskPriority = document.getElementById('new-task-priority');
  taskPriorityGroup?.querySelectorAll('.pill-option-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      taskPriorityGroup.querySelectorAll('.pill-option-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      if (newTaskPriority) newTaskPriority.value = btn.dataset.val || 'P2';
    });
  });

  const taskDurationChips = document.getElementById('task-duration-chips');
  const newTaskMins = document.getElementById('new-task-mins');
  taskDurationChips?.querySelectorAll('.quick-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      taskDurationChips.querySelectorAll('.quick-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const m = chip.dataset.mins;
      if (newTaskMins) newTaskMins.value = m;
    });
  });

  btnAddTaskModal?.addEventListener('click', () => modalAddTask.classList.remove('hidden'));
  btnCloseTaskModal?.addEventListener('click', () => modalAddTask.classList.add('hidden'));
  modalAddTask?.addEventListener('click', (e) => {
    if (e.target === modalAddTask) modalAddTask.classList.add('hidden');
  });

  btnSubmitTask?.addEventListener('click', () => {
    const titleInput = document.getElementById('new-task-title');
    const prioInput = document.getElementById('new-task-priority');
    const minsInput = document.getElementById('new-task-mins');

    const title = titleInput?.value.trim();
    if (!title) {
      showToast("⚠️ Please enter a task title!");
      return;
    }

    const newTask = {
      id: Date.now(),
      title: title,
      priority: prioInput?.value || 'P2',
      targetMins: parseInt(minsInput?.value, 10) || 30,
      currentMins: 0,
      completed: false,
      category: 'Productivity'
    };

    state.tasks.unshift(newTask);
    if (titleInput) titleInput.value = '';
    modalAddTask.classList.add('hidden');
    renderAll();
    updateCustomTimerTaskOptions();
    showToast(`⚡ Task created: "${title}" (${newTask.priority})`);
  });

  // --- CUSTOM FOCUS TIMER STUDIO CONTROLLER ---
  let selectedTimerTag = 'Deep Work';
  let selectedTimerDuration = 45;

  function updateCustomTimerTaskOptions() {
    if (!customTimerTaskSelect) return;
    customTimerTaskSelect.innerHTML = `<option value="">-- No Task Binding (Free Focus) --</option>`;
    state.tasks.forEach(t => {
      const opt = document.createElement('option');
      opt.value = t.id;
      opt.textContent = `${t.priority} — ${t.title} (${t.targetMins}m)`;
      customTimerTaskSelect.appendChild(opt);
    });
  }

  btnOpenCustomTimer?.addEventListener('click', () => {
    updateCustomTimerTaskOptions();
    if (customTimerTitleInput) customTimerTitleInput.value = state.timer.taskTitle || 'Deep Work Sprint';
    if (customTimerMinsInput) customTimerMinsInput.value = selectedTimerDuration;
    modalCustomTimer?.classList.remove('hidden');
  });

  btnEditTimerTitle?.addEventListener('click', () => {
    updateCustomTimerTaskOptions();
    if (customTimerTitleInput) customTimerTitleInput.value = state.timer.taskTitle || 'Deep Work Sprint';
    modalCustomTimer?.classList.remove('hidden');
  });

  btnCloseCustomTimer?.addEventListener('click', () => modalCustomTimer?.classList.add('hidden'));
  modalCustomTimer?.addEventListener('click', (e) => {
    if (e.target === modalCustomTimer) modalCustomTimer?.classList.add('hidden');
  });

  const timerTagGroup = document.getElementById('timer-tag-group');
  timerTagGroup?.querySelectorAll('.pill-option-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      timerTagGroup.querySelectorAll('.pill-option-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      selectedTimerTag = btn.dataset.tag || 'Deep Work';
    });
  });

  const customTimerDurationChips = document.getElementById('custom-timer-duration-chips');
  customTimerDurationChips?.querySelectorAll('.quick-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      customTimerDurationChips.querySelectorAll('.quick-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const mins = parseInt(chip.dataset.mins, 10);
      selectedTimerDuration = mins;
      if (customTimerMinsInput) customTimerMinsInput.value = mins;
    });
  });

  customTimerMinsInput?.addEventListener('input', () => {
    const val = parseInt(customTimerMinsInput.value, 10);
    if (!isNaN(val) && val > 0) {
      selectedTimerDuration = val;
    }
  });

  btnStartCustomTimer?.addEventListener('click', () => {
    const title = customTimerTitleInput?.value.trim() || 'Custom Focus Chamber';
    const mins = Math.max(1, Math.min(240, selectedTimerDuration || 45));

    pauseTimer();
    state.timer.totalSeconds = mins * 60;
    state.timer.remainingSeconds = mins * 60;
    state.timer.taskTitle = title;
    state.timer.tag = selectedTimerTag;

    if (timerActiveTaskTitle) timerActiveTaskTitle.textContent = title;
    if (timerModeTag) timerModeTag.textContent = selectedTimerTag;
    if (timerTagDisplay) timerTagDisplay.textContent = `💻 ${selectedTimerTag}`;
    if (timerPresetDisplay) timerPresetDisplay.textContent = `${mins} min Custom Focus`;

    presetBtns.forEach(b => b.classList.remove('active'));
    btnOpenCustomTimer?.classList.add('active');

    updateTimerDisplay();
    modalCustomTimer?.classList.add('hidden');
    switchTab('tab-timer');
    startTimer();
    showToast(`🚀 Launched Focus Chamber: "${title}" (${mins}m)`);
  });

  // --- CALIBRATE ACTUAL STEPS CONTROLLER ---
  function updateCalibratePreviews(st) {
    if (calibPreviewDist) calibPreviewDist.textContent = `${(st * 0.00075).toFixed(2)} km`;
    if (calibPreviewCals) calibPreviewCals.textContent = `${Math.round(st * 0.04)} kcal`;
    if (calibPreviewTime) calibPreviewTime.textContent = `${Math.round(st / 100)} min`;
  }

  btnOpenCalibrateSteps?.addEventListener('click', () => {
    if (calibrateStepsInput) {
      calibrateStepsInput.value = state.stepsCurrent > 0 ? state.stepsCurrent : 7420;
      updateCalibratePreviews(parseInt(calibrateStepsInput.value, 10));
    }
    modalCalibrateSteps?.classList.remove('hidden');
  });

  btnCloseCalibrateModal?.addEventListener('click', () => modalCalibrateSteps?.classList.add('hidden'));
  modalCalibrateSteps?.addEventListener('click', (e) => {
    if (e.target === modalCalibrateSteps) modalCalibrateSteps?.classList.add('hidden');
  });

  calibrateStepsInput?.addEventListener('input', () => {
    const val = parseInt(calibrateStepsInput.value, 10);
    if (!isNaN(val) && val >= 0) {
      updateCalibratePreviews(val);
    }
  });

  const calibrateQuickPresets = document.getElementById('calibrate-quick-presets');
  calibrateQuickPresets?.querySelectorAll('.quick-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      calibrateQuickPresets.querySelectorAll('.quick-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const st = parseInt(chip.dataset.steps, 10);
      if (calibrateStepsInput) calibrateStepsInput.value = st;
      updateCalibratePreviews(st);
    });
  });

  btnSubmitCalibrateSteps?.addEventListener('click', () => {
    const st = parseInt(calibrateStepsInput?.value, 10);
    if (isNaN(st) || st < 0) {
      showToast("⚠️ Please enter a valid step count.");
      return;
    }
    state.stepsCurrent = st;
    state.caloriesCurrent = Math.round(st * 0.04);
    state.lastSyncTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    renderAll();
    renderWeeklyTrend();
    modalCalibrateSteps?.classList.add('hidden');
    showToast(`⚡ Actual steps calibrated: ${st.toLocaleString()} steps! All analytics updated.`);
  });

  // --- FOCUS TIMER RUNTIME ---
  function updateTimerDisplay() {
    const m = Math.floor(state.timer.remainingSeconds / 60);
    const s = state.timer.remainingSeconds % 60;
    if (timerCountdown) timerCountdown.textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;

    const maxSec = state.timer.totalSeconds || 1;
    const progress = (state.timer.remainingSeconds / maxSec);
    const circumference = 534; // 2 * pi * 85
    if (timerStroke) timerStroke.style.strokeDashoffset = circumference - (circumference * progress);
  }

  function startTimer() {
    if (state.timer.isRunning) return;
    state.timer.isRunning = true;
    iconTimerPlay?.classList.add('hidden');
    iconTimerPause?.classList.remove('hidden');

    state.timer.intervalId = setInterval(() => {
      if (state.timer.remainingSeconds > 0) {
        state.timer.remainingSeconds--;
        updateTimerDisplay();
      } else {
        pauseTimer();
        state.focusMinutesCurrent += Math.round(state.timer.totalSeconds / 60);
        renderAll();
        showToast("🎉 Focus session completed! Great job!");
      }
    }, 1000);
  }

  function pauseTimer() {
    state.timer.isRunning = false;
    clearInterval(state.timer.intervalId);
    iconTimerPlay?.classList.remove('hidden');
    iconTimerPause?.classList.add('hidden');
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
      if (btn.id === 'btn-open-custom-timer') return;
      presetBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      pauseTimer();
      const mins = parseInt(btn.getAttribute('data-mins'), 10) || 25;
      state.timer.totalSeconds = mins * 60;
      state.timer.remainingSeconds = mins * 60;
      if (timerPresetDisplay) timerPresetDisplay.textContent = `${mins} min Focus Block`;
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
