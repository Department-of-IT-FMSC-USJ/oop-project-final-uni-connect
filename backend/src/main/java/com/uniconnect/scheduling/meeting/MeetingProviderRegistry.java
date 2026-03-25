package com.uniconnect.scheduling.meeting;

import com.uniconnect.scheduling.model.MeetingProviderType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MeetingProviderRegistry {

    private final Map<MeetingProviderType, MeetingProvider> providers = new EnumMap<>(MeetingProviderType.class);

    public MeetingProviderRegistry(List<MeetingProvider> availableProviders) {
        for (MeetingProvider provider : availableProviders) {
            providers.put(provider.providerType(), provider);
        }
    }

    public MeetingProvider resolve(MeetingProviderType providerType) {
        MeetingProvider provider = providers.get(providerType);
        if (provider == null) {
            throw new IllegalArgumentException("No meeting provider is registered for: " + providerType);
        }
        return provider;
    }
}

