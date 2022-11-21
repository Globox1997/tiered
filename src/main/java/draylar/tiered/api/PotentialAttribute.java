package draylar.tiered.api;

import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class PotentialAttribute {

    public static class Template {
        private final String id;
        private final List<ItemVerifier> verifiers;
        private final List<AttributeTemplate> attributes;
        private final HashMap<String, Object> nbtValues;

        public Template(String id, List<ItemVerifier> verifiers, List<AttributeTemplate> attributes, HashMap<String, Object> nbtValues) {
            this.id = id;
            this.verifiers = verifiers;
            this.attributes = attributes;
            this.nbtValues = nbtValues;
        }

        public String getId() {
            return id;
        }

        public List<ItemVerifier> getVerifiers() {
            return verifiers;
        }

        public List<AttributeTemplate> getAttributes() {
            return attributes;
        }

        @Nullable
        public HashMap<String, Object> getNbtValues() {
            return nbtValues;
        }

        @Override
        public int hashCode() {
            return (id == null ? "" : id).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Template other)) return false;
            if (this != other) return false;
            return StringUtils.equals(this.id, other.id);
        }
    }

    private final String id;
    private final int weight;
    private final Style style;
    private final List<ItemVerifier> verifiers;
    private final List<AttributeTemplate> attributes;
    private final HashMap<String, Object> nbtValues;
    private List<Template> templates;

    public PotentialAttribute(String id, int weight, Style style, List<ItemVerifier> verifiers, List<AttributeTemplate> attributes, HashMap<String, Object> nbtValues, List<Template> templates) {
        this.id = id;
        this.weight = weight;
        this.style = style;
        this.verifiers = verifiers;
        this.attributes = attributes;
        this.nbtValues = nbtValues;
        this.templates = templates;
    }


    public String getID() {
        return id;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isValid(Identifier id) {
        if (getTemplates() == null || getTemplates().isEmpty()) {
            return false;
        }
        return getTemplates().stream().anyMatch(it -> it.verifiers.stream().anyMatch(i -> i.isValid(id)));
//        return getTemplate(id) != null;
//        for (ItemVerifier verifier : verifiers) {
//            if (verifier.isValid(id))
//                return true;
//        }
//
//        return false;
    }

    public Style getStyle() {
        return style;
    }

    private boolean processed = false;
    public List<Template> getTemplates() {
        if (!processed) {
            if (verifiers != null && attributes != null || nbtValues != null) {
                if (templates == null) {
                    templates = new ArrayList<>();
                }
                templates.add(new Template(null, verifiers, attributes, nbtValues));
            }
            if (templates != null) {
                templates = templates.stream().distinct().collect(Collectors.toList());
            }
            processed = true;
        }
        return templates;
    }
    public Template getTemplate(Identifier itemId) {
        return getTemplate(itemId, null);
    }

    public Template getTemplate(Identifier itemId, String templateId) {
        if (getTemplates() == null || getTemplates().isEmpty()) {
            return null;
        }
        List<Template> hits = getTemplates().stream().filter(it -> it.verifiers.stream().anyMatch(i -> i.isValid(itemId))).toList();
        if (hits.isEmpty()) {
            return null;
        }
        if (hits.size() == 1) {
            return hits.get(0);
        }
        return hits.stream().filter(it -> StringUtils.equals(templateId, it.id)).findFirst()
                .orElse(hits.stream().min(Comparator.comparing(it -> RandomUtils.nextInt())).orElse(null));
    }
}
