package org.zerosec.teleport.config;

import lombok.Getter;
import lombok.Setter;
import org.zerosec.teleport.commom.QueueTypeEnum;
import org.zerosec.teleport.commom.RejectedExecutionHandlerEnum;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-18 09:50
 */
@Getter
@Setter
public class ThreadPoolProperties {
    /**
     * 线程池名称
     */
    private String threadPoolName = "TeleportThreadPool";

    /**
     * 核心线程数
     */
    private int corePoolSize = 1;

    /**
     * 最大线程数, 默认值为CPU核心数量
     */
    private int maximumPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 队列最大数量
     */
    private final int queueCapacity = Integer.MAX_VALUE;

    /**
     * 队列类型
     *
     * @see QueueTypeEnum
     */
    private String queueType = QueueTypeEnum.LINKED_BLOCKING_QUEUE.getType();

    /**
     * SynchronousQueue 是否公平策略
     */
    private boolean fair;

    /**
     * 拒绝策略
     *
     * @see RejectedExecutionHandlerEnum
     */
    private String rejectedExecutionType = RejectedExecutionHandlerEnum.ABORT_POLICY.getType();

    /**
     * 空闲线程存活时间
     */
    private long keepAliveTime;

    /**
     * 空闲线程存活时间单位
     */
    private TimeUnit unit = TimeUnit.MILLISECONDS;

    /**
     * 队列容量阀值，超过此值告警
     */
    private int queueCapacityThreshold = queueCapacity;
}
