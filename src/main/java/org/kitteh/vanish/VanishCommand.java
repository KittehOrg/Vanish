/*
 * * Copyright (C) 2016 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.vanish;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Vanish!
 */
class VanishCommand implements CommandExecutor {
    private final Vanish plugin;
    private final ParticleEffect effect;
    private static final int LIFE_TICKS = 3 * 20;

    VanishCommand(Vanish plugin) {
        this.plugin = plugin;
        this.effect = ParticleEffect.builder().type(ParticleTypes.SMOKE_LARGE).count(1).build();
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource commandSource, @Nonnull CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Text.of(TextColors.RED, "Vanish currently can only be used by players"));
            return CommandResult.empty();
        }
        Player player = (Player) commandSource;
        boolean wasVisible = player.get(Keys.INVISIBLE).orElse(false);
        player.offer(Keys.INVISIBLE, !wasVisible);
        player.offer(Keys.INVISIBILITY_IGNORES_COLLISION, !wasVisible);
        player.offer(Keys.INVISIBILITY_PREVENTS_TARGETING, !wasVisible);
        if (player.hasPermission(Vanish.PERMISSION_EFFECTS_BATS)) {
            Set<Entity> bats = new HashSet<>();
            Location location = player.getLocation();
            for (int i = 0; i < 10; i++) {
                location.getExtent().createEntity(EntityTypes.BAT, location.getPosition()).ifPresent(bat -> {
                    bats.add(bat);
                    location.getExtent().spawnEntity(bat, Cause.builder().owner(this.plugin).build()); // TODO Am I doing this right?
                    bat.offer(Keys.INVULNERABILITY_TICKS, LIFE_TICKS);
                });
            }
            // TODO remove on shutdown too!
            Sponge.getScheduler().createTaskBuilder().delayTicks(LIFE_TICKS).execute(() -> {
                bats.forEach(bat -> {
                    if (bat.isLoaded()) {
                        bat.getWorld().spawnParticles(this.effect, bat.getLocation().getPosition());
                        bat.remove();
                    }
                });
            }).submit(this.plugin);
        }
        player.sendMessage(Text.of(TextColors.AQUA, "You are now " + (wasVisible ? "" : "in") + "visible"));
        return CommandResult.success();
    }
}
