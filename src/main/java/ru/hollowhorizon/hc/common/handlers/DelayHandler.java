package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import ru.hollowhorizon.hc.common.events.action.ActionPackage;
import ru.hollowhorizon.hc.common.events.action.ActionStorage;
import ru.hollowhorizon.hc.common.events.action.HollowAction;

import java.util.*;

public class DelayHandler {
    private static final Map<String, List<ActionPackage>> actions = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::onServerStop);
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::onPlayerJoin);
        MinecraftForge.EVENT_BUS.addListener(DelayHandler::onPlayerLeave);
    }

    public static void addDelayForAction(int ticks, HollowAction action, ServerPlayer player) {
        addDelayForAction(ticks, ActionStorage.getName(action), player);
    }

    public static void addDelayForAction(int ticks, String action, ServerPlayer player) {
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
                            if (action != null) {
                                action.process(player);
                                //ActionsData.INSTANCE.removeActionData(player, cpack.getAction());
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
        var player = event.getEntity();
        String uuid = player.getUUID().toString();
        //if (!actions.containsKey(uuid)) actions.put(uuid, ActionsData.INSTANCE.getAllActions(player));
        //else {
        //List<ActionPackage> packages = ActionsData.INSTANCE.getAllActions(player);
        //packages.addAll(actions.get(uuid));
        //actions.put(uuid, packages);
        //}

    }

    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        String playerId = event.getEntity().getUUID().toString();

        if (actions.containsKey(playerId)) {
            List<ActionPackage> pack = actions.get(playerId);
            //pack.forEach((cpack) -> ActionsData.INSTANCE.addActionData(event.getPlayer().getUUID().toString(), cpack.getAction(), cpack.getDelay()));
            actions.get(playerId).clear();
        }

    }

    private static void onServerStop(ServerStoppedEvent event) {
        event.getServer().getPlayerList().getPlayers().forEach(player -> {
            if (actions.containsKey(player.getUUID().toString())) {
                //actions.get(player.getUUID().toString()).forEach((pack) -> ActionsData.INSTANCE.addActionData(player.getUUID().toString(), pack.getAction(), pack.getDelay()));
                actions.get(player.getUUID().toString()).clear();
            }
        });
    }
}
