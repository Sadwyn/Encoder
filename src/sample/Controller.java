package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Controller implements Initializable{

    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button encodeButton;
    @FXML
    public Button decodeButton;
    @FXML
    public Label fileNameLabel;
    @FXML
    public Label resultLabel;

    private Desktop desktop = Desktop.getDesktop();
    private RandomAccessFile stream; // поток чтения запси
    private FileChooser chooser = new FileChooser();
    private File myFile; // загружаемый файл
    private void setFile(RandomAccessFile stream) {
        this.stream = stream;
    }

    public void onClickAdd(ActionEvent actionEvent) {

        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".TXT", "*.txt"));
        if (myFile != null)
            chooser.setInitialDirectory(myFile.getParentFile());
        myFile = chooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
        if(myFile!=null) {
            try {
                setFile(new RandomAccessFile(myFile, "rw"));
                fileNameLabel.setText(myFile.getName());
            } catch (FileNotFoundException e) {
                fileNameLabel.setText("Ошибка загрузки файла");
            }
            encodeButton.setDisable(false);
            decodeButton.setDisable(false);
        }
        resultLabel.setText("");
    }


    public void onEncode(ActionEvent actionEvent) throws IOException {
        if (myFile != null) {
            resultLabel.setText("");
            progressBar.setProgress(0);
            int counter = 0;
            int read;
            stream = new RandomAccessFile(myFile, "rw");
            int streamLength = (int) stream.length();
            int progressStep = (int) ((streamLength / 100) * 0.1);
            String[] binary = new String[streamLength];
            while ((read = stream.read()) != -1) {
                progressBar.setProgress(progressStep+=progressStep);
                binary[counter++] = Integer.toBinaryString(read);
            }
            progressBar.setProgress(0.50);
            stream.setLength(0);
            for (String s : binary) {
                stream.seek(stream.length());
                stream.writeBytes(s + " ");
            }
            stream.close();
            progressBar.setProgress(1);
            if (myFile.exists())
                desktop.open(myFile);
            resultLabel.setText("Успешно");
        }
    }

    public void onDecode(ActionEvent actionEvent) throws IOException {

        Pattern p = Pattern.compile(" ");
        if (myFile != null) {
            progressBar.setProgress(0);
            resultLabel.setText("");
            ArrayList<String> list = new ArrayList<>();
            stream = new RandomAccessFile(myFile, "rw");
            String line;
            while ((line = stream.readLine()) != null) {
                String[] strings = p.split(line);
                Collections.addAll(list, strings);
            }
            progressBar.setProgress(0.50);
            stream.setLength(0);
            int cursorPos = 0;
            for (String str : list) {
                stream.seek(cursorPos++);
                stream.write(Integer.parseInt(str, 2));
            }
            stream.close();
            progressBar.setProgress(1);
            if (myFile.exists())
                desktop.open(myFile);
            resultLabel.setText("Успешно");
        }
    }

    //инициализация обработчиков drag&drop для файла
    public void initHandlers() {

        fileNameLabel.setOnDragDone(fileNameLabel.getScene().getOnDragDone());
        fileNameLabel.getScene().setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles())
                    event.acceptTransferModes(TransferMode.COPY);
                else event.consume();
            }
        });
        fileNameLabel.getScene().setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {

                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    File file = db.getFiles().get(0);
                    myFile = file;
                    fileNameLabel.setText(file.getName());
                    try {
                        setFile(new RandomAccessFile(myFile, "rw"));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    encodeButton.setDisable(false);
                    decodeButton.setDisable(false);
                }
                event.setDropCompleted(success);
                event.consume();
                resultLabel.setText("");
                progressBar.setProgress(0);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        encodeButton.setDisable(true);
        decodeButton.setDisable(true);
    }
}
