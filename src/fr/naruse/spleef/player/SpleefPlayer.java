package fr.naruse.spleef.player;

import com.google.common.collect.Maps;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.statistic.StatisticBuilder;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.sql.SpleefSQLResponse;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class SpleefPlayer {

    private final SpleefPlugin pl;
    private Spleef currentSpleef;
    private final String uuid;
    private Location lastLocation;
    private GameMode gameMode;

    private final Map<StatisticType, Integer> statisticMap = Maps.newHashMap();

    public SpleefPlayer(SpleefPlugin pl, String uuid) {
        this.pl = pl;
        this.uuid = uuid;
        reloadStatistics();
    }

    public void reloadStatistics(){
        if(pl.getSqlManager() == null){
            return;
        }
        pl.getSqlManager().isRegistered(uuid, new SpleefSQLResponse() {
            @Override
            public void handleResponse(Object response) {
                if(response == null){
                    return;
                }
                boolean exists = (boolean) response;
                if(exists){
                    pl.getSqlManager().getProperties(uuid, new SpleefSQLResponse(){
                        @Override
                        public void handleResponse(Object response) {
                            if(response == null){
                                return;
                            }
                            String json = (String) response;
                            StatisticBuilder.fromJson(json, statisticMap);
                        }
                    });
                }else{
                    pl.getSqlManager().register(uuid);
                }
            }
        });
    }

    public void saveStatistics(){
        if(pl.getSqlManager() == null){
            return;
        }
        pl.getSqlManager().save(uuid, StatisticBuilder.toJson(statisticMap));
    }

    public int getStatistic(StatisticType type){
        if (statisticMap.containsKey(type)) {
            return statisticMap.get(type);
        }
        return 0;
    }

    public void incrementStatistic(StatisticType type, int increment){
        if (statisticMap.containsKey(type)) {
            statisticMap.put(type, statisticMap.get(type)+increment);
        }
    }

    public void setStatistic(StatisticType type, int newValue){
        if (statisticMap.containsKey(type)) {
            statisticMap.put(type, newValue);
        }
    }

    private Inventory inv;
    public void registerInventory(Player p){
        inv = Bukkit.createInventory(null, 9*6, uuid);
        for(int i = 0; i < inv.getSize(); i++){
            try{
                if(p.getInventory().getItem(i) != null){
                    inv.setItem(i, p.getInventory().getItem(i));
                }
            }catch (Exception e){
                break;
            }
        }
        this.gameMode = p.getGameMode();
    }

    public void setPlayerInventory(Player p){
        if(inv == null){
            return;
        }
        for(int i = 0; i < 9*6; i++){
            if(inv.getItem(i) != null){
                p.getInventory().setItem(i, inv.getItem(i));
            }
        }
        p.setGameMode(gameMode);
    }

    public Spleef getCurrentSpleef() {
        return currentSpleef;
    }

    public void setCurrentSpleef(Spleef currentSpleef) {
        this.currentSpleef = currentSpleef;
    }

    public boolean hasSpleef(){
        return currentSpleef != null;
    }

    public String getUUID() {
        return uuid;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}
