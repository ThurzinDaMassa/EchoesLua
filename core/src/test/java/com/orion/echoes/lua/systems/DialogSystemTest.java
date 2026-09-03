package com.orion.echoes.lua.systems;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DialogSystemTest {
    @Test
    void advancesOneLineAndEmitsCompletionOnlyOnce() {
        DialogSystem dialog = new DialogSystem();
        dialog.start("VEGA", "linha um", "linha dois", "linha tres");
        assertEquals("linha um", dialog.getLine());
        dialog.next();
        assertEquals("linha dois", dialog.getLine());
        dialog.next();
        assertEquals("linha tres", dialog.getLine());
        dialog.next();
        assertFalse(dialog.isOpen());
        assertTrue(dialog.consumeFinished());
        assertFalse(dialog.consumeFinished());
    }
}
