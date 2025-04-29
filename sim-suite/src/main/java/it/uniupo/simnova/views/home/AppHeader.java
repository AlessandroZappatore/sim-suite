package it.uniupo.simnova.views.home;

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
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.FileStorageService;
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
 * @version 1.4
 */
public class AppHeader extends HorizontalLayout {

    private static final Logger logger = LoggerFactory.getLogger(AppHeader.class);
    private static final String CENTER_LOGO_FILENAME = "center_logo.png"; // Nome file standard per il logo del centro

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
        getStyle().set("background", "var(--lumo-primary-color-10pct)");

        // --- Sezione Sinistra: Loghi e Titolo ---

        // Logo SIM SUITE
        Image simSuiteLogo = new Image("icons/LogoSimsuiteNoSlogan.png", "SIM SUITE Logo");
        simSuiteLogo.setHeight("40px");
        simSuiteLogo.getStyle().set("cursor", "pointer");
        simSuiteLogo.addClickListener(e -> UI.getCurrent().navigate(""));
        Tooltip.forComponent(simSuiteLogo)
                .withText("Torna alla Home")
                .withPosition(Tooltip.TooltipPosition.BOTTOM);

        // Titolo applicazione
        Div appTitle = new Div();
        appTitle.setText("SIM SUITE");
        appTitle.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.PRIMARY
        );

        // Container per il logo del centro (o l'uploader)
        centerLogoContainer = new Div();
        centerLogoContainer.getStyle()
                .set("margin-left", LumoUtility.Margin.MEDIUM)
                .set("display", "flex") // Usa flex per allineare logo e pulsante
                .set("align-items", "center"); // Allinea verticalmente
        updateCenterLogoArea(); // Popola inizialmente con logo o uploader

        // Layout per loghi e titolo
        HorizontalLayout leftSection = new HorizontalLayout(simSuiteLogo, appTitle, centerLogoContainer);
        leftSection.setSpacing(true);
        leftSection.setAlignItems(Alignment.CENTER);


        // --- Sezione Destra: Pulsante Tema ---

        toggleThemeButton = new Button();
        toggleThemeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleThemeButton.setAriaLabel("Toggle dark mode");
        checkInitialTheme();
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
            String logoUrl = "/" + CENTER_LOGO_FILENAME;
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
            upload.setDropLabel(new Div(new Text("o trascina qui")));

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
        notification.add(new Paragraph("Sei sicuro di voler eliminare il logo del centro?"));
        notification.add(buttons); // Aggiunge i pulsanti alla notifica
        notification.setDuration(0); // Rende la notifica persistente finché non si clicca un pulsante
        notification.setPosition(Notification.Position.MIDDLE); // Posiziona al centro

        // Aggiunge listener per chiudere la notifica quando si clicca un pulsante
        cancelButton.addClickListener(e -> notification.close());
        confirmButton.addClickListener(e -> notification.close()); // Chiude anche su conferma

        notification.open(); // Mostra la notifica
    }


    /**
     * Elimina il file del logo del centro e aggiorna l'interfaccia.
     */
    private void deleteCenterLogo() {
        try {
            fileStorageService.deleteFile(CENTER_LOGO_FILENAME); // Assicurati che il metodo si chiami 'delete' o adattalo
            Notification.show("Logo eliminato con successo.", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS); // Usa tema successo
            // Aggiorna l'area nell'UI thread
            UI.getCurrent().access(this::updateCenterLogoArea);
        } catch (Exception ex) {
            logger.error("Errore durante l'eliminazione del logo del centro.", ex);
            Notification.show("Errore eliminazione logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR); // Usa tema errore
        }
    }


    /**
     * Verifica il tema iniziale dell'applicazione.
     */
    private void checkInitialTheme() {
        UI.getCurrent().getPage().executeJs(
                "return document.documentElement.getAttribute('theme') || 'light';"
        ).then(result -> {
            String theme = result.asString();
            isDarkMode = "dark".equals(theme);
            updateThemeIcon();
        });
    }

    /**
     * Cambia il tema tra light e dark mode.
     */
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0)",
                isDarkMode ? "dark" : "light"
        );
        updateThemeIcon();

        UI.getCurrent().getPage().executeJs(
                "document.documentElement.style.setProperty('color-scheme', $0)",
                isDarkMode ? "dark" : "light"
        );
    }

    /**
     * Aggiorna l'icona del pulsante in base al tema corrente.
     */
    private void updateThemeIcon() {
        Icon icon = isDarkMode ?
                VaadinIcon.SUN_O.create() :
                VaadinIcon.MOON_O.create();

        icon.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("width", "var(--lumo-icon-size-m)")
                .set("height", "var(--lumo-icon-size-m)");

        toggleThemeButton.setIcon(icon);
    }
}