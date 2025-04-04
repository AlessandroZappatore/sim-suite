package it.uniupo.simnova.views.media;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

        // Pulsante per tornare indietro
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());
        backButton.getStyle()
                .set("position", "absolute")
                .set("top", "1rem")
                .set("left", "1rem")
                .set("z-index", "1000");

        // Contenuto principale
        Component mediaContent = createMediaContent();

        add(backButton, mediaContent);
        expand(mediaContent);
    }

    /**
     * Crea il componente appropriato in base all'estensione del file.
     *
     * @return Componente Vaadin adatto al tipo di file
     */
    private Component createMediaContent() {
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        String mediaPath = "META-INF/resources/Media/" + filename;
        System.out.println("Trying to access: " + mediaPath); // Debug
        switch (fileExtension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                Image image = new Image("META-INF/resources/Media/" + filename, "Immagine");
                image.setWidth("100%");
                image.setHeight("100%");
                image.getStyle()
                        .set("object-fit", "contain")
                        .set("max-height", "100vh");
                return image;

            case "pdf":
                IFrame pdfFrame = new IFrame("META-INF/resources/Media/" + filename);
                pdfFrame.setSizeFull();
                pdfFrame.getStyle().set("border", "none");
                return pdfFrame;

            case "mp4":
                NativeVideo video = new NativeVideo();
                video.setSrc("META-INF/resources/Media/" + filename);
                video.setControls(true);
                video.setSizeFull();
                video.getElement().setAttribute("autoplay", true);
                return video;

            case "mp3":
                NativeAudio audio = new NativeAudio();
                audio.setSrc("META-INF/resources/Media/" + filename);
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
                return new Div(new Text("Formato file non supportato: " + filename));
        }
    }

    /**
     * Componente personalizzato per la riproduzione di video HTML5.
     */
    private static class NativeVideo extends Component {
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