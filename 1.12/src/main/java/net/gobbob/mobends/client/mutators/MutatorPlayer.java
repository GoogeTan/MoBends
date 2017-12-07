package net.gobbob.mobends.client.mutators;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBipedArmorM;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

/*
 * Instantiated one per RenderPlayer
 */
public class MutatorPlayer implements IBendsModel
{
	public static HashMap<RenderPlayer, MutatorPlayer> mutatorMap = new HashMap<RenderPlayer, MutatorPlayer>();
	
	public ModelPart body;
	public ModelPartChild head;
	public ModelPartChildExtended leftArm;
	public ModelPartChildExtended rightArm;
	public ModelPartChildExtended leftForeArm;
	public ModelPartChildExtended rightForeArm;
	public ModelPartExtended leftLeg;
	public ModelPartExtended rightLeg;
	public ModelPartChild leftForeLeg;
	public ModelPartChild rightForeLeg;
	
	public ModelPartChild bodywear;
	public ModelPartChild headwear;
	public ModelPartChild leftArmwear;
	public ModelPartChild rightArmwear;
	public ModelPartChild leftForeArmwear;
	public ModelPartChild rightForeArmwear;
	public ModelPartChild leftLegwear;
	public ModelPartChild rightLegwear;
	public ModelPartChild leftForeLegwear;
	public ModelPartChild rightForeLegwear;
	
	public ModelPartTransform leftItemTransform;
	public ModelPartTransform rightItemTransform;
	public float headYaw, headPitch;
	public boolean smallArms;
	public List<LayerRenderer<EntityLivingBase>> layerRenderers;
	public LayerBipedArmorM layerArmor;
	
	protected HashMap<String, IModelPart> nameToPartMap;
	
	public MutatorPlayer()
	{
		this.nameToPartMap = new HashMap<String, IModelPart>();
	}
	
	private static Field getField(Class classIn, String fieldName) throws NoSuchFieldException
	{
		try
		{
			return classIn.getDeclaredField(fieldName);
		}
		catch (NoSuchFieldException e)
		{
			Class superClass = classIn.getSuperclass();
			if (superClass == null)
			{
				throw e;
			}
			else
			{
				return getField(superClass, fieldName);
			}
		}
	}
	
	private static Field getObfuscatedField(Class classIn, String fieldName, String fieldNameObfuscated) {
		Field field = null;
		
		try
		{
			field = getField(classIn, fieldNameObfuscated);
		}
		catch (NoSuchFieldException e)
		{
			try
			{
				field = getField(classIn, fieldName);
			}
			catch (NoSuchFieldException e1) {}
		}
		catch (SecurityException | IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		
		return field;
	}
	
	public void fetchFields(RenderPlayer renderer) {
		// Getting the layer renderers
		this.layerRenderers = null;
		Field fieldLayers = getObfuscatedField(renderer.getClass(), "layerRenderers", "field_177097_h");
		if(fieldLayers != null) {
			fieldLayers.setAccessible(true);
			List<LayerRenderer<EntityLivingBase>> layers = null;
			try {
				layers = (List<LayerRenderer<EntityLivingBase>>) fieldLayers.get(renderer);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
			this.layerRenderers = layers;
		}
		
		// Does the renderer have Small Arms?
		//TODO Find out the obfuscated name for the smallArms field.
		Field fieldSmallArms = getObfuscatedField(renderer.getClass(), "smallArms", "field_177140_a");
		if(fieldSmallArms != null) {
			fieldSmallArms.setAccessible(true);
			try {
				this.smallArms = (boolean) fieldSmallArms.get(renderer);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		
		Field[] fields = renderer.getClass().getDeclaredFields();
		for(Field f : fields) {
			System.out.println("Field: " + f);
		}
	}
	
	public void mutate(AbstractClientPlayer entityPlayer, RenderPlayer renderer)
	{
		fetchFields(renderer);
		
		if(this.layerRenderers != null)
		{
			for(int i = 0; i < layerRenderers.size(); ++i)
			{
				LayerRenderer<EntityLivingBase> layer = layerRenderers.get(i);
				if(layer instanceof LayerBipedArmor)
				{
					this.layerArmor = new LayerBipedArmorM(renderer);
					layerRenderers.set(i, this.layerArmor);
					System.out.println(layer);
				}
			}
		}
		
		// Model Section
		ModelPlayer model = renderer.getMainModel();
		float scaleFactor = 0.0f;
		
		// Body
		model.bipedBody = body = new ModelPart(model, 16, 16).setPosition(0.0F, 12.0F, 0.0F);
		body.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);
		
		// Head
		model.bipedHead = head = (ModelPartChild) new ModelPartChild(model, 0, 0)
				.setParent(body).setHideLikeParent(false)
				.setPosition(0.0F, -12.0F, 0.0F);
		head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
		
		// Arms
		int armWidth = this.smallArms ? 3 : 4;
		float armHeight = this.smallArms ? -9.5F : -10F;
		
		model.bipedLeftArm = leftArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 32, 48)
				.setParent(body).setHideLikeParent(false)
				.setPosition(5.0F, armHeight, 0.0F);
		leftArm.addBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
		model.bipedRightArm = rightArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 40, 16)
				.setParent(body).setHideLikeParent(false)
				.setPosition(-5.0F, armHeight, 0.0F);
		rightArm.addBox(-armWidth+1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
		
		rightArm.offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(armWidth + 0.02f, 6.0f, 4.02f).updateVertices();
        leftArm.offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(armWidth + 0.02f, 6.0f, 4.02f).updateVertices();
    
        leftForeArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 32, 48+6).setParent(leftArm);
        leftForeArm.addBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor);
        leftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
        leftForeArm.getBox().offsetTextureQuad(leftForeArm, ModelBox.BOTTOM, 0, -6.0f);
        leftArm.setExtension(leftForeArm);
        rightForeArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 40, 16+6).setParent(rightArm);
        rightForeArm.addBox(-armWidth+1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor);
        rightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
        rightForeArm.getBox().offsetTextureQuad(rightForeArm, ModelBox.BOTTOM, 0, -6.0f);
        rightArm.setExtension(rightForeArm);
        
        // Items
        leftItemTransform = new ModelPartTransform();
        leftForeArm.setExtension(leftItemTransform);
        rightItemTransform = new ModelPartTransform();
        rightForeArm.setExtension(rightItemTransform);
        
        // Legs
        model.bipedRightLeg = rightLeg = (ModelPartExtended) new ModelPartExtended(model, 0, 16)
        		.setPosition(-1.9F, 12.0F, 0.0F);
        rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
        model.bipedLeftLeg = leftLeg = (ModelPartExtended)  new ModelPartExtended(model, 16, 48)
        		.setPosition(1.9F, 12.0F, 0.0F);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
        
        leftForeLeg = (ModelPartChild) new ModelPartChild(model, 16, 48+6).setParent(leftLeg).setPosition(0, 6.0F, -2.0F);
        leftForeLeg.addModelBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
        		.offsetTextureQuad(leftForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        leftLeg.setExtension(leftForeLeg);
        rightForeLeg = (ModelPartChild) new ModelPartChild(model, 0, 16+6).setParent(rightLeg).setPosition(0, 6.0F, -2.0F);
        rightForeLeg.addModelBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
        		.offsetTextureQuad(rightForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        rightLeg.setExtension(rightForeLeg);
        
        // Wear
        model.bipedHeadwear = headwear = new ModelPartChild(model, 32, 0).setParent(head);
        headwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        
        model.bipedLeftArmwear = leftArmwear = new ModelPartChild(model, 48, 48).setParent(leftArm);
        leftArmwear.addBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + 0.25F);
        ((ModelPart)leftArmwear).getBox().resY-=0.25f;
        ((ModelPart)leftArmwear).getBox().updateVertexPositions(leftArmwear);
        model.bipedRightArmwear = rightArmwear = new ModelPartChild(model, 40, 32).setParent(rightArm);
        rightArmwear.addBox(-armWidth+1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + 0.25F);
        ((ModelPart)rightArmwear).getBox().resY-=0.25f;
        ((ModelPart)rightArmwear).getBox().updateVertexPositions(rightArmwear);
        
        model.bipedLeftLegwear = leftLegwear = new ModelPartChild(model, 0, 48).setParent(leftLeg);
        leftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F);
        ((ModelPart)leftLegwear).getBox().resY-=0.25f;
        ((ModelPart)leftLegwear).getBox().updateVertexPositions(leftLegwear);
        ((ModelPart)leftLegwear).getBox().offsetTextureQuad(leftLegwear, ModelBox.BOTTOM, 4F, 0F);
        leftLegwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        model.bipedRightLegwear = rightLegwear = new ModelPartChild(model, 0, 32).setParent(rightLeg);
        rightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F);
        ((ModelPart) rightLegwear).getBox().resY-=0.25f;
        ((ModelPart) rightLegwear).getBox().updateVertexPositions(rightLegwear);
        ((ModelPart) rightLegwear).getBox().offsetTextureQuad(rightLegwear, ModelBox.BOTTOM, 4F, 0F);
        model.bipedBodyWear = bodywear = new ModelPartChild(model, 16, 32).setParent(body);
        bodywear.addBox(-4F, -12F, -2F, 8, 12, 4, scaleFactor + 0.25F);
        
        
        nameToPartMap = new HashMap<String, IModelPart>();
        nameToPartMap.put("body", body);
        nameToPartMap.put("head", head);
        nameToPartMap.put("leftArm", leftArm);
        nameToPartMap.put("rightArm", rightArm);
        nameToPartMap.put("leftLeg", leftLeg);
        nameToPartMap.put("rightLeg", rightLeg);
        nameToPartMap.put("leftForeArm", leftForeArm);
        nameToPartMap.put("rightForeArm", rightForeArm);
        nameToPartMap.put("leftForeLeg", leftForeLeg);
        nameToPartMap.put("rightForeLeg", rightForeLeg);
	}
	
	public void updateModel(AbstractClientPlayer player, RenderPlayer renderer, float partialTicks)
	{
		boolean shouldSit = player.isRiding() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit());
		float f = GUtil.interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
        float f1 = GUtil.interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
        float yaw = f1 - f;
        
        if (shouldSit && player.getRidingEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase) player.getRidingEntity();
            f = GUtil.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            yaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(yaw);

            if (f3 < -85.0F)
                f3 = -85.0F;
            if (f3 >= 85.0F)
                f3 = 85.0F;

            f = f1 - f3;

            if (f3 * f3 > 2500.0F)
                f += f3 * 0.2F;

            yaw = f1 - f;
        }

        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!player.isRiding())
        {
            f5 = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTicks;
            f6 = player.limbSwing - player.limbSwingAmount * (1.0F - partialTicks);

            if (player.isChild())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
            yaw = f1 - f;
        }
        
        this.headYaw = yaw;
        this.headPitch = pitch;
        performAnimations(player, renderer, yaw, pitch, partialTicks);
	}
	
	public void performAnimations(AbstractClientPlayer player, RenderPlayer renderer, float yaw, float pitch, float partialTicks)
	{
		EntityData entityData = EntityDatabase.instance.getAndMake(DataPlayer.class, player);
		if(!(entityData instanceof DataPlayer))
			return;
		DataPlayer data = (DataPlayer) entityData;
    	AnimatedEntity animatedEntity = AnimatedEntity.getByEntity(player);
		float ticks = player.ticksExisted + partialTicks;
		
		leftItemTransform.position.setY(-3);
		rightItemTransform.position.setY(-3);
		
		data.setHeadYaw(this.headYaw);
		data.setHeadPitch(this.headPitch);
		
		Controller controller = data.getController();
		if(controller != null) {
			controller.perform(data);
		}
		
		// Sync up with the EntityData
		head.syncUp(data.head);
		body.syncUp(data.body);
		leftArm.syncUp(data.leftArm);
		rightArm.syncUp(data.rightArm);
		leftLeg.syncUp(data.leftLeg);
		rightLeg.syncUp(data.rightLeg);
		leftForeArm.syncUp(data.leftForeArm);
		rightForeArm.syncUp(data.rightForeArm);
		leftForeLeg.syncUp(data.leftForeLeg);
		rightForeLeg.syncUp(data.rightForeLeg);
		
		//animatedEntity.getAnimation("stand").animate((EntityLivingBase)player, this, data);
        //BendsPack.animate(this, "player", "stand");
	}
	
	public static void apply(RenderPlayer renderer, AbstractClientPlayer entityPlayer, float partialTicks)
	{
		MutatorPlayer mutator = mutatorMap.get(renderer);
		if(!mutatorMap.containsKey(renderer))
		{
			mutator = new MutatorPlayer();
			mutator.mutate(entityPlayer, renderer);
			mutatorMap.put(renderer, mutator);
		}
		
		mutator.updateModel(entityPlayer, renderer, partialTicks);
	}
	
	/*
	 * Used to refresh the mutators in case of real-time
	 * changes during development.
	 */
	public static void refresh() {
		for(Map.Entry<RenderPlayer, MutatorPlayer> mutator : mutatorMap.entrySet()) {
			mutator.getValue().mutate(null, mutator.getKey());
			mutator.getValue().layerArmor.initArmor();
		}
	}

	@Override
	public Object getPartForName(String name)
	{
		return nameToPartMap.get(name);
	}
}
