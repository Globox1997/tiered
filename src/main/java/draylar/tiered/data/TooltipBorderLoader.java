package draylar.tiered.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import draylar.tiered.TieredClient;
import draylar.tiered.api.BorderTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class TooltipBorderLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("TieredZ");

    @Override
    public Identifier getFabricId() {
        return Identifier.of("tiered", "tooltip_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {

        resourceManager.findResources("tooltips", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (int u = 0; u < data.getAsJsonArray("tooltips").size(); u++) {
                    JsonObject data2 = (JsonObject) data.getAsJsonArray("tooltips").get(u);
                    List<String> decider = new ArrayList<String>();

                    for (int i = 0; i < data2.getAsJsonArray("decider").size(); i++) {
                        decider.add(data2.getAsJsonArray("decider").get(i).getAsString());
                    }

                    TieredClient.BORDER_TEMPLATES.add(new BorderTemplate(data2.get("index").getAsInt(), data2.get("texture").getAsString(),
                            new BigInteger(data2.get("start_border_gradient").getAsString(), 16).intValue(), new BigInteger(data2.get("end_border_gradient").getAsString(), 16).intValue(),
                            data2.has("background_gradient") ? new BigInteger(data2.get("background_gradient").getAsString(), 16).intValue() : -267386864, decider));
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

}
