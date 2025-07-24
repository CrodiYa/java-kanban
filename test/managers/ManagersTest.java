package managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    public void shouldBeNotNullWhenDefault(){
        assertNotNull(Managers.getDefault());
    }

    @Test
    public void shouldBeNotNullWhenHistoryDefault(){
        assertNotNull(Managers.getDefaultHistory());
    }
}