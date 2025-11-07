import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EventModelTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockCollection;
    @Mock
    private DocumentReference mockDocument;

    private EventModel eventModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockFirestore.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);

        // Inject mock Firestore
        eventModel = new EventModel("events") {
            {
                // Override Firestore initialization
                super.db = mockFirestore;
                super.eventsCollection = mockCollection;
            }
        };
    }

    @Test
    public void updateEvent_ThrowsException_WhenIdIsEmpty() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", "Updated Event");

        assertThrows(IllegalArgumentException.class,
                () -> eventModel.updateEvent("", updateData));
    }

    @Test
    public void deleteEvent_CallsDelete_WhenIdValid() {
        when(mockCollection.document("abc")).thenReturn(mockDocument);
        when(mockDocument.delete()).thenReturn(mock(Task.class));

        eventModel.deleteEvent("abc");

        verify(mockCollection).document("abc");
        verify(mockDocument).delete();
    }

    @Test
    public void getEvent_CallsGetDocument() {
        when(mockCollection.document("xyz")).thenReturn(mockDocument);
        when(mockDocument.get()).thenReturn(mock(Task.class));

        eventModel.getEvent("xyz");

        verify(mockDocument).get();
    }
}
