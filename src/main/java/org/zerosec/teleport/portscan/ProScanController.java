package org.zerosec.teleport.portscan;

import javafx.beans.property.IntegerProperty;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-20 17:48
 */
public class ProScanController {
    private PortScan portScan;

    private IntegerProperty stateProperty;

    public ProScanController() {
        stateProperty = portScan.statePropertyProperty();
    }


    /**
     * btn1单击事件
     */
    public void handleBtn1Click() {

    }

    /**
     * 处理这里点击
     */
    public void handleBtn2Click() {

    }
}
