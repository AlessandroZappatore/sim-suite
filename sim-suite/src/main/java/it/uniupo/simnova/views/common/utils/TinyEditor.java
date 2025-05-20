package it.uniupo.simnova.views.common.utils;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.tinymce.TinyMce;

/**
 * Classe di utilità per generare un editor di testo TinyMCE.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class TinyEditor extends HorizontalLayout {
    /**
     * Genera un editor di testo TinyMCE con configurazione predefinita.
     *
     * @return Un'istanza di TinyMce configurata.
     */
    public static TinyMce getEditor() {
        TinyMce editor = new TinyMce();
        editor.setWidthFull();
        editor.setHeight("450px");
        editor.configure("plugins: 'link lists table hr pagebreak image charmap preview', " + "toolbar: 'undo redo | blocks | bold italic | alignleft aligncenter alignright | bullist numlist | link image | table hr', " + "menubar: true, " + "skin: 'oxide', " + "content_css: 'default', " + "statusbar: true, " + "resize: true");
        editor.getElement().getStyle().set("border-radius", "8px").set("overflow", "hidden").set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");
        return editor;
    }
}
