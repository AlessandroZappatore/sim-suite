package it.uniupo.simnova.views.common.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
// Rimosso import ConfirmDialog
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph; // Aggiunto import Paragraph
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant; // Aggiunto import NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.popover.Popover; // Aggiunto import Popover
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Componente per l'header dell'applicazione.
 * <p>
 * Contiene il logo SIM SUITE, il titolo, il logo del centro (o un uploader se non presente)
 * e un pulsante per cambiare il tema.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.5
 */
public class AppHeader extends HorizontalLayout {

    private static final Logger logger = LoggerFactory.getLogger(AppHeader.class);
    private static final String CENTER_LOGO_FILENAME = "center_logo.png"; // Nome file standard per il logo del centro
    private static final String LOGO_URL = "icons/icon.png";
    private final FileStorageService fileStorageService;
    private final Div centerLogoContainer; // Contenitore dinamico per logo o uploader
    private final Button toggleThemeButton;
    private boolean isDarkMode = false;

    /**
     * Costruttore che inizializza l'header.
     * Richiede FileStorageService per gestire il logo del centro.
     *
     * @param fileStorageService Servizio per l'archiviazione dei file.
     */
    public AppHeader(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

        // Configurazione layout
        addClassName(LumoUtility.Padding.SMALL);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        getStyle().set("background", "var(--lumo-primary-color-10pct")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0,0,0,0.1)")
                .set("padding", "10px"); // Aggiunto padding per il layout

        Image simSuiteLogo = new Image(LOGO_URL, "SIM SUITE Logo");
        simSuiteLogo.setHeight("40px");
        simSuiteLogo.getStyle().set("cursor", "pointer");
        simSuiteLogo.addClickListener(e -> UI.getCurrent().navigate(""));
        Tooltip.forComponent(simSuiteLogo)
                .withText("Torna alla Home")
                .withPosition(Tooltip.TooltipPosition.BOTTOM);

        Div appTitle = new Div();
        appTitle.setText("SIM SUITE");
        appTitle.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.PRIMARY
        );

        centerLogoContainer = new Div();
        centerLogoContainer.getStyle()
                .set("margin-left", LumoUtility.Margin.MEDIUM)
                .set("display", "flex") // Usa flex per allineare logo e pulsante
                .set("align-items", "center");
        updateCenterLogoArea();

        // Mostra popover se il logo centro è assente
        showMissingLogoPopoverIfNeeded();

        // Layout per loghi e titolo
        HorizontalLayout leftSection = new HorizontalLayout(simSuiteLogo, appTitle, centerLogoContainer);
        leftSection.setSpacing(true);
        leftSection.setAlignItems(Alignment.CENTER);

        toggleThemeButton = new Button();
        toggleThemeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleThemeButton.setAriaLabel("Toggle dark mode");
        toggleThemeButton.getStyle()
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)")
                .set("border-radius", "50%");



        checkInitialTheme(); // Sets initial icon and isDarkMode state
        toggleThemeButton.addClickListener(e -> toggleTheme());

        // Aggiungi le sezioni all'header
        add(leftSection, toggleThemeButton);
    }

    /**
     * Aggiorna l'area dedicata al logo del centro.
     * Mostra l'immagine e un pulsante di eliminazione se il file esiste,
     * altrimenti mostra l'uploader.
     */
    private void updateCenterLogoArea() {
        centerLogoContainer.removeAll(); // Pulisce il contenuto precedente

        if (fileStorageService.fileExists(CENTER_LOGO_FILENAME)) {
            // Il logo esiste, mostra l'immagine e il pulsante elimina
            // Costruisce l'URL corretto per l'immagine servita dal controller
            String logoUrl = "/" + CENTER_LOGO_FILENAME; // Assumes a servlet/controller serves this path
            Image centerLogo = new Image(logoUrl, "Logo Centro");
            centerLogo.setHeight("40px");

            Button deleteButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            deleteButton.setAriaLabel("Elimina logo centro");
            deleteButton.getStyle().set("margin-left", LumoUtility.Margin.XSMALL); // Spazio tra logo e pulsante
            Tooltip.forComponent(deleteButton).withText("Elimina Logo Centro");

            deleteButton.addClickListener(e -> showDeleteConfirmation());

            centerLogoContainer.add(centerLogo, deleteButton); // Aggiunge logo e pulsante

        } else {
            // Il logo non esiste, mostra l'uploader
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
            upload.setMaxFiles(1);
            // upload.setMaxFileSize(1024 * 1024); // Esempio: 1MB

            // Personalizza pulsante e label
            Button uploadButton = new Button("Carica Logo Centro");
            upload.setUploadButton(uploadButton);
            upload.setDropLabel(new Div(new Text("o trascina qui"))); // Using Div for better control if needed

            // Stile compatto per l'uploader
            upload.getStyle()
                    .set("min-width", "180px") // Larghezza minima per visibilità
                    .set("height", "40px")
                    .set("display", "flex")
                    .set("align-items", "center");
            uploadButton.getStyle().set("height", "40px"); // Allinea altezza pulsante


            upload.addSucceededListener(event -> {
                try (InputStream inputStream = buffer.getInputStream()) {
                    fileStorageService.store(inputStream, CENTER_LOGO_FILENAME);
                    Notification.show("Logo caricato con successo!", 2000, Notification.Position.MIDDLE);
                    // Aggiorna l'area nell'UI thread
                    UI.getCurrent().access(this::updateCenterLogoArea);
                } catch (Exception ex) {
                    logger.error("Errore durante il salvataggio del logo caricato.", ex);
                    Notification.show("Errore caricamento logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR); // Aggiunto tema errore
                }
            });

            upload.addFileRejectedListener(event -> Notification.show("File rifiutato: " + event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)); // Aggiunto tema errore

            centerLogoContainer.add(upload);
        }
    }

    /**
     * Mostra una notifica per confermare l'eliminazione del logo.
     */
    private void showDeleteConfirmation() {
        // Crea i pulsanti
        Button confirmButton = new Button("Elimina", e -> {
            deleteCenterLogo(); // Chiama il metodo di eliminazione
            // La notifica si chiuderà automaticamente dal listener del pulsante
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Stile rosso per conferma

        Button cancelButton = new Button("Annulla");

        // Layout per i pulsanti
        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        // Crea la notifica
        Notification notification = new Notification();
        Paragraph question = new Paragraph("Sei sicuro di voler eliminare il logo del centro?");
        notification.add(question, buttons); // Aggiunge testo e pulsanti alla notifica
        notification.setDuration(0); // Rende la notifica persistente finché non si clicca un pulsante
        notification.setPosition(Notification.Position.MIDDLE); // Posiziona al centro

        // Aggiunge listener per chiudere la notifica quando si clicca un pulsante
        cancelButton.addClickListener(e -> notification.close());
        confirmButton.addClickListener(e -> notification.close()); // Chiude anche su conferma

        notification.open();
    }

    /**
     * Elimina il file del logo del centro e aggiorna l'interfaccia.
     */
    private void deleteCenterLogo() {
        try {
            fileStorageService.deleteFile(CENTER_LOGO_FILENAME);
            Notification.show("Logo eliminato con successo.", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().access(this::updateCenterLogoArea);
        } catch (Exception ex) {
            logger.error("Errore durante l'eliminazione del logo del centro.", ex);
            Notification.show("Errore eliminazione logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Mostra un popover se il logo del centro è assente e ci troviamo nella home page.
     */
    private void showMissingLogoPopoverIfNeeded() {
        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            String path = currentUrl.getPath();
            if ((path.isEmpty() || "/".equals(path)) && !fileStorageService.fileExists(CENTER_LOGO_FILENAME)) {
                UI.getCurrent().access(() -> {
                    Popover popover = new Popover();
                    popover.setOpened(true);
                    Paragraph message = new Paragraph("⚠️ Carica il logo del centro per averlo nei PDF.");
                    Button closeBtn = new Button("Chiudi", e -> popover.setOpened(false));
                    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                    HorizontalLayout layout = new HorizontalLayout(message, closeBtn);
                    layout.setAlignItems(Alignment.CENTER);

                    popover.add(layout);
                    popover.setTarget(centerLogoContainer);
                    popover.open();
                });
            }
        });
    }

    /**
     * Verifica il tema iniziale dell'applicazione leggendo l'attributo 'theme' da documentElement.
     * Aggiorna lo stato interno e l'icona del pulsante di conseguenza.
     */
    private void checkInitialTheme() {
        UI.getCurrent().getPage().executeJs(
                "return document.documentElement.getAttribute('theme') || 'light';"
        ).then(String.class, theme -> {
            isDarkMode = "dark".equals(theme);
            updateThemeIcon();
        });
    }

    /**
     * Cambia il tema tra light e dark mode.
     * Aggiorna l'attributo 'theme' su documentElement e la proprietà 'color-scheme'.
     * Infine, aggiorna l'icona del pulsante.
     */
    private void toggleTheme() {
        isDarkMode = !isDarkMode; // Toggle the state

        String themeToSet = isDarkMode ? "dark" : "light";

        UI.getCurrent().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0)",
                themeToSet
        );

        UI.getCurrent().getPage().executeJs(
                "document.documentElement.style.setProperty('color-scheme', $0)",
                themeToSet
        );

        updateThemeIcon();
    }

    /**
     * Aggiorna l'icona del pulsante in base al tema corrente (isDarkMode).
     * Se è dark mode, mostra l'icona del sole (per passare a light).
     * Se è light mode, mostra l'icona della luna (per passare a dark).
     */
    private void updateThemeIcon() {
        Icon icon;
        if (isDarkMode) {
            icon = VaadinIcon.SUN_O.create();
            icon.setColor("var(--lumo-warning-color)");
        } else {
            icon = VaadinIcon.MOON_O.create();
            icon.setColor("var(--lumo-contrast)");
        }

        icon.getStyle()
                .set("width", "var(--lumo-icon-size-m)")
                .set("height", "var(--lumo-icon-size-m)");

        toggleThemeButton.setIcon(icon);
    }
}
