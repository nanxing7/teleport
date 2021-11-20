package org.zerosec.teleport.portscan;

import lombok.ToString;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-20 14:18
 */
@ToString
public class ScanTask {
    private InetSocketAddress address;
    private int timeout;

    public ScanTask(InetSocketAddress address, int timeout) {
        this.address = address;
        this.timeout = timeout;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public int getTimeout() {
        return timeout;
    }
}
