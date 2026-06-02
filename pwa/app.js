// ============================================================
// 531 Routine PWA — ported from Java Swing WorkoutFrame
// ============================================================

// ---------- Constants ----------
const WARMUP_PCT = [0.40, 0.50, 0.60];
const DELOAD_PCT = [0.40, 0.50, 0.60];
const WEEK1_PCT  = [0.65, 0.75, 0.85];
const WEEK2_PCT  = [0.70, 0.80, 0.90];
const WEEK3_PCT  = [0.75, 0.85, 0.95];

const WARMUP_REPS = [5, 5, 3];
const WEEK_REPS   = [[5,5,5], [3,3,3], [5,3,1]];
const DELOAD_REPS = [5, 5, 5];

const INCREMENT_LOWER = 5.0;
const INCREMENT_UPPER = 2.5;

const LIFTS = [
  { key: "squat",    label: "스쿼트",         lower: true  },
  { key: "deadlift", label: "데드리프트",     lower: true  },
  { key: "bench",    label: "벤치프레스",     lower: false },
  { key: "press",    label: "오버헤드프레스", lower: false },
];

const STORAGE_KEY = "wendler531.state.v1";
const HISTORY_KEY = "wendler531.history.v1";
const LOG_KEY     = "wendler531.log.v1";

// ---------- Phase helpers ----------
function phaseIsIncreased(p) { return p !== "FIRST_THREE"; }
function phaseIsDeload(p)    { return p === "DELOAD"; }
function phaseFirstWeek(p) {
  if (p === "FIRST_THREE") return 1;
  if (p === "LAST_THREE")  return 4;
  return 7;
}
function phaseLabel(p) {
  if (p === "FIRST_THREE") return "전3주 (1~3주차)";
  if (p === "LAST_THREE")  return "후3주 (4~6주차)";
  return "디로딩 (7주차)";
}

// ---------- Math ----------
function roundTo2_5(v) { return Math.round(v / 2.5) * 2.5; }
function tmFor(oneRM)  { return oneRM * 0.9; }
function tmForPhase(baseTM, lower, phase) {
  if (phaseIsIncreased(phase)) {
    return baseTM + (lower ? INCREMENT_LOWER : INCREMENT_UPPER);
  }
  return baseTM;
}
function pctSets(tm, pcts) { return pcts.map(p => roundTo2_5(tm * p)); }
function estimateOneRM(weight, reps) {
  if (weight <= 0 || reps <= 0) return 0;
  return weight * (1 + reps / 30);
}
function fmtKg(v) { return `${v.toFixed(1)} kg`; }
function fmtCell(w, r, amrap) {
  return `${w.toFixed(1)} × ${r}${amrap ? "+" : ""}`;
}

// ---------- DOM helpers ----------
const $ = (id) => document.getElementById(id);
function show(el)  { el.classList.remove("hidden"); }
function hide(el)  { el.classList.add("hidden"); }
function showToast(msg) {
  const t = $("toast");
  t.textContent = msg;
  show(t);
  clearTimeout(t._timer);
  t._timer = setTimeout(() => hide(t), 1800);
}

// ---------- State ----------
function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch { return null; }
}
function saveState(state) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}
function readInputs() {
  const num = (id) => {
    const v = parseFloat($(id).value);
    return isNaN(v) ? 0 : v;
  };
  return {
    squat:    num("in-squat"),
    deadlift: num("in-deadlift"),
    bench:    num("in-bench"),
    press:    num("in-press"),
    phase:    $("sel-phase").value,
    assistance: $("sel-assist").value,
  };
}
function writeInputs(s) {
  const set = (id, v) => { if (v && v > 0) $(id).value = v; };
  set("in-squat",    s.squat);
  set("in-deadlift", s.deadlift);
  set("in-bench",    s.bench);
  set("in-press",    s.press);
  if (s.phase)      $("sel-phase").value  = s.phase;
  if (s.assistance) $("sel-assist").value = s.assistance;
}

// ---------- History (manual PR records) ----------
function loadHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch { return []; }
}
function saveHistory(list) {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list));
}
function appendHistory(entry) {
  const list = loadHistory();
  list.push(entry);
  saveHistory(list);
}

// ---------- Set completion log ----------
function loadLog() {
  try {
    const raw = localStorage.getItem(LOG_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch { return []; }
}
function saveLog(list) {
  localStorage.setItem(LOG_KEY, JSON.stringify(list));
}
function todayISO() {
  // Local date (so completing a set at 1am still counts as today)
  const d = new Date();
  const tz = d.getTimezoneOffset() * 60000;
  return new Date(d - tz).toISOString().slice(0, 10);
}
function findLogIndex(log, date, lift, phase, row, idx) {
  return log.findIndex(e =>
    e.date === date && e.lift === lift &&
    e.phase === phase && e.row === row && e.setIdx === idx);
}
function isSetCompleted(lift, phase, row, idx) {
  const log = loadLog();
  return findLogIndex(log, todayISO(), lift, phase, row, idx) >= 0;
}
function toggleSet(lift, phase, row, idx, weight, reps, amrap) {
  const log = loadLog();
  const date = todayISO();
  const i = findLogIndex(log, date, lift, phase, row, idx);
  if (i >= 0) {
    log.splice(i, 1);
    saveLog(log);
    return false;
  }
  log.push({
    date, lift, phase, row, setIdx: idx,
    weight, reps, amrap,
    completedAt: new Date().toISOString(),
  });
  saveLog(log);
  return true;
}

// ---------- Render result cards ----------
function assistanceText(name, lift, tm) {
  if (!name || name === "None") return "";
  if (name === "FSL") {
    const w = roundTo2_5(tm * WEEK1_PCT[0]);
    return `보조 FSL · ${w.toFixed(1)} kg × 8회 × 5세트`;
  }
  if (name === "SSL") {
    const w = roundTo2_5(tm * WEEK1_PCT[1]);
    const reps = lift.lower ? 4 : 8;
    return `보조 SSL · ${w.toFixed(1)} kg × ${reps}회 × 5세트`;
  }
  if (name === "BBB") {
    const w = roundTo2_5(tm * 0.5);
    const reps = lift.lower ? 5 : 10;
    return `보조 BBB · ${w.toFixed(1)} kg × ${reps}회 × 5세트`;
  }
  return "";
}

function setCell(lift, phase, row, idx, weight, reps, amrap) {
  const completed = isSetCompleted(lift.key, phase, row, idx);
  const classes = ["set-cell"];
  if (amrap)     classes.push("amrap");
  if (completed) classes.push("completed");
  return `
    <td class="${classes.join(" ")}"
        data-lift="${lift.key}" data-row="${row}" data-idx="${idx}"
        data-weight="${weight}" data-reps="${reps}" data-amrap="${amrap}">
      <div class="set-text">${fmtCell(weight, reps, amrap)}</div>
      <div class="set-mark" aria-hidden="true">✓</div>
    </td>`;
}

function renderLiftCard(lift, oneRM, phase, assistance) {
  const baseTM = tmFor(oneRM);
  const tm = tmForPhase(baseTM, lift.lower, phase);

  const warmup = pctSets(tm, WARMUP_PCT);
  const deload = phaseIsDeload(phase) ? pctSets(tm, DELOAD_PCT) : null;
  const weeks = phaseIsDeload(phase)
    ? [deload]
    : [pctSets(tm, WEEK1_PCT), pctSets(tm, WEEK2_PCT), pctSets(tm, WEEK3_PCT)];

  const startWeek = phaseFirstWeek(phase);

  let tableHTML = `
    <table class="lift-table">
      <thead>
        <tr><th></th><th>Set 1</th><th>Set 2</th><th>Set 3</th></tr>
      </thead>
      <tbody>
        <tr>
          <td class="row-label">워밍업</td>
          ${setCell(lift, phase, "warmup", 0, warmup[0], WARMUP_REPS[0], false)}
          ${setCell(lift, phase, "warmup", 1, warmup[1], WARMUP_REPS[1], false)}
          ${setCell(lift, phase, "warmup", 2, warmup[2], WARMUP_REPS[2], false)}
        </tr>
  `;

  if (phaseIsDeload(phase)) {
    tableHTML += `
      <tr>
        <td class="row-label">디로딩</td>
        ${setCell(lift, phase, "deload", 0, deload[0], DELOAD_REPS[0], false)}
        ${setCell(lift, phase, "deload", 1, deload[1], DELOAD_REPS[1], false)}
        ${setCell(lift, phase, "deload", 2, deload[2], DELOAD_REPS[2], false)}
      </tr>
    `;
  } else {
    for (let w = 0; w < 3; w++) {
      const sets = weeks[w];
      const reps = WEEK_REPS[w];
      const rowKey = `week${w + 1}`;
      tableHTML += `
        <tr>
          <td class="row-label">${startWeek + w}주차</td>
          ${setCell(lift, phase, rowKey, 0, sets[0], reps[0], false)}
          ${setCell(lift, phase, rowKey, 1, sets[1], reps[1], false)}
          ${setCell(lift, phase, rowKey, 2, sets[2], reps[2], true)}
        </tr>
      `;
    }
  }
  tableHTML += `</tbody></table>`;

  const footer = phaseIsDeload(phase)
    ? "디로딩 주차 · 보조 운동 생략"
    : assistanceText(assistance, lift, tm);

  return `
    <div class="lift-card">
      <div class="lift-header">
        <div class="lift-title">${lift.label}</div>
        <div class="lift-meta">
          <span class="lift-tm">TM ${tm.toFixed(1)} kg</span>
          <span class="pill">${phaseLabel(phase)}</span>
        </div>
      </div>
      ${tableHTML}
      <div class="lift-footer">${footer || "&nbsp;"}</div>
    </div>
  `;
}

function renderResults(state) {
  const html = LIFTS
    .map(l => renderLiftCard(l, state[l.key], state.phase, state.assistance))
    .join("");
  $("results").innerHTML = html;
}

// ---------- Calculate / reset / toggle ----------
function onCalculate() {
  const s = readInputs();
  if (s.squat <= 0 && s.deadlift <= 0 && s.bench <= 0 && s.press <= 0) {
    showToast("1RM을 하나 이상 입력하세요");
    return;
  }
  saveState(s);
  renderResults(s);
  hideInput();
}
function onReset() {
  ["in-squat","in-deadlift","in-bench","in-press"].forEach(id => $(id).value = "");
  $("sel-phase").value  = "FIRST_THREE";
  $("sel-assist").value = "None";
  $("results").innerHTML = "";
  localStorage.removeItem(STORAGE_KEY);
  showInput();
  showToast("초기화 완료");
}
function hideInput() {
  hide($("input-panel"));
  show($("btn-edit"));
}
function showInput() {
  show($("input-panel"));
  hide($("btn-edit"));
}

// ---------- Rest Timer ----------
const timer = {
  total: 0,
  remaining: 0,
  intervalId: null,
  endTs: 0,

  start(seconds) {
    this.stop();
    this.total = seconds;
    this.remaining = seconds;
    this.endTs = Date.now() + seconds * 1000;
    this.render();
    this.intervalId = setInterval(() => this.tick(), 200);
  },
  stop() {
    if (this.intervalId) clearInterval(this.intervalId);
    this.intervalId = null;
    this.remaining = 0;
    this.total = 0;
    this.render();
  },
  tick() {
    const left = Math.max(0, (this.endTs - Date.now()) / 1000);
    this.remaining = left;
    this.render();
    if (left <= 0) {
      clearInterval(this.intervalId);
      this.intervalId = null;
      this.onFinish();
    }
  },
  render() {
    const left = Math.ceil(this.remaining);
    const mm = String(Math.floor(left / 60)).padStart(2, "0");
    const ss = String(left % 60).padStart(2, "0");
    $("timer-time").textContent = `${mm}:${ss}`;
    const pct = this.total > 0 ? (1 - this.remaining / this.total) * 100 : 0;
    $("timer-bar").style.width = `${pct}%`;
  },
  onFinish() {
    if (navigator.vibrate) navigator.vibrate([300, 120, 300, 120, 600]);
    beep();
    showToast("휴식 끝!");
  }
};

let audioCtx = null;
function beep() {
  try {
    if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    if (audioCtx.state === "suspended") audioCtx.resume();
    const o = audioCtx.createOscillator();
    const g = audioCtx.createGain();
    o.type = "sine";
    o.frequency.value = 880;
    o.connect(g); g.connect(audioCtx.destination);
    g.gain.setValueAtTime(0.0001, audioCtx.currentTime);
    g.gain.exponentialRampToValueAtTime(0.4, audioCtx.currentTime + 0.02);
    g.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + 0.7);
    o.start();
    o.stop(audioCtx.currentTime + 0.7);
  } catch {}
}

// ---------- Record dialog ----------
function recalcEstimate() {
  const w = parseFloat($("rec-weight").value) || 0;
  const r = parseInt($("rec-reps").value, 10) || 0;
  const est = estimateOneRM(w, r);
  $("rec-estimate").textContent = est > 0 ? fmtKg(est) : "—";
  return est;
}
function onSaveRecord() {
  const w = parseFloat($("rec-weight").value) || 0;
  const r = parseInt($("rec-reps").value, 10) || 0;
  if (w <= 0 || r <= 0) { showToast("무게와 반복 횟수를 입력하세요"); return; }
  const est = estimateOneRM(w, r);
  const lift = $("rec-lift").value;
  const date = new Date().toISOString().slice(0, 10);
  appendHistory({ date, lift, weight: w, reps: r, estimate: est });
  showToast("기록 저장됨");
}
function onApplyRecord() {
  const est = recalcEstimate();
  if (est <= 0) { showToast("무게와 반복 횟수를 입력하세요"); return; }
  const lift = $("rec-lift").value;
  const rounded = roundTo2_5(est);
  $(`in-${lift}`).value = rounded;
  showInput();
  showToast(`1RM 반영: ${rounded} kg`);
  hide($("record-overlay"));
}

// ---------- History view (daily grouped) ----------
function liftLabel(k) { return (LIFTS.find(l => l.key === k) || {}).label || k; }
function rowLabel(row) {
  if (row === "warmup") return "워밍업";
  if (row === "deload") return "디로딩";
  const m = row.match(/^week(\d+)$/);
  if (m) return `${m[1]}주차`;
  return row;
}
function rowOrder(row) {
  if (row === "warmup") return 0;
  if (row === "deload") return 5;
  const m = row.match(/^week(\d+)$/);
  if (m) return parseInt(m[1], 10);
  return 9;
}

function aggregateByDay() {
  const setLog = loadLog();
  const prs    = loadHistory();
  const days = {};
  const touch = (d) => { if (!days[d]) days[d] = { date: d, sets: [], prs: [] }; return days[d]; };
  setLog.forEach(e => touch(e.date).sets.push(e));
  prs.forEach(e => touch(e.date).prs.push(e));
  return Object.values(days).sort((a, b) => b.date.localeCompare(a.date));
}

function renderHistory() {
  // Always start at list view
  $("history-detail-view").classList.add("hidden");
  $("history-list-view").classList.remove("hidden");
  $("btn-history-back").classList.add("hidden");
  $("history-title").textContent = "운동 기록";

  const days = aggregateByDay();
  const list = $("day-list");
  if (days.length === 0) {
    list.innerHTML = "";
    show($("history-empty"));
    return;
  }
  hide($("history-empty"));

  list.innerHTML = days.map(day => {
    // Per-lift set count for the day
    const liftCounts = {};
    day.sets.forEach(s => { liftCounts[s.lift] = (liftCounts[s.lift] || 0) + 1; });
    const liftSummary = LIFTS
      .filter(l => liftCounts[l.key])
      .map(l => `${l.label} ${liftCounts[l.key]}세트`)
      .join(" · ");
    const prCount = day.prs.length;
    const summary = [
      liftSummary,
      prCount > 0 ? `PR ${prCount}건` : "",
    ].filter(Boolean).join(" · ") || "기록 없음";
    return `
      <button class="day-row" data-date="${day.date}">
        <div class="day-date">${day.date}</div>
        <div class="day-summary">${summary}</div>
      </button>
    `;
  }).join("");
}

function renderDayDetail(date) {
  $("history-list-view").classList.add("hidden");
  $("history-detail-view").classList.remove("hidden");
  $("btn-history-back").classList.remove("hidden");
  $("history-title").textContent = date;

  const days = aggregateByDay();
  const day = days.find(d => d.date === date);
  if (!day) {
    $("history-detail-view").innerHTML = `<div class="empty-state">기록을 찾을 수 없습니다.</div>`;
    return;
  }

  // Group sets by lift then by row
  const byLift = {};
  day.sets.forEach(s => {
    if (!byLift[s.lift]) byLift[s.lift] = {};
    if (!byLift[s.lift][s.row]) byLift[s.lift][s.row] = [];
    byLift[s.lift][s.row].push(s);
  });

  let html = "";
  LIFTS.forEach(lift => {
    if (!byLift[lift.key]) return;
    const rows = byLift[lift.key];
    const rowKeys = Object.keys(rows).sort((a, b) => rowOrder(a) - rowOrder(b));
    let rowsHTML = rowKeys.map(rk => {
      const sets = rows[rk].slice().sort((a, b) => a.setIdx - b.setIdx);
      const cells = sets.map(s =>
        `<span class="detail-set">${s.weight.toFixed(1)} × ${s.reps}${s.amrap ? "+" : ""}</span>`
      ).join("");
      return `
        <div class="detail-row">
          <div class="detail-row-label">${rowLabel(rk)}</div>
          <div class="detail-row-sets">${cells}</div>
        </div>`;
    }).join("");
    html += `
      <div class="detail-lift-card">
        <div class="detail-lift-title">${lift.label}</div>
        ${rowsHTML}
      </div>`;
  });

  if (day.prs.length > 0) {
    const prRows = day.prs.map(p => `
      <div class="detail-row">
        <div class="detail-row-label">${liftLabel(p.lift)}</div>
        <div class="detail-row-sets">
          <span class="detail-set">${p.weight.toFixed(1)} × ${p.reps}</span>
          <span class="detail-pr">추정 1RM ${p.estimate.toFixed(1)} kg</span>
        </div>
      </div>`).join("");
    html += `
      <div class="detail-lift-card">
        <div class="detail-lift-title">PR 기록</div>
        ${prRows}
      </div>`;
  }

  if (!html) html = `<div class="empty-state">이 날 기록이 없습니다.</div>`;

  $("history-detail-view").innerHTML = html;
}

// ---------- Backup ----------
function exportData() {
  const data = {
    state:   loadState(),
    history: loadHistory(),
    log:     loadLog(),
    exportedAt: new Date().toISOString(),
  };
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `531-backup-${data.exportedAt.slice(0,10)}.json`;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  showToast("내보내기 완료");
}
function importData(file) {
  const reader = new FileReader();
  reader.onload = () => {
    try {
      const data = JSON.parse(reader.result);
      if (data.state)   saveState(data.state);
      if (data.history) saveHistory(data.history);
      if (data.log)     saveLog(data.log);
      if (data.state) {
        writeInputs(data.state);
        renderResults(data.state);
        hideInput();
      }
      renderHistory();
      showToast("가져오기 완료");
      hide($("menu-overlay"));
    } catch (e) {
      showToast("잘못된 파일입니다");
    }
  };
  reader.readAsText(file);
}
function clearAll() {
  if (!confirm("저장된 모든 데이터를 지웁니다. 계속할까요?")) return;
  localStorage.removeItem(STORAGE_KEY);
  localStorage.removeItem(HISTORY_KEY);
  localStorage.removeItem(LOG_KEY);
  onReset();
  renderHistory();
  hide($("menu-overlay"));
  showToast("초기화됨");
}

// ---------- Bindings ----------
function bindEvents() {
  $("btn-calc").addEventListener("click", onCalculate);
  $("btn-reset").addEventListener("click", onReset);
  $("btn-edit").addEventListener("click", showInput);

  // Live re-render when phase/assistance changes (if we already have results)
  ["sel-phase", "sel-assist"].forEach(id => {
    $(id).addEventListener("change", () => {
      const s = readInputs();
      saveState(s);
      if ($("results").children.length > 0) renderResults(s);
    });
  });

  // Tap a set cell to toggle completion
  $("results").addEventListener("click", (e) => {
    const cell = e.target.closest(".set-cell");
    if (!cell) return;
    const lift   = cell.dataset.lift;
    const row    = cell.dataset.row;
    const idx    = parseInt(cell.dataset.idx, 10);
    const weight = parseFloat(cell.dataset.weight);
    const reps   = parseInt(cell.dataset.reps, 10);
    const amrap  = cell.dataset.amrap === "true";
    const phase  = $("sel-phase").value;
    const nowChecked = toggleSet(lift, phase, row, idx, weight, reps, amrap);
    cell.classList.toggle("completed", nowChecked);
    if (navigator.vibrate) navigator.vibrate(15);
  });

  // Top bar
  $("btn-timer").addEventListener("click",   () => show($("timer-overlay")));
  $("btn-history").addEventListener("click", () => { renderHistory(); show($("history-overlay")); });
  $("btn-menu").addEventListener("click",    () => show($("menu-overlay")));

  // History day drill-in
  $("day-list").addEventListener("click", (e) => {
    const row = e.target.closest(".day-row");
    if (!row) return;
    renderDayDetail(row.dataset.date);
  });
  $("btn-history-back").addEventListener("click", renderHistory);

  // Close buttons
  document.querySelectorAll(".close-btn").forEach(b => {
    b.addEventListener("click", () => hide($(b.dataset.close)));
  });
  // Click outside sheet to close
  document.querySelectorAll(".overlay").forEach(o => {
    o.addEventListener("click", (e) => { if (e.target === o) hide(o); });
  });

  // Timer
  $("timer-start").addEventListener("click", () => {
    const sec = parseInt($("timer-duration").value, 10) || 90;
    timer.start(sec);
  });
  $("timer-stop").addEventListener("click", () => timer.stop());

  // Record
  $("btn-add-record").addEventListener("click", () => {
    hide($("history-overlay"));
    $("rec-weight").value = "";
    $("rec-reps").value = "";
    $("rec-estimate").textContent = "—";
    show($("record-overlay"));
  });
  $("rec-weight").addEventListener("input", recalcEstimate);
  $("rec-reps").addEventListener("input", recalcEstimate);
  $("rec-save").addEventListener("click", onSaveRecord);
  $("rec-apply").addEventListener("click", onApplyRecord);

  // Menu
  $("m-export").addEventListener("click", exportData);
  $("m-import").addEventListener("click", () => $("import-file").click());
  $("import-file").addEventListener("change", (e) => {
    const f = e.target.files[0];
    if (f) importData(f);
    e.target.value = "";
  });
  $("m-clear").addEventListener("click", clearAll);
}

// ---------- Boot ----------
function boot() {
  bindEvents();
  const state = loadState();
  if (state) {
    writeInputs(state);
    renderResults(state);
    hideInput();
  } else {
    showInput();
  }

  // Service worker
  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("sw.js").catch(() => {});
    });
  }
}

document.addEventListener("DOMContentLoaded", boot);
