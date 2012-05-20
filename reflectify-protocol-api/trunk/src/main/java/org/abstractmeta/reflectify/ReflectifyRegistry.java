package org.abstractmeta.reflectify;


import java.util.Collection;

/**
 * Represents reflectify protocol registry.
 *
 * @author Adrian Witas
 */

public interface ReflectifyRegistry {

    void register(ReflectifyProtocol reflectifyToolbox);

    void registerAll(Collection<ReflectifyProtocol> reflectifyToolboxes);

    boolean isRegistered(Class type);

    void unregister(Class type);

     void unregisterAll();

    <T> ReflectifyProtocol<T> get(Class<T> type);

    Collection<ReflectifyProtocol> getReflectifyProtocols();

}
