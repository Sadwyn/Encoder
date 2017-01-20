package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class Controller {

    @FXML
    public ProgressBar progressBar;
    private Desktop desktop = Desktop.getDesktop();
    @FXML
    public Label fileNameLabel;
    private RandomAccessFile stream; // поток чтения запси
    private FileChooser chooser = new FileChooser();
    File myFile; // загружаемый файл

    void setFile(RandomAccessFile stream) {
        this.stream = stream;
    }

    public void onClickAdd(ActionEvent actionEvent) {

        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".TXT", "*.txt"));
        if (myFile != null)
            chooser.setInitialDirectory(myFile.getParentFile());
        myFile = chooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
        try {
            setFile(new RandomAccessFile(myFile, "rw"));
            fileNameLabel.setText(myFile.getName());
        } catch (FileNotFoundException e) {
            fileNameLabel.setText("Ошибка загрузки файла");
        }
    }


    public void onEncode(ActionEvent actionEvent) throws IOException {
        if (myFile != null) {
            Runtime.getRuntime().exec("taskkill /IM notepad.exe");
            int counter = 0;
            int read;
            stream = new RandomAccessFile(myFile, "rw");
            String[] binary = new String[(int) stream.length()];
            while ((read = stream.read()) != -1) {
                binary[counter++] = Integer.toBinaryString(read);
            }

            stream.setLength(0);
            for (String s : binary) {
                stream.seek(stream.length());
                stream.writeBytes(s + " ");
            }
            stream.close();
            if (myFile.exists())
                desktop.open(myFile);
        }
    }

    public void onDecode(ActionEvent actionEvent) throws IOException {
        Runtime.getRuntime().exec("taskkill /IM notepad.exe");
        Pattern p = Pattern.compile(" ");
        if (myFile != null) {

            ArrayList<String> list = new ArrayList<>();
            stream = new RandomAccessFile(myFile, "rw");
            String line;
            while ((line = stream.readLine()) != null) {
                String[] strings = p.split(line);
                Collections.addAll(list, strings);
            }
            stream.setLength(0);
            int cursorPos = 0;
            for (String str : list) {
                stream.seek(cursorPos++);
                stream.write(Integer.parseInt(str, 2));
            }
            stream.close();
            if (myFile.exists())
                desktop.open(myFile);
        }
    }

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
                }
                event.setDropCompleted(success);
                event.consume();

            }
        });
    }
}
