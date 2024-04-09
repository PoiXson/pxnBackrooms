package com.poixson.backrooms.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.gens.Gen_001;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.events.PlayerMoveNormalEvent;
import com.poixson.tools.events.xListener;


//   1 | Basement - lights
// 309 | Radio Station - forest stairs
public class Listener_MoveNormal implements xListener {

	public static final int BASEMENT_LIGHT_RADIUS = 20;

	protected final BackroomsPlugin plugin;

	protected final HashMap<UUID, List<Location>> player_lights = new HashMap<UUID, List<Location>>();

	protected final int bedrock_barrier;



	public Listener_MoveNormal(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		this.bedrock_barrier = plugin.getBedrockBarrier();
	}



	public void register() {
		xListener.super.register(this.plugin);
	}
	@Override
	public void unregister() {
		xListener.super.unregister();
		synchronized (this.player_lights) {
			Block blk;
			for (final List<Location> list : this.player_lights.values()) {
				for (final Location loc : list) {
					blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
			}
			this.player_lights.clear();
		}
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMoveNormal(final PlayerMoveNormalEvent event) {
		final Player player = event.getPlayer();
		final int level = this.plugin.getLevel(player);
		// basement lights
		if (level == 1) {
			final Level_000 level_000 = (Level_000) this.plugin.getBackroomsLevel(0);
			final Gen_001 gen_001 = level_000.gen_001;
			final Location to = event.getTo();
			final World world = to.getWorld();
			final List<Location> lights = this.getPlayerLightsList(player.getUniqueId());
			// turn off lights
			{
				Location loc;
				Block blk;
				final Iterator<Location> it = lights.iterator();
				while (it.hasNext()) {
					loc = it.next();
					if (to.distance(loc) > BASEMENT_LIGHT_RADIUS) {
						it.remove();
						if (this.canTurnOff(loc)) {
							blk = loc.getBlock();
							if (Material.REDSTONE_TORCH.equals(blk.getType()))
								blk.setType(Material.BEDROCK);
						}
					}
				}
			}
			final int y = gen_001.level_y + gen_001.subfloor + gen_001.lamp_y + this.bedrock_barrier + 1;
			final int r = BASEMENT_LIGHT_RADIUS;
			int xx, zz;
			for (int iz=0-r-1; iz<r; iz+=10) {
				zz = Math.floorDiv(iz+to.getBlockZ(), 10) * 10;
				for (int ix=0-r-1; ix<r; ix+=10) {
					xx = Math.floorDiv(ix+to.getBlockX(), 10) * 10;
					final Block blk = world.getBlockAt(xx, y, zz);
					if (to.distance(blk.getLocation()) < BASEMENT_LIGHT_RADIUS) {
						if (Material.BEDROCK.equals(blk.getType())
						||  Material.REDSTONE_TORCH.equals(blk.getType()) ) {
							lights.add(blk.getLocation());
							world.setType(xx, y, zz, Material.REDSTONE_TORCH);
						}
					}
				}
			}
		// outside of basement
		} else {
			// player left the basement
			final UUID uuid = player.getUniqueId();
			if (this.player_lights.containsKey(uuid)) {
				// turn off all lights for player
				final List<Location> list = this.player_lights.get(uuid);
				this.player_lights.remove(uuid);
				for (final Location loc : list)
					this.lightTurnOff(loc);
				this.player_lights.remove(uuid);
			}
		}
		// forest stairs
		if (level == 309) {
			final Location loc = player.getLocation().clone().add(0, -1, 0);
			final Material type = loc.getBlock().getType();
			if (Material.STONE_BRICK_STAIRS.equals(type))
				this.plugin.addFreakOut(player);
		}
	}



	// -------------------------------------------------------------------------------
	// basement lights



	protected void lightTurnOff(final Location loc) {
		if (this.canTurnOff(loc)) {
			final Block block = loc.getBlock();
			if (Material.REDSTONE_TORCH.equals(block.getType()))
				block.setType(Material.BEDROCK);
		}
	}
	protected boolean canTurnOff(final Location loc) {
		for (final List<Location> list : this.player_lights.values()) {
			if (list.contains(loc))
				return false;
		}
		return true;
	}



	protected List<Location> getPlayerLightsList(final UUID uuid) {
		// existing list
		{
			final List<Location> list = this.player_lights.get(uuid);
			if (list != null)
				return list;
		}
		// new list
		{
			final List<Location> list = new ArrayList<Location>();
			this.player_lights.put(uuid, list);
			return list;
		}
	}



}