package org.fusesource.hawtbuf.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.fusesource.hawtbuf.proto.CodedInputStream;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for skipping unknown fields.
 */
public class SkipUnknownTest
{
  @Test
  public void testCorruption() throws Exception {
    EnrichedMessage message = new EnrichedMessage();
    message.setNodeId("foo");
    message.setRepoId("bar");
    message.setPath("/a/b/c.txt");

    Enriched enriched = new Enriched();
    enriched.setType(Enriched.Type.FILE);
    enriched.setSize(1234);
    enriched.setMimeType("text/plain");
    message.setEnriched(enriched);

    System.out.println("Original >>\n" + message);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    message.writeFramed(output);

    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    SimpleMessage decoded = new SimpleMessage().mergeFramed(input);
    System.out.println("Decoded >>\n" + decoded);

    assertThat(decoded.getNodeId(), is(message.getNodeId()));
    assertThat(decoded.getRepoId(), is(message.getRepoId()));
    assertThat(decoded.getPath(), is(message.getPath()));
  }
}
