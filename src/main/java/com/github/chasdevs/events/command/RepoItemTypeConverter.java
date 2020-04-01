package com.github.chasdevs.events.command;

import com.github.chasdevs.events.services.repo.RepoItemType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RepoItemTypeConverter implements Converter<String, RepoItemType> {
    @Override
    public RepoItemType convert(String source) {
        RepoItemType selectedItemType = null;
        for(RepoItemType itemType : RepoItemType.values()) {
            if(itemType.getLabel().equalsIgnoreCase(source)) {
                selectedItemType = itemType;
            }
        }
        return selectedItemType;
    }
}
