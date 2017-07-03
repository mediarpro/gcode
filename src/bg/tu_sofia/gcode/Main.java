package bg.tu_sofia.gcode;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application
{

    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        final Scene scene = new Scene(FXMLLoader.load(getClass().getResource("Home.fxml")));

        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResource("res/icon.png").toString()));
        primaryStage.setTitle("GCode v1.0");
        primaryStage.show();
    }

    public static void main(final String[] args)
    {
        Main.launch(args);
    }

}