package org.zerosec.teleport.portscan;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-18 10:57
 */
public enum StateEnum {
    /**
     * 停止状态
     */
    STOP(0),
    /**
     * 扫描状态
     */
    SCAN(1),
    /**
     * 暂停状态
     */
    SUSPEND(2),
    ;

    private final int code;

    StateEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
