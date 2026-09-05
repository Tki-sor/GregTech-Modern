package brachy.modularui;

import lombok.Getter;

class LombokProbe {
    @Getter private final int value = 1;
    int read() { return getValue(); }
}
