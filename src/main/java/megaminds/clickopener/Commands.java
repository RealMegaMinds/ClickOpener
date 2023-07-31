package megaminds.clickopener;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import megaminds.clickopener.api.ClickType;
import megaminds.clickopener.impl.ArgumentChecker;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

//TODO Add commands for blacklist and other config values
public class Commands {
	private static final SuggestionProvider<ServerCommandSource> BLOCK_ITEM_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(Registries.ITEM.stream().filter(BlockItem.class::isInstance).map(Registries.ITEM::getId), builder);
	private static final SuggestionProvider<ServerCommandSource> ITEM_TAG_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(Registries.ITEM.streamTags().map(TagKey::id), builder);
	private static final SuggestionProvider<ServerCommandSource> BLOCK_TAG_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(Registries.BLOCK.streamTags().map(TagKey::id), builder);
	private static final SuggestionProvider<ServerCommandSource> LISTED_ITEMS_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(ClickOpenerMod.CONFIG.getIdList(), builder);
	private static final SuggestionProvider<ServerCommandSource> CLICK_TYPE_SUGGESTIONS = (context, builder) -> CommandSource.suggestMatching(Arrays.stream(ClickType.values()).filter(c->!ClickType.OTHER.equals(c)).map(Enum::name), builder);
	private static final String ID = "id";
	private static final String CLICK_TYPE = "clickType";

	//Really the feedback should be made translatable
	private static final String ADDED_TO_LIST = " added to list.";
	private static final String REMOVED_FROM_LIST = " removed from list.";

	private Commands() {}

	@SuppressWarnings({"java:S1172", "unused"})
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
		var serverRoot = literal(ClickOpenerMod.MODID)
				.requires(s->s.hasPermissionLevel(4));

		var reload = literal("reload")
				.executes(Commands::reload);

		var add = literal("add")
				.then(literal("item")
						.then(argument(ID, identifier())
								.suggests(BLOCK_ITEM_SUGGESTIONS)
								.executes(Commands::addItem))
						.executes(Commands::addItem))
				.then(literal("blocktag")
						.then(argument(ID, identifier())
								.suggests(BLOCK_TAG_SUGGESTIONS)
								.executes(Commands::addBlockTag)))
				.then(literal("itemtag")
						.then(argument(ID, identifier())
								.suggests(ITEM_TAG_SUGGESTIONS)
								.executes(Commands::addItemTag)));

		var remove = literal("remove")
				.then(literal("item")
						.then(argument(ID, identifier())
								.suggests(LISTED_ITEMS_SUGGESTIONS)
								.executes(Commands::remove)))
				.then(literal("blocktag")
						.then(argument(ID, identifier())
								.suggests(BLOCK_TAG_SUGGESTIONS)
								.executes(Commands::removeBlockTag)))
				.then(literal("itemtag")
						.then(argument(ID, identifier())
								.suggests(ITEM_TAG_SUGGESTIONS)
								.executes(Commands::removeItemTag)));

		serverRoot.then(reload)
		.then(add)
		.then(remove);		

		var playerRoot = literal(ClickOpenerMod.MODID+"_player");

		var clickType = literal(CLICK_TYPE)
				.then(argument(CLICK_TYPE, word())
						.suggests(CLICK_TYPE_SUGGESTIONS)
						.executes(Commands::clickType));

		playerRoot.then(clickType);

		dispatcher.register(serverRoot);
		dispatcher.register(playerRoot);
	}

	private static int clickType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var type = ClickType.tryValueOf(getString(context, CLICK_TYPE), ClickType.OTHER);
		if (ClickType.OTHER.equals(type)) {
			context.getSource().sendError(Text.of("Invalid ClickType"));
			return 0;
		}

		ClickOpenerMod.PLAYER_CONFIGS.setClickType(context.getSource().getPlayerOrThrow(), type);
		context.getSource().sendFeedback(() -> Text.of("ClickType set to "+type), false);
		return 1;
	}

	private static int reload(CommandContext<ServerCommandSource> context) {
		ClickOpenerMod.CONFIG.reload();
		context.getSource().sendFeedback(() -> Text.of("ClickOpener Config Reloaded"), false);
		return 1;
	}

	private static int addItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var item = ArgumentChecker.hasArgument(context, ID) ? getIdentifier(context, ID) : Registries.ITEM.getId(context.getSource().getPlayerOrThrow().getMainHandStack().getItem());
		if (item.equals(Registries.ITEM.getDefaultId()) || !(Registries.ITEM.get(item) instanceof BlockItem)) {
			context.getSource().sendError(Text.of("Invalid Item"));
			return 0;
		}

		ClickOpenerMod.CONFIG.addBlockItem(item, true);
		context.getSource().sendFeedback(() -> Text.of(item+ADDED_TO_LIST), false);
		return 1;
	}

	private static int addBlockTag(CommandContext<ServerCommandSource> context) {
		var tag = getIdentifier(context, ID);
		ClickOpenerMod.CONFIG.addBlockTag(tag);
		context.getSource().sendFeedback(() -> Text.of("#"+tag+ADDED_TO_LIST), false);
		return 1;
	}

	private static int addItemTag(CommandContext<ServerCommandSource> context) {
		var tag = getIdentifier(context, ID);
		ClickOpenerMod.CONFIG.addItemTag(tag);
		context.getSource().sendFeedback(() -> Text.of("#"+tag+ADDED_TO_LIST), false);
		return 1;
	}

	private static int remove(CommandContext<ServerCommandSource> context) {
		var item = getIdentifier(context, ID);
		ClickOpenerMod.CONFIG.removeBlockItem(item, true);
		context.getSource().sendFeedback(() -> Text.of(item+REMOVED_FROM_LIST), false);
		return 1;
	}

	private static int removeBlockTag(CommandContext<ServerCommandSource> context) {
		var tag = getIdentifier(context, ID);
		ClickOpenerMod.CONFIG.removeBlockTag(tag);
		context.getSource().sendFeedback(() -> Text.of("#"+tag+REMOVED_FROM_LIST), false);
		return 1;
	}

	private static int removeItemTag(CommandContext<ServerCommandSource> context) {
		var tag = getIdentifier(context, ID);
		ClickOpenerMod.CONFIG.removeItemTag(tag);
		context.getSource().sendFeedback(() -> Text.of("#"+tag+REMOVED_FROM_LIST), false);
		return 1;
	}
}
