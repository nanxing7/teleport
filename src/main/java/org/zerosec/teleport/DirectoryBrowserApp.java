package org.zerosec.teleport;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-17 17:12
 */
public class DirectoryBrowserApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    private Parent createContent() {
        ListView<String> listView = new ListView<>();

        Button btn = new Button("Browse");
        btn.setOnAction(e->{

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
            File file = chooser.showDialog(null);

            if (file != null) {
                Path startDir = file.toPath();
                try {
                    listView.getItems().clear();

                    Files.walk(startDir)
                            .filter(path -> Files.isDirectory(path))
                            .forEach(dir -> listView.getItems().add(dir.toString()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }


        });

        VBox root = new VBox(btn, listView);
        root.setPrefSize(800, 600);

        return root;
    }


}
