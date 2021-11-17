package org.zerosec.teleport;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-10-25 10:29
 */
@SpringBootApplication
public class MvpApplication extends Application {
    private ConfigurableApplicationContext applicationContext;

    /**
     * 图标流 应该在程序退出的时候关闭
     */
    private InputStream iconStream;

    public static void main(String[] args) {
        launch(args);
    }

    private Parent createContent() {
        VBox root = new VBox();
        root.setPrefSize(1280, 720);

        TextField input = new TextField();
        Text output = new Text();

        Button button = new Button();
        root.getChildren().addAll(input, output, button);

        output.textProperty().bind(input.textProperty());

        return root;
    }

    @Override
    public void start(Stage primaryStage) {
        iconStream = ClassLoader.getSystemResourceAsStream("icon.png");
        Image icon = new Image(iconStream);

        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Teleport - by ZeroSec Team");
        primaryStage.setMaxWidth(1280);
        primaryStage.setMaxHeight(720);
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    @Override
    public void init() {
        // 在这里初始化启动 Spring
        applicationContext = SpringApplication.run(MvpApplication.class);
    }

    @Override
    public void stop() throws IOException {
        SpringApplication.exit(applicationContext);
        Platform.exit();
        iconStream.close();
    }
}
