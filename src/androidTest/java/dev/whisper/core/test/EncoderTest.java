/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.whisper.core.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.googlecode.javacpp.Loader;

import dev.whisper.core.audio.encoder.CELT7Encoder;
import dev.whisper.core.audio.encoder.IEncoder;
import dev.whisper.core.audio.encoder.OpusEncoder;
import dev.whisper.core.audio.javacpp.Opus;
import dev.whisper.core.exception.NativeAudioException;
import dev.whisper.core.net.PacketBuffer;

/**
 * This class tests the Opus and CELT encoders with blank PCM data.
 * The bitrate is set to 40000bps. TODO: add test for varying bitrates.
 * If any of these methods throw a NativeAudioException, then the test will fail.
 * Created by andrew on 13/10/13.
 */
@RunWith(AndroidJUnit4.class)
public class EncoderTest {
    private static final int MAX_BUFFER_SIZE = 960;
    private static final int SAMPLE_RATE = 48000;
    private static final int BITRATE = 40000;
    private static final int FRAME_SIZE = 480;
    private static final int FRAMES_PER_PACKET = 4;

    static {
        Loader.load(Opus.class);
    }

    @Test
    public void testOpusEncode() throws NativeAudioException {
        IEncoder encoder = new OpusEncoder(SAMPLE_RATE, 1, FRAME_SIZE, FRAMES_PER_PACKET, BITRATE, MAX_BUFFER_SIZE);
        testEncoder(encoder);
        encoder.destroy();
    }

    @Test
    public void testCELT7Encode() throws NativeAudioException {
        CELT7Encoder encoder = new CELT7Encoder(SAMPLE_RATE, FRAME_SIZE, 1, FRAMES_PER_PACKET,
                BITRATE, MAX_BUFFER_SIZE);
        testEncoder(encoder);
        encoder.destroy();
    }

    @Test
    public void testEncoder(IEncoder encoder) throws NativeAudioException {
        assertFalse(encoder.isReady());
        assertEquals(0, encoder.getBufferedFrames());

        //
        final short[] dummyFrame = new short[FRAME_SIZE];
        for (int i = 0; i < FRAMES_PER_PACKET; i++) {
            assertFalse(encoder.isReady());
            encoder.encode(dummyFrame, FRAME_SIZE);
        }
        assertTrue(encoder.isReady());
        assertEquals(FRAMES_PER_PACKET, encoder.getBufferedFrames());

        // Flushing
        PacketBuffer buffer = PacketBuffer.allocate(MAX_BUFFER_SIZE);
        encoder.getEncodedData(buffer);
        assertFalse(encoder.isReady());
        assertEquals(0, encoder.getBufferedFrames());

        // Termination (for frames per packet > 1)
        encoder.encode(dummyFrame, FRAME_SIZE);
        assertFalse(encoder.isReady());
        encoder.terminate();
        assertTrue(encoder.isReady());

    }
}
