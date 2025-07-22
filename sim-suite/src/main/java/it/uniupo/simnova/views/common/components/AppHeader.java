package it.uniupo.simnova.views.common.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.themeselect.ThemeSelect;

import java.io.InputStream;

/**
 * Componente per l'header dell'applicazione.
 * Contiene il logo dell'applicazione, il titolo, un'area per il logo del centro (con upload/gestione)
 * e un pulsante per cambiare il tema (modalità scura/chiara).
 *
 * @author Alessandro Zappatore
 * @version 1.6
 */
public class AppHeader extends HorizontalLayout {
    private static final Logger logger = LoggerFactory.getLogger(AppHeader.class);
    private static final String CENTER_LOGO_FILENAME = "center_logo.png";
    private static final String LOGO_URL = "icons/icon.png";

    private final FileStorageService fileStorageService;
    private final Div centerLogoContainer;

    public AppHeader(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

        // Stili di base dell'header
        addClassName(LumoUtility.Padding.SMALL);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName("card-style");

        // Logo SIM SUITE (a sinistra)
        Image simSuiteLogo = new Image(LOGO_URL, "SIM SUITE Logo");
        simSuiteLogo.setHeight("40px");
        simSuiteLogo.getStyle().set("cursor", "pointer");
        simSuiteLogo.addClickListener(e -> UI.getCurrent().navigate(""));
        Tooltip.forComponent(simSuiteLogo)
                .withText("Torna alla Home")
                .withPosition(Tooltip.TooltipPosition.BOTTOM);

        // Titolo dell'applicazione
        Div appTitle = new Div();
        appTitle.setText("SIM SUITE");
        appTitle.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.PRIMARY
        );

        // Contenitore per il logo del centro
        centerLogoContainer = new Div();
        centerLogoContainer.getStyle()
                .set("margin-left", LumoUtility.Margin.MEDIUM)
                .set("display", "flex")
                .set("align-items", "center");
        updateCenterLogoArea();

        showMissingLogoPopoverIfNeeded();

        // Sezione sinistra dell'header
        HorizontalLayout leftSection = new HorizontalLayout(simSuiteLogo, appTitle, centerLogoContainer);
        leftSection.setSpacing(true);
        leftSection.setAlignItems(Alignment.CENTER);

        ThemeSelect minimalSelect = new ThemeSelect("Choose theme");
        minimalSelect.addClassNames("minimal");
        add(leftSection, minimalSelect);
    }

    private void updateCenterLogoArea() {
        centerLogoContainer.removeAll(); // Pulisce il contenitore prima di aggiornarlo

        if (fileStorageService.fileExists(CENTER_LOGO_FILENAME)) {
            // Se il logo esiste, visualizza l'immagine e un pulsante per eliminarla
            String logoUrl = "/" + CENTER_LOGO_FILENAME; // URL relativo per accedere al file statico
            Image centerLogo = new Image(logoUrl, "Logo Centro");
            centerLogo.setHeight("40px");

            Button deleteButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            deleteButton.getStyle().set("margin-left", LumoUtility.Margin.XSMALL);
            Tooltip.forComponent(deleteButton).withText("Elimina Logo Centro");

            deleteButton.addClickListener(e -> showDeleteConfirmation()); // Mostra conferma prima di eliminare

            centerLogoContainer.add(centerLogo, deleteButton);

        } else {
            // Se il logo non esiste, mostra un componente di upload
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
            upload.setMaxFiles(1);

            Button uploadButton = new Button("Carica Logo Centro");
            upload.setUploadButton(uploadButton);
            upload.setDropLabel(new Div(new Text("o trascina qui"))); // Testo per il drag-and-drop

            // Stili per il componente upload
            upload.getStyle()
                    .set("min-width", "180px")
                    .set("height", "40px")
                    .set("display", "flex")
                    .set("align-items", "center");
            uploadButton.getStyle().set("height", "40px"); // Allinea il pulsante di upload

            upload.addSucceededListener(event -> {
                try (InputStream inputStream = buffer.getInputStream()) {
                    fileStorageService.store(inputStream, CENTER_LOGO_FILENAME); // Salva il file caricato
                    Notification.show("Logo caricato con successo!", 2000, Notification.Position.MIDDLE);
                    UI.getCurrent().access(this::updateCenterLogoArea); // Aggiorna l'UI dopo il caricamento
                } catch (Exception ex) {
                    logger.error("Errore durante il salvataggio del logo caricato.", ex);
                    Notification.show("Errore caricamento logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            upload.addFileRejectedListener(event -> Notification.show("File rifiutato: " + event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR));

            centerLogoContainer.add(upload);
        }
    }

    private void showDeleteConfirmation() {
        Button confirmButton = new Button("Elimina", e -> deleteCenterLogo());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annulla");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        Notification notification = new Notification();
        Paragraph question = new Paragraph("Sei sicuro di voler eliminare il logo del centro?");
        notification.add(question, buttons);
        notification.setDuration(0); // Notifica persistente
        notification.setPosition(Notification.Position.MIDDLE);

        cancelButton.addClickListener(e -> notification.close());
        confirmButton.addClickListener(e -> notification.close()); // Chiude la notifica dopo la conferma

        notification.open();
    }

    private void deleteCenterLogo() {
        try {
            fileStorageService.deleteFile(CENTER_LOGO_FILENAME);
            Notification.show("Logo eliminato con successo.", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().access(this::updateCenterLogoArea); // Aggiorna l'UI dopo l'eliminazione
        } catch (Exception ex) {
            logger.error("Errore durante l'eliminazione del logo del centro.", ex);
            Notification.show("Errore eliminazione logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showMissingLogoPopoverIfNeeded() {
        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            String path = currentUrl.getPath();
            // Il popover viene mostrato solo sulla homepage ("" o "/") e se il logo non esiste
            if ((path.isEmpty() || "/".equals(path)) && !fileStorageService.fileExists(CENTER_LOGO_FILENAME)) {
                UI.getCurrent().access(() -> {
                    Popover popover = new Popover();
                    popover.setOpened(true); // Apri il popover automaticamente
                    Paragraph message = new Paragraph("⚠️ Carica il logo del centro per averlo nei PDF.");
                    Button closeBtn = new Button("Chiudi", e -> popover.setOpened(false));
                    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                    HorizontalLayout layout = new HorizontalLayout(message, closeBtn);
                    layout.setAlignItems(Alignment.CENTER);

                    popover.add(layout);
                    popover.setTarget(centerLogoContainer); // Collega il popover al contenitore del logo
                    popover.open();
                });
            }
        });
    }
}