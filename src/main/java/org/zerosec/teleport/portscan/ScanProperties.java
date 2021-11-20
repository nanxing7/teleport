package org.zerosec.teleport.portscan;

import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-20 14:24
 */
@Getter
@Setter
public class ScanProperties {
    /**
     * 马克斯执行数量
     */
    private int maxExecuteNumber;

    /**
     * 超时（单位：毫秒）
     */
    private int timeout;
}
