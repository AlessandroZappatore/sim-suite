/**
 * SimSuite v1.0 - Main JavaScript Logic
 * 
 * Questo file contiene tutta la logica JavaScript per:
 * - Gestione delle tab e navigazione
 * - Animazioni e effetti visivi
 * - Sistema di download con form modal
 * - Integrazione Google Apps Script
 * - Utilities varie
 */

// ==========================================
// CONFIGURAZIONE E VARIABILI GLOBALI
// ==========================================

// URLs per i download
const downloadUrls = {
    installer: 'https://github.com/AlessandroZappatore/sim-suite/releases/download/SimSuite_1.0/SimSuite_Installer_1.0.0.exe',
    zip: 'https://github.com/AlessandroZappatore/sim-suite/releases/download/SimSuite_1.0/Sim_suite.zip'
};

// URL del Google Apps Script Web App
const GOOGLE_SCRIPT_URL = 'https://script.google.com/macros/s/AKfycbySuUXG_POgHZjW7OQtSflj_ucS3-8qZbK2Q1oaRmL8WxMuG0Ohe8ubGh9-BHAzT8yD/exec';

// Variabili globali
let currentDownloadType = '';

// Elementi DOM
let tabButtons, contentSections, osTabButtons, osInstructions, toastElement;
let feedbackModal, feedbackForm, feedbackCancelButton, feedbackSubmitButton, starButtons, ratingInput;

// ==========================================
// ANIMAZIONI E EFFETTI VISIVI
// ==========================================

/**
 * Anima i contatori numerici nella pagina
 */
function animateCounters() {
    const counters = document.querySelectorAll('.animated-counter');
    counters.forEach(counter => {
        const target = counter.textContent;
        if (target.includes('∞') || target.includes('%') || target.includes('min') || 
            target.includes('MB') || target.includes('Java') || target.includes('Web') || 
            target.includes('Open')) {
            return; // Skip non-numeric counters
        }

        const finalValue = parseInt(target);
        if (isNaN(finalValue)) return;

        let current = 0;
        const increment = finalValue / 30;
        const timer = setInterval(() => {
            current += increment;
            if (current >= finalValue) {
                counter.textContent = finalValue;
                clearInterval(timer);
            } else {
                counter.textContent = Math.floor(current);
            }
        }, 50);
    });
}

/**
 * Inizializza gli effetti 3D per le card
 */
function init3DEffects() {
    const cards = document.querySelectorAll('.interactive-card, .feature-card');
    cards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const rotateX = (y - centerY) / 1000;
            const rotateY = (centerX - x) / 1000;

            card.style.transform = `perspective(1500px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.03)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(1500px) rotateX(0) rotateY(0) scale(1)';
        });
    });
}

/**
 * Inizializza le animazioni al scroll
 */
function initScrollAnimations() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -30px 0px'
    });

    document.querySelectorAll('.animate-slide-up, .animate-fade-in').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = el.classList.contains('animate-slide-up') ? 'translateY(30px)' : 'translateY(0)';
        el.style.transition = 'all 0.7s cubic-bezier(0.25, 0.8, 0.25, 1)';
        observer.observe(el);
    });
}

/**
 * Crea particelle fluttuanti dinamiche
 */
function createFloatingParticle() {
    const particleContainer = document.querySelector('.fixed.inset-0.overflow-hidden');
    if (!particleContainer) return;

    const particle = document.createElement('div');
    particle.className = 'particle';
    particle.style.top = Math.random() * 100 + '%';
    particle.style.left = Math.random() * 100 + '%';
    particle.style.animationDelay = Math.random() * 5 + 's';
    particle.style.animationDuration = (4 + Math.random() * 6) + 's';

    particleContainer.appendChild(particle);

    setTimeout(() => {
        particle.remove();
    }, parseFloat(particle.style.animationDuration) * 1000 + parseFloat(particle.style.animationDelay) * 1000);
}

// ==========================================
// GESTIONE NAVIGAZIONE E TAB
// ==========================================

/**
 * Inizializza gli eventi per le tab principali
 */
function initTabEvents() {
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            contentSections.forEach(section => section.classList.remove('active'));
            const targetSectionId = button.dataset.tab;
            const targetSection = document.getElementById(targetSectionId);
            if (targetSection) {
                targetSection.classList.add('active');
                setTimeout(() => {
                    if (targetSectionId === 'benvenuto' || targetSectionId === 'installazione') {
                        animateCounters();
                    }
                }, 100);
            }
        });
    });
}

/**
 * Inizializza gli eventi per le tab del sistema operativo
 */
function initOsTabEvents() {
    osTabButtons.forEach(button => {
        button.addEventListener('click', () => {
            osTabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            osInstructions.forEach(instruction => instruction.classList.remove('active'));
            const targetOsInstructionId = button.dataset.os + '-instructions';
            const targetOsInstruction = document.getElementById(targetOsInstructionId);
            if (targetOsInstruction) {
                targetOsInstruction.classList.add('active');
            }
        });
    });
}

// ==========================================
// SISTEMA TOAST E NOTIFICHE
// ==========================================

/**
 * Mostra un messaggio toast
 * @param {string} message - Il messaggio da mostrare
 */
function showToast(message) {
    toastElement.textContent = message;
    toastElement.classList.add('show');
    setTimeout(() => {
        toastElement.classList.remove('show');
    }, 3000);
}

/**
 * Copia il contenuto di un elemento negli appunti
 * @param {string} elementId - ID dell'elemento da copiare
 * @param {string} message - Messaggio da mostrare nel toast
 */
function copyToClipboard(elementId, message) {
    const codeElement = document.getElementById(elementId);
    if (!codeElement) return;
    const textToCopy = codeElement.innerText;
    navigator.clipboard.writeText(textToCopy).then(() => {
        showToast(message || 'Copiato negli appunti! ✨');
    }).catch(err => {
        showToast('Errore durante la copia.');
        console.error('Errore durante la copia: ', err);
    });
}

/**
 * Copia un testo statico negli appunti
 * @param {string} textToCopy - Testo da copiare
 * @param {string} message - Messaggio da mostrare nel toast
 */
function copyToClipboardStatic(textToCopy, message) {
    navigator.clipboard.writeText(textToCopy).then(() => {
        showToast(message || 'Copiato negli appunti! ✨');
    }).catch(err => {
        showToast('Errore durante la copia.');
        console.error('Errore durante la copia: ', err);
    });
}

// ==========================================
// GESTIONE MODAL DI FEEDBACK
// ==========================================

/**
 * Apre il modal di feedback
 */
function openFeedbackModal() {
    console.log('🔍 openFeedbackModal chiamata');
    
    feedbackModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    
    // Reset form
    feedbackForm.reset();
    resetStarRating();
    
    // Focus sul primo campo
    setTimeout(() => {
        document.getElementById('feedbackName').focus();
    }, 300);
}

/**
 * Chiude il modal di feedback
 */
function closeFeedbackModal() {
    feedbackModal.classList.add('hidden');
    document.body.style.overflow = 'auto';
}

/**
 * Inizializza gli eventi del modal di feedback
 */
function initFeedbackModalEvents() {
    // Evento click su pulsante annulla
    feedbackCancelButton.addEventListener('click', closeFeedbackModal);

    // Chiudi modal cliccando fuori
    feedbackModal.addEventListener('click', (e) => {
        if (e.target === feedbackModal) {
            closeFeedbackModal();
        }
    });

    // Chiudi modal con ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !feedbackModal.classList.contains('hidden')) {
            closeFeedbackModal();
        }
    });

    // Gestione rating a stelle
    starButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const rating = parseInt(button.dataset.rating);
            setStarRating(rating);
        });

        button.addEventListener('mouseenter', () => {
            const rating = parseInt(button.dataset.rating);
            highlightStars(rating);
        });
    });

    // Reset stelle quando si esce dall'area rating
    document.getElementById('starRating').addEventListener('mouseleave', () => {
        const currentRating = parseInt(ratingInput.value) || 0;
        highlightStars(currentRating);
    });

    // Validazione email real-time
    document.getElementById('feedbackEmail').addEventListener('input', (e) => {
        const email = e.target.value;
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        if (email && !emailRegex.test(email)) {
            e.target.style.borderColor = '#ef4444';
        } else {
            e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
        }
    });

    // Gestione invio form
    feedbackForm.addEventListener('submit', handleFeedbackSubmit);
}

/**
 * Imposta il rating delle stelle
 */
function setStarRating(rating) {
    ratingInput.value = rating;
    highlightStars(rating);
}

/**
 * Evidenzia le stelle fino al rating specificato
 */
function highlightStars(rating) {
    starButtons.forEach((button, index) => {
        if (index < rating) {
            button.classList.add('active');
        } else {
            button.classList.remove('active');
        }
    });
}

/**
 * Resetta il rating delle stelle
 */
function resetStarRating() {
    ratingInput.value = '';
    starButtons.forEach(button => {
        button.classList.remove('active');
    });
}

/**
 * Gestisce l'invio del form di feedback
 * @param {Event} e - Evento di submit
 */
async function handleFeedbackSubmit(e) {
    e.preventDefault();
    
    // Verifica che sia stata data una valutazione
    if (!ratingInput.value) {
        showToast('⚠️ Per favore, dai una valutazione con le stelle');
        return;
    }

    // Mostra loading
    const feedbackText = feedbackSubmitButton.querySelector('.feedback-text');
    const feedbackLoader = feedbackSubmitButton.querySelector('.feedback-loader');
    
    feedbackText.classList.add('hidden');
    feedbackLoader.classList.remove('hidden');
    feedbackSubmitButton.disabled = true;

    try {
        // Raccogli dati del form
        const formData = {
            type: 'feedback', // Identificatore per il tipo di dati
            name: document.getElementById('feedbackName').value,
            email: document.getElementById('feedbackEmail').value,
            feedbackType: document.getElementById('feedbackType').value,
            rating: parseInt(ratingInput.value),
            comment: document.getElementById('feedbackComment').value,
            favoriteFeature: document.getElementById('favoriteFeature').value || '',
            suggestions: document.getElementById('feedbackSuggestions').value || '',
            timestamp: new Date().toISOString(),
            userAgent: navigator.userAgent,
            language: navigator.language
        };

        // Invia dati a Google Spreadsheet
        await sendDataToGoogleSheets(formData);
        
        // Mostra successo
        showToast('✅ Grazie per il tuo feedback! È molto importante per noi 🙏');
        
        // Chiudi modal
        closeFeedbackModal();
        
    } catch (error) {
        console.error('Errore nell\'invio del feedback:', error);
        showToast('⚠️ Errore nell\'invio del feedback. Riprova più tardi.');
    } finally {
        // Reset button state
        feedbackText.classList.remove('hidden');
        feedbackLoader.classList.add('hidden');
        feedbackSubmitButton.disabled = false;
    }
}

// ==========================================
// INTEGRAZIONE GOOGLE APPS SCRIPT
// ==========================================

/**
 * Invia i dati del form a Google Spreadsheet
 * @param {Object} data - Dati da inviare
 */
async function sendDataToGoogleSheets(data) {
    // Se l'URL di Google Apps Script non è configurato, simula l'invio
    if (!GOOGLE_SCRIPT_URL || GOOGLE_SCRIPT_URL === 'YOUR_GOOGLE_APPS_SCRIPT_URL_HERE') {
        console.log('🔧 Google Apps Script URL non configurato (o è un placeholder). Dati che sarebbero stati inviati:', data);
        // Simula un delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        return { status: 'success', message: 'Modalità demo - URL non configurato o placeholder' };
    }

    // Debug: mostra l'URL utilizzato
    console.log('🔗 URL Google Apps Script:', GOOGLE_SCRIPT_URL);    
    
    // Metodo ottimizzato: FormData con no-cors per evitare problemi CORS
    try {
        console.log('📤 Invio dati FormData a Google Apps Script...');
        
        const formData = new FormData();
        Object.keys(data).forEach(key => {
            formData.append(key, data[key]);
        });

        await fetch(GOOGLE_SCRIPT_URL, {
            method: 'POST',
            mode: 'no-cors', // Evita problemi CORS
            body: formData
        });

        // Con no-cors non possiamo leggere la risposta, ma se arriviamo qui senza errori
        // significa che la richiesta è stata inviata correttamente
        console.log('✅ Dati inviati con FormData (no-cors mode)');
        return { status: 'success', message: 'Dati inviati correttamente' };

    } catch (error) {
        console.error('❌ Errore durante l\'invio con FormData:', error);
        
        // Fallback: form submission in iframe nascosto
        console.log('🔄 Tentativo con form submission in iframe...');
        
        return new Promise((resolve, reject) => {
            try {
                // Crea un form dinamico per l'invio
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = GOOGLE_SCRIPT_URL;
                form.target = 'hiddenFrame';
                form.style.display = 'none';

                // Aggiungi i campi del form
                Object.keys(data).forEach(key => {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = key;
                    input.value = data[key];
                    form.appendChild(input);
                });

                // Crea iframe nascosto per ricevere la risposta
                const iframe = document.createElement('iframe');
                iframe.name = 'hiddenFrame';
                iframe.style.display = 'none';
                
                // Gestisci il load dell'iframe
                iframe.onload = function() {
                    setTimeout(() => {
                        document.body.removeChild(form);
                        document.body.removeChild(iframe);
                        console.log('✅ Dati inviati tramite form submission');
                        resolve({ status: 'success', message: 'Dati inviati tramite form submission' });
                    }, 1000);
                };

                // Aggiungi elementi al DOM e invia
                document.body.appendChild(iframe);
                document.body.appendChild(form);
                form.submit();

            } catch (fallbackError) {
                console.error('❌ Errore nel fallback iframe:', fallbackError);
                reject(fallbackError);
            }
        });
    }
}

// ==========================================
// GESTIONE DOWNLOAD
// ==========================================

/**
 * Avvia il download del file
 * @param {string} downloadType - Tipo di download ('installer' o 'zip')
 */
function initiateDownload(downloadType) {
    // Debug logging
    console.log('🔍 initiateDownload chiamata con:', {
        downloadType: downloadType,
        availableUrls: Object.keys(downloadUrls),
        currentDownloadType: currentDownloadType
    });
    
    const url = downloadUrls[downloadType];
    if (!url) {
        console.error('❌ URL non trovato per downloadType:', downloadType);
        console.error('📋 URLs disponibili:', downloadUrls);
        showToast('❌ Errore: URL di download non trovato');
        return;
    }

    console.log('✅ URL trovato:', url);

    // Crea un link temporaneo per il download
    const link = document.createElement('a');
    link.href = url;
    link.download = ''; // Forza il download
    link.style.display = 'none';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    showToast('🚀 Download avviato! Controlla la cartella Downloads');
}

// ==========================================
// INIZIALIZZAZIONE
// ==========================================

/**
 * Inizializza tutte le funzionalità quando il DOM è caricato
 */
function initializeApp() {    // Inizializza elementi DOM
    tabButtons = document.querySelectorAll('.tab-button');
    contentSections = document.querySelectorAll('.content-section');
    osTabButtons = document.querySelectorAll('.os-tab-button');
    osInstructions = document.querySelectorAll('.os-instructions');
    toastElement = document.getElementById('toast');
    
    // Inizializza elementi DOM per feedback modal
    feedbackModal = document.getElementById('feedbackModal');
    feedbackForm = document.getElementById('feedbackForm');
    feedbackCancelButton = document.getElementById('cancelFeedback');
    feedbackSubmitButton = document.getElementById('submitFeedback');
    starButtons = document.querySelectorAll('.star-button');
    ratingInput = document.getElementById('feedbackRating');

    // Debug: verifica che gli elementi siano trovati
    console.log('🔍 Elementi DOM trovati:', {
        tabButtons: tabButtons.length,
        contentSections: contentSections.length,
        osTabButtons: osTabButtons.length,
        toastElement: !!toastElement,
        feedbackModal: !!feedbackModal,
        starButtons: starButtons.length
    });

    setTimeout(() => {
        animateCounters();
        init3DEffects();
        initScrollAnimations();
    }, 300);    // Inizializza eventi
    initTabEvents();
    initOsTabEvents();
    initFeedbackModalEvents();

    // Crea particelle ogni tot secondi
    if (document.querySelectorAll('.particle').length < 15) {
        setInterval(createFloatingParticle, 2500);
    }
}

// ==========================================
// EXPORT GLOBALI
// ==========================================

// Esporta le funzioni per uso globale nel HTML
window.initiateDownload = initiateDownload;
window.openFeedbackModal = openFeedbackModal;
window.copyToClipboard = copyToClipboard;
window.copyToClipboardStatic = copyToClipboardStatic;
window.showToast = showToast;

// Debug: funzione di test per il download diretto
window.testDownload = function(downloadType) {
    console.log('🧪 Test download diretto per:', downloadType);
    initiateDownload(downloadType);
};

// Debug: funzione per verificare lo stato delle variabili
window.debugState = function() {
    console.log('🔍 Stato debug:', {
        currentDownloadType: currentDownloadType,
        downloadUrls: downloadUrls,
        downloadModal: !!downloadModal,
        submitButton: !!submitButton
    });
};

// Inizializza l'app quando il DOM è pronto
document.addEventListener('DOMContentLoaded', initializeApp);
