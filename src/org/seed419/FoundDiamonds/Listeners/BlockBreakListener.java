package org.seed419.FoundDiamonds.Listeners;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.seed419.FoundDiamonds.*;

public class BlockBreakListener implements Listener  {

    private FoundDiamonds fd;
    private static final Logger log = Logger.getLogger("FoundDiamonds");
    private List<Block> blockList;
    private List<Block> checkedBlocks;
    private List<Player> recievedAdminMessage = new LinkedList<Player>();
    private boolean consoleRecieved;


    public BlockBreakListener(FoundDiamonds instance) {
        fd = instance;
    }




    /*
     * BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        //Check to see if the block is a light level block.
        if (fd.getLightLevelBlocks().contains(mat))
        if (!isAbleToMineAtSpecifiedLightLevel(ei)) {
            event.setCancelled(true);
            return;
        }

        //See if block was placed by a player
        if (wasPlacedRemove(ei.getLocation())) {
            if (debug) {
                log.info(FoundDiamonds.getDebugPrefix() + " That block was placed.");
            }
            return;
        }

        // Handle the material
        materialNeedsHandled(ei);

        // After broadcast, admin messages, then log the material.
        if (mat == Material.DIAMOND_ORE) {
            if (fd.getConfig().getBoolean(Config.logDiamondBreaks)) {
                handleLogging(ei.getPlayer(), ei.getBlock(), false, false, false);
            }
        }
    }




    /*
     * Main handler
     */
    private void materialNeedsHandled(EventInformation ei) {
        if (alreadyAnnounced(ei.getLocation())) {
            fd.getAnnouncedBlocks().remove(ei.getLocation());
            if (debug) {
                log.info(FoundDiamonds.getDebugPrefix() + " Broadcast canceled: Block already announced.");
            }
            return;
        }

        //TODO admin messages
        if (!fd.hasPerms(ei.getPlayer(), "fd.admin")) {
            //isEnabledAdminMessageMaterial(player, mat, block);
        }
        if (!isValidWorld(ei.getPlayer())) {
            if (debug) {
                log.info(FoundDiamonds.getDebugPrefix() + " Broadcast canceled: User is not in an enabled world.");
            }
            return;
        }
        if (!isValidGameMode(ei.getPlayer())) {
            if (debug) {
                log.info(FoundDiamonds.getDebugPrefix() + " Broadcast canceled: User is in creative mode.");
            }
             return;
        }
        String playername = getBroadcastName(ei.getPlayer());
        handleBroadcast(ei, playername);
    }




    /*
     * Placed block methods
     */
    private boolean wasPlacedRemove(Location loc) {
        if (fd.getPlacedBlocks().contains(loc)) {
            fd.getPlacedBlocks().remove(loc);
            return true;
        }
        return false;
    }

    private boolean wasPlaced(Block block) {
        return (fd.getPlacedBlocks().contains(block.getLocation()));
    }




    /*
     * Admin Message Handlers
     */

    //TODO needs a new list
    private void isEnabledAdminMessageMaterial(Player player, Material mat, Block block) {
        if (!alreadyAnnounced(block.getLocation())) {
            if (fd.getAdminMessageBlocks().contains(mat)) {
//            if ((mat == Material.DIAMOND_ORE && fd.getConfig().getBoolean(config.getDiamondAdmin())) ||
//                (mat == Material.GOLD_ORE && fd.getConfig().getBoolean(config.getGoldAdmin())) ||
//                (mat == Material.LAPIS_ORE && fd.getConfig().getBoolean(config.getLapisAdmin())) ||
//                (mat == Material.IRON_ORE && fd.getConfig().getBoolean(config.getIronAdmin())) ||
//                (mat == Material.GLOWING_REDSTONE_ORE && fd.getConfig().getBoolean(config.getRedstoneAdmin())) ||
//                (mat == Material.REDSTONE_ORE && fd.getConfig().getBoolean(config.getRedstoneAdmin()))) {
                //sendAdminMessage(player, block);
            }
        }
    }

    //Maybe instead of ignoring those with permission fd.messages, use a separate permission for ignoring?
/*    private void sendAdminMessage(Player player, Block block) {
        //EventInformation b = getBlockInformation(block);
        String playerName = player.getName();
        String message = Format.formatMessage(fd, config, FoundDiamonds.getAdminPrefix(), b.getMaterial(), b.getColor(), b.getTotal(), playerName);
        //This is incredibly confusing, but must be done.
        String formatted = customTranslateAlternateColorCodes('&', message);
        fd.getServer().getConsoleSender().sendMessage(formatted);
        consoleRecieved = true;
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getAdminMessageMap().containsKey(y)) {
                if (fd.getAdminMessageMap().get(y)) {
                    y.sendMessage(formatted);
                    recievedAdminMessage.add(y);
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + "Sent admin message to " + y.getName());
                    }
                } else {
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + y.getName() + "'s admin messages are toggled off.");
                    }
                }
            } else {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + y.getName() + " doesn't have permission fd.messages");
                }
            }
        }
    }*/

    private void sendLightAdminMessage(EventInformation ei, int lightLevel) {
        String lightAdminMessage = FoundDiamonds.getAdminPrefix() + ChatColor.YELLOW + ei.getPlayer().getName() +
                ChatColor.GRAY +" was denied mining " + ChatColor.YELLOW +
                ei.getMatName() + ChatColor.GRAY + " at" + " light level " + ChatColor.WHITE +  lightLevel;
        fd.getServer().getConsoleSender().sendMessage(lightAdminMessage);
        for (Player y : fd.getServer().getOnlinePlayers()) {
            if (fd.getAdminMessageMap().containsKey(y)) {
                if (fd.getAdminMessageMap().get(y)) {
                    if (y != ei.getPlayer()) {
                        y.sendMessage(lightAdminMessage);
                        if (debug) {
                            log.info(FoundDiamonds.getDebugPrefix() + "Sent admin message to " + y.getName());
                        }
                    } else {
                        if (debug) {
                            log.info(FoundDiamonds.getDebugPrefix() +y.getName() + " was not sent an admin message because it was them who was denied mining.");
                        }
                    }
                } else {
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + y.getName() + "'s admin messages are toggled off");
                    }
                }
            } else {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + y.getName() + " either doesn't have permission 'fd.admin' or needs to turn them on with /fd toggle admin");
                }
            }
        }
    }




    /*
     * Trap block handlers
     */
    private boolean isTrapBlock(Block block) {
        if (fd.getTrapBlocks().contains(block.getLocation())) {
            return true;
        }
        return false;
    }

    private void removeTrapBlock(Block block) {
        fd.getTrapBlocks().remove(block.getLocation());
    }

    private void handleTrapBlock(Player player, Block block, BlockBreakEvent event) {
        if(fd.getConfig().getBoolean(Config.adminAlertsOnAllTrapBreaks)) {
            for (Player x: fd.getServer().getOnlinePlayers()) {
                if(fd.hasPerms(x, "FD.admin") && (x != player)) {
                    x.sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " " + player.getName()
                            + " just broke a trap block");
                }
            }
        }
        if (fd.hasPerms(player, "fd.trap")) {
            player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Trap block removed");
            event.setCancelled(true);
            block.setType(Material.AIR);
        } else {
            fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " " +  player.getName()
                    + " just broke a trap block");
            event.setCancelled(true);
        }
        boolean banned = false;
        boolean kicked = false;
        if (fd.getConfig().getBoolean(Config.kickOnTrapBreak)  && !fd.hasPerms(player, "FD.trap")) {
            player.kickPlayer(fd.getConfig().getString(Config.kickMessage));
            kicked = true;
        }
        if (fd.getConfig().getBoolean(Config.banOnTrapBreak) && !fd.hasPerms(player, "FD.trap")) {
            player.setBanned(true);
            banned = true;
        }
        if((fd.getConfig().getBoolean(Config.logTrapBreaks)) && (!fd.hasPerms(player, "fd.trap"))) {
            handleLogging(player, block, true, kicked, banned);
        }
        removeTrapBlock(block);
    }




    /*
     * Logging Handlers
     */
    private void handleLogging(Player player, Block block, boolean trapBlock, boolean kicked, boolean banned) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fd.getLogFile(), true)));
            pw.print("[" + getFormattedDate() + "]");
            if (trapBlock) {
                pw.print(" [TRAP BLOCK]");
            }
            pw.println(" " + block.getType().name().toLowerCase().replace("_", " ") + " broken by "
                    + player.getName() + " at (x: " + block.getX() + ", y: " + block.getY() + ", z: " + block.getZ()
                    + ") in " + player.getWorld().getName());
            if (trapBlock) {
                pw.print("[" + getFormattedDate() + "]" + " [ACTION TAKEN] ");
                if (kicked && !banned) {
                    pw.println(player.getName() + " was kicked from the sever.");
                } else if (banned && !kicked) {
                    pw.println(player.getName() + " was banned from the sever.");
                } else if (banned && kicked) {
                    pw.println(player.getName() + " was kicked and banned from the sever.");
                } else if (!banned && !kicked) {
                    pw.println(player.getName() + " was neither kicked nor banned per the configuration.");
                }
            }
            pw.flush();
            fd.close(pw);
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to write to log file {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    private void logLightLevelViolation(EventInformation ei,  int lightLevel) {
        String lightLogMsg = "[" + getFormattedDate() + "]" + " " + ei.getPlayer().getName() + " was denied mining "
                + ei.getMatName() + " at" + " light level " +  lightLevel;
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fd.getLogFile(), true)));
            pw.println(lightLogMsg);
            pw.flush();
            fd.close(pw);
        } catch (IOException ex) {
            log.severe(MessageFormat.format("[{0}] Unable to write to log file {1}", FoundDiamonds.getPrefix(), ex));
        }
    }

    private void writeToCleanLog(EventInformation ei, String playerName) {
        String formattedDate = getFormattedDate();
        String message;
        if (ei.getMaterial() == Material.GLOWING_REDSTONE_ORE || ei.getMaterial() == Material.REDSTONE_ORE) {
            if (ei.getTotal() > 1) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "redstone ores");
            } else {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "redstone ore");
            }
        } else if (ei.getMaterial() == Material.OBSIDIAN) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", "obsidian");
        } else {
            String blockName = ei.getMatName();
            if (ei.getTotal() > 1) {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", blockName +
                        (ei.getMaterial() == Material.DIAMOND_ORE ? "s!" : "s"));
            } else {
                message = fd.getConfig().getString(Config.bcMessage).replace("@Player@", playerName
                        ).replace("@Number@", String.valueOf(ei.getTotal())).replace("@BlockName@", blockName +
                        (ei.getMaterial() == Material.DIAMOND_ORE ? "!" : ""));
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fd.getCleanLog(), true)));
            pw.println("[" + formattedDate + "] " + message);
            pw.flush();
            fd.close(pw);
        } catch (IOException ex) {
            Logger.getLogger(BlockBreakListener.class.getName()).log(Level.SEVERE, "Couldn't write to clean log!", ex);
        }
    }

    private String getFormattedDate() {
        Date todaysDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
        return formatter.format(todaysDate);
    }




    /*
     * Random Item methods
     */
    private void handleRandomItems(int randomNumber) {
        int randomItem;
        if (randomNumber < 50) {
            randomItem = fd.getConfig().getInt(Config.randomItem1);
        } else if (randomNumber >= 50 && randomNumber < 100) {
            randomItem = fd.getConfig().getInt(Config.randomItem2);
        } else {
            randomItem = fd.getConfig().getInt(Config.randomItem3);
        }
        int amount = getRandomAmount();
        broadcastRandomItem(randomItem, amount);
        giveItems(randomItem, amount);
    }

    private void broadcastRandomItem(int item, int amount) {
        fd.getServer().broadcastMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " Everyone else got " + amount +
        " " + Format.getFormattedName(Material.getMaterial(item), amount));
    }

    @SuppressWarnings("deprecation")
    private void giveItems(int item, int amount) {
        for(Player p: fd.getServer().getOnlinePlayers()) {
            p.getInventory().addItem(new ItemStack(item, amount));
            p.updateInventory();
        }
    }

    private int getRandomAmount(){
        Random rand = new Random();
        int amount = rand.nextInt(3);
        return amount;
    }




    /*
     * Spells
     */

    private void handleRandomPotions(int randomNumber) {
        PotionEffect potion;
        String potionMessage;
        int strength = fd.getConfig().getInt(Config.potionStrength);
        if (randomNumber < 25) {
            potion = new PotionEffect(PotionEffectType.SPEED, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.speed);
        } else if (randomNumber >= 25 && randomNumber < 50) {
            potion = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.strength);
        } else if (randomNumber >=50 && randomNumber < 100) {
            potion = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.resist);
        } else if (randomNumber >=100 && randomNumber < 125) {
            potion = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.fireresist);
        } else if (randomNumber >=125 && randomNumber < 150) {
            potion = new PotionEffect(PotionEffectType.FAST_DIGGING, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.fastdig);
        } else if (randomNumber >=150 && randomNumber < 175) {
            potion = new PotionEffect(PotionEffectType.WATER_BREATHING, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.waterbreathe);
        } else if (randomNumber >=175 && randomNumber < 200) {
            potion = new PotionEffect(PotionEffectType.REGENERATION, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.regeneration);
        } else {
            potion = new PotionEffect(PotionEffectType.JUMP, 3000, strength);
            potionMessage = fd.getConfig().getString(Config.jump);
        }
        givePotions(potion, potionMessage);
    }

    private void givePotions(PotionEffect potion, String potionMsg) {
        for (Player p : fd.getServer().getOnlinePlayers()) {
            if (!p.hasPotionEffect(potion.getType()) && fd.getConfig().getList(Config.enabledWorlds).contains(p.getWorld().getName())) {
                p.addPotionEffect(potion);
                if (potion.getType() == PotionEffectType.JUMP) {
                    fd.getJumpPotion().put(p, Boolean.TRUE);
                }
                p.sendMessage(FoundDiamonds.getPrefix() + ChatColor.DARK_RED + " " + potionMsg);
            }
        }
        sendPotionMessageToConsole(potion);
    }

    private void sendPotionMessageToConsole(PotionEffect potion) {
       if (potion.getType() == PotionEffectType.SPEED) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A speed potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.FIRE_RESISTANCE) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A fire resistance potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.INCREASE_DAMAGE) {
           log.info(FoundDiamonds.getLoggerPrefix() + " An attack buff potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.JUMP) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A jump potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A damage resistance potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.FAST_DIGGING) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A fast digging potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.REGENERATION) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A regeneration potion has been awarded to the players");
       } else if (potion.getType() == PotionEffectType.WATER_BREATHING) {
           log.info(FoundDiamonds.getLoggerPrefix() + " A water breathing potion has been awarded to the players");
       }

    }




    /*
     * Broadcasting
     */

    private void handleBroadcast(EventInformation ei, String playername) {
        if (ei.getMaterial() == Material.DIAMOND_ORE) {
            broadcastFoundBlock(ei, playername);
            if (fd.getConfig().getBoolean(Config.itemsForFindingDiamonds)) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(Config.chanceToGetItem)) {
                    int randomNumber = (int)(Math.random()*150);
                    if (randomNumber >= 0 && randomNumber <= 150) {
                        handleRandomItems(randomNumber);
                    }
                }
            }
            if (fd.getConfig().getBoolean(Config.potionsForFindingDiamonds)) {
                int randomInt = (int) (Math.random()*100);
                if (randomInt <= fd.getConfig().getInt(Config.chanceToGetPotion)) {
                    int randomNumber = (int)(Math.random()*225);
                    if (randomNumber >= 0 && randomNumber <= 225) {
                        handleRandomPotions(randomNumber);
                    }
                }
            }
        } else {
            broadcastFoundBlock(ei, playername);
        }
    }

    private void broadcastFoundBlock(EventInformation ei, String playerName) {
        String matName = Format.getFormattedName(ei.getMaterial(), ei.getTotal());
        String message = fd.getConfig().getString(Config.bcMessage).replace("@Prefix@", FoundDiamonds.getPrefix() + ei.getColor()).replace("@Player@",
                playerName +  (fd.getConfig().getBoolean(Config.useOreColors) ? ei.getColor() : "")).replace("@Number@",
                (ei.getTotal() == 1000 ? "a lot of" :String.valueOf(ei.getTotal()))).replace("@BlockName@", matName);
        String formatted = customTranslateAlternateColorCodes('&', message);

        //Prevent redunant output to the console if an admin message was already sent.
        if (!consoleRecieved) {
            fd.getServer().getConsoleSender().sendMessage(formatted);
        }

        for (Player x : fd.getServer().getOnlinePlayers()) {
            if (x.hasPermission("fd.broadcast") && isValidWorld(x)) {
                if (!recievedAdminMessage.contains(x)) {
                    x.sendMessage(formatted);
                    if (debug) {
                        log.info(FoundDiamonds.getDebugPrefix() + "Sent broadcast to " + x.getName());
                    }
                } else if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + x.getName() + "recieved an admin message already, so not broadcasting to " + x.getName());
                }
            } else {
                if (debug) {
                    if (!x.hasPermission("fd.broadcast")) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " does not have permission 'fd.broadcast'.  Not broadcasting to " + x.getName());
                    }
                    if (!isValidWorld(x)) {
                        log.info(FoundDiamonds.getDebugPrefix() + x.getName() + " is not in an enabled world, so not broadcasting to  " + x.getName());
                    }
                }
            }
        }

        //reset message checks after successful broadcast
        recievedAdminMessage.clear();
        consoleRecieved = false;

        //write to log if cleanlogging.
        if (fd.getConfig().getBoolean(Config.cleanLog)) {
            writeToCleanLog(ei, playerName);
        }
    }

    private String getBroadcastName(Player player) {
        if (fd.getConfig().getBoolean(Config.useNick)) {
            return player.getDisplayName();
        } else {
            return player.getName();
        }
    }




    /*
     * Total block counters
     */
    private int getTotalBlocks(Block origBlock) {
        blockList = new LinkedList<Block>();
        checkedBlocks = new LinkedList<Block>();
        //fd.getAnnouncedBlocks().add(origBlock.getLocation());
        blockList.add(origBlock);
        for (BlockFace y : BlockFace.values()) {
            Block cycle = origBlock.getRelative(y);
            if ((cycle.getType() == origBlock.getType() && !blockList.contains(cycle) && !checkedBlocks.contains(cycle) && !wasPlaced(cycle)) ||
                    ((isRedstone(origBlock) && isRedstone(cycle)) &&
                    !blockList.contains(cycle) && !checkedBlocks.contains(cycle) && !wasPlaced(cycle))) {
                fd.getAnnouncedBlocks().add(cycle.getLocation());
                //System.out.println("Total+=" + cycle.getType().name() + " X: "+ cycle.getX() + " Y:" + cycle.getY() + " Z:" + cycle.getZ());
                blockList.add(cycle);
                checkCyclesRelative(origBlock, cycle);
                if (blockList.size() >= 1000) {
                    return 1000;
                }
            } else {
                if (!checkedBlocks.contains(cycle)) {
                    checkedBlocks.add(cycle);
                }
            }
        }
        return blockList.size();
    }

    private void checkCyclesRelative(Block origBlock, Block cycle) {
        for (BlockFace y : BlockFace.values()) {
            Block secondCycle = cycle.getRelative(y);
            if ((secondCycle.getType() == origBlock.getType() && !blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle) && !wasPlaced(secondCycle)) ||
               (isRedstone(origBlock) && isRedstone(secondCycle) && (!blockList.contains(secondCycle) && !checkedBlocks.contains(secondCycle) && !wasPlaced(secondCycle)))) {
                blockList.add(secondCycle);
                fd.getAnnouncedBlocks().add(secondCycle.getLocation());
                //System.out.println("Total+=" + secondCycle.getType().name() + " X: "+ secondCycle.getX() + " Y:" + secondCycle.getY() + " Z:" + secondCycle.getZ());
                if (blockList.size() >= 1000) {
                    return;
                }
                checkCyclesRelative(origBlock, secondCycle);
            } else {
                if (!checkedBlocks.contains(secondCycle)) {
                    checkedBlocks.add(secondCycle);
                }
            }
        }
    }




    /*
     * Other Methods
     */
    private boolean isRedstone(Block m) {
        return (m.getType() == Material.REDSTONE_ORE || m.getType() == Material.GLOWING_REDSTONE_ORE);
    }

    private boolean isValidWorld(Player player) {
        return fd.getConfig().getList(Config.enabledWorlds).contains(player.getWorld().getName());
    }

    private boolean isValidGameMode(Player player) {
        return !((player.getGameMode() == GameMode.CREATIVE) && (fd.getConfig().getBoolean(Config.disableInCreative)));
    }

    private boolean alreadyAnnounced(Location loc) {
        return (fd.getAnnouncedBlocks().contains(loc));
    }

    public static String customTranslateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] charArray = textToTranslate.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == altColorChar && "0123456789AaBbCcDdEeFfKkNnRrLlMmOo".indexOf(charArray[i+1]) > -1) {
                charArray[i] = ChatColor.COLOR_CHAR;
                charArray[i+1] = Character.toLowerCase(charArray[i+1]);
            }
        }
        return new String(charArray);
    }




    /*
     * Light Methods
     */
    private boolean blockSeesNoLight(EventInformation ei) {
        double percentage = Double.parseDouble(fd.getConfig().getString(Config.percentOfLightRequired).replaceAll("%", ""));
        double levelToDisableAt = percentage / 15.0;
        DecimalFormat dform = new DecimalFormat("#.##");
        String formattedLightLevel = dform.format(levelToDisableAt);
        int lightLevel = 0;
        int highestLevel = 0;
        for (BlockFace y : BlockFace.values()) {
            lightLevel = ei.getBlock().getRelative(y).getLightLevel();
            if (lightLevel > highestLevel) {
                highestLevel = lightLevel;
            }
            if (lightLevel > levelToDisableAt) {
                if (debug) {
                    log.info(FoundDiamonds.getDebugPrefix() + ei.getPlayer().getName() + " just mined " + ei.getMatName()
                        + " at light level " + highestLevel + ".  We are disabling ore mining at light level " + formattedLightLevel
                        + " or " + percentage + "%");
                }
                return false;
            }
        }
        sendLightAdminMessage(ei, highestLevel);
        if ((fd.getConfig().getBoolean(Config.logLightLevelViolations))) {
            logLightLevelViolation(ei, highestLevel);
        }
        if (debug) {
            log.info(FoundDiamonds.getDebugPrefix() + ei.getPlayer().getName() + " was denied mining "+ ei.getMatName()
                    + " at light level " + highestLevel + ".  We are disabling ore mining at light level " + formattedLightLevel
                    + " or " + percentage + "%");
        }
        return true;
    }

    private boolean isAbleToMineAtSpecifiedLightLevel(EventInformation ei) {
        if (fd.hasPerms(ei.getPlayer(), "fd.monitor")) {
            if (blockSeesNoLight(ei) && ei.getPlayer().getWorld().getEnvironment() != World.Environment.NETHER) {
                ei.getEvent().setCancelled(true);
                ei.getPlayer().sendMessage(FoundDiamonds.getPrefix() + ChatColor.RED + " Mining in the dark is dangerous, place a torch!");
                return false;
            }
        }
        return true;
    }

}