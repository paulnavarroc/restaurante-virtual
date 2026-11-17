(function () {
    const grid = document.getElementById('product-grid');
    const statusEl = document.getElementById('menu-status');
    const emptyEl = document.getElementById('menu-empty');
    if (!grid) return;

    function isAuthenticated() {
        const el = document.getElementById('menu-page') || document.querySelector('[data-authenticated]');
        return el && el.getAttribute('data-authenticated') === 'true';
    }

    function money(v) {
        const n = Number(v);
        if (Number.isNaN(n)) return '0.00';
        return n.toFixed(2);
    }

    function escapeHtml(s) {
        if (s == null) return '';
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function cardHtml(p) {
        const img = p.imageUrl
            ? `<img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}" class="h-full w-full object-cover">`
            : `<i class="bi bi-egg-fried text-5xl text-slate-300"></i>`;
        const region = p.regionName
            ? `<span class="absolute left-3 top-3 rounded-full bg-white/90 px-2.5 py-0.5 text-[11px] font-semibold text-slate-700 shadow-sm">${escapeHtml(p.regionName)}</span>`
            : '';
        const promo = p.onPromoPrice
            ? `<span class="text-sm text-slate-400 line-through">S/ ${money(p.listPrice)}</span>`
            : '';
        const sold = p.soldOut
            ? `<span class="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-600">Agotado</span>`
            : '';
        const prep = p.prepTimeMinutes != null
            ? `<p class="text-xs text-slate-400"><i class="bi bi-clock"></i> ${p.prepTimeMinutes} min</p>`
            : '';
        const branches = Array.isArray(p.branches) ? p.branches : [];
        const branchLine = branches.length
            ? `<p class="mt-1 text-xs text-slate-500">📍 ${branches.map(b => escapeHtml(b.branchName)).join(' · ')}</p>`
            : '';

        const branchesJson = escapeHtml(JSON.stringify(branches));

        let actions = '';
        if (!isAuthenticated()) {
            actions = `<a href="/login" class="inline-flex items-center gap-1 rounded-xl border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Entrar para pedir</a>`;
        } else if (p.available && !p.soldOut) {
            const dmi = p.dailyMenuItemId != null ? p.dailyMenuItemId : '';
            actions = `
                <div class="flex flex-col items-end gap-1" data-cart-actions data-product-id="${p.id}" data-dmi-id="${dmi}" data-branches='${branchesJson}'>
                    <button type="button" data-action="add" class="inline-flex items-center gap-1 rounded-xl bg-sky-600 px-3 py-2 text-sm font-semibold text-white hover:bg-sky-700">
                        <i class="bi bi-cart-plus"></i> Agregar
                    </button>
                    <div data-in-cart class="items-center gap-1" style="display:none">
                        <button type="button" data-action="minus" class="flex h-8 w-8 items-center justify-center rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50">−</button>
                        <span class="min-w-[4.5rem] text-center text-xs font-semibold text-sky-700">En carrito (<span data-qty>0</span>)</span>
                        <button type="button" data-action="plus" class="flex h-8 w-8 items-center justify-center rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50">+</button>
                    </div>
                </div>`;
        } else {
            actions = `<span class="text-sm text-slate-400">No disponible</span>`;
        }

        return `
            <article class="group flex flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:border-sky-200 hover:shadow-md">
                <div class="relative flex h-44 items-center justify-center bg-slate-100">
                    ${img}
                    ${region}
                </div>
                <div class="flex flex-1 flex-col p-4">
                    <span class="text-xs font-medium uppercase tracking-wide text-sky-600">${escapeHtml(p.categoryName || '')}</span>
                    <h2 class="mt-1 text-lg font-semibold text-slate-900">${escapeHtml(p.name)}</h2>
                    ${branchLine}
                    <p class="mt-1 line-clamp-2 flex-1 text-sm text-slate-500">${escapeHtml(p.description || '')}</p>
                    <div class="mt-4 flex items-center justify-between gap-2">
                        <div>
                            <div class="flex items-baseline gap-2">
                                <span class="text-xl font-bold text-slate-900">S/ ${money(p.salePrice)}</span>
                                ${promo}
                            </div>
                            ${sold}
                            ${prep}
                        </div>
                        ${actions}
                    </div>
                </div>
            </article>`;
    }

    function setStatus(text, show) {
        if (!statusEl) return;
        if (!show) {
            statusEl.classList.add('hidden');
            statusEl.textContent = '';
            return;
        }
        statusEl.textContent = text;
        statusEl.classList.remove('hidden');
    }

    function getParams() {
        const q = document.getElementById('filter-q')?.value?.trim() || '';
        const categoryId = document.getElementById('filter-category')?.value || '';
        const regionId = document.getElementById('filter-region')?.value || '';
        const branchId = document.getElementById('filter-branch')?.value || '';
        const params = new URLSearchParams();
        if (q) params.set('q', q);
        if (categoryId) params.set('categoryId', categoryId);
        if (regionId) params.set('regionId', regionId);
        if (branchId) params.set('branchId', branchId);
        return params;
    }

    async function loadMenu() {
        const params = getParams();
        setStatus('Cargando…', true);
        grid.style.opacity = '0.5';

        try {
            const res = await fetch('/api/menu?' + params.toString(), {
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            if (!res.ok) throw new Error('Error al cargar el menú');
            const data = await res.json();

            const qs = params.toString();
            history.replaceState(null, '', qs ? ('/menu?' + qs) : '/menu');

            if (!data.hasMenu) {
                grid.innerHTML = '';
                if (emptyEl) {
                    emptyEl.textContent = 'Hoy no hay menú publicado. Vuelve más tarde o prueba otra sede.';
                    emptyEl.classList.remove('hidden');
                } else {
                    grid.innerHTML = '<p class="col-span-full text-center text-slate-500 py-12">Hoy no hay menú publicado</p>';
                }
                setStatus('', false);
                return;
            }

            const products = data.products || [];
            if (emptyEl) emptyEl.classList.add('hidden');

            if (products.length === 0) {
                grid.innerHTML = '<p class="col-span-full text-center text-slate-500 py-12">No hay platos con estos filtros</p>';
            } else {
                grid.innerHTML = products.map(cardHtml).join('');
            }

            setStatus('', false);

            // Reaplicar cantidades del carrito en las cards nuevas
            if (window.CartApi && typeof window.CartApi.load === 'function' && isAuthenticated()) {
                window.CartApi.load();
            }
        } catch (e) {
            setStatus(e.message || 'No se pudo cargar el menú', true);
        } finally {
            grid.style.opacity = '1';
        }
    }

    let searchTimer = null;
    document.getElementById('filter-q')?.addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(loadMenu, 350);
    });
    document.getElementById('filter-category')?.addEventListener('change', loadMenu);
    document.getElementById('filter-region')?.addEventListener('change', loadMenu);
    document.getElementById('filter-branch')?.addEventListener('change', loadMenu);
    document.getElementById('filter-apply')?.addEventListener('click', loadMenu);
    document.getElementById('filter-clear')?.addEventListener('click', function () {
        const q = document.getElementById('filter-q');
        const c = document.getElementById('filter-category');
        const r = document.getElementById('filter-region');
        const b = document.getElementById('filter-branch');
        if (q) q.value = '';
        if (c) c.value = '';
        if (r) r.value = '';
        if (b) b.value = '';
        loadMenu();
    });

    loadMenu();
})();

(function branchModal() {
    const modal = document.getElementById('branch-modal');
    const options = document.getElementById('branch-modal-options');
    const cancel = document.getElementById('branch-modal-cancel');
    if (!modal || !options) return;

    function close() {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        options.innerHTML = '';
    }

    cancel?.addEventListener('click', close);
    modal.addEventListener('click', function (e) {
        if (e.target === modal) close();
    });

    document.addEventListener('click', function (e) {
        const add = e.target.closest('[data-action="add"]');
        if (!add) return;

        const wrap = add.closest('[data-cart-actions]');
        if (!wrap) return;

        let branches = [];
        try {
            branches = JSON.parse(wrap.getAttribute('data-branches') || '[]');
        } catch (_) {}

        const available = branches.filter(b => b.available);
        // Una sola sede o ya hay dailyMenuItemId → deja que cart.js maneje
        if (available.length <= 1) return;

        e.preventDefault();
        e.stopPropagation();
        e.stopImmediatePropagation();

        options.innerHTML = available.map(b => `
            <button type="button"
                  class="branch-pick w-full rounded-xl border border-slate-200 px-3 py-2.5 text-left text-sm hover:border-sky-300 hover:bg-sky-50"
                  data-product-id="${wrap.getAttribute('data-product-id')}"
                  data-dmi-id="${b.dailyMenuItemId}">
                <span class="font-medium text-slate-900">${escapeHtml(b.branchName)}</span>
                ${b.remainingQuota != null ? `<span class="block text-xs text-slate-500">Cupo: ${b.remainingQuota}</span>` : ''}
            </button>
    `).join('');

        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }, true); // capture: antes que el listener de cart.js

    options.addEventListener('click', function (e) {
        const btn = e.target.closest('.branch-pick');
        if (!btn || !window.CartApi) return;
        const productId = btn.getAttribute('data-product-id');
        const dmiId = btn.getAttribute('data-dmi-id');
        close();
        window.CartApi.add(productId, dmiId, 1);
    });

    function escapeHtml(s) {
        if (s == null) return '';
        return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }
})();