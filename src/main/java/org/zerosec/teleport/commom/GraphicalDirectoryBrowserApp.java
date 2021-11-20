package org.zerosec.teleport.commom;

import com.google.common.util.concurrent.RateLimiter;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
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
public class GraphicalDirectoryBrowserApp extends Application {

    private HBox hBox = new HBox(15);

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    private Parent createContent() {
        ScrollPane scrollPane = new ScrollPane(hBox);

        StackPane root = new StackPane(scrollPane);
        root.setPrefSize(800, 600);

        setContents(Paths.get("./"));

        return root;
    }

    private void setContents(Path dir) {
        hBox.getChildren().clear();

        try {
            Files.walk(dir, 1)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(dir))
                    .forEach(contentDir -> {
                        DirectoryView view = new DirectoryView(
                                contentDir.getFileName().toString(),
                                contentDir
                        );

                        view.setOnMouseClicked(e -> {
                            setContents(view.directory);
                        });

                        hBox.getChildren().add(view);
                    });
        } catch (IOException e) {
            System.out.println("Can't walk dir: " + dir);
        }
    }

    private static class DirectoryView extends VBox {
        private Path directory;

        public DirectoryView(String name, Path directory) {
            setSpacing(5);
            setAlignment(Pos.TOP_CENTER);

            this.directory = directory;

            Text text = new Text(name);
            text.setFont(Font.font(24));

            Rectangle rect = new Rectangle(75, 50, Color.LIGHTYELLOW);
            rect.setStroke(Color.BLACK);

            getChildren().addAll(rect, text);
        }
    }

}
