// SmartBiz - Produits : petites ameliorations UX pour la page liste.
document.addEventListener('DOMContentLoaded', function () {
    const modalCategorie = document.getElementById('nouvelleCategorie');
    if (modalCategorie) {
        modalCategorie.addEventListener('hidden.bs.modal', function () {
            const form = modalCategorie.querySelector('form');
            if (form) form.reset();
        });
    }
});
