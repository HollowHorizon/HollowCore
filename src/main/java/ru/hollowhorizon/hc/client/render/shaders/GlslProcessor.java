package ru.hollowhorizon.hc.client.render.shaders;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GlslProcessor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern VERSION_PATTERN = Pattern.compile("^#version (.*)$");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^#import (?>(?><(.*)>)|(?>\"(.*)\"))$");

    private final ResourceLocation shader;
    private final Map<ResourceLocation, ProcessorEntry> processorLookup = new HashMap<>();
    private final LinkedList<ProcessorEntry> newProcessors = new LinkedList<>();

    public GlslProcessor(ResourceLocation shader) {
        this.shader = shader;
        newProcessors.add(processorLookup.computeIfAbsent(shader, ProcessorEntry::new));
    }

    public String process() {
        ProcessorEntry mainEntry = newProcessors.peek();
        String mainVersion = Objects.requireNonNull(mainEntry.version, "Main Shader '" + shader + "' in chain requires #version.");

        if (mainEntry.includes.isEmpty()) {
            return String.join("\n", mainEntry.lines);
        }

        MutableGraph<ResourceLocation> graph = GraphBuilder.directed().build();
        while (!newProcessors.isEmpty()) {
            ProcessorEntry entry = newProcessors.pop();
            if (entry.version != null && !entry.version.equals(mainVersion)) {
                LOGGER.warn("Shader chain {} -> {} version discrepency. Main Shader: {}, Included Shader: {}. This shader may not compile.", shader, entry.resource, mainVersion, entry.version);
            }
            for (ResourceLocation include : entry.includes) {
                graph.putEdge(include, entry.resource);
                if (!processorLookup.containsKey(include)) {
                    newProcessors.add(processorLookup.computeIfAbsent(include, ProcessorEntry::new));
                }
            }
        }

        List<ResourceLocation> order = TopologicalSort.topologicalSort(graph, null);
        List<String> outputLines = new LinkedList<>();
        outputLines.add("#version " + mainVersion);
        for (ResourceLocation resourceLocation : order) {
            outputLines.add("");
            outputLines.add("/*" + resourceLocation + "*/");
            ProcessorEntry entry = processorLookup.get(resourceLocation);
            for (int i = 0; i < entry.lines.size(); i++) {
                String line = entry.lines.get(i);
                if (entry.linesToComment.contains(i)) {
                    outputLines.add("/*" + line + "*/");
                } else {
                    outputLines.add(line);
                }
            }
        }

        return String.join("\n", outputLines);
    }

    private static class ProcessorEntry {

        private final ResourceLocation resource;
        private final List<String> lines;
        private final List<ResourceLocation> includes;
        @Nullable
        private final String version;
        private final IntSet linesToComment;

        private ProcessorEntry(ResourceLocation resource) {
            this.resource = resource;
            lines = loadResource(resource);
            linesToComment = new IntOpenHashSet();
            includes = extractIncludes(lines, linesToComment);
            version = extractVersion(lines, linesToComment);
        }

        private static List<ResourceLocation> extractIncludes(List<String> lines, IntSet linesToComment) {
            List<ResourceLocation> imports = new LinkedList<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher matcher = IMPORT_PATTERN.matcher(line);
                if (matcher.find()) {
                    linesToComment.add(i);
                    String match = matcher.group(1);
                    boolean includeFolder = match != null;
                    if (!includeFolder) {
                        match = matcher.group(2);
                    }
                    ResourceLocation loc = new ResourceLocation(match);
                    if (includeFolder) {
                        loc = new ResourceLocation(loc.getNamespace(), FilenameUtils.normalize("shaders/include/" + loc.getPath(), true));
                    }
                    imports.add(loc);
                }
            }

            return new ArrayList<>(imports);
        }

        @Nullable
        private static String extractVersion(List<String> lines, IntSet linesToComment) {
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher matcher = VERSION_PATTERN.matcher(line);
                if (matcher.find()) {
                    linesToComment.add(i);
                    return matcher.group(1);
                }
            }
            return null;
        }

        private static List<String> loadResource(ResourceLocation location) {
            try {
                Resource resource = Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.toList());
                }
            } catch (IOException ex) {
                throw new RuntimeException("Unable to read asset '" + location + "'.", ex);
            }
        }
    }

}
