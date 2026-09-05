// SmartBiz - Kanban : glisser-deposer des taches entre colonnes,
// avec synchronisation immediate cote serveur via Fetch API (pas de rechargement de page).
document.addEventListener('DOMContentLoaded', function () {
    const cards = document.querySelectorAll('.kanban-card');
    const columns = document.querySelectorAll('.kanban-column-body');

    cards.forEach(function (card) {
        card.addEventListener('dragstart', function () {
            card.classList.add('dragging');
        });
        card.addEventListener('dragend', function () {
            card.classList.remove('dragging');
        });
    });

    columns.forEach(function (column) {
        column.addEventListener('dragover', function (e) {
            e.preventDefault();
            column.classList.add('drag-over');
        });

        column.addEventListener('dragleave', function () {
            column.classList.remove('drag-over');
        });

        column.addEventListener('drop', function (e) {
            e.preventDefault();
            column.classList.remove('drag-over');

            const dragging = document.querySelector('.kanban-card.dragging');
            if (!dragging) return;

            column.appendChild(dragging);

            const tacheId = dragging.getAttribute('data-tache-id');
            const nouveauStatut = column.closest('.kanban-column').getAttribute('data-statut');

            const csrfToken = document.querySelector('meta[name="_csrf"]');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
            const headers = { 'X-Requested-With': 'XMLHttpRequest' };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader.content] = csrfToken.content;
            }

            fetch('/taches/' + tacheId + '/statut?statut=' + encodeURIComponent(nouveauStatut), {
                method: 'POST',
                headers: headers
            }).catch(function (err) {
                console.error('Impossible de mettre a jour le statut de la tache :', err);
            });

            updateColumnCounts();
        });
    });

    function updateColumnCounts() {
        document.querySelectorAll('.kanban-column').forEach(function (col) {
            const count = col.querySelectorAll('.kanban-card').length;
            const badge = col.querySelector('.kanban-column-header .badge');
            if (badge) badge.textContent = count;
        });
    }
});
