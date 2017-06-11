package net.kineticraft.lostcity.config.configs;

import lombok.Getter;
import lombok.Setter;
import net.kineticraft.lostcity.config.Configs;
import net.kineticraft.lostcity.config.JsonConfig;
import net.kineticraft.lostcity.data.JsonData;
import net.kineticraft.lostcity.data.maps.JsonStringMap;

/**
 * Main plugin config.
 *
 * Created by Kneesnap on 6/3/2017.
 */
@Getter @Setter
public class MainConfig extends JsonConfig {

    private String voteURL;
    private JsonStringMap filter = new JsonStringMap();

    public MainConfig() {
        super("main");
    }

    @Override
    public void load(JsonData data) {
        setVoteURL(data.getString("voteURL", "http://google.com/"));
        setFilter(data.getStringMap("filter"));
    }

    @Override
    public JsonData save() {
        return new JsonData().setString("voteURL", getVoteURL()).setElement("filter", getFilter());
    }

}