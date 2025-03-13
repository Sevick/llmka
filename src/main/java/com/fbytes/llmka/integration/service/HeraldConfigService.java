package com.fbytes.llmka.integration.service;

import com.fbytes.llmka.model.heraldchannel.Herald;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HeraldConfigService {
    Herald[] heralds;

    static public String heraldName(Herald herald){
        return StringUtils.capitalize(herald.getType().toLowerCase()) + "-" + herald.getName();
    }
}
