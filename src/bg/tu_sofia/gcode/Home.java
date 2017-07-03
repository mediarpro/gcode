package bg.tu_sofia.gcode;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.pixelduke.javafx.ribbon.GalleryItem;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

public class Home
{

    private enum EFile {NONE, FILE1, FILE2}
    private enum ELanguage {C, CPP, JAVA}
    private enum ESwitch {ON, OFF}
    private enum EColor {GREEN, ORANGE}
    private enum EImage {PNG, AI}

    private static final String EMPTY_STRING = "";
    private static final String PATH_SEPARATOR = "\\";
    private static final JSONParser JSON_PARSER = new JSONParser();
    private static final ImageView IMG_VISUALIZATION = new ImageView();

    private static EFile eFile = EFile.NONE;
    private static ELanguage eLanguage = ELanguage.C;
    private static ESwitch eLabels = ESwitch.OFF;
    private static ESwitch eDetails = ESwitch.OFF;
    private static EColor eColor = EColor.GREEN;
    private static EImage eImage = EImage.PNG;

    private static int id_file = 0;
    private static int id_color = 0;
    private static int id_mode = 0;

    private static String lastDirPath = Home.EMPTY_STRING;
    private static String modelFilePath = Home.EMPTY_STRING;

    @FXML
    private transient ScrollPane scrVisualization;

    public Home()
    {
        // Nothing
    }

    @FXML
    private void initialize()
    {
        Home.updateVisualization();
        createZoomPane();
    }

    private void createZoomPane()
    {
        final double SCALE_DELTA = 1.1;
        final StackPane zoomPane = new StackPane();

        zoomPane.getChildren().add(Home.IMG_VISUALIZATION);

        final Group scrollContent = new Group(zoomPane);
        scrVisualization.setContent(scrollContent);

        scrVisualization.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
            {
              zoomPane.setMinSize(newValue.getWidth(), newValue.getHeight());
            }
        });

        scrVisualization.setPrefViewportWidth(256);
        scrVisualization.setPrefViewportHeight(256);

        zoomPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event)
            {
                event.consume();

                if (event.getDeltaY() == 0)
                {
                    return;
                }

                double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;

                // amount of scrolling in each direction in scrollContent coordinate
                // units
                Point2D scrollOffset = figureScrollOffset(scrollContent, scrVisualization);

                Home.IMG_VISUALIZATION.setScaleX(Home.IMG_VISUALIZATION.getScaleX() * scaleFactor);
                Home.IMG_VISUALIZATION.setScaleY(Home.IMG_VISUALIZATION.getScaleY() * scaleFactor);

                // move viewport so that old center remains in the center after the
                // scaling
                repositionScroller(scrollContent, scrVisualization, scaleFactor, scrollOffset);
            }
        });

        // Panning via drag....
        final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<Point2D>();
        scrollContent.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
            }
        });

        scrollContent.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                double deltaX = event.getX() - lastMouseCoordinates.get().getX();
                double extraWidth = scrollContent.getLayoutBounds().getWidth() - scrVisualization.getViewportBounds().getWidth();
                double deltaH = deltaX * (scrVisualization.getHmax() - scrVisualization.getHmin()) / extraWidth;
                double desiredH = scrVisualization.getHvalue() - deltaH;
                scrVisualization.setHvalue(Math.max(0, Math.min(scrVisualization.getHmax(), desiredH)));

                double deltaY = event.getY() - lastMouseCoordinates.get().getY();
                double extraHeight = scrollContent.getLayoutBounds().getHeight() - scrVisualization.getViewportBounds().getHeight();
                double deltaV = deltaY * (scrVisualization.getHmax() - scrVisualization.getHmin()) / extraHeight;
                double desiredV = scrVisualization.getVvalue() - deltaV;
                scrVisualization.setVvalue(Math.max(0, Math.min(scrVisualization.getVmax(), desiredV)));
            }
        });
    }

    private Point2D figureScrollOffset(final Node scrollContent, final ScrollPane scroller)
    {
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        double hScrollProportion = (scroller.getHvalue() - scroller.getHmin()) / (scroller.getHmax() - scroller.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double vScrollProportion = (scroller.getVvalue() - scroller.getVmin()) / (scroller.getVmax() - scroller.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
        return new Point2D(scrollXOffset, scrollYOffset);
    }

    private void repositionScroller(final Node scrollContent, final ScrollPane scroller, final double scaleFactor, final Point2D scrollOffset)
    {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();

        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0)
        {
            double halfWidth = scroller.getViewportBounds().getWidth() / 2 ;
            double newScrollXOffset = (scaleFactor - 1) *  halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        }
        else
        {
            scroller.setHvalue(scroller.getHmin());
        }

        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0)
        {
            double halfHeight = scroller.getViewportBounds().getHeight() / 2 ;
            double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        }
        else
        {
            scroller.setHvalue(scroller.getHmin());
        }
    }

    private static void setLastDirPath(final File fileLastDirPath)
    {
        Home.lastDirPath = fileLastDirPath.getAbsolutePath().replaceAll(System.lineSeparator(), Home.PATH_SEPARATOR);
        Home.lastDirPath = Home.lastDirPath.substring(0, Home.lastDirPath.lastIndexOf(Home.PATH_SEPARATOR));
    }

    private static File getLastDirPath()
    {
        return new File(Home.lastDirPath.isEmpty() ? System.getProperty("user.home") : Home.lastDirPath);
    }

    private static String readFully(final InputStream isInput) throws IOException
    {
        final StringBuilder sbInput = new StringBuilder();

        try (final BufferedReader brInput = new BufferedReader(new InputStreamReader(isInput)))
        {
            String lineInput = null;

            while (null != (lineInput = brInput.readLine()))
            {
                sbInput.append(lineInput).append(System.lineSeparator());
            }
        }

        return sbInput.toString();
    }

    private static void msgNotCompleted()
    {
        ZDialog.inf("Oops, we ran into a problem...", "Hold on there, tiger... This feature is not completed yet, but the development is going strong!");
    }

    private static void updateVisualization()
    {
        if (EFile.NONE == Home.eFile)
        {
            Home.id_file = 0;
            Home.id_color = 0;
            Home.id_mode = 0;
        }
        else
        {
            Home.id_file = (EFile.FILE1 == Home.eFile ? 1 : 2);
            Home.id_color = (EColor.GREEN == Home.eColor ? 1 : 2);

            if (ESwitch.ON == Home.eLabels)
            {
                if (ESwitch.ON == Home.eDetails)
                {
                    Home.id_mode = 4;
                }
                else
                {
                    Home.id_mode = 3;
                }
            }
            else
            {
                if (ESwitch.ON == Home.eDetails)
                {
                    Home.id_mode = 2;
                }
                else
                {
                    Home.id_mode = 1;
                }
            }
        }

        if (EFile.NONE == Home.eFile)
        {
            Home.IMG_VISUALIZATION.setImage(null);
        }
        else if (EFile.FILE1 == Home.eFile && 1 != Home.id_mode)
        {
            Home.msgNotCompleted();
            Home.IMG_VISUALIZATION.setImage(null);
        }
        else
        {
            Home.IMG_VISUALIZATION.setImage(new Image(Home.class.getResource("io/visualization_"+Home.id_file+"_"+Home.id_color+"_"+Home.id_mode+".png").toString()));
        }
    }

    @FXML
    private void newModel(final ActionEvent event)
    {
        Home.eFile = EFile.NONE;
        Home.updateVisualization();
    }

    @FXML
    private void openModel(final ActionEvent event)
    {
        try
        {
            final FileChooser fcModel = new FileChooser();
            fcModel.setTitle("Select model file");
            fcModel.setInitialDirectory(Home.getLastDirPath());
            fcModel.getExtensionFilters().add(new FileChooser.ExtensionFilter("GCODE (*.gcode)", "*.gcode"));
            final File fileModel = fcModel.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
            if (null != fileModel)
            {
                Home.setLastDirPath(fileModel);
                Home.modelFilePath = fileModel.getAbsolutePath();
                final JSONObject jsonModel = (JSONObject) Home.JSON_PARSER.parse(new FileReader(fileModel.getAbsolutePath()));
                Home.eFile = EFile.valueOf((String) jsonModel.get("MODEL"));

                Home.updateVisualization();
            }
        }
        catch (IllegalArgumentException | NullPointerException | IOException | ParseException e)
        {
            ZDialog.exc("Unable to load the model from the model file!", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveModel()
    {
        try
        {
            final JSONObject jsonModel = new JSONObject();
            jsonModel.put("MODEL", Home.eFile.toString());

            final FileWriter fw = new FileWriter(Home.modelFilePath);
            fw.write(jsonModel.toJSONString());
            fw.flush();
            fw.close();
        }
        catch (NullPointerException | IOException e)
        {
            ZDialog.exc("Unable to save the model from in model file!", e);
        }
    }

    @FXML
    private void saveModel(final ActionEvent event)
    {
        if (!Home.EMPTY_STRING.equals(Home.modelFilePath))
        {
            saveModel();
        }
        else
        {
            saveAsModel(event);
        }
    }

    @FXML
    private void saveAsModel(final ActionEvent event)
    {
        final FileChooser fcModel = new FileChooser();
        fcModel.setTitle("Select model file");
        fcModel.setInitialDirectory(Home.getLastDirPath());
        fcModel.getExtensionFilters().add(new FileChooser.ExtensionFilter("GCODE (*.gcode)", "*.gcode"));
        final File fileModel = fcModel.showSaveDialog(((Button) event.getSource()).getScene().getWindow());
        if (null != fileModel)
        {
            Home.setLastDirPath(fileModel);
            Home.modelFilePath = fileModel.getAbsolutePath();

            saveModel();
        }
    }

    private String prepareCode(final String fileContent)
    {
        return fileContent.replaceAll("[^A-Za-z0-9]", Home.EMPTY_STRING);
    }

    private boolean isCodeEqual(final String code1, final String code2)
    {
        return prepareCode(code1).equals(prepareCode(code2));
    }

    private void createModel(final String input) throws IOException
    {
        if (isCodeEqual(input, Home.readFully(Home.class.getResourceAsStream("io/input_1.txt"))))
        {
            Home.eFile = EFile.FILE1;
        }
        else if (isCodeEqual(input, Home.readFully(Home.class.getResourceAsStream("io/input_2.txt"))))
        {
            Home.eFile = EFile.FILE2;
        }
        else
        {
            Home.msgNotCompleted();
        }
    }

    @FXML
    private void createModelFromText(final ActionEvent event)
    {
        switch (Home.eLanguage)
        {
            case C:
                Home.msgNotCompleted();
                break;
            case CPP:
                Home.msgNotCompleted();
                break;
            case JAVA:
                try
                {
                    final String input = ZDialog.inputCode();

                    if (!input.isEmpty())
                    {
                        createModel(input);
                    }
                }
                catch (NullPointerException | IOException e)
                {
                    ZDialog.exc("Unable to create model from the JAVA text!", e);
                }
                break;
        }

        Home.updateVisualization();
    }

    @FXML
    private void createModelFromFile(final ActionEvent event)
    {
        switch (Home.eLanguage)
        {
            case C:
                Home.msgNotCompleted();
                break;
            case CPP:
                Home.msgNotCompleted();
                break;
            case JAVA:
                try
                {
                    final FileChooser fcInput = new FileChooser();
                    fcInput.setTitle("Select input file");
                    fcInput.setInitialDirectory(Home.getLastDirPath());
                    fcInput.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAVA (*.java)", "*.java"));
                    final File fileInput = fcInput.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
                    if (null != fileInput)
                    {
                        Home.setLastDirPath(fileInput);
                        createModel(Home.readFully(new FileInputStream(fileInput)));
                    }
                }
                catch (NullPointerException | IOException e)
                {
                    ZDialog.exc("Unable to create model from the JAVA file!", e);
                }
                break;
        }

        Home.updateVisualization();
    }

    @FXML
    private void generateModel(final ActionEvent event)
    {
        if (EFile.NONE != Home.eFile)
        {
            switch (Home.eLanguage)
            {
                case C:
                    Home.msgNotCompleted();
                    break;
                case CPP:
                    Home.msgNotCompleted();
                    break;
                case JAVA:
                    try
                    {
                        final FileChooser fcOutput = new FileChooser();
                        fcOutput.setTitle("Select output file");
                        fcOutput.setInitialDirectory(Home.getLastDirPath());
                        fcOutput.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAVA (*.java)", "*.java"));
                        final File fileOutput = fcOutput.showSaveDialog(((Button) event.getSource()).getScene().getWindow());
                        if (null != fileOutput)
                        {
                            Home.setLastDirPath(fileOutput);
                            final FileWriter fileWriter = new FileWriter(fileOutput);
                            fileWriter.write(Home.readFully(Home.class.getResourceAsStream("io/output_"+Home.id_file+".txt")));
                            fileWriter.flush();
                            fileWriter.close();
                        }
                    }
                    catch (NullPointerException | IOException e)
                    {
                        ZDialog.exc("Unable to generate JAVA file from the model!", e);
                    }
                    break;
            }
        }
        else
        {
            ZDialog.err("The model is empty!", "Please add some elements to the model first.");
        }
    }

    @FXML
    private void addElement(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void editElement(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void deleteElement(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void moveElement(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void transformModel(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void changeLabels(final MouseEvent event)
    {
        if (((ToggleButton) event.getSource()).isSelected())
        {
            Home.eLabels = ESwitch.ON;
        }
        else
        {
            Home.eLabels = ESwitch.OFF;
        }

        Home.updateVisualization();
    }

    @FXML
    private void changeDetails(final MouseEvent event)
    {
        if (((ToggleButton) event.getSource()).isSelected())
        {
            Home.eDetails = ESwitch.ON;
        }
        else
        {
            Home.eDetails = ESwitch.OFF;
        }

        Home.updateVisualization();
    }

    @FXML
    private void zoomInVisualization(final ActionEvent event)
    {
        Home.IMG_VISUALIZATION.setScaleX(Home.IMG_VISUALIZATION.getScaleX() * 1.5);
        Home.IMG_VISUALIZATION.setScaleY(Home.IMG_VISUALIZATION.getScaleY() * 1.5);
    }

    @FXML
    private void zoomOutVisualization(final ActionEvent event)
    {
        Home.IMG_VISUALIZATION.setScaleX(Home.IMG_VISUALIZATION.getScaleX() * 1 / 1.5);
        Home.IMG_VISUALIZATION.setScaleY(Home.IMG_VISUALIZATION.getScaleY() * 1 / 1.5);
    }

    @FXML
    private void zoomResetVisualization(final ActionEvent event)
    {
        Home.IMG_VISUALIZATION.setScaleX(1);
        Home.IMG_VISUALIZATION.setScaleY(1);
    }

    @FXML
    private void copyImage(final ActionEvent event)
    {
        if (EFile.NONE != Home.eFile)
        {
            if (EImage.PNG == Home.eImage)
            {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putImage(new Image(Home.class.getResource("io/visualization_"+Home.id_file+"_"+Home.id_color+"_"+Home.id_mode+".png").toString()));
                clipboard.setContent(content);
            }
            else
            {
                Home.msgNotCompleted();
            }
        }
        else
        {
            ZDialog.err("The model is empty!", "Please add some elements to the model first.");
        }
    }

    @FXML
    private void saveImage(final ActionEvent event)
    {
        if (EFile.NONE != Home.eFile)
        {
            if (EImage.PNG == Home.eImage)
            {
                try
                {
                    final FileChooser fcOutput = new FileChooser();
                    fcOutput.setTitle("Select output file");
                    fcOutput.setInitialDirectory(Home.getLastDirPath());
                    fcOutput.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));
                    final File fileOutput = fcOutput.showSaveDialog(((Button) event.getSource()).getScene().getWindow());
                    if (null != fileOutput)
                    {
                        Home.setLastDirPath(fileOutput);
                        final BufferedImage biOutput = SwingFXUtils.fromFXImage(new Image(Home.class.getResource("io/visualization_"+Home.id_file+"_"+Home.id_color+"_"+Home.id_mode+".png").toString()), null);
                        ImageIO.write(biOutput, "png", fileOutput);
                    }
                }
                catch (IOException | NullPointerException e)
                {
                    ZDialog.exc("Unable to save the visualization in the PNG file!", e);
                }
            }
            else
            {
                Home.msgNotCompleted();
            }
        }
        else
        {
            ZDialog.err("The model is empty!", "Please add some elements to the model first.");
        }
    }

    @FXML
    private void openModules(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void openSettings(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void openHelp(final ActionEvent event)
    {
        Home.msgNotCompleted();
    }

    @FXML
    private void openAbout(final ActionEvent event)
    {
        ZDialog.inf("GCode v1.0", "This is just a demo version! Some part of the functionality might still not be working.");
    }

    public static void changeModule(final GalleryItem galleryItem)
    {
        switch (galleryItem.getCategory())
        {
            case "Language modules":
                switch (galleryItem.getName())
                {
                    case "C":
                        Home.eLanguage = ELanguage.C;
                        break;
                    case "C++":
                        Home.eLanguage = ELanguage.CPP;
                        break;
                    case "Java":
                        Home.eLanguage = ELanguage.JAVA;
                        break;
                    default:
                        Home.msgNotCompleted();
                        break;
                }
                break;

            case "Transformation modules":
                // Nothing
                break;

            case "Graphical modules":
                switch (galleryItem.getName())
                {
                    case "Green-Gray":
                        Home.eColor = EColor.GREEN;
                        break;
                    case "Orange-Gray":
                        Home.eColor = EColor.ORANGE;
                        break;
                    default:
                        Home.msgNotCompleted();
                        break;
                }
                Home.updateVisualization();
                break;

            case "Image modules":
                switch (galleryItem.getName())
                {
                    case "PNG (Raster)":
                        Home.eImage = EImage.PNG;
                        break;
                    case "Adobe Illustractor (Vector)":
                        Home.eImage = EImage.AI;
                        break;
                    default:
                        Home.msgNotCompleted();
                        break;
                }
                break;

            default:
                Home.msgNotCompleted();
                break;
        }

    }

}
