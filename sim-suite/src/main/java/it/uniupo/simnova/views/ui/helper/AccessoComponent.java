package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.List;
import java.util.Optional;

public class AccessoComponent extends HorizontalLayout {
    /**
     * Campo di selezione per il tipo di accesso (venoso o arterioso).
     */
    private final Select<String> tipoSelect;
    /**
     * Campo di testo per la posizione dell'accesso.
     */
    private final TextField posizioneField;
    /**
     * Oggetto Accesso associato a questo componente.
     */
    private final Select<String> latoSelect;

    private final Select<Integer> misuraSelect;

    private final Accesso accesso;

    /**
     * Costruttore del componente di accesso.
     *
     * @param tipo il tipo di accesso (venoso o arterioso)
     */
    public AccessoComponent(String tipo, boolean hasDelete) {
        setAlignItems(Alignment.BASELINE);
        setSpacing(true);
        getStyle().set("flex-wrap", "wrap");

        this.accesso = new Accesso(0, "", "", "", 0);

        tipoSelect = FieldGenerator.createSelect(
                "Tipo accesso " + tipo,
                List.of("Periferico", "Centrale", "CVC a breve termine", "CVC tunnellizzato",
                        "PICC", "Midline", "Intraosseo", "PORT", "Dialysis catheter", "Altro"),
                null,
                true
        );
        if (tipo.equals("Arterioso")) {
            tipoSelect.setItems(
                    "Radiale", "Femorale", "Omerale", "Brachiale", "Ascellare", "Pedidia", "Altro"
            );
        }
        tipoSelect.setWidth(null);
        tipoSelect.getStyle()
                .set("flex-basis", "220px")
                .set("min-width", "180px");

        tipoSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                accesso.setTipologia(e.getValue());
            }
        });

        posizioneField = FieldGenerator.createTextField(
                "Posizione",
                "(es. avambraccio, collo, torace)",
                true
        );
        posizioneField.setWidth(null);
        posizioneField.getStyle()
                .set("flex-basis", "200px")
                .set("min-width", "180px");
        posizioneField.addValueChangeListener(e -> accesso.setPosizione(e.getValue()));

        latoSelect = FieldGenerator.createSelect(
                "Lato",
                List.of("DX", "SX"),
                null,
                true
        );
        latoSelect.setWidth(null);
        latoSelect.getStyle()
                .set("flex-basis", "100px")
                .set("min-width", "90px");
        latoSelect.addValueChangeListener(e -> accesso.setLato(e.getValue()));

        misuraSelect = FieldGenerator.createSelect(
                "Misura (Gauge)",
                List.of(14, 16, 18, 20, 22, 24, 26),
                null,
                true
        );
        misuraSelect.setWidth(null);
        misuraSelect.getStyle()
                .set("flex-basis", "140px")
                .set("min-width", "120px");
        misuraSelect.addValueChangeListener(e -> accesso.setMisura(e.getValue()));

        Button removeButton = StyleApp.getButton(
                "Rimuovi",
                VaadinIcon.TRASH,
                ButtonVariant.LUMO_ERROR,
                "var(--lumo-error-color)"
        );
        removeButton.getStyle()
                .set("flex-grow", "0")
                .set("flex-shrink", "0")
                .set("align-self", "flex-end");
        removeButton.addClickListener(e -> removeSelf());

        add(tipoSelect, posizioneField, latoSelect, misuraSelect);
        if (hasDelete) add(removeButton);
    }

    /**
     * Rimuove il componente di accesso dalla vista e dalla lista appropriata.
     */
    private void removeSelf() {
        Optional<Component> parentOpt = getParent();
        parentOpt.ifPresent(parent -> {
            if (parent instanceof VerticalLayout container) {
                container.remove(this);
            }
        });

    }

    /**
     * Restituisce l'oggetto Accesso associato a questo componente.
     *
     * @return l'oggetto Accesso con i dati correnti
     */
    public Accesso getAccesso() {
        accesso.setTipologia(tipoSelect.getValue());
        accesso.setPosizione(posizioneField.getValue());
        accesso.setLato(latoSelect.getValue());
        accesso.setMisura(misuraSelect.getValue());
        return accesso;
    }

}
