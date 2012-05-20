package org.abstractmeta.reflectify.core;

import org.abstractmeta.reflectify.ReflectifyProtocol;
import org.abstractmeta.reflectify.ReflectifyRegistry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents ReflectifyProxyRegistry
 *
 * @author Adrian Witas
 */
public class ReflectifyProxyRegistry implements ReflectifyRegistry {


    private final Collection<ReflectifyRegistry> reflectifyRegistries;

    public ReflectifyProxyRegistry(Collection<ReflectifyRegistry> reflectifyRegistries) {
        this.reflectifyRegistries = reflectifyRegistries;
    }

    @Override
    public void register(ReflectifyProtocol reflectifyToolbox) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerAll(Collection<ReflectifyProtocol> reflectifyToolboxes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRegistered(Class type) {
        for (ReflectifyRegistry reflectifyRegistry : reflectifyRegistries) {
            if (reflectifyRegistry.isRegistered(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void unregister(Class type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> ReflectifyProtocol<T> get(Class<T> type) {
        for (ReflectifyRegistry reflectifyRegistry : reflectifyRegistries) {
            ReflectifyProtocol<T> result = reflectifyRegistry.get(type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Collection<ReflectifyProtocol> getReflectifyProtocols() {
        Collection<ReflectifyProtocol> result = new ArrayList<ReflectifyProtocol>();
        for (ReflectifyRegistry registry : reflectifyRegistries) {
            result.addAll(registry.getReflectifyProtocols());
        }
        return result;
    }


}
