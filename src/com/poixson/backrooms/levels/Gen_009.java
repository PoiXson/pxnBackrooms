package com.poixson.backrooms.levels;

import java.util.LinkedList;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 9 | Suburbs
public class Gen_009 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;



	public Gen_009(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
//				final int xx = (chunkX * 16) + x;
//				final int zz = (chunkZ * 16) + z;
				
				
				
			} // end x
		} // end z
	}



}
