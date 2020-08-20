const express = require('express');
const bodyParser = require('body-parser');


const socketio = require('socket.io')
var app = express();

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());


var server = app.listen(3000,()=>{
    console.log('Server is running on port number 3000')
})


var roomInfo = {};
var io = socketio.listen(server)


io.on('connection',function(socket) {
    console.log(`Connection : SocketId = ${socket.id}`)

    var userName = '';

    
    socket.on('subscribe', function(data) {
        console.log('subscribe trigged')
        const room_data = JSON.parse(data)
        userName = room_data.userName;
        const roomName = room_data.roomName;
        
        if (roomInfo[roomName] != undefined) {
            roomInfo[roomName] = [].concat(roomInfo[roomName][0], userName)
        } else {
            roomInfo[roomName] = [userName]
        }

        socket.join(`${roomName}`)
        console.log(`Username : ${userName} joined Room Name : ${roomName}`)
        console.log(`Count: : ${io.sockets.adapter.rooms[roomName].length}`)
        
        io.to(`${roomName}`).emit('newUserToRoom', JSON.stringify(roomInfo[roomName]));
        io.to(`${roomName}`).emit('userCount', io.sockets.adapter.rooms[roomName].length)
    })

    socket.on('unsubscribe',function(data) {
        console.log('unsubscribe trigged')
        const room_data = JSON.parse(data)
        const userName = room_data.userName;
        const roomName = room_data.roomName;

        roomInfo[roomName] = undefined
    
        console.log(`Username : ${userName} leaved Room Name : ${roomName}`)
        socket.broadcast.to(`${roomName}`).emit('userLeftChatRoom',userName)
        socket.leave(`${roomName}`)
    })

    socket.on('newMove',function(data) {
        const statusData = JSON.parse(data)
        const move = statusData.move
        const roomName = statusData.roomName

        console.log("newMove")
    
        socket.broadcast.to(`${roomName}`).emit('updateMove',move) 
    })

    socket.on('newTick',function(data) {
        const tickData = JSON.parse(data)
        const count = tickData.count
        const roomName = tickData.roomName

        socket.broadcast.to(`${roomName}`).emit('updateTick', parseInt(count))
    })

    socket.on('changeUserTurn',function(data) {
        console.log("userTurn")
        socket.broadcast.to(`${JSON.parse(data).roomName}`).emit('updateUserTurn', 1) 
    })

    socket.on('onWin',function(data) {
        console.log("win")
        socket.broadcast.to(`${JSON.parse(data).roomName}`).emit('announceWin', 1) 
    })

    socket.on('onTie',function(data) {
        console.log("tie")
        socket.broadcast.to(`${JSON.parse(data).roomName}`).emit('announceTie', 1) 
    })


    socket.on('disconnect', function () {
        console.log("One of sockets disconnected from our server.")
    });
})

module.exports = server;