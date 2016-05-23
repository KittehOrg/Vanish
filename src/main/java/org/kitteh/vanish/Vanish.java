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

import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

/**
 * Spiritual successor to VanishNoPacket, for Sponge.
 */
@Plugin(id = "vanish", name = "Vanish", version = "4.0.4-SNAPSHOT")
public class Vanish {
    public static final String PERMISSION_VANISH = "vanish.vanish";

    public static final String PERMISSION_EFFECTS_BATS = "vanish.effects.bats";
    public static final String PERMISSION_EFFECTS_CATS = "vanish.effects.cats";

    /**
     * Sometimes, people make mistakes like trying to run a plugin.
     */
    public static class Main {
        /**
         * Let's tell them just how wrong they are.
         *
         * @param args maybe they'll be argumentative about it later
         */
        public static void main(String[] args) {
            System.out.println();
            System.out.println("    Meow!  :3");
            System.out.println();
            System.out.println();
            System.out.println("This is a Sponge plugin. Not something to be run by itself!");
            System.out.println();
            System.out.println("    To use this plugin, you need to have Sponge.");
            System.out.println();
            System.out.println("     https://www.spongepowered.org");
            System.out.println();
            System.out.println();
        }
    }

    @Inject
    private Game game;

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        CommandSpec vanishCommandSpec = CommandSpec.builder().permission(PERMISSION_VANISH).executor(new VanishCommand(this)).build();
        this.game.getCommandManager().register(this, vanishCommandSpec, "vanish", "v");
    }
}
