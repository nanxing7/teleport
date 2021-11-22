package org.zerosec.teleport.portscan;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zerosec.teleport.util.SpringContextUtil;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-20 17:48
 */
@Slf4j
@Component
public class PortScanController {
    private PortScanModel portScanModel;

    private Button btn1;
    private Button btn2;
    private ListView<String> listView;

    private TextArea ipText;
    private TextArea portText;
    private Text logText;

    private VBox root;

    /**
     * btn1单击事件
     */
    public void handleBtn1Click(Event event) {
        Platform.runLater(() -> {
            int state = portScanModel.getState();
            if (StateEnum.STOP.getCode() == state) {
                portScanModel.scan();
            } else {
                portScanModel.stop();
            }
        });
    }

    /**
     * 处理这里点击
     */
    public void handleBtn2Click(Event event) {
        Platform.runLater(() -> {
            int state = portScanModel.getState();
            if (StateEnum.SUSPEND.getCode() == state) {
                portScanModel.resume();
            } else {
                portScanModel.suspend();
            }
        });
    }

    public void init() {
        portScanModel = SpringContextUtil.getApplicationContext().getBean(PortScanModel.class);
        btn1 = new Button();
        btn2 = new Button();
        ipText = new TextArea("127.0.0.1");
        portText = new TextArea("1-200");
        logText = new Text();

        portScanModel.ipTextProperty().bind(ipText.textProperty());
        ipText.disableProperty().bind(portScanModel.ipDisableProperty());

        portText.disableProperty().bind(portScanModel.portDisableProperty());

        portScanModel.portTextProperty().bind(portText.textProperty());

        btn1.disableProperty().bind(portScanModel.btn1DisableProperty());
        btn1.textProperty().bind(portScanModel.btn1TextProperty());
        btn1.setOnMouseClicked(this::handleBtn1Click);

        btn2.disableProperty().bind(portScanModel.btn2DisableProperty());
        btn2.textProperty().bind(portScanModel.btn2TextProperty());
        btn2.setOnMouseClicked(this::handleBtn2Click);

        logText.textProperty().bind(portScanModel.logTextProperty());

        root = new VBox();

        HBox hBox = new HBox();

        hBox.getChildren().addAll(btn1, btn2);

        root.getChildren().addAll(hBox, portText, ipText, logText);
    }


    public Parent getView() {
        return root;
    }


//    private class StateChangeListener implements ChangeListener<Number> {
//
//        @Override
//        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
//            portScanModel.lock.lock();
//            try {
//                if (oldValue.equals(StateEnum.STOP.getCode())
//                        && newValue.equals(StateEnum.SCAN.getCode())) {
//                    Platform.runLater(() -> {
//
//                    });
//                } else if (oldValue.equals(StateEnum.SCAN.getCode())
//                        && newValue.equals(StateEnum.STOP.getCode())) {
//
//                    Platform.runLater(() -> {
//
//                    });
//
//                } else if (oldValue.equals(StateEnum.SCAN.getCode())
//                        && newValue.equals(StateEnum.SUSPEND.getCode())) {
//
//
//
//                } else if (oldValue.equals(StateEnum.SUSPEND.getCode())
//                        && newValue.equals(StateEnum.STOP.getCode())) {
//
//                    portScanModel.btn1TextProperty().setValue("开始扫描");
//                    portScanModel.btn2TextProperty().setValue("暂停扫描");
//
//                    portScanModel.btn2DisableProperty().setValue(true);
//
//
//                } else if (oldValue.equals(StateEnum.SUSPEND.getCode())
//                        && newValue.equals(StateEnum.SCAN.getCode())) {
//                    portScanModel.btn2TextProperty().setValue("暂停扫描");
//                    portScanModel.resume();
//                }
//            } finally {
//                portScanModel.lock.unlock();
//            }
//
//        }
//    }
}
