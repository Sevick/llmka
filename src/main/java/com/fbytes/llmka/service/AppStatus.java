package com.fbytes.llmka.service;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.appevent.AppEvent;
import com.fbytes.llmka.service.Maintenance.AppEventSenderService.IAppEventSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AppStatus implements SmartLifecycle {
    private static final Logger logger = Logger.getLogger(AppStatus.class);
    private AtomicBoolean runningFlag = new AtomicBoolean(false);

    @Autowired
    IAppEventSenderService<AppEvent> appEventSenderService;

    @Override
    public void start() {
        boolean result = runningFlag.compareAndSet(false, true);
        if (result)
            appEventSenderService.sendEvent(new AppEvent("AppStatus", formatDescription(), AppEvent.EventType.STARTUP));
    }

    @Override
    public void stop() {
        boolean result = runningFlag.compareAndSet(true, false);
        if (result) {
            appEventSenderService.sendEvent(new AppEvent("AppStatus", formatDescription(), AppEvent.EventType.SHUTDOWN));
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    private String formatDescription() {
        String description;
        try {
            description = MessageFormat.format("\n{0}\n{1}",
                    InetAddress.getLocalHost().getHostName(),
                    InetAddress.getLocalHost()
            );
        } catch (UnknownHostException e) {
            description = "\n" + this.toString();
        }
        return description;
    }

    @Override
    public boolean isRunning() {
        return runningFlag.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Highest phase ensures last initialization and first shutdown
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
