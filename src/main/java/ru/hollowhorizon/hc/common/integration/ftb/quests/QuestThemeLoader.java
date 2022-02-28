package ru.hollowhorizon.hc.common.integration.ftb.quests;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.SelectorProperties;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.theme.selector.*;
import dev.ftb.mods.ftbquests.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class QuestThemeLoader {
    public static final QuestTheme STORY_THEME = load(new ResourceLocation(MODID, "quest_themes/story.theme"));
    public static final QuestTheme DEFAULT_THEME = load(new ResourceLocation(MODID, "quest_themes/default.theme"));

    public static QuestTheme load(ResourceLocation location) {
        Map<ThemeSelector, SelectorProperties> map = new HashMap<>();

        try {
            InputStream in = HollowJavaUtils.getResource(location);
            parse(map, FileUtils.read(in));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (map.isEmpty()) {
            FTBQuests.LOGGER.error("FTB Quests theme file is missing! Some mod has broken resource loading, inspect log for errors");
        }

        QuestTheme theme = new QuestTheme();
        theme.defaults = map.remove(AllSelector.INSTANCE);

        if (theme.defaults == null) {
            theme.defaults = new SelectorProperties(AllSelector.INSTANCE);
        }

        theme.selectors.addAll(map.values());
        theme.selectors.sort(null);

        return theme;
    }

    public static void setTheme(QuestTheme theme) {
        QuestTheme.instance = theme;

        LinkedHashSet<String> list = new LinkedHashSet<>();
        list.add("circle");
        list.add("square");
        list.add("rsquare");

        for (String s : ThemeProperties.EXTRA_QUEST_SHAPES.get().split(",")) {
            list.add(s.trim());
        }

        QuestShape.reload(new ArrayList<>(list));
    }

    private static void parse(Map<ThemeSelector, SelectorProperties> selectorPropertyMap, List<String> lines) {
        List<SelectorProperties> current = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            int si, ei;

            if (line.length() > 2 && ((si = line.indexOf('[')) < (ei = line.indexOf(']')))) {
                current.clear();

                for (String sel : line.substring(si + 1, ei).split("\\|")) {
                    AndSelector andSelector = new AndSelector();

                    for (String sel1 : sel.trim().split("&")) {
                        ThemeSelector themeSelector = parse(Pattern.compile("\\s").matcher(sel1).replaceAll(""));

                        if (themeSelector != null) {
                            andSelector.selectors.add(themeSelector);
                        }
                    }

                    if (!andSelector.selectors.isEmpty()) {
                        ThemeSelector selector = andSelector.selectors.size() == 1 ? andSelector.selectors.get(0) : andSelector;
                        current.add(selectorPropertyMap.computeIfAbsent(selector, SelectorProperties::new));
                    }
                }
            } else if (!current.isEmpty()) {
                String[] s1 = line.split(":", 2);

                if (s1.length == 2) {
                    String k = s1[0].trim();
                    String v = s1[1].trim();

                    if (!k.isEmpty() && !v.isEmpty()) {
                        for (SelectorProperties selectorProperties : current) {
                            selectorProperties.properties.put(k, v);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private static ThemeSelector parse(String sel) {
        if (sel.isEmpty()) {
            return null;
        } else if (sel.equals("*")) {
            return AllSelector.INSTANCE;
        } else if (sel.startsWith("!")) {
            ThemeSelector s = parse(sel.substring(1));
            return s == null ? null : new NotSelector(s);
        } else if (QuestObjectType.NAME_MAP.map.containsKey(sel)) {
            return new TypeSelector(QuestObjectType.NAME_MAP.get(sel));
        } else if (sel.startsWith("#")) {
            String s = sel.substring(1);
            return s.isEmpty() ? null : new TagSelector(s);
        }

        try {
            return new IDSelector(Long.valueOf(sel, 16).intValue());
        } catch (Exception ex) {
            return null;
        }
    }

    public static void init() {
    }
}
