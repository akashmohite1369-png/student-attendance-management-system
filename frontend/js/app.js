const API_BASE = (window.ATTENDLY_CONFIG?.API_BASE || ((location.hostname === 'localhost' || location.hostname === '127.0.0.1') ? 'http://localhost:8080/api' : '/api')).replace(/\/+$/, '');

let subjects = [];
let students = [];
let selectedRole = 'STUDENT';
let authMode = 'LOGIN';
let currentUser = null;
let activePage = 'Overview';
let attendanceDraft = {};
let loading = false;

const $ = id => document.getElementById(id);
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const today = () => new Date().toISOString().slice(0,10);
const initials = name => String(name || '').split(' ').filter(Boolean).map(p => p[0]).slice(0,2).join('').toUpperCase() || 'ST';
const api = async (path, options = {}) => {
  const response = await fetch(API_BASE + path, {
    ...options,
    headers: {'Content-Type':'application/json', ...(options.headers || {})}
  });
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (!response.ok) {
    const message = data?.message || data?.error || (typeof data === 'string' ? data : 'Request failed');
    throw new Error(message);
  }
  return data;
};

async function loadMasterData() {
  [subjects, students] = await Promise.all([api('/subjects'), api('/students')]);
}

function loadingView(message='Connecting to Java backend…') {
  $('app').innerHTML = `<div style="min-height:100vh;display:grid;place-items:center;background:#f5f7fb;color:#173957;font-family:Inter,system-ui"><div style="background:#fff;border:1px solid #e3e9f0;border-radius:16px;padding:28px 34px;box-shadow:0 15px 40px #0000000d;text-align:center"><div style="font-size:26px;margin-bottom:10px">◌</div><strong>${esc(message)}</strong><div style="font-size:11px;color:#75849a;margin-top:6px">Please keep Spring Boot running on port 8080.</div></div></div>`;
}

function loginView(errorMessage='') {
  $('app').innerHTML = `<section class="login-shell">
    <div class="login-art"><div class="brand brand-light"><span class="brand-mark">A</span><span>attendly</span></div>
      <div class="art-copy"><span class="eyebrow">ENGINEERING · SEMESTER 03</span><h1>Every class<br>counts.</h1><p>A calmer, clearer way to keep your semester on track.</p>
        <div class="art-stat"><div class="art-stat-icon">✓</div><div><strong>75% attendance goal</strong><span>Know where you stand, at a glance.</span></div></div>
      </div><div class="art-orbit orbit-one"></div><div class="art-orbit orbit-two"></div><div class="art-footer">STUDENT ATTENDANCE MANAGEMENT SYSTEM</div>
    </div>
    <div class="login-panel"><div class="mobile-brand brand"><span class="brand-mark">A</span><span>attendly</span></div>
      <div class="login-intro"><span class="eyebrow">${authMode==='LOGIN'?'WELCOME BACK':(selectedRole==='TEACHER'?'NEW FACULTY':'NEW STUDENT')}</span><h2>${authMode==='LOGIN'?'Sign in to your space':`Create your ${selectedRole==='TEACHER'?'faculty':'student'} account`}</h2><p>${authMode==='LOGIN'?'Use your college email to continue.':'Register once, then sign in from any device.'}</p></div>
      <div class="role-switch"><button type="button" class="role-option ${selectedRole==='STUDENT'?'active':''}" data-role="STUDENT">Student</button><button type="button" class="role-option ${selectedRole==='TEACHER'?'active':''}" data-role="TEACHER">Teacher</button></div>
      ${authMode==='REGISTER' ? `<form id="register-form">
        <label for="reg-name">Full name</label><div class="input-wrap"><span class="input-icon">♙</span><input id="reg-name" type="text" placeholder="Your full name" autocomplete="name" required></div>
        ${selectedRole==='STUDENT' ? '<label for="reg-roll">Roll number</label><div class="input-wrap"><span class="input-icon">#</span><input id="reg-roll" type="text" placeholder="e.g. 23CS006" autocomplete="off" required></div>' : ''}
        <label for="reg-email">College email</label><div class="input-wrap"><span class="input-icon">✉</span><input id="reg-email" type="email" placeholder="you@college.edu" autocomplete="email" required></div>
        <label for="reg-password">Password</label><div class="input-wrap"><span class="input-icon">⌑</span><input id="reg-password" type="password" placeholder="At least 6 characters" autocomplete="new-password" minlength="6" required></div>
        <label for="reg-confirm">Confirm password</label><div class="input-wrap"><span class="input-icon">⌑</span><input id="reg-confirm" type="password" placeholder="Re-enter your password" autocomplete="new-password" minlength="6" required></div>
        <div class="form-row"><span class="secure-note">● Password stored securely</span></div>
        <button class="btn btn-primary btn-wide" type="submit">Create account <span>↗</span></button><p class="login-error" id="register-error" aria-live="polite">${esc(errorMessage)}</p>
      </form>` : `<form id="login-form">
        <label for="email">College email</label><div class="input-wrap"><span class="input-icon">✉</span><input id="email" type="email" placeholder="you@college.edu" autocomplete="username" required></div>
        <label for="password">Password</label><div class="input-wrap"><span class="input-icon">⌑</span><input id="password" type="password" placeholder="Enter your password" autocomplete="current-password" required></div>
        <div class="form-row"><label class="check-label"><input type="checkbox" id="remember"> <span>Remember me</span></label><span class="secure-note">● Secure portal</span></div>
        <button class="btn btn-primary btn-wide" type="submit">Sign in <span>↗</span></button><p class="login-error" id="login-error" aria-live="polite">${esc(errorMessage)}</p>
      </form>`}
      <div class="demo-note"><span class="demo-dot"></span><div><strong>${authMode==='LOGIN'?'Existing demo accounts':(selectedRole==='TEACHER'?'Faculty registration':'Student registration')}</strong><p>${authMode==='LOGIN' ? 'Student: student@college.edu / student123<br>Teacher: teacher@college.edu / teacher123<br><br>Students and faculty can create their own account below.' : (selectedRole==='TEACHER' ? 'Use your name, college email and password. The faculty account is saved in H2 through the Java API.' : 'Use your name, roll number, college email and password. The student account is saved in H2 through the Java API.')}</p></div></div>
      <button type="button" id="auth-mode-toggle" class="text-link" style="margin-top:12px">${authMode==='LOGIN' && selectedRole==='STUDENT'?'New student? Create an account':'Back to sign in'}</button>
      <p class="login-legal">Passwords are checked by the Spring Boot API and stored as secure password hashes.</p>
    </div>
  </section>`;
  document.querySelectorAll('[data-role]').forEach(button => button.onclick = () => { selectedRole = button.dataset.role; loginView(); });
  $('auth-mode-toggle').onclick = () => { authMode = authMode==='LOGIN'?'REGISTER':'LOGIN'; loginView(); };
  if (authMode==='REGISTER') $('register-form').onsubmit = handleRegister;
  else $('login-form').onsubmit = handleLogin;
}

async function handleLogin(event) {
  event.preventDefault();
  const email = $('email').value.trim();
  const password = $('password').value;
  if (!email || !password) { $('login-error').textContent = 'Enter your email and password.'; return; }
  $('login-error').textContent = 'Signing in…';
  try {
    const result = await api('/auth/login', {method:'POST', body:JSON.stringify({email, password, role:selectedRole})});
    currentUser = result;
    if (result.role === 'STUDENT') {
      if (!result.studentId) throw new Error('Student profile not found for this email.');
      const student = students.find(s => Number(s.id) === Number(result.studentId)) || students.find(s => s.email.toLowerCase() === result.email.toLowerCase());
      if (!student) throw new Error('Student profile not found for this email.');
      currentUser.studentId = result.studentId || student.id;
      currentUser.student = student;
    }
    authMode = 'LOGIN';
    activePage = 'Overview';
    await render();
  } catch (error) {
    $('login-error').textContent = error.message || 'Unable to sign in.';
  }
}

async function handleRegister(event) {
  event.preventDefault();
  const name = $('reg-name').value.trim();
  const rollNumber = selectedRole==='STUDENT' ? $('reg-roll').value.trim() : '';
  const email = $('reg-email').value.trim();
  const password = $('reg-password').value;
  const confirmPassword = $('reg-confirm').value;
  const error = $('register-error');
  if (!name || (selectedRole==='STUDENT' && !rollNumber) || !email || !password || !confirmPassword) { error.textContent = 'Complete all required fields.'; return; }
  if (password !== confirmPassword) { error.textContent = 'Passwords do not match.'; return; }
  error.textContent = 'Creating account…';
  try {
    const result = await api('/auth/register', {method:'POST', body:JSON.stringify({name, rollNumber, email, password, confirmPassword, role:selectedRole})});
    await loadMasterData();
    currentUser = result;
    if (result.role === 'STUDENT') {
      currentUser.studentId = result.studentId;
      currentUser.student = students.find(s => Number(s.id) === Number(result.studentId)) || students.find(s => s.email.toLowerCase() === result.email.toLowerCase());
    }
    selectedRole = result.role;
    authMode = 'LOGIN';
    activePage = 'Overview';
    await render();
    showToast('Account created successfully.');
  } catch (apiError) {
    error.textContent = apiError.message || 'Unable to create the account.';
  }
}

function shell(content) {
  const teacher = currentUser.role === 'TEACHER';
  const nav = teacher ? [['▦','Overview'],['✓','Attendance'],['▤','Reports']] : [['▦','Overview'],['▤','My Attendance'],['⌁','Check-in']];
  $('app').innerHTML = `<div class="dashboard"><aside class="sidebar"><div class="brand"><span class="brand-mark">A</span><span>attendly</span></div><div class="side-label">WORKSPACE</div><nav class="side-nav">${nav.map(([ico,label])=>`<button class="nav-item ${activePage===label?'active':''}" data-page="${label}"><span class="nav-ico">${ico}</span>${label}</button>`).join('')}</nav><div class="sidebar-bottom"><div class="side-label">ACCOUNT</div><button class="nav-item" id="logout"><span class="nav-ico">↪</span>Sign out</button><div class="profile-mini"><div class="avatar">${initials(currentUser.name)}</div><div><strong>${esc(currentUser.name)}</strong><small>${teacher?'Faculty account':'Student account'}</small></div></div></div></aside><main class="main-area"><header class="topbar"><div class="crumb">Workspace / <strong>${activePage}</strong></div><div class="top-actions"><span class="date-pill">◷ &nbsp; ${new Date().toLocaleDateString('en-IN',{day:'2-digit',month:'short',year:'numeric'})}</span><button class="icon-btn" id="top-logout" title="Sign out">↗</button></div></header><div class="content">${content}</div></main></div>`;
  document.querySelectorAll('[data-page]').forEach(button => button.onclick = () => { activePage = button.dataset.page; render(); });
  $('logout').onclick = $('top-logout').onclick = () => { currentUser = null; selectedRole = 'STUDENT'; loginView(); };
}

function statCard(icon,label,value,foot,kind='') { return `<article class="stat-card"><div class="stat-top"><span>${label}</span><span class="stat-icon">${icon}</span></div><div class="stat-value">${value}</div><div class="stat-foot ${kind}">${foot}</div></article>`; }
function percentBar(value) { const p = Number(value) || 0; return `<div class="progress-track"><div class="progress-fill ${p<75?'low':''}" style="width:${Math.max(0,Math.min(100,p))}%"></div></div>`; }
function statusBadge(value, hasRecords=true) { if (!hasRecords) return '<span class="status neutral">No record</span>'; return `<span class="status ${value>=75?'ok':'low'}">${value>=75?'✓ Eligible':'! Below 75%'}</span>`; }

function subjectList(summary) {
  return subjects.map(subject => {
    const row = summary.find(x => x.subjectCode === subject.code) || {percentage:0,classesHeld:0,classesPresent:0,eligible:false};
    return `<div class="subject-line"><div><strong class="subject-code">${esc(subject.code)}</strong><span class="subject-name">${esc(subject.name)}</span></div><div>${percentBar(row.percentage)}<div style="font-size:9px;color:var(--muted);margin-top:5px">${row.classesPresent} / ${row.classesHeld} present</div></div><div class="percent">${Number(row.percentage).toFixed(row.percentage%1?1:0)}%</div></div>`;
  }).join('');
}

async function studentOverview() {
  const summary = await api(`/attendance/summary?studentId=${currentUser.studentId}`);
  const values = summary.filter(x => x.classesHeld > 0).map(x => Number(x.percentage));
  const overall = values.length ? Math.round(values.reduce((a,b)=>a+b,0)/values.length) : 0;
  const low = summary.filter(x => x.classesHeld > 0 && Number(x.percentage) < 75).length;
  return `<div class="welcome-row"><div><h1>Good ${new Date().getHours()<12?'morning':new Date().getHours()<17?'afternoon':'evening'}, ${esc(currentUser.name.split(' ')[0])} 👋</h1><p>Your attendance is now reading directly from the Java + H2 backend.</p></div><button class="btn btn-primary" data-page="My Attendance">View full report ↗</button></div>
    <div class="stats-grid">${statCard('◉','Overall attendance',`${overall}%`,values.length?(low?`${low} subject(s) below 75%`:'You’re on track'):'No attendance recorded yet',low?'warning':'positive')}${statCard('▤','Subjects enrolled',String(subjects.length),'Semester 03 · Engineering')}${statCard('✓','Attendance goal','75%','Minimum required')}${statCard('!','Needs attention',String(low),low?'Review low-attendance subjects':'All recorded subjects meet goal',low?'warning':'positive')}</div>
    <div class="content-grid"><section class="panel"><div class="panel-head"><div><h3>Subject-wise attendance</h3><p>Live values calculated from saved attendance records</p></div><button class="text-link" data-page="My Attendance">Full report →</button></div><div class="subject-list">${subjectList(summary)}</div></section><section class="panel"><div class="panel-head"><div><h3>Attendance goal</h3><p>Your semester target</p></div><span class="status ${overall>=75?'ok':'low'}">${overall>=75?'On track':'Below target'}</span></div><div style="padding:12px 0 18px;text-align:center"><div style="font:800 48px Manrope;letter-spacing:-2px">${overall}%</div><div style="font-size:11px;color:var(--muted)">Average of subjects with records</div>${percentBar(overall)}<p style="font-size:11px;color:var(--muted);line-height:1.7">${values.length?(low?`You have ${low} subject(s) below 75%. Attend upcoming classes to improve your standing.`:'Great work — your recorded attendance is at or above the required target.'):'Ask your teacher to record attendance for your classes.'}</p></div><button class="btn btn-secondary btn-wide" data-page="Check-in">Open attendance check-in</button></section></div>`;
}

async function studentAttendance() {
  const summary = await api(`/attendance/summary?studentId=${currentUser.studentId}`);
  return `<div class="welcome-row"><div><h1>My attendance</h1><p>Real subject-wise attendance from the Java backend.</p></div><span class="status neutral">SEMESTER 03</span></div><section class="panel"><div class="panel-head"><div><h3>Attendance report</h3><p>Percentages are calculated from H2 attendance records.</p></div></div><div class="table-wrap"><table><thead><tr><th>Subject</th><th>Classes held</th><th>Present</th><th>Attendance</th><th>Eligibility</th></tr></thead><tbody>${subjects.map(subject=>{const r=summary.find(x=>x.subjectCode===subject.code)||{classesHeld:0,classesPresent:0,percentage:0};const p=Number(r.percentage);return `<tr><td><strong>${esc(subject.code)}</strong><div style="font-size:10px;color:var(--muted);margin-top:4px">${esc(subject.name)}</div></td><td>${r.classesHeld}</td><td>${r.classesPresent}</td><td><strong>${p.toFixed(p%1?1:0)}%</strong>${percentBar(p)}</td><td>${statusBadge(p,r.classesHeld>0)}</td></tr>`}).join('')}</tbody></table></div></section>`;
}

function checkIn() { return `<div class="welcome-row"><div><h1>Attendance check-in</h1><p>Use a teacher-issued session code so student check-in stays controlled.</p></div></div><section class="panel" style="max-width:700px"><div class="panel-head"><div><h3>Join a class session</h3><p>This screen is ready for the session-code workflow; attendance is only recorded through verified teacher actions in this build.</p></div></div><label style="display:block;font-size:11px;font-weight:700;margin-bottom:8px">Subject</label><select class="field-select" id="check-subject" style="width:100%;margin-bottom:17px">${subjects.map(s=>`<option value="${esc(s.code)}">${esc(s.code)} — ${esc(s.name)}</option>`).join('')}</select><label style="display:block;font-size:11px;font-weight:700;margin-bottom:8px">Session code</label><input id="session-code" class="field-select" style="width:100%;margin-bottom:18px" placeholder="e.g. AOA-4821"><button class="btn btn-primary" id="request-checkin">Request check-in ↗</button><p id="checkin-message" style="font-size:11px;color:var(--muted);margin-top:15px">Demo request only — it does not create attendance without teacher verification.</p></section>`; }

async function teacherOverview() {
  const reports = await Promise.all(subjects.map(async s => ({code:s.code, rows:await api(`/attendance/report?subjectCode=${encodeURIComponent(s.code)}`)})));
  const allRows = reports.flatMap(x=>x.rows); const uniqueRisk = new Set(allRows.filter(r=>r.classesHeld>0 && Number(r.percentage)<75).map(r=>r.studentId)).size;
  return `<div class="welcome-row"><div><h1>Faculty overview</h1><p>Attendance data, subjects and reports are now coming from Java + H2.</p></div><button class="btn btn-primary" data-page="Attendance">Take attendance +</button></div><div class="stats-grid">${statCard('▤','Subjects assigned',String(subjects.length),'Semester 03')}${statCard('♙','Students',String(students.length),'Current roster')}${statCard('✓','Backend status','LIVE','Spring Boot · port 8080','positive')}${statCard('!','Students at risk',String(uniqueRisk),uniqueRisk?'Below 75% in at least one subject':'No student below 75%','warning')}</div><div class="content-grid"><section class="panel"><div class="panel-head"><div><h3>Subject attendance snapshot</h3><p>Average across saved student records</p></div><button class="text-link" data-page="Reports">View reports →</button></div><div class="subject-list">${reports.map(r=>{const active=r.rows.filter(x=>x.classesHeld>0);const p=active.length?Math.round(active.reduce((a,b)=>a+Number(b.percentage),0)/active.length):0;const sub=subjects.find(s=>s.code===r.code);return `<div class="subject-line"><div><strong class="subject-code">${esc(r.code)}</strong><span class="subject-name">${esc(sub?.name||'')}</span></div><div>${percentBar(p)}<div style="font-size:9px;color:var(--muted);margin-top:5px">${active.length} students with records</div></div><div class="percent">${p}%</div></div>`}).join('')}</div></section><section class="panel"><div class="panel-head"><div><h3>Quick actions</h3><p>Common faculty tasks</p></div></div><button class="btn btn-primary btn-wide" data-page="Attendance">✓ &nbsp; Mark attendance</button><button class="btn btn-secondary btn-wide" data-page="Reports" style="margin-top:10px">▤ &nbsp; View eligibility report</button><div class="demo-note" style="margin-top:20px"><span class="demo-dot"></span><div><strong>75% rule</strong><p>Students below 75% in any subject are flagged for follow-up.</p></div></div></section></div>`;
}

async function teacherAttendance() {
  const subject = window.selectedSubject || (subjects[0]?.code || 'AOA');
  const date = window.selectedDate || today();
  const records = await api(`/attendance/report?subjectCode=${encodeURIComponent(subject)}`);
  const rows = students.map(student => {
    const record = records.find(r => r.studentId === student.id && r.attendanceDate === date);
    const key = `${student.id}|${subject}|${date}`;
    const state = attendanceDraft[key] || record?.status || 'PRESENT';
    return `<tr><td><div class="student-cell"><div class="avatar">${initials(student.name)}</div><div><strong>${esc(student.name)}</strong><small>${esc(student.rollNumber || student.roll || '')}</small></div></div></td><td>${esc(student.email)}</td><td><div class="attendance-choice"><button class="choice ${state==='PRESENT'?'selected-present':''}" data-att="${esc(key)}" data-state="PRESENT">Present</button><button class="choice ${state==='ABSENT'?'selected-absent':''}" data-att="${esc(key)}" data-state="ABSENT">Absent</button></div></td></tr>`;
  }).join('');
  return `<div class="welcome-row"><div><h1>Take attendance</h1><p>Write today’s register directly to H2 through the Spring Boot API.</p></div><button class="btn btn-primary" id="save-attendance">Save attendance ✓</button></div><section class="panel"><div class="filter-row"><select id="subject-select">${subjects.map(s=>`<option value="${esc(s.code)}" ${s.code===subject?'selected':''}>${esc(s.code)} — ${esc(s.name)}</option>`).join('')}</select><input id="date-select" type="date" value="${date}"></div><div class="panel-head"><div><h3>${esc(subject)} · Attendance register</h3><p>${students.length} students · ${date}</p></div><span class="status neutral">LIVE DATABASE</span></div><div class="table-wrap"><table><thead><tr><th>Student</th><th>College email</th><th>Mark status</th></tr></thead><tbody>${rows}</tbody></table></div><div class="actions-row"><small>Saving updates existing records for the same student, subject and date.</small><button class="btn btn-primary" id="save-attendance-bottom">Save attendance ✓</button></div></section>`;
}

async function saveAttendance() {
  const subject = $('subject-select').value; const date = $('date-select').value;
  const requests = students.map(student => { const key=`${student.id}|${subject}|${date}`; const status=attendanceDraft[key]||'PRESENT'; return api('/attendance',{method:'POST',body:JSON.stringify({studentId:student.id,subjectCode:subject,attendanceDate:date,status})}); });
  try { await Promise.all(requests); showToast(`${subject} attendance saved to H2.`); attendanceDraft = {}; await render(); }
  catch (error) { showToast(`Save failed: ${error.message}`); }
}

async function teacherReports() {
  return `<div class="welcome-row"><div><h1>Attendance reports</h1><p>Live subject-wise reports from the Java backend.</p></div><span class="status neutral">SEMESTER 03</span></div><section class="panel"><div class="filter-row"><select id="report-subject">${subjects.map(s=>`<option value="${esc(s.code)}">${esc(s.code)} — ${esc(s.name)}</option>`).join('')}</select><input id="report-search" placeholder="Search student or roll no."></div><div class="panel-head"><div><h3 id="report-title">AOA · Eligibility report</h3><p>Below 75% students are highlighted.</p></div></div><div class="table-wrap" id="report-table"><div class="empty">Loading report…</div></div></section>`;
}

async function loadReportTable() {
  const code = $('report-subject').value; const query = ($('report-search').value || '').toLowerCase();
  $('report-title').textContent = `${code} · Eligibility report`; $('report-table').innerHTML = '<div class="empty">Loading report…</div>';
  try {
    const rows = await api(`/attendance/report?subjectCode=${encodeURIComponent(code)}`);
    const filtered = rows.filter(r => (`${r.studentName}${r.rollNumber}`).toLowerCase().includes(query));
    $('report-table').innerHTML = `<table><thead><tr><th>Student</th><th>Classes held</th><th>Present</th><th>Attendance</th><th>Status</th></tr></thead><tbody>${filtered.map(r=>{const p=Number(r.percentage);return `<tr><td><div class="student-cell"><div class="avatar">${initials(r.studentName)}</div><div><strong>${esc(r.studentName)}</strong><small>${esc(r.rollNumber)}</small></div></div></td><td>${r.classesHeld}</td><td>${r.classesPresent}</td><td><strong>${p.toFixed(p%1?1:0)}%</strong></td><td>${statusBadge(p,r.classesHeld>0)}</td></tr>`}).join('') || '<tr><td class="empty" colspan="5">No students match this filter.</td></tr>'}</tbody></table>`;
  } catch (error) { $('report-table').innerHTML = `<div class="empty">Unable to load report: ${esc(error.message)}</div>`; }
}

async function render() {
  if (!currentUser) { loginView(); return; }
  try {
    let content;
    if (currentUser.role === 'STUDENT') {
      if (activePage === 'Overview') content = await studentOverview();
      else if (activePage === 'My Attendance') content = await studentAttendance();
      else content = checkIn();
    } else {
      if (activePage === 'Overview') content = await teacherOverview();
      else if (activePage === 'Attendance') content = await teacherAttendance();
      else content = await teacherReports();
    }
    shell(content);
    document.querySelectorAll('[data-page]').forEach(b => b.onclick = () => { activePage = b.dataset.page; render(); });
    if (currentUser.role === 'TEACHER' && activePage === 'Attendance') {
      $('subject-select').onchange = e => { window.selectedSubject = e.target.value; render(); };
      $('date-select').onchange = e => { window.selectedDate = e.target.value; render(); };
      document.querySelectorAll('[data-att]').forEach(b => b.onclick = () => { attendanceDraft[b.dataset.att] = b.dataset.state; render(); });
      $('save-attendance').onclick = $('save-attendance-bottom').onclick = saveAttendance;
    }
    if (currentUser.role === 'TEACHER' && activePage === 'Reports') {
      $('report-subject').onchange = loadReportTable;
      $('report-search').oninput = loadReportTable;
      loadReportTable();
    }
    if (currentUser.role === 'STUDENT' && activePage === 'Check-in') {
      $('request-checkin').onclick = () => { const code = $('session-code').value.trim(); $('checkin-message').textContent = code ? `Request created for ${$('check-subject').value}. A teacher must verify it before it becomes a real attendance record.` : 'Enter the session code shared by your teacher.'; };
    }
  } catch (error) {
    shell(`<section class="panel"><div class="empty"><strong>Could not load this page.</strong><div style="margin-top:8px">${esc(error.message)}</div><div style="margin-top:14px"><button class="btn btn-primary" id="retry">Retry</button></div></div></section>`);
    $('retry').onclick = render;
  }
}

function showToast(message) {
  document.querySelector('.toast')?.remove();
  const toast = document.createElement('div'); toast.className='toast'; toast.textContent=message; document.body.appendChild(toast);
  setTimeout(()=>toast.remove(),3000);
}

(async function boot(){
  loadingView('Loading subjects and students…');
  try { await loadMasterData(); loginView(); }
  catch (error) { loadingView(`Backend connection failed: ${error.message}`); setTimeout(()=>loginView(`Start Spring Boot first: ${error.message}`),900); }
})();
