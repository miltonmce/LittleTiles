package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline.KeyDeselectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline.KeySelectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.KeyControl;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel.TimelineChannelDouble;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel.TimelineChannelInteger;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiToolTipEvent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.client.gui.controls.GuiLTDistance;
import com.creativemd.littletiles.client.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiDialogAxis.GuiAxisButton;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.LTStructureAnnotation;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.util.animation.AnimationKey;
import com.creativemd.littletiles.common.util.animation.AnimationState;
import com.creativemd.littletiles.common.util.animation.AnimationTimeline;
import com.creativemd.littletiles.common.util.animation.ValueTimeline;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleAdvancedDoor extends LittleDoorBase {
	
	public static PairList<Integer, Double> loadPairListDouble(int[] array) {
		PairList<Integer, Double> list = new PairList<>();
		int i = 0;
		while (i < array.length) {
			list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
			i += 3;
		}
		return list;
	}
	
	public static PairList<Integer, Double> loadPairListInteger(int[] array) {
		PairList<Integer, Double> list = new PairList<>();
		int i = 0;
		while (i < array.length) {
			list.add(array[i], (double) array[i + 1]);
			i += 2;
		}
		return list;
	}
	
	public static PairList<Integer, Double> loadPairListDouble(int[] array, int from, int length) {
		PairList<Integer, Double> list = new PairList<>();
		int i = from;
		while (i < from + length) {
			list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
			i += 3;
		}
		return list;
	}
	
	public static int[] savePairListDouble(PairList<Integer, Double> list) {
		if (list == null)
			return null;
		
		int[] array = new int[list.size() * 3];
		for (int i = 0; i < list.size(); i++) {
			Pair<Integer, Double> pair = list.get(i);
			array[i * 3] = pair.key;
			long value = Double.doubleToLongBits(pair.value);
			array[i * 3 + 1] = (int) (value >> 32);
			array[i * 3 + 2] = (int) value;
		}
		return array;
	}
	
	public static int[] savePairListInteger(PairList<Integer, Integer> list) {
		if (list == null)
			return null;
		
		int[] array = new int[list.size() * 2];
		for (int i = 0; i < list.size(); i++) {
			Pair<Integer, Integer> pair = list.get(i);
			array[i * 2] = pair.key;
			array[i * 2 + 1] = pair.value;
		}
		return array;
	}
	
	public PairList<Integer, Double> interpolateToDouble(PairList<Integer, Integer> list) {
		if (list == null)
			return null;
		
		PairList<Integer, Double> converted = new PairList<>();
		for (Pair<Integer, Integer> pair : list) {
			converted.add(pair.key, offGrid.pixelSize * pair.value);
		}
		
		return converted;
	}
	
	public PairList<Integer, Double> invert(PairList<Integer, Double> list) {
		if (list == null)
			return null;
		
		PairList<Integer, Double> inverted = new PairList<>();
		for (int i = list.size() - 1; i >= 0; i--) {
			Pair<Integer, Double> pair = list.get(i);
			inverted.add(duration - pair.key, pair.value);
		}
		return inverted;
	}
	
	public static boolean isAligned(AnimationKey key, ValueTimeline timeline) {
		if (timeline == null)
			return true;
		
		return key.isAligned(timeline.first(key));
	}
	
	public LittleAdvancedDoor(LittleStructureType type) {
		super(type);
	}
	
	@LTStructureAnnotation(color = ColorUtils.RED)
	public StructureRelative axisCenter;
	
	public ValueTimeline rotX;
	public ValueTimeline rotY;
	public ValueTimeline rotZ;
	
	public LittleGridContext offGrid;
	public ValueTimeline offX;
	public ValueTimeline offY;
	public ValueTimeline offZ;
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		
		NBTTagCompound animation = new NBTTagCompound();
		if (rotX != null)
			animation.setIntArray("rotX", rotX.write());
		if (rotY != null)
			animation.setIntArray("rotY", rotY.write());
		if (rotZ != null)
			animation.setIntArray("rotZ", rotZ.write());
		
		if (offGrid != null) {
			animation.setInteger("offGrid", offGrid.size);
			if (offX != null)
				animation.setIntArray("offX", offX.write());
			if (offY != null)
				animation.setIntArray("offY", offY.write());
			if (offZ != null)
				animation.setIntArray("offZ", offZ.write());
		}
		nbt.setTag("animation", animation);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		
		if (nbt.hasKey("animation")) {
			NBTTagCompound animation = nbt.getCompoundTag("animation");
			if (animation.hasKey("rotX"))
				rotX = ValueTimeline.read(animation.getIntArray("rotX"));
			if (animation.hasKey("rotY"))
				rotY = ValueTimeline.read(animation.getIntArray("rotY"));
			if (animation.hasKey("rotZ"))
				rotZ = ValueTimeline.read(animation.getIntArray("rotZ"));
			
			if (animation.hasKey("offGrid")) {
				offGrid = LittleGridContext.get(animation.getInteger("offGrid"));
				if (animation.hasKey("offX"))
					offX = ValueTimeline.read(animation.getIntArray("offX"));
				if (animation.hasKey("offY"))
					offY = ValueTimeline.read(animation.getIntArray("offY"));
				if (animation.hasKey("offZ"))
					offZ = ValueTimeline.read(animation.getIntArray("offZ"));
			}
		} else { // before pre132
			if (nbt.hasKey("rotX"))
				rotX = ValueTimeline.create(interpolation).addPoints(loadPairListDouble(nbt.getIntArray("rotX")));
			if (nbt.hasKey("rotY"))
				rotY = ValueTimeline.create(interpolation).addPoints(loadPairListDouble(nbt.getIntArray("rotY")));
			if (nbt.hasKey("rotZ"))
				rotZ = ValueTimeline.create(interpolation).addPoints(loadPairListDouble(nbt.getIntArray("rotZ")));
			
			if (nbt.hasKey("offGrid")) {
				offGrid = LittleGridContext.get(nbt.getInteger("offGrid"));
				if (nbt.hasKey("offX"))
					offX = ValueTimeline.create(interpolation).addPoints(loadPairListInteger(nbt.getIntArray("offX")));
				if (nbt.hasKey("offY"))
					offY = ValueTimeline.create(interpolation).addPoints(loadPairListInteger(nbt.getIntArray("offY")));
				if (nbt.hasKey("offZ"))
					offZ = ValueTimeline.create(interpolation).addPoints(loadPairListInteger(nbt.getIntArray("offZ")));
			}
		}
	}
	
	@Override
	public void onFlip(LittleGridContext context, Axis axis, LittleVec doubledCenter) {
		super.onFlip(context, axis, doubledCenter);
		
		switch (axis) {
		case X:
			if (rotY != null)
				rotY.flip();
			if (rotZ != null)
				rotZ.flip();
			
			if (offX != null)
				offX.flip();
			break;
		case Y:
			if (rotX != null)
				rotX.flip();
			if (rotZ != null)
				rotZ.flip();
			
			if (offY != null)
				offY.flip();
			break;
		case Z:
			if (rotX != null)
				rotX.flip();
			if (rotY != null)
				rotY.flip();
			
			if (offZ != null)
				offZ.flip();
			break;
		}
	}
	
	@Override
	public void onRotate(LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
		super.onRotate(context, rotation, doubledCenter);
		ValueTimeline rotX = this.rotX;
		ValueTimeline rotY = this.rotY;
		ValueTimeline rotZ = this.rotZ;
		
		this.rotX = rotation.getX(rotX, rotY, rotZ);
		if (rotation.negativeX() && this.rotX != null)
			this.rotX.flip();
		this.rotY = rotation.getY(rotX, rotY, rotZ);
		if (rotation.negativeY() && this.rotY != null)
			this.rotY.flip();
		this.rotZ = rotation.getZ(rotX, rotY, rotZ);
		if (rotation.negativeZ() && this.rotZ != null)
			this.rotZ.flip();
		
		ValueTimeline offX = this.offX;
		ValueTimeline offY = this.offY;
		ValueTimeline offZ = this.offZ;
		
		this.offX = rotation.getX(offX, offY, offZ);
		if (rotation.negativeX() && this.offX != null)
			this.offX.flip();
		this.offY = rotation.getY(offX, offY, offZ);
		if (rotation.negativeY() && this.offY != null)
			this.offY.flip();
		this.offZ = rotation.getZ(offX, offY, offZ);
		if (rotation.negativeZ() && this.offZ != null)
			this.offZ.flip();
	}
	
	@Override
	public LittleTransformation[] getDoorTransformations(EntityPlayer player) {
		return new LittleTransformation[] { new LittleTransformation(getMainTile().te.getPos(), 0, 0, 0, new LittleVec(0, 0, 0), new LittleVecContext()) };
	}
	
	@Override
	public void transformDoorPreview(LittleAbsolutePreviewsStructure previews, LittleTransformation transformation) {
		LittleAdvancedDoor newDoor = (LittleAdvancedDoor) previews.getStructure();
		if (newDoor.axisCenter.getContext().size > previews.context.size)
			previews.convertTo(newDoor.axisCenter.getContext());
		else if (newDoor.axisCenter.getContext().size < previews.context.size)
			newDoor.axisCenter.convertTo(previews.context);
	}
	
	@Override
	public DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, LittleAbsolutePreviewsStructure previews, LittleTransformation transformation, int completeDuration) {
		LittleAdvancedDoor newDoor = (LittleAdvancedDoor) previews.getStructure();
		int duration = newDoor.duration;
		
		PairList<AnimationKey, ValueTimeline> open = new PairList<>();
		PairList<AnimationKey, ValueTimeline> close = new PairList<>();
		
		AnimationState opened = new AnimationState();
		AnimationState closed = new AnimationState();
		if (offX != null) {
			opened.set(AnimationKey.offX, offGrid.toVanillaGrid(offX.last(AnimationKey.offX)));
			closed.set(AnimationKey.offX, offGrid.toVanillaGrid(offX.first(AnimationKey.offX)));
			
			open.add(AnimationKey.offX, offX.copy().factor(offGrid.pixelSize));
			close.add(AnimationKey.offX, offX.invert(duration).factor(offGrid.pixelSize));
		}
		if (offY != null) {
			opened.set(AnimationKey.offY, offGrid.toVanillaGrid(offY.last(AnimationKey.offY)));
			closed.set(AnimationKey.offY, offGrid.toVanillaGrid(offY.first(AnimationKey.offY)));
			
			open.add(AnimationKey.offY, offY.copy().factor(offGrid.pixelSize));
			close.add(AnimationKey.offY, offY.invert(duration).factor(offGrid.pixelSize));
		}
		if (offZ != null) {
			opened.set(AnimationKey.offZ, offGrid.toVanillaGrid(offZ.last(AnimationKey.offZ)));
			closed.set(AnimationKey.offZ, offGrid.toVanillaGrid(offZ.first(AnimationKey.offZ)));
			
			open.add(AnimationKey.offZ, offZ.copy().factor(offGrid.pixelSize));
			close.add(AnimationKey.offZ, offZ.invert(duration).factor(offGrid.pixelSize));
		}
		if (rotX != null) {
			opened.set(AnimationKey.rotX, rotX.last(AnimationKey.rotX));
			closed.set(AnimationKey.rotX, rotX.first(AnimationKey.rotX));
			
			open.add(AnimationKey.rotX, rotX);
			close.add(AnimationKey.rotX, rotX.invert(duration));
		}
		if (rotY != null) {
			opened.set(AnimationKey.rotY, rotY.last(AnimationKey.rotY));
			closed.set(AnimationKey.rotY, rotY.first(AnimationKey.rotY));
			
			open.add(AnimationKey.rotY, rotY);
			close.add(AnimationKey.rotY, rotY.invert(duration));
		}
		if (rotZ != null) {
			opened.set(AnimationKey.rotZ, rotZ.last(AnimationKey.rotZ));
			closed.set(AnimationKey.rotZ, rotZ.first(AnimationKey.rotZ));
			
			open.add(AnimationKey.rotZ, rotZ);
			close.add(AnimationKey.rotZ, rotZ.invert(duration));
		}
		
		return new DoorController(result, supplier, closed, opened, stayAnimated ? null : false, duration, completeDuration, new AnimationTimeline(duration, open), new AnimationTimeline(duration, close), interpolation);
	}
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		if (axisCenter == null)
			return new StructureAbsolute(getMainTile().te.getPos(), getMainTile().box, getMainTile().getContext());
		return new StructureAbsolute(lastMainTileVec != null ? lastMainTileVec : getMainTile().getAbsolutePos(), axisCenter);
	}
	
	public static class LittleAdvancedDoorParser extends LittleStructureGuiParser {
		
		public LittleGridContext context;
		
		public LittleAdvancedDoorParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			LittleAdvancedDoor door = structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null;
			List<TimelineChannel> channels = new ArrayList<>();
			channels.add(new TimelineChannelDouble("rot X").addKeys(door != null && door.rotX != null ? door.rotX.getPointsCopy() : null));
			channels.add(new TimelineChannelDouble("rot Y").addKeys(door != null && door.rotY != null ? door.rotY.getPointsCopy() : null));
			channels.add(new TimelineChannelDouble("rot Z").addKeys(door != null && door.rotZ != null ? door.rotZ.getPointsCopy() : null));
			channels.add(new TimelineChannelInteger("off X").addKeys(door != null && door.offX != null ? door.offX.getRoundedPointsCopy() : null));
			channels.add(new TimelineChannelInteger("off Y").addKeys(door != null && door.offY != null ? door.offY.getRoundedPointsCopy() : null));
			channels.add(new TimelineChannelInteger("off Z").addKeys(door != null && door.offZ != null ? door.offZ.getRoundedPointsCopy() : null));
			parent.controls.add(new GuiTimeline("timeline", 0, 0, 190, 67, door != null ? door.duration : 50, channels, handler).setSidebarWidth(30));
			parent.controls.add(new GuiLabel("tick", "0", 150, 75));
			
			context = door != null ? (door.offGrid != null ? door.offGrid : LittleGridContext.get()) : LittleGridContext.get();
			parent.controls.add((GuiControl) new GuiTextfield("keyValue", "", 0, 75, 40, 10).setFloatOnly().setEnabled(false));
			parent.controls.add(new GuiLTDistance("keyDistance", 0, 75, context, 0).setVisible(false));
			
			parent.controls.add(new GuiLabel("Position:", 90, 90));
			parent.controls.add((GuiControl) new GuiTextfield("keyPosition", "", 149, 90, 40, 10).setNumbersOnly().setEnabled(false));
			
			parent.controls.add(new GuiAxisButton("axis", "open axis", 0, 93, 50, 10, previews.context, structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null, handler));
			
			parent.controls.add(new GuiCheckBox("stayAnimated", CoreControl.translate("gui.door.stayAnimated"), 0, 123, structure instanceof LittleAdvancedDoor ? ((LittleDoorBase) structure).stayAnimated : false).setCustomTooltip(CoreControl.translate("gui.door.stayAnimatedTooltip")));
			parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
			parent.controls.add(new GuiTextfield("duration_s", structure instanceof LittleAdvancedDoor ? "" + ((LittleDoorBase) structure).duration : "" + 50, 149, 121, 40, 8).setNumbersOnly());
			parent.controls.add(new GuiCheckBox("rightclick", CoreControl.translate("gui.door.rightclick"), 0, 108, structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true));
			parent.controls.add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
			parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
			updateTimeline();
		}
		
		public void updateTimeline() {
			GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
			GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
			AnimationTimeline animation = new AnimationTimeline(timeline.getDuration(), new PairList<>());
			GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
			int interpolation = interpolationButton.getState();
			
			ValueTimeline rotX = ValueTimeline.create(interpolation, timeline.channels.get(0).getPairs());
			if (rotX != null)
				animation.values.add(AnimationKey.rotX, rotX);
			
			ValueTimeline rotY = ValueTimeline.create(interpolation, timeline.channels.get(1).getPairs());
			if (rotY != null)
				animation.values.add(AnimationKey.rotY, rotY);
			
			ValueTimeline rotZ = ValueTimeline.create(interpolation, timeline.channels.get(2).getPairs());
			if (rotZ != null)
				animation.values.add(AnimationKey.rotZ, rotZ);
			
			ValueTimeline offX = ValueTimeline.create(interpolation, timeline.channels.get(3).getPairs());
			if (offX != null)
				animation.values.add(AnimationKey.offX, offX.factor(context.pixelSize));
			
			ValueTimeline offY = ValueTimeline.create(interpolation, timeline.channels.get(4).getPairs());
			if (offY != null)
				animation.values.add(AnimationKey.offY, offY.factor(context.pixelSize));
			
			ValueTimeline offZ = ValueTimeline.create(interpolation, timeline.channels.get(5).getPairs());
			if (offZ != null)
				animation.values.add(AnimationKey.offZ, offZ.factor(context.pixelSize));
			
			handler.setTimeline(animation, children.events);
			
			GuiCheckBox stayAnimated = (GuiCheckBox) parent.get("stayAnimated");
			stayAnimated.enabled = animation.isFirstAligned();
		}
		
		@SideOnly(Side.CLIENT)
		private KeyControl selected;
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onKeySelected(KeySelectedEvent event) {
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
			
			selected = (KeyControl) event.source;
			
			if (((KeyControl) event.source).value instanceof Double) {
				distance.setVisible(false);
				textfield.setEnabled(true);
				textfield.setVisible(true);
				textfield.text = "" + selected.value;
			} else {
				distance.setEnabled(true);
				distance.setVisible(true);
				textfield.setVisible(false);
				
				distance.setDistance(context, (int) selected.value);
			}
			
			GuiTextfield position = (GuiTextfield) parent.get("keyPosition");
			position.setEnabled(true);
			position.text = "" + selected.tick;
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onChange(GuiControlChangedEvent event) {
			if (event.source.is("keyDistance")) {
				
				if (!selected.modifiable)
					return;
				
				GuiLTDistance distance = (GuiLTDistance) event.source;
				LittleGridContext newContext = distance.getDistanceContext();
				if (newContext.size > context.size) {
					int scale = newContext.size / context.size;
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					for (TimelineChannel channel : timeline.channels) {
						if (channel instanceof TimelineChannelInteger) {
							for (Object control : channel.controls) {
								((KeyControl<Integer>) control).value *= scale;
							}
						}
					}
					context = newContext;
				}
				
				int scale = context.size / newContext.size;
				selected.value = distance.getDistance();
			} else if (event.source.is("keyValue")) {
				if (!selected.modifiable)
					return;
				
				try {
					selected.value = Double.parseDouble(((GuiTextfield) event.source).text);
				} catch (NumberFormatException e) {
					
				}
			} else if (event.source.is("keyPosition")) {
				if (!selected.modifiable)
					return;
				
				try {
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					
					int tick = selected.tick;
					int newTick = Integer.parseInt(((GuiTextfield) event.source).text);
					if (selected.channel.isSpaceFor(selected, newTick)) {
						selected.tick = newTick;
						selected.channel.movedKey(selected);
						if (tick != selected.tick)
							timeline.adjustKeysPositionX();
					}
				} catch (NumberFormatException e) {
					
				}
			} else if (event.source.is("duration_s")) {
				try {
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					timeline.setDuration(Integer.parseInt(((GuiTextfield) event.source).text));
				} catch (NumberFormatException e) {
					
				}
			} else if (event.source.is("timeline") || event.source.is("children_activate") || event.source.is("interpolation"))
				updateTimeline();
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onKeyDeselected(KeyDeselectedEvent event) {
			selected = null;
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			textfield.setEnabled(false);
			textfield.text = "";
			textfield.setCursorPositionZero();
			
			textfield = (GuiTextfield) parent.get("keyPosition");
			textfield.setEnabled(false);
			textfield.text = "";
			textfield.setCursorPositionZero();
			
			GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
			distance.setEnabled(false);
			distance.resetTextfield();
			
			updateTimeline();
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void toolTip(GuiToolTipEvent event) {
			if (event.source.is("timeline")) {
				((GuiLabel) parent.get("tick")).caption = event.tooltip.get(0);
				event.CancelEvent();
			}
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleStructure parseStructure(LittlePreviews previews) {
			LittleAdvancedDoor door = createStructure(LittleAdvancedDoor.class);
			GuiTileViewer viewer = ((GuiAxisButton) parent.get("axis")).viewer;
			GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
			door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
			GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
			door.duration = timeline.getDuration();
			GuiCheckBox checkBox = (GuiCheckBox) parent.get("stayAnimated");
			GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
			GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
			door.events = button.events;
			door.disableRightClick = !rightclick.value;
			door.interpolation = interpolationButton.getState();
			
			door.rotX = ValueTimeline.create(door.interpolation, timeline.channels.get(0).getPairs());
			door.rotY = ValueTimeline.create(door.interpolation, timeline.channels.get(1).getPairs());
			door.rotZ = ValueTimeline.create(door.interpolation, timeline.channels.get(2).getPairs());
			door.offX = ValueTimeline.create(door.interpolation, timeline.channels.get(3).getPairs());
			door.offY = ValueTimeline.create(door.interpolation, timeline.channels.get(4).getPairs());
			door.offZ = ValueTimeline.create(door.interpolation, timeline.channels.get(5).getPairs());
			
			if (!isAligned(AnimationKey.offX, door.offX) || !isAligned(AnimationKey.offY, door.offY) || !isAligned(AnimationKey.offZ, door.offZ) || !isAligned(AnimationKey.rotX, door.rotX) || !isAligned(AnimationKey.rotY, door.rotY) || !isAligned(AnimationKey.rotZ, door.rotZ))
				door.stayAnimated = true;
			else
				door.stayAnimated = checkBox.value;
			door.offGrid = context;
			return door;
		}
		
	}
	
}
