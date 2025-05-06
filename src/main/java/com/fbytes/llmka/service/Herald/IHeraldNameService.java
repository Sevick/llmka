package com.fbytes.llmka.service.Herald;

import com.fbytes.llmka.model.config.heraldchannel.HeraldConfig;
import org.apache.commons.lang3.StringUtils;

public interface IHeraldNameService {
    public static String makeFullName(HeraldConfig heraldConfig){
        return StringUtils.capitalize(heraldConfig.getType().toLowerCase()) + "-" + heraldConfig.getName();
    }
}
