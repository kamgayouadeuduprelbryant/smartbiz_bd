// SmartBiz - comportements JS communs a toutes les pages.
document.addEventListener('DOMContentLoaded', function () {
    // Active automatiquement le lien de la sidebar correspondant a la page courante.
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sb-nav a').forEach(function (link) {
        if (currentPath.startsWith(link.getAttribute('href')) && link.getAttribute('href') !== '/') {
            link.classList.add('active');
        }
    });
});
