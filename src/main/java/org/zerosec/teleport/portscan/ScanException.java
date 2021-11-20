package org.zerosec.teleport.portscan;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-20 15:10
 */
public class ScanException extends RuntimeException {

    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
