const connection = new WebSocket('ws://localhost:5080/omvideo/ws');
const videoTag = document.querySelector("#video-tag");
const mimeCodec = 'video/mp4; codecs="avc1.64001E, mp4a.40.2"';

let mediaSource = null;
let sourceBuffer = null;

function getMediaSource() {
	let mediaSource = null;
	if ('MediaSource' in window && MediaSource.isTypeSupported(mimeCodec)) {
		mediaSource = new MediaSource;
        videoTag.src = URL.createObjectURL(mediaSource);
        mediaSource.addEventListener('sourceopen', () => {
        	sourceBuffer = mediaSource.addSourceBuffer(mimeCodec);
        });
    } else {
    	console.error('Unsupported MIME type or codec: ', mimeCodec);
    }

    return mediaSource;
}

function startVideo() {
	mediaSource = getMediaSource();
	videoTag.play();
}

connection.onopen = function () {
  console.log("Connected");
  startVideo();
}

connection.onmessage = function (message) {
  console.log("Got message", message.data);
  sourceBuffer.appendBuffer(new Uint8Array(message));
  videoTag.play();
};

connection.onerror = function (err) {
  console.log("Got error", err);
};
