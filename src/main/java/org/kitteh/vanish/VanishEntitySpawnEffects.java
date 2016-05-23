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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Handles spawning of entities when players (un)vanish.
 */
public class VanishEntitySpawnEffects {
    // How long the spawned entities should stay around for.
    private static final int LIFE_TICKS = 3 * 20;

    // The number of entities to spawn.
    private static final int NUM_ENTITIES = 10;

    // The Cause for spawning the entities.
    private final Cause entityCause;

    // The ParticleEffect used when spawning entities.
    private final ParticleEffect effect;

    // The plugin instance.
    private final Vanish plugin;

    // A Map of permission strings to EntityTypes, used for determining which entities to spawn.
    private final Map<String, EntityType> permEntityMap = new HashMap<>();

    // A Set of all entities spawned, used to remove entities when the server stops, if needed.
    private final Set<Entity> allEntities = Collections.newSetFromMap(new WeakHashMap<>());

    VanishEntitySpawnEffects(Vanish plugin) {
        this.plugin = plugin;
        this.entityCause = Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build())
                .owner(this.plugin).build();
        this.effect = ParticleEffect.builder().type(ParticleTypes.SMOKE_LARGE).count(1).build();

        // Set up permissions/entities map.
        this.permEntityMap.put(Vanish.PERMISSION_EFFECTS_BATS, EntityTypes.BAT);
        this.permEntityMap.put(Vanish.PERMISSION_EFFECTS_CATS, EntityTypes.OCELOT);

        // Register to listen to events.
        Sponge.getEventManager().registerListeners(this.plugin, this);
    }

    /**
     * Spawns entities around a player, based on which permissions they have. Schedules the entities it spawns for later
     * deletion.
     *
     * @param player the player to spawn entities at
     */
    public void applySpawnEffect(Player player) {
        Location<World> location = player.getLocation();
        Set<Entity> ourEntities = new HashSet<>();

        for (String permission : this.permEntityMap.keySet()) {
            if (player.hasPermission(permission)) {
                for (int i = 0; i < NUM_ENTITIES; i++) {
                    location.getExtent().createEntity(this.permEntityMap.get(permission), location.getPosition())
                            .ifPresent(entity -> {
                                ourEntities.add(entity);
                                this.allEntities.add(entity);
                                location.getExtent().spawnEntity(entity, this.entityCause);

                                // Entities should be invulnerable for the duration of time that they'll be alive.
                                entity.offer(Keys.INVULNERABILITY_TICKS, LIFE_TICKS);

                                // Entities should despawn if players move far enough away.
                                entity.offer(Keys.PERSISTS, false);
                            });
                }
            }
        }

        // Schedule cleanup for later.
        Sponge.getScheduler().createTaskBuilder().delayTicks(LIFE_TICKS).execute(() -> {
            this.removeEntities(ourEntities);
        }).submit(this.plugin);
    }

    @Listener
    public void onServerStoppingEvent(GameStoppingServerEvent event) {
        // Remove all our spawned entities when the server stops.
        this.removeEntities(this.allEntities);
    }

    /**
     * Removes a collection of entities from the world they're in.
     *
     * @param entities the entities to remove
     */
    private void removeEntities(Collection<Entity> entities) {
        entities.forEach(entity -> {
            if (entity.isLoaded()) {
                entity.getWorld().spawnParticles(this.effect, entity.getLocation().getPosition());
                entity.remove();
            }
        });
    }
}
