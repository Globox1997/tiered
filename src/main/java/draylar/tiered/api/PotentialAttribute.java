package draylar.tiered.api;

import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import java.util.*;

import org.jetbrains.annotations.Nullable;

public class PotentialAttribute {

    public static class Template {
        private final List<ItemVerifier> verifiers;
        private final List<AttributeTemplate> attributes;
        private final HashMap<String, Object> nbtValues;

        public Template(List<ItemVerifier> verifiers, List<AttributeTemplate> attributes, HashMap<String, Object> nbtValues) {
            this.verifiers = verifiers;
            this.attributes = attributes;
            this.nbtValues = nbtValues;
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
        return getTemplate(id) != null;
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
                templates.add(new Template(verifiers, attributes, nbtValues));
            }
            processed = true;
        }
        return templates;
    }

    public Template getTemplate(Identifier id) {
        if (getTemplates() == null) {
            return null;
        }
        for (Template template : getTemplates()) {
            if (template.verifiers.stream().anyMatch(it -> it.isValid(id))) {
                return template;
            }
        }
        return null;
    }
}
