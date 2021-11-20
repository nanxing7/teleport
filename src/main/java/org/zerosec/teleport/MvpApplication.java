package org.zerosec.teleport;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.zerosec.teleport.portscan.StateEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-10-25 10:29
 */
@SpringBootApplication
public class MvpApplication extends Application {
    private ConfigurableApplicationContext applicationContext;
    private ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * 状态 （0-停止扫描；1-正在扫描；2-暂停扫描；）
     */
    private final IntegerProperty stateProperty = new SimpleIntegerProperty(0);
    private final StringProperty descProperty = new SimpleStringProperty();

    /**
     * 图标流 应该在程序退出的时候关闭
     */
    private InputStream iconStream;

    public static void main(String[] args) {
        launch(args);
    }

    private MenuBar createMenubar() {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("帮助");
        MenuItem about = new MenuItem("关于");
        menu.getItems().addAll(about);
        menuBar.getMenus().addAll(menu);
        menuBar.setStyle("-fx-background-color: #FFFFFF;");
        return menuBar;
    }

    private Parent createContent() {
        VBox root = new VBox();

        MenuBar menuBar = createMenubar();

        root.setStyle("-fx-background-color: #FFFFFF");

        HBox btnBox = new HBox();

        TextField input = new TextField();
        Text output = new Text();

        Button btn1 = new Button("开始扫描");
        Button btn2 = new Button("暂停扫描");
        btn2.disableProperty().setValue(true);// 初始化默认禁用

        output.textProperty().bind(descProperty);

        stateProperty.addListener((observableValue, oldValue, newValue) -> {
            descProperty.setValue("旧值（" + oldValue + "）新值（" + newValue + "）");

            if (oldValue.equals(StateEnum.STOP.getCode())
                    && newValue.equals(StateEnum.SCAN.getCode())) {

                btn1.textProperty().setValue("停止扫描");
                btn2.disableProperty().setValue(false);

            } else if (oldValue.equals(StateEnum.SCAN.getCode())
                    && newValue.equals(StateEnum.STOP.getCode())) {
                btn1.textProperty().setValue("开始扫描");
                btn2.textProperty().setValue("暂停扫描");

                btn2.disableProperty().setValue(true);

            } else if (oldValue.equals(StateEnum.SCAN.getCode())
                    && newValue.equals(StateEnum.SUSPEND.getCode())) {

                btn2.textProperty().setValue("恢复扫描");

            } else if (oldValue.equals(StateEnum.SUSPEND.getCode())
                    && newValue.equals(StateEnum.STOP.getCode())) {
                btn1.textProperty().setValue("开始扫描");
                btn2.textProperty().setValue("暂停扫描");

                btn2.disableProperty().setValue(true);

            } else if (oldValue.equals(StateEnum.SUSPEND.getCode())
                    && newValue.equals(StateEnum.SCAN.getCode())) {
                btn2.textProperty().setValue("暂停扫描");
            }
        });

        btn1.setOnMouseClicked(event -> {
            if (stateProperty.getValue().equals(StateEnum.STOP.getCode())) {
                stateProperty.setValue(StateEnum.SCAN.getCode());
            } else {
                stateProperty.setValue(StateEnum.STOP.getCode());
            }
        });

        btn2.setOnMouseClicked(event -> {
            if (stateProperty.getValue().equals(StateEnum.STOP.getCode())
                    || stateProperty.getValue().equals(StateEnum.SUSPEND.getCode())) {
                stateProperty.setValue(StateEnum.SCAN.getCode());
            } else {
                stateProperty.setValue(StateEnum.SUSPEND.getCode());
            }
        });

        btnBox.getChildren().addAll(btn1, btn2);
        root.getChildren().addAll(menuBar, btnBox);
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
