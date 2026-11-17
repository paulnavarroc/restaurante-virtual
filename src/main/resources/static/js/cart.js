(function () {
    function csrfHeaders() {
        const h = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        // Preferido con CookieCsrfTokenRepository
        const m = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
        if (m) {
            h['X-XSRF-TOKEN'] = decodeURIComponent(m[1]);
        }

        // Respaldo meta (si existe)
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        if (token && header) {
            h[header] = token;
        }

        return h;
    }


    function updateBadges(count) {
        document.querySelectorAll('[data-cart-count]').forEach(function (el) {
            el.textContent = count;
            if (count > 0) el.classList.remove('hidden');
            else el.classList.add('hidden');
        });
    }

    function lineKey(productId, dmiId) {
        const p = productId == null ? '' : String(productId);
        const d = dmiId == null || dmiId === '' || dmiId === 'null' ? 'null' : String(dmiId);
        return p + ':' + d;
    }

    function qtyFromResponse(data, productId, dmiId) {
        if (!data || !data.lines) return 0;
        const key = lineKey(productId, dmiId);
        const line = data.lines.find(function (l) {
            return lineKey(l.productId, l.dailyMenuItemId) === key;
        });
        return line ? line.quantity : 0;
    }

    function refreshCardButtons(data) {
        document.querySelectorAll('[data-cart-actions]').forEach(function (wrap) {
            const productId = wrap.getAttribute('data-product-id');
            const dmiAttr = wrap.getAttribute('data-dmi-id');
            const dmiId = (dmiAttr === null || dmiAttr === '') ? null : dmiAttr;
            const qty = qtyFromResponse(data, productId, dmiId);

            const addBtn = wrap.querySelector('[data-action="add"]');
            const inCart = wrap.querySelector('[data-in-cart]');
            const qtyLabel = wrap.querySelector('[data-qty]');

            if (qtyLabel) {
                qtyLabel.textContent = String(qty);
            }

            if (qty > 0) {
                if (addBtn) addBtn.style.display = 'none';
                if (inCart) inCart.style.display = 'flex';
            } else {
                if (addBtn) addBtn.style.display = '';
                if (inCart) inCart.style.display = 'none';
            }
        });
    }

    async function api(path, body) {
        const res = await fetch(path, {
            method: 'POST',
            headers: csrfHeaders(),
            credentials: 'same-origin',
            body: JSON.stringify(body)
        });
        if (res.status === 401 || res.status === 403) {
            window.location.href = '/login';
            return null;
        }
        const data = await res.json().catch(function () { return null; });
        if (!res.ok) {
            const msg = (data && data.message) ? data.message : 'No se pudo actualizar el carrito';
            alert(msg);
            return data;
        }
        return data;
    }

    async function applyResult(data) {
        if (!data) return;
        if (typeof data.cartCount === 'number') updateBadges(data.cartCount);
        if (data.lines) refreshCardButtons(data);
    }

    window.CartApi = {
        async add(productId, dailyMenuItemId, quantity) {
            const data = await api('/api/cart/items', {
                productId: Number(productId),
                dailyMenuItemId: dailyMenuItemId == null || dailyMenuItemId === ''
                    ? null
                    : Number(dailyMenuItemId),
                quantity: quantity || 1
            });
            await applyResult(data);
            return data;
        },
        async update(productId, dailyMenuItemId, quantity) {
            const data = await api('/api/cart/items/update', {
                productId: Number(productId),
                dailyMenuItemId: dailyMenuItemId == null || dailyMenuItemId === ''
                    ? null
                    : Number(dailyMenuItemId),
                quantity: Number(quantity)
            });
            await applyResult(data);
            return data;
        },
        async load() {
            const res = await fetch('/api/cart', {
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            if (!res.ok) return;
            const data = await res.json();
            updateBadges(data.cartCount);
            refreshCardButtons(data);
        }
    };

    document.addEventListener('click', function (e) {
        const add = e.target.closest('[data-action="add"]');
        if (add) {
            e.preventDefault();
            const wrap = add.closest('[data-cart-actions]');
            if (!wrap) return;
            add.disabled = true;
            CartApi.add(
                wrap.getAttribute('data-product-id'),
                wrap.getAttribute('data-dmi-id'),
                1
            ).finally(function () { add.disabled = false; });
            return;
        }
        const plus = e.target.closest('[data-action="plus"]');
        if (plus) {
            e.preventDefault();
            const wrap = plus.closest('[data-cart-actions]');
            const qtyEl = wrap.querySelector('[data-qty]');
            const q = (parseInt(qtyEl?.textContent || '0', 10) || 0) + 1;
            CartApi.update(
                wrap.getAttribute('data-product-id'),
                wrap.getAttribute('data-dmi-id'),
                q
            );
            return;
        }
        const minus = e.target.closest('[data-action="minus"]');
        if (minus) {
            e.preventDefault();
            const wrap = minus.closest('[data-cart-actions]');
            const qtyEl = wrap.querySelector('[data-qty]');
            const q = Math.max(0, (parseInt(qtyEl?.textContent || '0', 10) || 0) - 1);
            CartApi.update(
                wrap.getAttribute('data-product-id'),
                wrap.getAttribute('data-dmi-id'),
                q
            );
        }
    });

    // Al cargar menú (logueado), sincronizar badges y botones
    if (document.querySelector('[data-cart-actions]')) {
        CartApi.load();
    }
})();