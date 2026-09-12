/**
 * SIW Cinema - Catalogo & Ricerca Film (React 18 Puro / Vanilla React)
 * Implementazione nativa con React.createElement - ZERO dipendenze da Babel o transpiler esterni.
 */
(function() {
    'use strict';

    const e = React.createElement;

    function FilmCatalogApp() {
        const [films, setFilms] = React.useState([]);
        const [titolo, setTitolo] = React.useState('');
        const [genere, setGenere] = React.useState('');
        const [regista, setRegista] = React.useState('');
        const [anno, setAnno] = React.useState('');
        const [loading, setLoading] = React.useState(true);

        React.useEffect(function() {
            fetch('/api/movies')
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error('HTTP status ' + response.status);
                    }
                    return response.json();
                })
                .then(function(data) {
                    setFilms(data || []);
                    setLoading(false);
                })
                .catch(function(error) {
                    console.error('Errore nel recupero dei film:', error);
                    setLoading(false);
                });
        }, []);

        const filteredFilms = films.filter(function(film) {
            const matchTitolo = !titolo || (film.titolo && film.titolo.toLowerCase().includes(titolo.toLowerCase().trim()));
            const matchGenere = !genere || (film.genere && film.genere.toLowerCase().includes(genere.toLowerCase().trim()));
            const dName = film.nomeRegista || film.registaNome;
            const matchRegista = !regista || (dName && dName.toLowerCase().includes(regista.toLowerCase().trim()));
            const matchAnno = !anno || (film.anno && film.anno.toString() === anno.toString().trim());
            return matchTitolo && matchGenere && matchRegista && matchAnno;
        });

        const uniqueGenres = Array.from(new Set(films.map(function(f) { return f.genere; }).filter(Boolean)));

        const handleReset = function() {
            setTitolo('');
            setGenere('');
            setRegista('');
            setAnno('');
        };

        const isFiltered = Boolean(titolo || genere || regista || anno);

        // --- Pannello dei Filtri ---
        const inputTitolo = e('div', { style: { flex: '2', minWidth: '180px' } },
            e('label', { style: { display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.35rem', fontWeight: '500' } },
                e('i', { className: 'fa-solid fa-magnifying-glass', style: { marginRight: '0.35rem', color: 'var(--accent-primary)' } }),
                'Titolo'
            ),
            e('input', {
                type: 'text',
                className: 'form-control',
                placeholder: 'Cerca titolo...',
                value: titolo,
                onChange: function(evt) { setTitolo(evt.target.value); }
            })
        );

        const genreOptions = [e('option', { key: 'all', value: '' }, 'Tutti i generi')];
        uniqueGenres.forEach(function(g) {
            genreOptions.push(e('option', { key: g, value: g }, g));
        });

        const selectGenere = e('div', { style: { flex: '1.2', minWidth: '140px' } },
            e('label', { style: { display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.35rem', fontWeight: '500' } },
                e('i', { className: 'fa-solid fa-masks-theater', style: { marginRight: '0.35rem', color: 'var(--accent-primary)' } }),
                'Genere'
            ),
            e('select', {
                className: 'form-control',
                value: genere,
                onChange: function(evt) { setGenere(evt.target.value); }
            }, genreOptions)
        );

        const inputRegista = e('div', { style: { flex: '1.5', minWidth: '160px' } },
            e('label', { style: { display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.35rem', fontWeight: '500' } },
                e('i', { className: 'fa-solid fa-video', style: { marginRight: '0.35rem', color: 'var(--accent-primary)' } }),
                'Regista'
            ),
            e('input', {
                type: 'text',
                className: 'form-control',
                placeholder: 'Nome regista...',
                value: regista,
                onChange: function(evt) { setRegista(evt.target.value); }
            })
        );

        const inputAnno = e('div', { style: { flex: '0.8', minWidth: '100px' } },
            e('label', { style: { display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.35rem', fontWeight: '500' } },
                e('i', { className: 'fa-regular fa-calendar', style: { marginRight: '0.35rem', color: 'var(--accent-primary)' } }),
                'Anno'
            ),
            e('input', {
                type: 'number',
                className: 'form-control',
                placeholder: 'Es. 2024',
                value: anno,
                onChange: function(evt) { setAnno(evt.target.value); }
            })
        );

        const filterControls = [inputTitolo, selectGenere, inputRegista, inputAnno];

        if (isFiltered) {
            filterControls.push(
                e('div', { key: 'reset-btn-wrap', style: { marginBottom: '2px' } },
                    e('button', {
                        type: 'button',
                        onClick: handleReset,
                        className: 'btn btn-outline btn-sm',
                        style: { height: '38px' }
                    },
                    e('i', { className: 'fa-solid fa-xmark', style: { marginRight: '0.35rem' } }),
                    'Reset'
                    )
                )
            );
        }

        const filterBox = e('div', { className: 'filter-box' },
            e('div', { style: { display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' } }, filterControls),
            e('div', { style: { marginTop: '0.85rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' } },
                e('span', { style: { fontSize: '0.825rem', color: 'var(--text-muted)' } },
                    'Risultati: ',
                    e('strong', { style: { color: 'var(--accent-primary)' } }, filteredFilms.length),
                    ' film su ' + films.length
                ),
                e('span', { className: 'badge', style: { background: 'var(--accent-primary-light)', color: '#c7d2fe', border: '1px solid var(--accent-primary-border)', fontSize: '0.725rem' } },
                    e('i', { className: 'fa-brands fa-react', style: { marginRight: '0.3rem' } }),
                    'Filtro Dinamico React Puro'
                )
            )
        );

        // --- Stato di Caricamento ---
        const loadingBox = loading ? e('div', { style: { textAlign: 'center', padding: '3.5rem 0' } },
            e('i', { className: 'fa-solid fa-spinner fa-spin', style: { fontSize: '2rem', color: 'var(--accent-primary)', marginBottom: '0.75rem' } }),
            e('p', { style: { color: 'var(--text-muted)', fontSize: '0.9rem' } }, 'Caricamento catalogo...')
        ) : null;

        // --- Griglia delle Schede Film ---
        let cardsGrid = null;
        if (!loading && filteredFilms.length > 0) {
            const filmCards = filteredFilms.map(function(film) {
                const posterImg = e('div', { className: 'film-poster' },
                    e('img', {
                        src: film.locandina || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600',
                        alt: film.titolo
                    }),
                    e('div', { className: 'film-score' },
                        e('i', { className: 'fa-solid fa-star' }),
                        e('span', null, (film.mediaVoti && film.mediaVoti > 0) ? film.mediaVoti + ' / 5' : 'N/D')
                    )
                );

                const cardBody = e('div', { className: 'card-body' },
                    e('h3', { className: 'card-title' },
                        e('a', { href: '/film/' + film.id }, film.titolo)
                    ),
                    e('div', { className: 'card-meta' },
                        e('span', null, film.anno),
                        ' • ',
                        e('span', null, film.genere),
                        ' • ',
                        e('span', null, film.durata + ' min')
                    ),
                    e('p', { style: { fontSize: '0.825rem', color: 'var(--text-muted)', marginBottom: '0.85rem' } },
                        'Regia: ',
                        e('strong', { style: { color: 'var(--text-main)' } }, film.nomeRegista || film.registaNome || 'N/D')
                    ),
                    e('div', { className: 'card-footer' },
                        e('a', { href: '/film/' + film.id, className: 'btn btn-sm btn-secondary', style: { width: '100%', textAlign: 'center' } },
                            'Scheda & Recensioni'
                        )
                    )
                );

                return e('div', { key: film.id, className: 'film-card' }, posterImg, cardBody);
            });

            cardsGrid = e('div', { className: 'cards-grid' }, filmCards);
        }

        // --- Nessun Risultato ---
        let noResultsBox = null;
        if (!loading && filteredFilms.length === 0) {
            const noResultsChildren = [
                e('i', { key: 'icon', className: 'fa-solid fa-film', style: { fontSize: '2.5rem', color: 'var(--text-dim)', marginBottom: '0.5rem' } }),
                e('h3', { key: 'h3' }, 'Nessun film trovato'),
                e('p', { key: 'p', style: { color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.25rem' } }, 'Nessun film corrisponde ai filtri selezionati.')
            ];

            if (isFiltered) {
                noResultsChildren.push(
                    e('button', {
                        key: 'btn',
                        type: 'button',
                        onClick: handleReset,
                        className: 'btn btn-sm btn-primary',
                        style: { marginTop: '1rem' }
                    }, 'Mostra tutti i film')
                );
            }

            noResultsBox = e('div', {
                style: {
                    textAlign: 'center',
                    padding: '3.5rem 0',
                    background: 'var(--bg-card)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-subtle)'
                }
            }, noResultsChildren);
        }

        return e('div', null, filterBox, loadingBox, cardsGrid, noResultsBox);
    }

    // Inizializzazione React 18
    document.addEventListener('DOMContentLoaded', function() {
        const rootElement = document.getElementById('react-film-search');
        if (rootElement && window.ReactDOM && window.React) {
            const root = ReactDOM.createRoot(rootElement);
            root.render(e(FilmCatalogApp));
        }
    });

})();
