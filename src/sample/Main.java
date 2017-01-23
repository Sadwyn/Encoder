package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);

        BackgroundImage image = new BackgroundImage(new Image(this.getClass().getResource("/background.jpg").toExternalForm()), BackgroundRepeat.REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Controller controller = loader.getController();
        controller.AnchorPane.setBackground(new Background(image));
        primaryStage.setTitle("Encoder 1.0");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        controller.initHandlers();
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
