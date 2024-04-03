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


// 264 | Museum
public class Gen_264 extends BackroomsGen {

	// default params
	public static final int DEFAULT_LEVEL_H    = 8;
	public static final int DEFAULT_SUBFLOOR   = 3;
	public static final int DEFAULT_SUBCEILING = 3;

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;



	public Gen_264(final BackroomsLevel backlevel, final int seed, final BackroomsGen gen_below) {
		super(backlevel, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		// params
		this.enable_gen = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top = cfgParams.getBoolean("Enable-Top"  );
		this.level_y    = cfgParams.getInt(    "Level-Y"     );
		this.level_h    = cfgParams.getInt(    "Level-Height");
		this.subfloor   = cfgParams.getInt(    "SubFloor"    );
		this.subceiling = cfgParams.getInt(    "SubCeiling"  );
	}



	@Override
	public int getLevelNumber() {
		return 264;
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
		final Level_011 backlevel = (Level_011) this.backlevel;
		final int level_264_y = backlevel.gen_264.level_y;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++)
				chunk.setBlock(ix, level_264_y, iz, Material.BEDROCK);
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",   Boolean.TRUE                       );
		cfgParams.addDefault("Enable-Top",   Boolean.TRUE                       );
		cfgParams.addDefault("Level-Y",      Integer.valueOf(this.getDefaultY()));
		cfgParams.addDefault("Level-Height", Integer.valueOf(DEFAULT_LEVEL_H   ));
		cfgParams.addDefault("SubFloor",     Integer.valueOf(DEFAULT_SUBFLOOR  ));
		cfgParams.addDefault("SubCeiling",   Integer.valueOf(DEFAULT_SUBCEILING));
	}



}
