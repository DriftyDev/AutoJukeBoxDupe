package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import events.client.EventClientTick;
import util.entity.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import module.Module;
import module.Value;
import util.TickedTimer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class AutoDuperModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"Mode"}, "The Mode to use", Modes.Frame);
    public final Value<Integer> Ticks = new Value<Integer>("Tick delay", new String[] {"Ticks"}, "Tick delay", 3, 0, 20, 1);
    public final Value<Integer> DupePerTick = new Value<Integer>("Dupes per Tick", new String[] {"DupeTick"}, "Dupe per Tick", 1, 0, 5, 1);
    public final Value<Boolean> DoubleJukebox = new Value<Boolean>("Bypass", new String[] {"Bypass"}, "Jukebox Bypass", true);

    private TickedTimer tickedTimer;

    public enum Modes {
        Frame,
        Jukebox
    }

    public AutoDuperModule()
    {
        super("AutoDuper", new String[] {"Duper"}, "Auto Duper", "NONE", 0xFF0000, ModuleType.EXPLOIT);
        tickedTimer = new TickedTimer();
        tickedTimer.stop();
    }

    private boolean Sending = false;
    private Entity entity;
    private boolean has_disc;
    private BlockJukebox.TileEntityJukebox jukeboxEntity;
    private BlockPos l_pos;

    @Override
    public void onEnable()
    {
        super.onEnable();
        switch (Mode.getValue()){
            case Frame:
                entity = null;
                Sending = false;
            case Jukebox:
                jukeboxEntity = null;
                l_pos = null;
        }
        tickedTimer.start();
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        tickedTimer.stop();
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
    }

    @Override
    public void toggleNoSave()
    {

    }

    public boolean isValidDisc(int slot) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_13) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_CAT) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_BLOCKS) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_CHIRP) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_FAR) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_MALL) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_MELLOHI) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_STAL) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_STRAD) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_WARD) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_11) {
            return true;
        } else if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.RECORD_WAIT) {
            return true;
        } else {
            return false;
        }
    }

    public void switchSlot(int slot) {
        if (slot == -1) return;
        if (mc.player.inventory.getStackInSlot(slot).getItem() == mc.player.inventory.getStackInSlot(45).getItem()) return;
        if (!isValidDisc(slot)) return;
        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.updateController();
    }

    private int getItemSlot() {
        for (int i = 36; i >= 0; i--) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (isValidDisc(i)) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }

    public static java.util.List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plusY) {
        List<BlockPos> circleBlocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plusY, z);
                        circleBlocks.add(l);
                    }
                }
            }
        }
        return circleBlocks;
    }


    private boolean isEmpty(EntityItemFrame itemFrame) {
        if (itemFrame.getDisplayedItem() == null) {
            return true;
        }
        return false;
    }

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event ->
    {
        if (!tickedTimer.passed(Ticks.getValue().intValue()))
            return;
        switch (Mode.getValue()) {
            case Frame:

                // no frame dupe src for u

            case Jukebox:

                if (!tickedTimer.passed(Ticks.getValue().intValue()))
                    return;

                // Block list
                List<BlockPos> blockPosList = getSphere(new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), 4, 4, false, true, 0);
                for (BlockPos pos : blockPosList) {

                    // Block checking

                    if (mc.world.getBlockState(pos).getBlock().equals(Blocks.JUKEBOX)) {
                        l_pos = pos;
                        for (TileEntity entity : mc.world.loadedTileEntityList) {
                            if (entity instanceof BlockJukebox.TileEntityJukebox) {
                                jukeboxEntity = (BlockJukebox.TileEntityJukebox) entity;
                                if (jukeboxEntity.getRecord().isEmpty()) {
                                    has_disc = false;
                                }
                            }
                        }
                        continue;
                    }
                }

                //RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double) l_pos.getX() + 0.5, (double) l_pos.getY() - 0.5, (double) l_pos.getZ() + 0.5));
                //EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;

                // Checks for shulker
                if (mc.player.getHeldItemMainhand() == null || mc.player.getHeldItemMainhand().getItem() == Items.AIR)
                    return;

                int slotDisc = getItemSlot();
                if (mc.player.getHeldItemOffhand() == null || mc.player.getHeldItemOffhand().getItem() == Items.AIR)
                    switchSlot(slotDisc);

                /// @todo: Send the packets
                // Enter disc packet

                if (DoubleJukebox.getValue()) {
                    if (!mc.player.isSneaking()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    }
                    if (!has_disc) {
                         mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(l_pos, EnumFacing.UP, EnumHand.OFF_HAND, 0, 0, 0));
                    }
                    int dSlotDisc = getItemSlot();
                    if (mc.player.getHeldItemOffhand() == null || mc.player.getHeldItemOffhand().getItem() == Items.AIR) {
                        switchSlot(dSlotDisc);
                    }
                }
                for (int l_I = 0; l_I < DupePerTick.getValue(); ++l_I) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(l_pos, EnumFacing.UP, has_disc ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, 0, 0, 0));
                    has_disc = !has_disc;
                }
        }
        tickedTimer.reset();
    });
}
