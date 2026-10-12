const state = { employees: [], requests: [], filter: 'ALL' };
const requestList = document.querySelector('#request-list');
const employeeList = document.querySelector('#employee-list');
const toast = document.querySelector('#toast');

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (character) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

function showToast(message, isError = false) {
  toast.textContent = message;
  toast.className = `toast visible${isError ? ' toast-error' : ''}`;
  window.clearTimeout(showToast.timeout);
  showToast.timeout = window.setTimeout(() => toast.classList.remove('visible'), 3500);
}

async function api(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers }
  });
  const result = response.status === 204 ? null : await response.json();
  if (!response.ok) throw new Error(result?.message || 'Something went wrong. Please try again.');
  return result;
}

function formatDate(value, includeYear = false) {
  return new Intl.DateTimeFormat('en', { day: 'numeric', month: 'short', ...(includeYear ? { year: 'numeric' } : {}) }).format(new Date(`${value}T12:00:00`));
}

function renderRequests() {
  const filtered = state.filter === 'ALL' ? state.requests : state.requests.filter((request) => request.status === state.filter);
  document.querySelector('#shown-count').textContent = filtered.length;
  if (!filtered.length) {
    requestList.innerHTML = '<div class="empty-state"><span class="empty-mark">✓</span><strong>Nothing in this view</strong><span>Requests will appear here as soon as they are submitted.</span></div>';
    return;
  }
  requestList.innerHTML = filtered.map((request) => `
    <article class="request-row">
      <div class="request-avatar">${escapeHtml(request.employee.name.charAt(0))}</div>
      <div class="request-main">
        <div class="request-person-line"><strong>${escapeHtml(request.employee.name)}</strong><span class="request-department">${escapeHtml(request.employee.department)}</span></div>
        <div class="request-meta"><span class="type-label">${escapeHtml(request.leaveType)}</span><span class="meta-dot">·</span><span>${formatDate(request.startDate)} – ${formatDate(request.endDate, true)}</span><span class="meta-dot">·</span><span>${request.daysRequested} working days</span></div>
        <p class="request-reason">${escapeHtml(request.reason)}</p>
        ${request.reviewNote ? `<p class="review-note">Note: ${escapeHtml(request.reviewNote)}</p>` : ''}
      </div>
      <div class="request-actions"><span class="status-pill status-${request.status.toLowerCase()}">${escapeHtml(request.status)}</span>
        ${request.status === 'PENDING' ? `<form class="decision-form" data-request-id="${request.id}"><input type="text" name="reviewNote" maxlength="500" placeholder="Optional review note" aria-label="Optional review note"><button class="decision-button approve-button" type="submit" name="decision" value="APPROVED">Approve</button><button class="decision-button reject-button" type="submit" name="decision" value="REJECTED">Reject</button></form>` : ''}
      </div>
    </article>`).join('');
}

function renderEmployees() {
  const select = document.querySelector('#employee-select');
  const selected = select.value;
  select.innerHTML = '<option value="" disabled>Select a person</option>' + state.employees.map((employee) => `<option value="${employee.id}">${escapeHtml(employee.name)} · ${escapeHtml(employee.department)}</option>`).join('');
  if (selected && state.employees.some((employee) => String(employee.id) === selected)) select.value = selected;
  employeeList.innerHTML = state.employees.length ? state.employees.map((employee, index) => `
    <tr><td><div class="person-cell"><span class="avatar avatar-${index % 4}">${escapeHtml(employee.name.charAt(0))}</span><strong>${escapeHtml(employee.name)}</strong></div></td><td>${escapeHtml(employee.department)}</td><td class="email-cell">${escapeHtml(employee.email)}</td><td><span class="balance-value">${employee.annualLeaveBalance}</span><span class="balance-unit">days</span></td></tr>`).join('') : '<tr><td class="empty-table" colspan="4">Add your first employee to get started.</td></tr>';
  document.querySelector('#employee-count').textContent = state.employees.length;
  document.querySelector('#people-total').textContent = state.employees.length;
}

function renderSummary() {
  const pending = state.requests.filter((request) => request.status === 'PENDING').length;
  const approved = state.requests.filter((request) => request.status === 'APPROVED').length;
  document.querySelector('#pending-count').textContent = pending;
  document.querySelector('#nav-pending').textContent = pending;
  document.querySelector('#approved-count').textContent = approved;
  document.querySelector('#request-count').textContent = state.requests.length;
}

async function refresh() {
  [state.employees, state.requests] = await Promise.all([
    api('/api/employees'), api('/api/leave-requests')
  ]);
  renderEmployees();
  renderRequests();
  renderSummary();
}

document.querySelector('#today').textContent = new Intl.DateTimeFormat('en', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' }).format(new Date());
document.querySelector('#status-filter').addEventListener('change', (event) => {
  state.filter = event.target.value;
  renderRequests();
});
document.querySelectorAll('[data-filter-link]').forEach((link) => link.addEventListener('click', () => {
  state.filter = link.dataset.filterLink;
  document.querySelector('#status-filter').value = state.filter;
  renderRequests();
}));

document.querySelector('#request-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = Object.fromEntries(new FormData(form));
  values.employeeId = Number(values.employeeId);
  try {
    await api('/api/leave-requests', { method: 'POST', body: JSON.stringify(values) });
    form.reset();
    showToast('Leave request sent for review.');
    await refresh();
  } catch (error) { showToast(error.message, true); }
});

document.querySelector('#employee-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = Object.fromEntries(new FormData(form));
  values.annualLeaveBalance = Number(values.annualLeaveBalance);
  try {
    await api('/api/employees', { method: 'POST', body: JSON.stringify(values) });
    form.reset();
    form.closest('details').open = false;
    showToast('Employee added to the directory.');
    await refresh();
  } catch (error) { showToast(error.message, true); }
});

requestList.addEventListener('submit', async (event) => {
  const form = event.target.closest('.decision-form');
  if (!form) return;
  event.preventDefault();
  const decision = event.submitter.value;
  try {
    await api(`/api/leave-requests/${form.dataset.requestId}/decision`, {
      method: 'POST',
      body: JSON.stringify({ decision, reviewNote: new FormData(form).get('reviewNote') })
    });
    showToast(`Leave request ${decision.toLowerCase()}.`);
    await refresh();
  } catch (error) { showToast(error.message, true); }
});

refresh().catch((error) => {
  requestList.innerHTML = `<div class="error-state">${escapeHtml(error.message)} Check that the application server is running, then refresh.</div>`;
  showToast('Could not load leave data.', true);
});