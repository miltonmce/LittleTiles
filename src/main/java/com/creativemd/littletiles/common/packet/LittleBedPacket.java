package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class LittleBedPacket extends CreativeCorePacket {
	
	public LittleIdentifierAbsolute coord;
	public int playerID;
	
	public LittleBedPacket() {
		
	}
	
	public LittleBedPacket(LittleIdentifierAbsolute coord) {
		this.coord = coord;
		this.playerID = -1;
	}
	
	public LittleBedPacket(LittleIdentifierAbsolute coord, EntityPlayer player) {
		this(coord);
		this.playerID = player.getEntityId();
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeAbsoluteCoord(coord, buf);
		buf.writeInt(playerID);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		coord = LittleAction.readAbsoluteCoord(buf);
		playerID = buf.readInt();
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		Entity entity = playerID == -1 ? player : player.world.getEntityByID(playerID);
		if (entity instanceof EntityPlayer) {
			LittleTile tile;
			try {
				tile = LittleAction.getTile(player.world, coord);
				LittleStructure structure;
				if (tile.isConnectedToStructure() && (structure = tile.connection.getStructure(player.world)) instanceof LittleBed) {
					((LittleBed) structure).trySleep((EntityPlayer) entity, structure.getHighestCenterVec());
				}
			} catch (LittleActionException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
