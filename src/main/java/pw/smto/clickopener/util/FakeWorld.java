package pw.smto.clickopener.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.component.type.MapIdComponent;
import net.minecraft.world.dimension.PortalForcer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.datafixers.util.Pair;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.longs.LongSet;
import pw.smto.clickopener.impl.BlockOpenContext;
import pw.smto.clickopener.impl.BlockScreenOpener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.tick.TickPriority;
import net.minecraft.world.tick.WorldTickScheduler;

@SuppressWarnings({"deprecation", "java:S5803"})
public class FakeWorld extends ServerWorld {
	private static final long RANDOM_OFFSET = UnsafeAccess.fieldOffset(World.class, FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary", "net.minecraft.class_1937", "field_9229", "Lnet/minecraft/class_5819;"));
	private static final long CONTEXT_OFFSET = UnsafeAccess.fieldOffset(FakeWorld.class, "context");

	private final BlockOpenContext context;

	private FakeWorld() {
		super(null, null, null, null, null, null, null, false, 0, null, false, null);
		throw new IllegalStateException("FakeWorld constructor should not be used.");
	}

	@SuppressWarnings("resource")
	public static FakeWorld create(BlockOpenContext context) {
		try {
			var fakeWorld = (FakeWorld) UnsafeAccess.UNSAFE.allocateInstance(FakeWorld.class);
			//set public fields from world
			UnsafeAccess.UNSAFE.putObject(fakeWorld, RANDOM_OFFSET, context.player().getServerWorld().random);
			//isClient is false by default

			UnsafeAccess.UNSAFE.putObject(fakeWorld, CONTEXT_OFFSET, context);
			return fakeWorld;
		} catch (InstantiationException e) {
			throw new ItemOpenException("Failed to allocate instance of FakeWorld.", e);
		}
	}

	private ServerWorld delegate() {
		return context.player().getServerWorld();
	}

	private <T> T ifHandlesOrElse(BlockPos pos, Supplier<T> ifHandles, Supplier<T> orElse) {
		return context.handles(pos) ? ifHandles.get() : orElse.get();
	}

	private void ifHandlesOrElse(BlockPos pos, Runnable ifHandles, Runnable orElse) {
		if (context.handles(pos)) {
			ifHandles.run();
		} else {
			orElse.run();
		}
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
		return ifHandlesOrElse(pos, () -> {
			context.setBlockState(state);
			return true;
		}, () -> delegate().setBlockState(pos, state, flags));
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
		return ifHandlesOrElse(pos, () -> {
			context.setBlockState(state);
			return true;
		}, () -> delegate().setBlockState(pos, state, flags, maxUpdateDepth));
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state) {
		return ifHandlesOrElse(pos, () -> {
			context.setBlockState(state);
			return true;
		}, () -> delegate().setBlockState(pos, state));
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean move) {
		return ifHandlesOrElse(pos, () -> {
			context.setBlockState(Blocks.AIR.getDefaultState());
			return true;
		}, () -> delegate().removeBlock(pos, move));
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return ifHandlesOrElse(pos, context::getBlockState, () -> delegate().getBlockState(pos));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
		return ifHandlesOrElse(pos, () -> (Optional<T>) Optional.ofNullable(context.getBlockEntity()).filter(be -> be.getType() == type), () -> delegate().getBlockEntity(pos, type));
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return ifHandlesOrElse(pos, context::getBlockEntity, () -> delegate().getBlockEntity(pos));
	}

	@Override
	public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
		ifHandlesOrElse(pos, () -> context.getBlockState().onSyncedBlockEvent(this, pos, type, data), () -> delegate().addSyncedBlockEvent(pos, block, type, data));
	}

	@Override
	protected EntityLookup<Entity> getEntityLookup() {
		return null;
	}

	@Override
	public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
		if (context.handles(pos)) return;
		delegate().replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth);
	}

	@Override
	public void updateComparators(BlockPos pos, Block block) {
		if (context.handles(pos)) return;
		delegate().updateComparators(pos, block);
	}

	@Override
	public void markDirty(BlockPos pos) {
		ifHandlesOrElse(pos, () -> context.openerConsumer(BlockScreenOpener::onMarkDirty), () -> delegate().markDirty(pos));
	}

	@Override
	public boolean isSpaceEmpty(Box box) {
		return true;
	}

	/*
	 * Delegated methods
	 */

	@Override
	public float getMoonSize() {
		return delegate().getMoonSize();
	}

	@Override
	public boolean isValidForSetBlock(BlockPos pos) {
		return delegate().isValidForSetBlock(pos);
	}

	@Override
	public void spawnEntityAndPassengers(Entity entity) {
		delegate().spawnEntityAndPassengers(entity);
	}

	@Override
	public int getStrongRedstonePower(BlockPos pos, Direction direction) {
		return delegate().getStrongRedstonePower(pos, direction);
	}

	@Override
	public float getSkyAngle(float tickDelta) {
		return delegate().getSkyAngle(tickDelta);
	}

	@Override
	public void setCurrentlyGeneratingStructureName(Supplier<String> structureName) {
		delegate().setCurrentlyGeneratingStructureName(structureName);
	}

	@Override
	public int getLightLevel(LightType type, BlockPos pos) {
		return delegate().getLightLevel(type, pos);
	}

	@Override
	public int getReceivedStrongRedstonePower(BlockPos pos) {
		return delegate().getReceivedStrongRedstonePower(pos);
	}

	@Override
	public int getMoonPhase() {
		return delegate().getMoonPhase();
	}

	@Override
	public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
		return delegate().getBaseLightLevel(pos, ambientDarkness);
	}

	@Override
	public int getTopY() {
		return delegate().getTopY();
	}

	@Override
	public boolean isSkyVisible(BlockPos pos) {
		return delegate().isSkyVisible(pos);
	}

	@Override
	public List<VoxelShape> getEntityCollisions(Entity entity, Box box) {
		return delegate().getEntityCollisions(entity, box);
	}

	@Override
	public int countVerticalSections() {
		return delegate().countVerticalSections();
	}

	@Override
	public boolean doesNotIntersectEntities(Entity except, VoxelShape shape) {
		return delegate().doesNotIntersectEntities(except, shape);
	}

	@Override
	public long getLunarTime() {
		return delegate().getLunarTime();
	}

	@Override
	public boolean canPlace(BlockState state, BlockPos pos, ShapeContext context) {
		return delegate().canPlace(state, pos, context);
	}

	@Override
	public BlockPos getTopPosition(Type heightmap, BlockPos pos) {
		return delegate().getTopPosition(heightmap, pos);
	}

	@Override
	public @Nullable Object getBlockEntityRenderData(BlockPos pos) {
		return delegate().getBlockEntityRenderData(pos);
	}

	@Override
	public int getBottomSectionCoord() {
		return delegate().getBottomSectionCoord();
	}

	@Override
	public int getEmittedRedstonePower(BlockPos pos, Direction direction, boolean onlyFromGate) {
		return delegate().getEmittedRedstonePower(pos, direction, onlyFromGate);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByClass(Class<T> entityClass, Box box,
			Predicate<? super T> predicate) {
		return delegate().getEntitiesByClass(entityClass, box, predicate);
	}

	@Override
	public boolean doesNotIntersectEntities(Entity entity) {
		return delegate().doesNotIntersectEntities(entity);
	}

	@Override
	public boolean isSpaceEmpty(Entity entity) {
		return delegate().isSpaceEmpty(entity);
	}

	@Override
	public void scheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority) {
		delegate().scheduleBlockTick(pos, block, delay, priority);
	}

	@Override
	public RegistryEntry<Biome> getBiome(BlockPos pos) {
		return delegate().getBiome(pos);
	}

	@Override
	public int getTopSectionCoord() {
		return delegate().getTopSectionCoord();
	}

	@Override
	public boolean isSpaceEmpty(Entity entity, Box box) {
		return delegate().isSpaceEmpty(entity, box);
	}

	@Override
	public Stream<BlockState> getStatesInBoxIfLoaded(Box box) {
		return delegate().getStatesInBoxIfLoaded(box);
	}

	@Override
	public void scheduleBlockTick(BlockPos pos, Block block, int delay) {
		delegate().scheduleBlockTick(pos, block, delay);
	}

	@Override
	public int getLuminance(BlockPos pos) {
		return delegate().getLuminance(pos);
	}

	@Override
	public boolean isEmittingRedstonePower(BlockPos pos, Direction direction) {
		return delegate().isEmittingRedstonePower(pos, direction);
	}

	@Override
	public int getMaxLightLevel() {
		return delegate().getMaxLightLevel();
	}

	@Override
	public List<Entity> getOtherEntities(Entity except, Box box) {
		return delegate().getOtherEntities(except, box);
	}

	@Override
	public Stream<BlockState> getStatesInBox(Box box) {
		return delegate().getStatesInBox(box);
	}

	@Override
	public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
		delegate().scheduleFluidTick(pos, fluid, delay, priority);
	}

	@Override
	public int getEmittedRedstonePower(BlockPos pos, Direction direction) {
		return delegate().getEmittedRedstonePower(pos, direction);
	}

	@Override
	public BlockHitResult raycast(BlockStateRaycastContext context) {
		return delegate().raycast(context);
	}

	@Override
	public boolean isOutOfHeightLimit(BlockPos pos) {
		return delegate().isOutOfHeightLimit(pos);
	}

	@Override
	public int hashCode() {
		return delegate().hashCode();
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return delegate().getColor(pos, colorResolver);
	}

	@Override
	public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay) {
		delegate().scheduleFluidTick(pos, fluid, delay);
	}

	@Override
	public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
		return delegate().getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
	}

	@Override
	public Iterable<VoxelShape> getCollisions(Entity entity, Box box) {
		return delegate().getCollisions(entity, box);
	}

	@Override
	public boolean isReceivingRedstonePower(BlockPos pos) {
		return delegate().isReceivingRedstonePower(pos);
	}

	@Override
	public boolean isOutOfHeightLimit(int y) {
		return delegate().isOutOfHeightLimit(y);
	}

	@Override
	public Difficulty getDifficulty() {
		return delegate().getDifficulty();
	}

	@Override
	public Iterable<VoxelShape> getBlockCollisions(Entity entity, Box box) {
		return delegate().getBlockCollisions(entity, box);
	}

	@Override
	public boolean isChunkLoaded(int chunkX, int chunkZ) {
		return delegate().isChunkLoaded(chunkX, chunkZ);
	}

	@Override
	public BlockHitResult raycast(RaycastContext context) {
		return delegate().raycast(context);
	}

	@Override
	public boolean hasBiomes() {
		return delegate().hasBiomes();
	}

	@Override
	public int getSectionIndex(int y) {
		return delegate().getSectionIndex(y);
	}

	@Override
	public int getReceivedRedstonePower(BlockPos pos) {
		return delegate().getReceivedRedstonePower(pos);
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean drop) {
		return delegate().breakBlock(pos, drop);
	}

	@Override
	public boolean canCollide(Entity entity, Box box) {
		return delegate().canCollide(entity, box);
	}

	@Override
	public int getBottomY() {
		return delegate().getBottomY();
	}

	@Override
	public int getHeight() {
		return delegate().getHeight();
	}

	@Override
	public void playSound(PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category) {
		delegate().playSound(except, pos, sound, category);
	}

	@Override
	public int sectionCoordToIndex(int coord) {
		return delegate().sectionCoordToIndex(coord);
	}

	@Override
	public @UnknownNullability RegistryEntry<Biome> getBiomeFabric(BlockPos pos) {
		return delegate().getBiomeFabric(pos);
	}

	@Override
	public <T extends Entity> List<T> getNonSpectatingEntities(Class<T> entityClass, Box box) {
		return delegate().getNonSpectatingEntities(entityClass, box);
	}

	@Override
	public Optional<BlockPos> findSupportingBlockPos(Entity entity, Box box) {
		return delegate().findSupportingBlockPos(entity, box);
	}

	@Override
	public int sectionIndexToCoord(int index) {
		return delegate().sectionIndexToCoord(index);
	}

	@Override
	public boolean isAir(BlockPos pos) {
		return delegate().isAir(pos);
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean drop, Entity breakingEntity) {
		return delegate().breakBlock(pos, drop, breakingEntity);
	}

	@Override
	public boolean isSkyVisibleAllowingSea(BlockPos pos) {
		return delegate().isSkyVisibleAllowingSea(pos);
	}

	@Override
	public void syncWorldEvent(int eventId, BlockPos pos, int data) {
		delegate().syncWorldEvent(eventId, pos, data);
	}

	@Override
	public boolean equals(Object obj) {
		return delegate().equals(obj);
	}

	@Override
	public void emitGameEvent(Entity entity, RegistryEntry<GameEvent> event, Vec3d pos) {
		delegate().emitGameEvent(entity, event, pos);
	}

	@Override
	public void emitGameEvent(Entity entity, RegistryEntry<GameEvent> event, BlockPos pos) {
		delegate().emitGameEvent(entity, event, pos);
	}

	@Override
	public Optional<Vec3d> findClosestCollision(Entity entity, VoxelShape shape, Vec3d target, double x, double y,
			double z) {
		return delegate().findClosestCollision(entity, shape, target, x, y, z);
	}

	@Override
	public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance,
			Predicate<Entity> targetPredicate) {
		return delegate().getClosestPlayer(x, y, z, maxDistance, targetPredicate);
	}

	@Override
	public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
		return delegate().raycastBlock(start, end, pos, shape, state);
	}

	@Override
	public void emitGameEvent(RegistryEntry<GameEvent> event, BlockPos pos, Emitter emitter) {
		delegate().emitGameEvent(event, pos, emitter);
	}

	@Override
	public float getPhototaxisFavor(BlockPos pos) {
		return delegate().getPhototaxisFavor(pos);
	}

	@Override
	public float getBrightness(BlockPos pos) {
		return delegate().getBrightness(pos);
	}

	@Override
	public double getDismountHeight(VoxelShape blockCollisionShape,
			Supplier<VoxelShape> belowBlockCollisionShapeGetter) {
		return delegate().getDismountHeight(blockCollisionShape, belowBlockCollisionShapeGetter);
	}

	@Override
	public PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
		return delegate().getClosestPlayer(entity, maxDistance);
	}

	@Override
	public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
		return delegate().getClosestPlayer(x, y, z, maxDistance, ignoreCreative);
	}

	@Override
	public Chunk getChunk(BlockPos pos) {
		return delegate().getChunk(pos);
	}

	@Override
	public double getDismountHeight(BlockPos pos) {
		return delegate().getDismountHeight(pos);
	}

	@Override
	public boolean isPlayerInRange(double x, double y, double z, double range) {
		return delegate().isPlayerInRange(x, y, z, range);
	}

	@Override
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status) {
		return delegate().getChunk(chunkX, chunkZ, status);
	}

	@Override
	public boolean isWater(BlockPos pos) {
		return delegate().isWater(pos);
	}

	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
		return delegate().getClosestPlayer(targetPredicate, entity);
	}

	@Override
	public boolean containsFluid(Box box) {
		return delegate().containsFluid(box);
	}

	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y,
			double z) {
		return delegate().getClosestPlayer(targetPredicate, entity, x, y, z);
	}

	@Override
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
		return delegate().getClosestPlayer(targetPredicate, x, y, z);
	}

	@Override
	public boolean isClient() {
		return delegate().isClient();
	}

	@Override
	public <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClass, TargetPredicate targetPredicate,
			LivingEntity entity, double x, double y, double z, Box box) {
		return delegate().getClosestEntity(entityClass, targetPredicate, entity, x, y, z, box);
	}

	@Override
	public boolean isInBuildLimit(BlockPos pos) {
		return delegate().isInBuildLimit(pos);
	}

	@Override
	public int getLightLevel(BlockPos pos) {
		return delegate().getLightLevel(pos);
	}

	@Override
	public int getLightLevel(BlockPos pos, int ambientDarkness) {
		return delegate().getLightLevel(pos, ambientDarkness);
	}

	@Override
	public <T extends LivingEntity> T getClosestEntity(List<? extends T> entityList, TargetPredicate targetPredicate,
			LivingEntity entity, double x, double y, double z) {
		return delegate().getClosestEntity(entityList, targetPredicate, entity, x, y, z);
	}

	@Override
	public boolean isPosLoaded(int x, int z) {
		return delegate().isPosLoaded(x, z);
	}

	@Override
	public boolean isChunkLoaded(BlockPos pos) {
		return delegate().isChunkLoaded(pos);
	}

	@Override
	public boolean isRegionLoaded(BlockPos min, BlockPos max) {
		return delegate().isRegionLoaded(min, max);
	}

	@Override
	public List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
		return delegate().getPlayers(targetPredicate, entity, box);
	}

	@Override
	public boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		return delegate().isRegionLoaded(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public <T extends LivingEntity> List<T> getTargets(Class<T> entityClass, TargetPredicate targetPredicate,
			LivingEntity targetingEntity, Box box) {
		return delegate().getTargets(entityClass, targetPredicate, targetingEntity, box);
	}

	@Override
	public boolean isRegionLoaded(int minX, int minZ, int maxX, int maxZ) {
		return delegate().isRegionLoaded(minX, minZ, maxX, maxZ);
	}

	@Override
	public PlayerEntity getPlayerByUuid(UUID uuid) {
		return delegate().getPlayerByUuid(uuid);
	}

	@Override
	public WorldChunk getWorldChunk(BlockPos pos) {
		return delegate().getWorldChunk(pos);
	}

	@Override
	public <T> RegistryWrapper<T> createCommandRegistryWrapper(
			RegistryKey<? extends Registry<? extends T>> registryRef) {
		return delegate().createCommandRegistryWrapper(registryRef);
	}

	@Override
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
		return delegate().getChunk(chunkX, chunkZ, leastStatus, create);
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean drop, Entity breakingEntity, int maxUpdateDepth) {
		return delegate().breakBlock(pos, drop, breakingEntity, maxUpdateDepth);
	}

	@Override
	public void setEnderDragonFight(EnderDragonFight enderDragonFight) {
		delegate().setEnderDragonFight(enderDragonFight);
	}

	@Override
	public void addBlockBreakParticles(BlockPos pos, BlockState state) {
		delegate().addBlockBreakParticles(pos, state);
	}

	@Override
	public void setWeather(int clearDuration, int rainDuration, boolean raining, boolean thundering) {
		delegate().setWeather(clearDuration, rainDuration, raining, thundering);
	}

	@Override
	public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
		delegate().scheduleBlockRerenderIfNeeded(pos, old, updated);
	}

	@Override
	public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
		return delegate().getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
	}

	@Override
	public StructureAccessor getStructureAccessor() {
		return delegate().getStructureAccessor();
	}

	@Override
	public void tick(BooleanSupplier shouldKeepTicking) {
		delegate().tick(shouldKeepTicking);
	}

	@Override
	public int getTopY(Type heightmap, int x, int z) {
		return delegate().getTopY(heightmap, x, z);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return delegate().getLightingProvider();
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return delegate().getFluidState(pos);
	}

	@Override
	public boolean isDay() {
		return delegate().isDay();
	}

	@Override
	public boolean isNight() {
		return delegate().isNight();
	}

	@Override
	public void playSound(Entity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume,
			float pitch) {
		delegate().playSound(except, pos, sound, category, volume, pitch);
	}

	@Override
	public void playSound(PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume,
			float pitch) {
		delegate().playSound(except, pos, sound, category, volume, pitch);
	}

	@Override
	public void playSound(PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category,
			float volume, float pitch, long seed) {
		delegate().playSound(except, x, y, z, sound, category, volume, pitch, seed);
	}

	@Override
	public void playSound(PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category,
			float volume, float pitch) {
		delegate().playSound(except, x, y, z, sound, category, volume, pitch);
	}

	@Override
	public boolean shouldTickBlocksInChunk(long chunkPos) {
		return delegate().shouldTickBlocksInChunk(chunkPos);
	}

	@Override
	public void playSoundFromEntity(PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category,
			float volume, float pitch) {
		delegate().playSoundFromEntity(except, entity, sound, category, volume, pitch);
	}

	@Override
	public void playSoundAtBlockCenter(BlockPos pos, SoundEvent sound, SoundCategory category, float volume,
			float pitch, boolean useDistance) {
		delegate().playSoundAtBlockCenter(pos, sound, category, volume, pitch, useDistance);
	}

	@Override
	public void setTimeOfDay(long timeOfDay) {
		delegate().setTimeOfDay(timeOfDay);
	}

	@Override
	public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume,
			float pitch, boolean useDistance) {
		delegate().playSound(x, y, z, sound, category, volume, pitch, useDistance);
	}

	@Override
	public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY,
			double velocityZ) {
		delegate().addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z,
			double velocityX, double velocityY, double velocityZ) {
		delegate().addParticle(parameters, alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX,
			double velocityY, double velocityZ) {
		delegate().addImportantParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z,
			double velocityX, double velocityY, double velocityZ) {
		delegate().addImportantParticle(parameters, alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public void tickSpawners(boolean spawnMonsters, boolean spawnAnimals) {
		delegate().tickSpawners(spawnMonsters, spawnAnimals);
	}

	@Override
	public float getSkyAngleRadians(float tickDelta) {
		return delegate().getSkyAngleRadians(tickDelta);
	}

	@Override
	public void addBlockEntityTicker(BlockEntityTickInvoker ticker) {
		delegate().addBlockEntityTicker(ticker);
	}

	@Override
	public void tickChunk(WorldChunk chunk, int randomTickSpeed) {
		delegate().tickChunk(chunk, randomTickSpeed);
	}

	@Override
	public <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity) {
		delegate().tickEntity(tickConsumer, entity);
	}

	@Override
	public boolean shouldUpdatePostDeath(Entity entity) {
		return delegate().shouldUpdatePostDeath(entity);
	}

	@Override
	public boolean shouldTickBlockPos(BlockPos pos) {
		return delegate().shouldTickBlockPos(pos);
	}

	@Override
	public Explosion createExplosion(Entity entity, double x, double y, double z, float power,
			ExplosionSourceType explosionSourceType) {
		return delegate().createExplosion(entity, x, y, z, power, explosionSourceType);
	}

	@Override
	public Explosion createExplosion(Entity entity, double x, double y, double z, float power, boolean createFire,
			ExplosionSourceType explosionSourceType) {
		return delegate().createExplosion(entity, x, y, z, power, createFire, explosionSourceType);
	}

	@Override
	public Explosion createExplosion(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, Vec3d pos,
			float power, boolean createFire, ExplosionSourceType explosionSourceType) {
		return delegate().createExplosion(entity, damageSource, behavior, pos, power, createFire, explosionSourceType);
	}

	//@Override
	public Explosion createExplosion(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x,
			double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType,
			boolean particles) {
		return delegate().createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType);
	}

	@Override
	public void addBlockEntity(BlockEntity blockEntity) {
		delegate().addBlockEntity(blockEntity);
	}

	@Override
	public boolean isInBlockTick() {
		return delegate().isInBlockTick();
	}

	@Override
	public boolean isSleepingEnabled() {
		return delegate().isSleepingEnabled();
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		delegate().removeBlockEntity(pos);
	}

	@Override
	public boolean canSetBlock(BlockPos pos) {
		return delegate().canSetBlock(pos);
	}

	@Override
	public boolean isDirectionSolid(BlockPos pos, Entity entity, Direction direction) {
		return delegate().isDirectionSolid(pos, entity, direction);
	}

	@Override
	public void updateSleepingPlayers() {
		delegate().updateSleepingPlayers();
	}

	@Override
	public boolean isTopSolid(BlockPos pos, Entity entity) {
		return delegate().isTopSolid(pos, entity);
	}

	@Override
	public void calculateAmbientDarkness() {
		delegate().calculateAmbientDarkness();
	}

	@Override
	public void setMobSpawnOptions(boolean spawnMonsters, boolean spawnAnimals) {
		delegate().setMobSpawnOptions(spawnMonsters, spawnAnimals);
	}

	@Override
	public BlockPos getSpawnPos() {
		return delegate().getSpawnPos();
	}

	@Override
	public float getSpawnAngle() {
		return delegate().getSpawnAngle();
	}

	@Override
	public BlockView getChunkAsView(int chunkX, int chunkZ) {
		return delegate().getChunkAsView(chunkX, chunkZ);
	}

	@Override
	public List<Entity> getOtherEntities(Entity except, Box box, Predicate<? super Entity> predicate) {
		return delegate().getOtherEntities(except, box, predicate);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box,
			Predicate<? super T> predicate) {
		return delegate().getEntitiesByType(filter, box, predicate);
	}

	@Override
	public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Box box,
			Predicate<? super T> predicate, List<? super T> result) {
		delegate().collectEntitiesByType(filter, box, predicate, result);
	}

	@Override
	public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Box box,
			Predicate<? super T> predicate, List<? super T> result, int limit) {
		delegate().collectEntitiesByType(filter, box, predicate, result, limit);
	}

	@Override
	public void resetIdleTimeout() {
		delegate().resetIdleTimeout();
	}

	@Override
	public void tickEntity(Entity entity) {
		delegate().tickEntity(entity);
	}

	@Override
	public int getSeaLevel() {
		return delegate().getSeaLevel();
	}

	@Override
	public void disconnect() {
		delegate().disconnect();
	}

	@Override
	public long getTime() {
		return delegate().getTime();
	}

	@Override
	public long getTimeOfDay() {
		return delegate().getTimeOfDay();
	}

	@Override
	public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
		return delegate().canPlayerModifyAt(player, pos);
	}

	@Override
	public void save(ProgressListener progressListener, boolean flush, boolean savingDisabled) {
		delegate().save(progressListener, flush, savingDisabled);
	}

	@Override
	public WorldProperties getLevelProperties() {
		return delegate().getLevelProperties();
	}

	@Override
	public GameRules getGameRules() {
		return delegate().getGameRules();
	}

	@Override
	public float getThunderGradient(float delta) {
		return delegate().getThunderGradient(delta);
	}

	@Override
	public <T extends Entity> List<? extends T> getEntitiesByType(TypeFilter<Entity, T> filter,
			Predicate<? super T> predicate) {
		return delegate().getEntitiesByType(filter, predicate);
	}

	@Override
	public void setThunderGradient(float thunderGradient) {
		delegate().setThunderGradient(thunderGradient);
	}

	@Override
	public float getRainGradient(float delta) {
		return delegate().getRainGradient(delta);
	}

	@Override
	public void setRainGradient(float rainGradient) {
		delegate().setRainGradient(rainGradient);
	}

	@Override
	public boolean isThundering() {
		return delegate().isThundering();
	}

	@Override
	public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Predicate<? super T> predicate,
			List<? super T> result) {
		delegate().collectEntitiesByType(filter, predicate, result);
	}

	@Override
	public boolean isRaining() {
		return delegate().isRaining();
	}

	@Override
	public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Predicate<? super T> predicate,
			List<? super T> result, int limit) {
		delegate().collectEntitiesByType(filter, predicate, result, limit);
	}

	@Override
	public boolean hasRain(BlockPos pos) {
		return delegate().hasRain(pos);
	}

	@Override
	public List<? extends EnderDragonEntity> getAliveEnderDragons() {
		return delegate().getAliveEnderDragons();
	}

	@Override
	public List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> predicate) {
		return delegate().getPlayers(predicate);
	}

	@Override
	public CrashReportSection addDetailsToCrashReport(CrashReport report) {
		return delegate().addDetailsToCrashReport(report);
	}

	@Override
	public List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> predicate, int limit) {
		return delegate().getPlayers(predicate, limit);
	}

	@Override
	public ServerPlayerEntity getRandomAlivePlayer() {
		return delegate().getRandomAlivePlayer();
	}

	//@Override
	//public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ,
	//		NbtCompound nbt) {
	//	delegate().addFireworkParticle(x, y, z, velocityX, velocityY, velocityZ, nbt);
	//}

	@Override
	public boolean spawnEntity(Entity entity) {
		return delegate().spawnEntity(entity);
	}

	@Override
	public boolean tryLoadEntity(Entity entity) {
		return delegate().tryLoadEntity(entity);
	}

	@Override
	public void onDimensionChanged(Entity entity) {
		delegate().onDimensionChanged(entity);
	}

	@Override
	public LocalDifficulty getLocalDifficulty(BlockPos pos) {
		return delegate().getLocalDifficulty(pos);
	}

	//@Override
	//public void onPlayerTeleport(ServerPlayerEntity player) {
	//	delegate().onPlayerTeleport(player);
	//}

	@Override
	public int getAmbientDarkness() {
		return delegate().getAmbientDarkness();
	}

	//@Override
	//public void onPlayerChangeDimension(ServerPlayerEntity player) {
	//	delegate().onPlayerChangeDimension(player);
	//}

	@Override
	public void setLightningTicksLeft(int lightningTicksLeft) {
		delegate().setLightningTicksLeft(lightningTicksLeft);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return delegate().getWorldBorder();
	}

	@Override
	public void sendPacket(Packet<?> packet) {
		delegate().sendPacket(packet);
	}

	@Override
	public void onPlayerConnected(ServerPlayerEntity player) {
		delegate().onPlayerConnected(player);
	}

	@Override
	public DimensionType getDimension() {
		return delegate().getDimension();
	}

	//@Override
	//public RegistryKey<DimensionType> getDimensionKey() {
	//	return delegate().getDimensionKey();
	//}

	@Override
	public void onPlayerRespawned(ServerPlayerEntity player) {
		delegate().onPlayerRespawned(player);
	}

	@Override
	public RegistryEntry<DimensionType> getDimensionEntry() {
		return delegate().getDimensionEntry();
	}

	@Override
	public RegistryKey<World> getRegistryKey() {
		return delegate().getRegistryKey();
	}

	@Override
	public Random getRandom() {
		return delegate().getRandom();
	}

	@Override
	public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
		return delegate().testBlockState(pos, state);
	}

	@Override
	public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
		return delegate().testFluidState(pos, state);
	}

	@Override
	public BlockPos getRandomPosInChunk(int x, int y, int z, int i) {
		return delegate().getRandomPosInChunk(x, y, z, i);
	}

	@Override
	public boolean spawnNewEntityAndPassengers(Entity entity) {
		return delegate().spawnNewEntityAndPassengers(entity);
	}

	@Override
	public Profiler getProfiler() {
		return delegate().getProfiler();
	}

	@Override
	public Supplier<Profiler> getProfilerSupplier() {
		return delegate().getProfilerSupplier();
	}

	@Override
	public BiomeAccess getBiomeAccess() {
		return delegate().getBiomeAccess();
	}

	@Override
	public void unloadEntities(WorldChunk chunk) {
		delegate().unloadEntities(chunk);
	}

	@Override
	public void removePlayer(ServerPlayerEntity player, RemovalReason reason) {
		delegate().removePlayer(player, reason);
	}

	@Override
	public long getTickOrder() {
		return delegate().getTickOrder();
	}

	@Override
	public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
		delegate().setBlockBreakingInfo(entityId, pos, progress);
	}

	@Override
	public DynamicRegistryManager getRegistryManager() {
		return delegate().getRegistryManager();
	}

	@Override
	public DamageSources getDamageSources() {
		return delegate().getDamageSources();
	}

	@Override
	public WorldChunk getChunk(int chunkX, int chunkZ) {
		return delegate().getChunk(chunkX, chunkZ);
	}

	@Override
	public void playSound(PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound,
			SoundCategory category, float volume, float pitch, long seed) {
		delegate().playSound(except, x, y, z, sound, category, volume, pitch, seed);
	}

	@Override
	public void playSoundFromEntity(PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound,
			SoundCategory category, float volume, float pitch, long seed) {
		delegate().playSoundFromEntity(except, entity, sound, category, volume, pitch, seed);
	}

	@Override
	public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
		delegate().syncGlobalEvent(eventId, pos, data);
	}

	@Override
	public void syncWorldEvent(PlayerEntity player, int eventId, BlockPos pos, int data) {
		delegate().syncWorldEvent(player, eventId, pos, data);
	}

	@Override
	public int getLogicalHeight() {
		return delegate().getLogicalHeight();
	}

	@Override
	public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, Emitter emitter) {
		delegate().emitGameEvent(event, emitterPos, emitter);
	}

	@Override
	public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		delegate().updateListeners(pos, oldState, newState, flags);
	}

	@Override
	public void updateNeighborsAlways(BlockPos pos, Block sourceBlock) {
		delegate().updateNeighborsAlways(pos, sourceBlock);
	}

	@Override
	public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction) {
		delegate().updateNeighborsExcept(pos, sourceBlock, direction);
	}

	@Override
	public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
		delegate().updateNeighbor(pos, sourceBlock, sourcePos);
	}

	@Override
	public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		delegate().updateNeighbor(state, pos, sourceBlock, sourcePos, notify);
	}

	@Override
	public void sendEntityStatus(Entity entity, byte status) {
		delegate().sendEntityStatus(entity, status);
	}

	@Override
	public void sendEntityDamage(Entity entity, DamageSource damageSource) {
		delegate().sendEntityDamage(entity, damageSource);
	}

	@Override
	public Explosion createExplosion(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x,
			double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
		return delegate().createExplosion(entity, damageSource, behavior, x, y, z, power, createFire,
				explosionSourceType);
	}

	@Override
	public MinecraftServer getServer() {
		return delegate().getServer();
	}

	@Override
	public PortalForcer getPortalForcer() {
		return delegate().getPortalForcer();
	}

	@Override
	public StructureTemplateManager getStructureTemplateManager() {
		return delegate().getStructureTemplateManager();
	}

	@Override
	public <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count,
			double deltaX, double deltaY, double deltaZ, double speed) {
		return delegate().spawnParticles(particle, x, y, z, count, deltaX, deltaY, deltaZ, speed);
	}

	@Override
	public <T extends ParticleEffect> boolean spawnParticles(ServerPlayerEntity viewer, T particle, boolean force,
			double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
		return delegate().spawnParticles(viewer, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed);
	}

	@Override
	public Entity getEntityById(int id) {
		return delegate().getEntityById(id);
	}

	@Override
	public Entity getDragonPart(int id) {
		return delegate().getDragonPart(id);
	}

	@Override
	public Entity getEntity(UUID uuid) {
		return delegate().getEntity(uuid);
	}

	@Override
	public BlockPos locateStructure(TagKey<Structure> structureTag, BlockPos pos, int radius,
			boolean skipReferencedStructures) {
		return delegate().locateStructure(structureTag, pos, radius, skipReferencedStructures);
	}

	@Override
	public Pair<BlockPos, RegistryEntry<Biome>> locateBiome(Predicate<RegistryEntry<Biome>> predicate, BlockPos pos,
			int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval) {
		return delegate().locateBiome(predicate, pos, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval);
	}

	@Override
	public RecipeManager getRecipeManager() {
		return delegate().getRecipeManager();
	}

	@Override
	public boolean isSavingDisabled() {
		return delegate().isSavingDisabled();
	}

	@Override
	public PersistentStateManager getPersistentStateManager() {
		return delegate().getPersistentStateManager();
	}

	@Override
	public MapState getMapState(MapIdComponent id) {
		return delegate().getMapState(id);
	}

	@Override
	public void putMapState(MapIdComponent id, MapState state) {
		delegate().putMapState(id, state);
	}

	@Override
	public MapIdComponent increaseAndGetMapId() {
		return delegate().increaseAndGetMapId();
	}

	@Override
	public void setSpawnPos(BlockPos pos, float angle) {
		delegate().setSpawnPos(pos, angle);
	}

	@Override
	public LongSet getForcedChunks() {
		return delegate().getForcedChunks();
	}

	@Override
	public boolean setChunkForced(int x, int z, boolean forced) {
		return delegate().setChunkForced(x, z, forced);
	}

	@Override
	public List<ServerPlayerEntity> getPlayers() {
		return delegate().getPlayers();
	}

	@Override
	public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
		delegate().onBlockChanged(pos, oldBlock, newBlock);
	}

	@Override
	public PointOfInterestStorage getPointOfInterestStorage() {
		return delegate().getPointOfInterestStorage();
	}

	@Override
	public boolean isNearOccupiedPointOfInterest(BlockPos pos) {
		return delegate().isNearOccupiedPointOfInterest(pos);
	}

	@Override
	public boolean isNearOccupiedPointOfInterest(ChunkSectionPos sectionPos) {
		return delegate().isNearOccupiedPointOfInterest(sectionPos);
	}

	@Override
	public boolean isNearOccupiedPointOfInterest(BlockPos pos, int maxDistance) {
		return delegate().isNearOccupiedPointOfInterest(pos, maxDistance);
	}

	@Override
	public int getOccupiedPointOfInterestDistance(ChunkSectionPos pos) {
		return delegate().getOccupiedPointOfInterestDistance(pos);
	}

	@Override
	public RaidManager getRaidManager() {
		return delegate().getRaidManager();
	}

	@Override
	public Raid getRaidAt(BlockPos pos) {
		return delegate().getRaidAt(pos);
	}

	@Override
	public boolean hasRaidAt(BlockPos pos) {
		return delegate().hasRaidAt(pos);
	}

	@Override
	public void handleInteraction(EntityInteraction interaction, Entity entity, InteractionObserver observer) {
		delegate().handleInteraction(interaction, entity, observer);
	}

	@Override
	public void dump(Path path) throws IOException {
		delegate().dump(path);
	}

	@Override
	public void clearUpdatesInArea(BlockBox box) {
		delegate().clearUpdatesInArea(box);
	}

	@Override
	public void updateNeighbors(BlockPos pos, Block block) {
		delegate().updateNeighbors(pos, block);
	}

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		return delegate().getBrightness(direction, shaded);
	}

	@Override
	public Iterable<Entity> iterateEntities() {
		return delegate().iterateEntities();
	}

	@Override
	public String toString() {
		return delegate().toString();
	}

	@Override
	public boolean isFlat() {
		return delegate().isFlat();
	}

	@Override
	public long getSeed() {
		return delegate().getSeed();
	}

	@Override
	public EnderDragonFight getEnderDragonFight() {
		return delegate().getEnderDragonFight();
	}

	@Override
	public ServerWorld toServerWorld() {
		return delegate().toServerWorld();
	}

	@Override
	public String getDebugString() {
		return delegate().getDebugString();
	}

	@Override
	public void loadEntities(Stream<Entity> entities) {
		delegate().loadEntities(entities);
	}

	@Override
	public void addEntities(Stream<Entity> entities) {
		delegate().addEntities(entities);
	}

	@Override
	public void disableTickSchedulers(WorldChunk chunk) {
		delegate().disableTickSchedulers(chunk);
	}

	@Override
	public void cacheStructures(Chunk chunk) {
		delegate().cacheStructures(chunk);
	}

	@Override
	public void close() throws IOException {
		delegate().close();
	}

	@Override
	public String asString() {
		return delegate().asString();
	}

	@Override
	public boolean isChunkLoaded(long chunkPos) {
		return delegate().isChunkLoaded(chunkPos);
	}

	@Override
	public boolean shouldTickEntity(BlockPos pos) {
		return delegate().shouldTickEntity(pos);
	}

	@Override
	public boolean shouldTick(BlockPos pos) {
		return delegate().shouldTick(pos);
	}

	@Override
	public boolean shouldTick(ChunkPos pos) {
		return delegate().shouldTick(pos);
	}

	@Override
	public FeatureSet getEnabledFeatures() {
		return delegate().getEnabledFeatures();
	}

	@Override
	public Random getOrCreateRandom(Identifier id) {
		return delegate().getOrCreateRandom(id);
	}

	@Override
	public RandomSequencesState getRandomSequences() {
		return delegate().getRandomSequences();
	}

	@Override
	public ServerScoreboard getScoreboard() {
		return delegate().getScoreboard();
	}

	@Override
	public ServerChunkManager getChunkManager() {
		return delegate().getChunkManager();
	}

	@Override
	public WorldTickScheduler<Fluid> getFluidTickScheduler() {
		return delegate().getFluidTickScheduler();
	}

	@Override
	public WorldTickScheduler<Block> getBlockTickScheduler() {
		return delegate().getBlockTickScheduler();
	}
}
