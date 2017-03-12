package org.red5.omvideo;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.json.JSONException;
import org.json.JSONObject;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import java.io.File;
import java.io.FileInputStream;
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
        Movie movie = MovieCreator.build(VIDEO_FILE_PATH);

        FileChannel fc = new FileOutputStream(new File("/home/dmitry/code/om-video/new_small.mp4")).getChannel();
        Container mp4File = new FragmentedMp4Builder().build(movie);

        mp4File.writeContainer(fc);

        fc.close();

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
