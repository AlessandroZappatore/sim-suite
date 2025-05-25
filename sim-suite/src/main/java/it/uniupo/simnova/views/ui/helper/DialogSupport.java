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

public class DialogSupport {
    public static void showZipUploadDialog(AtomicBoolean detached, ExecutorService executorService, ScenarioImportService scenarioImportService, Runnable onSuccess) {
        if (detached.get()) {
            return;
        }

        Dialog uploadDialog = new Dialog();
        uploadDialog.setCloseOnEsc(true);
        uploadDialog.setCloseOnOutsideClick(true);
        uploadDialog.setWidth("550px");
        uploadDialog.setHeaderTitle("Importa Nuovo Scenario da ZIP");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        H4 title = new H4("Carica un file ZIP per importare un nuovo scenario");
        title.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph(
                "Seleziona un file ZIP (.zip) da caricare. " +
                        "Il file ZIP deve contenere un file 'scenario.json' alla radice " +
                        "e una cartella 'esami/' con eventuali file multimediali associati.");
        description.addClassName(LumoUtility.FontSize.SMALL);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".zip");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Trascina qui il file ZIP o clicca per cercare"));
        upload.setWidthFull();

        dialogLayout.add(title, description, upload);
        uploadDialog.add(dialogLayout);

        Button cancelButton = new Button("Annulla", e -> uploadDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        uploadDialog.getFooter().add(cancelButton);

        uploadDialog.open();

        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] zipBytes = inputStream.readAllBytes();
                String fileName = event.getFileName();
                uploadDialog.close();

                UI ui = UI.getCurrent();
                if (ui == null || detached.get()) return;

                Notification loadingNotification = new Notification("Importazione scenario in corso...", 0, Notification.Position.MIDDLE);
                loadingNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                loadingNotification.open();

                executorService.submit(() -> {
                    try {
                        boolean imported = scenarioImportService.importScenarioFromZip(zipBytes, fileName);
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                if (imported) {
                                    Notification.show("Scenario importato con successo!", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                    if (onSuccess != null) onSuccess.run();
                                } else {
                                    Notification.show("Errore durante l'importazione dello scenario.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                Notification.show("Errore importazione: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
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
    }
}
