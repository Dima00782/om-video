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

let buffer = [];

function getTotalLength(array) {
  let size = array.reduce((acc, curr) => acc + curr.byteLength, 0);
  return size;
}

function concatArrBuf(buffer1, buffer2) {
  var tmp = new Uint8Array(buffer1.byteLength + buffer2.byteLength);
  tmp.set(new Uint8Array(buffer1), 0);
  tmp.set(new Uint8Array(buffer2), buffer1.byteLength);
  return tmp.buffer;
};

connection.onmessage = function (message) {
  if (getTotalLength(buffer) >= 300000) {
    let bigBuffer = buffer.reduce((acc, curr) => concatArrBuf(acc, curr), new ArrayBuffer());
    sourceBuffer.appendBuffer(bigBuffer);
    buffer = [];
    videoTag.play();
    console.log("YEPPP");
  }
  buffer.push(message.data);
};

connection.onerror = function (err) {
  console.log("Got error", err);
};