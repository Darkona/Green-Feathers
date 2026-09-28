package com.darkona.feathers.compatibility;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.core.FeathersServiceImpl;
import net.minecraft.world.entity.player.Player;

/**
 * The thirst rules shared by every thirst mod's compat: thirst below full slows regeneration, hydration (quench,
 * saturation) speeds it up, and regenerating can cost thirst. Each compat supplies its own readings and its own way
 * to charge.
 */
public final class ThirstRegen {

    public static final int FULL_THIRST = 20;

    private ThirstRegen() {}

    /**
     * Feathers per second to add to regeneration.
     */
    public static double factor(int thirst, double hydration, double reductionPerPoint, double bonusPerPoint) {
        return -(FULL_THIRST - thirst) * reductionPerPoint + hydration * bonusPerPoint;
    }

    /**
     * How much thirst the feathers regenerated since the last call cost, in the compat's own unit. Tracked in the
     * player's feathers counters under {@code key}, so fractions carry over.
     *
     * @return whole units to charge now
     */
    public static int owedSinceLastCall(Player player, FeathersView feathers, String key, double costPerFeather) {
        FeathersData data = FeathersServiceImpl.data(player);
        double regenerated = feathers.totalRegenerated();
        double since = regenerated - data.getCounter(key + ".last_regenerated");
        data.setCounter(key + ".last_regenerated", regenerated);
        if (costPerFeather <= 0 || since <= 0) return 0;

        double owed = data.getCounter(key + ".owed") + since / Stamina.PER_FEATHER * costPerFeather;
        int whole = (int) owed;
        data.setCounter(key + ".owed", owed - whole);
        return whole;
    }

    /**
     * Like {@link #owedSinceLastCall}, for mods that charge fractional exhaustion instead of whole points.
     */
    public static float exhaustionSinceLastCall(Player player, FeathersView feathers, String key, double exhaustionPerFeather) {
        FeathersData data = FeathersServiceImpl.data(player);
        double regenerated = feathers.totalRegenerated();
        double since = regenerated - data.getCounter(key + ".last_regenerated");
        data.setCounter(key + ".last_regenerated", regenerated);
        if (exhaustionPerFeather <= 0 || since <= 0) return 0f;
        return (float) (since / Stamina.PER_FEATHER * exhaustionPerFeather);
    }
}
