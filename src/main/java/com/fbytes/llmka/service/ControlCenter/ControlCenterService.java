package com.fbytes.llmka.service.ControlCenter;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.appevent.AppEvent;
import com.fbytes.llmka.service.Maintenance.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ControlCenterService implements IControlCenter {
    private static final Logger logger = Logger.getLogger(ControlCenterService.class);

    @Autowired
    private MaintenanceService maintenanceService;

    @Override
    public void processEvent(AppEvent event) {
        logger.debug("processing event: {}", event);
        switch (event.getEventType()) {
            case METAHASH_COMPRESS:
                maintenanceService.compressDB(event.getInstance());
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
