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

@Route("media")
@PageTitle("Visualizzatore File")
public class MediaFullscreenView extends VerticalLayout implements HasUrlParameter<String> {

    private String filename;

    public MediaFullscreenView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String filename) {
        this.filename = filename;
        createView();
    }

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

    // Componente video nativo
    private static class NativeVideo extends Component {
        public NativeVideo() {
            super(new Element("video"));
        }

        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        public void setSizeFull() {
            getElement().setAttribute("width", "100%");
            getElement().setAttribute("height", "100%");
        }
    }

    // Componente audio nativo
    private static class NativeAudio extends Component {
        public NativeAudio() {
            super(new Element("audio"));
        }

        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        public void setWidth(String width) {
            getElement().setAttribute("width", width);
        }
    }
}