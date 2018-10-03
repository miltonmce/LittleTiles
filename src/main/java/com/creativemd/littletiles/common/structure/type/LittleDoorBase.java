package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.transformation.DoorTransformation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleDoorBase extends LittleStructure {
	
	public boolean isWaitingForApprove = false;
	
	public int duration = 50;
	
	@Override
	public void onUpdatePacketReceived() {
		isWaitingForApprove = false;
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		if (nbt.hasKey("duration"))
			duration = nbt.getInteger("duration");
		else
			duration = 50;
		isWaitingForApprove = false;
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setInteger("duration", duration);
	}
	
	public boolean place(World world, EntityPlayer player, LittleAbsolutePreviewsStructure previews, DoorTransformation transformation, UUID uuid, LittleTilePos absolute, LittleTileVec additional) {
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
		if (LittleActionPlaceStack.canPlaceTiles(player, world, splitted, PlacementMode.all.getCoordsToCheck(splitted, previews.pos), PlacementMode.all)) {
			ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
			WorldFake fakeWorld = WorldFake.createFakeWorld(world);
			LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, null);
			for (Iterator iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
				TileEntity te = (TileEntity) iterator.next();
				if (te instanceof TileEntityLittleTiles)
					blocks.add((TileEntityLittleTiles) te);
			}
			
			if (world.isRemote) {
				for (TileEntityLittleTiles te : tiles.keySet()) {
					if (te.waitingAnimation != null) {
						te.waitingAnimation.removeWaitingTe(te);
						te.waitingAnimation = null;
					}
				}
			}
			
			EntityDoorAnimation animation = new EntityDoorAnimation(world, fakeWorld, (LittleDoorBase) previews.getStructure(), blocks, previews, absolute, transformation, uuid, player, additional);
			world.spawnEntity(animation);
			return true;
		}
		
		return false;
	}
	
	public Rotation getDefaultRotation() {
		return null;
	}
	
	public abstract boolean activate(World world, @Nullable EntityPlayer player, Rotation rotation, BlockPos pos);
	
	public abstract LittleTilePos getAbsoluteAxisVec();
	
	public abstract LittleTileVec getAdditionalAxisVec();
	
	public abstract LittleDoorBase copyToPlaceDoor();
	
	public abstract LittleGridContext getMinContext();
	
	public LittleAbsolutePreviewsStructure getDoorPreviews() {
		BlockPos pos = getMainTile().te.getPos();
		
		NBTTagCompound structureNBT = new NBTTagCompound();
		writeToNBTPreview(structureNBT, pos);
		
		LittleAbsolutePreviewsStructure previews = new LittleAbsolutePreviewsStructure(structureNBT, pos, getMinContext());
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			LittleTilePreview preview = previews.addTile(tile);
		}
		
		for (IStructureChildConnector child : children.values()) {
			previews.addChild(child.getStructure(getWorld()).getPreviews(pos));
		}
		
		return previews;
	}
	
	public static abstract class LittleDoorBaseParser<T extends LittleDoorBase> extends LittleStructureGuiParser<T> {
		
		public LittleDoorBaseParser(String id, GuiParent parent) {
			super(id, parent);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, LittleStructure structure) {
			
			parent.controls.add(new GuiLabel("Duration:", 0, 141));
			parent.controls.add(new GuiSteppedSlider("duration_s", 50, 140, 50, 12, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 50, 1, 500));
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public T parseStructure(ItemStack stack) {
			GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
			return parseStructure((int) slider.value);
		}
		
		@SideOnly(Side.CLIENT)
		public abstract T parseStructure(int duration);
	}
}