/**
 * FTGO - Food To Go Operations Dashboard
 * Single Page Application
 *
 * A comprehensive operations dashboard for managing orders, restaurants,
 * consumers, and couriers in the FTGO food delivery system.
 */

// ============================================================================
// Constants
// ============================================================================

const STATE_COLORS = {
  APPROVED: '#3B82F6',
  ACCEPTED: '#8B5CF6',
  PREPARING: '#F59E0B',
  READY_FOR_PICKUP: '#14B8A6',
  PICKED_UP: '#6366F1',
  DELIVERED: '#10B981',
  CANCELLED: '#EF4444'
};

const STATE_ORDER = [
  'APPROVED', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP',
  'PICKED_UP', 'DELIVERED', 'CANCELLED'
];

const STORAGE_KEY = 'ftgo_dashboard_state';

// ============================================================================
// State Management
// ============================================================================

const state = {
  orders: {},       // { orderId: orderData }
  consumers: {},    // { consumerId: consumerData }
  restaurants: {},  // { restaurantId: restaurantData }
  couriers: {},     // { courierId: courierData }
};

/** Current view preference for orders page: 'kanban' or 'list' */
let ordersViewMode = 'kanban';

/**
 * Load state from localStorage. Initializes empty objects for any missing keys.
 */
function loadState() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      state.orders = parsed.orders || {};
      state.consumers = parsed.consumers || {};
      state.restaurants = parsed.restaurants || {};
      state.couriers = parsed.couriers || {};
    }
  } catch (e) {
    console.warn('Failed to load state from localStorage:', e);
  }
}

/**
 * Persist current state to localStorage.
 */
function saveState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (e) {
    console.warn('Failed to save state to localStorage:', e);
  }
}

/**
 * Refresh all stored entities. In demo mode this is a no-op since we use
 * mock data. When connected to a real backend, this would fetch each stored
 * entity from the API and remove 404s.
 */
async function refreshAll() {
  // Demo mode: state is already populated with mock data.
  // No network calls needed.
  updateOrdersBadge();
  const currentHash = window.location.hash || '#/';
  renderCurrentRoute(currentHash);
}

// ============================================================================
// Mock API Client (Demo Mode)
// ============================================================================

/** Auto-incrementing ID counter for mock entity creation */
let _mockNextId = 100;
function _nextId() { return _mockNextId++; }

/**
 * Mock API that operates entirely against in-memory state.
 * Simulates async behaviour with a small delay for realistic feel.
 */
function _delay(ms = 120) { return new Promise(r => setTimeout(r, ms)); }

const api = {
  orders: {
    async get(id) {
      await _delay(50);
      const order = state.orders[id];
      if (!order) throw new Error('Not found');
      return order;
    },

    async create(data) {
      await _delay();
      const id = _nextId();
      const restaurant = state.restaurants[data.restaurantId];
      let orderTotal = '0.00';
      if (restaurant && restaurant.menuItems) {
        let total = 0;
        for (const li of data.lineItems) {
          const mi = restaurant.menuItems.find(m => m.id === li.menuItemId);
          if (mi) total += (parseFloat(mi.price.amount || mi.price) * li.quantity);
        }
        orderTotal = total.toFixed(2);
      }
      const order = {
        orderId: id,
        state: 'APPROVED',
        orderTotal,
        restaurantName: restaurant ? restaurant.name : `Restaurant #${data.restaurantId}`,
        restaurantId: data.restaurantId,
        consumerId: data.consumerId,
        assignedCourier: null,
        courierActions: [],
        _createdAt: new Date().toISOString()
      };
      state.orders[id] = order;
      saveState();
      return { orderId: id };
    },

    async cancel(id) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      state.orders[id].state = 'CANCELLED';
      saveState();
      return state.orders[id];
    },

    async revise(id, data) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      // In demo mode just acknowledge the revision
      saveState();
      return state.orders[id];
    },

    async accept(id, readyBy) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      state.orders[id].state = 'ACCEPTED';
      // Auto-assign an available courier
      const availableCourier = Object.values(state.couriers).find(c => c.available);
      if (availableCourier) {
        state.orders[id].assignedCourier = availableCourier.id;
        state.orders[id].courierActions = [
          { type: 'PICKUP', time: readyBy },
          { type: 'DROPOFF', time: new Date(new Date(readyBy).getTime() + 30 * 60000).toISOString() }
        ];
      }
      saveState();
      return 'accepted';
    },

    async preparing(id) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      state.orders[id].state = 'PREPARING';
      saveState();
      return 'preparing';
    },

    async ready(id) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      state.orders[id].state = 'READY_FOR_PICKUP';
      saveState();
      return 'ready for pickup';
    },

    async pickedUp(id) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      state.orders[id].state = 'PICKED_UP';
      saveState();
      return 'picked up';
    },

    async delivered(id) {
      await _delay();
      if (!state.orders[id]) throw new Error('Not found');
      state.orders[id].state = 'DELIVERED';
      saveState();
      return 'delivered';
    },

    async listByConsumer(consumerId) {
      await _delay(50);
      return Object.values(state.orders).filter(o => String(o.consumerId) === String(consumerId));
    }
  },

  consumers: {
    async get(id) {
      await _delay(50);
      const c = state.consumers[id];
      if (!c) throw new Error('Not found');
      return c;
    },

    async create(data) {
      await _delay();
      const id = _nextId();
      state.consumers[id] = { consumerId: id, ...data, _lastRefresh: Date.now() };
      saveState();
      return { consumerId: id };
    }
  },

  restaurants: {
    async get(id) {
      await _delay(50);
      const r = state.restaurants[id];
      if (!r) throw new Error('Not found');
      return r;
    },

    async create(data) {
      await _delay();
      const id = _nextId();
      state.restaurants[id] = { id, ...data, menuItems: data.menu?.menuItems || [], _lastRefresh: Date.now() };
      saveState();
      return { id };
    }
  },

  couriers: {
    async get(id) {
      await _delay(50);
      const c = state.couriers[id];
      if (!c) throw new Error('Not found');
      return c;
    },

    async create(data) {
      await _delay();
      const id = _nextId();
      state.couriers[id] = { id, ...data, available: false, plan: { actions: [] }, _lastRefresh: Date.now() };
      saveState();
      return { id };
    },

    async updateAvailability(id, available) {
      await _delay();
      if (!state.couriers[id]) throw new Error('Not found');
      state.couriers[id].available = available;
      saveState();
      return available ? 'courier available' : 'courier unavailable';
    }
  }
};

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Format a numeric amount as a dollar string.
 * @param {string|number} amount
 * @returns {string} e.g., "$12.34"
 */
function formatMoney(amount) {
  if (amount == null) return '$0.00';
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(num)) return '$0.00';
  return '$' + num.toFixed(2);
}

/**
 * Format a date string or timestamp into a readable format.
 * @param {string|number} dateStr
 * @returns {string} Formatted date string
 */
function formatDate(dateStr) {
  if (!dateStr) return '—';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  } catch {
    return dateStr;
  }
}

/**
 * Convert an order state enum value to a human-readable label.
 * @param {string} s - State like "READY_FOR_PICKUP"
 * @returns {string} Like "Ready for Pickup"
 */
function formatState(s) {
  if (!s) return 'Unknown';
  return s.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()).replace(/\bFor\b/g, 'for');
}

/**
 * Get the CSS badge class for a given order state.
 * @param {string} s
 * @returns {string} e.g., "badge badge-approved"
 */
function getStateBadgeClass(s) {
  if (!s) return 'badge';
  return `badge badge-${s.toLowerCase()}`;
}

/**
 * Get the hex color for a given order state.
 * @param {string} s
 * @returns {string} Hex color
 */
function getStateColor(s) {
  return STATE_COLORS[s] || '#6B7280';
}

/**
 * Determine the next valid action for an order state.
 * @param {string} s - Current order state
 * @returns {object|null} { label, action, needsInput } or null for terminal states
 */
function getNextAction(s) {
  switch (s) {
    case 'APPROVED':
      return { label: 'Accept Order', action: 'accept', needsInput: true };
    case 'ACCEPTED':
      return { label: 'Start Preparing', action: 'preparing', needsInput: false };
    case 'PREPARING':
      return { label: 'Ready for Pickup', action: 'ready', needsInput: false };
    case 'READY_FOR_PICKUP':
      return { label: 'Picked Up', action: 'pickedup', needsInput: false };
    case 'PICKED_UP':
      return { label: 'Delivered', action: 'delivered', needsInput: false };
    default:
      return null;
  }
}

/**
 * Generate initials from a person name object or string.
 * @param {object|string} name
 * @returns {string} e.g., "JD"
 */
function getInitials(name) {
  if (!name) return '?';
  if (typeof name === 'string') {
    return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }
  const first = (name.firstName || '')[0] || '';
  const last = (name.lastName || '')[0] || '';
  return (first + last).toUpperCase() || '?';
}

/**
 * Generate a full name string from a person name object.
 * @param {object} name - { firstName, lastName }
 * @returns {string}
 */
function getFullName(name) {
  if (!name) return 'Unknown';
  if (typeof name === 'string') return name;
  return `${name.firstName || ''} ${name.lastName || ''}`.trim() || 'Unknown';
}

/**
 * Count orders by a given state.
 * @param {string} targetState
 * @returns {number}
 */
function countOrdersByState(targetState) {
  return Object.values(state.orders).filter(o => o.state === targetState).length;
}

/**
 * Count active orders (not DELIVERED or CANCELLED).
 * @returns {number}
 */
function countActiveOrders() {
  return Object.values(state.orders).filter(
    o => o.state !== 'DELIVERED' && o.state !== 'CANCELLED'
  ).length;
}

/**
 * Update the orders badge in the sidebar to show count of active orders.
 */
function updateOrdersBadge() {
  const badge = document.getElementById('orders-badge');
  if (!badge) return;
  const count = countActiveOrders();
  if (count > 0) {
    badge.textContent = count;
    badge.style.display = '';
  } else {
    badge.style.display = 'none';
  }
}

/**
 * Get a default "ready by" time (2 hours from now) as an ISO string.
 * @returns {string}
 */
function getDefaultReadyBy() {
  const d = new Date();
  d.setHours(d.getHours() + 2);
  // Format as YYYY-MM-DDTHH:mm:ss (without timezone for the datetime-local input)
  return d.toISOString().slice(0, 19);
}

/**
 * Escape HTML special characters to prevent XSS.
 * @param {string} str
 * @returns {string}
 */
function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

// ============================================================================
// Toast Notifications
// ============================================================================

/**
 * Show a toast notification at the bottom-right of the screen.
 * @param {string} message - The message to display
 * @param {string} type - 'success', 'error', or 'info'
 */
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const iconMap = {
    success: '✓',
    error: '✕',
    info: 'ℹ'
  };

  toast.innerHTML = `
    <div class="toast-content">
      <span class="toast-icon">${iconMap[type] || iconMap.info}</span>
      <span class="toast-message">${escapeHtml(message)}</span>
    </div>
    <button class="toast-close" aria-label="Close">&times;</button>
  `;

  container.appendChild(toast);

  // Trigger entrance animation
  requestAnimationFrame(() => {
    toast.classList.add('toast-visible');
  });

  // Close button handler
  const closeBtn = toast.querySelector('.toast-close');
  closeBtn.addEventListener('click', () => removeToast(toast));

  // Auto-remove after 4 seconds
  setTimeout(() => removeToast(toast), 4000);
}

/**
 * Remove a toast element with fade-out animation.
 * @param {HTMLElement} toast
 */
function removeToast(toast) {
  if (!toast || !toast.parentNode) return;
  toast.classList.remove('toast-visible');
  toast.classList.add('toast-hiding');
  setTimeout(() => {
    if (toast.parentNode) toast.parentNode.removeChild(toast);
  }, 300);
}

// ============================================================================
// Modal System
// ============================================================================

/**
 * Open the modal with specified content.
 * @param {string} title
 * @param {string} bodyHtml
 * @param {string} footerHtml
 */
function openModal(title, bodyHtml, footerHtml = '') {
  const backdrop = document.getElementById('modal-backdrop');
  const modalTitle = document.getElementById('modal-title');
  const modalBody = document.getElementById('modal-body');
  const modalFooter = document.getElementById('modal-footer');

  modalTitle.textContent = title;
  modalBody.innerHTML = bodyHtml;
  modalFooter.innerHTML = footerHtml;

  backdrop.classList.add('modal-open');

  // Focus the first input if present
  requestAnimationFrame(() => {
    const firstInput = modalBody.querySelector('input, select, textarea');
    if (firstInput) firstInput.focus();
  });
}

/**
 * Close the modal and clear its content.
 */
function closeModal() {
  const backdrop = document.getElementById('modal-backdrop');
  backdrop.classList.remove('modal-open');

  // Clear content after animation
  setTimeout(() => {
    document.getElementById('modal-title').textContent = '';
    document.getElementById('modal-body').innerHTML = '';
    document.getElementById('modal-footer').innerHTML = '';
  }, 300);
}

// ============================================================================
// Router
// ============================================================================

/**
 * Set up the hash-based router.
 */
function setupRouter() {
  window.addEventListener('hashchange', () => {
    const hash = window.location.hash || '#/';
    renderCurrentRoute(hash);
  });
}

/**
 * Navigate to a given hash path.
 * @param {string} hash
 */
function navigateTo(hash) {
  window.location.hash = hash;
}

/**
 * Parse the current hash and render the appropriate view.
 * @param {string} hash
 */
function renderCurrentRoute(hash) {
  const content = document.getElementById('content');
  const pageTitle = document.getElementById('page-title');
  const topBarActions = document.getElementById('top-bar-actions');

  if (!content) return;

  // Determine route
  const path = hash.replace('#', '') || '/';

  // Update active nav link
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.remove('active');
  });

  if (path === '/') {
    // Dashboard
    setActiveNav('dashboard');
    pageTitle.textContent = 'Dashboard';
    topBarActions.innerHTML = '';
    renderDashboard(content);
  } else if (path === '/orders') {
    setActiveNav('orders');
    pageTitle.textContent = 'Orders';
    topBarActions.innerHTML = `
      <button class="btn btn-primary" id="btn-new-order">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16" style="margin-right:4px;vertical-align:middle;">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        New Order
      </button>
    `;
    renderOrders(content);
    document.getElementById('btn-new-order')?.addEventListener('click', openCreateOrderModal);
  } else if (path.match(/^\/orders\/(\d+)$/)) {
    const orderId = path.match(/^\/orders\/(\d+)$/)[1];
    setActiveNav('orders');
    pageTitle.textContent = `Order #${orderId}`;
    topBarActions.innerHTML = `
      <button class="btn btn-secondary" id="btn-back-orders">
        ← Back to Orders
      </button>
    `;
    renderOrderDetail(content, orderId);
    document.getElementById('btn-back-orders')?.addEventListener('click', () => navigateTo('#/orders'));
  } else if (path === '/restaurants') {
    setActiveNav('restaurants');
    pageTitle.textContent = 'Restaurants';
    topBarActions.innerHTML = `
      <button class="btn btn-primary" id="btn-add-restaurant">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16" style="margin-right:4px;vertical-align:middle;">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        Add Restaurant
      </button>
    `;
    renderRestaurants(content);
    document.getElementById('btn-add-restaurant')?.addEventListener('click', openCreateRestaurantModal);
  } else if (path === '/consumers') {
    setActiveNav('consumers');
    pageTitle.textContent = 'Consumers';
    topBarActions.innerHTML = `
      <button class="btn btn-primary" id="btn-add-consumer">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16" style="margin-right:4px;vertical-align:middle;">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        Add Consumer
      </button>
    `;
    renderConsumers(content);
    document.getElementById('btn-add-consumer')?.addEventListener('click', openCreateConsumerModal);
  } else if (path === '/couriers') {
    setActiveNav('couriers');
    pageTitle.textContent = 'Couriers';
    topBarActions.innerHTML = `
      <button class="btn btn-primary" id="btn-add-courier">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16" style="margin-right:4px;vertical-align:middle;">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        Add Courier
      </button>
    `;
    renderCouriers(content);
    document.getElementById('btn-add-courier')?.addEventListener('click', openCreateCourierModal);
  } else {
    // 404 fallback
    pageTitle.textContent = 'Not Found';
    topBarActions.innerHTML = '';
    content.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">404</div>
        <h3 class="empty-state-title">Page Not Found</h3>
        <p class="empty-state-text">The page you're looking for doesn't exist.</p>
        <a href="#/" class="btn btn-primary">Go to Dashboard</a>
      </div>
    `;
  }

  updateOrdersBadge();
}

/**
 * Set the active navigation link by data-view attribute.
 * @param {string} view
 */
function setActiveNav(view) {
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.toggle('active', link.getAttribute('data-view') === view);
  });
}

// ============================================================================
// Event Listeners Setup
// ============================================================================

/**
 * Setup global event listeners (sidebar toggle, modal close, etc.).
 */
function setupEventListeners() {
  // Sidebar toggle for mobile
  const menuToggle = document.getElementById('menu-toggle');
  const sidebar = document.getElementById('sidebar');
  if (menuToggle && sidebar) {
    menuToggle.addEventListener('click', () => {
      sidebar.classList.toggle('sidebar-open');
    });

    // Close sidebar when clicking a nav link on mobile
    sidebar.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', () => {
        sidebar.classList.remove('sidebar-open');
      });
    });
  }

  // Modal close handlers
  const modalClose = document.getElementById('modal-close');
  const modalBackdrop = document.getElementById('modal-backdrop');

  if (modalClose) {
    modalClose.addEventListener('click', closeModal);
  }
  if (modalBackdrop) {
    modalBackdrop.addEventListener('click', (e) => {
      if (e.target === modalBackdrop) closeModal();
    });
  }

  // Close modal on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });
}

// ============================================================================
// View Renderers
// ============================================================================

// ---------- Dashboard ----------

/**
 * Render the dashboard overview page.
 * @param {HTMLElement} container
 */
function renderDashboard(container) {
  const totalOrders = Object.keys(state.orders).length;
  const activeOrders = countActiveOrders();
  const totalRestaurants = Object.keys(state.restaurants).length;
  const totalCouriers = Object.keys(state.couriers).length;

  // Count orders by state for the pipeline
  const stateCounts = {};
  STATE_ORDER.forEach(s => { stateCounts[s] = 0; });
  Object.values(state.orders).forEach(o => {
    if (stateCounts[o.state] !== undefined) {
      stateCounts[o.state]++;
    }
  });

  // Recent orders (last 10, sorted by orderId descending)
  const recentOrders = Object.values(state.orders)
    .sort((a, b) => (b.orderId || 0) - (a.orderId || 0))
    .slice(0, 10);

  // Pipeline bar segments
  const pipelineTotal = totalOrders || 1; // avoid division by zero
  const pipelineSegments = STATE_ORDER.map(s => {
    const count = stateCounts[s];
    if (count === 0) return '';
    const pct = Math.max((count / pipelineTotal) * 100, 4);
    return `
      <div class="pipeline-segment" data-state="${s}" style="width:${pct}%;background-color:${getStateColor(s)}" title="${formatState(s)}: ${count}">
        <span class="pipeline-count">${count}</span>
      </div>
    `;
  }).join('');

  // Pipeline labels
  const pipelineLabels = STATE_ORDER.map(s => `
    <div class="pipeline-label">
      <span class="pipeline-dot" style="background:${getStateColor(s)}"></span>
      <span>${formatState(s)}</span>
      <strong>${stateCounts[s]}</strong>
    </div>
  `).join('');

  // Recent orders table rows
  const recentRows = recentOrders.map(o => `
    <tr class="clickable-row" data-order-id="${o.orderId}">
      <td><strong>#${o.orderId}</strong></td>
      <td>${escapeHtml(o.restaurantName || '—')}</td>
      <td><span class="${getStateBadgeClass(o.state)}">${formatState(o.state)}</span></td>
      <td>${formatMoney(o.orderTotal)}</td>
      <td>${o._lastRefresh ? formatDate(o._lastRefresh) : '—'}</td>
    </tr>
  `).join('');

  container.innerHTML = `
    <!-- Stat Cards -->
    <div class="stats-grid">
      <div class="stat-card" style="border-left-color: #3B82F6;">
        <div class="stat-card-icon" style="background: rgba(59,130,246,0.1); color: #3B82F6;">
          <svg viewBox="0 0 20 20" fill="currentColor" width="24" height="24">
            <path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm2 3h8a1 1 0 110 2H6a1 1 0 110-2zm0 4h8a1 1 0 110 2H6a1 1 0 110-2z"/>
          </svg>
        </div>
        <div class="stat-card-info">
          <div class="stat-card-value">${totalOrders}</div>
          <div class="stat-card-label">Total Orders</div>
        </div>
      </div>

      <div class="stat-card" style="border-left-color: #F59E0B;">
        <div class="stat-card-icon" style="background: rgba(245,158,11,0.1); color: #F59E0B;">
          <svg viewBox="0 0 20 20" fill="currentColor" width="24" height="24">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
          </svg>
        </div>
        <div class="stat-card-info">
          <div class="stat-card-value">${activeOrders}</div>
          <div class="stat-card-label">Active Orders</div>
        </div>
      </div>

      <div class="stat-card" style="border-left-color: #10B981;">
        <div class="stat-card-icon" style="background: rgba(16,185,129,0.1); color: #10B981;">
          <svg viewBox="0 0 20 20" fill="currentColor" width="24" height="24">
            <path d="M7 2a1 1 0 00-1 1v1H5a1 1 0 000 2h1v10a2 2 0 002 2h.5a1 1 0 001-1V6h1v11a1 1 0 001 1H12a2 2 0 002-2V6h1a1 1 0 100-2h-1V3a1 1 0 10-2 0v1H8V3a1 1 0 00-1-1z"/>
          </svg>
        </div>
        <div class="stat-card-info">
          <div class="stat-card-value">${totalRestaurants}</div>
          <div class="stat-card-label">Restaurants</div>
        </div>
      </div>

      <div class="stat-card" style="border-left-color: #8B5CF6;">
        <div class="stat-card-icon" style="background: rgba(139,92,246,0.1); color: #8B5CF6;">
          <svg viewBox="0 0 20 20" fill="currentColor" width="24" height="24">
            <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z"/>
          </svg>
        </div>
        <div class="stat-card-info">
          <div class="stat-card-value">${totalCouriers}</div>
          <div class="stat-card-label">Couriers</div>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <!-- Order Pipeline -->
      <div class="card">
        <div class="card-header">
          <h3>Order Pipeline</h3>
        </div>
        <div class="card-body">
          ${totalOrders > 0 ? `
            <div class="pipeline-bar">
              ${pipelineSegments}
            </div>
            <div class="pipeline-labels">
              ${pipelineLabels}
            </div>
          ` : `
            <p style="text-align:center;color:var(--text-secondary,#6b7280);padding:1rem 0;">No orders yet. Create your first order to see the pipeline.</p>
          `}
        </div>
      </div>

      <!-- Recent Orders -->
      <div class="card">
        <div class="card-header">
          <h3>Recent Orders</h3>
          <div class="card-header-actions">
            <a href="#/orders" class="btn btn-sm btn-outline">View All</a>
          </div>
        </div>
        <div class="card-body" style="padding:0;">
          ${recentOrders.length > 0 ? `
            <table class="orders-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Restaurant</th>
                  <th>Status</th>
                  <th>Total</th>
                  <th>Updated</th>
                </tr>
              </thead>
              <tbody>
                ${recentRows}
              </tbody>
            </table>
          ` : `
            <div class="empty-state" style="padding:2rem;">
              <div class="empty-state-icon">📋</div>
              <p class="empty-state-text">No orders yet</p>
            </div>
          `}
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="card">
        <div class="card-header">
          <h3>Quick Actions</h3>
        </div>
        <div class="card-body">
          <div class="quick-actions-grid">
            <button class="btn btn-primary btn-lg quick-action-btn" id="qa-create-order">
              <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                <path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4z"/>
              </svg>
              Create Order
            </button>
            <button class="btn btn-success btn-lg quick-action-btn" id="qa-add-restaurant">
              <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                <path d="M7 2a1 1 0 00-1 1v1H5a1 1 0 000 2h1v10a2 2 0 002 2h.5a1 1 0 001-1V6h1v11a1 1 0 001 1H12a2 2 0 002-2V6h1a1 1 0 100-2h-1V3a1 1 0 10-2 0v1H8V3a1 1 0 00-1-1z"/>
              </svg>
              Add Restaurant
            </button>
            <button class="btn btn-secondary btn-lg quick-action-btn" id="qa-add-consumer">
              <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zm8 0a3 3 0 11-6 0 3 3 0 016 0zm-4.07 11c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
              </svg>
              Add Consumer
            </button>
            <button class="btn btn-outline btn-lg quick-action-btn" id="qa-add-courier">
              <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z"/>
              </svg>
              Add Courier
            </button>
          </div>
        </div>
      </div>
    </div>
  `;

  // Attach event listeners for dashboard
  container.querySelectorAll('.clickable-row[data-order-id]').forEach(row => {
    row.addEventListener('click', () => {
      navigateTo(`#/orders/${row.dataset.orderId}`);
    });
  });

  document.getElementById('qa-create-order')?.addEventListener('click', openCreateOrderModal);
  document.getElementById('qa-add-restaurant')?.addEventListener('click', openCreateRestaurantModal);
  document.getElementById('qa-add-consumer')?.addEventListener('click', openCreateConsumerModal);
  document.getElementById('qa-add-courier')?.addEventListener('click', openCreateCourierModal);
}

// ---------- Orders ----------

/**
 * Render the Orders page with kanban or list view.
 * @param {HTMLElement} container
 */
function renderOrders(container) {
  const orders = Object.values(state.orders).sort((a, b) => (b.orderId || 0) - (a.orderId || 0));

  // View toggle
  const viewToggle = `
    <div class="view-toggle" style="margin-bottom:1rem;">
      <button class="toggle-btn ${ordersViewMode === 'kanban' ? 'active' : ''}" data-mode="kanban">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path d="M2 4a1 1 0 011-1h3a1 1 0 011 1v12a1 1 0 01-1 1H3a1 1 0 01-1-1V4zm6 0a1 1 0 011-1h3a1 1 0 011 1v12a1 1 0 01-1 1H9a1 1 0 01-1-1V4zm7-1a1 1 0 00-1 1v12a1 1 0 001 1h3a1 1 0 001-1V4a1 1 0 00-1-1h-3z"/></svg>
        Kanban
      </button>
      <button class="toggle-btn ${ordersViewMode === 'list' ? 'active' : ''}" data-mode="list">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd"/></svg>
        List
      </button>
    </div>
  `;

  if (orders.length === 0) {
    container.innerHTML = `
      ${viewToggle}
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
        </div>
        <h3 class="empty-state-title">No Orders Yet</h3>
        <p class="empty-state-text">Create your first order to get started. You'll need at least one consumer and one restaurant.</p>
        <button class="btn btn-primary" id="empty-create-order">Create Order</button>
      </div>
    `;
    attachViewToggle(container);
    document.getElementById('empty-create-order')?.addEventListener('click', openCreateOrderModal);
    return;
  }

  if (ordersViewMode === 'kanban') {
    renderOrdersKanban(container, orders, viewToggle);
  } else {
    renderOrdersList(container, orders, viewToggle);
  }

  attachViewToggle(container);
}

/**
 * Attach view toggle event listeners.
 * @param {HTMLElement} container
 */
function attachViewToggle(container) {
  container.querySelectorAll('.toggle-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      ordersViewMode = btn.dataset.mode;
      renderOrders(container);
    });
  });
}

/**
 * Render orders in a kanban board layout.
 * @param {HTMLElement} container
 * @param {Array} orders
 * @param {string} viewToggle - HTML for the view toggle buttons
 */
function renderOrdersKanban(container, orders, viewToggle) {
  // Group orders by state
  const columns = {};
  STATE_ORDER.forEach(s => { columns[s] = []; });
  orders.forEach(o => {
    if (columns[o.state]) {
      columns[o.state].push(o);
    }
  });

  const columnsHtml = STATE_ORDER.map(s => {
    const colOrders = columns[s];
    const cardsHtml = colOrders.map(o => {
      const nextAction = getNextAction(o.state);
      const consumerName = o.consumerId && state.consumers[o.consumerId]
        ? getFullName(state.consumers[o.consumerId].name)
        : (o.consumerId ? `Consumer #${o.consumerId}` : '—');

      return `
        <div class="kanban-card" data-order-id="${o.orderId}">
          <div class="kanban-card-header">
            <span class="order-id">#${o.orderId}</span>
            <span class="order-total">${formatMoney(o.orderTotal)}</span>
          </div>
          <div class="kanban-card-body">
            <div class="order-restaurant">${escapeHtml(o.restaurantName || '—')}</div>
            <div class="order-consumer">${escapeHtml(consumerName)}</div>
            <div class="order-time">${o._lastRefresh ? formatDate(o._lastRefresh) : '—'}</div>
          </div>
          ${nextAction ? `
            <div class="kanban-card-footer">
              <button class="btn btn-sm btn-primary kanban-action-btn" data-order-id="${o.orderId}" data-action="${nextAction.action}" data-needs-input="${nextAction.needsInput}">
                ${nextAction.label}
              </button>
            </div>
          ` : ''}
        </div>
      `;
    }).join('');

    return `
      <div class="kanban-column">
        <div class="kanban-column-header" style="border-top-color: ${getStateColor(s)}">
          <span class="column-title">${formatState(s)}</span>
          <span class="column-count">${colOrders.length}</span>
        </div>
        <div class="kanban-cards">
          ${cardsHtml || `<div class="kanban-empty" style="text-align:center;padding:1rem;color:var(--text-tertiary,#9CA3AF);font-size:0.85rem;">No orders</div>`}
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = `
    ${viewToggle}
    <div class="kanban-board">
      ${columnsHtml}
    </div>
  `;

  // Attach card click (navigate to detail)
  container.querySelectorAll('.kanban-card').forEach(card => {
    card.addEventListener('click', (e) => {
      // Don't navigate if clicking the action button
      if (e.target.closest('.kanban-action-btn')) return;
      navigateTo(`#/orders/${card.dataset.orderId}`);
    });
  });

  // Attach action buttons
  container.querySelectorAll('.kanban-action-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      handleOrderAction(btn.dataset.orderId, btn.dataset.action, btn.dataset.needsInput === 'true', btn);
    });
  });
}

/**
 * Render orders in a table list view.
 * @param {HTMLElement} container
 * @param {Array} orders
 * @param {string} viewToggle
 */
function renderOrdersList(container, orders, viewToggle) {
  const rows = orders.map(o => {
    const nextAction = getNextAction(o.state);
    const consumerName = o.consumerId && state.consumers[o.consumerId]
      ? getFullName(state.consumers[o.consumerId].name)
      : (o.consumerId ? `#${o.consumerId}` : '—');

    return `
      <tr class="clickable-row" data-order-id="${o.orderId}">
        <td><strong>#${o.orderId}</strong></td>
        <td>${escapeHtml(o.restaurantName || '—')}</td>
        <td>${escapeHtml(consumerName)}</td>
        <td><span class="${getStateBadgeClass(o.state)}">${formatState(o.state)}</span></td>
        <td>${formatMoney(o.orderTotal)}</td>
        <td>${o.assignedCourier ? `Courier #${o.assignedCourier}` : '—'}</td>
        <td>
          ${nextAction ? `<button class="btn btn-sm btn-primary list-action-btn" data-order-id="${o.orderId}" data-action="${nextAction.action}" data-needs-input="${nextAction.needsInput}">${nextAction.label}</button>` : '<span style="color:var(--text-tertiary,#9CA3AF);">—</span>'}
        </td>
      </tr>
    `;
  }).join('');

  container.innerHTML = `
    ${viewToggle}
    <div class="card" style="padding:0;">
      <table class="orders-table">
        <thead>
          <tr>
            <th>Order</th>
            <th>Restaurant</th>
            <th>Consumer</th>
            <th>Status</th>
            <th>Total</th>
            <th>Courier</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
    </div>
  `;

  // Attach row clicks
  container.querySelectorAll('.clickable-row[data-order-id]').forEach(row => {
    row.addEventListener('click', (e) => {
      if (e.target.closest('.list-action-btn')) return;
      navigateTo(`#/orders/${row.dataset.orderId}`);
    });
  });

  // Attach action buttons
  container.querySelectorAll('.list-action-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      handleOrderAction(btn.dataset.orderId, btn.dataset.action, btn.dataset.needsInput === 'true', btn);
    });
  });
}

// ---------- Order Detail ----------

/**
 * Render the detail view for a single order.
 * @param {HTMLElement} container
 * @param {string|number} orderId
 */
async function renderOrderDetail(container, orderId) {
  // Show loading state initially
  container.innerHTML = `
    <div class="loading-spinner" style="text-align:center;padding:3rem;">
      <div class="spinner"></div>
      <p style="margin-top:1rem;color:var(--text-secondary,#6b7280);">Loading order #${orderId}...</p>
    </div>
  `;

  // Try to fetch latest from API
  try {
    const data = await api.orders.get(orderId);
    if (data && data.orderId) {
      state.orders[orderId] = { ...state.orders[orderId], ...data, _lastRefresh: Date.now() };
      saveState();
    }
  } catch (err) {
    // If 404, it may have been deleted
    if (err.message === 'Not found') {
      delete state.orders[orderId];
      saveState();
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <h3 class="empty-state-title">Order Not Found</h3>
          <p class="empty-state-text">Order #${orderId} could not be found.</p>
          <a href="#/orders" class="btn btn-primary">Back to Orders</a>
        </div>
      `;
      return;
    }
    // For other errors, try to use cached data
  }

  const order = state.orders[orderId];
  if (!order) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h3 class="empty-state-title">Order Not Found</h3>
        <p class="empty-state-text">Order #${orderId} is not in the local cache. Try looking it up first.</p>
        <a href="#/orders" class="btn btn-primary">Back to Orders</a>
      </div>
    `;
    return;
  }

  const nextAction = getNextAction(order.state);
  const canCancel = order.state === 'APPROVED';

  // Resolve consumer name
  const consumerName = order.consumerId && state.consumers[order.consumerId]
    ? getFullName(state.consumers[order.consumerId].name)
    : (order.consumerId ? `Consumer #${order.consumerId}` : '—');

  // Build action buttons
  let actionButtons = '';
  if (nextAction) {
    actionButtons += `<button class="btn btn-primary btn-lg" id="detail-next-action" data-action="${nextAction.action}" data-needs-input="${nextAction.needsInput}">${nextAction.label}</button>`;
  }
  if (canCancel) {
    actionButtons += `<button class="btn btn-danger btn-lg" id="detail-cancel-action">Cancel Order</button>`;
  }

  // Courier info
  let courierSection = '';
  if (order.assignedCourier) {
    const courierData = state.couriers[order.assignedCourier];
    const courierName = courierData ? getFullName(courierData.name) : `Courier #${order.assignedCourier}`;

    let actionsHtml = '';
    if (order.courierActions && order.courierActions.length > 0) {
      actionsHtml = `
        <div class="detail-row">
          <span class="detail-label">Delivery Actions</span>
          <span class="detail-value">
            ${order.courierActions.map(a => `
              <span class="badge badge-${(a.type || '').toLowerCase()}" style="margin-right:0.25rem;">${a.type}${a.time ? ` at ${formatDate(a.time)}` : ''}</span>
            `).join('')}
          </span>
        </div>
      `;
    }

    courierSection = `
      <div class="detail-section">
        <h4 class="detail-section-title">Courier Assignment</h4>
        <div class="detail-row">
          <span class="detail-label">Courier</span>
          <span class="detail-value">${escapeHtml(courierName)}</span>
        </div>
        ${actionsHtml}
      </div>
    `;
  }

  container.innerHTML = `
    <div class="order-detail">
      <div class="order-detail-header">
        <div>
          <span class="order-status-large ${getStateBadgeClass(order.state)}" style="font-size:1.1rem;">${formatState(order.state)}</span>
          <h2 style="margin-top:0.5rem;">Order #${order.orderId}</h2>
        </div>
        ${actionButtons ? `<div class="order-actions">${actionButtons}</div>` : ''}
      </div>

      <div class="detail-grid">
        <div class="detail-section">
          <h4 class="detail-section-title">Order Information</h4>
          <div class="detail-row">
            <span class="detail-label">Order ID</span>
            <span class="detail-value">#${order.orderId}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">State</span>
            <span class="detail-value"><span class="${getStateBadgeClass(order.state)}">${formatState(order.state)}</span></span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Restaurant</span>
            <span class="detail-value">${escapeHtml(order.restaurantName || '—')}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Consumer</span>
            <span class="detail-value">${escapeHtml(consumerName)}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Order Total</span>
            <span class="detail-value" style="font-weight:600;font-size:1.1rem;">${formatMoney(order.orderTotal)}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Last Updated</span>
            <span class="detail-value">${order._lastRefresh ? formatDate(order._lastRefresh) : '—'}</span>
          </div>
        </div>

        ${courierSection}
      </div>

      ${order._lineItems && order._lineItems.length > 0 ? `
        <div class="detail-section" style="margin-top:1.5rem;">
          <h4 class="detail-section-title">Line Items</h4>
          <table class="line-items-table">
            <thead>
              <tr>
                <th>Item</th>
                <th>Qty</th>
                <th>Price</th>
              </tr>
            </thead>
            <tbody>
              ${order._lineItems.map(item => `
                <tr>
                  <td>${escapeHtml(item.name || item.menuItemId)}</td>
                  <td>${item.quantity}</td>
                  <td>${item.price ? formatMoney(item.price) : '—'}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      ` : ''}
    </div>
  `;

  // Attach action listeners
  const nextActionBtn = document.getElementById('detail-next-action');
  if (nextActionBtn) {
    nextActionBtn.addEventListener('click', () => {
      handleOrderAction(
        orderId,
        nextActionBtn.dataset.action,
        nextActionBtn.dataset.needsInput === 'true',
        nextActionBtn
      );
    });
  }

  const cancelBtn = document.getElementById('detail-cancel-action');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
      handleCancelOrder(orderId, cancelBtn);
    });
  }
}

// ---------- Restaurants ----------

/**
 * Render the Restaurants list page.
 * @param {HTMLElement} container
 */
function renderRestaurants(container) {
  const restaurants = Object.values(state.restaurants);

  if (restaurants.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
          </svg>
        </div>
        <h3 class="empty-state-title">No Restaurants Yet</h3>
        <p class="empty-state-text">Add your first restaurant to start accepting orders.</p>
        <button class="btn btn-primary" id="empty-add-restaurant">Add Restaurant</button>
      </div>
    `;
    document.getElementById('empty-add-restaurant')?.addEventListener('click', openCreateRestaurantModal);
    return;
  }

  const cards = restaurants.map(r => {
    const menuCount = r._menuItems ? r._menuItems.length : 0;
    const address = r._address
      ? `${r._address.street1 || ''}, ${r._address.city || ''}, ${r._address.state || ''} ${r._address.zip || ''}`
      : '—';

    return `
      <div class="entity-card">
        <div class="entity-card-header">
          <div class="entity-avatar" style="background:rgba(16,185,129,0.1);color:#10B981;">
            <svg viewBox="0 0 20 20" fill="currentColor" width="24" height="24">
              <path d="M7 2a1 1 0 00-1 1v1H5a1 1 0 000 2h1v10a2 2 0 002 2h.5a1 1 0 001-1V6h1v11a1 1 0 001 1H12a2 2 0 002-2V6h1a1 1 0 100-2h-1V3a1 1 0 10-2 0v1H8V3a1 1 0 00-1-1z"/>
            </svg>
          </div>
          <div class="entity-name">${escapeHtml(r.name || 'Unnamed Restaurant')}</div>
        </div>
        <div class="entity-card-body">
          <div class="entity-detail-row">
            <span class="entity-detail-label">ID</span>
            <span class="entity-detail-value">#${r.id}</span>
          </div>
          <div class="entity-detail-row">
            <span class="entity-detail-label">Address</span>
            <span class="entity-detail-value">${escapeHtml(address)}</span>
          </div>
          <div class="entity-detail-row">
            <span class="entity-detail-label">Menu Items</span>
            <span class="entity-detail-value">${menuCount} item${menuCount !== 1 ? 's' : ''}</span>
          </div>
        </div>
        <div class="entity-card-footer">
          <button class="btn btn-sm btn-outline view-restaurant-btn" data-id="${r.id}">View Details</button>
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = `<div class="entity-grid">${cards}</div>`;

  // View restaurant detail (show in modal)
  container.querySelectorAll('.view-restaurant-btn').forEach(btn => {
    btn.addEventListener('click', () => viewRestaurantDetail(btn.dataset.id));
  });
}

/**
 * Show restaurant details in a modal.
 * @param {string|number} restaurantId
 */
async function viewRestaurantDetail(restaurantId) {
  const r = state.restaurants[restaurantId];
  if (!r) {
    showToast('Restaurant not found in cache', 'error');
    return;
  }

  const menuItems = r._menuItems || [];
  const menuHtml = menuItems.length > 0
    ? `
      <table class="line-items-table" style="width:100%;">
        <thead><tr><th>ID</th><th>Name</th><th>Price</th></tr></thead>
        <tbody>
          ${menuItems.map(m => `<tr><td>${escapeHtml(m.id)}</td><td>${escapeHtml(m.name)}</td><td>${formatMoney(m.price?.amount || m.price)}</td></tr>`).join('')}
        </tbody>
      </table>
    `
    : '<p style="color:var(--text-secondary,#6b7280);">No menu items recorded.</p>';

  const address = r._address
    ? `${r._address.street1 || ''}, ${r._address.city || ''}, ${r._address.state || ''} ${r._address.zip || ''}`
    : 'Not recorded';

  openModal(
    `Restaurant: ${r.name || 'Unknown'}`,
    `
      <div class="detail-section">
        <div class="detail-row"><span class="detail-label">ID</span><span class="detail-value">#${r.id}</span></div>
        <div class="detail-row"><span class="detail-label">Name</span><span class="detail-value">${escapeHtml(r.name)}</span></div>
        <div class="detail-row"><span class="detail-label">Address</span><span class="detail-value">${escapeHtml(address)}</span></div>
      </div>
      <div class="detail-section" style="margin-top:1rem;">
        <h4 class="detail-section-title">Menu</h4>
        ${menuHtml}
      </div>
    `,
    `<button class="btn btn-secondary" onclick="closeModal()">Close</button>`
  );
}

// ---------- Consumers ----------

/**
 * Render the Consumers list page.
 * @param {HTMLElement} container
 */
function renderConsumers(container) {
  const consumers = Object.values(state.consumers);

  if (consumers.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
          </svg>
        </div>
        <h3 class="empty-state-title">No Consumers Yet</h3>
        <p class="empty-state-text">Add consumers who will place orders.</p>
        <button class="btn btn-primary" id="empty-add-consumer">Add Consumer</button>
      </div>
    `;
    document.getElementById('empty-add-consumer')?.addEventListener('click', openCreateConsumerModal);
    return;
  }

  // Count orders per consumer
  const orderCounts = {};
  Object.values(state.orders).forEach(o => {
    if (o.consumerId) {
      orderCounts[o.consumerId] = (orderCounts[o.consumerId] || 0) + 1;
    }
  });

  const cards = consumers.map(c => {
    const id = c.consumerId || c.id;
    const name = getFullName(c.name);
    const initials = getInitials(c.name);
    const orders = orderCounts[id] || 0;

    return `
      <div class="entity-card">
        <div class="entity-card-header">
          <div class="entity-avatar" style="background:rgba(59,130,246,0.1);color:#3B82F6;">
            ${initials}
          </div>
          <div class="entity-name">${escapeHtml(name)}</div>
        </div>
        <div class="entity-card-body">
          <div class="entity-detail-row">
            <span class="entity-detail-label">ID</span>
            <span class="entity-detail-value">#${id}</span>
          </div>
          <div class="entity-detail-row">
            <span class="entity-detail-label">Orders</span>
            <span class="entity-detail-value">${orders} order${orders !== 1 ? 's' : ''}</span>
          </div>
        </div>
        <div class="entity-card-footer">
          <button class="btn btn-sm btn-outline refresh-consumer-orders" data-consumer-id="${id}">Refresh Orders</button>
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = `<div class="entity-grid">${cards}</div>`;

  // Refresh orders for a consumer
  container.querySelectorAll('.refresh-consumer-orders').forEach(btn => {
    btn.addEventListener('click', async () => {
      const consumerId = btn.dataset.consumerId;
      btn.disabled = true;
      btn.textContent = 'Loading...';
      try {
        const orders = await api.orders.listByConsumer(consumerId);
        if (Array.isArray(orders)) {
          orders.forEach(o => {
            state.orders[o.orderId] = { ...state.orders[o.orderId], ...o, consumerId, _lastRefresh: Date.now() };
          });
          saveState();
          updateOrdersBadge();
          showToast(`Found ${orders.length} order(s) for consumer #${consumerId}`, 'success');
          renderConsumers(document.getElementById('content'));
        }
      } catch (err) {
        showToast(`Failed to fetch orders: ${err.message}`, 'error');
      }
      btn.disabled = false;
      btn.textContent = 'Refresh Orders';
    });
  });
}

// ---------- Couriers ----------

/**
 * Render the Couriers list page.
 * @param {HTMLElement} container
 */
function renderCouriers(container) {
  const couriers = Object.values(state.couriers);

  if (couriers.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
        </div>
        <h3 class="empty-state-title">No Couriers Yet</h3>
        <p class="empty-state-text">Add couriers to deliver orders to customers.</p>
        <button class="btn btn-primary" id="empty-add-courier">Add Courier</button>
      </div>
    `;
    document.getElementById('empty-add-courier')?.addEventListener('click', openCreateCourierModal);
    return;
  }

  const cards = couriers.map(c => {
    const id = c.id;
    const name = getFullName(c.name);
    const initials = getInitials(c.name);
    const isAvailable = c.available === true;
    const address = c.address
      ? `${c.address.street1 || ''}, ${c.address.city || ''}, ${c.address.state || ''} ${c.address.zip || ''}`
      : '—';

    // Check if courier has assigned deliveries
    const planActions = c.plan && c.plan.actions ? c.plan.actions : [];
    const activeDeliveries = planActions.length;

    return `
      <div class="entity-card">
        <div class="entity-card-header">
          <div class="entity-avatar" style="background:rgba(139,92,246,0.1);color:#8B5CF6;">
            ${initials}
          </div>
          <div class="entity-name">${escapeHtml(name)}</div>
        </div>
        <div class="entity-card-body">
          <div class="entity-detail-row">
            <span class="entity-detail-label">ID</span>
            <span class="entity-detail-value">#${id}</span>
          </div>
          <div class="entity-detail-row">
            <span class="entity-detail-label">Address</span>
            <span class="entity-detail-value">${escapeHtml(address)}</span>
          </div>
          <div class="entity-detail-row">
            <span class="entity-detail-label">Availability</span>
            <span class="entity-detail-value">
              <label class="availability-toggle" title="${isAvailable ? 'Available' : 'Unavailable'}">
                <input type="checkbox" class="courier-availability-toggle" data-courier-id="${id}" ${isAvailable ? 'checked' : ''}>
                <span class="toggle-slider"></span>
                <span class="toggle-label">${isAvailable ? 'Available' : 'Unavailable'}</span>
              </label>
            </span>
          </div>
          ${activeDeliveries > 0 ? `
            <div class="entity-detail-row">
              <span class="entity-detail-label">Active Deliveries</span>
              <span class="entity-detail-value">${activeDeliveries} action(s)</span>
            </div>
          ` : ''}
        </div>
        <div class="entity-card-footer">
          <button class="btn btn-sm btn-outline refresh-courier-btn" data-courier-id="${id}">Refresh</button>
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = `<div class="entity-grid">${cards}</div>`;

  // Availability toggle handlers
  container.querySelectorAll('.courier-availability-toggle').forEach(toggle => {
    toggle.addEventListener('change', async () => {
      const courierId = toggle.dataset.courierId;
      const available = toggle.checked;
      const label = toggle.parentElement.querySelector('.toggle-label');
      toggle.disabled = true;
      try {
        await api.couriers.updateAvailability(courierId, available);
        state.couriers[courierId] = { ...state.couriers[courierId], available };
        saveState();
        if (label) label.textContent = available ? 'Available' : 'Unavailable';
        showToast(`Courier #${courierId} is now ${available ? 'available' : 'unavailable'}`, 'success');
      } catch (err) {
        // Revert
        toggle.checked = !available;
        showToast(`Failed to update availability: ${err.message}`, 'error');
      }
      toggle.disabled = false;
    });
  });

  // Refresh courier handlers
  container.querySelectorAll('.refresh-courier-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const courierId = btn.dataset.courierId;
      btn.disabled = true;
      btn.textContent = 'Loading...';
      try {
        const data = await api.couriers.get(courierId);
        if (data) {
          state.couriers[courierId] = { ...state.couriers[courierId], ...data, _lastRefresh: Date.now() };
          saveState();
          showToast(`Courier #${courierId} refreshed`, 'success');
          renderCouriers(document.getElementById('content'));
        }
      } catch (err) {
        showToast(`Failed to refresh courier: ${err.message}`, 'error');
      }
      btn.disabled = false;
      btn.textContent = 'Refresh';
    });
  });
}

// ============================================================================
// Order Actions
// ============================================================================

/**
 * Handle an order state transition action.
 * @param {string|number} orderId
 * @param {string} action - The action endpoint name
 * @param {boolean} needsInput - Whether the action requires user input (e.g., readyBy)
 * @param {HTMLElement} btn - The button that was clicked (for loading state)
 */
async function handleOrderAction(orderId, action, needsInput, btn) {
  if (needsInput && action === 'accept') {
    // Show accept modal with readyBy input
    openAcceptOrderModal(orderId);
    return;
  }

  // Direct action (no input needed)
  const originalText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Processing...';

  try {
    let result;
    switch (action) {
      case 'preparing':
        result = await api.orders.preparing(orderId);
        break;
      case 'ready':
        result = await api.orders.ready(orderId);
        break;
      case 'pickedup':
        result = await api.orders.pickedUp(orderId);
        break;
      case 'delivered':
        result = await api.orders.delivered(orderId);
        break;
      default:
        throw new Error(`Unknown action: ${action}`);
    }

    showToast(`Order #${orderId}: ${result || 'Action completed'}`, 'success');

    // Refresh the order from API
    try {
      const updated = await api.orders.get(orderId);
      if (updated) {
        state.orders[orderId] = { ...state.orders[orderId], ...updated, _lastRefresh: Date.now() };
        saveState();
      }
    } catch {
      // If refresh fails, just update state optimistically
      const stateMap = {
        preparing: 'PREPARING',
        ready: 'READY_FOR_PICKUP',
        pickedup: 'PICKED_UP',
        delivered: 'DELIVERED'
      };
      if (state.orders[orderId] && stateMap[action]) {
        state.orders[orderId].state = stateMap[action];
        state.orders[orderId]._lastRefresh = Date.now();
        saveState();
      }
    }

    updateOrdersBadge();

    // Re-render the current view
    const hash = window.location.hash || '#/';
    renderCurrentRoute(hash);
  } catch (err) {
    showToast(`Failed: ${err.message}`, 'error');
    btn.disabled = false;
    btn.textContent = originalText;
  }
}

/**
 * Handle cancelling an order.
 * @param {string|number} orderId
 * @param {HTMLElement} btn
 */
async function handleCancelOrder(orderId, btn) {
  const originalText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Cancelling...';

  try {
    const result = await api.orders.cancel(orderId);
    if (result && result.orderId) {
      state.orders[orderId] = { ...state.orders[orderId], ...result, _lastRefresh: Date.now() };
    } else {
      state.orders[orderId].state = 'CANCELLED';
      state.orders[orderId]._lastRefresh = Date.now();
    }
    saveState();
    updateOrdersBadge();
    showToast(`Order #${orderId} has been cancelled`, 'success');

    // Re-render
    const hash = window.location.hash || '#/';
    renderCurrentRoute(hash);
  } catch (err) {
    showToast(`Failed to cancel: ${err.message}`, 'error');
    btn.disabled = false;
    btn.textContent = originalText;
  }
}

/**
 * Show the accept order modal with a readyBy time input.
 * @param {string|number} orderId
 */
function openAcceptOrderModal(orderId) {
  const defaultTime = getDefaultReadyBy();

  openModal(
    `Accept Order #${orderId}`,
    `
      <div class="form-group">
        <label class="form-label" for="accept-ready-by">Ready By Time</label>
        <input type="datetime-local" class="form-input" id="accept-ready-by" value="${defaultTime}">
        <p class="form-help">When the order will be ready. Defaults to 2 hours from now.</p>
      </div>
    `,
    `
      <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary" id="confirm-accept-order">Accept Order</button>
    `
  );

  document.getElementById('confirm-accept-order')?.addEventListener('click', async () => {
    const readyByInput = document.getElementById('accept-ready-by');
    const readyBy = readyByInput ? readyByInput.value : defaultTime;

    const btn = document.getElementById('confirm-accept-order');
    btn.disabled = true;
    btn.textContent = 'Accepting...';

    try {
      await api.orders.accept(orderId, readyBy);
      showToast(`Order #${orderId} accepted`, 'success');

      // Refresh from API
      try {
        const updated = await api.orders.get(orderId);
        if (updated) {
          state.orders[orderId] = { ...state.orders[orderId], ...updated, _lastRefresh: Date.now() };
        }
      } catch {
        state.orders[orderId].state = 'ACCEPTED';
        state.orders[orderId]._lastRefresh = Date.now();
      }

      saveState();
      updateOrdersBadge();
      closeModal();

      // Re-render
      const hash = window.location.hash || '#/';
      renderCurrentRoute(hash);
    } catch (err) {
      showToast(`Failed to accept: ${err.message}`, 'error');
      btn.disabled = false;
      btn.textContent = 'Accept Order';
    }
  });
}

// ============================================================================
// Create Entity Modals
// ============================================================================

// ---------- Create Order Modal ----------

/**
 * Open the Create Order modal form.
 * Requires at least one consumer and one restaurant in state.
 */
function openCreateOrderModal() {
  const consumers = Object.entries(state.consumers);
  const restaurants = Object.entries(state.restaurants);

  if (consumers.length === 0 || restaurants.length === 0) {
    showToast('You need at least one consumer and one restaurant to create an order.', 'info');
    return;
  }

  const consumerOptions = consumers.map(([id, c]) => {
    const name = getFullName(c.name);
    return `<option value="${id}">${name} (#${id})</option>`;
  }).join('');

  const restaurantOptions = restaurants.map(([id, r]) => {
    return `<option value="${id}">${escapeHtml(r.name || 'Unnamed')} (#${id})</option>`;
  }).join('');

  openModal(
    'Create New Order',
    `
      <div class="form-group">
        <label class="form-label" for="order-consumer">Consumer</label>
        <select class="form-select" id="order-consumer">
          <option value="">Select a consumer...</option>
          ${consumerOptions}
        </select>
      </div>
      <div class="form-group">
        <label class="form-label" for="order-restaurant">Restaurant</label>
        <select class="form-select" id="order-restaurant">
          <option value="">Select a restaurant...</option>
          ${restaurantOptions}
        </select>
      </div>
      <div id="order-menu-items-container" style="display:none;">
        <h4 class="detail-section-title" style="margin:1rem 0 0.5rem;">Menu Items</h4>
        <p class="form-help">Set quantity for items you want to include (0 = skip).</p>
        <div id="order-menu-items"></div>
      </div>
    `,
    `
      <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary" id="confirm-create-order" disabled>Create Order</button>
    `
  );

  const restaurantSelect = document.getElementById('order-restaurant');
  const menuContainer = document.getElementById('order-menu-items-container');
  const menuItemsDiv = document.getElementById('order-menu-items');
  const createBtn = document.getElementById('confirm-create-order');

  // When restaurant is selected, show its menu items
  restaurantSelect?.addEventListener('change', () => {
    const restaurantId = restaurantSelect.value;
    if (!restaurantId) {
      menuContainer.style.display = 'none';
      menuItemsDiv.innerHTML = '';
      createBtn.disabled = true;
      return;
    }

    const restaurant = state.restaurants[restaurantId];
    const menuItems = restaurant?._menuItems || [];

    if (menuItems.length === 0) {
      menuContainer.style.display = 'block';
      menuItemsDiv.innerHTML = `
        <p style="color:var(--text-secondary,#6b7280);">No menu items found for this restaurant. You can enter a menu item ID and quantity manually.</p>
        <div class="menu-item-row" style="margin-top:0.5rem;">
          <div class="form-row">
            <div class="form-group" style="flex:1;">
              <label class="form-label">Menu Item ID</label>
              <input type="text" class="form-input menu-item-id" placeholder="e.g. 1">
            </div>
            <div class="form-group" style="flex:0 0 80px;">
              <label class="form-label">Qty</label>
              <input type="number" class="form-input menu-item-qty" min="0" value="1">
            </div>
          </div>
        </div>
      `;
      createBtn.disabled = false;
    } else {
      menuContainer.style.display = 'block';
      menuItemsDiv.innerHTML = menuItems.map(item => `
        <div class="menu-item-row" data-item-id="${escapeHtml(item.id)}">
          <div class="form-row">
            <div class="form-group" style="flex:1;">
              <label class="form-label">${escapeHtml(item.name)} (${formatMoney(item.price?.amount || item.price)})</label>
            </div>
            <div class="form-group" style="flex:0 0 80px;">
              <label class="form-label">Qty</label>
              <input type="number" class="form-input menu-item-qty" min="0" value="0" data-item-id="${escapeHtml(item.id)}">
            </div>
          </div>
        </div>
      `).join('');
      createBtn.disabled = false;
    }
  });

  // Create order handler
  createBtn?.addEventListener('click', async () => {
    const consumerId = document.getElementById('order-consumer')?.value;
    const restaurantId = document.getElementById('order-restaurant')?.value;

    if (!consumerId || !restaurantId) {
      showToast('Please select a consumer and restaurant', 'error');
      return;
    }

    // Collect line items
    const lineItems = [];
    const restaurant = state.restaurants[restaurantId];
    const menuItems = restaurant?._menuItems || [];

    if (menuItems.length === 0) {
      // Manual entry mode
      const idInput = menuItemsDiv.querySelector('.menu-item-id');
      const qtyInput = menuItemsDiv.querySelector('.menu-item-qty');
      if (idInput && qtyInput) {
        const id = idInput.value.trim();
        const qty = parseInt(qtyInput.value) || 0;
        if (id && qty > 0) {
          lineItems.push({ menuItemId: id, quantity: qty });
        }
      }
    } else {
      // Known menu items
      menuItemsDiv.querySelectorAll('.menu-item-qty[data-item-id]').forEach(input => {
        const qty = parseInt(input.value) || 0;
        if (qty > 0) {
          lineItems.push({ menuItemId: input.dataset.itemId, quantity: qty });
        }
      });
    }

    if (lineItems.length === 0) {
      showToast('Please select at least one menu item with quantity > 0', 'error');
      return;
    }

    createBtn.disabled = true;
    createBtn.textContent = 'Creating...';

    try {
      const result = await api.orders.create({
        consumerId: parseInt(consumerId),
        restaurantId: parseInt(restaurantId),
        lineItems
      });

      const newOrderId = result.orderId;
      showToast(`Order #${newOrderId} created successfully!`, 'success');

      // Store initial order data with the line items we sent
      state.orders[newOrderId] = {
        orderId: newOrderId,
        state: 'APPROVED',
        consumerId: parseInt(consumerId),
        restaurantId: parseInt(restaurantId),
        restaurantName: restaurant?.name || null,
        _lineItems: lineItems.map(li => {
          const menuItem = menuItems.find(m => m.id === li.menuItemId);
          return {
            menuItemId: li.menuItemId,
            quantity: li.quantity,
            name: menuItem?.name || li.menuItemId,
            price: menuItem?.price?.amount || menuItem?.price || null
          };
        }),
        _lastRefresh: Date.now()
      };

      // Try to get full order data from API
      try {
        const orderData = await api.orders.get(newOrderId);
        if (orderData) {
          state.orders[newOrderId] = {
            ...state.orders[newOrderId],
            ...orderData,
            _lineItems: state.orders[newOrderId]._lineItems,
            _lastRefresh: Date.now()
          };
        }
      } catch {
        // Keep the local data we have
      }

      saveState();
      updateOrdersBadge();
      closeModal();

      // Navigate to orders
      navigateTo('#/orders');
    } catch (err) {
      showToast(`Failed to create order: ${err.message}`, 'error');
      createBtn.disabled = false;
      createBtn.textContent = 'Create Order';
    }
  });
}

// ---------- Create Restaurant Modal ----------

/**
 * Open the Create Restaurant modal form.
 */
function openCreateRestaurantModal() {
  openModal(
    'Add Restaurant',
    `
      <div class="form-group">
        <label class="form-label" for="restaurant-name">Restaurant Name</label>
        <input type="text" class="form-input" id="restaurant-name" placeholder="e.g. My Restaurant" required>
      </div>

      <h4 class="detail-section-title" style="margin:1rem 0 0.5rem;">Address</h4>
      <div class="form-group">
        <label class="form-label" for="restaurant-street">Street</label>
        <input type="text" class="form-input" id="restaurant-street" placeholder="e.g. 1 Main St">
      </div>
      <div class="form-row">
        <div class="form-group" style="flex:2;">
          <label class="form-label" for="restaurant-city">City</label>
          <input type="text" class="form-input" id="restaurant-city" placeholder="e.g. Oakland">
        </div>
        <div class="form-group" style="flex:1;">
          <label class="form-label" for="restaurant-state">State</label>
          <input type="text" class="form-input" id="restaurant-state" placeholder="e.g. CA" maxlength="2">
        </div>
        <div class="form-group" style="flex:1;">
          <label class="form-label" for="restaurant-zip">ZIP</label>
          <input type="text" class="form-input" id="restaurant-zip" placeholder="e.g. 94619">
        </div>
      </div>

      <h4 class="detail-section-title" style="margin:1rem 0 0.5rem;">Menu Items</h4>
      <div id="menu-items-list">
        <div class="menu-item-row" data-index="0">
          <div class="form-row">
            <div class="form-group" style="flex:0 0 60px;">
              <label class="form-label">ID</label>
              <input type="text" class="form-input mi-id" placeholder="1" value="1">
            </div>
            <div class="form-group" style="flex:2;">
              <label class="form-label">Name</label>
              <input type="text" class="form-input mi-name" placeholder="Chicken Vindaloo">
            </div>
            <div class="form-group" style="flex:1;">
              <label class="form-label">Price</label>
              <input type="number" class="form-input mi-price" placeholder="12.34" step="0.01" min="0">
            </div>
            <div class="form-group" style="flex:0 0 40px;display:flex;align-items:flex-end;">
              <button class="btn btn-sm btn-danger btn-icon btn-remove" title="Remove" style="margin-bottom:0.5rem;">&times;</button>
            </div>
          </div>
        </div>
      </div>
      <button class="btn btn-sm btn-outline btn-add-item" id="add-menu-item">+ Add Menu Item</button>
    `,
    `
      <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary" id="confirm-create-restaurant">Create Restaurant</button>
    `
  );

  let menuItemIndex = 1;
  const menuItemsList = document.getElementById('menu-items-list');

  // Add menu item row
  document.getElementById('add-menu-item')?.addEventListener('click', () => {
    menuItemIndex++;
    const row = document.createElement('div');
    row.className = 'menu-item-row';
    row.dataset.index = menuItemIndex;
    row.innerHTML = `
      <div class="form-row">
        <div class="form-group" style="flex:0 0 60px;">
          <label class="form-label">ID</label>
          <input type="text" class="form-input mi-id" placeholder="${menuItemIndex}" value="${menuItemIndex}">
        </div>
        <div class="form-group" style="flex:2;">
          <label class="form-label">Name</label>
          <input type="text" class="form-input mi-name" placeholder="Item name">
        </div>
        <div class="form-group" style="flex:1;">
          <label class="form-label">Price</label>
          <input type="number" class="form-input mi-price" placeholder="9.99" step="0.01" min="0">
        </div>
        <div class="form-group" style="flex:0 0 40px;display:flex;align-items:flex-end;">
          <button class="btn btn-sm btn-danger btn-icon btn-remove" title="Remove" style="margin-bottom:0.5rem;">&times;</button>
        </div>
      </div>
    `;
    menuItemsList.appendChild(row);
  });

  // Remove menu item row (event delegation)
  menuItemsList?.addEventListener('click', (e) => {
    if (e.target.closest('.btn-remove')) {
      const row = e.target.closest('.menu-item-row');
      if (menuItemsList.children.length > 1) {
        row.remove();
      } else {
        showToast('At least one menu item is required', 'info');
      }
    }
  });

  // Create restaurant handler
  document.getElementById('confirm-create-restaurant')?.addEventListener('click', async () => {
    const name = document.getElementById('restaurant-name')?.value?.trim();
    const street1 = document.getElementById('restaurant-street')?.value?.trim();
    const city = document.getElementById('restaurant-city')?.value?.trim();
    const st = document.getElementById('restaurant-state')?.value?.trim();
    const zip = document.getElementById('restaurant-zip')?.value?.trim();

    if (!name) {
      showToast('Restaurant name is required', 'error');
      return;
    }

    // Collect menu items
    const menuItems = [];
    menuItemsList.querySelectorAll('.menu-item-row').forEach(row => {
      const id = row.querySelector('.mi-id')?.value?.trim();
      const itemName = row.querySelector('.mi-name')?.value?.trim();
      const price = parseFloat(row.querySelector('.mi-price')?.value) || 0;

      if (id && itemName && price > 0) {
        menuItems.push({ id, name: itemName, price: { amount: price } });
      }
    });

    if (menuItems.length === 0) {
      showToast('Add at least one valid menu item (ID, name, and price required)', 'error');
      return;
    }

    const btn = document.getElementById('confirm-create-restaurant');
    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
      const result = await api.restaurants.create({
        name,
        address: { street1, city, state: st, zip },
        menu: { menuItems }
      });

      const newId = result.id;
      showToast(`Restaurant "${name}" created (ID: #${newId})`, 'success');

      state.restaurants[newId] = {
        id: newId,
        name,
        _address: { street1, city, state: st, zip },
        _menuItems: menuItems,
        _lastRefresh: Date.now()
      };
      saveState();
      closeModal();
      navigateTo('#/restaurants');
    } catch (err) {
      showToast(`Failed to create restaurant: ${err.message}`, 'error');
      btn.disabled = false;
      btn.textContent = 'Create Restaurant';
    }
  });
}

// ---------- Create Consumer Modal ----------

/**
 * Open the Create Consumer modal form.
 */
function openCreateConsumerModal() {
  openModal(
    'Add Consumer',
    `
      <div class="form-group">
        <label class="form-label" for="consumer-first-name">First Name</label>
        <input type="text" class="form-input" id="consumer-first-name" placeholder="John" required>
      </div>
      <div class="form-group">
        <label class="form-label" for="consumer-last-name">Last Name</label>
        <input type="text" class="form-input" id="consumer-last-name" placeholder="Doe" required>
      </div>
    `,
    `
      <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary" id="confirm-create-consumer">Create Consumer</button>
    `
  );

  document.getElementById('confirm-create-consumer')?.addEventListener('click', async () => {
    const firstName = document.getElementById('consumer-first-name')?.value?.trim();
    const lastName = document.getElementById('consumer-last-name')?.value?.trim();

    if (!firstName || !lastName) {
      showToast('Both first and last name are required', 'error');
      return;
    }

    const btn = document.getElementById('confirm-create-consumer');
    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
      const result = await api.consumers.create({
        name: { firstName, lastName }
      });

      const newId = result.consumerId;
      showToast(`Consumer "${firstName} ${lastName}" created (ID: #${newId})`, 'success');

      state.consumers[newId] = {
        consumerId: newId,
        name: { firstName, lastName },
        _lastRefresh: Date.now()
      };
      saveState();
      closeModal();
      navigateTo('#/consumers');
    } catch (err) {
      showToast(`Failed to create consumer: ${err.message}`, 'error');
      btn.disabled = false;
      btn.textContent = 'Create Consumer';
    }
  });
}

// ---------- Create Courier Modal ----------

/**
 * Open the Create Courier modal form.
 */
function openCreateCourierModal() {
  openModal(
    'Add Courier',
    `
      <div class="form-row">
        <div class="form-group" style="flex:1;">
          <label class="form-label" for="courier-first-name">First Name</label>
          <input type="text" class="form-input" id="courier-first-name" placeholder="Jane" required>
        </div>
        <div class="form-group" style="flex:1;">
          <label class="form-label" for="courier-last-name">Last Name</label>
          <input type="text" class="form-input" id="courier-last-name" placeholder="Courier" required>
        </div>
      </div>

      <h4 class="detail-section-title" style="margin:1rem 0 0.5rem;">Address</h4>
      <div class="form-group">
        <label class="form-label" for="courier-street">Street</label>
        <input type="text" class="form-input" id="courier-street" placeholder="e.g. 1 Scenic Dr">
      </div>
      <div class="form-row">
        <div class="form-group" style="flex:2;">
          <label class="form-label" for="courier-city">City</label>
          <input type="text" class="form-input" id="courier-city" placeholder="e.g. Oakland">
        </div>
        <div class="form-group" style="flex:1;">
          <label class="form-label" for="courier-state">State</label>
          <input type="text" class="form-input" id="courier-state" placeholder="CA" maxlength="2">
        </div>
        <div class="form-group" style="flex:1;">
          <label class="form-label" for="courier-zip">ZIP</label>
          <input type="text" class="form-input" id="courier-zip" placeholder="94555">
        </div>
      </div>
    `,
    `
      <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary" id="confirm-create-courier">Create Courier</button>
    `
  );

  document.getElementById('confirm-create-courier')?.addEventListener('click', async () => {
    const firstName = document.getElementById('courier-first-name')?.value?.trim();
    const lastName = document.getElementById('courier-last-name')?.value?.trim();
    const street1 = document.getElementById('courier-street')?.value?.trim();
    const city = document.getElementById('courier-city')?.value?.trim();
    const st = document.getElementById('courier-state')?.value?.trim();
    const zip = document.getElementById('courier-zip')?.value?.trim();

    if (!firstName || !lastName) {
      showToast('Both first and last name are required', 'error');
      return;
    }

    const btn = document.getElementById('confirm-create-courier');
    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
      const result = await api.couriers.create({
        name: { firstName, lastName },
        address: { street1, city, state: st, zip }
      });

      const newId = result.id;
      showToast(`Courier "${firstName} ${lastName}" created (ID: #${newId})`, 'success');

      state.couriers[newId] = {
        id: newId,
        name: { firstName, lastName },
        address: { street1, city, state: st, zip },
        available: false,
        _lastRefresh: Date.now()
      };
      saveState();
      closeModal();
      navigateTo('#/couriers');
    } catch (err) {
      showToast(`Failed to create courier: ${err.message}`, 'error');
      btn.disabled = false;
      btn.textContent = 'Create Courier';
    }
  });
}

// ============================================================================
// Initialization
// ============================================================================

// ============================================================================
// Demo Seed Data
// ============================================================================

/**
 * Populate state with realistic mock data if empty (first load or cleared).
 * This gives the dashboard a rich, demo-ready appearance out of the box.
 */
function seedDemoData() {
  // Only seed if state is empty (no restaurants means fresh start)
  if (Object.keys(state.restaurants).length > 0) return;

  const now = new Date();
  const ago = (mins) => new Date(now.getTime() - mins * 60000).toISOString();
  const ahead = (mins) => new Date(now.getTime() + mins * 60000).toISOString();

  // --- Restaurants ---
  state.restaurants = {
    1: {
      id: 1, name: 'Taj Palace', address: { street1: '42 Curry Lane', city: 'Oakland', state: 'CA', zip: '94612' },
      menuItems: [
        { id: '1', name: 'Chicken Vindaloo', price: { amount: 16.50 } },
        { id: '2', name: 'Lamb Biryani', price: { amount: 18.95 } },
        { id: '3', name: 'Paneer Tikka Masala', price: { amount: 14.75 } },
        { id: '4', name: 'Garlic Naan (2pc)', price: { amount: 4.50 } }
      ], _lastRefresh: Date.now()
    },
    2: {
      id: 2, name: 'Sakura Sushi', address: { street1: '88 Ocean Ave', city: 'San Francisco', state: 'CA', zip: '94110' },
      menuItems: [
        { id: '10', name: 'Dragon Roll', price: { amount: 15.00 } },
        { id: '11', name: 'Salmon Nigiri (6pc)', price: { amount: 12.50 } },
        { id: '12', name: 'Miso Ramen', price: { amount: 14.00 } },
        { id: '13', name: 'Edamame', price: { amount: 5.50 } }
      ], _lastRefresh: Date.now()
    },
    3: {
      id: 3, name: 'Bella Napoli', address: { street1: '15 Elm Street', city: 'Berkeley', state: 'CA', zip: '94704' },
      menuItems: [
        { id: '20', name: 'Margherita Pizza', price: { amount: 13.00 } },
        { id: '21', name: 'Penne Arrabbiata', price: { amount: 12.50 } },
        { id: '22', name: 'Tiramisu', price: { amount: 8.00 } },
        { id: '23', name: 'Bruschetta', price: { amount: 9.50 } }
      ], _lastRefresh: Date.now()
    },
    4: {
      id: 4, name: 'Golden Dragon', address: { street1: '200 Grant Ave', city: 'San Francisco', state: 'CA', zip: '94108' },
      menuItems: [
        { id: '30', name: 'Kung Pao Chicken', price: { amount: 14.50 } },
        { id: '31', name: 'Beef Chow Fun', price: { amount: 13.75 } },
        { id: '32', name: 'Har Gow (8pc)', price: { amount: 10.00 } },
        { id: '33', name: 'Char Siu Bao (3pc)', price: { amount: 7.50 } }
      ], _lastRefresh: Date.now()
    },
    5: {
      id: 5, name: 'El Camino Taqueria', address: { street1: '330 Mission St', city: 'San Francisco', state: 'CA', zip: '94105' },
      menuItems: [
        { id: '40', name: 'Carne Asada Burrito', price: { amount: 12.00 } },
        { id: '41', name: 'Fish Tacos (3pc)', price: { amount: 11.50 } },
        { id: '42', name: 'Chips & Guacamole', price: { amount: 7.00 } },
        { id: '43', name: 'Churros', price: { amount: 5.50 } }
      ], _lastRefresh: Date.now()
    }
  };

  // --- Consumers ---
  state.consumers = {
    1: { consumerId: 1, name: { firstName: 'Alice', lastName: 'Chen' }, _lastRefresh: Date.now() },
    2: { consumerId: 2, name: { firstName: 'Bob', lastName: 'Martinez' }, _lastRefresh: Date.now() },
    3: { consumerId: 3, name: { firstName: 'Sarah', lastName: 'Johnson' }, _lastRefresh: Date.now() },
    4: { consumerId: 4, name: { firstName: 'David', lastName: 'Kim' }, _lastRefresh: Date.now() },
    5: { consumerId: 5, name: { firstName: 'Emily', lastName: 'Patel' }, _lastRefresh: Date.now() },
    6: { consumerId: 6, name: { firstName: 'Marcus', lastName: 'Williams' }, _lastRefresh: Date.now() },
    7: { consumerId: 7, name: { firstName: 'Olivia', lastName: 'Brown' }, _lastRefresh: Date.now() }
  };

  // --- Couriers ---
  state.couriers = {
    1: { id: 1, name: { firstName: 'Jake', lastName: 'Thompson' }, address: { street1: '5 Scenic Dr', city: 'Oakland', state: 'CA', zip: '94555' }, available: true, plan: { actions: [] }, _lastRefresh: Date.now() },
    2: { id: 2, name: { firstName: 'Maria', lastName: 'Gonzalez' }, address: { street1: '12 Bay Blvd', city: 'San Francisco', state: 'CA', zip: '94107' }, available: true, plan: { actions: [] }, _lastRefresh: Date.now() },
    3: { id: 3, name: { firstName: 'Tyler', lastName: 'Brooks' }, address: { street1: '77 Cedar St', city: 'Berkeley', state: 'CA', zip: '94702' }, available: false, plan: { actions: [] }, _lastRefresh: Date.now() },
    4: { id: 4, name: { firstName: 'Priya', lastName: 'Sharma' }, address: { street1: '300 Market St', city: 'San Francisco', state: 'CA', zip: '94114' }, available: true, plan: { actions: [] }, _lastRefresh: Date.now() }
  };

  // --- Orders (spread across various states for a lively dashboard) ---
  state.orders = {
    1001: {
      orderId: 1001, state: 'DELIVERED', orderTotal: '37.95', restaurantName: 'Taj Palace',
      restaurantId: 1, consumerId: 1, assignedCourier: 1,
      courierActions: [{ type: 'PICKUP', time: ago(180) }, { type: 'DROPOFF', time: ago(150) }],
      _createdAt: ago(240)
    },
    1002: {
      orderId: 1002, state: 'DELIVERED', orderTotal: '27.50', restaurantName: 'Sakura Sushi',
      restaurantId: 2, consumerId: 3, assignedCourier: 2,
      courierActions: [{ type: 'PICKUP', time: ago(120) }, { type: 'DROPOFF', time: ago(90) }],
      _createdAt: ago(200)
    },
    1003: {
      orderId: 1003, state: 'PICKED_UP', orderTotal: '42.00', restaurantName: 'Bella Napoli',
      restaurantId: 3, consumerId: 2, assignedCourier: 1,
      courierActions: [{ type: 'PICKUP', time: ago(20) }, { type: 'DROPOFF', time: ahead(10) }],
      _createdAt: ago(65)
    },
    1004: {
      orderId: 1004, state: 'READY_FOR_PICKUP', orderTotal: '28.25', restaurantName: 'Golden Dragon',
      restaurantId: 4, consumerId: 5, assignedCourier: 4,
      courierActions: [{ type: 'PICKUP', time: ahead(5) }, { type: 'DROPOFF', time: ahead(35) }],
      _createdAt: ago(55)
    },
    1005: {
      orderId: 1005, state: 'PREPARING', orderTotal: '23.50', restaurantName: 'El Camino Taqueria',
      restaurantId: 5, consumerId: 4, assignedCourier: 2,
      courierActions: [{ type: 'PICKUP', time: ahead(15) }, { type: 'DROPOFF', time: ahead(45) }],
      _createdAt: ago(40)
    },
    1006: {
      orderId: 1006, state: 'ACCEPTED', orderTotal: '33.50', restaurantName: 'Taj Palace',
      restaurantId: 1, consumerId: 6, assignedCourier: 3,
      courierActions: [{ type: 'PICKUP', time: ahead(25) }, { type: 'DROPOFF', time: ahead(55) }],
      _createdAt: ago(30)
    },
    1007: {
      orderId: 1007, state: 'APPROVED', orderTotal: '15.00', restaurantName: 'Sakura Sushi',
      restaurantId: 2, consumerId: 7, assignedCourier: null, courierActions: [],
      _createdAt: ago(10)
    },
    1008: {
      orderId: 1008, state: 'APPROVED', orderTotal: '26.00', restaurantName: 'Bella Napoli',
      restaurantId: 3, consumerId: 1, assignedCourier: null, courierActions: [],
      _createdAt: ago(5)
    },
    1009: {
      orderId: 1009, state: 'CANCELLED', orderTotal: '14.50', restaurantName: 'Golden Dragon',
      restaurantId: 4, consumerId: 2, assignedCourier: null, courierActions: [],
      _createdAt: ago(300)
    },
    1010: {
      orderId: 1010, state: 'DELIVERED', orderTotal: '55.00', restaurantName: 'El Camino Taqueria',
      restaurantId: 5, consumerId: 3, assignedCourier: 4,
      courierActions: [{ type: 'PICKUP', time: ago(360) }, { type: 'DROPOFF', time: ago(330) }],
      _createdAt: ago(420)
    },
    1011: {
      orderId: 1011, state: 'PREPARING', orderTotal: '31.75', restaurantName: 'Sakura Sushi',
      restaurantId: 2, consumerId: 5, assignedCourier: 1,
      courierActions: [{ type: 'PICKUP', time: ahead(20) }, { type: 'DROPOFF', time: ahead(50) }],
      _createdAt: ago(25)
    },
    1012: {
      orderId: 1012, state: 'DELIVERED', orderTotal: '19.50', restaurantName: 'Taj Palace',
      restaurantId: 1, consumerId: 4, assignedCourier: 2,
      courierActions: [{ type: 'PICKUP', time: ago(500) }, { type: 'DROPOFF', time: ago(470) }],
      _createdAt: ago(560)
    }
  };

  saveState();
}

// ============================================================================
// Initialization
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
  loadState();
  seedDemoData();
  setupRouter();
  setupEventListeners();

  // Render initial route
  const hash = window.location.hash || '#/';
  renderCurrentRoute(hash);

  // Update orders badge immediately
  updateOrdersBadge();

  // Background refresh (no-op in demo mode, but keeps pattern for real backend)
  refreshAll();
});
