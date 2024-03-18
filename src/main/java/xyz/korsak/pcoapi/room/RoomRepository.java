package xyz.korsak.pcoapi.room;

public interface RoomRepository {
    void create(Room room);

    Room findById(String id);

    Room findByToken(String token);

    void delete(String id);
}
