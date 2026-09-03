package com.orion.echoes.lua.systems;

/** Maquina de estados pequena e segura para conversas lineares. */
public final class DialogSystem {
    private String speaker = "";
    private String[] lines = new String[0];
    private int index;
    private boolean open;
    private boolean finished;

    public void start(String speaker, String... lines) {
        if (open) return;
        this.speaker = speaker == null ? "" : speaker;
        this.lines = lines == null ? new String[0] : lines;
        index = 0;
        open = this.lines.length > 0;
        finished = false;
    }

    public void next() {
        if (!open) return;
        index++;
        if (index >= lines.length) {
            index = lines.length;
            open = false;
            finished = true;
        }
    }

    public boolean consumeFinished() {
        if (!finished) return false;
        finished = false;
        return true;
    }

    public boolean isOpen() { return open; }
    public String getSpeaker() { return speaker; }
    public int getIndex() { return index; }
    public int getLineCount() { return lines.length; }
    public String getLine() {
        return open && index >= 0 && index < lines.length ? lines[index] : "";
    }
}
