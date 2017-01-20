package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        Controller controller = loader.getController();
        primaryStage.setTitle("Encoder 1.0");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
        controller.initHandlers();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
