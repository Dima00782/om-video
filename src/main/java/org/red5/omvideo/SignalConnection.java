package org.red5.omvideo;

import org.json.JSONException;
import org.json.JSONObject;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.mp4parser.tools.IsoTypeReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Dmitry Bezheckov
 */

@ServerEndpoint(value = "/ws")
public class SignalConnection {
	
	private static final Logger LOG = Red5LoggerFactory.getLogger(SignalConnection.class, "omvideo");

	private static final String VIDEO_FILE_PATH = "/home/dmitry/code/om-video/small.mp4";

	List<String> containers = Arrays.asList(
		"moov",
        "trak",
        "mdia",
        "minf",
        "udta",
        "stbl"
    );

	private void print(FileChannel fc, int level, long start, long end) throws IOException {
        fc.position(start);
        if(end <= 0) {
            end = start + fc.size();
            System.out.println("Setting END to " + end);
        }
        while (end - fc.position() > 8) {
            long begin = fc.position();
            ByteBuffer bb = ByteBuffer.allocate(8);
            fc.read(bb);
            bb.rewind();
            long size = IsoTypeReader.readUInt32(bb);
            String type = IsoTypeReader.read4cc(bb);
            long fin = begin + size;
            // indent by the required number of spaces
            for (int i = 0; i < level; ++i) {
                System.out.print(" ");
            }

            System.out.println(type + "@" + (begin) + " size: " + size);
            if (containers.contains(type)) {
                print(fc, level + 1, begin + 8, fin);
                if (fc.position() != fin) {
                    System.out.println("End of container contents at " + fc.position());
                    System.out.println("  FIN = " + fin);
                }
            }

            fc.position(fin);
        }
    }
	
    @OnOpen
    public void onOpen(Session session) throws IOException {
    	FileInputStream fis = new FileInputStream(new File(VIDEO_FILE_PATH));
    	print(fis.getChannel(), 0, 0, 0);

        LOG.info("User connected");
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
    	LOG.info("Got message: " + message);
    }

	@OnError
    public void onError(Throwable t) {
    	LOG.error("Error " + t.toString(), t);
    }

    @OnClose
    public void onClose() {
    	LOG.info("Close");
    }
}
