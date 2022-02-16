package ru.hollowhorizon.hc.client.render.shader;

import org.lwjgl.opengl.GL20;
import ru.hollowhorizon.hc.HollowCore;

public abstract class Uniform<T> {

    private static final int NOT_FOUND = -1;

    private final String name;
    private int location;

    public Uniform(final String name){
        this.name = name;
    }

    public void storeUniformLocation(final int programID){
        location = GL20.glGetUniformLocation(programID, name);
        if(location == NOT_FOUND){
            HollowCore.LOGGER.error("No uniform variable called " + name + " found!");
        }
    }

    public int getLocation(){
        return location;
    }

    public abstract void load(final T toLoad);
}
