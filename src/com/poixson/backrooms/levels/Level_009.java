package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 9 | suburbs
public class Level_009 extends BackroomsLevel {

	public static final int SUBURBS_Y = 0;



	public Level_009(final BackroomsPlugin plugin) {
		super(plugin);
	}
	@Override
	public void unload() {
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 9) throw new RuntimeException("Invalid level: "+Integer.toString(level));
		final int x = BackroomsPlugin.Rnd10K();
		final int z = BackroomsPlugin.Rnd10K();
		final int y = SUBURBS_Y;
		return this.getSpawn(level, 10, x, y, z);
	}
	@Override
	public int getLevelFromY(final int y) {
		return 9;
	}
	public int getYFromLevel(final int level) {
		return SUBURBS_Y;
	}
	public int getMaxYFromLevel(final int level) {
		return 255;
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
if (chunkX == -1 && chunkZ == 1) return;
//if (chunkX % 10 == 0 || chunkZ % 10 == 0) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				chunk.setBlock(x, 0, z, Material.BEDROCK);
				chunk.setBlock(x, 1, z, Material.STONE);
			}
		}
	}



}
