package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

public class ResidencePlayer {

    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;
    private UUID uuid = null;

    private Map<String, ClaimedResidence> ResidenceList = new HashMap<String, ClaimedResidence>();
    private ClaimedResidence mainResidence = null;

    private PermissionGroup group = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;
    private int maxSubzoneDepth = -1;

    public ResidencePlayer(Player player) {
	if (player == null)
	    return;
	Residence.getOfflinePlayerMap().put(player.getName(), player);
	Residence.addCachedPlayerNameUUIDs(player.getUniqueId(), player.getName());
	this.updatePlayer(player);
	this.RecalculatePermissions();
    }

    public ResidencePlayer(String userName) {
	this.userName = userName;
	updatePlayer();
	RecalculatePermissions();
    }

    public void setMainResidence(ClaimedResidence res) {
	if (mainResidence != null)
	    mainResidence.setMainResidence(false);
	mainResidence = res;
    }

    public ClaimedResidence getMainResidence() {
	if (mainResidence == null) {
	    for (Entry<String, ClaimedResidence> one : ResidenceList.entrySet()) {
		if (one.getValue() == null)
		    continue;
		if (one.getValue().isMainResidence()) {
		    mainResidence = one.getValue();
		    return mainResidence;
		}
	    }
	    for (String one : Residence.getRentManager().getRentedLands(this.userName)) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(one);
		if (res != null) {
		    mainResidence = res;
		    return mainResidence;
		}
	    }
	    for (Entry<String, ClaimedResidence> one : ResidenceList.entrySet()) {
		if (one.getValue() == null)
		    continue;
		mainResidence = one.getValue();
		return mainResidence;
	    }
	}
	return mainResidence;
    }

    public void RecalculatePermissions() {
	getGroup();
	recountMaxRes();
	recountMaxRents();
	recountMaxSubzones();
    }

    public void recountMaxRes() {
	if (this.group != null)
	    this.maxRes = this.group.getMaxZones();
	for (int i = 1; i <= Residence.getConfigManager().getMaxResCount(); i++) {
	    if (player != null && player.isOnline()) {
		if (this.player.hasPermission("residence.max.res." + i))
		    this.maxRes = i;
	    } else if (ofPlayer != null) {
		if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.res." + i, Residence.getConfigManager().getDefaultWorld()))
		    this.maxRes = i;
	    }
	}
    }

    public void recountMaxRents() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxRentCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.rents." + i))
		    this.maxRents = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.rents." + i, Residence.getConfigManager().getDefaultWorld()))
			this.maxRents = i;
	    }
	}

	int m = this.getGroup().getMaxRents();
	if (this.maxRents < m)
	    this.maxRents = m;
    }

    public int getMaxRents() {
	recountMaxRents();
	return this.maxRents;
    }

    public void recountMaxSubzones() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxSubzonesCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.subzones." + i))
		    this.maxSubzones = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzones." + i, Residence.getConfigManager().getDefaultWorld()))
			this.maxSubzones = i;
	    }
	}

	int m = this.getGroup().getMaxSubzones();
	if (this.maxSubzones < m)
	    this.maxSubzones = m;
    }

    public int getMaxSubzones() {
	recountMaxSubzones();
	return this.maxSubzones;
    }

    public void recountMaxSubzoneDepth() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxSubzoneDepthCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.subzonedepth." + i))
		    this.maxSubzoneDepth = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzonedepth." + i, Residence.getConfigManager().getDefaultWorld()))
			this.maxSubzoneDepth = i;
	    }
	}

	int m = this.getGroup().getMaxSubzoneDepth();
	if (this.maxSubzoneDepth < m)
	    this.maxSubzoneDepth = m;
    }

    public int getMaxSubzoneDepth() {
	recountMaxSubzoneDepth();
	return this.maxSubzoneDepth;
    }

    public int getMaxRes() {
	Residence.getPermissionManager().updateGroupNameForPlayer(this.userName, this.player != null && this.player.isOnline() ? this.player.getPlayer().getLocation()
	    .getWorld().getName() : Residence.getConfigManager().getDefaultWorld(), true);
	recountMaxRes();
	PermissionGroup g = getGroup();
	if (this.maxRes < g.getMaxZones()) {
	    return g.getMaxZones();
	}
	return this.maxRes;
    }

    public PermissionGroup getGroup() {
	return getGroup(this.player != null ? player.getWorld().getName() : Residence.getConfigManager().getDefaultWorld());
    }

    public PermissionGroup getGroup(String world) {
	String gp = Residence.getPermissionManager().getGroupNameByPlayer(this.userName, world);
	this.group = Residence.getPermissionManager().getGroupByName(gp);
	return this.group;
    }

    public void recountRes() {
	if (this.userName != null) {
	    ResidenceManager m = Residence.getResidenceManager();
	    this.ResidenceList = m.getResidenceMapList(this.userName, true);
	}
    }

    public ResidencePlayer updatePlayer(Player player) {
	this.player = player;
	this.uuid = player.getUniqueId();
	this.userName = player.getName();
	this.ofPlayer = player;
	return this;
    }

    private void updatePlayer() {
	if (player != null && player.isOnline())
	    return;
	if (this.uuid != null && Bukkit.getPlayer(this.uuid) != null) {
	    player = Bukkit.getPlayer(this.uuid);
	    this.userName = player.getName();
	    return;
	}

	if (this.userName != null) {
	    player = Bukkit.getPlayer(this.userName);
	}
	if (player != null) {
	    this.userName = player.getName();
	    this.uuid = player.getUniqueId();
	    this.ofPlayer = player;
	    return;
	}
	if (this.player == null && ofPlayer == null)
	    ofPlayer = Residence.getOfflinePlayer(userName);
	if (ofPlayer != null) {
	    this.userName = ofPlayer.getName();
	    this.uuid = ofPlayer.getUniqueId();
	    return;
	}
    }

    public void addResidence(ClaimedResidence residence) {
	if (residence == null)
	    return;
	this.ResidenceList.put(residence.getName().toLowerCase(), residence);
    }

    public void removeResidence(String residence) {
	if (residence == null)
	    return;
	residence = residence.toLowerCase();
	this.ResidenceList.remove(residence);
    }

    public void renameResidence(String oldResidence, String newResidence) {
	if (oldResidence == null)
	    return;
	if (newResidence == null)
	    return;
	oldResidence = oldResidence.toLowerCase();

	ClaimedResidence res = ResidenceList.get(oldResidence);
	if (res != null) {
	    removeResidence(oldResidence);
	    ResidenceList.put(newResidence.toLowerCase(), res);
	}
    }

    public int getResAmount() {
	return ResidenceList.size();
    }

    public List<ClaimedResidence> getResList() {
	List<ClaimedResidence> temp = new ArrayList<ClaimedResidence>();
	for (Entry<String, ClaimedResidence> one : this.ResidenceList.entrySet()) {
	    temp.add(one.getValue());
	}
	return temp;
    }

    public Map<String, ClaimedResidence> getResidenceMap() {
	return this.ResidenceList;
    }

}
