package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.scenario.operations.ScenarioImportService;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.uniupo.simnova.views.creation.ScenariosListView.detached;

public class DialogSupport {

    public static boolean showJsonUploadDialog(ExecutorService executorService, ScenarioImportService scenarioImportService) {
        AtomicBoolean success = new AtomicBoolean(false);
        if (detached.get()) {
            return success.get();
        }

        Dialog uploadDialog = new Dialog();
        uploadDialog.setCloseOnEsc(true);
        uploadDialog.setCloseOnOutsideClick(true);
        uploadDialog.setWidth("550px");
        uploadDialog.setHeaderTitle("Carica Nuovo Scenario");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false); // Il padding è gestito dal Dialog
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH); // Stretch per l'upload

        H4 title = new H4("Carica un file JSON per creare un nuovo scenario");
        title.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph("Seleziona un file JSON (.json) da caricare. Il sistema proverà a creare un nuovo scenario basato sul contenuto del file.");
        description.addClassName(LumoUtility.FontSize.SMALL);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".json");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Trascina qui il file JSON o clicca per cercare"));
        upload.setWidthFull();

        dialogLayout.add(title, description, upload);
        uploadDialog.add(dialogLayout);

        Button cancelButton = new Button("Annulla", e -> uploadDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        uploadDialog.getFooter().add(cancelButton); // Aggiungi pulsanti al footer del Dialog

        uploadDialog.open();

        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] jsonBytes = inputStream.readAllBytes();
                uploadDialog.close();

                UI ui = UI.getCurrent();
                if (ui == null || detached.get()) return;

                Notification loadingNotification = new Notification("Creazione scenario in corso...", 0, Notification.Position.MIDDLE);
                loadingNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY); // Stile moderno
                loadingNotification.open();

                executorService.submit(() -> {
                    try {
                        boolean created = scenarioImportService.createScenarioByJSON(jsonBytes);
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                if (created) {
                                    Notification.show("Scenario creato con successo!", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                    success.set(true);
                                } else {
                                    Notification.show("Errore durante la creazione dello scenario.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                Notification.show("Errore creazione: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            });
                        }
                    }
                });
            } catch (IOException ex) {
                Notification.show("Errore lettura file: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                uploadDialog.close();
            }
        });
        upload.addFailedListener(event -> Notification.show("Caricamento fallito: " + event.getReason().getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
        return success.get();
    }
}
