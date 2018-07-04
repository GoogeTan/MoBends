package net.gobbob.mobends.animation.bit.zombie;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.ZombieData;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieLeanAnimationBit extends AnimationBit
{

	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] {};
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof ZombieData))
			return;
		if (!(entityData.getEntity() instanceof EntityZombie))
			return;
		
		ZombieData data = (ZombieData) entityData;
		
		data.renderOffset.slideY(-3F);
		
		data.body.rotation.localRotateX(30F);
		data.head.rotation.rotateX(-30F);
		
		data.rightArm.rotation.rotateX(-30F);
		data.leftArm.rotation.rotateX(-30F);
		
		data.rightLeg.rotation.rotateZ(10F);
		data.leftLeg.rotation.rotateZ(-10F);
		
		data.rightLeg.rotation.rotateX(-20);
		data.leftLeg.rotation.rotateX(-20);
		
		data.rightForeLeg.rotation.rotateX(25);
		data.leftForeLeg.rotation.rotateX(25);
		
		if(!data.isStillHorizontally() && data.getCurrentWalkingState() == 1)
		{
			data.rightArm.rotation.orientX(-90 - 30.0f);
			data.leftArm.rotation.orientX(-90 - 30.0f);
		}
	}
}