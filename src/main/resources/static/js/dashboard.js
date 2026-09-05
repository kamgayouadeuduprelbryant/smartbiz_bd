// SmartBiz - Dashboard : rafraichissement des cartes via Fetch API (sans F5)
// et initialisation des graphiques Chart.js.
(function () {
    function rafraichirStats() {
        if (!window.SB_STATS_URL) return;

        fetch(window.SB_STATS_URL, { headers: { 'Accept': 'application/json' } })
            .then(function (res) {
                if (!res.ok) throw new Error('Reponse invalide du serveur');
                return res.json();
            })
            .then(function (stats) {
                const elEmployes = document.getElementById('statEmployes');
                const elDepartements = document.getElementById('statDepartements');
                const elClients = document.getElementById('statClients');
                const elProduits = document.getElementById('statProduits');
                const elCA = document.getElementById('statCA');
                const elDepenses = document.getElementById('statDepenses');
                const elBenefice = document.getElementById('statBenefice');
                const elFacturesImpayees = document.getElementById('statFacturesImpayees');
                if (elEmployes) elEmployes.textContent = stats.nombreEmployesActifs;
                if (elDepartements) elDepartements.textContent = stats.nombreDepartements;
                if (elClients) elClients.textContent = stats.nombreClients;
                if (elProduits) elProduits.textContent = stats.nombreProduits;
                if (elCA) elCA.textContent = stats.chiffreAffaires;
                if (elDepenses) elDepenses.textContent = stats.totalDepenses;
                if (elBenefice) elBenefice.textContent = stats.benefice;
                if (elFacturesImpayees) elFacturesImpayees.textContent = stats.nombreFacturesImpayees;
            })
            .catch(function (err) {
                console.warn('Impossible de rafraichir les statistiques du dashboard :', err);
            });
    }

    function initGraphiques() {
        const ctxRevenus = document.getElementById('chartRevenus');
        if (ctxRevenus && window.Chart) {
            const revenus = window.SB_REVENUS_MENSUELS || [];
            const labels = revenus.length ? revenus.map(function (r) { return r.mois; }) : ['—'];
            const valeurs = revenus.length ? revenus.map(function (r) { return r.montant; }) : [0];

            new Chart(ctxRevenus, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Revenus',
                        data: valeurs,
                        borderColor: '#4f46e5',
                        backgroundColor: 'rgba(79,70,229,0.1)',
                        tension: 0.35,
                        fill: true
                    }]
                },
                options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
            });
        }

        const ctxEmployes = document.getElementById('chartEmployes');
        if (ctxEmployes && window.Chart) {
            new Chart(ctxEmployes, {
                type: 'doughnut',
                data: {
                    labels: ['Actifs', 'En conge', 'Suspendus'],
                    datasets: [{ data: [window.SB_EMPLOYES_ACTIFS || 0, 0, 0], backgroundColor: ['#16a34a', '#ca8a04', '#dc2626'] }]
                },
                options: { plugins: { legend: { position: 'bottom' } } }
            });
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        initGraphiques();
        // Rafraichit les cartes toutes les 30 secondes, sans jamais recharger la page.
        setInterval(rafraichirStats, 30000);
    });
})();
