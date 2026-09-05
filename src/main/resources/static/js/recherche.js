// SmartBiz - Recherche globale (header).
// L'endpoint /recherche?q=... sera branche dans une partie ulterieure du
// projet, une fois les modules Clients/Produits/Factures en place (point 25
// du cahier des charges). Pour l'instant, ce script prepare l'interaction
// Fetch API sans appeler d'endpoint inexistant.
(function () {
    const SEARCH_ENDPOINT = '/recherche';
    let debounceTimer = null;

    document.addEventListener('DOMContentLoaded', function () {
        const input = document.getElementById('rechercheGlobale');
        const resultsBox = document.getElementById('rechercheResultats');
        if (!input || !resultsBox) return;

        input.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            const q = input.value.trim();

            if (q.length < 2) {
                resultsBox.classList.add('d-none');
                resultsBox.innerHTML = '';
                return;
            }

            debounceTimer = setTimeout(function () {
                fetch(SEARCH_ENDPOINT + '?q=' + encodeURIComponent(q))
                    .then(function (res) {
                        if (!res.ok) throw new Error('Recherche indisponible');
                        return res.json();
                    })
                    .then(function (resultats) {
                        afficherResultats(resultsBox, resultats);
                    })
                    .catch(function () {
                        // Le module de recherche globale n'est pas encore branche : on n'affiche rien.
                        resultsBox.classList.add('d-none');
                    });
            }, 300);
        });

        document.addEventListener('click', function (e) {
            if (!resultsBox.contains(e.target) && e.target !== input) {
                resultsBox.classList.add('d-none');
            }
        });
    });

    function afficherResultats(box, resultats) {
        if (!resultats || resultats.length === 0) {
            box.innerHTML = '<div class="p-3 text-muted small">Aucun resultat.</div>';
        } else {
            box.innerHTML = resultats.map(function (r) {
                return '<a class="dropdown-item d-block px-3 py-2" href="' + r.url + '">' +
                    '<span class="badge bg-secondary me-2">' + r.type + '</span>' + r.libelle + '</a>';
            }).join('');
        }
        box.classList.remove('d-none');
    }
})();
