let catalog = {
    products: [],
    tables: []
};

let waiters = [];
let ingredients = [];
let cart = [];
let tableId = null;
let payOrder = null;
let owner = null;

const $ = selector => document.querySelector(selector);

const money = amount =>
    Number(amount || 0).toLocaleString() + ' Ks';

const today = () =>
    new Date().toISOString().slice(0, 10);

const toast = message => {
    const element = $('#toast');

    element.textContent = message;
    element.style.display = 'block';

    setTimeout(() => {
        element.style.display = 'none';
    }, 2200);
};


/* =========================================================
   API
   ========================================================= */

async function api(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json'
        },
        ...options
    });

    if (!response.ok) {
        let message = 'Request failed';

        try {
            const body = await response.json();
            message = body.message || body.error || message;
        } catch (error) {
            // Response body is not JSON.
        }

        if (response.status === 401) {
            message = 'Owner login required';
        }

        if (response.status === 403) {
            message = 'Owner access required';
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const contentType =
        response.headers.get('content-type') || '';

    return contentType.includes('application/json')
        ? response.json()
        : response.text();
}


/* =========================================================
   INITIALIZATION
   ========================================================= */

async function init() {
    await checkOwner();

    catalog = await api('/api/catalog');

    await loadWaiters();

    setDates();
    renderCatalog();

    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js');
    }

    syncQueue();
}

function setDates() {
    const dateInputs = [
        'moveFrom',
        'moveTo',
        'reportFrom',
        'reportTo'
    ];

    dateInputs.forEach(id => {
        const input = $('#' + id);

        if (input) {
            input.value = today();
        }
    });
}


/* =========================================================
   OWNER LOGIN
   ========================================================= */

async function checkOwner() {
    try {
        const state = await api('/api/auth/me');
        owner = state.authenticated ? state : null;
    } catch (error) {
        owner = null;
    }

    document.body.classList.toggle(
        'owner-authenticated',
        Boolean(owner)
    );

    const loginButton = $('#ownerLoginButton');

    if (loginButton) {
        loginButton.style.display = owner ? 'none' : '';

        if (owner) {
            loginButton.title =
                'Logged in as ' + owner.username;
        }
    }
}

$('#ownerLoginButton').onclick = () => {
    $('#ownerLoginError').hidden = true;
    $('#ownerUsername').value = 'owner';
    $('#ownerPassword').value = '';

    $('#ownerLoginDialog').showModal();

    setTimeout(() => {
        $('#ownerPassword').focus();
    }, 50);
};

$('#ownerLoginForm').onsubmit = async event => {
    event.preventDefault();

    try {
        owner = await api('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                username: $('#ownerUsername').value,
                password: $('#ownerPassword').value
            })
        });

        $('#ownerLoginDialog').close();

        await checkOwner();
        await loadWaiters();

        toast('Owner login successful');
    } catch (error) {
        $('#ownerLoginError').textContent =
            error.message === 'Request failed'
                ? 'Username or password is incorrect'
                : error.message;

        $('#ownerLoginError').hidden = false;
    }
};

$('#ownerLogoutButton').onclick = async () => {
    await api('/api/auth/logout', {
        method: 'POST'
    });

    owner = null;

    await checkOwner();

    document
        .querySelectorAll('.tab, nav button[data-tab]')
        .forEach(element => {
            element.classList.remove('active');
        });

    $('[data-tab="pos"]').classList.add('active');
    $('#pos').classList.add('active');

    await loadWaiters();

    toast('Logged out');
};


/* =========================================================
   CATALOG AND CART
   ========================================================= */

function renderCatalog() {
    $('#tables').innerHTML = catalog.tables
        .map(table => `
            <button
                onclick="pickTable(${table.id})"
                class="${tableId === table.id ? 'selected' : ''}"
            >
                ${table.name}
                <small>${table.area}</small>
            </button>
        `)
        .join('');

    $('#products').innerHTML = catalog.products
        .filter(product => product.active)
        .map(product => `
            <button
                class="product"
                onclick="add(${product.id})"
            >
                <strong>
                    ${product.nameMm || product.name}
                </strong>

                <small>${product.station}</small>

                <span>${money(product.price)}</span>
            </button>
        `)
        .join('');

    renderCart();
}

function pickTable(id) {
    tableId = id;
    renderCatalog();
}

function add(id) {
    const product = catalog.products.find(
        item => item.id === id
    );

    if (!product) {
        return toast('ပစ္စည်းကို ရှာမတွေ့ပါ');
    }

    const existingItem = cart.find(
        item => item.product.id === id
    );

    if (existingItem) {
        existingItem.quantity++;
    } else {
        cart.push({
            product,
            quantity: 1
        });
    }

    renderCart();
}

function change(id, difference) {
    const item = cart.find(
        cartItem => cartItem.product.id === id
    );

    if (!item) {
        return;
    }

    item.quantity += difference;

    if (item.quantity < 1) {
        cart = cart.filter(
            cartItem => cartItem !== item
        );
    }

    renderCart();
}

function renderCart() {
    $('#cart').innerHTML =
        cart
            .map(item => `
                <div class="line">
                    <div>
                        ${item.product.nameMm || item.product.name}
                        <small>× ${item.quantity}</small>
                    </div>

                    <button
                        type="button"
                        onclick="change(${item.product.id}, -1)"
                    >
                        −
                    </button>

                    <button
                        type="button"
                        onclick="change(${item.product.id}, 1)"
                    >
                        +
                    </button>
                </div>
            `)
            .join('') || '<p>ပစ္စည်းရွေးပါ</p>';

    const total = cart.reduce(
        (sum, item) =>
            sum +
            Number(item.product.price) *
            item.quantity,
        0
    );

    $('#total').textContent = money(total);
}


/* =========================================================
   CREATE ORDER
   ========================================================= */

$('#send').onclick = async () => {
    if (!tableId || !cart.length) {
        return toast('ဝိုင်းနှင့် ပစ္စည်းရွေးပါ');
    }

    const note = $('#note').value.trim();

    const body = {
        clientRef: crypto.randomUUID(),
        tableId,
        waiterId: $('#orderWaiter').value || null,

        items: cart.map(item => ({
            productId: item.product.id,
            quantity: item.quantity,
            modifier: note,
            note
        }))
    };

    try {
        await api('/api/orders', {
            method: 'POST',
            body: JSON.stringify(body)
        });

        toast('အော်ဒါပို့ပြီးပါပြီ');
    } catch (error) {
        if (navigator.onLine) {
            return toast(error.message);
        }

        const queue = JSON.parse(
            localStorage.offlineOrders || '[]'
        );

        queue.push(body);

        localStorage.offlineOrders =
            JSON.stringify(queue);

        toast('Offline သိမ်းထားပြီးပါပြီ');
    }

    cart = [];
    $('#note').value = '';

    renderCart();
}


/* =========================================================
   OFFLINE ORDER SYNCHRONIZATION
   ========================================================= */

async function syncQueue() {
    if (!navigator.onLine) {
        return;
    }

    const queue = JSON.parse(
        localStorage.offlineOrders || '[]'
    );

    const remainingOrders = [];

    for (const order of queue) {
        try {
            await api('/api/orders', {
                method: 'POST',
                body: JSON.stringify(order)
            });
        } catch (error) {
            remainingOrders.push(order);
        }
    }

    localStorage.offlineOrders =
        JSON.stringify(remainingOrders);

    if (queue.length && !remainingOrders.length) {
        toast('Offline orders synced');
    }
}


/* =========================================================
   ORDER MANAGEMENT
   ========================================================= */

async function loadOrders() {
    try {
        const orders = await api('/api/orders');

        $('#order-list').innerHTML =
            orders
                .map(order => {
                    const itemList = (order.items || [])
                        .map(item => {
                            const productName =
                                item.product.nameMm ||
                                item.product.name;

                            const modifier = item.modifier
                                ? ` (${item.modifier})`
                                : '';

                            return `${productName} × ${item.quantity}${modifier}`;
                        })
                        .join(' · ');

                    const waiterName = order.waiter
                        ? 'စားပွဲထိုး: ' + order.waiter.name
                        : 'စားပွဲထိုး မသတ်မှတ်ရသေးပါ';

                    const actions =
                        order.status !== 'PAID'
                            ? `
                                <div class="actions">
                                    <button
                                        type="button"
                                        onclick="status(${order.id}, 'READY')"
                                    >
                                        အသင့်ဖြစ်ပြီ
                                    </button>

                                    <button
                                        type="button"
                                        onclick="openPay(${order.id}, ${order.total})"
                                    >
                                        ငွေရှင်း
                                    </button>
                                </div>
                            `
                            : '';

                    return `
                        <div class="order">
                            <div class="order-head">
                                <b>
                                    ${order.shopTable.name}
                                    · #${order.id}
                                </b>

                                <span class="badge">
                                    ${order.status}
                                </span>
                            </div>

                            <small>
                                ${waiterName}
                                ·
                                ${new Date(order.createdAt)
                                    .toLocaleString()}
                            </small>

                            <p>${itemList}</p>

                            <strong>
                                ${money(order.total)}
                            </strong>

                            ${actions}
                        </div>
                    `;
                })
                .join('') ||
            '<p>အော်ဒါမရှိသေးပါ</p>';
    } catch (error) {
        toast(error.message);
    }
}

async function status(id, newStatus) {
    try {
        await api(
            `/api/orders/${id}/status?value=${newStatus}`,
            {
                method: 'PATCH'
            }
        );

        await loadOrders();
    } catch (error) {
        toast(error.message);
    }
}


/* =========================================================
   PAYMENT
   ========================================================= */

function openPay(id, total) {
    payOrder = id;

    $('#payTotal').textContent =
        'ကျသင့်ငွေ ' + money(total);

    $('#amount').value = total;
    $('#reference').value = '';

    $('#payDialog').showModal();
}

$('#confirmPay').onclick = async event => {
    event.preventDefault();

    try {
        await api(
            `/api/orders/${payOrder}/payments`,
            {
                method: 'POST',
                body: JSON.stringify({
                    payments: [
                        {
                            method: $('#method').value,
                            amount: $('#amount').value,
                            reference: $('#reference').value
                        }
                    ]
                })
            }
        );

        $('#payDialog').close();

        toast('ငွေရှင်းပြီးပါပြီ');

        await loadOrders();
    } catch (error) {
        toast(error.message);
    }
};


/* =========================================================
   WAITER MANAGEMENT
   ========================================================= */

async function loadWaiters() {
    waiters = await api('/api/waiters');

    const options = waiters
        .map(waiter => `
            <option value="${waiter.id}">
                ${waiter.name} (${waiter.code})
            </option>
        `)
        .join('');

    $('#orderWaiter').innerHTML =
        '<option value="">တာဝန်မပေးရသေးပါ</option>' +
        options;

    /*
     * W001 - မျိုးမြင့်ကို Order အတွက်
     * default waiter အဖြစ်ရွေးပါမည်။
     */
    const defaultWaiter = waiters.find(
        waiter => waiter.code === 'W001'
    );

    if (defaultWaiter) {
        $('#orderWaiter').value =
            String(defaultWaiter.id);
    }

    $('#reportWaiter').innerHTML =
        '<option value="">စားပွဲထိုးအားလုံး</option>' +
        options;

    if (owner) {
        await loadOwnerWaiters();
    } else {
        $('#waiter-list').innerHTML = '';
    }
}

async function loadOwnerWaiters() {
    if (!owner) {
        return;
    }

    try {
        waiters = await api('/api/manage/waiters');

        $('#waiter-list').innerHTML =
            waiters
                .map(waiter => {
                    const tableNames =
                        (waiter.assignedTables || [])
                            .map(table => table.name)
                            .join(', ') || 'All';

                    return `
                        <div class="card">
                            <div class="order-head">
                                <b>${waiter.name}</b>

                                <span class="badge">
                                    ${waiter.active
                                        ? 'Active'
                                        : 'Inactive'}
                                </span>
                            </div>

                            <p>
                                Code: ${waiter.code}
                                <br>

                                Phone: ${waiter.phone || '-'}
                                <br>

                                Tables: ${tableNames}
                            </p>

                            <button
                                type="button"
                                onclick="openWaiter(${waiter.id})"
                            >
                                Edit
                            </button>
                        </div>
                    `;
                })
                .join('') ||
            '<p>စားပွဲထိုးမရှိသေးပါ</p>';
    } catch (error) {
        toast(error.message);
    }
}

function openWaiter(id) {
    if (!owner) {
        return $('#ownerLoginButton').click();
    }

    const waiter = waiters.find(
        item => item.id === id
    );

    $('#waiterId').value = waiter?.id || '';
    $('#waiterName').value = waiter?.name || '';
    $('#waiterCode').value = waiter?.code || '';
    $('#waiterPin').value = '';
    $('#waiterPhone').value = waiter?.phone || '';

    $('#waiterActive').value =
        String(waiter?.active ?? true);

    const assignedTableIds = new Set(
        (waiter?.assignedTables || [])
            .map(table => table.id)
    );

    $('#waiterTables').innerHTML =
        catalog.tables
            .map(table => `
                <label>
                    <input
                        type="checkbox"
                        value="${table.id}"
                        ${assignedTableIds.has(table.id)
                            ? 'checked'
                            : ''}
                    >

                    ${table.name}
                </label>
            `)
            .join('');

    $('#waiterDialog').showModal();
}

async function saveWaiter(event) {
    event.preventDefault();

    if (!owner) {
        return toast('Owner access required');
    }

    const id = $('#waiterId').value;

    const body = {
        name: $('#waiterName').value.trim(),
        code: $('#waiterCode').value.trim(),
        pin: $('#waiterPin').value,
        phone: $('#waiterPhone').value.trim(),

        active:
            $('#waiterActive').value === 'true',

        tableIds: [
            ...document.querySelectorAll(
                '#waiterTables input:checked'
            )
        ].map(input => Number(input.value))
    };

    try {
        await api(
            '/api/manage/waiters' +
            (id ? '/' + id : ''),
            {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(body)
            }
        );

        $('#waiterDialog').close();

        await loadWaiters();

        toast('Waiter saved');
    } catch (error) {
        toast(error.message);
    }
}


/* =========================================================
   INVENTORY MANAGEMENT
   ========================================================= */

async function loadInventory() {
    try {
        /*
         * Ingredient list ကို Guest နှင့် Owner
         * နှစ်ဦးလုံး ကြည့်နိုင်ရပါမည်။
         *
         * Backend မှာ public endpoint သီးခြားရှိပါက:
         * /api/inventory/ingredients ကို အသုံးပြုပါ။
         *
         * လက်ရှိ project endpoint အတိုင်း:
         * /api/manage/ingredients ကိုထားရှိထားပါသည်။
         */
        ingredients = await api('/api/manage/ingredients');

        const movements = await api(
            `/api/manage/movements` +
            `?from=${$('#moveFrom').value}` +
            `&to=${$('#moveTo').value}`
        );

        $('#ingredient-list').innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Stock</th>
                        <th>Safety</th>
                        <th>Purchase</th>
                        <th>Expiry</th>
                        <th>Supplier</th>
                        <th class="owner-only">
                            Action
                        </th>
                    </tr>
                </thead>

                <tbody>
                    ${ingredients
                        .map(ingredient => `
                            <tr>
                                <td>
                                    ${ingredient.name}
                                </td>

                                <td class="${
                                    Number(ingredient.stockQty) <=
                                    Number(ingredient.safetyQty)
                                        ? 'danger'
                                        : ''
                                }">
                                    ${ingredient.stockQty}
                                    ${ingredient.unit}
                                </td>

                                <td>
                                    ${ingredient.safetyQty}
                                </td>

                                <td>
                                    ${ingredient.lastPurchaseDate || '-'}
                                </td>

                                <td class="${
                                    ingredient.expiryDate &&
                                    ingredient.expiryDate <= today()
                                        ? 'danger'
                                        : ''
                                }">
                                    ${ingredient.expiryDate || '-'}
                                </td>

                                <td>
                                    ${ingredient.supplier || '-'}
                                </td>

                                <td class="owner-only">
                                    <button
                                        type="button"
                                        onclick="openStock(${ingredient.id})"
                                    >
                                        Stock
                                    </button>

                                    <button
                                        type="button"
                                        onclick="openIngredient(${ingredient.id})"
                                    >
                                        Edit
                                    </button>
                                </td>
                            </tr>
                        `)
                        .join('')}
                </tbody>
            </table>
        `;

        $('#movement-list').innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Ingredient</th>
                        <th>Type</th>
                        <th>Qty</th>
                        <th>Balance</th>
                        <th>Reference</th>
                    </tr>
                </thead>

                <tbody>
                    ${movements
                        .map(movement => `
                            <tr>
                                <td>
                                    ${new Date(
                                        movement.createdAt
                                    ).toLocaleString()}
                                </td>

                                <td>
                                    ${movement.ingredient.name}
                                </td>

                                <td>
                                    ${movement.type}
                                </td>

                                <td>
                                    ${movement.quantity}
                                </td>

                                <td>
                                    ${movement.balanceAfter}
                                </td>

                                <td>
                                    ${movement.reference || '-'}
                                </td>
                            </tr>
                        `)
                        .join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        toast(error.message);
    }
}

function openIngredient(id) {
    if (!owner) {
        toast('ကုန်ကြမ်းထည့်ရန် Owner login ဝင်ရပါမည်');
        return $('#ownerLoginButton').click();
    }

    const ingredient = ingredients.find(
        item => item.id === id
    );

    $('#ingredientId').value =
        ingredient?.id || '';

    $('#ingredientName').value =
        ingredient?.name || '';

    $('#ingredientUnit').value =
        ingredient?.unit || '';

    $('#ingredientStock').value =
        ingredient?.stockQty ?? 0;

    $('#ingredientSafety').value =
        ingredient?.safetyQty ?? 0;

    $('#ingredientCost').value =
        ingredient?.costPerUnit || '';

    $('#ingredientPurchase').value =
        ingredient?.lastPurchaseDate || '';

    $('#ingredientExpiry').value =
        ingredient?.expiryDate || '';

    $('#ingredientSupplier').value =
        ingredient?.supplier || '';

    $('#ingredientDialog').showModal();
}

async function saveIngredient(event) {
    event.preventDefault();

    if (!owner) {
        return toast('Owner access required');
    }

    const id = $('#ingredientId').value;

    const body = {
        name: $('#ingredientName').value.trim(),
        unit: $('#ingredientUnit').value.trim(),
        stockQty: $('#ingredientStock').value,
        safetyQty: $('#ingredientSafety').value,

        costPerUnit:
            $('#ingredientCost').value || null,

        lastPurchaseDate:
            $('#ingredientPurchase').value || null,

        expiryDate:
            $('#ingredientExpiry').value || null,

        supplier:
            $('#ingredientSupplier').value.trim()
    };

    try {
        await api(
            '/api/manage/ingredients' +
            (id ? '/' + id : ''),
            {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(body)
            }
        );

        $('#ingredientDialog').close();

        await loadInventory();

        toast('Ingredient saved');
    } catch (error) {
        toast(error.message);
    }
}

function openStock(id) {
    if (!owner) {
        toast('Stock ပြင်ရန် Owner login ဝင်ရပါမည်');
        return $('#ownerLoginButton').click();
    }

    $('#stockIngredientId').value = id;
    $('#stockQty').value = '';
    $('#stockPurchase').value = today();
    $('#stockExpiry').value = '';
    $('#stockRef').value = '';
    $('#stockNote').value = '';

    $('#stockDialog').showModal();
}

async function saveStock(event) {
    event.preventDefault();

    if (!owner) {
        return toast('Owner access required');
    }

    const body = {
        type: $('#stockType').value,
        quantity: $('#stockQty').value,

        purchaseDate:
            $('#stockPurchase').value || null,

        expiryDate:
            $('#stockExpiry').value || null,

        reference:
            $('#stockRef').value.trim(),

        note:
            $('#stockNote').value.trim()
    };

    try {
        const ingredientId =
            $('#stockIngredientId').value;

        await api(
            `/api/manage/ingredients/${ingredientId}/stock`,
            {
                method: 'POST',
                body: JSON.stringify(body)
            }
        );

        $('#stockDialog').close();

        await loadInventory();

        toast('Stock updated');
    } catch (error) {
        toast(error.message);
    }
}


/* =========================================================
   REPORTS
   ========================================================= */

async function report() {
    if (!owner) {
        return $('#ownerLoginButton').click();
    }

    try {
        const waiterId = $('#reportWaiter').value;

        let reportUrl =
            `/api/manage/reports` +
            `?from=${$('#reportFrom').value}` +
            `&to=${$('#reportTo').value}`;

        if (waiterId) {
            reportUrl += '&waiterId=' + waiterId;
        }

        const reportData = await api(reportUrl);

        const lowStock = await api(
            '/api/inventory/low-stock'
        );

        const expiringIngredients = await api(
            '/api/manage/expiry?days=30'
        );

        $('#stats').innerHTML = `
            <div class="stat">
                Orders
                <b>${reportData.orders}</b>
            </div>

            <div class="stat">
                Paid orders
                <b>${reportData.paidOrders}</b>
            </div>

            <div class="stat">
                Sales
                <b>${money(reportData.sales)}</b>
            </div>
        `;

        $('#waiter-sales').innerHTML =
            Object.entries(
                reportData.salesByWaiter || {}
            )
                .map(([waiterName, sales]) => `
                    <p>
                        <b>${waiterName}</b>:
                        ${money(sales)}
                    </p>
                `)
                .join('') || '-';

        $('#low').innerHTML =
            lowStock
                .map(ingredient => `
                    <p class="danger">
                        ⚠ ${ingredient.name}:
                        ${ingredient.stockQty}
                        ${ingredient.unit}
                    </p>
                `)
                .join('') ||
            'Stock level အဆင်ပြေပါသည်';

        $('#expiry').innerHTML =
            expiringIngredients
                .map(ingredient => `
                    <p class="danger">
                        ⚠ ${ingredient.name}:
                        ${ingredient.expiryDate}
                    </p>
                `)
                .join('') ||
            'No upcoming expiry';
    } catch (error) {
        toast(error.message);
    }
}


/* =========================================================
   TAB NAVIGATION
   ========================================================= */

document
    .querySelectorAll('nav button[data-tab]')
    .forEach(button => {
        button.onclick = async () => {
            const tabName = button.dataset.tab;

            const ownerOnlyTab =
                tabName === 'waiters' ||
                tabName === 'report';

            if (ownerOnlyTab && !owner) {
                return $('#ownerLoginButton').click();
            }

            document
                .querySelectorAll(
                    '.tab, nav button[data-tab]'
                )
                .forEach(element => {
                    element.classList.remove('active');
                });

            button.classList.add('active');

            $('#' + tabName).classList.add('active');

            if (tabName === 'orders') {
                await loadOrders();
            }

            if (tabName === 'inventory') {
                await loadInventory();
            }

            if (tabName === 'waiters') {
                await loadOwnerWaiters();
            }

            if (tabName === 'report') {
                await report();
            }
        };
    });


/* =========================================================
   NETWORK STATUS
   ========================================================= */

function updateNetworkStatus() {
    $('#net').textContent =
        navigator.onLine
            ? '● Online'
            : '● Offline';

    if (navigator.onLine) {
        syncQueue();
    }
}

addEventListener('online', updateNetworkStatus);
addEventListener('offline', updateNetworkStatus);

updateNetworkStatus();

init().catch(error => {
    toast(error.message);
});