package it.uniupo.simnova.views.support;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.tinymce.TinyMce;

public class TinyEditor extends HorizontalLayout {

    public TinyEditor(){

    }

    public static TinyMce getEditor(){
        TinyMce editor = new TinyMce();
        editor.setWidthFull();
        editor.setHeight("450px");
        editor.configure("plugins: 'link lists table hr pagebreak image charmap preview', " +
                "toolbar: 'undo redo | blocks | bold italic | alignleft aligncenter alignright | bullist numlist | link image | table hr', " +
                "menubar: true, " +
                "skin: 'oxide', " +
                "content_css: 'default', " +
                "statusbar: true, " +
                "resize: true");
        editor.getElement().getStyle()
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");
        return editor;
    }
}
