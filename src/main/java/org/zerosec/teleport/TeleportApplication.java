package org.zerosec.teleport;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.zerosec.teleport.portscan.PortScanController;
import org.zerosec.teleport.util.SpringContextUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-10-25 10:29
 */
@SpringBootApplication
public class TeleportApplication extends Application {
    private ConfigurableApplicationContext applicationContext;
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

    @Override
    public void start(Stage primaryStage) {
        iconStream = ClassLoader.getSystemResourceAsStream("icon.png");
        Image icon = new Image(iconStream);

        PortScanController controller = SpringContextUtil.getApplicationContext().getBean(PortScanController.class);
        controller.init();

        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Teleport - by ZeroSec Team");
        primaryStage.setMaxWidth(1280);
        primaryStage.setMaxHeight(720);

        primaryStage.setScene(new Scene(controller.getView()));
        primaryStage.show();
    }

    @Override
    public void init() {
        // 在这里初始化启动 Spring
        applicationContext = SpringApplication.run(TeleportApplication.class);
    }

    @Override
    public void stop() throws IOException {
        SpringApplication.exit(applicationContext);
        Platform.exit();
        iconStream.close();
    }
}
