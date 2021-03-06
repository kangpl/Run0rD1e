package ch.epfl.sdp.entities.enemy;

import org.junit.Test;

import ch.epfl.sdp.entities.enemy.Enemy;
import ch.epfl.sdp.entities.enemy.EnemyManager;

import static junit.framework.TestCase.assertEquals;

public class EnemyManagerTest {

    @Test
    public void updateEnemiesShouldAddEnemy() {
        EnemyManager manager = EnemyManager.getInstance();
        Enemy enemy = new Enemy(1, null);
        manager.updateEnemies(enemy);
        assertEquals(1, manager.getEnemies().size());
    }

    @Test
    public void updateEnemiesShouldUpdateEnemyOnly() {
        EnemyManager manager = EnemyManager.getInstance();
        Enemy enemy = new Enemy(1, null);
        manager.updateEnemies(enemy);
        manager.updateEnemies(enemy);
        assertEquals(1, manager.getEnemies().size());
    }

    @Test
    public void removeEnemyShouldRemoveEnemy() {
        EnemyManager manager = EnemyManager.getInstance();
        Enemy enemy = new Enemy(1, null);
        manager.updateEnemies(enemy);
        manager.removeEnemy(enemy);
        assertEquals(0, manager.getEnemies().size());
    }

    @Test
    public void removeAllTest() {
        EnemyManager manager = EnemyManager.getInstance();
        Enemy enemy = new Enemy(1, null);
        manager.updateEnemies(enemy);
        manager.clear();
        assertEquals(0, manager.getEnemies().size());
    }
}
