package ru.hollowhorizon.hc.common.animations;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Мои комментарии пипец полезные :D
 */
public class AnimationManager {
    private static final List<AnimationInfo> recordingAnimations = new ArrayList<>();
    private static final List<AnimationInfo> playingAnimations = new ArrayList<>();

    public static void startAnimation(AnimationInfo animationInfo) {
        playingAnimations.add(animationInfo);
    }

    public static void endAnimation(AnimationInfo animation) {
        playingAnimations.remove(animation);
    }

    public static void startRecording(ServerPlayerEntity player) {
        for (AnimationInfo animation : recordingAnimations) {
            if (animation.getPlayer().getUUID().equals(player.getUUID())) return;
        }
        recordingAnimations.add(new AnimationInfo(player));
    }

    /**
     * Закончить запись
     */
    public static void endRecording(ServerPlayerEntity player, String fileName) {
        Iterator<AnimationInfo> iterator = recordingAnimations.iterator();
        while (iterator.hasNext()) {
            AnimationInfo animation = iterator.next();
            if (animation.getPlayer().getUUID().equals(player.getUUID())) {
                saveAnimation(animation, fileName);
                iterator.remove();
            }
        }
    }

    public static AnimationInfo loadAnimation(InputStream animation, LivingEntity entity) {
        try {
            DataInputStream stream = new DataInputStream(animation);
            double startPosX = stream.readDouble();
            double startPosY = stream.readDouble();
            double startPosZ = stream.readDouble();
            int size = stream.readInt();
            AnimationInfo result = new AnimationInfo(entity);
            result.setStartPos(startPosX, startPosY, startPosZ);
            for (int i = 0; i < size; i++) {
                double posX = stream.readDouble();
                double posY = stream.readDouble();
                double posZ = stream.readDouble();

                float rotX = stream.readFloat();
                float rotY = stream.readFloat();
                float rotZ = stream.readFloat();

                result.addPosInfo(posX, posY, posZ, rotX, rotY, rotZ);
            }
            result.setEndless(stream.readBoolean());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    

    private static void saveAnimation(AnimationInfo animation, String fileName) {
        try {
            String config = ServerLifecycleHooks.getCurrentServer().getServerDirectory().toPath().resolve("config").resolve("hollow-core").resolve("animations").resolve(fileName + ".hollowanim").toFile().getAbsolutePath();
            DataOutputStream data = new DataOutputStream(new FileOutputStream(config));

            int size = animation.getMobPos().size();

            Vector3d startPos = animation.getMobPos().get(0).getMobPosition();

            data.writeDouble(startPos.x);
            data.writeDouble(startPos.y);
            data.writeDouble(startPos.z);

            data.writeInt(size);

            for (int i = 0; i < size; i++) {
                Vector3d pos = animation.getMobPos().get(i).getMobPosition();
                Vector3f rot = animation.getMobPos().get(i).getMobRotation();

                data.writeDouble(pos.x - startPos.x);
                data.writeDouble(pos.y - startPos.y);
                data.writeDouble(pos.z - startPos.z);

                data.writeFloat(rot.x());
                data.writeFloat(rot.y());
                data.writeFloat(rot.z());
            }

            data.writeBoolean(animation.isEndless());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка анимации
     */
    public static void tick(TickEvent.ServerTickEvent event) {
        for (AnimationInfo animation : recordingAnimations) {
            if (animation.isPause()) {
                Vector3d pos = animation.getPlayer().position();
                Vector3f rot = new Vector3f(animation.getPlayer().xRot, animation.getPlayer().yRot, 0F);

                animation.addPosInfo(pos, rot);
            }
        }

        Iterator<AnimationInfo> iterator = playingAnimations.iterator();
        while (iterator.hasNext()) {
            AnimationInfo animation = iterator.next();
            if (animation.getCounter() < animation.getMobPos().size()) {
                animation.tickCounter();
            } else {
                if (animation.isEndless()) {
                    animation.setCounter(0);
                } else {
                    iterator.remove();
                    return;
                }
            }

            LivingEntity entity = animation.getPlayer();
            MobPosInfo posRotInfo = animation.getMobPos().get(animation.getCounter());

            entity.moveTo(posRotInfo.getPosX(), posRotInfo.getPosY(), posRotInfo.getPosZ(), posRotInfo.getRotY(), posRotInfo.getRotX());
        }
    }
}
