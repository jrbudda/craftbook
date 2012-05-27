package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CookingPot extends PersistentMechanic implements SelfTriggeringMechanic{
    
	int lastTick = 0;
	
    /**
     * Plugin.
     */
    protected MechanismsPlugin plugin;
    
    /**
     * Location.
     */
    protected BlockWorldVector pt;
    
    /**
     * Construct a gate for a location.
     * 
     * @param pt
     * @param plugin 
     */
    public CookingPot(BlockWorldVector pt, MechanismsPlugin plugin) {
        super();
        this.pt = pt;
        this.plugin = plugin;
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isActive() {
        return true; 
    }

    public static class Factory extends AbstractMechanicFactory<CookingPot> {
        
        protected MechanismsPlugin plugin;
        
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public CookingPot detect(BlockWorldVector pt) {
            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                        return new CookingPot(pt, plugin);
                    }
                }
            }
            
            return null;
        }
        
        /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException 
         */
        @Override
        public CookingPot detect(BlockWorldVector pt, LocalPlayer player, Sign sign) throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Cook]")) {
                if (!player.hasPermission("craftbook.mech.cook")) {
                    throw new InsufficientPermissionsException();
                }
                
                sign.setLine(1, "[Cook]");
                player.print("Cooking pot created.");
            } else {
                return null;
            }
            
            throw new ProcessedMechanismException();
        }

    }

	@Override
	public void think() {
		lastTick++;
		if(lastTick<200) return;
		lastTick = 0;
		Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
		if (block.getState() instanceof Sign) {
			plugin.getLogger().log(Level.SEVERE, "[FOUND SIGN]");
			Sign sign = (Sign) block.getState();
			Block b = SignUtil.getBackBlock(sign.getBlock());
			int x = b.getX();
			int y = b.getY()+2;
			int z = b.getZ();
			Block cb = sign.getWorld().getBlockAt(x,y,z);
			plugin.getLogger().log(Level.SEVERE, "[its a]: " + cb.getType().name());
			if (cb.getType() == Material.CHEST) {
				plugin.getLogger().log(Level.SEVERE, "[FOUND CHEST]");
				Block fire = sign.getWorld().getBlockAt(x,y-1,z);
				if(fire.getType() == Material.FIRE)
				{
					plugin.getLogger().log(Level.SEVERE, "[FOUND FIRE]");
					if (cb.getState() instanceof Chest) {
						Chest chest = (Chest) cb.getState();
						plugin.getLogger().log(Level.SEVERE, "[SEARCHING CHEST]");
						for(ItemStack i : chest.getInventory().getContents())
						{
							if(i.getType() == Material.RAW_BEEF)
							{
								chest.getInventory().addItem(new ItemStack(Material.COOKED_BEEF,1));
								chest.getInventory().removeItem(new ItemStack(Material.RAW_BEEF,1));
								break;
							}
							if(i.getType() == Material.RAW_CHICKEN)
							{
								chest.getInventory().addItem(new ItemStack(Material.COOKED_CHICKEN,1));
								chest.getInventory().removeItem(new ItemStack(Material.RAW_CHICKEN,1));
								break;
							}
							if(i.getType() == Material.RAW_FISH)
							{
								chest.getInventory().addItem(new ItemStack(Material.COOKED_FISH,1));
								chest.getInventory().removeItem(new ItemStack(Material.RAW_FISH,1));
								break;
							}
						}
						plugin.getLogger().log(Level.SEVERE, "[FOUND NOTHING]");
					}
				}
			}
		}
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		
	}

	@Override
	public void onLeftClick(PlayerInteractEvent event) {
		
	}

	@Override
	public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
		
	}

	@Override
	public List<BlockWorldVector> getWatchedPositions() {
		return new ArrayList<BlockWorldVector>();
	}
}