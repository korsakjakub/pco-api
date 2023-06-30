package xyz.korsak.pcoapi.room;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoomTest {
    @Test
    void testGettersAndSetters() {
        Room room = new Room();

        room.setId("123");
        room.setName("Test Room");

        assertEquals("123", room.getId());
        assertEquals("Test Room", room.getName());
    }
}
