package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.littletiles.common.tile.place.PlacePreviews;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

public class PlaceModeAll extends PlaceModeNormal {
	
	public PlaceModeAll(String name, PreviewMode mode) {
		super(name, mode, false);
	}
	
	@Override
	public boolean canPlaceStructures() {
		return true;
	}
	
	@Override
	public PlacementMode place() {
		if (GuiScreen.isCtrlKeyDown())
			return PlacementMode.overwrite;
		return super.place();
	}
	
	@Override
	public List<BlockPos> getCoordsToCheck(HashMap<BlockPos, PlacePreviews> splittedTiles, BlockPos pos) {
		return new ArrayList<>(splittedTiles.keySet());
	}
}
