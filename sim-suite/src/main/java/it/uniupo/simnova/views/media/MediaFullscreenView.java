package it.uniupo.simnova.views.media;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.*;

/**
 * Vista per la visualizzazione a schermo intero di file multimediali.
 * <p>
 * Supporta immagini (JPG, PNG, GIF), PDF, video MP4 e audio MP3.
 * Implementa l'interfaccia HasUrlParameter per ricevere il nome del file come parametro.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Route("media")
@PageTitle("Visualizzatore File")
public class MediaFullscreenView extends VerticalLayout implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MediaFullscreenView.class);
    /**
     * Nome del file da visualizzare.
     * Può contenere anche il percorso relativo.
     */
    private String filename;

    /**
     * Costruttore che configura il layout base della vista.
     */
    public MediaFullscreenView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
    }

    /**
     * Imposta il parametro ricevuto dall'URL (nome del file) e crea la vista.
     *
     * @param event    l'evento di navigazione
     * @param filename il nome del file da visualizzare (può contenere path)
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String filename) {
        this.filename = filename;
        createView();
    }

    /**
     * Crea la vista con il pulsante "Indietro" e il contenuto multimediale.
     */
    private void createView() {
        removeAll();
        // Contenuto principale
        Component mediaContent = createMediaContent();

        add(mediaContent);
        expand(mediaContent);
    }

    /**
     * Crea il componente appropriato in base all'estensione del file.
     *
     * @return Componente Vaadin adatto al tipo di file
     */
    private Component createMediaContent() {
        String fileExtension;
        // Assicurati che ci sia un punto prima di cercare l'estensione
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
            fileExtension = filename.substring(lastDotIndex + 1).toLowerCase();
        } else {
            // Gestisce il caso in cui non c'è estensione o il punto è l'ultimo carattere
            logger.warn("Impossibile determinare l'estensione del file: {}", filename);
            return new Div(new Text("Impossibile determinare il tipo di file: " + filename));
        }

        // Il percorso deve essere relativo alla radice del contesto web,
        // dato che Spring Boot serve i file da 'uploads' alla radice.
        String mediaPath = "/" + filename; // Percorso relativo alla radice
        System.out.println("Tentativo di accesso a: " + mediaPath); // Debug

        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp":
                Image image = new Image(mediaPath, "Immagine");
                image.setWidth("100%");
                image.setHeight("100%");
                image.getStyle()
                        .set("object-fit", "contain")
                        .set("max-height", "100vh");
                return image;

            case "pdf":
                IFrame pdfFrame = new IFrame(mediaPath);
                pdfFrame.setSizeFull();
                pdfFrame.getStyle().set("border", "none");
                return pdfFrame;

            case "mp4", "webm", "mov":
                NativeVideo video = new NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true);
                video.setSizeFull();
                video.getElement().setAttribute("autoplay", true); // Considera se l'autoplay è desiderato
                return video;

            case "mp3", "wav", "ogg":
                NativeAudio audio = new NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true);
                audio.setWidth("100%");
                audio.getStyle()
                        .set("max-width", "800px")
                        .set("margin", "auto")
                        .set("position", "absolute")
                        .set("top", "50%")
                        .set("left", "50%")
                        .set("transform", "translate(-50%, -50%)");
                return audio;

            default:
                // Aggiungi un logger per il formato non supportato
                logger.warn("Formato file non supportato: {}", filename);
                return new Div(new Text("Formato file non supportato: " + filename));
        }
    }

    /**
     * Componente personalizzato per la riproduzione di video HTML5.
     */
    private static class NativeVideo extends Component {
        /**
         * Costruttore che crea un elemento video HTML.
         */
        public NativeVideo() {
            super(new Element("video"));
        }

        /**
         * Imposta il percorso del file video.
         *
         * @param src percorso del file video
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Abilita o disabilita i controlli di riproduzione.
         *
         * @param controls true per mostrare i controlli
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta le dimensioni al 100% del contenitore.
         */
        public void setSizeFull() {
            getElement().setAttribute("width", "100%");
            getElement().setAttribute("height", "100%");
        }
    }

    /**
     * Componente personalizzato per la riproduzione di audio HTML5.
     */
    private static class NativeAudio extends Component {
        /**
         * Costruttore che crea un elemento audio HTML.
         */
        public NativeAudio() {
            super(new Element("audio"));
        }

        /**
         * Imposta il percorso del file audio.
         *
         * @param src percorso del file audio
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Abilita o disabilita i controlli di riproduzione.
         *
         * @param controls true per mostrare i controlli
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta la larghezza del componente.
         *
         * @param width larghezza (es. "100%" o "300px")
         */
        public void setWidth(String width) {
            getElement().setAttribute("width", width);
        }
    }
}