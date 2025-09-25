package bw.development.facelessItems.Effects;

// No need for Bukkit imports here, as the context handles them.
public interface Effect {

    /**
     * Applies the effect using the contextual data provided.
     * All concrete effect classes will typically call their implementation-specific
     * logic using the context.
     * @param context The object containing the user, the target, and the event.
     */
    void apply(EffectContext context);

    /** Returns the type of effect for identification and debugging. */
    String getType();
}