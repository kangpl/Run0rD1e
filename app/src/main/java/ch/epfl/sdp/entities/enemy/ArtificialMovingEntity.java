package ch.epfl.sdp.entities.enemy;

import java.util.Random;

import ch.epfl.sdp.entities.AoeRadiusEntity;
import ch.epfl.sdp.entities.enemy.artificial_intelligence.LinearMovement;
import ch.epfl.sdp.entities.enemy.artificial_intelligence.Movement;
import ch.epfl.sdp.game.Updatable;
import ch.epfl.sdp.geometry.area.Area;
import ch.epfl.sdp.map.location.GeoPoint;
import ch.epfl.sdp.geometry.area.UnboundedArea;

/**
 * Represents an entity of the game that can move automatically by setting a movement. The movement
 * can be limited by setting an area where the entity can reside.
 */
public abstract class ArtificialMovingEntity extends AoeRadiusEntity implements Updatable {
    private Movement movement;
    private Area area;
    private boolean moving;

    /**
     * When the forceMove is true the entity is allowed to go move outside the area.
     */
    private boolean forceMove = false;
    private final Random rand = new Random();

    /**
     * Creates a default moving artificial entity, without a bounded area and with a linear
     * movement.
     */
    public ArtificialMovingEntity() {
        this(new GeoPoint(0,0), new LinearMovement(),
                new UnboundedArea());
    }

    /**
     * Creates a default moving artificial entity, by specifying a movement, an area and if it's
     * already moving.
     *  @param movement the type of movement the entity use
     * @param area the area the entity can move in
     */
    private ArtificialMovingEntity(GeoPoint location, Movement movement, Area area) {
        super(location);
        this.movement = movement;
        this.area = area;
        this.moving = true;
    }

    /**
     * Gets the area of the moving artificial entity.
     *
     * @return An area where the moving artificial entity can reside.
     */
    public Area getLocalArea() {
        return area;
    }

    /**
     * Gets the area of the moving artificial entity.
     *
     * @param area An area where the moving artificial entity can reside.
     */
    public void setLocalArea(Area area) {
        this.area = area;
    }

    /**
     * Sets the forceMove flag.
     *
     * @param forceMove A flag allowing the moving artificial entity to go outside the area.
     */
    public void setForceMove(boolean forceMove) {
        this.forceMove = forceMove;
    }

    /**
     * Sets the moving flag.
     *
     * @param moving A flag allowing the moving artificial entity to move.
     */
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    /**
     * Change the orientation and bounces against the bounds of the area.
     */
    private void bounce() {
        movement.setOrientation(rand.nextFloat() * 2 * (float) (Math.PI));
    }

    /**
     * Gets the movement of the moving artificial entity.
     *
     * @return A movement which defines the next position in the 2D plane.
     */
    public Movement getMovement() {
        return movement;
    }

    /**
     * Gets the movement of the moving artificial entity.
     *
     * @param movement A movement which defines the next position in the 2D plane.
     */
    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    /**
     * Goes to the next position based on the movement of the entity.
     */
    private void move() {
        GeoPoint position = movement.nextPosition(getLocation());
        if ((area.isInside(position) && moving) || forceMove) {
            super.setLocation(position);
        } else {
            bounce();
        }
    }

    /**
     * Checks if the entity is moving.
     * @return True if and only if the entity is moving (i.e. moving flag is true).
     */
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void update() {
        if (moving) {
            move();
        }
    }
}