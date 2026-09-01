package com.orion.echoes.lua.progress;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.enums.RepairType;
import com.orion.echoes.lua.utils.GameConstants;

/** Estado central compartilhado entre a Lua, o HUD e Marte. */
public class MissionState {
    public static final int MAGAZINE_SIZE = 8;
    public static final int LUNAR_ENEMY_TARGET = 4;
    public static final int MARS_SATELLITE_TARGET = 4;
    public static final int INVENTORY_SIZE = 24;
    public static final int MARS_STORAGE_SIZE = 18;
    public static final int LUNAR_CHEST_COUNT = 4;
    public static final int MARS_CHEST_COUNT = 5;

    private final Map<ItemType, Integer> inventory = new EnumMap<>(ItemType.class);
    private final Map<ItemType, Integer> marsStorage = new EnumMap<>(ItemType.class);
    private final Map<RepairType, Boolean> repairs = new EnumMap<>(RepairType.class);
    private final ItemType[] inventoryLayout = new ItemType[INVENTORY_SIZE];
    private final ItemType[] marsStorageLayout = new ItemType[MARS_STORAGE_SIZE];
    private final boolean[] lunarChests = new boolean[LUNAR_CHEST_COUNT];
    private final boolean[] marsChests = new boolean[MARS_CHEST_COUNT];
    private ItemType equippedHelmet;
    private ItemType equippedChest;
    private ItemType equippedBoots;
    private ItemType equippedMiningTool;
    private ItemType equippedRepairTool;

    private boolean weaponCrafted;
    private int magazineAmmo;
    private boolean marsStorageCrafted;
    private int enemiesDefeated;
    private long worldSeed;
    private final boolean[] marsSites = new boolean[MARS_SATELLITE_TARGET];
    private String lastMessage = "Localize as pecas da colonia";

    public MissionState() {
        worldSeed = ThreadLocalRandom.current().nextLong();
        int slot = 0;
        for (ItemType type : ItemType.values()) {
            inventory.put(type, 0);
            marsStorage.put(type, 0);
            if (slot < inventoryLayout.length) inventoryLayout[slot++] = type;
        }
        for (RepairType type : RepairType.values()) {
            repairs.put(type, false);
        }
    }

    public void collect(ItemType type) {
        collect(type, 1);
    }

    public void collect(ItemType type, int amount) {
        if (type == null || amount <= 0) return;
        ensureInventorySlot(type);
        inventory.put(type, Math.min(type.getMaxStack(), getCount(type) + amount));
        lastMessage = "Item coletado: " + getItemLabel(type);
    }

    public int getCount(ItemType type) {
        return inventory.getOrDefault(type, 0);
    }

    public ItemType getInventorySlot(int index) {
        return index >= 0 && index < inventoryLayout.length ? inventoryLayout[index] : null;
    }

    public void swapInventorySlots(int first, int second) {
        if (first < 0 || second < 0 || first >= inventoryLayout.length || second >= inventoryLayout.length) return;
        ItemType held = inventoryLayout[first];
        inventoryLayout[first] = inventoryLayout[second];
        inventoryLayout[second] = held;
    }

    public ItemType getStorageSlot(int index) {
        return index >= 0 && index < marsStorageLayout.length ? marsStorageLayout[index] : null;
    }

    public void swapStorageSlots(int first, int second) {
        if (first < 0 || second < 0 || first >= marsStorageLayout.length
            || second >= marsStorageLayout.length) return;
        ItemType held = marsStorageLayout[first];
        marsStorageLayout[first] = marsStorageLayout[second];
        marsStorageLayout[second] = held;
    }

    /** Move uma pilha completa da mochila para um slot vazio/compativel do bau. */
    public boolean moveInventoryStackToStorage(int inventorySlot, int storageSlot) {
        if (!marsStorageCrafted || inventorySlot < 0 || inventorySlot >= INVENTORY_SIZE
            || storageSlot < 0 || storageSlot >= MARS_STORAGE_SIZE) return false;
        ItemType type = inventoryLayout[inventorySlot];
        if (type == null || getCount(type) <= 0 || isEquipped(type)) return false;
        int existingStorageSlot = findStorageSlot(type);
        if (existingStorageSlot >= 0) storageSlot = existingStorageSlot;
        ItemType target = marsStorageLayout[storageSlot];
        if (target != null && target != type && getStoredCount(target) > 0) return false;
        int capacity = type.getMaxStack() - getStoredCount(type);
        int moved = Math.min(getCount(type), capacity);
        if (moved <= 0) return false;
        inventory.put(type, getCount(type) - moved);
        marsStorage.put(type, getStoredCount(type) + moved);
        marsStorageLayout[storageSlot] = type;
        if (getCount(type) == 0) inventoryLayout[inventorySlot] = null;
        lastMessage = moved + "x " + getItemLabel(type) + " guardado no bau";
        return true;
    }

    /** Move uma pilha completa do bau para um slot vazio/compativel da mochila. */
    public boolean moveStorageStackToInventory(int storageSlot, int inventorySlot) {
        if (!marsStorageCrafted || storageSlot < 0 || storageSlot >= MARS_STORAGE_SIZE
            || inventorySlot < 0 || inventorySlot >= INVENTORY_SIZE) return false;
        ItemType type = marsStorageLayout[storageSlot];
        if (type == null || getStoredCount(type) <= 0) return false;
        int existingInventorySlot = findInventorySlot(type);
        if (existingInventorySlot >= 0 && getCount(type) > 0) inventorySlot = existingInventorySlot;
        ItemType target = inventoryLayout[inventorySlot];
        if (target != null && target != type && getCount(target) > 0) return false;
        int capacity = type.getMaxStack() - getCount(type);
        int moved = Math.min(getStoredCount(type), capacity);
        if (moved <= 0) return false;
        marsStorage.put(type, getStoredCount(type) - moved);
        inventory.put(type, getCount(type) + moved);
        inventoryLayout[inventorySlot] = type;
        if (getStoredCount(type) == 0) marsStorageLayout[storageSlot] = null;
        lastMessage = moved + "x " + getItemLabel(type) + " retirado do bau";
        return true;
    }

    void restoreInventorySlot(int index, ItemType type) {
        if (index >= 0 && index < inventoryLayout.length) inventoryLayout[index] = type;
    }

    void restoreStorageSlot(int index, ItemType type) {
        if (index >= 0 && index < marsStorageLayout.length) marsStorageLayout[index] = type;
    }

    public boolean canCraftEquipment(ItemType result) {
        if (result == null || !result.isEquipment() || getCount(result) > 0) return false;
        return switch (result) {
            case ARMOR_HELMET -> hasIngredients(2, 0, 1);
            case ARMOR_CHEST -> hasIngredients(3, 1, 2);
            case ARMOR_BOOTS -> hasIngredients(2, 0, 1);
            case MINING_TOOL -> hasIngredients(2, 1, 0);
            case REPAIR_TOOL -> hasIngredients(1, 1, 1);
            default -> false;
        };
    }

    public boolean craftEquipment(ItemType result) {
        if (!canCraftEquipment(result)) {
            lastMessage = getCount(result) > 0 ? "Equipamento ja fabricado" : "Materiais insuficientes";
            return false;
        }
        int alloy = 0, core = 0, fiber = 0;
        switch (result) {
            case ARMOR_HELMET, ARMOR_BOOTS -> { alloy = 2; fiber = 1; }
            case ARMOR_CHEST -> { alloy = 3; core = 1; fiber = 2; }
            case MINING_TOOL -> { alloy = 2; core = 1; }
            case REPAIR_TOOL -> { alloy = 1; core = 1; fiber = 1; }
            default -> { return false; }
        }
        consume(ItemType.ALLOY_PLATE, alloy);
        consume(ItemType.QUANTUM_CORE, core);
        consume(ItemType.FIBER_MESH, fiber);
        collect(result);
        lastMessage = getItemLabel(result) + " fabricado // equipe pelo inventario";
        return true;
    }

    private boolean hasIngredients(int alloy, int core, int fiber) {
        return getCount(ItemType.ALLOY_PLATE) >= alloy
            && getCount(ItemType.QUANTUM_CORE) >= core
            && getCount(ItemType.FIBER_MESH) >= fiber;
    }

    public String getRecipe(ItemType result) {
        return switch (result) {
            case ARMOR_HELMET, ARMOR_BOOTS -> "2 liga + 1 fibra";
            case ARMOR_CHEST -> "3 liga + 1 nucleo + 2 fibras";
            case MINING_TOOL -> "2 ligas + 1 nucleo";
            case REPAIR_TOOL -> "1 liga + 1 nucleo + 1 fibra";
            default -> "";
        };
    }

    public boolean equip(ItemType type) {
        if (type == null || getCount(type) <= 0 || !type.isEquipment()) return false;
        switch (type) {
            case ARMOR_HELMET -> equippedHelmet = type;
            case ARMOR_CHEST -> equippedChest = type;
            case ARMOR_BOOTS -> equippedBoots = type;
            case MINING_TOOL -> equippedMiningTool = type;
            case REPAIR_TOOL -> equippedRepairTool = type;
            default -> { return false; }
        }
        lastMessage = getItemLabel(type) + " equipado";
        return true;
    }

    public void unequip(ItemType type) {
        if (type == equippedHelmet) equippedHelmet = null;
        if (type == equippedChest) equippedChest = null;
        if (type == equippedBoots) equippedBoots = null;
        if (type == equippedMiningTool) equippedMiningTool = null;
        if (type == equippedRepairTool) equippedRepairTool = null;
    }

    public boolean isEquipped(ItemType type) {
        return type != null && (type == equippedHelmet || type == equippedChest || type == equippedBoots
            || type == equippedMiningTool || type == equippedRepairTool);
    }

    public ItemType getEquippedHelmet() { return equippedHelmet; }
    public ItemType getEquippedChest() { return equippedChest; }
    public ItemType getEquippedBoots() { return equippedBoots; }
    public ItemType getEquippedMiningTool() { return equippedMiningTool; }
    public ItemType getEquippedRepairTool() { return equippedRepairTool; }

    public float getArmorProtection() {
        float protection = 0f;
        if (equippedHelmet != null) protection += 0.08f;
        if (equippedChest != null) protection += 0.16f;
        if (equippedBoots != null) protection += 0.08f;
        return protection;
    }

    void restoreEquipment(ItemType helmet, ItemType chest, ItemType boots, ItemType mining, ItemType repair) {
        equippedHelmet = helmet;
        equippedChest = chest;
        equippedBoots = boots;
        equippedMiningTool = mining;
        equippedRepairTool = repair;
    }

    public boolean openChest(boolean mars, int index) {
        boolean[] chests = mars ? marsChests : lunarChests;
        if (index < 0 || index >= chests.length || chests[index]) return false;
        chests[index] = true;
        lastMessage = "Bau inspecionado // itens ejetados";
        return true;
    }

    public boolean isChestOpened(boolean mars, int index) {
        boolean[] chests = mars ? marsChests : lunarChests;
        return index >= 0 && index < chests.length && chests[index];
    }

    void restoreChest(boolean mars, int index, boolean opened) {
        boolean[] chests = mars ? marsChests : lunarChests;
        if (index >= 0 && index < chests.length) chests[index] = opened;
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
        collect(ItemType.AMMO_CELL, 24);
        reloadMagazine();
        lastMessage = "Arma EVA pronta // pente 8 + reserva 16";
        return true;
    }

    public boolean hasWeapon() {
        return weaponCrafted;
    }

    public int getMagazineAmmo() { return magazineAmmo; }
    public int getReserveAmmo() { return getCount(ItemType.AMMO_CELL); }

    public boolean hasMarsStorage() { return marsStorageCrafted; }
    public int getStoredCount(ItemType type) { return marsStorage.getOrDefault(type, 0); }

    public boolean craftMarsStorage() {
        if (marsStorageCrafted) return false;
        if (getCount(ItemType.ALLOY_PLATE) < 2 || getCount(ItemType.FIBER_MESH) < 1) {
            lastMessage = "Bau requer 2 ligas + 1 fibra";
            return false;
        }
        consume(ItemType.ALLOY_PLATE, 2);
        consume(ItemType.FIBER_MESH, 1);
        marsStorageCrafted = true;
        lastMessage = "Bau de carga fabricado // armazenamento online";
        return true;
    }

    public boolean storeOne(ItemType type) {
        if (!marsStorageCrafted || type == null || getCount(type) <= 0 || isEquipped(type)) return false;
        if (!consume(type, 1)) return false;
        marsStorage.put(type, getStoredCount(type) + 1);
        ensureStorageSlot(type);
        lastMessage = getItemLabel(type) + " armazenado";
        return true;
    }

    public boolean retrieveOne(ItemType type) {
        int stored = getStoredCount(type);
        if (!marsStorageCrafted || stored <= 0 || getCount(type) >= type.getMaxStack()) return false;
        marsStorage.put(type, stored - 1);
        collect(type, 1);
        if (getStoredCount(type) == 0) clearStorageSlot(type);
        lastMessage = getItemLabel(type) + " retirado";
        return true;
    }

    public boolean consumeMagazineRound() {
        if (magazineAmmo <= 0) return false;
        magazineAmmo--;
        return true;
    }

    public int reloadMagazine() {
        int missing = MAGAZINE_SIZE - magazineAmmo;
        int loaded = Math.min(missing, getReserveAmmo());
        if (loaded <= 0) return 0;
        consume(ItemType.AMMO_CELL, loaded);
        magazineAmmo += loaded;
        return loaded;
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
        lastMessage = "Satelite marciano restaurado";
        return true;
    }

    public boolean repairMarsSatellite(int index) {
        return scanMarsSite(index);
    }

    public boolean isMarsSiteScanned(int index) {
        return index >= 0 && index < marsSites.length && marsSites[index];
    }

    public int getMarsSitesScanned() {
        int count = 0;
        for (boolean scanned : marsSites) if (scanned) count++;
        return count;
    }

    public int getMarsSatellitesRepaired() {
        return getMarsSitesScanned();
    }

    public boolean isPortalReady(float oxygen) {
        return getRepairCount() >= 4
            && weaponCrafted
            && enemiesDefeated >= LUNAR_ENEMY_TARGET
            && oxygen > GameConstants.CRITICAL_OXYGEN_THRESHOLD;
    }

    public String getCurrentObjective(float oxygen) {
        return getStageInstruction(oxygen);
    }

    public int getLunarStage() {
        if (getRepairCount() < 4) return 1;
        if (!weaponCrafted) return 2;
        if (enemiesDefeated < LUNAR_ENEMY_TARGET) return 3;
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
                ? "Partes completas // entre na base e use a bancada"
                : "Colete as pecas A, B e C (" + getWeaponPartCount() + "/3)";
        }
        if (enemiesDefeated < LUNAR_ENEMY_TARGET) {
            return "Elimine todas as ameacas lunares (" + enemiesDefeated
                + "/" + LUNAR_ENEMY_TARGET + ")";
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

    void restoreMagazine(int amount) {
        magazineAmmo = Math.max(0, Math.min(MAGAZINE_SIZE, amount));
    }

    void restoreMarsStorage(boolean crafted) { marsStorageCrafted = crafted; }
    void restoreStoredCount(ItemType type, int count) {
        marsStorage.put(type, Math.max(0, count));
        if (count > 0) ensureStorageSlot(type);
    }

    private void ensureInventorySlot(ItemType type) {
        for (ItemType slot : inventoryLayout) if (slot == type) return;
        for (int i = 0; i < inventoryLayout.length; i++) {
            if (inventoryLayout[i] == null) { inventoryLayout[i] = type; return; }
        }
    }

    private void ensureStorageSlot(ItemType type) {
        for (ItemType slot : marsStorageLayout) if (slot == type) return;
        for (int i = 0; i < marsStorageLayout.length; i++) {
            if (marsStorageLayout[i] == null) { marsStorageLayout[i] = type; return; }
        }
    }

    private void clearStorageSlot(ItemType type) {
        for (int i = 0; i < marsStorageLayout.length; i++) {
            if (marsStorageLayout[i] == type) marsStorageLayout[i] = null;
        }
    }

    private int findInventorySlot(ItemType type) {
        for (int i = 0; i < inventoryLayout.length; i++) {
            if (inventoryLayout[i] == type) return i;
        }
        return -1;
    }

    private int findStorageSlot(ItemType type) {
        for (int i = 0; i < marsStorageLayout.length; i++) {
            if (marsStorageLayout[i] == type) return i;
        }
        return -1;
    }

    void restoreEnemiesDefeated(int count) {
        enemiesDefeated = Math.max(0, count);
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    void restoreWorldSeed(long seed) {
        worldSeed = seed;
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

    public boolean consumeItem(ItemType type, int amount) {
        return consume(type, amount);
    }

    public static boolean isMissionPart(ItemType type) {
        return type == ItemType.ANTENNA_PART || type == ItemType.ENERGY_PART
            || type == ItemType.EXTRACTION_PART || type == ItemType.GREENHOUSE_PART
            || type == ItemType.WEAPON_PART_A || type == ItemType.WEAPON_PART_B
            || type == ItemType.WEAPON_PART_C;
    }

    public static String getItemLabel(ItemType type) {
        return switch (type) {
            case OXYGEN -> "oxigenio";
            case FOOD -> "alimento";
            case ICE_ROCK -> "gelo lunar";
            case ANTENNA_PART -> "antena";
            case ENERGY_PART -> "energia";
            case EXTRACTION_PART -> "extracao";
            case GREENHOUSE_PART -> "estufa";
            case WEAPON_PART_A -> "arma A";
            case WEAPON_PART_B -> "arma B";
            case WEAPON_PART_C -> "arma C";
            case AMMO_CELL -> "carga de municao";
            case MEDKIT -> "kit medico";
            case ALLOY_PLATE -> "placa de liga";
            case QUANTUM_CORE -> "nucleo quantico";
            case FIBER_MESH -> "malha de fibra";
            case MINING_TOOL -> "picareta lunar";
            case REPAIR_TOOL -> "ferramenta de reparo";
            case ARMOR_HELMET -> "capacete blindado";
            case ARMOR_CHEST -> "peitoral blindado";
            case ARMOR_BOOTS -> "botas blindadas";
        };
    }
}
