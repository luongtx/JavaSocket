package com.client.playground;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sherif
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
    The LoopingByteInputStream is a ByteArrayInputStream that
    loops indefinitely. The looping stops when the close() method
    is called.
    <p>Possible ideas to extend this class:<ul>
    <li>Add an option to only loop a certain number of times.
    </ul>
*/
public class LoopingByteInputStream extends ByteArrayInputStream {

    private boolean closed;

    /**
        Creates a new LoopingByteInputStream with the specified
        byte array. The array is not copied.
     * @param buffer
    */
    public LoopingByteInputStream(byte[] buffer) {
        super(buffer);
        closed = false;
    }
    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
    }

}

