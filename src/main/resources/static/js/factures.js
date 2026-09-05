// SmartBiz - Facture : pre-remplissage du libelle et du prix unitaire
// lorsqu'un produit du catalogue est choisi pour une nouvelle ligne.
document.addEventListener('DOMContentLoaded', function () {
    const select = document.getElementById('selectProduitFacture');
    const libelle = document.getElementById('libelleFacture');
    const prix = document.getElementById('prixFacture');
    if (!select || !libelle || !prix) return;

    select.addEventListener('change', function () {
        const option = select.options[select.selectedIndex];
        const nom = option.getAttribute('data-nom');
        const prixValeur = option.getAttribute('data-prix');

        if (nom) {
            libelle.value = nom;
        }
        if (prixValeur) {
            prix.value = prixValeur;
        }
    });
});
