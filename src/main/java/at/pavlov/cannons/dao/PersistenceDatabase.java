package at.pavlov.cannons.dao;

import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.avaje.ebean.Query;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.projectile.Projectile;

public class PersistenceDatabase
{
	private final Cannons plugin;

	public PersistenceDatabase(Cannons _plugin)
	{
		plugin = _plugin;
	}

	public boolean loadCannons()
	{
		plugin.getCannonManager().clearCannonList();
		// create a query that returns CannonBean
		Query<CannonBean> query = plugin.getDatabase().find(CannonBean.class);
		List<CannonBean> beans = query.findList();

		// process the result
		if (beans == null || beans.size() == 0)
		{
			// nothing found; list is empty
			return false;
		}
		else
		{
			// found cannons - load them
			for (CannonBean bean : beans)
			{

				//check if cannon design exists
				CannonDesign design = plugin.getCannonDesign(bean.getDesignId());
				if (design == null)
				{
					plugin.logSevere("Design " + bean.getDesignId() + " not found in plugin/designs");
				}
				else
				{
					//load values for the cannon
					String world = bean.getWorld();
					Vector offset = new Vector(bean.getLocX(), bean.getLocY(), bean.getLocZ());
					BlockFace cannonDirection = BlockFace.valueOf(bean.getCannonDirection());
					String owner = bean.getOwner();
					
					//make a cannon
					Cannon cannon = new Cannon(design, world, offset, cannonDirection, owner);
					// cannon created - load properties
					cannon.setDatabaseId(bean.getId());
					cannon.setCannonName(bean.getName());
					cannon.setLoadedGunpowder(bean.getGunpowder());
					
					//load projectile
					cannon.setLoadedProjectile(plugin.getProjectile(cannon, bean.getProjectileID(), bean.getProjectileData()));
					
					//angles
					cannon.setHorizontalAngle(bean.getHorizontalAngle());
					cannon.setVerticalAngle(bean.getVerticalAngle());
					
					cannon.setValid(bean.isValid());

					//add a cannon to the cannon list
					plugin.createCannon(cannon);
					
					// update sign
					cannon.updateCannonSigns();	
				}

			}
			return true;
		}
	}

	/**
	 * save all cannons in the database
	 */
	public void saveAllCannons()
	{

		// get list of all cannons
		List<Cannon> cannonList = plugin.getCannonManager().getCannonList();

		// save all cannon to database
		for (Cannon cannon : cannonList)
		{
			boolean noError = saveCannon(cannon);
			if (!noError)
			{
				//if a error occurs while save the cannon, stop it
				return;
			}
		}
	}

	/**
	 * saves this cannon in the database
	 * 
	 * @param cannon
	 */
	public boolean saveCannon(Cannon cannon)
	{
		try
		{
			// search if the is cannon already stored in the database
			CannonBean bean = plugin.getDatabase().find(CannonBean.class).where().idEq(cannon.getDatabaseId()).findUnique();
			
			if (bean == null)
			{
				plugin.logDebug("creating new database entry");
				// create a new bean that is managed by bukkit
				bean = plugin.getDatabase().createEntityBean(CannonBean.class);
				cannon.setDatabaseId(bean.getId());
			}
			else
			{
				plugin.logDebug("saving cannons in database as id " + cannon.getDatabaseId());
			}

			// fill the bean with values to store
			// since bukkit manages the bean, we do not need to set
			// the ID property
			bean.setOwner(cannon.getOwner());
			bean.setWorld(cannon.getWorld());
			// save offset
			bean.setLocX(cannon.getOffset().getBlockX());
			bean.setLocY(cannon.getOffset().getBlockY());
			bean.setLocZ(cannon.getOffset().getBlockZ());
			// cannon direction
			bean.setCannonDirection(cannon.getCannonDirection().toString());
			// name
			bean.setName(cannon.getCannonName());
			// amount of gunpowder
			bean.setGunpowder(cannon.getLoadedGunpowder());
			
			// load projectile
			// if no projectile is found, set it to air
			Projectile projectile = cannon.getLoadedProjectile();
			if (projectile != null)
			{
				bean.setProjectileID(projectile.getLoadingItem().getId());
				bean.setProjectileData(projectile.getLoadingItem().getData());	
			}
			else
			{
				bean.setProjectileID(0);
				bean.setProjectileData(0);	
			}
			// angles
			bean.setHorizontalAngle(cannon.getHorizontalAngle());
			bean.setVerticalAngle(cannon.getVerticalAngle());
			// id
			bean.setDesignId(cannon.getDesignID());
			bean.setValid(cannon.isValid());

			// store the bean
			plugin.getDatabase().save(bean);
			cannon.setDatabaseId(bean.getId());	
			return true;
		}
		catch (Exception e)
		{
			plugin.logDebug("can't save to database. " + e);
			return false;
		}
	}

	/**
	 * removes all cannon of this player from the database
	 * 
	 * @param owner
     * @return returns true is there is an entry of this player in the database
	 */
	public boolean deleteCannons(String owner)
	{
		// create a query that returns CannonBean
		List<CannonBean> beans = plugin.getDatabase().find(CannonBean.class).where().eq("owner", owner).findList();

		// process the result
		if (beans == null || beans.size() == 0)
		{
			// nothing found; list is empty
			return false;
		}
		else
		{
			// found cannons - load them
			for (CannonBean bean : beans)
			{
				plugin.getDatabase().delete(CannonBean.class, bean.getId());
			}
            return true;
		}
	}
	
	/**
	 * removes all cannon of this player from the database
	 * 
	 * @param cannon
	 */
	public void deleteCannon(Cannon cannon)
	{
		// if the database id is null nothing, it is not saved in the database
		if (cannon.getDatabaseId() >= 0)
				plugin.getDatabase().delete(CannonBean.class, cannon.getDatabaseId());
	}

}
