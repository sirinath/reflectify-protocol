package org.abstractmeta.reflectify.core;

import org.abstractmeta.reflectify.ReflectifyProtocol;
import org.abstractmeta.reflectify.ReflectifyRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides default implementation for the  reflectify registry.
 *
 * @author Adrian Witas
 */
public class ReflectifyRegistryImpl implements ReflectifyRegistry {

    private final Map<Class, ReflectifyProtocol> reflectifyProtocols;


    public ReflectifyRegistryImpl() {
        this(new HashMap<Class, ReflectifyProtocol>());
    }

    protected ReflectifyRegistryImpl(Map<Class, ReflectifyProtocol> reflectifyProtocols) {
        this.reflectifyProtocols = reflectifyProtocols;
    }

    @Override
    public void register(ReflectifyProtocol reflectifyToolbox) {
        reflectifyProtocols.put(reflectifyToolbox.getType(), reflectifyToolbox);
    }

    @Override
    public void registerAll(Collection<ReflectifyProtocol> reflectifyProtocols) {
        for (ReflectifyProtocol protocol : reflectifyProtocols) {
            register(protocol);
        }
    }

    @Override
    public boolean isRegistered(Class type) {
        return reflectifyProtocols.containsKey(type);
    }

    @Override
    public void unregister(Class type) {
        reflectifyProtocols.remove(type);
    }

    @Override
    public void unregisterAll() {
        reflectifyProtocols.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ReflectifyProtocol<T> get(Class<T> type) {
        return (ReflectifyProtocol<T>)reflectifyProtocols.get(type);
    }

    @Override
    public Collection<ReflectifyProtocol> getReflectifyProtocols() {
        return reflectifyProtocols.values();
    }

}
