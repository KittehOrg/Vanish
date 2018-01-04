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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;

/**
 * Vanish!
 */
class VanishCommand implements CommandExecutor {

    private final VanishEntitySpawnEffects entitySpawnEffects;

    VanishCommand(Vanish plugin) {
        this.entitySpawnEffects = new VanishEntitySpawnEffects(plugin);
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource commandSource, @Nonnull CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "Vanish currently can only be used by players"));
        }
        Player player = (Player) commandSource;
        boolean wasVisible = player.get(Keys.VANISH).orElse(false);
        player.offer(Keys.INVISIBLE, !wasVisible);
        player.offer(Keys.VANISH, !wasVisible);
        player.offer(Keys.VANISH_IGNORES_COLLISION, !wasVisible);
        player.offer(Keys.VANISH_PREVENTS_TARGETING, !wasVisible);

        this.entitySpawnEffects.applySpawnEffect(player);

        player.sendMessage(Text.of(TextColors.AQUA, "You are now " + (wasVisible ? "" : "in") + "visible"));
        return CommandResult.success();
    }
}
