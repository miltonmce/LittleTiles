package com.creativemd.littletiles.common.utils.animation;

import java.util.Set;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;

import net.minecraft.nbt.NBTTagCompound;

public class AnimationState {
	
	private PairList<AnimationKey, Double> values = new PairList<>();
	
	public double get(AnimationKey key) {
		Double value = values.getValue(key);
		if (value == null)
			return key.getDefault();
		return value;
	}
	
	public AnimationState set(AnimationKey key, double value) {
		Pair<AnimationKey, Double> pair = values.getPair(key);
		if (pair != null)
			pair.setValue(value);
		else
			values.add(key, value);
		return this;
	}
	
	public AnimationState(NBTTagCompound nbt) {
		for (AnimationKey key : AnimationKey.getKeys())
			if (nbt.hasKey(key.name))
				values.add(key, nbt.getDouble(key.name));
	}
	
	public AnimationState() {
		
	}
	
	public Vector3d getRotation() {
		return new Vector3d(get(AnimationKey.rotX), get(AnimationKey.rotY), get(AnimationKey.rotZ));
	}
	
	public Vector3d getOffset() {
		return new Vector3d(get(AnimationKey.offX), get(AnimationKey.offY), get(AnimationKey.offZ));
	}
	
	public void clear() {
		values.clear();
	}
	
	public Set<AnimationKey> keys() {
		return values.keys();
	}
	
	public PairList<AnimationKey, Double> getValues() {
		return values;
	}
	
	public boolean isAligned() {
		for (Pair<AnimationKey, Double> pair : values)
			if (!pair.key.isAligned(pair.value))
				return false;
		return true;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		for (Pair<AnimationKey, Double> pair : values)
			nbt.setDouble(pair.key.name, pair.value);
		return nbt;
	}
}
