//package org.zerosec.teleport.util;
//
//import ch.qos.logback.classic.spi.ILoggingEvent;
//import ch.qos.logback.core.AppenderBase;
//import okhttp3.OkHttpClient;
//import org.springframework.stereotype.Component;
//
///**
// * https://stackoverflow.com/questions/9395358/how-link-log-from-logback-in-swing-application/33657637#33657637
// *
// * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
// * created at 2021-11-17 15:47
// */
//@Component
//public class Appender extends AppenderBase<ILoggingEvent> {
//    private OkHttpClient okHttpClient;
//
//    @Override
//    protected void append(ILoggingEvent iLoggingEvent) {
//        okHttpClient = SpringContextUtil.getApplicationContext().getBean(OkHttpClient.class);
//    }
//}
