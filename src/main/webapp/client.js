const mimeCodec = 'video/mp4; codecs="avc1.42E01E, mp4a.40.2"';
const videoTag = document.querySelector("#video-tag");
let mediaSource = null;
let sourceBuffer = null;

function getMediaSource() {
  if ('MediaSource' in window && MediaSource.isTypeSupported(mimeCodec)) {
    mediaSource = new MediaSource;
    videoTag.src = URL.createObjectURL(mediaSource);
    mediaSource.addEventListener('sourceopen', () => {
      sourceBuffer = mediaSource.addSourceBuffer(mimeCodec);
      videoTag.addEventListener('canplay', function () {
        videoTag.play();
      });
    });
  } else {
    console.error('Unsupported MIME type or codec: ', mimeCodec);
  }
  return mediaSource;
}

mediaSource = getMediaSource();
const connection = new WebSocket('ws://localhost:5080/omvideo/ws');
connection.binaryType = "arraybuffer";


connection.onopen = function () {
  console.log("Connected");
}

connection.onmessage = function (message) {
  console.log(message.data);
  sourceBuffer.appendBuffer(message.data);
  videoTag.play();
};

connection.onerror = function (err) {
  console.log("Got error", err);
};