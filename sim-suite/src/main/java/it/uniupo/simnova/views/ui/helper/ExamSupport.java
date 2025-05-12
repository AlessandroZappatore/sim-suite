package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExamSupport {
    private static final Logger logger = LoggerFactory.getLogger(ExamSupport.class);

    public static VerticalLayout createExamsContent(List<EsameReferto> esami) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        // Ottieni gli esami/referti dal servizio
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
                        .set("width", "100%")
                        .set("box-sizing", "border-box"); // AGGIUNTO

                H3 examTitle = new H3(esame.getTipo());
                examTitle.getStyle()
                        .set("margin-top", "0")
                        .set("margin-bottom", "var(--lumo-space-s)")
                        .set("color", "var(--lumo-primary-text-color)");

                VerticalLayout examContent = new VerticalLayout();
                examContent.setPadding(false);
                examContent.setSpacing(true);
                examContent.setWidthFull();

                // Anteprima file multimediale
                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    examContent.add(createMediaPreview(esame.getMedia()));
                }

                // Referto testuale - ora viene aggiunto come parte dell'examCard con stile migliorato
                if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                    Div refertoContainer = new Div();
                    refertoContainer.getStyle()
                            .set("background-color", "var(--lumo-shade-5pct)")
                            .set("border-radius", "var(--lumo-border-radius-m)")
                            .set("padding", "var(--lumo-space-m)")
                            .set("margin-top", "var(--lumo-space-m)")
                            .set("border-left", "3px solid var(--lumo-primary-color)")
                            .set("width", "100%")
                            .set("box-sizing", "border-box"); // AGGIUNTO

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
                            .set("white-space", "pre-wrap");  // Mantiene la formattazione del testo

                    refertoContainer.add(refertoHeader, refertoText);
                    examContent.add(refertoContainer);
                }

                examCard.add(examTitle, examContent);
                layout.add(examCard);
            }
        } else {
            layout.add(new Paragraph("Nessun esame/referto disponibile"));
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

        // Container principale centrato
        Div previewContainer = new Div();
        previewContainer.setWidthFull(); // Questo imposta width: 100%
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
                .set("box-sizing", "border-box"); // AGGIUNTO

        // Effetto hover
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

        // Intestazione con tipo file e nome
        HorizontalLayout mediaHeader = new HorizontalLayout();
        mediaHeader.setWidthFull();
        mediaHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        mediaHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        mediaHeader.setPadding(false);
        mediaHeader.setSpacing(true);
        mediaHeader.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        // Mostra tipo file e nome file
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

        // Container per il contenuto media
        Div mediaContentContainer = new Div();
        mediaContentContainer.setWidthFull(); // Anche qui, 100%
        mediaContentContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("min-height", "200px")
                .set("max-width", "800px") // Questo potrebbe limitare il contenuto interno
                .set("margin", "0 auto")
                .set("box-sizing", "border-box"); // AGGIUNTO per coerenza, anche se qui non ha padding/border diretti.

        String mediaPath = "/" + fileName;
        logger.debug("Percorso media per anteprima: {}", mediaPath);

        Component mediaComponent;
        Icon typeIcon = getIconForFileType(fileExtension);

        // Pulsante per aprire il file a schermo intero
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

        // Generazione componente in base al tipo di file
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
                        .set("border", "none") // Nessun bordo qui, quindi box-sizing non è strettamente necessario per questo
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
                // Se videoContainer avesse padding, servirebbe box-sizing

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
                        .set("padding", "var(--lumo-space-m)") // Ha padding e width 100%
                        .set("background-color", "var(--lumo-shade-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("align-items", "center")
                        .set("box-sizing", "border-box"); // AGGIUNTO

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
                        .set("padding", "var(--lumo-space-l)") // Ha padding
                        .set("text-align", "center")
                        .set("width", "100%") // Assumiamo che debba occupare tutta la larghezza disponibile
                        .set("box-sizing", "border-box"); // AGGIUNTO

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

        // Assemblaggio dei componenti
        mediaHeader.add(new HorizontalLayout(typeIcon, fileTypeLabel, fileNameLabel));
        mediaContentContainer.add(mediaComponent);

        previewContainer.add(mediaHeader, mediaContentContainer, fullscreenButton);
        return previewContainer;
    }

    private static Component createErrorPreview(String message) {
        Div errorContainer = new Div();
        errorContainer.getStyle()
                .set("width", "100%")
                .set("padding", "var(--lumo-space-l)") // Ha padding e width 100%
                .set("text-align", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("margin", "var(--lumo-space-s) 0")
                .set("box-sizing", "border-box"); // AGGIUNTO

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

    // Metodi di supporto
    private static String getShortFileName(String fileName) {
        // Mostra solo il nome file senza il percorso, limitando la lunghezza
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