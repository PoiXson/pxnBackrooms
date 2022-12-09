package com.poixson.backrooms.generators;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;


public class Level_309 extends BackroomsGenerator {

	public static final int SUBFLOOR = BackGen_000.SUBFLOOR;

	public static final int PATH_Y        = 54;
	public static final int PATH_WIDTH    = 3;
	public static final int PATH_CLEARING = 10;

	public static final Material PATH_TREE_TRUNK  = Material.BIRCH_LOG;
	public static final Material PATH_TREE_LEAVES = Material.BIRCH_LEAVES;

	protected final FastNoiseLiteD noisePath;
	protected final FastNoiseLiteD noisePathGround;
	protected final FastNoiseLiteD noiseTrees;

	public final TreePopulator treePop;
	public final PathTracer pathTrace;
	protected final AtomicReference<ConcurrentHashMap<Integer, Double>> pathCache =
			new AtomicReference<ConcurrentHashMap<Integer, Double>>(null);



	public Level_309(final BackroomsPlugin plugin) {
		super(plugin);
		// path
		this.noisePath = new FastNoiseLiteD();
		this.noisePath.setFrequency(0.01f);
		// path ground
		this.noisePathGround = new FastNoiseLiteD();
		this.noisePathGround.setFrequency(0.002f);
		this.noisePathGround.setFractalType(FractalType.Ridged);
		this.noisePathGround.setFractalOctaves(3);
		this.noisePathGround.setFractalGain(0.5f);
		this.noisePathGround.setFractalLacunarity(2.0f);
		// tree noise
		this.noiseTrees = new FastNoiseLiteD();
		this.noiseTrees.setFrequency(0.2f);
		// populators
		this.treePop = new TreePopulator309(this.noiseTrees, PATH_Y);
		this.pathTrace = new PathTracer(this.noisePath, this.getPathCacheMap());
	}

	@Override
	public void unload() {
		super.unload();
		this.pathCache.set(null);
	}



	public void setSeed(final int seed) {
		this.noisePath.setSeed(seed);
		this.noisePathGround.setSeed(seed);
		this.noiseTrees.setSeed(seed);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}



	protected void generateWoodsPath(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y = PATH_Y;
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int i=0; i<SUBFLOOR; i++) {
			chunk.setBlock(x, y+i, z, Material.STONE);
		}
		y += SUBFLOOR;
		final double ground;
		{
			final double g = this.noisePathGround.getNoise(xx, zz);
			ground = 1.0f + (g < 0.0f ? g * 0.6f : g);
		}
		// dirt
		final int elevation = (int) (ground * 2.5f); // 0 to 5
		for (int i=0; i<elevation; i++) {
			if (i >= elevation-1) {
				if (this.pathTrace.isPath(xx, zz, PATH_WIDTH)) {
					chunk.setBlock(x, y+i, z, Material.DIRT_PATH);
				} else {
					chunk.setBlock(x, y+i, z, Material.GRASS_BLOCK);
				}
			} else {
				chunk.setBlock(x, y+i, z, Material.DIRT);
			}
		}
	}



	public ConcurrentHashMap<Integer, Double> getPathCacheMap() {
		// existing
		{
			final ConcurrentHashMap<Integer, Double> cache = this.pathCache.get();
			if (cache != null)
				return cache;
		}
		// new instance
		{
			final ConcurrentHashMap<Integer, Double> cache = new ConcurrentHashMap<Integer, Double>();
			if (this.pathCache.compareAndSet(null, cache))
				return cache;
		}
		return this.getPathCacheMap();
	}



	public class TreePopulator309 extends TreePopulator {

		public TreePopulator309(final FastNoiseLiteD noise, final int chunkY) {
			super(
				noise, chunkY,
				PATH_TREE_TRUNK,
				PATH_TREE_LEAVES
			);
		}

		public boolean isTree(final int x, final int z) {
			if (!super.isTree(x, z))
				return false;
			if (Level_309.this.pathTrace.isPath(x, z, PATH_CLEARING))
				return false;
			return true;
		}

	}



}