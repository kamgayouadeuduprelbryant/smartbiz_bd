// SmartBiz - Assistant : envoie la question au serveur en Fetch API et
// affiche la reponse dans le fil de discussion, sans recharger la page.
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('assistantForm');
    const input = document.getElementById('assistantQuestion');
    const fil = document.getElementById('assistantFil');

    if (!form || !input || !fil) return;

    function getCsrf() {
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        return token && header ? { name: header.content, value: token.content } : null;
    }

    function ajouterMessage(texte, estUtilisateur) {
        const ligne = document.createElement('div');
        ligne.className = 'd-flex mb-3' + (estUtilisateur ? ' justify-content-end' : '');

        const bulle = document.createElement('div');
        bulle.className = (estUtilisateur ? 'text-white' : 'bg-body-secondary') + ' rounded p-3';
        bulle.style.maxWidth = '75%';
        if (estUtilisateur) bulle.style.background = 'var(--sb-primary)';
        bulle.textContent = texte;

        ligne.appendChild(bulle);
        fil.appendChild(ligne);
        fil.scrollTop = fil.scrollHeight;
    }

    function poserQuestion(question) {
        if (!question.trim()) return;
        ajouterMessage(question, true);
        input.value = '';

        const csrf = getCsrf();
        const params = new URLSearchParams();
        params.append('question', question);

        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        if (csrf) headers[csrf.name] = csrf.value;

        fetch('/assistant/question', { method: 'POST', headers: headers, body: params.toString() })
            .then(function (res) { return res.json(); })
            .then(function (data) { ajouterMessage(data.reponse, false); })
            .catch(function () { ajouterMessage("Une erreur est survenue, reessayez.", false); });
    }

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        poserQuestion(input.value);
    });

    document.addEventListener('click', function (e) {
        if (e.target.classList.contains('assistant-suggestion')) {
            e.preventDefault();
            poserQuestion(e.target.textContent);
        }
    });
});
