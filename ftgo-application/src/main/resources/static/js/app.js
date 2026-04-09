/**
 * FTGO Order Management Dashboard
 *
 * A single-page dashboard for monitoring and managing orders in the
 * FTGO (Food To Go) application. Provides pipeline visualization,
 * kanban board, order detail view, and recent orders table.
 */

/* =====================================================================
 * Configuration & Constants
 * ===================================================================== */

const API_BASE = '';

/**
 * Ordered list of order states matching the backend OrderState enum.
 * Controls the display order in the pipeline bar and kanban columns.
 */
const STATE_ORDER = [
  'APPROVED',
  'ACCEPTED',
  'PREPARING',
  'READY_FOR_PICKUP',
  'PICKED_UP',
  'DELIVERED',
  'CANCELLED'
];

/**
 * Color assignments for each order state.
 * Used consistently across pipeline segments, kanban headers,
 * badges, and legend dots.
 */
const STATE_COLORS = {
  APPROVED:          '#3B82F6',
  ACCEPTED:          '#8B5CF6',
  PREPARING:         '#F59E0B',
  READY_FOR_PICKUP:  '#10B981',
  PICKED_UP:         '#06B6D4',
  DELIVERED:         '#22C55E',
  CANCELLED:         '#EF4444'
};

/**
 * Icons (SVG paths) for each order state, used in kanban headers
 * and order detail views.
 */
const STATE_ICONS = {
  APPROVED:         '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M8 0a8 8 0 100 16A8 8 0 008 0zm3.5 6.5l-4 4a.5.5 0 01-.7 0l-2-2a.5.5 0 01.7-.7L7.1 9.4l3.6-3.6a.5.5 0 01.8.7z" fill="currentColor"/></svg>',
  ACCEPTED:         '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M8 0a8 8 0 100 16A8 8 0 008 0zm1 11H7V7h2v4zm0-6H7V3h2v2z" fill="currentColor"/></svg>',
  PREPARING:        '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M8 1a7 7 0 100 14A7 7 0 008 1zm0 12.5A5.5 5.5 0 1113.5 8 5.5 5.5 0 018 13.5zM8.5 4H7v5l4.3 2.5.7-1.2-3.5-2V4z" fill="currentColor"/></svg>',
  READY_FOR_PICKUP: '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M14 6h-2V4a4 4 0 00-8 0v2H2a1 1 0 00-1 1v7a1 1 0 001 1h12a1 1 0 001-1V7a1 1 0 00-1-1zM6 4a2 2 0 014 0v2H6V4z" fill="currentColor"/></svg>',
  PICKED_UP:        '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M13 3H3a1 1 0 00-1 1v8a1 1 0 001 1h10a1 1 0 001-1V4a1 1 0 00-1-1zM8 10L3 7V5l5 3 5-3v2l-5 3z" fill="currentColor"/></svg>',
  DELIVERED:        '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M6.5 13a.5.5 0 01-.35-.15l-3.5-3.5a.5.5 0 01.7-.7L6.5 11.79l6.15-6.14a.5.5 0 01.7.7l-6.5 6.5a.5.5 0 01-.35.15z" fill="currentColor"/></svg>',
  CANCELLED:        '<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M8 0a8 8 0 100 16A8 8 0 008 0zm3.5 10.1l-1.4 1.4L8 9.4l-2.1 2.1-1.4-1.4L6.6 8 4.5 5.9l1.4-1.4L8 6.6l2.1-2.1 1.4 1.4L9.4 8l2.1 2.1z" fill="currentColor"/></svg>'
};

/** Auto-refresh interval in milliseconds (30 seconds). */
const REFRESH_INTERVAL = 30000;

/** Minimum percentage width for a pipeline segment so small counts remain visible. */
const MIN_SEGMENT_PCT = 4;


/* =====================================================================
 * State
 * ===================================================================== */

let orders = [];
let selectedOrderId = null;
let refreshTimer = null;


/* =====================================================================
 * Utility Functions
 * ===================================================================== */

/**
 * Convert a backend enum state string to a human-readable label.
 *
 * Input is an uppercase, underscore-separated string like "READY_FOR_PICKUP".
 * Output is title-cased with underscores replaced by spaces: "Ready for Pickup".
 *
 * The function lowercases first so that the subsequent \b\w title-casing
 * regex actually has an effect. Minor words like "for" are then de-capitalised
 * to match AP-style title case.
 *
 * @param {string} s - The state enum value.
 * @returns {string} The formatted state label.
 */
function formatState(s) {
  if (!s) return 'Unknown';
  return s
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, c => c.toUpperCase())
    .replace(/\bFor\b/g, 'for');
}

/**
 * Format a monetary amount as a USD string.
 *
 * @param {number|string} amount - The amount in dollars (or cents string).
 * @returns {string} Formatted dollar string, e.g. "$12.50".
 */
function formatMoney(amount) {
  if (amount == null) return '$0.00';
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  return '$' + num.toFixed(2);
}

/**
 * Create a relative-time string like "2 min ago" from an ISO timestamp.
 *
 * @param {string} iso - ISO 8601 timestamp.
 * @returns {string} Human-readable relative time.
 */
function timeAgo(iso) {
  if (!iso) return '';
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return mins + ' min ago';
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return hrs + 'h ago';
  const days = Math.floor(hrs / 24);
  return days + 'd ago';
}

/**
 * Escape HTML special characters in user-supplied text.
 *
 * @param {string} str - Raw text.
 * @returns {string} Escaped string safe for innerHTML.
 */
function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

/**
 * Return the CSS color for a given order state.
 *
 * @param {string} state - Order state enum value.
 * @returns {string} Hex color code.
 */
function stateColor(state) {
  return STATE_COLORS[state] || '#6B7280';
}


/* =====================================================================
 * Data Layer
 * ===================================================================== */

/**
 * Fetch all orders from the backend.
 *
 * The FTGO API requires a consumerId query parameter, but for the dashboard
 * we attempt to fetch all orders. We fall back to demo data when the API
 * is unavailable (e.g. when the database is not running).
 *
 * @returns {Promise<Array>} Resolved list of order objects.
 */
async function fetchOrders() {
  try {
    const resp = await fetch(API_BASE + '/orders?consumerId=0');
    if (!resp.ok) throw new Error('API returned ' + resp.status);
    return await resp.json();
  } catch (e) {
    console.warn('Could not reach orders API, using demo data:', e.message);
    return getDemoOrders();
  }
}

/**
 * Fetch a single order by ID.
 *
 * @param {number} orderId
 * @returns {Promise<Object|null>}
 */
async function fetchOrderDetail(orderId) {
  try {
    const resp = await fetch(API_BASE + '/orders/' + orderId);
    if (!resp.ok) throw new Error('API returned ' + resp.status);
    return await resp.json();
  } catch (e) {
    console.warn('Could not reach order detail API, using demo data:', e.message);
    return orders.find(o => o.orderId === orderId) || null;
  }
}

/**
 * Perform a state-transition action on an order.
 *
 * @param {number} orderId
 * @param {string} action - One of: cancel, accept, preparing, ready, pickedup, delivered
 * @param {Object} [body] - Optional request body.
 * @returns {Promise<boolean>} True if the action succeeded.
 */
async function performAction(orderId, action, body) {
  try {
    const opts = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    };
    if (body) opts.body = JSON.stringify(body);
    const resp = await fetch(API_BASE + '/orders/' + orderId + '/' + action, opts);
    return resp.ok;
  } catch (e) {
    console.error('Action failed:', e);
    return false;
  }
}


/* =====================================================================
 * Demo / Sample Data
 * ===================================================================== */

/**
 * Generate realistic demo orders when the backend is unavailable.
 *
 * @returns {Array<Object>} Array of order objects matching the API shape.
 */
function getDemoOrders() {
  const restaurants = [
    'Ajanta Indian',
    'Sushi Zen',
    'Bella Italia',
    'Taco Fiesta',
    'Golden Dragon',
    'Le Petit Bistro',
    'Burger Junction',
    'Pho Palace',
    'Mediterranean Grill',
    'Pizza Napoli'
  ];

  const demoData = [
    { orderId: 1001, orderState: 'APPROVED',          restaurantName: restaurants[0], orderTotal: '24.50', assignedCourierId: null },
    { orderId: 1002, orderState: 'APPROVED',          restaurantName: restaurants[1], orderTotal: '38.00', assignedCourierId: null },
    { orderId: 1003, orderState: 'ACCEPTED',          restaurantName: restaurants[2], orderTotal: '15.75', assignedCourierId: null },
    { orderId: 1004, orderState: 'ACCEPTED',          restaurantName: restaurants[3], orderTotal: '22.30', assignedCourierId: null },
    { orderId: 1005, orderState: 'ACCEPTED',          restaurantName: restaurants[4], orderTotal: '45.00', assignedCourierId: null },
    { orderId: 1006, orderState: 'PREPARING',         restaurantName: restaurants[5], orderTotal: '31.50', assignedCourierId: 201 },
    { orderId: 1007, orderState: 'PREPARING',         restaurantName: restaurants[6], orderTotal: '18.90', assignedCourierId: 202 },
    { orderId: 1008, orderState: 'READY_FOR_PICKUP',  restaurantName: restaurants[7], orderTotal: '27.80', assignedCourierId: 203 },
    { orderId: 1009, orderState: 'READY_FOR_PICKUP',  restaurantName: restaurants[8], orderTotal: '42.15', assignedCourierId: 204 },
    { orderId: 1010, orderState: 'PICKED_UP',         restaurantName: restaurants[9], orderTotal: '19.00', assignedCourierId: 205 },
    { orderId: 1011, orderState: 'PICKED_UP',         restaurantName: restaurants[0], orderTotal: '33.60', assignedCourierId: 206 },
    { orderId: 1012, orderState: 'PICKED_UP',         restaurantName: restaurants[1], orderTotal: '16.25', assignedCourierId: 207 },
    { orderId: 1013, orderState: 'DELIVERED',          restaurantName: restaurants[2], orderTotal: '29.90', assignedCourierId: 208 },
    { orderId: 1014, orderState: 'DELIVERED',          restaurantName: restaurants[3], orderTotal: '55.00', assignedCourierId: 209 },
    { orderId: 1015, orderState: 'DELIVERED',          restaurantName: restaurants[4], orderTotal: '12.50', assignedCourierId: 210 },
    { orderId: 1016, orderState: 'DELIVERED',          restaurantName: restaurants[5], orderTotal: '41.75', assignedCourierId: 211 },
    { orderId: 1017, orderState: 'CANCELLED',          restaurantName: restaurants[6], orderTotal: '23.00', assignedCourierId: null },
  ];

  return demoData;
}


/* =====================================================================
 * Rendering — Stats Row
 * ===================================================================== */

/**
 * Count orders per state and render summary stat cards.
 *
 * @param {Array<Object>} orderList - The current order list.
 */
function renderStats(orderList) {
  const container = document.getElementById('stats-row');
  if (!container) return;

  const total = orderList.length;
  const active = orderList.filter(o =>
    o.orderState !== 'DELIVERED' && o.orderState !== 'CANCELLED'
  ).length;
  const delivered = orderList.filter(o => o.orderState === 'DELIVERED').length;
  const cancelled = orderList.filter(o => o.orderState === 'CANCELLED').length;

  container.innerHTML = [
    { label: 'Total Orders',    value: total,     color: '#6366F1' },
    { label: 'Active',          value: active,    color: '#3B82F6' },
    { label: 'Delivered',       value: delivered,  color: '#22C55E' },
    { label: 'Cancelled',       value: cancelled,  color: '#EF4444' },
  ].map(s => `
    <div class="stat-card">
      <div class="stat-value" style="color:${s.color}">${s.value}</div>
      <div class="stat-label">${s.label}</div>
    </div>
  `).join('');
}


/* =====================================================================
 * Rendering — Order Pipeline
 * ===================================================================== */

/**
 * Compute per-state order counts.
 *
 * @param {Array<Object>} orderList
 * @returns {Object} Map of state -> count.
 */
function computeStateCounts(orderList) {
  const counts = {};
  STATE_ORDER.forEach(s => { counts[s] = 0; });
  orderList.forEach(o => {
    if (counts[o.orderState] !== undefined) {
      counts[o.orderState]++;
    }
  });
  return counts;
}

/**
 * Render the pipeline bar (stacked horizontal segments) and the
 * legend below it.
 *
 * Fix applied (Bug 3): States with 0 orders do not render a segment.
 * This prevents invisible 0-width slivers between adjacent coloured
 * segments.
 *
 * @param {Array<Object>} orderList
 */
function renderPipeline(orderList) {
  const barEl = document.getElementById('pipeline-bar');
  const labelsEl = document.getElementById('pipeline-labels');
  const badgeEl = document.getElementById('pipeline-total-badge');
  if (!barEl || !labelsEl) return;

  const stateCounts = computeStateCounts(orderList);
  const pipelineTotal = orderList.length;

  if (badgeEl) {
    badgeEl.textContent = pipelineTotal + ' orders';
  }

  // --- Pipeline segments ---------------------------------------------------
  // Bug 3 fix: skip segments where count is 0 so no invisible slivers appear.
  const pipelineSegments = STATE_ORDER.map(s => {
    const count = stateCounts[s];
    if (count === 0) return '';
    const pct = Math.max((count / pipelineTotal) * 100, MIN_SEGMENT_PCT);
    return `
      <div class="pipeline-segment" data-state="${s}" style="width:${pct}%;background:${stateColor(s)}" title="${formatState(s)}: ${count}">
        <span class="pipeline-count">${count}</span>
      </div>
    `;
  }).join('');

  barEl.innerHTML = pipelineSegments;

  // --- Legend ---------------------------------------------------------------
  const legendItems = STATE_ORDER.map(s => {
    const count = stateCounts[s];
    return `
      <div class="pipeline-label" data-state="${s}">
        <span class="pipeline-dot" style="background:${stateColor(s)}"></span>
        <span>${formatState(s)}</span>
        <strong>${count}</strong>
      </div>
    `;
  }).join('');

  labelsEl.innerHTML = legendItems;

  // Hover interaction: highlight corresponding bar segment
  labelsEl.querySelectorAll('.pipeline-label').forEach(label => {
    label.addEventListener('mouseenter', () => {
      const state = label.dataset.state;
      barEl.querySelectorAll('.pipeline-segment').forEach(seg => {
        seg.style.opacity = seg.dataset.state === state ? '1' : '0.4';
      });
    });
    label.addEventListener('mouseleave', () => {
      barEl.querySelectorAll('.pipeline-segment').forEach(seg => {
        seg.style.opacity = '1';
      });
    });
  });
}


/* =====================================================================
 * Rendering — Kanban Board
 * ===================================================================== */

/**
 * Available next-action mapping per state. Determines which action
 * button to show on each kanban card.
 */
const STATE_ACTIONS = {
  APPROVED:         { label: 'Accept',   action: 'accept',    body: { readyBy: new Date(Date.now() + 1800000).toISOString() } },
  ACCEPTED:         { label: 'Prepare',  action: 'preparing', body: null },
  PREPARING:        { label: 'Ready',    action: 'ready',     body: null },
  READY_FOR_PICKUP: { label: 'Pick Up',  action: 'pickedup',  body: null },
  PICKED_UP:        { label: 'Deliver',  action: 'delivered',  body: null },
};

/**
 * Render the kanban board with one column per active state.
 *
 * @param {Array<Object>} orderList
 */
function renderKanban(orderList) {
  const board = document.getElementById('kanban-board');
  if (!board) return;

  const stateCounts = computeStateCounts(orderList);

  // Show only active states (exclude DELIVERED and CANCELLED) for kanban
  const kanbanStates = STATE_ORDER.filter(s => s !== 'DELIVERED' && s !== 'CANCELLED');

  board.innerHTML = kanbanStates.map(state => {
    const stateOrders = orderList.filter(o => o.orderState === state);
    const actionInfo = STATE_ACTIONS[state];
    const cards = stateOrders.map(order => `
      <div class="kanban-card" data-order-id="${order.orderId}" onclick="selectOrder(${order.orderId})">
        <div class="kanban-card-header">
          <span class="kanban-order-id">#${order.orderId}</span>
          <span class="order-badge" style="background:${stateColor(state)}15;color:${stateColor(state)}">${formatState(state)}</span>
        </div>
        <div class="kanban-card-body">
          <div class="kanban-restaurant">${escapeHtml(order.restaurantName || 'Unknown')}</div>
          <div class="kanban-total">${formatMoney(order.orderTotal)}</div>
        </div>
        ${actionInfo ? `
          <div class="kanban-card-footer">
            <button class="btn btn-sm btn-action" onclick="event.stopPropagation();advanceOrder(${order.orderId},'${actionInfo.action}',${actionInfo.body ? "'" + JSON.stringify(actionInfo.body).replace(/'/g, "\\'") + "'" : 'null'})">${actionInfo.label}</button>
          </div>
        ` : ''}
      </div>
    `).join('');

    return `
      <div class="kanban-column">
        <div class="kanban-column-header" style="border-top: 3px solid ${stateColor(state)}">
          <span class="kanban-column-icon" style="color:${stateColor(state)}">${STATE_ICONS[state] || ''}</span>
          <span class="kanban-column-title">${formatState(state)}</span>
          <span class="kanban-column-count">${stateCounts[state]}</span>
        </div>
        <div class="kanban-column-body">
          ${cards || '<div class="kanban-empty">No orders</div>'}
        </div>
      </div>
    `;
  }).join('');
}


/* =====================================================================
 * Rendering — Order Detail Panel
 * ===================================================================== */

/**
 * Show detailed information for a selected order.
 *
 * @param {Object} order - The order object to display.
 */
function renderDetail(order) {
  const panel = document.getElementById('detail-panel');
  if (!panel) return;

  if (!order) {
    panel.innerHTML = '<div class="detail-empty"><p>Select an order from the kanban board to view details.</p></div>';
    return;
  }

  const state = order.orderState;
  const color = stateColor(state);
  const actionInfo = STATE_ACTIONS[state];
  const cancelable = state === 'APPROVED';

  panel.innerHTML = `
    <div class="detail-header">
      <h3 class="detail-order-id">Order #${order.orderId}</h3>
      <span class="order-badge order-badge-lg" style="background:${color}15;color:${color}">${formatState(state)}</span>
    </div>
    <div class="detail-grid">
      <div class="detail-field">
        <span class="detail-field-label">Restaurant</span>
        <span class="detail-field-value">${escapeHtml(order.restaurantName || 'Unknown')}</span>
      </div>
      <div class="detail-field">
        <span class="detail-field-label">Total</span>
        <span class="detail-field-value">${formatMoney(order.orderTotal)}</span>
      </div>
      <div class="detail-field">
        <span class="detail-field-label">Courier</span>
        <span class="detail-field-value">${order.assignedCourierId ? '#' + order.assignedCourierId : 'Unassigned'}</span>
      </div>
      <div class="detail-field">
        <span class="detail-field-label">State</span>
        <span class="detail-field-value">${formatState(state)}</span>
      </div>
    </div>
    <div class="detail-actions">
      ${actionInfo ? `<button class="btn btn-primary" onclick="advanceOrder(${order.orderId},'${actionInfo.action}',${actionInfo.body ? "'" + JSON.stringify(actionInfo.body).replace(/'/g, "\\'") + "'" : 'null'})">${actionInfo.label}</button>` : ''}
      ${cancelable ? `<button class="btn btn-danger" onclick="cancelOrder(${order.orderId})">Cancel</button>` : ''}
    </div>
  `;
}


/* =====================================================================
 * Rendering — Recent Orders Table
 * ===================================================================== */

/**
 * Render the recent orders table.
 *
 * @param {Array<Object>} orderList
 */
function renderTable(orderList) {
  const tbody = document.getElementById('orders-table-body');
  if (!tbody) return;

  tbody.innerHTML = orderList.map(order => {
    const state = order.orderState;
    const color = stateColor(state);
    return `
      <tr class="${selectedOrderId === order.orderId ? 'row-selected' : ''}" onclick="selectOrder(${order.orderId})">
        <td><strong>#${order.orderId}</strong></td>
        <td>${escapeHtml(order.restaurantName || 'Unknown')}</td>
        <td>
          <span class="order-badge" style="background:${color}15;color:${color}">${formatState(state)}</span>
        </td>
        <td>${formatMoney(order.orderTotal)}</td>
        <td>${order.assignedCourierId ? '#' + order.assignedCourierId : '<span class="text-muted">Unassigned</span>'}</td>
        <td>
          <button class="btn btn-sm btn-outline" onclick="event.stopPropagation();selectOrder(${order.orderId})">View</button>
        </td>
      </tr>
    `;
  }).join('');
}


/* =====================================================================
 * Actions
 * ===================================================================== */

/**
 * Select an order and show its detail.
 *
 * @param {number} orderId
 */
function selectOrder(orderId) {
  selectedOrderId = orderId;
  const order = orders.find(o => o.orderId === orderId);
  renderDetail(order);
  renderTable(orders);
}

/**
 * Advance an order to the next state.
 *
 * @param {number} orderId
 * @param {string} action
 * @param {string|null} bodyStr - JSON string of the request body, or null.
 */
async function advanceOrder(orderId, action, bodyStr) {
  const body = bodyStr ? JSON.parse(bodyStr) : null;
  const success = await performAction(orderId, action, body);
  if (success) {
    await refreshData();
  } else {
    // Optimistic update for demo mode
    const order = orders.find(o => o.orderId === orderId);
    if (order) {
      const stateIndex = STATE_ORDER.indexOf(order.orderState);
      if (stateIndex >= 0 && stateIndex < STATE_ORDER.length - 2) {
        order.orderState = STATE_ORDER[stateIndex + 1];
      }
      renderAll();
    }
  }
}

/**
 * Cancel an order.
 *
 * @param {number} orderId
 */
async function cancelOrder(orderId) {
  const success = await performAction(orderId, 'cancel');
  if (success) {
    await refreshData();
  } else {
    // Optimistic update for demo mode
    const order = orders.find(o => o.orderId === orderId);
    if (order && order.orderState === 'APPROVED') {
      order.orderState = 'CANCELLED';
      renderAll();
    }
  }
}


/* =====================================================================
 * Refresh & Render Orchestration
 * ===================================================================== */

/**
 * Render all dashboard components with the current data.
 */
function renderAll() {
  renderStats(orders);
  renderPipeline(orders);
  renderKanban(orders);
  renderTable(orders);

  if (selectedOrderId) {
    const order = orders.find(o => o.orderId === selectedOrderId);
    renderDetail(order);
  }

  // Update timestamp
  const tsEl = document.getElementById('last-updated');
  if (tsEl) {
    const now = new Date();
    tsEl.textContent = 'Updated ' + now.toLocaleTimeString();
  }
}

/**
 * Fetch fresh data from the API and re-render.
 */
async function refreshData() {
  try {
    orders = await fetchOrders();
    renderAll();
  } catch (e) {
    console.error('Failed to refresh data:', e);
  }
}

/**
 * Start the auto-refresh timer.
 */
function startAutoRefresh() {
  if (refreshTimer) clearInterval(refreshTimer);
  refreshTimer = setInterval(refreshData, REFRESH_INTERVAL);
}


/* =====================================================================
 * Initialisation
 * ===================================================================== */

document.addEventListener('DOMContentLoaded', async () => {
  // Bind refresh button
  const refreshBtn = document.getElementById('refresh-btn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', refreshData);
  }

  // Initial data load
  await refreshData();

  // Start auto-refresh
  startAutoRefresh();
});
