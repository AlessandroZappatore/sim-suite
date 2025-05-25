package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExamSupport {
    private static final Logger logger = LoggerFactory.getLogger(ExamSupport.class);

    public static VerticalLayout createExamsContent(EsameRefertoService esameRefertoService, Integer scenarioId){
        List<EsameReferto> esami = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        if (esami != null && !esami.isEmpty()) {
            for (EsameReferto esame : esami) {
                Div examCard = new Div();
                examCard.addClassName("exam-card");
                examCard.getStyle()
                        .set("background-color", "var(--lumo-base-color)")
                        .set("border-radius", "var(--lumo-border-radius-l)")
                        .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                        .set("padding", "var(--lumo-space-m)")
                        .set("margin-bottom", "var(--lumo-space-m)")
                        .set("width", "95%")
                        .set("box-sizing", "border-box")
                        .set("margin-left", "auto")
                        .set("margin-right", "auto");

                HorizontalLayout cardHeader = new HorizontalLayout();
                cardHeader.setWidthFull();
                cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
                cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                H3 examTitle = new H3(esame.getTipo());
                examTitle.getStyle()
                        .set("margin-top", "0")
                        .set("margin-bottom", "0")
                        .set("color", "var(--lumo-primary-text-color)");

                Button deleteButton = new Button(VaadinIcon.TRASH.create());
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteButton.setTooltipText("Elimina Esame " + esame.getTipo());
                deleteButton.getElement().setAttribute("aria-label", "Elimina Esame " + esame.getTipo());
                deleteButton.addClickListener(e -> {
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.setCloseOnEsc(true);
                    confirmDialog.setCloseOnOutsideClick(true);

                    confirmDialog.add(new H4("Conferma Eliminazione"));
                    confirmDialog.add(new Paragraph("Sei sicuro di voler eliminare l'esame/referto '" + esame.getTipo() + "'? Questa operazione non può essere annullata."));

                    Button confirmDeleteButton = new Button("Elimina", VaadinIcon.TRASH.create());
                    confirmDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                    confirmDeleteButton.addClickListener(confirmEvent -> {
                        esameRefertoService.deleteEsameReferto(esame.getIdEsame(), scenarioId);
                        Notification.show("Esame '" + esame.getTipo() + "' eliminato con successo.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        confirmDialog.close();
                        layout.remove(examCard);
                    });

                    Button cancelDeleteButton = new Button("Annulla");
                    cancelDeleteButton.addClickListener(cancelEvent -> confirmDialog.close());

                    HorizontalLayout dialogButtons = new HorizontalLayout(confirmDeleteButton, cancelDeleteButton);
                    dialogButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                    dialogButtons.setWidthFull();
                    confirmDialog.add(dialogButtons);
                    confirmDialog.open();
                });

                cardHeader.add(examTitle, deleteButton);
                examCard.add(cardHeader);

                VerticalLayout examContent = new VerticalLayout();
                examContent.setPadding(false);
                examContent.setSpacing(true);
                examContent.setWidthFull();

                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    examContent.add(createMediaPreview(esame.getMedia()));
                }


                if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                    Div refertoContainer = new Div();
                    refertoContainer.getStyle()
                            .set("background-color", "var(--lumo-shade-5pct)")
                            .set("border-radius", "var(--lumo-border-radius-m)")
                            .set("padding", "var(--lumo-space-m)")
                            .set("margin-top", "var(--lumo-space-m)")
                            .set("border-left", "3px solid var(--lumo-primary-color)")
                            .set("width", "90%")
                            .set("box-sizing", "border-box")
                            .set("margin-left", "auto")
                            .set("margin-right", "auto");

                    HorizontalLayout refertoHeader = new HorizontalLayout();
                    refertoHeader.setPadding(false);
                    refertoHeader.setSpacing(true);
                    refertoHeader.setAlignItems(FlexComponent.Alignment.CENTER);

                    Icon refertoIcon = new Icon(VaadinIcon.FILE_TEXT);
                    refertoIcon.getStyle().set("color", "var(--lumo-primary-color)");

                    H4 refertoTitle = new H4("Referto");
                    refertoTitle.getStyle()
                            .set("margin", "0")
                            .set("color", "var(--lumo-primary-color)");

                    refertoHeader.add(refertoIcon, refertoTitle);

                    Paragraph refertoText = new Paragraph(esame.getRefertoTestuale());
                    refertoText.getStyle()
                            .set("margin", "var(--lumo-space-s) 0 0 0")
                            .set("color", "var(--lumo-body-text-color)")
                            .set("white-space", "pre-wrap")
                            .set("box-sizing", "border-box");

                    refertoContainer.add(refertoHeader, refertoText);
                    examContent.add(refertoContainer);

                    examContent.setAlignItems(FlexComponent.Alignment.CENTER);
                }

                examCard.add(examContent);
                layout.add(examCard);
            }
        } else {
            Div errorDiv = EmptySupport.createErrorContent("Nessun esame disponibile");
            layout.add(errorDiv);
        }
        return layout;
    }

    /**
     * Crea un'anteprima per i file multimediali.
     *
     * @param fileName nome del file
     * @return il componente di anteprima
     */
    private static Component createMediaPreview(String fileName) {
        String fileExtension;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            logger.warn("Impossibile determinare l'estensione del file per l'anteprima: {}", fileName);
            return createErrorPreview("Tipo file non riconosciuto: " + fileName);
        }

        logger.debug("Creazione anteprima media per file: {}, estensione: {}", fileName, fileExtension);

        Div previewContainer = new Div();
        previewContainer.setWidthFull();
        previewContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin", "var(--lumo-space-s) 0")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
                .set("box-sizing", "border-box");

        previewContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        " this.style.transform = 'translateY(-2px)'; " +
                        " this.style.boxShadow = '0 6px 15px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        " this.style.transform = 'translateY(0)'; " +
                        " this.style.boxShadow = '0 3px 10px rgba(0, 0, 0, 0.08)'; " +
                        "});"
        );

        HorizontalLayout mediaHeader = new HorizontalLayout();
        mediaHeader.setWidthFull();
        mediaHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        mediaHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        mediaHeader.setPadding(false);
        mediaHeader.setSpacing(true);
        mediaHeader.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Span fileTypeLabel = new Span(fileExtension.toUpperCase());
        fileTypeLabel.getStyle()
                .set("background-color", getColorForFileType(fileExtension))
                .set("color", "white")
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("text-transform", "uppercase");

        Span fileNameLabel = new Span(getShortFileName(fileName));
        fileNameLabel.getStyle()
                .set("margin-left", "var(--lumo-space-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("max-width", "200px");

        Div mediaContentContainer = new Div();
        mediaContentContainer.setWidthFull();
        mediaContentContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("min-height", "200px")
                .set("max-width", "800px")
                .set("margin", "0 auto")
                .set("box-sizing", "border-box");

        String mediaPath = "/" + fileName;
        logger.debug("Percorso media per anteprima: {}", mediaPath);

        Component mediaComponent;
        Icon typeIcon = getIconForFileType(fileExtension);

        Button fullscreenButton = new Button("Apri in una nuova pagina", new Icon(VaadinIcon.EXPAND_FULL));
        fullscreenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fullscreenButton.getStyle()
                .set("border-radius", "30px")
                .set("margin-top", "var(--lumo-space-m)")
                .set("transition", "transform 0.2s ease")
                .set("cursor", "pointer");

        fullscreenButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.transform = 'scale(1.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.transform = 'scale(1)'; " +
                        "});"
        );

        fullscreenButton.addClassName("hover-effect");
        fullscreenButton.addClickListener(e -> openFullMedia(fileName));

        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp":
                Image image = new Image(mediaPath, fileName);
                image.setMaxWidth("100%");
                image.setHeight("auto");
                image.getStyle()
                        .set("max-height", "320px")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("object-fit", "contain")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
                mediaComponent = image;
                break;

            case "pdf":
                IFrame pdfPreview = new IFrame();
                pdfPreview.setSrc(mediaPath);
                pdfPreview.setWidth("100%");
                pdfPreview.setHeight("500px");
                pdfPreview.getStyle()
                        .set("border", "none")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
                mediaComponent = pdfPreview;
                break;

            case "mp4", "webm", "mov":
                Div videoContainer = new Div();
                videoContainer.getStyle()
                        .set("width", "100%")
                        .set("position", "relative")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("overflow", "hidden")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

                ExamSupport.NativeVideo video = new ExamSupport.NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true);
                video.setWidth("100%");
                video.getStyle().set("display", "block");

                videoContainer.add(video);
                mediaComponent = videoContainer;
                break;

            case "mp3", "wav", "ogg":
                Div audioContainer = new Div();
                audioContainer.getStyle()
                        .set("width", "100%")
                        .set("padding", "var(--lumo-space-m)")
                        .set("background-color", "var(--lumo-shade-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("align-items", "center")
                        .set("box-sizing", "border-box");

                Icon musicIcon = new Icon(VaadinIcon.MUSIC);
                musicIcon.setSize("3em");
                musicIcon.getStyle()
                        .set("color", "var(--lumo-primary-color)")
                        .set("margin-bottom", "var(--lumo-space-s)");

                ExamSupport.NativeAudio audio = new ExamSupport.NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true);
                audio.setWidth("100%");

                audioContainer.add(musicIcon, audio);
                mediaComponent = audioContainer;
                break;

            default:
                Div unknownContainer = new Div();
                unknownContainer.getStyle()
                        .set("padding", "var(--lumo-space-l)")
                        .set("text-align", "center")
                        .set("width", "100%")
                        .set("box-sizing", "border-box");

                Icon fileIcon = new Icon(VaadinIcon.FILE_O);
                fileIcon.setSize("4em");
                fileIcon.getStyle()
                        .set("color", "var(--lumo-contrast-50pct)")
                        .set("margin-bottom", "var(--lumo-space-m)");

                Span message = new Span("Anteprima non disponibile per: " + fileExtension.toUpperCase());
                message.getStyle()
                        .set("display", "block")
                        .set("color", "var(--lumo-secondary-text-color)");

                unknownContainer.add(fileIcon, message);
                mediaComponent = unknownContainer;
                break;
        }

        HorizontalLayout fileInfoLayout = new HorizontalLayout(typeIcon, fileTypeLabel, fileNameLabel);
        fileInfoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        fileInfoLayout.setSpacing(true);

        mediaHeader.removeAll();
        mediaHeader.add(fileInfoLayout);

        mediaContentContainer.add(mediaComponent);

        previewContainer.add(mediaHeader, mediaContentContainer, fullscreenButton);
        return previewContainer;
    }

    private static Component createErrorPreview(String message) {
        Div errorContainer = new Div();
        errorContainer.getStyle()
                .set("width", "100%")
                .set("padding", "var(--lumo-space-l)")
                .set("text-align", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("margin", "var(--lumo-space-s) 0")
                .set("box-sizing", "border-box");

        Icon errorIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
        errorIcon.setSize("3em");
        errorIcon.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Paragraph errorMessage = new Paragraph(message);
        errorMessage.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin", "0");
        errorContainer.add(errorIcon, errorMessage);
        return errorContainer;
    }

    private static String getShortFileName(String fileName) {
        String shortName = fileName;
        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash > -1 && lastSlash < fileName.length() - 1) {
            shortName = fileName.substring(lastSlash + 1);
        }
        return shortName.length() > 30 ? shortName.substring(0, 27) + "..." : shortName;
    }

    private static Icon getIconForFileType(String fileExtension) {
        return switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> {
                Icon icon = new Icon(VaadinIcon.PICTURE);
                icon.getStyle().set("color", "var(--lumo-primary-color)");
                yield icon;
            }
            case "pdf" -> {
                Icon icon = new Icon(VaadinIcon.FILE_TEXT);
                icon.getStyle().set("color", "var(--lumo-error-color)");
                yield icon;
            }
            case "mp4", "webm", "mov" -> {
                Icon icon = new Icon(VaadinIcon.FILM);
                icon.getStyle().set("color", "var(--lumo-success-color)");
                yield icon;
            }
            case "mp3", "wav", "ogg" -> {
                Icon icon = new Icon(VaadinIcon.MUSIC);
                icon.getStyle().set("color", "var(--lumo-warning-color)");
                yield icon;
            }
            default -> {
                Icon icon = new Icon(VaadinIcon.FILE_O);
                icon.getStyle().set("color", "var(--lumo-contrast-50pct)");
                yield icon;
            }
        };
    }

    private static String getColorForFileType(String fileExtension) {
        return switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "var(--lumo-primary-color)";
            case "pdf" -> "var(--lumo-error-color)";
            case "mp4", "webm", "mov" -> "var(--lumo-success-color)";
            case "mp3", "wav", "ogg" -> "var(--lumo-warning-color)";
            default -> "var(--lumo-contrast-50pct)";
        };
    }

    /**
     * Apre il file multimediale completo in una nuova scheda.
     *
     * @param fileName nome del file
     */
    private static void openFullMedia(String fileName) {
        logger.debug("Opening full media for file: {}", fileName);
        UI.getCurrent().getPage().open("media/" + fileName, "_blank");
    }

    /**
     * Componente per la riproduzione di video nativo
     */
    private static class NativeVideo extends Component {
        /**
         * Costruttore per il video nativo.
         */
        public NativeVideo() {
            super(new Element("video"));
        }

        /**
         * Imposta l'attributo src del video.
         *
         * @param src il percorso del video
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Imposta l'attributo controls del video.
         *
         * @param controls true per mostrare i controlli, false altrimenti
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta la larghezza del video.
         *
         * @param width la larghezza del video
         */
        public void setWidth(String width) {
            getElement().setAttribute("width", width);
        }
    }

    /**
     * Componente per la riproduzione di audio nativo
     */
    private static class NativeAudio extends Component {
        /**
         * Costruttore per l'audio nativo.
         */
        public NativeAudio() {
            super(new Element("audio"));
        }

        /**
         * Imposta l'attributo src dell'audio.
         *
         * @param src il percorso dell'audio
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Imposta l'attributo controls dell'audio.
         *
         * @param controls true per mostrare i controlli, false altrimenti
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta la larghezza dell'audio.
         *
         * @param width la larghezza dell'audio
         */
        public void setWidth(String width) {
            getElement().getStyle().set("width", width);
        }
    }
}