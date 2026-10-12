import React, { useEffect, useState } from 'react';

async function api(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers }
  });
  const result = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    throw new Error(result?.message || 'Something went wrong. Please try again.');
  }
  return result;
}

function formatDate(value, includeYear = false) {
  return new Intl.DateTimeFormat('en', {
    day: 'numeric',
    month: 'short',
    ...(includeYear ? { year: 'numeric' } : {})
  }).format(new Date(`${value}T12:00:00`));
}

function Toast({ message, isError }) {
  return (
    <div
      className={`toast${message ? ' visible' : ''}${isError ? ' toast-error' : ''}`}
      role="status"
      aria-live="polite"
    >
      {message}
    </div>
  );
}

function RequestRow({ request, onDecision }) {
  return (
    <article className="request-row">
      <div className="request-avatar">{request.employee.name.charAt(0)}</div>
      <div className="request-main">
        <div className="request-person-line">
          <strong>{request.employee.name}</strong>
          <span className="request-department">{request.employee.department}</span>
        </div>
        <div className="request-meta">
          <span className="type-label">{request.leaveType}</span>
          <span className="meta-dot">·</span>
          <span>{formatDate(request.startDate)} – {formatDate(request.endDate, true)}</span>
          <span className="meta-dot">·</span>
          <span>{request.daysRequested} working days</span>
        </div>
        <p className="request-reason">{request.reason}</p>
        {request.reviewNote && <p className="review-note">Note: {request.reviewNote}</p>}
      </div>
      <div className="request-actions">
        <span className={`status-pill status-${request.status.toLowerCase()}`}>
          {request.status}
        </span>
        {request.status === 'PENDING' && (
          <form
            className="decision-form"
            onSubmit={(event) => onDecision(event, request.id)}
          >
            <input
              type="text"
              name="reviewNote"
              maxLength="500"
              placeholder="Optional review note"
              aria-label="Optional review note"
            />
            <button
              className="decision-button approve-button"
              type="submit"
              name="decision"
              value="APPROVED"
            >
              Approve
            </button>
            <button
              className="decision-button reject-button"
              type="submit"
              name="decision"
              value="REJECTED"
            >
              Reject
            </button>
          </form>
        )}
      </div>
    </article>
  );
}

function EmployeeRow({ employee, index }) {
  return (
    <tr>
      <td>
        <div className="person-cell">
          <span className={`avatar avatar-${index % 4}`}>{employee.name.charAt(0)}</span>
          <strong>{employee.name}</strong>
        </div>
      </td>
      <td>{employee.department}</td>
      <td className="email-cell">{employee.email}</td>
      <td>
        <span className="balance-value">{employee.annualLeaveBalance}</span>
        <span className="balance-unit">days</span>
      </td>
    </tr>
  );
}

export default function App() {
  const [employees, setEmployees] = useState([]);
  const [requests, setRequests] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [toast, setToast] = useState({ message: '', isError: false });

  useEffect(() => {
    const timeout = toast.message
      ? window.setTimeout(() => setToast({ message: '', isError: false }), 3500)
      : undefined;
    return () => window.clearTimeout(timeout);
  }, [toast]);

  async function refresh() {
    const [employeeData, requestData] = await Promise.all([
      api('/api/employees'),
      api('/api/leave-requests')
    ]);
    setEmployees(employeeData);
    setRequests(requestData);
    setLoadError('');
  }

  useEffect(() => {
    refresh()
      .catch((error) => {
        setLoadError(error.message);
        setToast({ message: 'Could not load leave data.', isError: true });
      })
      .finally(() => setLoading(false));
  }, []);

  function showToast(message, isError = false) {
    setToast({ message, isError });
  }

  async function handleRequestSubmit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form));
    values.employeeId = Number(values.employeeId);
    try {
      await api('/api/leave-requests', {
        method: 'POST',
        body: JSON.stringify(values)
      });
      form.reset();
      showToast('Leave request sent for review.');
      await refresh();
    } catch (error) {
      showToast(error.message, true);
    }
  }

  async function handleEmployeeSubmit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form));
    values.annualLeaveBalance = Number(values.annualLeaveBalance);
    try {
      await api('/api/employees', {
        method: 'POST',
        body: JSON.stringify(values)
      });
      form.reset();
      form.closest('details').open = false;
      showToast('Employee added to the directory.');
      await refresh();
    } catch (error) {
      showToast(error.message, true);
    }
  }

  async function handleDecision(event, requestId) {
    event.preventDefault();
    const form = event.currentTarget;
    const decision = event.nativeEvent.submitter.value;
    try {
      await api(`/api/leave-requests/${requestId}/decision`, {
        method: 'POST',
        body: JSON.stringify({
          decision,
          reviewNote: new FormData(form).get('reviewNote')
        })
      });
      showToast(`Leave request ${decision.toLowerCase()}.`);
      await refresh();
    } catch (error) {
      showToast(error.message, true);
    }
  }

  const filteredRequests = filter === 'ALL'
    ? requests
    : requests.filter((request) => request.status === filter);
  const pendingCount = requests.filter((request) => request.status === 'PENDING').length;
  const approvedCount = requests.filter((request) => request.status === 'APPROVED').length;
  const today = new Intl.DateTimeFormat('en', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(new Date());

  function setRequestFilter(nextFilter) {
    setFilter(nextFilter);
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#overview" aria-label="Northstar People home">
          <span className="brand-mark">N</span>
          <span><strong>northstar</strong><small>PEOPLE OPERATIONS</small></span>
        </a>
        <p className="side-label">WORKSPACE</p>
        <nav className="navigation" aria-label="Main navigation">
          <a className="nav-link active" href="#overview"><span className="nav-symbol">◫</span>Overview</a>
          <a className="nav-link" href="#requests"><span className="nav-symbol">▤</span>Leave requests<span className="nav-count">{pendingCount}</span></a>
          <a className="nav-link" href="#employees"><span className="nav-symbol">♧</span>People</a>
        </nav>
        <div className="sidebar-bottom">
          <p className="side-label">YOUR SPACE</p>
          <div className="profile-row"><span className="avatar avatar-admin">HR</span><span><strong>People team</strong><small>Administrator</small></span></div>
          <div className="sidebar-foot">Leave desk <span>v1.0</span></div>
        </div>
      </aside>

      <main className="main-content" id="overview">
        <header className="topbar">
          <div className="breadcrumb">Workspace <span>/</span> Time away</div>
          <div className="topbar-date"><span className="live-dot"></span>People operations <span className="date-divider">·</span><time>{today}</time></div>
        </header>
        <div className="page-wrap">
          <section className="page-heading">
            <div><div className="eyebrow">PEOPLE / TIME AWAY</div><h1>Leave desk<span className="heading-period">.</span></h1><p className="page-subtitle">A clear view of who's away and what's waiting on you.</p></div>
            <a className="primary-button heading-action" href="#new-request"><span aria-hidden="true">＋</span> New request</a>
          </section>
          <Toast message={toast.message} isError={toast.isError} />

          <section className="summary-grid" aria-label="Leave overview">
            <article className="summary-card summary-featured"><div className="summary-top"><span>Needs your attention</span><span className="summary-glyph">↗</span></div><div className="summary-number">{pendingCount}</div><div className="summary-caption">pending requests</div><a href="#requests" className="summary-link">Review queue <span>→</span></a></article>
            <article className="summary-card"><div className="summary-top"><span>People</span><span className="metric-mark green-mark">♧</span></div><div className="summary-number">{employees.length}</div><div className="summary-caption">in your directory</div><a href="#employees" className="summary-link">View people <span>→</span></a></article>
            <article className="summary-card"><div className="summary-top"><span>Approved</span><span className="metric-mark coral-mark">✓</span></div><div className="summary-number">{approvedCount}</div><div className="summary-caption">requests on record</div><a href="#requests" onClick={() => setRequestFilter('APPROVED')} className="summary-link">See approved <span>→</span></a></article>
            <article className="summary-card"><div className="summary-top"><span>All activity</span><span className="metric-mark blue-mark">◷</span></div><div className="summary-number">{requests.length}</div><div className="summary-caption">requests submitted</div><a href="#requests" onClick={() => setRequestFilter('ALL')} className="summary-link">View activity <span>→</span></a></article>
          </section>

          <section className="content-grid">
            <section className="panel request-panel" id="requests">
              <div className="panel-heading">
                <div><div className="section-kicker">INBOX</div><h2>Leave requests</h2></div>
                <label className="filter-label"><span className="visually-hidden">Filter requests</span><select aria-label="Filter requests" value={filter} onChange={(event) => setRequestFilter(event.target.value)}><option value="ALL">All requests</option><option value="PENDING">Pending</option><option value="APPROVED">Approved</option><option value="REJECTED">Rejected</option></select></label>
              </div>
              <div className="request-list">
                {loading
                  ? <div className="loading-state">Loading leave requests...</div>
                  : loadError
                    ? <div className="error-state">{loadError} Check that the application server is running, then refresh.</div>
                    : filteredRequests.length
                      ? filteredRequests.map((request) => <RequestRow key={request.id} request={request} onDecision={handleDecision} />)
                      : <div className="empty-state"><span className="empty-mark">✓</span><strong>Nothing in this view</strong><span>Requests will appear here as soon as they are submitted.</span></div>}
              </div>
              <div className="panel-foot"><span>Showing <strong>{filteredRequests.length}</strong> requests</span><span className="foot-dot"></span><span>Newest first</span></div>
            </section>

            <aside className="right-column">
              <section className="panel form-panel" id="new-request">
                <div className="panel-heading compact-heading"><div><div className="section-kicker">TIME OFF</div><h2>New request</h2></div><span className="form-icon">＋</span></div>
                <form className="stacked-form" onSubmit={handleRequestSubmit}>
                  <label>Employee<select name="employeeId" required defaultValue=""><option value="" disabled>Select a person</option>{employees.map((employee) => <option key={employee.id} value={employee.id}>{employee.name} · {employee.department}</option>)}</select></label>
                  <label>Leave type<select name="leaveType" required defaultValue="ANNUAL"><option value="ANNUAL">Annual</option><option value="SICK">Sick</option><option value="PERSONAL">Personal</option></select></label>
                  <div className="date-fields"><label>From<input type="date" name="startDate" required /></label><label>To<input type="date" name="endDate" required /></label></div>
                  <label>Reason<textarea name="reason" rows="3" maxLength="1000" placeholder="Add a little context..." required></textarea></label>
                  <button className="primary-button submit-button" type="submit">Send for review <span>→</span></button>
                  <p className="form-footnote">Weekdays are counted automatically. Balance updates on approval.</p>
                </form>
              </section>
              <section className="callout-panel"><div className="callout-top"><span className="callout-star">✳</span><span>GOOD TO KNOW</span></div><p>Approved leave is deducted from each person's available balance. Rejected dates can be requested again.</p><a href="#employees">Check team balances <span>→</span></a></section>
            </aside>
          </section>

          <section className="panel people-panel" id="employees">
            <div className="panel-heading people-heading">
              <div><div className="section-kicker">YOUR TEAM</div><h2>People directory</h2></div>
              <details className="add-employee-details"><summary className="outline-button"><span>＋</span> Add employee</summary>
                <form className="employee-form" onSubmit={handleEmployeeSubmit}><label>Full name<input name="name" maxLength="120" required placeholder="e.g. Sam Rivera" /></label><label>Work email<input name="email" type="email" maxLength="180" required placeholder="sam@company.com" /></label><label>Department<input name="department" maxLength="100" required placeholder="e.g. Design" /></label><label>Days available<input name="annualLeaveBalance" type="number" min="0" max="60" defaultValue="20" required /></label><button className="primary-button" type="submit">Add to directory</button></form>
              </details>
            </div>
            <div className="people-table-wrap"><table className="people-table"><thead><tr><th>EMPLOYEE</th><th>DEPARTMENT</th><th>EMAIL</th><th>AVAILABLE DAYS</th></tr></thead><tbody>{loading ? <tr><td className="empty-table" colSpan="4">Loading people...</td></tr> : employees.length ? employees.map((employee, index) => <EmployeeRow key={employee.id} employee={employee} index={index} />) : <tr><td className="empty-table" colSpan="4">{loadError || 'Add your first employee to get started.'}</td></tr>}</tbody></table></div>
            <div className="panel-foot people-foot"><span><strong>{employees.length}</strong> people</span><span className="foot-dot"></span><span>Balances update when leave is approved</span></div>
          </section>
          <footer className="page-footer"><span>Northstar People</span><span>Time away, thoughtfully managed.</span></footer>
        </div>
      </main>
    </div>
  );
}
