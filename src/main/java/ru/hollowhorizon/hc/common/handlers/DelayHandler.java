package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.hollowhorizon.hc.common.events.action.ActionPackage;
import ru.hollowhorizon.hc.common.events.action.ActionStorage;
import ru.hollowhorizon.hc.common.events.action.HollowAction;
import ru.hollowhorizon.hc.common.network.data.ActionsData;

import java.util.*;

public class DelayHandler {
    private static final Map<String, List<ActionPackage>> actions = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::onServerStop);
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::onPlayerJoin);
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::onPlayerLeave);
    }

    public static void addDelayForAction(int ticks, HollowAction action, ServerPlayerEntity player) {
        addDelayForAction(ticks, ActionStorage.getName(action), player);
    }

    public static void addDelayForAction(int ticks, String action, ServerPlayerEntity player) {
        if (actions.containsKey(player.getUUID().toString())) {
            ActionPackage pack = new ActionPackage(action, ticks);
            if (!actions.get(player.getUUID().toString()).contains(pack))
                actions.get(player.getUUID().toString()).add(pack);
        } else {
            ArrayList<ActionPackage> list = new ArrayList<>();
            list.add(new ActionPackage(action, ticks));
            actions.put(player.getUUID().toString(), list);
        }

    }

    private static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            server.getPlayerList().getPlayers().forEach(player -> {
                String playerId = player.getUUID().toString();

                if (actions.containsKey(playerId)) {
                    List<ActionPackage> pack = actions.get(playerId);
                    if (pack == null) return;

                    for (Iterator<ActionPackage> iterator = pack.iterator(); iterator.hasNext(); ) {
                        ActionPackage cpack = iterator.next();
                        if (cpack.tick()) {
                            HollowAction action = ActionStorage.getAction(cpack.getAction());
                            if(action!=null) {
                                action.process(player);
                                ActionsData.INSTANCE.removeActionData(player, cpack.getAction());
                                iterator.remove();
                            }
                        }
                    }

                    actions.put(playerId, pack);
                }
            });
        }
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        String uuid = player.getUUID().toString();
        if (!actions.containsKey(uuid)) actions.put(uuid, ActionsData.INSTANCE.getAllActions(player));
        else {
            List<ActionPackage> packages = ActionsData.INSTANCE.getAllActions(player);
            packages.addAll(actions.get(uuid));
            actions.put(uuid, packages);
        }

    }

    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        String playerId = event.getPlayer().getUUID().toString();

        if (actions.containsKey(playerId)) {
            List<ActionPackage> pack = actions.get(playerId);
            pack.forEach((cpack) -> ActionsData.INSTANCE.addActionData(event.getPlayer().getUUID().toString(), cpack.getAction(), cpack.getDelay()));
            actions.get(playerId).clear();
        }

    }

    private static void onServerStop(FMLServerStoppingEvent event) {
        event.getServer().getPlayerList().getPlayers().forEach(player -> {
            if (actions.containsKey(player.getUUID().toString())) {
                actions.get(player.getUUID().toString()).forEach((pack) -> ActionsData.INSTANCE.addActionData(player.getUUID().toString(), pack.getAction(), pack.getDelay()));
                actions.get(player.getUUID().toString()).clear();
            }
        });
    }
}
