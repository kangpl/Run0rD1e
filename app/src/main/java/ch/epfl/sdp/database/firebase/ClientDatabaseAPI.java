package ch.epfl.sdp.database.firebase;

public interface ClientDatabaseAPI extends CommonDatabaseAPI {
    void sendHealthPoints(double healthPoints);
    void clearItemBoxes();
    void sendAoeRadius(double aoeRadius);
}
