// SmartBiz - Notifications : rafraichit la pastille et la liste du header
// via Fetch API, sans rechargement de page (point 22 du cahier des charges).
(function () {
    function getCsrf() {
        const header = document.querySelector('.sb-header');
        if (!header) return null;
        return {
            token: header.getAttribute('data-csrf-token'),
            headerName: header.getAttribute('data-csrf-header')
        };
    }

    function rafraichirNotifications() {
        fetch('/notifications/recentes', { headers: { 'Accept': 'application/json' } })
            .then(function (res) {
                if (!res.ok) throw new Error('Reponse invalide');
                return res.json();
            })
            .then(function (data) {
                const badge = document.getElementById('notifBadge');
                const liste = document.getElementById('notifList');
                if (!badge || !liste) return;

                if (data.nonLues > 0) {
                    badge.textContent = data.nonLues > 9 ? '9+' : data.nonLues;
                    badge.classList.remove('d-none');
                } else {
                    badge.classList.add('d-none');
                }

                if (!data.notifications || data.notifications.length === 0) {
                    liste.innerHTML = '<div class="text-muted small px-3 py-2">Aucune nouvelle notification.</div>';
                    return;
                }

                liste.innerHTML = data.notifications.map(function (n) {
                    const lienOuverture = n.lien || '#';
                    const classeNonLue = n.lue ? '' : 'fw-semibold';
                    return '<a href="' + lienOuverture + '" class="dropdown-item small notif-item ' + classeNonLue + '" data-id="' + n.id + '">' +
                        '<div>' + n.message + '</div>' +
                        '<div class="text-muted" style="font-size:.72rem">' + n.dateCreation + '</div>' +
                        '</a>';
                }).join('');
            })
            .catch(function (err) {
                console.warn('Impossible de rafraichir les notifications :', err);
            });
    }

    document.addEventListener('click', function (e) {
        const item = e.target.closest('.notif-item');
        if (!item) return;

        const id = item.getAttribute('data-id');
        const csrf = getCsrf();
        const headers = {};
        if (csrf && csrf.token) headers[csrf.headerName] = csrf.token;

        fetch('/notifications/' + id + '/lue', { method: 'POST', headers: headers })
            .catch(function (err) { console.warn('Marquage comme lue impossible :', err); });
    });

    document.addEventListener('DOMContentLoaded', function () {
        rafraichirNotifications();
        setInterval(rafraichirNotifications, 20000);
    });
})();
