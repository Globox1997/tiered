package draylar.tiered.platform;

import draylar.tiered.platform.services.INetworkHelper;
import draylar.tiered.platform.services.IPlatformHelper;
import draylar.tiered.platform.services.IRegistryHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    public static INetworkHelper network() {
        return NetworkHolder.NETWORK;
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    private static final class NetworkHolder {
        private static final INetworkHelper NETWORK = load(INetworkHelper.class);
    }
}
