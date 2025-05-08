package it.uniupo.simnova.views.support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class ValidationError {

    /**
     * Mostra un errore di validazione per un campo specifico.
     *
     * @param field   Il componente che ha fallito la validazione
     * @param message Il messaggio di errore da mostrare
     */
    public static void showValidationError(Component field, String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);

        if (field instanceof HasValidation) {
            ((HasValidation) field).setInvalid(true);
            ((HasValidation) field).setErrorMessage(message);
        }

        if (field instanceof Focusable) {
            ((Focusable<?>) field).focus();
        }
    }

    /**
     * Mostra un errore di validazione per un campo e restituisce false.
     * Utile per concatenare nelle condizioni di validazione.
     *
     * @param field   Il componente che ha fallito la validazione
     * @param message Il messaggio di errore da mostrare
     * @return sempre false, per facilitare l'uso in metodi di validazione
     */
    public static boolean showErrorAndReturnFalse(Component field, String message) {
        showValidationError(field, message);
        return false;
    }
}