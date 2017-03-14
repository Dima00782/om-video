package org.red5.omvideo;

import org.json.JSONException;
import org.json.JSONObject;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * 
 * @author Dmitry Bezheckov
 */

@ServerEndpoint(value = "/ws")
public class SignalConnection {
	
	private static final Logger LOG = Red5LoggerFactory.getLogger(SignalConnection.class, "omvideo");

	private static final String VIDEO_FILE_PATH = "/home/dmitry/code/om-video/small.mp4";
	
    @OnOpen
    public void onOpen(Session session) throws IOException, URISyntaxException {
        ClassicMp4ContainerSource classicMp4ContainerSource = new ClassicMp4ContainerSource(
                new URI("http://video.blendertestbuilds.de/download.php?file=download.blender.org/peach/trailer_480p.mov").toURL().openStream()
        );
        List<StreamingTrack> streamingTracks = classicMp4ContainerSource.getTracks();
        File f = new File("/home/dmitry/code/om-video/new_small.mp4");
        FragmentedMp4Writer writer = new FragmentedMp4Writer(streamingTracks, new FileOutputStream(f).getChannel());

        LOG.info("Reading and writing started.");
        classicMp4ContainerSource.call();
        writer.close();
        LOG.info(f.getAbsolutePath());

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
