package com.lithial.me.handlers.utils;

import java.util.Random;

import com.lithial.me.enchantments.Enchantments;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class HorticultureHelper {
	///Misc Variables
	public static final Random rand = new Random();

	static int maxGrassDelay = 3;
	static int grassDelay = maxGrassDelay;
	static int delay = 0;
	static int maxdelay = 100;
	static int vineDelay = 0;
	static int maxVineDelay = 10;


	// Update plants arround player
	public static void updateSurroundingPlantBlocks(EntityPlayer player) {
		///////////
		//Timers
		//////////
		boolean foundGrass  = false;
		vineDelay = vineDelay - 1;
		grassDelay = grassDelay - 1;
		int vineFoundId = -1;
		boolean foundVine = false;
		if (vineDelay < 1) {
			vineDelay = maxVineDelay;
		}
		delay--;
		if (delay < 0) {
			delay = maxdelay;
		}
		////////////////////////////
		//player pos and area around
		////////////////////////////
		// The block position of the player (rounded down)
		int x = (int) (player.posX);
		int y = (int) (player.posY);
		int z = (int) (player.posZ);

		//distance around the player
		int dist = 4;
		for (int i = -dist; i <= dist; i++) {
			for (int k = -dist; k <= dist; k++) {
				for (int j = -dist; j <= dist; j++) {
					BlockPos pos = new BlockPos(x + i, y + j, z + k);
					Block block = player.worldObj.getBlockState(pos).getBlock();

					/////////////////////////////////
					// Update surrounding grass blocks.
					//////////////////////////////////
					if (delay % 100 == 0) {
						updateGrassBlock(x + i, y + j, z + k, player);
					}
					//////////////////////////////////
					// Update surrounding plant blocks.
					//////////////////////////////////
					if (delay % 100 == 50) {
						updatePlantBlock(x + i, y + j, z + k, player);
					}
					/////////////////////////
					// Make vines grow down.
					/////////////////////////
					if (vineDelay == maxVineDelay) {
						if (foundVine == false)
							foundVine = updateVineBlock(x + i, y + j, z + k, player);


						else if (block.getBlockState().getBlock() == Block.getBlockById(0)) {
							foundVine = false;
						}
					}
				}
			}
		}
	}

	///////////////////////////////////
	//Updates and grows grass/mycelium
	///////////////////////////////////
	public static void updateGrassBlock(int x, int y, int z, EntityPlayer player) {
		World world = player.worldObj;
		BlockPos pos = new BlockPos(x, y, z);
		Block block = world.getBlockState(pos).getBlock();

		if (block instanceof BlockGrass || block instanceof BlockMycelium) {
			block.updateTick(world,pos,block.getStateFromMeta(1), rand);
		}
	}
	///////////////////////////////////
	//Updates and grows all plants
	///////////////////////////////////
	public static void updatePlantBlock(int x, int y, int z, EntityPlayer player) {
		//variables
		World world = player.worldObj;
		BlockPos pos = new BlockPos(x, y, z);
		BlockPos pos2 = new BlockPos(x, y-1, z);
		BlockPos pos3 = new BlockPos(x, y+2, z);
		Block block = world.getBlockState(pos).getBlock();
		Block blockabove = world.getBlockState(pos3).getBlock();
		if (block == null) {return;}
		int blockMeta = world.getBlockState(pos).getBlock().getMetaFromState(block.getBlockState().getBaseState());
		// The update rate is how many times you force the block to update.
		int updateRate = 0;
		
		/////////////////////////////////////////////
		//variable update rates for different plants
		/////////////////////////////////////////////
		// If flowers/netherstalk/melon/pumpkin stem/reeds
		if (block instanceof BlockFlower || block instanceof BlockNetherWart || block instanceof BlockStem || block instanceof BlockReed || block instanceof BlockCrops) {
			// Force it to update 15+1 times faster
			updateRate = Enchantments.flowerUpdate;
		}
		// If block is a cactus or Cocoa bean.
		if (block instanceof BlockCactus || block instanceof BlockCocoa) {
			// Force it to update 5+1 times faster
			updateRate = Enchantments.cactusUpdate;
		}
		if (block instanceof BlockSapling) {
			// Force it to update 3+1 times faster
			updateRate = Enchantments.saplingUpdate;
		}
		// If a melon or pumpkin stem/that is already nearly fully grown.
		if (block instanceof BlockStem && blockMeta >= 7) {

			updateRate = Enchantments.melonUpdate;
		}
				//Anything not covered in here. Added to increase compat
		if(block instanceof IPlantable)
		{
			updateRate = Enchantments.modUpdate;
		}

		////////////////////
		//Updating Flowers
		////////////////////
		if ((block instanceof BlockTallGrass) || block instanceof BlockFlower) {
			updateRate = Enchantments.tallGrassUpdate;
			if (grassDelay < 0) {
				// Check blocks adjacent to and directly above and below that
				// block

				Block grass = Block.getBlockFromName("tallgrass");
				for (int i = x - 1; i <= x + 1; i++) {
					for (int j = y - 2; j <= y; j++) {
						for (int k = z - 1; k <= z + 1; k++) {
							//Check for grass and Light
							Block temp = block;

							if (temp instanceof BlockGrass && (world.getLight(pos3) >= 9)) {
								if (blockabove instanceof BlockAir) {
									//tall grass update
									if (grassDelay < 0) {
										//9/10 chance of tall grass
										if (rand.nextInt(10) < 9) {
											//world.setBlock (i, j + 1, k, grass);
										}

										// With a chance of 1/20 set to yellow flower.
										else if (rand.nextInt(2) == 0)
										{

											world.setBlockState(pos3,Block.getStateById(37));
										}
										// With a chance of 1/20 set to red flower.
										else
										{

											world.setBlockState(pos3,Block.getStateById(38));
										}
										grassDelay = maxGrassDelay;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		/////////////////////////
		//keeps farmlands watered
		/////////////////////////
		if (block instanceof BlockFarmland) {
			Block blockAbove = world.getBlockState(pos3).getBlock();


			if (blockMeta < 7 && (blockAbove instanceof BlockFlower || blockAbove instanceof BlockStem)) {
				// Set farmland to fully moist.

				world.setBlockState(pos,Block.getStateById(7));

			}
		}

		//////////////////////
		//Update Timer
		//////////////////////
		if (updateRate > 0) {
		
			for (int i = 0; i < updateRate; i++) {
				if(block.getTickRandomly()){
					world.scheduleUpdate(pos, block, world.rand.nextInt(updateRate));
                 //System.out.print("isticking"); 
				}
				//block.updateTick(world,pos,world.setBlockState(pos,Block.getState( ), world, pos), rand);
				//block.updateTick(world, pos,state , rand);
			}
		}
	}
	///////////////////////
	//Vine updater
	///////////////////////
	public static boolean updateVineBlock(int x, int y, int z, EntityPlayer player) {
		//Look below current block for vines
		for (int v = y; v > 0; v--) {
			//vine variables
			World world = player.worldObj;
			BlockPos pos = new BlockPos(x, y, z);
			Block block = world.getBlockState(pos).getBlock();
			BlockPos pos2 = new BlockPos(x, y-1, z);
			Block lowerBlock = world.getBlockState(pos2).getBlock();

			int meta = world.getBlockState(pos).getBlock().getMetaFromState(block.getBlockState().getBaseState());



			//if found update vines
			if (block instanceof BlockVine) {
				if (lowerBlock instanceof BlockAir) {
					block.updateTick(world,pos.down(),block.getDefaultState(), rand);
					return true;
				}
			}
		}
		return false;
	}
}

