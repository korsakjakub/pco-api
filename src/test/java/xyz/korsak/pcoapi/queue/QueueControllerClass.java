package xyz.korsak.pcoapi.queue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class QueueControllerTest {

    @Test
    void getPlayersInQueue_ReturnsPlayersInQueue_WhenValidQueueId() {
        // Arrange
        String queueId = "123";
        GetPlayersResponse expectedPlayers = new GetPlayersResponse(new ArrayList<>());

        QueueService queueService = mock(QueueService.class);
        when(queueService.getPlayersInQueue(eq(queueId))).thenReturn(expectedPlayers);

        QueueController queueController = new QueueController(queueService);

        // Act
        ResponseEntity<GetPlayersResponse> response = queueController.getPlayersInQueue(queueId);

        // Assert
        verify(queueService, times(1)).getPlayersInQueue(eq(queueId));
        verifyNoMoreInteractions(queueService);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPlayers, response.getBody());
    }
}
