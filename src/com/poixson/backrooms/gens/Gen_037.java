package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_037;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_037;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.commonmc.tools.plotter.PlotterFactory;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.StringUtils;


// 37 | Poolrooms
public class Gen_037 extends BackroomsGen {

	public static final int WATER_DEPTH = 3;

	public static final double THRESH_ROOM   = 0.2;
	public static final double THRESH_PORTAL = 0.5;

	// noise
	public final FastNoiseLiteD noisePoolRooms;
	public final FastNoiseLiteD noiseTunnels;
	public final FastNoiseLiteD noisePortalLobby;
	public final FastNoiseLiteD noisePortalHotel;

	// blocks
	public final AtomicReference<String> block_wall_a     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_wall_b     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling    = new AtomicReference<String>(null);



	public Gen_037(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// pool rooms
		this.noisePoolRooms = this.register(new FastNoiseLiteD());
		this.noisePoolRooms.setFrequency(0.004);
		this.noisePoolRooms.setFractalOctaves(2);
		this.noisePoolRooms.setNoiseType(NoiseType.OpenSimplex2);
		this.noisePoolRooms.setFractalType(FractalType.PingPong);
		this.noisePoolRooms.setFractalGain(0.1);
		this.noisePoolRooms.setFractalPingPongStrength(2.8);
		// tunnels
		this.noiseTunnels = this.register(new FastNoiseLiteD());
		this.noiseTunnels.setFrequency(0.015);
		this.noiseTunnels.setNoiseType(NoiseType.Cellular);
		this.noiseTunnels.setFractalType(FractalType.PingPong);
		this.noiseTunnels.setFractalPingPongStrength(5.0);
		this.noiseTunnels.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// portal to lobby
		this.noisePortalLobby = this.register(new FastNoiseLiteD());
		this.noisePortalLobby.setFrequency(0.02);
		this.noisePortalLobby.setFractalOctaves(2);
		this.noisePortalLobby.setFractalType(FractalType.FBm);
		// portal to hotel
		this.noisePortalHotel = this.register(new FastNoiseLiteD());
		this.noisePortalHotel.setFrequency(0.01);
	}



	public enum RoomType {
		OPEN,
		SOLID,
	};

	public class PoolData implements PreGenData {

		public final double valueRoom, valuePortalHotel, valuePortalLobby;
		public final boolean possiblePortalHotel;
		public final boolean possiblePortalLobby;
		public RoomType type;

		public PoolData(final int x, final int z) {
			this.valueRoom        = Gen_037.this.noisePoolRooms.getNoise(x, z);
			this.valuePortalHotel = Gen_037.this.noisePortalHotel.getNoise(x, z);
			this.valuePortalLobby = Gen_037.this.noisePortalLobby.getNoise(x, z);
			if (this.valueRoom < THRESH_ROOM) {
				this.type = RoomType.SOLID;
				this.possiblePortalHotel = (this.valuePortalHotel > THRESH_PORTAL);
				this.possiblePortalLobby = false;
			} else {
				this.type = RoomType.OPEN;
				this.possiblePortalHotel = false;
				this.possiblePortalLobby = (
					this.valuePortalLobby > Gen_037.this.noisePortalLobby.getNoise(x, z-1) &&
					this.valuePortalLobby > Gen_037.this.noisePortalLobby.getNoise(x, z+1) &&
					this.valuePortalLobby > Gen_037.this.noisePortalLobby.getNoise(x+1, z) &&
					this.valuePortalLobby > Gen_037.this.noisePortalLobby.getNoise(x-1, z)
				);
			}
		}

		public boolean isSolid() {
			return RoomType.SOLID.equals(this.type);
		}

	}



	public void pregenerate(final Map<Iab, PoolData> data,
			final int chunkX, final int chunkZ) {
		PoolData dao;
		int xx, zz;
		for (int rz=-1; rz<3; rz++) {
			zz = (chunkZ * 16) + (rz * 8) + 4;
			for (int rx=-1; rx<3; rx++) {
				xx = (chunkX * 16) + (rx * 8) + 4;
				dao = new PoolData(xx, zz);
				data.put(new Iab(rx, rz), dao);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_037) return;
		final Material block_wall_a     = Material.matchMaterial(this.block_wall_a    .get());
		final Material block_wall_b     = Material.matchMaterial(this.block_wall_b    .get());
		final Material block_subfloor   = Material.matchMaterial(this.block_subfloor  .get());
		final Material block_subceiling = Material.matchMaterial(this.block_subceiling.get());
		final Material block_ceiling    = Material.matchMaterial(this.block_ceiling   .get());
		if (block_wall_a     == null) throw new RuntimeException("Invalid block type for level 37 Wall A"    );
		if (block_wall_b     == null) throw new RuntimeException("Invalid block type for level 37 Wall B"    );
		if (block_subfloor   == null) throw new RuntimeException("Invalid block type for level 37 SubFloor"  );
		if (block_subceiling == null) throw new RuntimeException("Invalid block type for level 37 SubCeiling");
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 37 Ceiling"   );
		final Map<Iab, PoolData>  poolData  = ((PregenLevel0)pregen).pools;
		final Map<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_h + y + 1;
		final int h  = this.level_h + 2;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				// subceiling
				if (ENABLE_TOP_037) {
					for (int iy=0; iy<SUBCEILING; iy++)
						chunk.setBlock(ix, cy+iy+1, iz, block_subceiling);
				}
			}
		}
		PoolData dao;
		boolean solid_n,  solid_s,  solid_e,  solid_w;
		boolean solid_ne, solid_nw, solid_se, solid_sw;
		for (int rz=0; rz<2; rz++) {
			for (int rx=0; rx<2; rx++) {
				final BlockPlotter plot =
					(new PlotterFactory())
					.placer(chunk)
					.axis("use")
					.xyz(rx*8, y, rz*8)
					.whd(8, h, 8)
					.build();
				plot.type('#', block_wall_a);
				plot.type('@', block_wall_b);
				plot.type('w', Material.WATER, "0");
				plot.type('g', block_ceiling);
				dao = poolData.get(new Iab(rx, rz));
				solid_n  = poolData.get(new Iab(rx,   rz-1)).isSolid();
				solid_s  = poolData.get(new Iab(rx,   rz+1)).isSolid();
				solid_e  = poolData.get(new Iab(rx+1, rz  )).isSolid();
				solid_w  = poolData.get(new Iab(rx-1, rz  )).isSolid();
				solid_ne = poolData.get(new Iab(rx+1, rz-1)).isSolid();
				solid_nw = poolData.get(new Iab(rx-1, rz-1)).isSolid();
				solid_se = poolData.get(new Iab(rx+1, rz+1)).isSolid();
				solid_sw = poolData.get(new Iab(rx-1, rz+1)).isSolid();
				final StringBuilder[][] matrix = plot.getMatrix3D();
				switch (dao.type) {
				case SOLID: {
					for (int iz=0; iz<8; iz++) {
						for (int iy=0; iy<h; iy++)
							matrix[iy][iz].append(StringUtils.Repeat(8, '@'));
					}
					// outside-corner
					{
						// north/east outside-corner
						if (!solid_n && !solid_e && !solid_ne) {
							StringUtils.ReplaceInString(matrix[0][0], "####", 4);
							StringUtils.ReplaceInString(matrix[0][1], "##",   6);
							StringUtils.ReplaceInString(matrix[0][2], "#",    7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "    ", 4);
								StringUtils.ReplaceInString(matrix[iy][1], "  ",   6);
								StringUtils.ReplaceInString(matrix[iy][2], " ",    7);
							}
						}
						// north/west outside-corner
						if (!solid_n && !solid_w && !solid_nw) {
							StringUtils.ReplaceInString(matrix[0][0], "####", 0);
							StringUtils.ReplaceInString(matrix[0][1], "##",   0);
							StringUtils.ReplaceInString(matrix[0][2], "#",    0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "    ", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "  ",   0);
								StringUtils.ReplaceInString(matrix[iy][2], " ",    0);
							}
						}
						// south/east outside-corner
						if (!solid_s && !solid_e && !solid_se) {
							StringUtils.ReplaceInString(matrix[0][7], "####", 4);
							StringUtils.ReplaceInString(matrix[0][6], "##",   6);
							StringUtils.ReplaceInString(matrix[0][5], "#",    7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "    ", 4);
								StringUtils.ReplaceInString(matrix[iy][6], "  ",   6);
								StringUtils.ReplaceInString(matrix[iy][5], " ",    7);
							}
						}
						// south/west outside-corner
						if (!solid_s && !solid_w && !solid_sw) {
							StringUtils.ReplaceInString(matrix[0][7], "####", 0);
							StringUtils.ReplaceInString(matrix[0][6], "##",   0);
							StringUtils.ReplaceInString(matrix[0][5], "#",    0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "    ", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "  ",   0);
								StringUtils.ReplaceInString(matrix[iy][5], " ",    0);
							}
						}
					}
					// cross-corner
					{
						// north/east cross-corner
						if (!solid_n && !solid_e && solid_ne) {
							StringUtils.ReplaceInString(matrix[0][0], "#####", 3);
							StringUtils.ReplaceInString(matrix[0][1], "###",   5);
							StringUtils.ReplaceInString(matrix[0][2], "##",    6);
							StringUtils.ReplaceInString(matrix[0][3], "#",     7);
							StringUtils.ReplaceInString(matrix[0][4], "#",     7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "     ", 3);
								StringUtils.ReplaceInString(matrix[iy][1], "   ",   5);
								StringUtils.ReplaceInString(matrix[iy][2], "  ",    6);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     7);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     7);
							}
						}
						// north/west cross-corner
						if (!solid_n && !solid_w && solid_nw) {
							StringUtils.ReplaceInString(matrix[0][0], "#####", 0);
							StringUtils.ReplaceInString(matrix[0][1], "###",   0);
							StringUtils.ReplaceInString(matrix[0][2], "##",    0);
							StringUtils.ReplaceInString(matrix[0][3], "#",     0);
							StringUtils.ReplaceInString(matrix[0][4], "#",     0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "     ", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "   ",   0);
								StringUtils.ReplaceInString(matrix[iy][2], "  ",    0);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     0);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     0);
							}
						}
						// south/east cross-corner
						if (!solid_s && !solid_e && solid_se) {
							StringUtils.ReplaceInString(matrix[0][7], "#####", 3);
							StringUtils.ReplaceInString(matrix[0][6], "###",   5);
							StringUtils.ReplaceInString(matrix[0][5], "##",    6);
							StringUtils.ReplaceInString(matrix[0][4], "#",     7);
							StringUtils.ReplaceInString(matrix[0][3], "#",     7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "     ", 3);
								StringUtils.ReplaceInString(matrix[iy][6], "   ",   5);
								StringUtils.ReplaceInString(matrix[iy][5], "  ",    6);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     7);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     7);
							}
						}
						// south/west cross-corner
						if (!solid_s && !solid_w && solid_sw) {
							StringUtils.ReplaceInString(matrix[0][7], "#####", 0);
							StringUtils.ReplaceInString(matrix[0][6], "###",   0);
							StringUtils.ReplaceInString(matrix[0][5], "##",    0);
							StringUtils.ReplaceInString(matrix[0][4], "#",     0);
							StringUtils.ReplaceInString(matrix[0][3], "#",     0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "     ", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "   ",   0);
								StringUtils.ReplaceInString(matrix[iy][5], "  ",    0);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     0);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     0);
							}
						}
					}
					break;
				}
				case OPEN: {
					for (int iz=0; iz<8; iz++) {
						matrix[0][iz].append(StringUtils.Repeat(8, '#'));
						for (int iy=1; iy<h; iy++)
							matrix[iy][iz].append(StringUtils.Repeat(8, ' '));
					}
					// inside corner
					{
						// north/east inside corner
						if (solid_n && solid_e && solid_ne) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "@@", 6);
								StringUtils.ReplaceInString(matrix[iy][1], "@",  7);
							}
						}
						// north/west inside corner
						if (solid_n && solid_w && solid_nw) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "@@", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "@",  0);
							}
						}
						// south/east inside corner
						if (solid_s && solid_e && solid_se) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "@@", 6);
								StringUtils.ReplaceInString(matrix[iy][6], "@",  7);
							}
						}
						// south/west inside corner
						if (solid_s && solid_w && solid_sw) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "@@", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "@",  0);
							}
						}
					}
					// portal to lobby
					if (dao.possiblePortalLobby) {
						int xx, zz;
						LobbyData lobby;
						boolean foundWall = false;
						Z_LOOP:
						for (int iz=0; iz<8; iz++) {
							zz = (rz * 8) + iz;
							for (int ix=0; ix<8; ix++) {
								xx = (rx * 8) + ix;
								lobby = lobbyData.get(new Iab(xx, zz));
								if (lobby.isWall) {
									foundWall = true;
									break Z_LOOP;
								}
							}
						}
						if (!foundWall) {
							xx = (chunkX * 16) + (rx * 8);
							zz = (chunkZ * 16) + (rz * 8);
							((Level_000)this.backlevel).portal_0_to_37.add(xx, zz);
							final int hh = Level_000.H_000 + SUBCEILING + Level_000.H_006 + SUBFLOOR + 5;
							final BlockPlotter pp =
								(new PlotterFactory())
								.placer(chunk)
								.axis("use")
								.xz(rx*8, rz*8)
								.y(Level_000.Y_000+SUBFLOOR)
								.whd(6, hh+1, 6)
								.build();
							pp.type('#', Material.BEDROCK  );
							pp.type('g', Material.GLOWSTONE);
							pp.type('.', Material.AIR      );
							pp.type(',', Material.WATER, "8");
							final StringBuilder[][] mtx = pp.getMatrix3D();
							// floor
							mtx[0][0].append(" #### "); mtx[1][0].append(" #### ");
							mtx[0][1].append("##gg##"); mtx[1][1].append("##,,##");
							mtx[0][2].append("#gggg#"); mtx[1][2].append("#,,,,#");
							mtx[0][3].append("#gggg#"); mtx[1][3].append("#,,,,#");
							mtx[0][4].append("##gg##"); mtx[1][4].append("##,,##");
							mtx[0][5].append(" #### "); mtx[1][5].append(" #### ");
							final int hhh = Level_000.H_000 + 3;
							for (int yi=2; yi<hhh; yi++) {
								mtx[yi][0].append(" .... ");
								mtx[yi][1].append("..,,..");
								mtx[yi][2].append(".,,,,.");
								mtx[yi][3].append(".,,,,.");
								mtx[yi][4].append("..,,..");
								mtx[yi][5].append(" .... ");
							}
							// shaft
							for (int yi=hhh; yi<hh; yi++) {
								mtx[yi][0].append(" #### ");
								mtx[yi][1].append("##,,##");
								mtx[yi][2].append("#,,,,#");
								mtx[yi][3].append("#,,,,#");
								mtx[yi][4].append("##,,##");
								mtx[yi][5].append(" #### ");
							}
							// top
							mtx[hh][0].append("  gg  ");
							mtx[hh][1].append(" g,,g ");
							mtx[hh][2].append("g,,,,g");
							mtx[hh][3].append("g,,,,g");
							mtx[hh][4].append(" g,,g ");
							mtx[hh][5].append("  gg  ");
							plots.add(pp);
						}
					}
					break;
				}
				default: throw new RuntimeException("Unknown pool room type: " + dao.type.toString());
				} // end type switch
				// water
				for (int iz=0; iz<8; iz++) {
					for (int iy=0; iy<=WATER_DEPTH; iy++)
						StringUtils.ReplaceWith(matrix[iy][iz], ' ', 'w');
				}
				// ceiling
				if (ENABLE_TOP_037) {
					for (int iz=0; iz<8; iz++)
						StringUtils.ReplaceWith(matrix[h-1][iz], ' ', 'g');
				}
				plot.run();
			} // end room x
		} // end room z
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		final ConfigurationSection cfg = this.plugin.getLevelBlocks(37);
		this.block_wall_a    .set(cfg.getString("WallA"     ));
		this.block_wall_b    .set(cfg.getString("WallB"     ));
		this.block_subfloor  .set(cfg.getString("SubFloor"  ));
		this.block_subceiling.set(cfg.getString("SubCeiling"));
		this.block_ceiling   .set(cfg.getString("Ceiling"   ));
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Level37.Blocks.WallA",      "minecraft:prismarine_bricks");
		cfg.addDefault("Level37.Blocks.WallB",      "minecraft:prismarine"       );
		cfg.addDefault("Level37.Blocks.SubFloor",   "minecraft:dark_prismarine"  );
		cfg.addDefault("Level37.Blocks.SubCeiling", "minecraft:dark_prismarine"  );
		cfg.addDefault("Level37.Blocks.Ceiling",    "minecraft:glowstone"        );
	}



}
