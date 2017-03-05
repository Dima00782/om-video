const connection = new WebSocket('ws://localhost:5080/omvideo/ws');

const videoTag = document.querySelector("#video-tag");

connection.onopen = function () {
  console.log("Connected");
}

connection.onmessage = function (message) {
  console.log("Got message", message.data);

  // TODO
};

connection.onerror = function (err) {
  console.log("Got error", err);
};
