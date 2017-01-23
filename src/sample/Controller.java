package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button encodeButton;
    @FXML
    public Button decodeButton;
    @FXML
    public Label fileNameLabel;
    @FXML
    public Button chooseFileButton;
    @FXML
    public javafx.scene.layout.AnchorPane AnchorPane;
    @FXML
    public Label checkFileStateLabel;

    private Desktop desktop = Desktop.getDesktop();
    private RandomAccessFile stream; // поток чтения запси
    private FileChooser chooser = new FileChooser();
    private File myFile; // загружаемый файл
    private boolean isFileEncoded;
    private boolean isFileDecoded;

    private void setFile(RandomAccessFile stream) {
        this.stream = stream;
    }

    public void onClickAdd(ActionEvent actionEvent) {
        isFileDecoded = false;
        isFileEncoded = false;
        progressBar.setProgress(0);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".TXT", "*.txt"));
        if (myFile != null)
            chooser.setInitialDirectory(myFile.getParentFile());
            myFile = chooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
            if (myFile != null) {
                try {
                    setFile(new RandomAccessFile(myFile, "rw"));
                    fileNameLabel.setText(myFile.getName());
                } catch (FileNotFoundException e) {
                    fileNameLabel.setText("Ошибка загрузки файла");
                }
                encodeButton.setDisable(false);
                decodeButton.setDisable(false);
                 new FileStateCheckThread().start();
            }



    }

    private class FileStateCheckThread extends Thread{
        int read;

        @Override
        public void run() {
            Platform.runLater(()->checkFileStateLabel.setText("Файл проверяется..."));
            chooseFileButton.setDisable(true);
            decodeButton.setDisable(true);
            encodeButton.setDisable(true);
            try {
                while ((read = stream.read())!=-1){
                    if(!((read == 32)||(read == 48)||(read==49))){
                        isFileDecoded = true;
                        break;
                    }
                }
                if(!isFileDecoded) {
                    isFileEncoded = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            Platform.runLater(Controller.this::setButtonsState);
            chooseFileButton.setDisable(false);
        }
    }

    private class EncodeThread extends Thread {
        @Override
        public void run() {
            if (myFile != null) {
                encodeButton.setDisable(true);
                decodeButton.setDisable(true);
                chooseFileButton.setDisable(true);


                int counter = 0;
                int read;
                try {
                    stream = new RandomAccessFile(myFile, "rw");
                    int streamLength = (int) stream.length();
                    double progressStep = (streamLength * 0.01);

                    double j = 0.005 / progressStep;
                    String[] binary = new String[streamLength];
                    while ((read = stream.read()) != -1) {
                        progressBar.setProgress(j += (0.005 / progressStep));
                        binary[counter++] = Integer.toBinaryString(read);
                    }
                    streamLength = binary.length;
                    progressStep = (int) (streamLength * 0.01);


                    stream.setLength(0);
                    for (String s : binary) {
                        progressBar.setProgress(j += (0.005 / progressStep));
                        stream.seek(stream.length());
                        stream.writeBytes(s + " ");
                    }
                    stream.close();
                    isFileEncoded = true;
                    isFileDecoded = false;
                    if (myFile.exists())
                        desktop.open(myFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Platform.runLater(Controller.this::setButtonsState);
                chooseFileButton.setDisable(false);

            }
        }
    }

    private class DecodeThread extends Thread {
        @Override
        public void run() {
            if (myFile != null) {
                encodeButton.setDisable(true);
                decodeButton.setDisable(true);
                chooseFileButton.setDisable(true);

                ArrayList<String> list = new ArrayList<>();

                try {
                    stream = new RandomAccessFile(myFile, "rw");

                    int streamLength = (int) stream.length();
                    double progressStep = streamLength * 0.01;
                    double j = 0.005 / progressStep;


                    StringBuilder string = new StringBuilder();
                    for (int i = 0; i < streamLength; i++) {

                        int read = stream.read();
                        if (Character.isSpaceChar(read)) {
                            list.add(string.toString());
                            string = new StringBuilder();
                        } else {
                            string.append((char) read);
                        }
                        progressBar.setProgress(j += (0.005 / progressStep));
                    }

                    streamLength = list.size();
                    progressStep = (int) (streamLength * 0.01);

                    stream.setLength(0);
                    int cursorPos = 0;
                    for (String str : list) {
                        stream.seek(cursorPos++);
                        stream.write(Integer.parseInt(str, 2));
                        progressBar.setProgress(j += (0.005 / progressStep));
                    }
                    stream.close();
                    chooseFileButton.setDisable(false);
                    isFileDecoded = true;
                    isFileEncoded = false;
                    if (myFile.exists())
                        desktop.open(myFile);


                } catch (IOException e) {
                    e.printStackTrace();
                }

               Platform.runLater(Controller.this::setButtonsState);
                chooseFileButton.setDisable(false);

            }
        }
    }

    public void onEncode(ActionEvent actionEvent) throws IOException, InterruptedException {
        EncodeThread encodeThread = new EncodeThread();
        progressBar.setProgress(0);
        encodeThread.start();
    }

    public void onDecode(ActionEvent actionEvent) throws IOException, InterruptedException {
        DecodeThread decodeThread = new DecodeThread();
        progressBar.setProgress(0);
        decodeThread.start();
    }

    //инициализация обработчиков drag&drop для файла
    public void initHandlers() {

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
                isFileDecoded = false;
                isFileEncoded = false;
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


                }
                event.setDropCompleted(success);
                event.consume();
                new FileStateCheckThread().start();

                progressBar.setProgress(0);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        encodeButton.setDisable(true);
        decodeButton.setDisable(true);
    }

    public void setButtonsState(){
        if(isFileDecoded){
            encodeButton.setDisable(false);
            decodeButton.setDisable(true);
            checkFileStateLabel.setText("Файл не закодирован");
        }
        if(isFileEncoded){
            encodeButton.setDisable(true);
            decodeButton.setDisable(false);
            checkFileStateLabel.setText("Файл закодирован");
        }
    }
}

