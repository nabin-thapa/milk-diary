// INITIALIZE STATE & LOCAL DATABASE
let db = {
  records: [], // { id, date, type: 'purchase'|'sale', milkType: 'cow'|'buffalo', liters, rate, total, contactId, paid: true|false }
  contacts: [], // { id, name, phone, address, role: 'customer'|'supplier' }
  rates: { cow: 85, buffalo: 115 },
  darkMode: false
};

// SELECTORS
const DOM = {
  body: document.body,
  lblGreeting: document.getElementById('lbl-greeting'),
  lblTodayDate: document.getElementById('lbl-today-date'),
  cardTodayEarnings: document.getElementById('card-today-earnings'),
  cardCowLiters: document.getElementById('card-cow-liters'),
  cardBufLiters: document.getElementById('card-buf-liters'),
  cardOutstanding: document.getElementById('card-outstanding'),
  cardTodayProfit: document.getElementById('card-today-profit'),
  emptyState: document.getElementById('empty-state-container'),
  
  // Tanks
  tankCowFill: document.getElementById('tank-cow-fill'),
  tankCowPercent: document.getElementById('tank-cow-percent'),
  tankCowLiters: document.getElementById('tank-cow-liters'),
  tankBufFill: document.getElementById('tank-buf-fill'),
  tankBufPercent: document.getElementById('tank-buf-percent'),
  tankBufLiters: document.getElementById('tank-buf-liters'),
  tankTotalFill: document.getElementById('tank-total-fill'),
  tankTotalPercent: document.getElementById('tank-total-percent'),
  tankTotalLiters: document.getElementById('tank-total-liters'),
  
  // Finances
  profitRevenue: document.getElementById('profit-revenue'),
  profitPurchases: document.getElementById('profit-purchases'),
  profitNet: document.getElementById('profit-net'),
  profitBarToday: document.getElementById('profit-bar-today'),
  profitBarValToday: document.getElementById('profit-bar-val-today'),
  
  // Month overview
  monthTotalLiters: document.getElementById('month-total-liters'),
  monthTotalEarnings: document.getElementById('month-total-earnings'),
  monthTotalOutstanding: document.getElementById('month-total-outstanding'),
  
  // Modals & Forms
  addEntryOverlay: document.getElementById('add-entry-overlay'),
  formEntryType: document.getElementById('form-entry-type'),
  bottomSheetTitle: document.getElementById('bottom-sheet-title'),
  formLabelContact: document.getElementById('form-label-contact'),
  formContactSelect: document.getElementById('form-contact-select'),
  formLiters: document.getElementById('form-liters'),
  formRate: document.getElementById('form-rate'),
  formLiveTotal: document.getElementById('form-live-total'),
  formSalePaidGroup: document.getElementById('form-sale-paid-group'),
  formSalePaid: document.getElementById('form-sale-paid'),
  formMilkCow: document.getElementById('form-milk-cow'),
  formMilkBuf: document.getElementById('form-milk-buf'),
  
  // Contacts Modal
  contactFormOverlay: document.getElementById('contact-form-overlay'),
  contactName: document.getElementById('contact-name'),
  contactPhone: document.getElementById('contact-phone'),
  contactAddress: document.getElementById('contact-address'),
  contactsScreenTitle: document.getElementById('contacts-screen-title'),
  btnSegmentCustomers: document.getElementById('btn-segment-customers'),
  btnSegmentSuppliers: document.getElementById('btn-segment-suppliers'),
  
  // Navigation Screens & Tabs
  screens: document.querySelectorAll('.screen-section'),
  navItems: document.querySelectorAll('.nav-item'),
  statusTime: document.getElementById('status-time'),
  
  // Lists
  transactionList: document.getElementById('transaction-list-container'),
  contactList: document.getElementById('contact-list-container'),
  historySearchInput: document.getElementById('history-search-input'),
  contactsSearchInput: document.getElementById('contacts-search-input'),
  
  // Reports
  lblReportMonth: document.getElementById('lbl-report-month'),
  reportTotalLiters: document.getElementById('report-total-liters'),
  reportTotalRevenue: document.getElementById('report-total-revenue'),
  svgReportChart: document.getElementById('svg-report-chart'),
  reportAvgSold: document.getElementById('report-avg-sold'),
  reportBufSold: document.getElementById('report-buf-sold'),
  reportCowSold: document.getElementById('report-cow-sold'),
  reportOutstanding: document.getElementById('report-outstanding'),
  
  // Profile settings
  settingsDarkMode: document.getElementById('settings-dark-mode'),
  rateCow: document.getElementById('rate-cow'),
  rateBuf: document.getElementById('rate-buf'),
  
  // Showcase view
  btnToggleShowcase: document.getElementById('btn-toggle-showcase-view'),
  designSystemView: document.getElementById('design-system-view'),
  toggleShowcaseDark: document.getElementById('showcase-toggle-dark')
};

// FORMAT UTILS
const Format = {
  money: (amount) => `Rs. ${parseFloat(amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
  liters: (vol) => `${parseFloat(vol).toFixed(2)} L`,
  dbDate: (d) => d.toISOString().split('T')[0],
  displayDate: (dateStr) => {
    const options = { month: 'short', day: 'numeric', year: 'numeric' };
    return new Date(dateStr).toLocaleDateString('en-US', options);
  }
};

// SEED MOCK DATA
const SAMPLE_CONTACTS = [
  { id: 'c1', name: 'Krishna Prasad', phone: '9841234567', address: 'Baluwatar, KTM', role: 'customer' },
  { id: 'c2', name: 'Sita Devi Shrestha', phone: '9803124567', address: 'Satdobato, Lalitpur', role: 'customer' },
  { id: 'c3', name: 'Hari Bahadur Thapa', phone: '9851023456', address: 'Koteshwor, KTM', role: 'customer' },
  { id: 's1', name: 'Gopal Dairy Farm', phone: '9813567890', address: 'Bhaktapur', role: 'supplier' },
  { id: 's2', name: 'Organo Buffalo Breed', phone: '9845012345', address: 'Kirtipur', role: 'supplier' },
  { id: 's3', name: 'Himalayan Cow Ranch', phone: '9808987654', address: 'Godavari', role: 'supplier' }
];

const SAMPLE_RECORDS = [
  { id: 'r1', date: '2026-06-12', type: 'purchase', milkType: 'cow', liters: 45, rate: 85, total: 3825, contactId: 's3', paid: true },
  { id: 'r2', date: '2026-06-12', type: 'purchase', milkType: 'buffalo', liters: 35, rate: 115, total: 4025, contactId: 's2', paid: true },
  { id: 'r3', date: '2026-06-12', type: 'sale', milkType: 'cow', liters: 30, rate: 110, total: 3300, contactId: 'c1', paid: true },
  { id: 'r4', date: '2026-06-12', type: 'sale', milkType: 'buffalo', liters: 25, rate: 150, total: 3750, contactId: 'c2', paid: false },
  
  // Previous Days for Reports & Weekly Graph
  { id: 'r5', date: '2026-06-11', type: 'purchase', milkType: 'cow', liters: 40, rate: 85, total: 3400, contactId: 's3', paid: true },
  { id: 'r6', date: '2026-06-11', type: 'sale', milkType: 'cow', liters: 38, rate: 110, total: 4180, contactId: 'c3', paid: true },
  { id: 'r7', date: '2026-06-10', type: 'purchase', milkType: 'buffalo', liters: 30, rate: 115, total: 3450, contactId: 's2', paid: true },
  { id: 'r8', date: '2026-06-10', type: 'sale', milkType: 'buffalo', liters: 28, rate: 148, total: 4144, contactId: 'c1', paid: true },
  { id: 'r9', date: '2026-06-09', type: 'purchase', milkType: 'cow', liters: 50, rate: 85, total: 4250, contactId: 's3', paid: true },
  { id: 'r10', date: '2026-06-09', type: 'sale', milkType: 'cow', liters: 45, rate: 112, total: 5040, contactId: 'c2', paid: true },
  { id: 'r11', date: '2026-06-08', type: 'purchase', milkType: 'buffalo', liters: 42, rate: 115, total: 4830, contactId: 's1', paid: true },
  { id: 'r12', date: '2026-06-08', type: 'sale', milkType: 'buffalo', liters: 40, rate: 150, total: 6000, contactId: 'c3', paid: true }
];

// STATE MANAGEMENT
function loadDb() {
  const data = localStorage.getItem('milk_diary_db');
  if (data) {
    db = JSON.parse(data);
  } else {
    // Default seed
    db.contacts = [...SAMPLE_CONTACTS];
    db.records = [...SAMPLE_RECORDS];
    saveDb();
  }
  
  // Theme check
  if (db.darkMode) {
    DOM.body.classList.add('dark-mode');
    DOM.settingsDarkMode.checked = true;
  }
}

function saveDb() {
  localStorage.setItem('milk_diary_db', JSON.stringify(db));
}

// APP ENTRY & INITIALIZATION
window.addEventListener('DOMContentLoaded', () => {
  loadDb();
  updateTime();
  setInterval(updateTime, 1000);
  
  // Set date headers
  const todayStr = Format.displayDate(new Date());
  DOM.lblTodayDate.textContent = todayStr;
  
  // Greeting based on hour
  const hour = new Date().getHours();
  let greeting = 'Good Morning';
  if (hour >= 12 && hour < 17) greeting = 'Good Afternoon';
  else if (hour >= 17) greeting = 'Good Evening';
  DOM.lblGreeting.textContent = `${greeting}, Nabin 👋`;
  
  // Hook up event listeners
  DOM.settingsDarkMode.addEventListener('change', (e) => {
    toggleDarkMode(e.target.checked);
  });
  DOM.toggleShowcaseDark.addEventListener('click', () => {
    const isDark = !DOM.body.classList.contains('dark-mode');
    DOM.settingsDarkMode.checked = isDark;
    toggleDarkMode(isDark);
  });
  
  document.getElementById('showcase-seed').addEventListener('click', () => {
    db.contacts = [...SAMPLE_CONTACTS];
    db.records = [...SAMPLE_RECORDS];
    saveDb();
    refreshAll();
    showToast('Sample data seeded!');
  });
  
  document.getElementById('showcase-clear').addEventListener('click', () => {
    db.records = [];
    saveDb();
    refreshAll();
    showToast('All transaction records cleared!');
  });

  DOM.btnToggleShowcase.addEventListener('click', toggleDesignSystemView);
  
  // Form input changes
  DOM.rateCow.addEventListener('input', (e) => {
    db.rates.cow = parseFloat(e.target.value) || 85;
    saveDb();
  });
  DOM.rateBuf.addEventListener('input', (e) => {
    db.rates.buffalo = parseFloat(e.target.value) || 115;
    saveDb();
  });

  DOM.historySearchInput.addEventListener('input', renderHistory);
  DOM.contactsSearchInput.addEventListener('input', renderContacts);

  // Initial render
  refreshAll();
});

function updateTime() {
  const now = new Date();
  let hrs = now.getHours();
  let mins = now.getMinutes();
  hrs = hrs < 10 ? '0' + hrs : hrs;
  mins = mins < 10 ? '0' + mins : mins;
  DOM.statusTime.textContent = `${hrs}:${mins}`;
}

function toggleDarkMode(isDark) {
  db.darkMode = isDark;
  saveDb();
  if (isDark) {
    DOM.body.classList.add('dark-mode');
  } else {
    DOM.body.classList.remove('dark-mode');
  }
}

function toggleDesignSystemView() {
  const visible = DOM.designSystemView.style.display === 'flex';
  DOM.designSystemView.style.display = visible ? 'none' : 'flex';
}

function showToast(msg) {
  // Simple toast popup inside mobile simulator
  const toast = document.createElement('div');
  toast.className = 'toast-alert';
  toast.textContent = msg;
  DOM.phoneScreen.appendChild(toast);
  setTimeout(() => toast.classList.add('active'), 50);
  setTimeout(() => {
    toast.classList.remove('active');
    setTimeout(() => toast.remove(), 300);
  }, 2200);
}

// Styles injection for toast popup
const style = document.createElement('style');
style.textContent = `
  .toast-alert {
    position: absolute;
    bottom: 90px;
    left: 50%;
    transform: translate(-50%, 20px);
    background: rgba(15, 23, 42, 0.9);
    backdrop-filter: blur(10px);
    color: white;
    padding: 10px 18px;
    border-radius: 12px;
    font-size: 0.8rem;
    font-weight: 600;
    pointer-events: none;
    opacity: 0;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    z-index: 1000;
    box-shadow: 0 8px 20px rgba(0,0,0,0.2);
    text-align: center;
    width: 80%;
  }
  .toast-alert.active {
    opacity: 1;
    transform: translate(-50%, 0);
  }
`;
document.head.appendChild(style);

// CORE DATA COMPUTATION
let currentReportMonth = new Date().getMonth(); // 0-11
let currentReportYear = new Date().getFullYear();

function refreshAll() {
  renderDashboard();
  renderHistory();
  renderContacts();
  renderReports();
}

function renderDashboard() {
  const todayStr = Format.dbDate(new Date());
  
  // Filter today's records
  const todayRecords = db.records.filter(r => r.date === todayStr);
  const purchases = todayRecords.filter(r => r.type === 'purchase');
  const sales = todayRecords.filter(r => r.type === 'sale');
  
  // 1. Volumes
  const cowPurchaseLiters = purchases.filter(r => r.milkType === 'cow').reduce((sum, r) => sum + r.liters, 0);
  const bufPurchaseLiters = purchases.filter(r => r.milkType === 'buffalo').reduce((sum, r) => sum + r.liters, 0);
  const totalPurchaseLiters = cowPurchaseLiters + bufPurchaseLiters;
  const totalPurchaseCost = purchases.reduce((sum, r) => sum + r.total, 0);
  
  const cowSoldLiters = sales.filter(r => r.milkType === 'cow').reduce((sum, r) => sum + r.liters, 0);
  const bufSoldLiters = sales.filter(r => r.milkType === 'buffalo').reduce((sum, r) => sum + r.liters, 0);
  const totalSoldLiters = cowSoldLiters + bufSoldLiters;
  
  const totalEarnings = sales.reduce((sum, r) => sum + r.total, 0);
  const outstanding = sales.filter(r => !r.paid).reduce((sum, r) => sum + r.total, 0);
  
  // 2. Profit Aggregation
  // Cost of sold milk = total purchased cost * (sold liters / purchased liters)
  // Or simply average purchase rate * sold liters
  const avgPurchaseRate = totalPurchaseLiters > 0 ? (totalPurchaseCost / totalPurchaseLiters) : 0;
  const costOfSoldMilk = totalSoldLiters * avgPurchaseRate;
  const netProfit = totalEarnings > 0 ? (totalEarnings - costOfSoldMilk) : 0;
  
  // 3. Stock Level Remaining
  const stockCow = Math.max(0, cowPurchaseLiters - cowSoldLiters);
  const stockBuf = Math.max(0, bufPurchaseLiters - bufSoldLiters);
  const stockTotal = Math.max(0, totalPurchaseLiters - totalSoldLiters);

  // 4. Update Counters (With count animation)
  animateValue(DOM.cardTodayEarnings, totalEarnings, 'currency');
  DOM.cardCowLiters.textContent = Format.liters(cowPurchaseLiters);
  DOM.cardBufLiters.textContent = Format.liters(bufPurchaseLiters);
  animateValue(DOM.cardOutstanding, outstanding, 'currency');
  animateValue(DOM.cardTodayProfit, netProfit, 'currency');
  
  // Toggle empty state if no entries exist
  if (todayRecords.length === 0) {
    DOM.emptyState.style.display = 'block';
  } else {
    DOM.emptyState.style.display = 'none';
  }

  // 5. Update Milk Stocks (Tanks)
  const maxCapacity = 100; // Peak volume indicator
  
  const cowPct = Math.min(100, Math.round((stockCow / maxCapacity) * 100));
  DOM.tankCowFill.style.height = `${cowPct}%`;
  DOM.tankCowPercent.textContent = `${cowPct}%`;
  DOM.tankCowLiters.textContent = Format.liters(stockCow);
  
  const bufPct = Math.min(100, Math.round((stockBuf / maxCapacity) * 100));
  DOM.tankBufFill.style.height = `${bufPct}%`;
  DOM.tankBufPercent.textContent = `${bufPct}%`;
  DOM.tankBufLiters.textContent = Format.liters(stockBuf);

  const totalPct = Math.min(100, Math.round((stockTotal / maxCapacity) * 100));
  DOM.tankTotalFill.style.height = `${totalPct}%`;
  DOM.tankTotalPercent.textContent = `${totalPct}%`;
  DOM.tankTotalLiters.textContent = Format.liters(stockTotal);

  // 6. Finances section card update
  DOM.profitRevenue.textContent = Format.money(totalEarnings);
  DOM.profitPurchases.textContent = Format.money(costOfSoldMilk);
  DOM.profitNet.textContent = Format.money(netProfit);
  
  // Update today's bar on graph
  const todayProfitHeight = Math.min(95, Math.max(10, Math.round((netProfit / 5000) * 100)));
  DOM.profitBarToday.style.height = `${todayProfitHeight || 15}%`;
  DOM.profitBarValToday.textContent = netProfit > 0 ? `${(netProfit/1000).toFixed(1)}K` : '0';

  // 7. This Month quick totals
  const currentMonthPrefix = new Date().toISOString().substring(0, 7); // "YYYY-MM"
  const monthlyRecords = db.records.filter(r => r.date.startsWith(currentMonthPrefix));
  
  const mPurchases = monthlyRecords.filter(r => r.type === 'purchase');
  const mSales = monthlyRecords.filter(r => r.type === 'sale');
  
  const mTotalLiters = mPurchases.reduce((sum, r) => sum + r.liters, 0) + mSales.reduce((sum, r) => sum + r.liters, 0);
  const mTotalEarnings = mSales.reduce((sum, r) => sum + r.total, 0);
  const mTotalOutstanding = mSales.filter(r => !r.paid).reduce((sum, r) => sum + r.total, 0);
  
  DOM.monthTotalLiters.textContent = Format.liters(mTotalLiters);
  DOM.monthTotalEarnings.textContent = Format.money(mTotalEarnings);
  DOM.monthTotalOutstanding.textContent = Format.money(mTotalOutstanding);
}

// COUNT UP NUMBERS ANIMATION
function animateValue(obj, endVal, formatType) {
  let start = 0;
  const duration = 800; // ms
  const startTime = performance.now();
  
  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    
    // Ease out expo
    const easeProgress = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
    const currentVal = start + (endVal - start) * easeProgress;
    
    if (formatType === 'currency') {
      obj.textContent = Format.money(currentVal);
    } else {
      obj.textContent = currentVal.toFixed(2);
    }
    
    if (progress < 1) {
      requestAnimationFrame(update);
    }
  }
  requestAnimationFrame(update);
}

// NAVIGATION TABS SWITCHING
let currentActiveTab = 'home';
function switchTab(tabId, subTab) {
  // Inactive all screens
  DOM.screens.forEach(s => s.classList.remove('active'));
  DOM.navItems.forEach(n => n.classList.remove('active'));
  
  // Active selected
  const screen = document.getElementById(`screen-${tabId}`);
  if (screen) screen.classList.add('active');
  
  const navBtn = document.getElementById(`nav-${tabId}`);
  if (navBtn) navBtn.classList.add('active');
  
  currentActiveTab = tabId;
  
  // Sub-navigation triggers
  if (tabId === 'customers' && subTab === 'suppliers') {
    filterContacts('suppliers');
  } else if (tabId === 'customers') {
    filterContacts('customers');
  }
  
  // Re-run animation checks on reports
  if (tabId === 'reports') {
    renderReports();
  }
}

// BOTTOM SHEET MODAL ACTIONS
let activeFormMilkType = 'cow';
function openAddEntryModal(type = 'purchase') {
  DOM.formEntryType.value = type;
  DOM.formLiters.value = '';
  DOM.formRate.value = '';
  DOM.formLiveTotal.textContent = 'Rs. 0.00';
  
  if (type === 'purchase') {
    DOM.bottomSheetTitle.textContent = 'Add Purchased Record';
    DOM.formLabelContact.textContent = 'Supplier Source';
    DOM.formSalePaidGroup.style.display = 'none';
  } else {
    DOM.bottomSheetTitle.textContent = 'Add Sale Record';
    DOM.formLabelContact.textContent = 'Customer Target';
    DOM.formSalePaidGroup.style.display = 'flex';
  }
  
  setFormMilkType('cow');
  populateContactsDropdown(type === 'purchase' ? 'supplier' : 'customer');
  
  DOM.addEntryOverlay.classList.add('active');
}

function closeAddEntryModal() {
  DOM.addEntryOverlay.classList.remove('active');
}

function setFormMilkType(type) {
  activeFormMilkType = type;
  if (type === 'cow') {
    DOM.formMilkCow.classList.add('active');
    DOM.formMilkBuf.classList.remove('active');
    DOM.formRate.value = db.rates.cow;
  } else {
    DOM.formMilkCow.classList.remove('active');
    DOM.formMilkBuf.classList.add('active');
    DOM.formRate.value = db.rates.buffalo;
  }
  calculateLiveTotal();
}

function populateContactsDropdown(role) {
  DOM.formContactSelect.innerHTML = '';
  const filtered = db.contacts.filter(c => c.role === role);
  if (filtered.length === 0) {
    const opt = document.createElement('option');
    opt.textContent = 'No contacts found. Create one first!';
    opt.disabled = true;
    DOM.formContactSelect.appendChild(opt);
    return;
  }
  filtered.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id;
    opt.textContent = `${c.name} (${c.phone})`;
    DOM.formContactSelect.appendChild(opt);
  });
}

function calculateLiveTotal() {
  const liters = parseFloat(DOM.formLiters.value) || 0;
  const rate = parseFloat(DOM.formRate.value) || 0;
  DOM.formLiveTotal.textContent = Format.money(liters * rate);
}

// SAVE ENTRY FORM SUBMISSION
function saveEntry(e) {
  e.preventDefault();
  const type = DOM.formEntryType.value;
  const liters = parseFloat(DOM.formLiters.value) || 0;
  const rate = parseFloat(DOM.formRate.value) || 0;
  const total = liters * rate;
  const contactId = DOM.formContactSelect.value;
  const paid = type === 'purchase' ? true : DOM.formSalePaid.checked;
  
  if (!contactId || liters <= 0 || rate <= 0) {
    showToast('Please check inputs');
    return;
  }
  
  const entry = {
    id: 'rec_' + Date.now(),
    date: Format.dbDate(new Date()),
    type: type,
    milkType: activeFormMilkType,
    liters: liters,
    rate: rate,
    total: total,
    contactId: contactId,
    paid: paid
  };
  
  db.records.unshift(entry);
  saveDb();
  closeAddEntryModal();
  refreshAll();
  showToast(`${type === 'purchase' ? 'Purchase' : 'Sale'} recorded successfully!`);
}

// TRANSACTIONS HISTORY VIEW
let historyFilter = 'all';
function filterHistory(filterType) {
  historyFilter = filterType;
  const btns = document.querySelectorAll('#history-segment .segment-btn');
  btns.forEach(btn => {
    btn.classList.remove('active');
    if (btn.textContent.toLowerCase().includes(filterType) || (filterType === 'all' && btn.textContent.toLowerCase() === 'all')) {
      btn.classList.add('active');
    }
  });
  renderHistory();
}

function renderHistory() {
  DOM.transactionList.innerHTML = '';
  const searchVal = DOM.historySearchInput.value.toLowerCase();
  
  let list = db.records;
  if (historyFilter !== 'all') {
    list = list.filter(r => r.type === historyFilter);
  }
  
  if (searchVal) {
    list = list.filter(r => {
      const contact = db.contacts.find(c => c.id === r.contactId);
      const name = contact ? contact.name.toLowerCase() : '';
      return r.date.includes(searchVal) || 
             r.milkType.includes(searchVal) || 
             r.type.includes(searchVal) ||
             name.includes(searchVal);
    });
  }
  
  if (list.length === 0) {
    DOM.transactionList.innerHTML = `
      <div class="empty-list-indicator" style="text-align:center; padding:30px; color:var(--text-hint);">
        No matching records found.
      </div>`;
    return;
  }
  
  list.forEach((r, idx) => {
    const contact = db.contacts.find(c => c.id === r.contactId);
    const contactName = contact ? contact.name : 'Unknown';
    const isPurchase = r.type === 'purchase';
    
    const div = document.createElement('div');
    div.className = 'transaction-item';
    div.style.animationDelay = `${idx * 50}ms`;
    
    div.innerHTML = `
      <div class="tx-left">
        <div class="tx-icon ${isPurchase ? 'purchase' : 'sale'}">
          ${isPurchase ? '📥' : '📤'}
        </div>
        <div class="tx-details">
          <h4>${contactName}</h4>
          <p>${Format.displayDate(r.date)} • ${r.milkType.toUpperCase()} Milk</p>
          <span class="tx-meta-info">${Format.liters(r.liters)} @ Rs.${r.rate}/L</span>
        </div>
      </div>
      <div class="tx-right">
        <span class="tx-amount ${isPurchase ? 'purchase' : 'sale'}">
          ${isPurchase ? '-' : '+'}${Format.money(r.total)}
        </span>
        <span class="badge-status ${r.paid ? 'paid' : 'unpaid'}">
          ${r.paid ? 'Paid' : 'Unpaid'}
        </span>
      </div>
    `;
    DOM.transactionList.appendChild(div);
  });
}

// CONTACT DIRECTORY VIEW
let contactsFilter = 'customers';
function filterContacts(filterType) {
  contactsFilter = filterType;
  if (filterType === 'customers') {
    DOM.btnSegmentCustomers.classList.add('active');
    DOM.btnSegmentSuppliers.classList.remove('active');
    DOM.contactsScreenTitle.textContent = 'Customer Directory';
  } else {
    DOM.btnSegmentCustomers.classList.remove('active');
    DOM.btnSegmentSuppliers.classList.add('active');
    DOM.contactsScreenTitle.textContent = 'Supplier Directory';
  }
  renderContacts();
}

function renderContacts() {
  DOM.contactList.innerHTML = '';
  const searchVal = DOM.contactsSearchInput.value.toLowerCase();
  
  const roleType = contactsFilter === 'customers' ? 'customer' : 'supplier';
  let list = db.contacts.filter(c => c.role === roleType);
  
  if (searchVal) {
    list = list.filter(c => c.name.toLowerCase().includes(searchVal) || c.phone.includes(searchVal));
  }
  
  if (list.length === 0) {
    DOM.contactList.innerHTML = `
      <div class="empty-list-indicator" style="text-align:center; padding:30px; color:var(--text-hint);">
        No ${roleType}s listed yet.
      </div>`;
    return;
  }
  
  list.forEach(c => {
    const div = document.createElement('div');
    div.className = 'contact-item';
    
    // Get total transactions & outstanding
    const personalRecords = db.records.filter(r => r.contactId === c.id);
    const totalVolume = personalRecords.reduce((sum, r) => sum + r.liters, 0);
    const outstandingVal = personalRecords.filter(r => !r.paid).reduce((sum, r) => sum + r.total, 0);
    
    div.innerHTML = `
      <div class="tx-left">
        <div class="contact-avatar">
          ${c.name.charAt(0)}
        </div>
        <div class="contact-info">
          <h4>${c.name}</h4>
          <p>📞 ${c.phone} • ${c.address || 'No Address'}</p>
          <p style="font-size:0.65rem; color:var(--text-hint); margin-top:2px;">
            Total: ${Format.liters(totalVolume)} | Pending: <strong class="warning">${Format.money(outstandingVal)}</strong>
          </p>
        </div>
      </div>
      <div class="contact-actions">
        <button class="btn-contact-action" onclick="callContact('${c.phone}')" aria-label="Call">📞</button>
        <button class="btn-contact-action" onclick="deleteContact('${c.id}')" aria-label="Delete">🗑️</button>
      </div>
    `;
    DOM.contactList.appendChild(div);
  });
}

function callContact(phone) {
  showToast(`Simulating phone call connection to ${phone}...`);
}

function deleteContact(id) {
  db.contacts = db.contacts.filter(c => c.id !== id);
  // Also clean transactions or mark them
  saveDb();
  refreshAll();
  showToast('Contact deleted');
}

// NEW CONTACT DIALOG
function openContactFormModal() {
  DOM.contactName.value = '';
  DOM.contactPhone.value = '';
  DOM.contactAddress.value = '';
  DOM.contactFormOverlay.classList.add('active');
}

function closeContactFormModal() {
  DOM.contactFormOverlay.classList.remove('active');
}

function saveContact(e) {
  e.preventDefault();
  const name = DOM.contactName.value.trim();
  const phone = DOM.contactPhone.value.trim();
  const address = DOM.contactAddress.value.trim();
  const role = contactsFilter === 'customers' ? 'customer' : 'supplier';
  
  if (!name || !phone) return;
  
  const contact = {
    id: (role === 'customer' ? 'c_' : 's_') + Date.now(),
    name: name,
    phone: phone,
    address: address,
    role: role
  };
  
  db.contacts.push(contact);
  saveDb();
  closeContactFormModal();
  refreshAll();
  showToast(`Added ${name} as a ${role}`);
}

// MONTHLY REPORTS SCREEN & GRAPHING
const MONTH_NAMES = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

function adjustReportMonth(dir) {
  currentReportMonth += dir;
  if (currentReportMonth < 0) {
    currentReportMonth = 11;
    currentReportYear -= 1;
  } else if (currentReportMonth > 11) {
    currentReportMonth = 0;
    currentReportYear += 1;
  }
  renderReports();
}

function renderReports() {
  const monthName = MONTH_NAMES[currentReportMonth];
  DOM.lblReportMonth.textContent = `${monthName} ${currentReportYear}`;
  
  // Format month query e.g. "2026-06"
  const monthStr = `${currentReportYear}-${(currentReportMonth + 1).toString().padStart(2, '0')}`;
  const monthRecords = db.records.filter(r => r.date.startsWith(monthStr));
  
  const purchases = monthRecords.filter(r => r.type === 'purchase');
  const sales = monthRecords.filter(r => r.type === 'sale');
  
  const totalLiters = purchases.reduce((sum, r) => sum + r.liters, 0) + sales.reduce((sum, r) => sum + r.liters, 0);
  const totalRevenue = sales.reduce((sum, r) => sum + r.total, 0);
  const totalOutstanding = sales.filter(r => !r.paid).reduce((sum, r) => sum + r.total, 0);
  
  DOM.reportTotalLiters.textContent = Format.liters(totalLiters);
  DOM.reportTotalRevenue.textContent = Format.money(totalRevenue);
  DOM.reportOutstanding.textContent = Format.money(totalOutstanding);
  
  // Details
  const cowSold = sales.filter(r => r.milkType === 'cow').reduce((sum, r) => sum + r.liters, 0);
  const bufSold = sales.filter(r => r.milkType === 'buffalo').reduce((sum, r) => sum + r.liters, 0);
  const daysInMonth = new Date(currentReportYear, currentReportMonth + 1, 0).getDate();
  const avgSold = sales.reduce((sum, r) => sum + r.liters, 0) / daysInMonth;
  
  DOM.reportCowSold.textContent = Format.liters(cowSold);
  DOM.reportBufSold.textContent = Format.liters(bufSold);
  DOM.reportAvgSold.textContent = Format.liters(avgSold);
  
  // Draw Graph: sales by date in month
  // Create dataset for days 1 to daysInMonth
  const daysData = Array(daysInMonth).fill(0);
  sales.forEach(r => {
    const day = parseInt(r.date.split('-')[2]);
    if (day >= 1 && day <= daysInMonth) {
      daysData[day - 1] += r.liters;
    }
  });
  
  drawSVGReportChart(daysData);
}

function drawSVGReportChart(data) {
  DOM.svgReportChart.innerHTML = '';
  
  const width = 320;
  const height = 180;
  const padding = 25;
  const chartWidth = width - padding * 2;
  const chartHeight = height - padding * 2;
  
  const maxVal = Math.max(...data, 10); // Minimum scale baseline
  
  // Draw grid lines
  for (let i = 0; i <= 4; i++) {
    const y = padding + (chartHeight * i) / 4;
    const gridVal = (maxVal * (4 - i)) / 4;
    
    // Text label
    const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
    text.setAttribute("x", padding - 5);
    text.setAttribute("y", y + 4);
    text.setAttribute("text-anchor", "end");
    text.setAttribute("fill", "var(--text-hint)");
    text.setAttribute("font-size", "8");
    text.textContent = Math.round(gridVal);
    DOM.svgReportChart.appendChild(text);
    
    // Line
    const line = document.createElementNS("http://www.w3.org/2000/svg", "line");
    line.setAttribute("x1", padding);
    line.setAttribute("y1", y);
    line.setAttribute("x2", width - padding);
    line.setAttribute("y2", y);
    line.setAttribute("stroke", "var(--divider-color)");
    line.setAttribute("stroke-width", "1");
    DOM.svgReportChart.appendChild(line);
  }
  
  // Draw path for chart
  let points = '';
  const step = chartWidth / (data.length - 1);
  data.forEach((val, idx) => {
    const x = padding + idx * step;
    const y = padding + chartHeight * (1 - val / maxVal);
    points += `${x},${y} `;
    
    // Add micro point circle
    if (idx % 3 === 0 || idx === data.length - 1) {
      const circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
      circle.setAttribute("cx", x);
      circle.setAttribute("cy", y);
      circle.setAttribute("r", 2);
      circle.setAttribute("fill", "var(--primary-color)");
      DOM.svgReportChart.appendChild(circle);
    }
  });
  
  // Polyline for line
  const polyline = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
  polyline.setAttribute("fill", "none");
  polyline.setAttribute("stroke", "var(--primary-color)");
  polyline.setAttribute("stroke-width", "2.5");
  polyline.setAttribute("stroke-linecap", "round");
  polyline.setAttribute("stroke-linejoin", "round");
  polyline.setAttribute("points", points.trim());
  DOM.svgReportChart.appendChild(polyline);
  
  // Area fill below chart
  const areaPoints = `${padding},${padding + chartHeight} ${points.trim()} ${width - padding},${padding + chartHeight}`;
  const polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
  polygon.setAttribute("points", areaPoints);
  
  // Create gradient
  const defs = document.createElementNS("http://www.w3.org/2000/svg", "defs");
  const grad = document.createElementNS("http://www.w3.org/2000/svg", "linearGradient");
  grad.setAttribute("id", "svg-chart-grad");
  grad.setAttribute("x1", "0");
  grad.setAttribute("y1", "0");
  grad.setAttribute("x2", "0");
  grad.setAttribute("y2", "1");
  
  const stop1 = document.createElementNS("http://www.w3.org/2000/svg", "stop");
  stop1.setAttribute("offset", "0%");
  stop1.setAttribute("stop-color", "var(--primary-color)");
  stop1.setAttribute("stop-opacity", "0.25");
  
  const stop2 = document.createElementNS("http://www.w3.org/2000/svg", "stop");
  stop2.setAttribute("offset", "100%");
  stop2.setAttribute("stop-color", "var(--primary-color)");
  stop2.setAttribute("stop-opacity", "0.0");
  
  grad.appendChild(stop1);
  grad.appendChild(stop2);
  defs.appendChild(grad);
  DOM.svgReportChart.appendChild(defs);
  
  polygon.setAttribute("fill", "url(#svg-chart-grad)");
  DOM.svgReportChart.appendChild(polygon);
}

// PROFILE ACTIONS (Export & Backup Simulation)
function exportDataCSV() {
  let csvContent = "data:text/csv;charset=utf-8,ID,Date,Type,MilkType,Liters,Rate,TotalAmount,PaidStatus\n";
  db.records.forEach(r => {
    csvContent += `${r.id},${r.date},${r.type},${r.milkType},${r.liters},${r.rate},${r.total},${r.paid}\n`;
  });
  
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", `MilkDiary_Backup_${Format.dbDate(new Date())}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  
  showToast('Simulating CSV database export complete! 📥');
}

function backupRestoreDatabase() {
  showToast('Synchronized database backups successfully! 🔄');
}
