package com.poixson.backrooms.levels;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.listeners.Listener_078;


// 78 | Space
public class Level_078 extends LevelBackrooms {

	// generators
	public final Gen_078 gen;

	// listeners
	protected final Listener_078 listener_078;



	public Level_078(final BackroomsPlugin plugin) {
		super(plugin, 78);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(78, "space", "Space");
		}
		// generators
		this.gen = this.register(new Gen_078(plugin, 0, 0));
		// listeners
		this.listener_078 = new Listener_078(plugin);
	}



	@Override
	public void register() {
		super.register();
		this.listener_078.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_078.unregister();
	}



//TODO
	@Override
	public Location getSpawn(final int level) {
		return this.getSpawn(level, 0, 0);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 255, x, 0, z);
	}

	@Override
	public int getY(final int level) {
		return 255;
	}
	@Override
	public int getMaxY(final int level) {
		return 319;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
