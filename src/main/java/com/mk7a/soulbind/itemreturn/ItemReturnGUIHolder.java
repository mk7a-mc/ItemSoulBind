package com.mk7a.soulbind.itemreturn;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * This custom InventoryHolder is used to identify whether a gui of type RETURN_INV_TYPE 
 * in {@link ItemReturnCommand} belongs to the ItemReturnModule
 * @author LOOHP
 *
 */
public class ItemReturnGUIHolder implements InventoryHolder {
	
	protected ItemReturnGUIHolder() {
		
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}
