package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

public class CannonUseEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private Cannon cannon;
    private Player player;
    private InteractAction action;
    private boolean cancelled;

    public CannonUseEvent(Cannon cannon, Player player, InteractAction action)
    {
        this.cannon = cannon;
        this.player = player;
        this.action = action;
        this.cancelled = false;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Player getPlayer() {
        return player;
    }

    public InteractAction getAction() {
        return action;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
