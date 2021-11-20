package org.zerosec.teleport.portscan;

import com.google.common.base.Splitter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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
public class PortScan implements Closeable, Runnable {
    private ExecutorService executor;
    private final CopyOnWriteArrayList<Integer> coal = new CopyOnWriteArrayList<>();
    private final Queue<ScanTask> taskQueue = new LinkedList<>();

    private final int runLimit;

    private final AtomicInteger currentRunCount = new AtomicInteger(0);

    private final AtomicInteger totalExecuteCount = new AtomicInteger(0);

    private final int totalTaskCount;


    /**
     * 状态 （0-停止扫描；1-正在扫描；2-暂停扫描；）
     */
    private final IntegerProperty stateProperty = new SimpleIntegerProperty(0);

    private final Lock lock = new ReentrantLock();

    /**
     * 状态条件
     */
    private final Condition stateCondition = lock.newCondition();

    /**
     * 限制条件
     */
    private final Condition limitCondition = lock.newCondition();


    public PortScan(String rawPortText, String rawIpText, ScanProperties scanProperties) {

        Set<Integer> ports = parsePort(rawPortText);

        for (Integer port : ports) {
            taskQueue.offer(new ScanTask(
                    new InetSocketAddress(rawIpText, port),
                    scanProperties.getTimeout())
            );
        }
        runLimit = scanProperties.getMaxExecuteNumber();
        totalTaskCount = ports.size();
    }

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

    public void submit(ScanTask task) throws InterruptedException {
        executor.submit(() -> {
            InetSocketAddress address = task.getAddress();
            int timeout = task.getTimeout();
            log.info("端口：{} 扫描中", address.getPort());
            lock.lock();
            try (Socket socket = new Socket()) {
                socket.connect(address, timeout);
            } catch (IOException e) {
//                log.info("端口：{} 未开放", address.getPort());
            } finally {
                totalExecuteCount.incrementAndGet();
                currentRunCount.decrementAndGet();
                limitCondition.signalAll();
                lock.unlock();
            }
//            log.info("端口：{} 开放", address.getPort());
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
            stateProperty.setValue(StateEnum.SCAN.getCode());
            stateCondition.signalAll();
            log.info("我恢复啦");
        } finally {
            lock.unlock();
        }
    }

    public void suspend() {
        lock.lock();
        try {
            stateProperty.setValue(StateEnum.SUSPEND.getCode());
            log.info("我暂停了");
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        stateProperty.setValue((StateEnum.SCAN.getCode()));
        log.info("我启动啦");
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
                if (StateEnum.SUSPEND.getCode() == stateProperty.getValue()) {
                    log.info("任务已暂停");
                    stateCondition.await();
                }

                submit(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.lock();
            }
        }
        executor.shutdown();
    }

    public void stop() {
        lock.lock();
        try {
            stateProperty.setValue(StateEnum.STOP.getCode());
            log.info("我停止啦");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws SecurityException {
        executor.shutdown();
    }

    @Override
    public void run() {
        start();
    }


    public static void main(String[] args) throws InterruptedException {
        ScanProperties properties = new ScanProperties();
        properties.setTimeout(100);
        properties.setMaxExecuteNumber(20);
        PortScan scan = new PortScan("10-1000", "192.168.124.60", properties);
        scan.setExecutor(Executors.newFixedThreadPool(4));
        new Thread(scan).start();
        TimeUnit.SECONDS.sleep(1);
        scan.suspend();
        TimeUnit.SECONDS.sleep(5);
        scan.resume();
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public int getStateProperty() {
        return stateProperty.get();
    }

    public IntegerProperty statePropertyProperty() {
        return stateProperty;
    }
}
