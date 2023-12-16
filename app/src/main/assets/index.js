const express = require('express'); //requires express module
const socket = require('socket.io'); //requires socket.io module
const fs = require('fs');
const app = express();
    var PORT = process.env.PORT || 3000;
const server = app.listen(PORT); //tells to host server on localhost:3000
var request = require('request-promise');


//Playing variables:
app.use(express.static('public')); //show static files in 'public' directory
console.log('Server is running');
const io = socket(server);

var count = 0;

async function arraysum(count) {

    // This variable contains the data
    // you want to send
    var data = {
        array:count
    }

    var options = {
        method: 'POST',

        // http:flaskserverurl:port/route
        uri: 'http://127.0.0.1:5000/',
        body: data,

        // Automatically stringifies
        // the body to JSON
        json: true
    };

    var sendrequest = await request(options)

        // The parsedBody contains the data
        // sent back from the Flask server
        .then(function (parsedBody) {
            console.log(parsedBody);

            // You can do something with
            // returned data
            let result;
            result = parsedBody['result'];
            console.log("Sum of Array from Python: ", result);
        })
        .catch(function (err) {
            console.log(err);
        });

}

//Socket.io Connection------------------
io.on('connection', (socket) => {

    console.log("New socket connection: " + socket.id)

    socket.on('sus', () => {

       arraysum("sus");



    })
    socket.on('jos', () => {

        arraysum("jos");



    })
    socket.on('stanga', () => {
        arraysum("stanga");


    })
    socket.on('dreapta', () => {

        arraysum("dreapta");


    })
})