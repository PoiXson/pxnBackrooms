package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_011;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 40 | Arcade
public class Gen_040 extends BackroomsGen {

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;



	public Gen_040(final BackroomsLevel backlevel, final int seed, final int level_y) {
		super(backlevel, null, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		// params
		final Level_011 level_011 = (Level_011) backlevel;
		this.enable_gen = cfgParams.getBoolean("Enable-Gen");
		this.enable_top = cfgParams.getBoolean("Enable-Top");
		this.level_y    = level_011.gen_122.level_y;
		this.level_h    = level_011.gen_122.level_h;
		this.subfloor   = level_011.gen_122.subfloor;
		this.subceiling = level_011.gen_122.subceiling;
	}



	@Override
	public int getLevelNumber() {
		return 40;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.level_h;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++)
				chunk.setBlock(ix, this.level_y, iz, Material.GLOWSTONE);
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen", Boolean.TRUE);
		cfgParams.addDefault("Enable-Top", Boolean.TRUE);
	}



}
