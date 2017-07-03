package bg.tu_sofia.gcode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Utility class for handling various dialogs
 */
public final class ZDialog
{

    /**
     * Constructor, Protect instantiation
     */
    private ZDialog()
    {
    // Nothing
    }

    /**
     * Shows error dialog
     *
     * @param message
     *            Text to be displayed
     */
    public static void err(final String message)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    /**
     * Shows error dialog
     *
     * @param message
     *            Text to be displayed
     * @param details
     *            Additional information
     */
    public static void err(final String message, final String details)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Shows warning dialog
     *
     * @param message
     *            Text to be displayed
     */
    public static void wrn(final String message)
    {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    /**
     * Shows warning dialog
     *
     * @param message
     *            Text to be displayed
     * @param details
     *            Additional information
     */
    public static void wrn(final String message, final String details)
    {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Shows information dialog
     *
     * @param message
     *            Text to be displayed
     */
    public static void inf(final String message)
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    /**
     * Shows information dialog
     *
     * @param message
     *            Text to be displayed
     * @param details
     *            Additional information
     */
    public static void inf(final String message, final String details)
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Shows ok/cancel dialog
     *
     * @param title
     *            Title of the dialog
     * @param question
     *            Question to be asked
     */
    public static ButtonType showOkCancel(final String title, final String question)
    {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation required");
        alert.setHeaderText(title);
        alert.setContentText(question);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }

    /**
     * Shows yes/no dialog
     *
     * @param title
     *            Title of the dialog
     * @param question
     *            Question to be asked
     */
    public static ButtonType showYesNo(final String title, final String question)
    {
        Alert alert = new Alert(AlertType.CONFIRMATION, question, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation required");
        alert.setHeaderText(title);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }

    /**
     * Shows yes/no/cancel dialog
     *
     * @param title
     *            Title of the dialog
     * @param question
     *            Question to be asked
     */
    public static ButtonType showYesNoCancel(final String title, final String question)
    {
        Alert alert = new Alert(AlertType.CONFIRMATION, question, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("Confirmation required");
        alert.setHeaderText(title);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }

    /**
     *
     * @param message
     *            Text to be displayed
     * @param ex
     *            Exception to dump
     */
    public static void exc(final String message, final Exception ex)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception");
        alert.setHeaderText(message);
        alert.setContentText(ex.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Shows input dialog
     *
     * @param title
     *            Title of the dialog
     * @param prompt
     *            User prompt
     * @param value
     *            Initial value and response
     * @return Text that was entered
     */
    public static String input(final String title, final String prompt, final String value)
    {
        String resultValue = "";

        TextInputDialog dialog = new TextInputDialog(value);
        dialog.setTitle("Input required");
        dialog.setHeaderText(title);
        dialog.setContentText(prompt);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
        {
            resultValue = result.get();
        }

        return resultValue.trim();
    }

    public static String inputCode()
    {
        String resultValue = "";

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("AAA");
        alert.setHeaderText("BBB");
        alert.setContentText("CCC");

        TextArea textArea = new TextArea();

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent())
        {
            resultValue = textArea.getText();
        }

        return resultValue;
    }

}
