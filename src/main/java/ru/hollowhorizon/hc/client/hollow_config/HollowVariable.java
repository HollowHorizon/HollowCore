package ru.hollowhorizon.hc.client.hollow_config;

public class HollowVariable<T> {
    private T value;
    private final String name;

    public HollowVariable(T value, String name) {
        this.value = value;
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        if (value instanceof Boolean) {
            HollowCoreConfig.setBool(name, (Boolean) value);
        } else if (value instanceof Integer) {
            HollowCoreConfig.setInt(name, (Integer) value);
        } else if (value instanceof Float) {
            HollowCoreConfig.setFloat(name, (Float) value);
        }
    }

    public String getName() {
        return name;
    }
}
