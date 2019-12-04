package com.github.nitram509.jmacaroons;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class FuzzTests {

    @Fuzz
    public void fuzz (String data) {
        MacaroonsDeSerializer.deserialize(data);
    }
}
