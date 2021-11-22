package org.zerosec.teleport.portscan;

import com.google.common.base.Splitter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-19 15:12
 */
@Slf4j
@Component
public class PortScanModel {

    private ThreadPoolTaskExecutor commonThreadPool;

    private final Queue<ScanTask> taskQueue = new LinkedList<>();

    private int runLimit;

    private final AtomicInteger currentRunCount = new AtomicInteger(0);

    private final AtomicInteger totalExecuteCount = new AtomicInteger(0);


    /**
     * 状态 （0-停止扫描；1-正在扫描；2-暂停扫描；）
     */
    private int state = 0;

    private final BooleanProperty btn1Disable = new SimpleBooleanProperty(false);
    private final BooleanProperty btn2Disable = new SimpleBooleanProperty(true);

    private final BooleanProperty ipDisable = new SimpleBooleanProperty(false);
    private final StringProperty ipText = new SimpleStringProperty("127.0.0.1");

    private final BooleanProperty portDisable = new SimpleBooleanProperty(false);
    private final StringProperty portText = new SimpleStringProperty("1-65535");

    private final StringProperty btn1Text = new SimpleStringProperty("开始扫描");
    private final StringProperty btn2Text = new SimpleStringProperty("暂停扫描");

    private final StringProperty logText = new SimpleStringProperty("233");

    /**
     * 马克斯执行数量
     */
    private final IntegerProperty maxExecuteNumber = new SimpleIntegerProperty(10);

    /**
     * 超时（单位：毫秒）
     */
    private final IntegerProperty timeout = new SimpleIntegerProperty(100);


    private final ListProperty<String> scanList = new SimpleListProperty<>();

    protected final Lock lock = new ReentrantLock();

    /**
     * 状态条件
     */
    private final Condition stateCondition = lock.newCondition();

    /**
     * 限制条件
     */
    private final Condition limitCondition = lock.newCondition();


    public Set<Integer> parsePort(String rawPortText) {
        Set<Integer> ports = new HashSet<>();
        rawPortText = StringUtils.trimAllWhitespace(rawPortText);

        Iterable<String> result = Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .split(rawPortText);

        for (String s : result) {
            int port;

            if (s.contains("-")) {
                int start;
                int end;

                String[] sArr = StringUtils.split(s, "-");

                start = Integer.valueOf(sArr[0]);
                end = Integer.valueOf(sArr[1]);

                if (start >= end) {
                    throw new IllegalArgumentException("开始端口不能大于结束端口");
                }
                for (int i = start; i <= end; i++) {
                    checkPort(i);
                    ports.add(i);
                }
            } else {
                port = Integer.valueOf(s);
                checkPort(port);
                ports.add(port);
            }
        }
        return ports;
    }

    public void scan() {
        lock.lock();
        try {
            state = StateEnum.SCAN.getCode();
            totalExecuteCount.set(0);
            taskQueue.clear();
            Set<Integer> ports = parsePort(portText.get());

            for (Integer port : ports) {
                taskQueue.offer(new ScanTask(
                        new InetSocketAddress(ipText.get(), port),
                        timeout.get())
                );
            }
            runLimit = maxExecuteNumber.get();

            int totalTaskCount = ports.size();

            Platform.runLater(() -> {
                    btn1TextProperty().setValue("停止扫描");
                    btn2DisableProperty().setValue(false);
                    btn1DisableProperty().setValue(false);
                    logText.setValue("开始扫描");
            });

            commonThreadPool.submit(() -> {
                while (true) {
                    lock.lock();
                    try {
                        if (totalExecuteCount.get() == totalTaskCount) {
                            stop();
                            break;
                        }
                        ScanTask task = taskQueue.poll();
                        if (currentRunCount.incrementAndGet() >= runLimit) {
                            log.info("任务已达 " + (currentRunCount) + " 个");
                            limitCondition.await();
                        }
                        if (StateEnum.SUSPEND.getCode() == state) {
                            log.info("任务已暂停");
                            stateCondition.await();
                        }
                        if (StateEnum.STOP.getCode() == state) {
                            log.info("任务已停止");
                            stop();
                            break;
                        }
                        submit(task);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            });
        } finally {
            lock.unlock();
        }
    }

    public void submit(ScanTask task) throws InterruptedException {
        commonThreadPool.submit(() -> {
            InetSocketAddress address = task.getAddress();
            int timeout = task.getTimeout();
            lock.lock();
            try (Socket socket = new Socket()) {
                log.info("端口：{} 扫描中", address.getPort());
                Platform.runLater(() -> {
                    logText.setValue("端口：" + address.getPort() + " 扫描中");
                });
                socket.connect(address, timeout);
            } catch (IOException e) {
                log.info("端口：{} 未开放", address.getPort());
            } finally {
                totalExecuteCount.incrementAndGet();
                currentRunCount.decrementAndGet();
                limitCondition.signalAll();
                lock.unlock();
            }
            log.info("端口：{} 开放", address.getPort());
        });
    }

    private static void checkPort(int port) {
        if (port > 65535 || port < 1) {
            throw new IllegalArgumentException("端口范围异常");
        }
    }

    public void resume() {
        lock.lock();
        try {
            state = StateEnum.SCAN.getCode();
            stateCondition.signalAll();
            Platform.runLater(() -> {
                btn2TextProperty().setValue("暂停扫描");
            });
            log.info("我恢复啦");
        } finally {
            lock.unlock();
        }
    }

    public void suspend() {
        lock.lock();
        try {
            Platform.runLater(() -> {
                btn2TextProperty().setValue("恢复扫描");
            });
            state = StateEnum.SUSPEND.getCode();
            log.info("我暂停了");
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        lock.lock();
        try {
            state = StateEnum.STOP.getCode();
            Platform.runLater(() -> {
                    btn1TextProperty().setValue("开始扫描");
                    btn2TextProperty().setValue("暂停扫描");
                    btn2DisableProperty().setValue(true);
                    logText.setValue("扫描结束");
            });
            log.info("我停止啦");
        } finally {
            lock.unlock();
        }
    }

    public BooleanProperty btn1DisableProperty() {
        return btn1Disable;
    }

    public boolean getBtn2Disable() {
        return btn2Disable.get();
    }

    public BooleanProperty btn2DisableProperty() {
        return btn2Disable;
    }

    public String getBtn1Text() {
        return btn1Text.get();
    }

    public StringProperty btn1TextProperty() {
        return btn1Text;
    }

    public String getBtn2Text() {
        return btn2Text.get();
    }

    public StringProperty btn2TextProperty() {
        return btn2Text;
    }

    public boolean getIpDisable() {
        return ipDisable.get();
    }

    public BooleanProperty ipDisableProperty() {
        return ipDisable;
    }

    public String getIpText() {
        return ipText.get();
    }

    public StringProperty ipTextProperty() {
        return ipText;
    }

    public boolean isPortDisable() {
        return portDisable.get();
    }

    public BooleanProperty portDisableProperty() {
        return portDisable;
    }

    public String getPortText() {
        return portText.get();
    }

    public StringProperty portTextProperty() {
        return portText;
    }

    public ObservableList<String> getScanList() {
        return scanList.get();
    }

    public ListProperty<String> scanListProperty() {
        return scanList;
    }

    public String getLogText() {
        return logText.get();
    }

    public StringProperty logTextProperty() {
        return logText;
    }

    public int getState() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    @Autowired
    public void setCommonThreadPool(ThreadPoolTaskExecutor commonThreadPool) {
        this.commonThreadPool = commonThreadPool;
    }
}
