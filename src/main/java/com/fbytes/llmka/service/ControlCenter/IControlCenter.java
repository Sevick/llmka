package com.fbytes.llmka.service.ControlCenter;

import com.fbytes.llmka.model.appevent.AppEvent;

public interface IControlCenter {

    void processEvent(AppEvent event);
}
