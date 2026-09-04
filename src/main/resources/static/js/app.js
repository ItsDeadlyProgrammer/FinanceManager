// Syfe Personal Finance Manager Client Application

let currentUserEmail = "";
let currentCategories = [];

document.addEventListener("DOMContentLoaded", () => {
    checkAuthState();
    setupCurrentDates();
});

function setupCurrentDates() {
    const today = new Date().toISOString().split('T')[0];
    const txDate = document.getElementById("tx-date");
    if (txDate) txDate.value = today;

    const goalStart = document.getElementById("goal-start-date");
    if (goalStart) goalStart.value = today;

    const goalTarget = document.getElementById("goal-target-date");
    if (goalTarget) {
        const nextYear = new Date();
        nextYear.setFullYear(nextYear.getFullYear() + 1);
        goalTarget.value = nextYear.toISOString().split('T')[0];
    }
}

// -------------------------------------------------------------
// Authentication & Tab Navigation
// -------------------------------------------------------------

function switchAuthTab(tab) {
    const loginForm = document.getElementById("login-form");
    const regForm = document.getElementById("register-form");
    const loginBtn = document.getElementById("tab-login-btn");
    const regBtn = document.getElementById("tab-register-btn");

    if (tab === 'login') {
        loginForm.classList.remove("hidden");
        regForm.classList.add("hidden");
        loginBtn.classList.add("active");
        regBtn.classList.remove("active");
    } else {
        loginForm.classList.add("hidden");
        regForm.classList.remove("hidden");
        loginBtn.classList.remove("active");
        regBtn.classList.add("active");
    }
}

function switchNavTab(tabName) {
    document.querySelectorAll(".nav-btn").forEach(btn => {
        if (btn.dataset.tab === tabName) {
            btn.classList.add("active");
        } else {
            btn.classList.remove("active");
        }
    });

    document.querySelectorAll(".view-section").forEach(sec => {
        sec.classList.add("hidden");
    });

    const targetView = document.getElementById(`view-${tabName}`);
    if (targetView) {
        targetView.classList.remove("hidden");
    }

    if (tabName === 'dashboard') loadDashboard();
    else if (tabName === 'transactions') loadTransactions();
    else if (tabName === 'categories') loadCategories();
    else if (tabName === 'goals') loadGoals();
    else if (tabName === 'reports') generateReport();
}

async function checkAuthState() {
    try {
        const res = await fetch("/api/categories", { credentials: "same-origin" });
        if (res.ok) {
            const storedEmail = localStorage.getItem("user_email") || "Authenticated User";
            showApp(storedEmail);
        } else {
            showAuth();
        }
    } catch (e) {
        showAuth();
    }
}

function showAuth() {
    document.getElementById("auth-container").classList.remove("hidden");
    document.getElementById("app-container").classList.add("hidden");
}

function showApp(email) {
    currentUserEmail = email;
    document.getElementById("user-email-display").innerText = email;
    document.getElementById("auth-container").classList.add("hidden");
    document.getElementById("app-container").classList.remove("hidden");
    loadCategoriesOptions();
    switchNavTab("dashboard");
}

async function handleLogin(e) {
    e.preventDefault();
    const errDiv = document.getElementById("login-error");
    errDiv.classList.add("hidden");

    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;

    try {
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
            credentials: "same-origin"
        });

        const data = await res.json();
        if (res.ok) {
            localStorage.setItem("user_email", username);
            showApp(username);
        } else {
            errDiv.innerText = data.message || "Invalid login credentials";
            errDiv.classList.remove("hidden");
        }
    } catch (err) {
        errDiv.innerText = "Network error during login";
        errDiv.classList.remove("hidden");
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const errDiv = document.getElementById("register-error");
    const succDiv = document.getElementById("register-success");
    errDiv.classList.add("hidden");
    succDiv.classList.add("hidden");

    const fullName = document.getElementById("reg-fullname").value;
    const username = document.getElementById("reg-username").value;
    const phoneNumber = document.getElementById("reg-phone").value;
    const password = document.getElementById("reg-password").value;

    try {
        const res = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ fullName, username, phoneNumber, password }),
            credentials: "same-origin"
        });

        const data = await res.json();
        if (res.status === 201) {
            succDiv.innerText = "Registration successful! You can now log in.";
            succDiv.classList.remove("hidden");
            setTimeout(() => switchAuthTab("login"), 1500);
        } else {
            errDiv.innerText = data.message || "Registration failed";
            errDiv.classList.remove("hidden");
        }
    } catch (err) {
        errDiv.innerText = "Network error during registration";
        errDiv.classList.remove("hidden");
    }
}

async function handleLogout() {
    try {
        await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
    } catch (e) {}
    localStorage.removeItem("user_email");
    showAuth();
}

// -------------------------------------------------------------
// Categories & Dropdowns
// -------------------------------------------------------------

async function loadCategoriesOptions() {
    try {
        const res = await fetch("/api/categories", { credentials: "same-origin" });
        if (res.ok) {
            const data = await res.json();
            currentCategories = data.categories || [];
            populateCategoryDropdowns();
        }
    } catch (e) {}
}

function populateCategoryDropdowns() {
    const filterSelect = document.getElementById("filter-category");
    const txSelect = document.getElementById("tx-category");

    if (filterSelect) {
        filterSelect.innerHTML = '<option value="">All Categories</option>';
        currentCategories.forEach(c => {
            filterSelect.innerHTML += `<option value="${c.name}">${c.name} (${c.type})</option>`;
        });
    }

    if (txSelect) {
        txSelect.innerHTML = '';
        currentCategories.forEach(c => {
            txSelect.innerHTML += `<option value="${c.name}">${c.name} (${c.type})</option>`;
        });
    }
}

// -------------------------------------------------------------
// Dashboard View
// -------------------------------------------------------------

async function loadDashboard() {
    try {
        const res = await fetch("/api/transactions", { credentials: "same-origin" });
        if (res.ok) {
            const data = await res.json();
            const txs = data.transactions || [];

            let income = 0;
            let expenses = 0;

            txs.forEach(t => {
                if (t.type === 'INCOME') income += t.amount;
                else expenses += t.amount;
            });

            document.getElementById("dash-total-income").innerText = formatCurrency(income);
            document.getElementById("dash-total-expenses").innerText = formatCurrency(expenses);
            document.getElementById("dash-net-savings").innerText = formatCurrency(income - expenses);

            // Recent tx table
            const tbody = document.getElementById("dash-recent-tx-body");
            tbody.innerHTML = '';
            if (txs.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No transactions found.</td></tr>';
            } else {
                txs.slice(0, 5).forEach(t => {
                    const isInc = t.type === 'INCOME';
                    tbody.innerHTML += `
                        <tr>
                            <td>${t.date}</td>
                            <td>${t.category}</td>
                            <td>${t.description || '-'}</td>
                            <td class="text-right ${isInc ? 'income-color' : 'expense-color'}">
                                ${isInc ? '+' : '-'}${formatCurrency(t.amount)}
                            </td>
                        </tr>
                    `;
                });
            }
        }

        // Goals summary
        const gRes = await fetch("/api/goals", { credentials: "same-origin" });
        if (gRes.ok) {
            const gData = await gRes.json();
            const goals = gData.goals || [];
            const container = document.getElementById("dash-goals-list");
            container.innerHTML = '';
            if (goals.length === 0) {
                container.innerHTML = '<p class="empty-state text-muted">No active savings goals.</p>';
            } else {
                goals.slice(0, 3).forEach(g => {
                    const pct = Math.min(Math.max(g.progressPercentage, 0), 100).toFixed(1);
                    container.innerHTML += `
                        <div style="margin-bottom: 0.75rem;">
                            <div class="flex-between" style="font-size: 0.875rem;">
                                <strong>${g.goalName}</strong>
                                <span>${formatCurrency(g.currentProgress)} / ${formatCurrency(g.targetAmount)} (${pct}%)</span>
                            </div>
                            <div class="progress-bar-container">
                                <div class="progress-bar-fill" style="width: ${pct}%;"></div>
                            </div>
                        </div>
                    `;
                });
            }
        }
    } catch (e) {}
}

// -------------------------------------------------------------
// Transactions View & CRUD
// -------------------------------------------------------------

async function loadTransactions() {
    const startDate = document.getElementById("filter-start-date").value;
    const endDate = document.getElementById("filter-end-date").value;
    const category = document.getElementById("filter-category").value;
    const type = document.getElementById("filter-type").value;

    let url = "/api/transactions?";
    const params = new URLSearchParams();
    if (startDate) params.append("startDate", startDate);
    if (endDate) params.append("endDate", endDate);
    if (category) params.append("category", category);
    if (type) params.append("type", type);
    url += params.toString();

    try {
        const res = await fetch(url, { credentials: "same-origin" });
        if (res.ok) {
            const data = await res.json();
            renderTransactionsTable(data.transactions || []);
        }
    } catch (e) {}
}

function renderTransactionsTable(txs) {
    const tbody = document.getElementById("transactions-table-body");
    tbody.innerHTML = '';
    if (txs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No transactions found.</td></tr>';
        return;
    }

    txs.forEach(t => {
        const isInc = t.type === 'INCOME';
        const badgeClass = isInc ? 'badge-income' : 'badge-expense';
        const txJson = JSON.stringify(t).replace(/"/g, '&quot;');
        tbody.innerHTML += `
            <tr>
                <td>${t.date}</td>
                <td><strong>${t.category}</strong></td>
                <td><span class="badge ${badgeClass}">${t.type}</span></td>
                <td>${t.description || '-'}</td>
                <td class="text-right ${isInc ? 'income-color' : 'expense-color'}">
                    ${isInc ? '+' : '-'}${formatCurrency(t.amount)}
                </td>
                <td class="text-center">
                    <button class="btn btn-outline btn-sm" onclick="editTransaction('${t.id}', '${t.amount}', '${t.date}', '${t.category}', '${escapeJs(t.description || '')}')">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteTransaction(${t.id})">Delete</button>
                </td>
            </tr>
        `;
    });
}

function resetTxFilters() {
    document.getElementById("filter-start-date").value = '';
    document.getElementById("filter-end-date").value = '';
    document.getElementById("filter-category").value = '';
    document.getElementById("filter-type").value = '';
    loadTransactions();
}

function openTransactionModal(editMode = false) {
    document.getElementById("tx-modal-title").innerText = editMode ? "Edit Transaction" : "Add Transaction";
    document.getElementById("tx-date-hint").classList.toggle("hidden", !editMode);
    document.getElementById("tx-date").readOnly = editMode;
    document.getElementById("tx-modal").classList.remove("hidden");
    loadCategoriesOptions();
}

function closeTransactionModal() {
    document.getElementById("tx-modal").classList.add("hidden");
    document.getElementById("tx-form").reset();
    document.getElementById("tx-id").value = '';
    setupCurrentDates();
}

function editTransaction(id, amount, date, category, description) {
    document.getElementById("tx-id").value = id;
    document.getElementById("tx-amount").value = amount;
    document.getElementById("tx-date").value = date;
    document.getElementById("tx-category").value = category;
    document.getElementById("tx-description").value = description;
    openTransactionModal(true);
}

async function handleSaveTransaction(e) {
    e.preventDefault();
    const id = document.getElementById("tx-id").value;
    const amount = parseFloat(document.getElementById("tx-amount").value);
    const date = document.getElementById("tx-date").value;
    const category = document.getElementById("tx-category").value;
    const description = document.getElementById("tx-description").value;

    const payload = { amount, category, description };
    if (!id) payload.date = date;

    const method = id ? "PUT" : "POST";
    const url = id ? `/api/transactions/${id}` : "/api/transactions";

    try {
        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
            credentials: "same-origin"
        });

        if (res.ok) {
            closeTransactionModal();
            loadTransactions();
        } else {
            const err = await res.json();
            alert(err.message || "Failed to save transaction");
        }
    } catch (e) {
        alert("Error saving transaction");
    }
}

async function deleteTransaction(id) {
    if (!confirm("Are you sure you want to delete this transaction?")) return;
    try {
        const res = await fetch(`/api/transactions/${id}`, {
            method: "DELETE",
            credentials: "same-origin"
        });
        if (res.ok) loadTransactions();
        else {
            const err = await res.json();
            alert(err.message || "Failed to delete transaction");
        }
    } catch (e) {
        alert("Error deleting transaction");
    }
}

// -------------------------------------------------------------
// Categories View & Management
// -------------------------------------------------------------

async function loadCategories() {
    try {
        const res = await fetch("/api/categories", { credentials: "same-origin" });
        if (res.ok) {
            const data = await res.json();
            currentCategories = data.categories || [];
            renderCategoriesTable(currentCategories);
        }
    } catch (e) {}
}

function renderCategoriesTable(categories) {
    const tbody = document.getElementById("categories-table-body");
    tbody.innerHTML = '';
    categories.forEach(c => {
        const badgeClass = c.isCustom ? 'badge-custom' : 'badge-default';
        const typeClass = c.type === 'INCOME' ? 'badge-income' : 'badge-expense';

        tbody.innerHTML += `
            <tr>
                <td><strong>${c.name}</strong></td>
                <td><span class="badge ${typeClass}">${c.type}</span></td>
                <td><span class="badge ${badgeClass}">${c.isCustom ? 'Custom' : 'System Default'}</span></td>
                <td class="text-center">
                    ${c.isCustom ? `<button class="btn btn-danger btn-sm" onclick="deleteCategory('${c.name}')">Delete</button>` : '<span class="text-muted" style="font-size:0.75rem;">Protected</span>'}
                </td>
            </tr>
        `;
    });
}

function openCategoryModal() {
    document.getElementById("category-modal").classList.remove("hidden");
}

function closeCategoryModal() {
    document.getElementById("category-modal").classList.add("hidden");
    document.getElementById("category-form").reset();
}

async function handleSaveCategory(e) {
    e.preventDefault();
    const name = document.getElementById("cat-name").value;
    const type = document.getElementById("cat-type").value;

    try {
        const res = await fetch("/api/categories", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, type }),
            credentials: "same-origin"
        });

        if (res.status === 201) {
            closeCategoryModal();
            loadCategories();
            loadCategoriesOptions();
        } else {
            const err = await res.json();
            alert(err.message || "Failed to create category");
        }
    } catch (e) {
        alert("Error creating category");
    }
}

async function deleteCategory(name) {
    if (!confirm(`Are you sure you want to delete custom category "${name}"?`)) return;
    try {
        const res = await fetch(`/api/categories/${encodeURIComponent(name)}`, {
            method: "DELETE",
            credentials: "same-origin"
        });
        if (res.ok) {
            loadCategories();
            loadCategoriesOptions();
        } else {
            const err = await res.json();
            alert(err.message || "Cannot delete category");
        }
    } catch (e) {
        alert("Error deleting category");
    }
}

// -------------------------------------------------------------
// Savings Goals View & Management
// -------------------------------------------------------------

async function loadGoals() {
    try {
        const res = await fetch("/api/goals", { credentials: "same-origin" });
        if (res.ok) {
            const data = await res.json();
            renderGoalsGrid(data.goals || []);
        }
    } catch (e) {}
}

function renderGoalsGrid(goals) {
    const container = document.getElementById("goals-grid");
    container.innerHTML = '';
    if (goals.length === 0) {
        container.innerHTML = '<p class="empty-state text-muted">No savings goals created yet. Click "+ Create Savings Goal" to begin.</p>';
        return;
    }

    goals.forEach(g => {
        const pct = Math.min(Math.max(g.progressPercentage, 0), 100).toFixed(1);
        container.innerHTML += `
            <div class="goal-card">
                <div class="goal-title-row">
                    <div>
                        <h3>${g.goalName}</h3>
                        <div class="goal-dates">Start: ${g.startDate} | Target: ${g.targetDate}</div>
                    </div>
                    <div>
                        <button class="btn btn-outline btn-sm" onclick="editGoal(${g.id}, '${g.goalName}', ${g.targetAmount}, '${g.targetDate}', '${g.startDate}')">Edit</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteGoal(${g.id})">Delete</button>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar-fill" style="width: ${pct}%;"></div>
                </div>
                <div class="goal-stats-row">
                    <span>Progress: <strong>${formatCurrency(g.currentProgress)}</strong> (${pct}%)</span>
                    <span>Target: <strong>${formatCurrency(g.targetAmount)}</strong></span>
                </div>
                <div class="goal-stats-row" style="margin-top: 0.25rem;">
                    <span class="text-muted">Remaining: ${formatCurrency(g.remainingAmount)}</span>
                </div>
            </div>
        `;
    });
}

function openGoalModal() {
    document.getElementById("goal-modal-title").innerText = "Create Savings Goal";
    document.getElementById("goal-start-date").readOnly = false;
    document.getElementById("goal-modal").classList.remove("hidden");
}

function closeGoalModal() {
    document.getElementById("goal-modal").classList.add("hidden");
    document.getElementById("goal-form").reset();
    document.getElementById("goal-id").value = '';
    setupCurrentDates();
}

function editGoal(id, name, amount, targetDate, startDate) {
    document.getElementById("goal-modal-title").innerText = "Edit Savings Goal";
    document.getElementById("goal-id").value = id;
    document.getElementById("goal-name").value = name;
    document.getElementById("goal-target-amount").value = amount;
    document.getElementById("goal-target-date").value = targetDate;
    document.getElementById("goal-start-date").value = startDate;
    document.getElementById("goal-start-date").readOnly = true;
    document.getElementById("goal-modal").classList.remove("hidden");
}

async function handleSaveGoal(e) {
    e.preventDefault();
    const id = document.getElementById("goal-id").value;
    const goalName = document.getElementById("goal-name").value;
    const targetAmount = parseFloat(document.getElementById("goal-target-amount").value);
    const targetDate = document.getElementById("goal-target-date").value;
    const startDate = document.getElementById("goal-start-date").value;

    const payload = { goalName, targetAmount, targetDate };
    if (!id) payload.startDate = startDate;

    const method = id ? "PUT" : "POST";
    const url = id ? `/api/goals/${id}` : "/api/goals";

    try {
        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
            credentials: "same-origin"
        });

        if (res.ok) {
            closeGoalModal();
            loadGoals();
        } else {
            const err = await res.json();
            alert(err.message || "Failed to save goal");
        }
    } catch (e) {
        alert("Error saving goal");
    }
}

async function deleteGoal(id) {
    if (!confirm("Are you sure you want to delete this savings goal?")) return;
    try {
        const res = await fetch(`/api/goals/${id}`, {
            method: "DELETE",
            credentials: "same-origin"
        });
        if (res.ok) loadGoals();
        else {
            const err = await res.json();
            alert(err.message || "Failed to delete goal");
        }
    } catch (e) {
        alert("Error deleting goal");
    }
}

// -------------------------------------------------------------
// Financial Reports View
// -------------------------------------------------------------

function toggleReportPeriodInputs() {
    const type = document.getElementById("report-type-select").value;
    document.getElementById("report-month-group").style.display = type === 'monthly' ? 'block' : 'none';
}

async function generateReport() {
    const type = document.getElementById("report-type-select").value;
    const year = document.getElementById("report-year").value;
    const month = document.getElementById("report-month").value;

    const url = type === 'monthly'
        ? `/api/reports/monthly/${year}/${month}`
        : `/api/reports/yearly/${year}`;

    try {
        const res = await fetch(url, { credentials: "same-origin" });
        if (res.ok) {
            const data = await res.json();
            renderReportResults(data);
        } else {
            const err = await res.json();
            alert(err.message || "Failed to generate report");
        }
    } catch (e) {
        alert("Error generating report");
    }
}

function renderReportResults(data) {
    let sumIncome = 0;
    let sumExpense = 0;

    const incBody = document.getElementById("report-income-body");
    incBody.innerHTML = '';
    if (data.totalIncome && Object.keys(data.totalIncome).length > 0) {
        Object.entries(data.totalIncome).forEach(([cat, val]) => {
            sumIncome += val;
            incBody.innerHTML += `<tr><td>${cat}</td><td class="text-right income-color">+${formatCurrency(val)}</td></tr>`;
        });
    } else {
        incBody.innerHTML = '<tr><td colspan="2" class="text-muted text-center">No income records found</td></tr>';
    }

    const expBody = document.getElementById("report-expense-body");
    expBody.innerHTML = '';
    if (data.totalExpenses && Object.keys(data.totalExpenses).length > 0) {
        Object.entries(data.totalExpenses).forEach(([cat, val]) => {
            sumExpense += val;
            expBody.innerHTML += `<tr><td>${cat}</td><td class="text-right expense-color">-${formatCurrency(val)}</td></tr>`;
        });
    } else {
        expBody.innerHTML = '<tr><td colspan="2" class="text-muted text-center">No expense records found</td></tr>';
    }

    document.getElementById("report-total-income").innerText = formatCurrency(sumIncome);
    document.getElementById("report-total-expenses").innerText = formatCurrency(sumExpense);
    document.getElementById("report-net-savings").innerText = formatCurrency(data.netSavings);
}

// -------------------------------------------------------------
// Utilities
// -------------------------------------------------------------

function formatCurrency(val) {
    return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(val || 0);
}

function escapeJs(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, '\\"');
}
