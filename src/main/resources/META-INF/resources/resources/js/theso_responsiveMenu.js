function toggleSidebar() {
    document.body.classList.toggle('menu-open');
}

function handleResponsiveMenu() {
    if (window.innerWidth < 768) {
        document.body.classList.remove('menu-open');
    } else {
        document.body.classList.remove('menu-open'); // ou laisse activé selon le besoin
    }
}

window.addEventListener('resize', handleResponsiveMenu);
window.addEventListener('load', handleResponsiveMenu);



function toggleTreePanel() {
    const tree = document.getElementById('leftTreePanel');
    const iconSpan = document.querySelector('#treeToggleButton .ui-button-icon-left');
    const existingHeader = document.getElementById('treeMobileHeader');

    if (!tree || !iconSpan) return;

    const isVisible = tree.style.display === 'block';

    if (isVisible) {
        // Masquer arbre
        tree.style.display = 'none';

        // Supprimer le header mobile si présent
        if (existingHeader) {
            existingHeader.remove();
        }

        // Restaurer icône
        iconSpan.classList.remove('pi-times');
        iconSpan.classList.add('pi-sitemap');
    } else {
        // Afficher arbre
        tree.style.display = 'block';
        tree.style.position = 'fixed';
        tree.style.top = '50px';
        tree.style.left = '0';
        tree.style.width = '100vw';
        tree.style.height = 'calc(100vh - 50px)';
        tree.style.backgroundColor = '#e8f5e9';
        tree.style.zIndex = '9999';
        tree.style.overflowY = 'auto';

        // Si un ancien header existe (cas improbable), on le retire
        if (existingHeader) {
            existingHeader.remove();
        }

        // Ajouter header avec bouton retour
        const treeHeader = document.createElement('div');
        treeHeader.id = 'treeMobileHeader';
        treeHeader.style.position = 'fixed';
        treeHeader.style.top = '0';
        treeHeader.style.left = '0';
        treeHeader.style.width = '100vw';
        treeHeader.style.height = '50px';
        treeHeader.style.backgroundColor = 'var(--color-action-main)';
        treeHeader.style.display = 'flex';
        treeHeader.style.alignItems = 'center';
        treeHeader.style.justifyContent = 'center';
        treeHeader.style.zIndex = '10000';
        treeHeader.innerHTML = `
            <button onclick="toggleTreePanel()" 
                    style="
                        display: flex;
                        align-items: center;
                        gap: 0.5rem;
                        background-color: white;
                        color: var(--color-action-main);
                        border: none;
                        border-radius: 20px;
                        padding: 6px 12px;
                        font-size: 14px;
                        font-weight: 500;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                        cursor: pointer;
                    ">
                <i class="pi pi-arrow-left"></i>
                Retour
            </button>
        `;
        document.body.appendChild(treeHeader);

        // Changer icône du bouton principal
        iconSpan.classList.remove('pi-sitemap');
        iconSpan.classList.add('pi-times');
    }
}

