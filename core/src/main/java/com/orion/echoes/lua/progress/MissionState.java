package com.orion.echoes.lua.progress;

import java.util.EnumMap;
import java.util.Map;

import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.enums.RepairType;
import com.orion.echoes.lua.utils.GameConstants;

/** Estado central compartilhado entre a Lua, o HUD e Marte. */
public class MissionState {
    private final Map<ItemType, Integer> inventory = new EnumMap<>(ItemType.class);
    private final Map<RepairType, Boolean> repairs = new EnumMap<>(RepairType.class);

    private boolean weaponCrafted;
    private int enemiesDefeated;
    private final boolean[] marsSites = new boolean[3];
    private String lastMessage = "Localize as pecas da colonia";

    public MissionState() {
        for (ItemType type : ItemType.values()) {
            inventory.put(type, 0);
        }
        for (RepairType type : RepairType.values()) {
            repairs.put(type, false);
        }
    }

    public void collect(ItemType type) {
        if (!isMissionPart(type)) return;
        inventory.put(type, getCount(type) + 1);
        lastMessage = "Peca coletada: " + getItemLabel(type);
    }

    public int getCount(ItemType type) {
        return inventory.getOrDefault(type, 0);
    }

    public boolean repair(RepairType type) {
        if (isRepaired(type)) {
            lastMessage = type.getLabel() + " ja esta operacional";
            return false;
        }
        if (!consume(type.getRequiredPart(), 1)) {
            lastMessage = "Falta a peca de " + getItemLabel(type.getRequiredPart());
            return false;
        }
        repairs.put(type, true);
        lastMessage = type.getLabel() + " restaurada";
        return true;
    }

    public boolean isRepaired(RepairType type) {
        return repairs.getOrDefault(type, false);
    }

    public int getRepairCount() {
        int count = 0;
        for (boolean repaired : repairs.values()) if (repaired) count++;
        return count;
    }

    public boolean canCraftWeapon() {
        return !weaponCrafted
            && getCount(ItemType.WEAPON_PART_A) > 0
            && getCount(ItemType.WEAPON_PART_B) > 0
            && getCount(ItemType.WEAPON_PART_C) > 0;
    }

    public boolean craftWeapon() {
        if (weaponCrafted) {
            lastMessage = "Arma EVA ja esta online";
            return false;
        }
        if (!canCraftWeapon()) {
            lastMessage = "Craft bloqueado // partes da arma " + getWeaponPartCount() + "/3";
            return false;
        }
        consume(ItemType.WEAPON_PART_A, 1);
        consume(ItemType.WEAPON_PART_B, 1);
        consume(ItemType.WEAPON_PART_C, 1);
        weaponCrafted = true;
        lastMessage = "Arma EVA pronta // mire com o mouse e clique para disparar";
        return true;
    }

    public boolean hasWeapon() {
        return weaponCrafted;
    }

    public void recordEnemyDefeated() {
        enemiesDefeated++;
        lastMessage = "Ameaca neutralizada";
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public int getRepairPartCount() {
        return getCount(ItemType.ANTENNA_PART)
            + getCount(ItemType.ENERGY_PART)
            + getCount(ItemType.EXTRACTION_PART)
            + getCount(ItemType.GREENHOUSE_PART);
    }

    public int getWeaponPartCount() {
        if (weaponCrafted) return 3;
        return getCount(ItemType.WEAPON_PART_A)
            + getCount(ItemType.WEAPON_PART_B)
            + getCount(ItemType.WEAPON_PART_C);
    }

    public boolean scanMarsSite(int index) {
        if (index < 0 || index >= marsSites.length || marsSites[index]) return false;
        marsSites[index] = true;
        lastMessage = "Dados marcianos sincronizados";
        return true;
    }

    public boolean isMarsSiteScanned(int index) {
        return index >= 0 && index < marsSites.length && marsSites[index];
    }

    public int getMarsSitesScanned() {
        int count = 0;
        for (boolean scanned : marsSites) if (scanned) count++;
        return count;
    }

    public boolean isPortalReady(float oxygen) {
        return getRepairCount() >= 4
            && weaponCrafted
            && enemiesDefeated >= 1
            && oxygen > GameConstants.CRITICAL_OXYGEN_THRESHOLD;
    }

    public String getCurrentObjective(float oxygen) {
        return getStageInstruction(oxygen);
    }

    public int getLunarStage() {
        if (getRepairCount() < 4) return 1;
        if (!weaponCrafted) return 2;
        if (enemiesDefeated < 1) return 3;
        return 4;
    }

    public String getStageTitle() {
        return switch (getLunarStage()) {
            case 1 -> "RECUPERAR A COLONIA";
            case 2 -> "MONTAR A ARMA EVA";
            case 3 -> "NEUTRALIZAR A AMEACA";
            default -> "ATIVAR O PORTAL";
        };
    }

    public String getStageInstruction(float oxygen) {
        if (getRepairCount() < 4) {
            return "Ache cada peca e pressione E nas 4 estacoes (" + getRepairCount() + "/4)";
        }
        if (!weaponCrafted) {
            return canCraftWeapon()
                ? "Partes completas // volte a base e pressione C"
                : "Colete as pecas A, B e C (" + getWeaponPartCount() + "/3)";
        }
        if (enemiesDefeated < 1) {
            return "Mire com o mouse e segure o clique para disparar";
        }
        if (oxygen <= GameConstants.CRITICAL_OXYGEN_THRESHOLD) {
            return "Oxigenio baixo // recarregue dentro da base";
        }
        return "Tudo pronto // entre no portal para viajar a Marte";
    }

    public ItemType getRequestedItem() {
        if (getRepairCount() < 4) {
            for (RepairType type : RepairType.values()) {
                if (!isRepaired(type)) return type.getRequiredPart();
            }
        }
        if (!weaponCrafted) {
            if (getCount(ItemType.WEAPON_PART_A) == 0) return ItemType.WEAPON_PART_A;
            if (getCount(ItemType.WEAPON_PART_B) == 0) return ItemType.WEAPON_PART_B;
            if (getCount(ItemType.WEAPON_PART_C) == 0) return ItemType.WEAPON_PART_C;
        }
        return null;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void notifyAction(String message) {
        if (message != null && !message.isBlank()) lastMessage = message;
    }

    void restoreCount(ItemType type, int count) {
        inventory.put(type, Math.max(0, count));
    }

    void restoreRepair(RepairType type, boolean repaired) {
        repairs.put(type, repaired);
    }

    void restoreWeapon(boolean crafted) {
        weaponCrafted = crafted;
    }

    void restoreEnemiesDefeated(int count) {
        enemiesDefeated = Math.max(0, count);
    }

    void restoreMarsSite(int index, boolean scanned) {
        if (index >= 0 && index < marsSites.length) marsSites[index] = scanned;
    }

    private boolean consume(ItemType type, int amount) {
        int current = getCount(type);
        if (current < amount) return false;
        inventory.put(type, current - amount);
        return true;
    }

    public static boolean isMissionPart(ItemType type) {
        return type != ItemType.OXYGEN && type != ItemType.FOOD
            && type != ItemType.ICE_ROCK && type != ItemType.MEDKIT;
    }

    public static String getItemLabel(ItemType type) {
        return switch (type) {
            case ANTENNA_PART -> "antena";
            case ENERGY_PART -> "energia";
            case EXTRACTION_PART -> "extracao";
            case GREENHOUSE_PART -> "estufa";
            case WEAPON_PART_A -> "arma A";
            case WEAPON_PART_B -> "arma B";
            case WEAPON_PART_C -> "arma C";
            case MEDKIT -> "kit medico";
            default -> type.name();
        };
    }
}
