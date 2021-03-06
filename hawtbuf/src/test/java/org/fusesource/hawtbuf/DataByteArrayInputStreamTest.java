/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtbuf;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DataByteArrayInputStreamTest {

    /**
     * https://issues.apache.org/activemq/browse/AMQ-1911
     */
    @Test()
    public void testNonAscii() throws Exception {
        doMarshallUnMarshallValidation("meißen");
        
        int test = 0; // int to get Supplementary chars
        while(Character.isDefined(test)) {
            doMarshallUnMarshallValidation(String.valueOf((char)test));
            test++;
        }
        
        int massiveThreeByteCharValue = 0x0FFF;
        doMarshallUnMarshallValidation(String.valueOf((char)massiveThreeByteCharValue));
    }
    
    void doMarshallUnMarshallValidation(String value) throws Exception {        
        DataByteArrayOutputStream out = new DataByteArrayOutputStream();
        out.writeBoolean(true);
        out.writeUTF(value);
        out.close();
        
        DataByteArrayInputStream in = new DataByteArrayInputStream(out.getData());
        in.readBoolean();
        String readBack = in.readUTF();
        assertEquals(value, readBack);
    }

    @Test()
    public void testUnmarshallEmptyBuffer() throws Exception {
        DataByteArrayInputStream in = new DataByteArrayInputStream(new byte[]{});
        Buffer readBack = in.readBuffer(0);
        assertNotNull(readBack);
        assertEquals(0, readBack.length);
    }
}
