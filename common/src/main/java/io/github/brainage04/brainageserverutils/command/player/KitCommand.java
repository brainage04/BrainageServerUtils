package io.github.brainage04.brainageserverutils.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.brainage04.brainageserverutils.command.core.PlayerTargets;
import io.github.brainage04.brainageserverutils.enchantment.MaxEnchantment;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantment;

/// `/kit <preset> [targets]`: gives unbreakable, max-enchanted gear. Enchantment conflicts follow the configured
/// rankings, exactly as `/maxenchant` does without explicit preferences.
public final class KitCommand {
    private enum Kit {
        NETHERITE(() -> stacks(
                Items.NETHERITE_HELMET,
                Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS,
                Items.NETHERITE_BOOTS,
                Items.NETHERITE_SWORD,
                Items.NETHERITE_SPEAR,
                Items.MACE,
                Items.NETHERITE_PICKAXE,
                Items.NETHERITE_AXE,
                Items.NETHERITE_SHOVEL,
                Items.SHIELD
        )),
        RANGED(() -> Stream.concat(
                stacks(Items.BOW, Items.CROSSBOW, Items.TRIDENT).stream(),
                Stream.of(new ItemStack(Items.ARROW, Items.ARROW.getDefaultMaxStackSize()))
        ).toList()),
        ELYTRA(() -> List.of(new ItemStack(Items.ELYTRA), fireworks()));

        private final Supplier<List<ItemStack>> contents;

        Kit(Supplier<List<ItemStack>> contents) {
            this.contents = contents;
        }

        String commandName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private KitCommand() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("kit")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Kit kit : Kit.values()) {
            root.then(PlayerTargets.command(kit.commandName(), (source, targets) -> execute(source, targets, kit)));
        }
        dispatcher.register(root);
    }

    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets, Kit kit)
            throws CommandSyntaxException {
        HolderLookup.RegistryLookup<Enchantment> enchantments = source.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<List<Identifier>> rankings = MaxEnchantCommand.loadRankings(enchantments);
        for (ServerPlayer player : targets) {
            for (ItemStack stack : kit.contents.get()) {
                if (stack.isDamageableItem()) {
                    MaxEnchantment.apply(stack, enchantments, rankings, List.of());
                    stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
                }
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
        PlayerTargets.sendSuccess(source, "Gave the " + kit.commandName() + " kit to", targets);
        return targets.size();
    }

    private static List<ItemStack> stacks(Item... items) {
        return Stream.of(items).map(ItemStack::new).toList();
    }

    /// Flight-duration-3 rockets without explosions, so boosting an elytra never damages the wearer.
    private static ItemStack fireworks() {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, Items.FIREWORK_ROCKET.getDefaultMaxStackSize());
        stack.set(DataComponents.FIREWORKS, new Fireworks(3, List.of()));
        return stack;
    }
}
