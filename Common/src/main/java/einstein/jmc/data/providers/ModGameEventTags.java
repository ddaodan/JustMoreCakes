package einstein.jmc.data.providers;

import einstein.jmc.JustMoreCakes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public class ModGameEventTags {

    public static final TagKey<GameEvent> STEALTH_EFFECT_BLOCKS = TagKey.create(Registries.GAME_EVENT, JustMoreCakes.loc("stealth_effect_blocks"));
}
