package org.red5.omvideo;

import org.json.JSONException;
import org.json.JSONObject;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import java.io.*;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dmitry Bezheckov
 */

@ServerEndpoint(value = "/ws")
public class SignalConnection {
	
	private static final Logger LOG = Red5LoggerFactory.getLogger(SignalConnection.class, "omvideo");

	private static final String VIDEO_FILE_PATH = "/home/dmitry/Downloads/Avengers2.mp4";

	private static final ArrayList<Session> clients = new ArrayList<>();
	
    @OnOpen
    public void onOpen(final Session session) throws IOException {
        LOG.info("User connected");
        ClassicMp4ContainerSource classicMp4ContainerSource = new ClassicMp4ContainerSource(
                new FileInputStream(new File(VIDEO_FILE_PATH))
        );
        List<StreamingTrack> streamingTracks = classicMp4ContainerSource.getTracks();
        FragmentedMp4Writer writer = new FragmentedMp4Writer(streamingTracks, new WritableByteChannel() {
            @Override
            public boolean isOpen() {
               return session.isOpen();
           }

            @Override
            public void close() throws IOException {
                session.close();
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int size = src.remaining();
                session.getBasicRemote().sendBinary(src);
                return size;
            }
        });

        LOG.info("Reading and writing started.");
        classicMp4ContainerSource.call();

        writer.close();
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
