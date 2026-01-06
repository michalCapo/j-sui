(function () {
    // Theme management
    window.setTheme = function (mode) {
        try {
            localStorage.setItem('theme', mode);
        } catch (e) {}
        var root = document.documentElement;
        var eff = (mode === 'system') ?
            ((window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'light') :
            mode;
        if (eff === 'dark') {
            root.classList.add('dark');
        } else {
            root.classList.remove('dark');
        }
    };
    try {
        window.setTheme(localStorage.getItem('theme') || 'system');
    } catch (_) {
        window.setTheme('light');
    }

    // DOM Swapping & Script Execution
    window.__applySwap = function (id, swap, html) {
        try {
            var el = document.getElementById(id);
            if (!el) return;
            var temp = document.createElement('div');
            temp.innerHTML = html;
            var scripts = Array.from(temp.querySelectorAll('script'));
            if (swap === 'outline') {
                el.outerHTML = html;
            } else if (swap === 'append') {
                el.insertAdjacentHTML('beforeend', html);
            } else if (swap === 'prepend') {
                el.insertAdjacentHTML('afterbegin', html);
            } else {
                el.innerHTML = html;
            }
            scripts.forEach(function (s) {
                var ns = document.createElement('script');
                if (s.src) {ns.src = s.src;} else {ns.textContent = s.textContent;}
                document.body.appendChild(ns);
                if (!s.src) document.body.removeChild(ns);
            });
        } catch (_) {}
    };

    // Toast Messaging
    window.__msg = function (message, cls) {
        var box = document.getElementById('__messages__');
        if (!box) {
            box = document.createElement('div');
            box.id = '__messages__';
            box.className = 'fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none';
            document.body.appendChild(box);
        }
        var toast = document.createElement('div');
        toast.className = 'px-4 py-3 rounded-lg shadow-xl translate-x-10 opacity-0 transition-all duration-300 pointer-events-auto min-w-[300px] border ' + (cls || 'bg-blue-600 text-white');
        toast.innerHTML = '<div class="flex items-center gap-3"><span class="flex-1 font-semibold">' + message + '</span><button class="opacity-70 hover:opacity-100">&times;</button></div>';
        box.appendChild(toast);
        setTimeout(function () {toast.classList.remove('translate-x-10', 'opacity-0');}, 10);
        var close = function () {
            toast.classList.add('translate-x-10', 'opacity-0');
            setTimeout(function () {if (toast.parentNode) box.removeChild(toast);}, 300);
        };
        toast.querySelector('button').onclick = close;
        setTimeout(close, 5000);
    };

    // Submit handler - called directly from form onsubmit
    window.__submit = function (path, swap, id, e) {
        try {
            if (e) {
                if (e.preventDefault) e.preventDefault();
                if (e.stopPropagation) e.stopPropagation();
                if (e.stopImmediatePropagation) e.stopImmediatePropagation();
            }
            var f = null;
            if (e && e.target) {
                if (e.target.tagName === 'FORM') {
                    f = e.target;
                } else if (e.target.closest && e.target.closest('form')) {
                    f = e.target.closest('form');
                }
            }
            if (!f || f.tagName !== 'FORM') return false;
            var fd = new FormData(f);
            var pairs = [];
            fd.forEach(function (v, k) {
                pairs.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
            });
            var opts = {
                method: 'POST',
                headers: {'content-type': 'application/x-www-form-urlencoded;charset=UTF-8'},
                body: pairs.join('&')
            };
            fetch(path, opts)
                .then(function (r) {return r.text();})
                .then(function (t) {__applySwap(id, swap, t);})
                .catch(function () {});
            return false;
        } catch (err) {
            console.error('__submit error:', err);
            return false;
        }
    };

    // POST/Submit handler
    window.__post = function (as, path, swap, id, e) {
        try {
            if (e) {
                if (e.preventDefault) e.preventDefault();
                if (e.stopPropagation) e.stopPropagation();
                if (e.stopImmediatePropagation) e.stopImmediatePropagation();
            }
            var opts = {method: 'POST'};
            if (as === 'FORM') {
                var f = null;
                if (e && e.target) {
                    // Try to find form: first check if target IS a form, then closest form, then form association via submitter
                    if (e.target.tagName === 'FORM') {
                        f = e.target;
                    } else if (e.target.closest && e.target.closest('form')) {
                        f = e.target.closest('form');
                    } else if (e.submitter && e.submitter.form) {
                        // Form association: button outside form but linked via form attribute
                        f = e.submitter.form;
                    }
                }
                if (!f || f.tagName !== 'FORM') {
                    console.error('__post FORM: could not find form element');
                    return false;
                }
                var fd = new FormData(f);
                var pairs = [];
                fd.forEach(function (v, k) {
                    pairs.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
                });
                opts.headers = {'content-type': 'application/x-www-form-urlencoded;charset=UTF-8'};
                opts.body = pairs.join('&');
            }
            fetch(path, opts)
                .then(function (r) {return r.text();})
                .then(function (t) {__applySwap(id, swap, t);})
                .catch(function (err) {console.error('__post fetch error:', err);});
            return false;
        } catch (err) {
            console.error('__post error:', err);
            return false;
        }
    };

    // Loading overlay
    var __loader = (function () {
        var S = {count: 0, t: 0, el: null};
        function build() {
            var overlay = document.createElement('div');
            overlay.className = 'fixed inset-0 z-50 flex items-center justify-center transition-opacity opacity-0';
            try {overlay.style.backdropFilter = 'blur(3px)';} catch (_) {}
            try {overlay.style.webkitBackdropFilter = 'blur(3px)';} catch (_) {}
            try {overlay.style.background = 'rgba(255,255,255,0.28)';} catch (_) {}
            try {overlay.style.pointerEvents = 'auto';} catch (_) {}
            var badge = document.createElement('div');
            badge.className = 'absolute top-3 left-3 flex items-center gap-2 rounded-full px-3 py-1 text-white shadow-lg ring-1 ring-white/30';
            badge.style.background = 'linear-gradient(135deg, #6366f1, #22d3ee)';
            var dot = document.createElement('span');
            dot.className = 'inline-block h-2.5 w-2.5 rounded-full bg-white/95 animate-pulse';
            var label = document.createElement('span');
            label.className = 'font-semibold tracking-wide';
            label.textContent = 'Loading…';
            var sub = document.createElement('span');
            sub.className = 'ml-1 text-white/85 text-xs';
            sub.textContent = 'Please wait';
            sub.style.color = 'rgba(255,255,255,0.9)';
            badge.appendChild(dot);
            badge.appendChild(label);
            badge.appendChild(sub);
            overlay.appendChild(badge);
            document.body.appendChild(overlay);
            try {requestAnimationFrame(function () {overlay.style.opacity = '1';});} catch (_) {}
            return overlay;
        }
        function start() {
            S.count = S.count + 1;
            if (S.el != null) return {stop: stop};
            if (S.t) return {stop: stop};
            S.t = setTimeout(function () {
                S.t = 0;
                if (S.el == null) S.el = build();
            }, 120);
            return {stop: stop};
        }
        function stop() {
            if (S.count > 0) S.count = S.count - 1;
            if (S.count !== 0) return;
            if (S.t) {try {clearTimeout(S.t);} catch (_) {} S.t = 0;}
            if (S.el) {
                var el = S.el; S.el = null;
                try {el.style.opacity = '0';} catch (_) {}
                setTimeout(function () {
                    try {if (el && el.parentNode) el.parentNode.removeChild(el);} catch (_) {}
                }, 160);
            }
        }
        return {start: start};
    })();

    // Error handling
    window.__error = function (message) {
        (function () {
            try {
                var box = document.getElementById('__messages__');
                if (box == null) {
                    box = document.createElement('div');
                    box.id = '__messages__';
                    box.style.position = 'fixed';
                    box.style.top = '0';
                    box.style.right = '0';
                    box.style.padding = '8px';
                    box.style.zIndex = '9999';
                    box.style.pointerEvents = 'none';
                    document.body.appendChild(box);
                }
                var n = document.getElementById('__error_toast__');
                if (!n) {
                    n = document.createElement('div');
                    n.id = '__error_toast__';
                    n.style.display = 'flex';
                    n.style.alignItems = 'center';
                    n.style.gap = '10px';
                    n.style.padding = '12px 16px';
                    n.style.margin = '8px';
                    n.style.borderRadius = '12px';
                    n.style.minHeight = '44px';
                    n.style.minWidth = '340px';
                    n.style.maxWidth = '340px';
                    n.style.background = '#fee2e2';
                    n.style.color = '#991b1b';
                    n.style.border = '1px solid #fecaca';
                    n.style.borderLeft = '4px solid #dc2626';
                    n.style.boxShadow = '0 6px 18px rgba(0,0,0,0.08)';
                    n.style.fontWeight = '600';
                    n.style.pointerEvents = 'auto';
                    var dot = document.createElement('span');
                    dot.style.width = '10px';
                    dot.style.height = '10px';
                    dot.style.borderRadius = '9999px';
                    dot.style.background = '#dc2626';
                    n.appendChild(dot);
                    var span = document.createElement('span');
                    span.id = '__error_text__';
                    n.appendChild(span);
                    var btn = document.createElement('button');
                    btn.textContent = 'Reload';
                    btn.style.background = '#991b1b';
                    btn.style.color = '#fff';
                    btn.style.border = 'none';
                    btn.style.padding = '6px 10px';
                    btn.style.borderRadius = '8px';
                    btn.style.cursor = 'pointer';
                    btn.style.fontWeight = '700';
                    btn.onclick = function () {try {window.location.reload();} catch (_) {} };
                    n.appendChild(btn);
                    box.appendChild(n);
                }
                var spanText = document.getElementById('__error_text__');
                if (spanText) {
                    spanText.textContent = message || 'Something went wrong ...';
                }
            } catch (_) {
                try {alert(message || 'Something went wrong ...');} catch (__) {}
            }
        })();
    };

    // Client-side navigation load
    window.__load = function (href) {
        try {if (typeof event !== 'undefined' && event && event.preventDefault) {event.preventDefault();} } catch (e1) {}
        var loaderTimer = null;
        var loaderStarted = false;
        var L = null;
        loaderTimer = setTimeout(function () {
            if (!loaderStarted) {
                loaderStarted = true;
                try {L = (function () {try {return __loader.start();} catch (e2) {return {stop: function () {}};} })();} catch (e3) {}
            }
        }, 50);
        fetch(href, {method: 'GET'})
            .then(function (resp) {
                if (!resp.ok) {throw new Error('HTTP ' + resp.status);}
                return resp.text();
            })
            .then(function (html) {
                if (loaderTimer) {clearTimeout(loaderTimer); loaderTimer = null;}
                if (loaderStarted && L) {try {L.stop();} catch (e4) {} }
                var parser = new DOMParser();
                var doc = parser.parseFromString(html, 'text/html');
                document.title = doc.title;
                document.body.innerHTML = doc.body.innerHTML;
                var scripts = [...doc.body.querySelectorAll('script'), ...doc.head.querySelectorAll('script')];
                for (var i = 0; i < scripts.length; i++) {
                    var newScript = document.createElement('script');
                    newScript.textContent = scripts[i].textContent;
                    document.body.appendChild(newScript);
                }
                window.history.pushState({}, doc.title, href);
            })
            .catch(function (e5) {
                if (loaderTimer) {clearTimeout(loaderTimer); loaderTimer = null;}
                if (loaderStarted && L) {try {L.stop();} catch (e6) {} }
                try {__error('Something went wrong ...');} catch (e7) {}
            });
    };
})();
