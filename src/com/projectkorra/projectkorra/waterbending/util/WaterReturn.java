package com.projectkorra.projectkorra.waterbending.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.Map;

public class WaterReturn extends WaterAbility {

	private long time;
	private long interval;
	private double range;
	private Location location;
	private TempBlock block;
	
	private static final ItemStack standardWaterBottle;
	
	static {
	        ItemStack wbottle = new ItemStack(Material.POTION);
	        PotionMeta meta = (PotionMeta)wbottle.getItemMeta();
		meta.setBasePotionData(new PotionData(PotionType.WATER));
		wbottle.setItemMeta(meta);
		standardWaterBottle = wbottle;
	}

	public WaterReturn(Player player, Block block) {
		super(player);
		if (hasAbility(player, WaterReturn.class)) {
			return;
		}

		this.location = block.getLocation();
		this.range = 30;
		this.interval = 50;

		this.range = getNightFactor(range);

		if (bPlayer.canBendIgnoreBindsCooldowns(this)) {
			if (isTransparent(player, block) && ((TempBlock.isTempBlock(block) && block.isLiquid()) || !block.isLiquid()) && hasEmptyWaterBottle()) {
				this.block = new TempBlock(block, Material.WATER, (byte) 0);
			}
		}
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		} else if (!hasEmptyWaterBottle()) {
			remove();
			return;
		} else if (System.currentTimeMillis() < time + interval) {
			return;
		}

		Vector direction = GeneralMethods.getDirection(location, player.getEyeLocation()).normalize();
		time = System.currentTimeMillis();
		location = location.clone().add(direction);

		if (location == null || block == null) {
			remove();
			return;
		} else if (location.getBlock().equals(block.getLocation().getBlock())) {
			return;
		}

		if (location.distanceSquared(player.getEyeLocation()) > range * range) {
			remove();
			return;
		} else if (location.distanceSquared(player.getEyeLocation()) <= 1.5 * 1.5) {
			fillBottle();
			remove();
			return;
		}

		Block newblock = location.getBlock();
		if (isTransparent(player, newblock) && !newblock.isLiquid()) {
			block.revertBlock();
			block = new TempBlock(newblock, Material.WATER, (byte) 0);
		} else if (isTransparent(player, newblock)) {
			if (isWater(newblock)) {
				ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 5, newblock.getLocation().clone().add(.5, .5, .5), 255.0);
			}
		} else {
			remove();
			return;
		}

	}

	@Override
	public void remove() {
		super.remove();
		if (block != null) {
			block.revertBlock();
		}
	}

	private boolean hasEmptyWaterBottle() {
		PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			return true;
		}
		return false;
	}

	private void fillBottle() {
		int index = player.getInventory().first(Material.GLASS_BOTTLE);
		if (index != -1) {
			ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.POTION));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				Map<Integer, ItemStack> leftover = inventory.addItem(new ItemStack(Material.POTION));
				if (!leftover.isEmpty()) {
					player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.POTION));
				}
			}
		}
	}

	@Deprecated
	private static boolean isBending(Player player) {
		return hasAbility(player, WaterManipulation.class) || hasAbility(player, OctopusForm.class) || hasAbility(player, SurgeWall.class) || hasAbility(player, IceSpikeBlast.class);
		// || hasAbility(player, SurgeWave.class) NOTE: ONLY DISABLED TO PREVENT BOTTLEBENDING FROM BEING DISABLED FOREVER. ONCE BOTTLEBENDING HAS BEEN RECODED IN 1.9, THIS NEEDS TO BE READDED TO THE NEW SYSTEM.
	}

	@SuppressWarnings("deprecation")
	public static boolean hasWaterBottle(Player player) {
		return !hasAbility(player, WaterReturn.class) && !isBending(player) && getWaterBottleIndex(player) != -1;
		//hasAbility and isBending to be removed when this mechanic is properly checked by WaterManipulation, OctopusForm, SurgeWall, and IceSpikeBlast
	}

	public static boolean emptyWaterBottle(Player player) {
		int index = getWaterBottleIndex(player);
		if (index == -1) return false;
		player.getInventory().setItem(index, new ItemStack(Material.GLASS_BOTTLE);
		return true;
	}
	
	public static int getWaterBottleIndex(Player player) {
		return player.getInventory().first(standardWaterBottle);
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public TempBlock getBlock() {
		return block;
	}

	public void setBlock(TempBlock block) {
		this.block = block;
	}

	@Override
	public String getName() {
		return "Bottlebending";
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

}
