package ru.hollowhorizon.hc.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import kotlin.Pair;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngineManager;
import java.util.ArrayList;


public class HollowCommands {
    private static final ArrayList<Pair<String, Runnable>> commands = new ArrayList<>();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        commands.forEach((pair) -> {
            String name = pair.getFirst();
            Runnable runnable = pair.getSecond();
            dispatcher.register(Commands.literal(name).executes((data) -> {
                runnable.run();
                return 1;
            }));
        });
    }

    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        manager.getEngineFactories().forEach(f -> System.out.println(f.getEngineName()));
    }

    public static void addCommand(@NotNull Pair<String, Runnable> pair) {
        commands.add(pair);
    }
}
