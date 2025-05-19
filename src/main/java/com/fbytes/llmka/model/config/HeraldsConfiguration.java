package com.fbytes.llmka.model.config;

import com.fbytes.llmka.model.config.heraldchannel.HeraldConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HeraldsConfiguration {
    private HeraldConfig[] heraldConfigs;

    public static String heraldName(HeraldConfig heraldConfig) {
        return StringUtils.capitalize(heraldConfig.getType().toLowerCase()) + "-" + heraldConfig.getName();
    }
}
