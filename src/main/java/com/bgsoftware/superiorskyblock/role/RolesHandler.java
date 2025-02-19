package com.bgsoftware.superiorskyblock.role;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.RolesManager;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.role.container.RolesContainer;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.List;

public final class RolesHandler extends AbstractHandler implements RolesManager {

    private static final int GUEST_ROLE_INDEX = -2, COOP_ROLE_INDEX = -1;
    private int lastRole = Integer.MIN_VALUE;

    private final RolesContainer rolesContainer;

    public RolesHandler(SuperiorSkyblockPlugin plugin, RolesContainer rolesContainer){
        super(plugin);
        this.rolesContainer = rolesContainer;
    }

    @Override
    public void loadData() {
        ConfigurationSection rolesSection = plugin.getSettings().getIslandRoles().getSection();
        loadRole(rolesSection.getConfigurationSection("guest"), GUEST_ROLE_INDEX, null);
        loadRole(rolesSection.getConfigurationSection("coop"), COOP_ROLE_INDEX, (SPlayerRole) getGuestRole());
        SPlayerRole previousRole = (SPlayerRole) getCoopRole();
        for(String roleSection : rolesSection.getConfigurationSection("ladder").getKeys(false))
            previousRole = (SPlayerRole) getPlayerRole(loadRole(rolesSection.getConfigurationSection("ladder." + roleSection), 0, previousRole));
    }

    @Override
    @Nullable
    public PlayerRole getPlayerRole(int index) {
        return this.rolesContainer.getPlayerRole(index);
    }

    @Override
    @Nullable
    public PlayerRole getPlayerRoleFromId(int id) {
        return this.rolesContainer.getPlayerRoleFromId(id);
    }

    @Override
    public PlayerRole getPlayerRole(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.rolesContainer.getPlayerRole(name);
    }

    @Override
    public PlayerRole getDefaultRole() {
        return getPlayerRole(0);
    }

    @Override
    public PlayerRole getLastRole() {
        return getPlayerRole(lastRole);
    }

    @Override
    public PlayerRole getGuestRole() {
        return getPlayerRole(GUEST_ROLE_INDEX);
    }

    @Override
    public PlayerRole getCoopRole() {
        return getPlayerRole(COOP_ROLE_INDEX);
    }

    @Override
    public List<PlayerRole> getRoles(){
        return this.rolesContainer.getRoles();
    }

    private int loadRole(ConfigurationSection section, int type, SPlayerRole previousRole){
        int weight = section.getInt("weight", type);
        int id = section.getInt("id", weight);
        String name = section.getString("name");

        PlayerRole playerRole = new SPlayerRole(name, id, weight, section.getStringList("permissions"), previousRole);

        this.rolesContainer.addPlayerRole(playerRole);

        if(weight > lastRole)
            lastRole = weight;

        return weight;
    }

}
