var mysql = require('mysql');
var fs = require('fs');
var connect = require('connect');
var ejs = require('ejs');
var express = require('express');
var html = require('http');
var bodyParser = require('body-parser');
var multer = require('multer');

var client = mysql.createConnection({user:'root', password:'1212', database:'user'});

client.connect(function(err){
 if(err){
  console.error(err);
  throw err;
 }
});

var app = express();
app.listen(9000, function(){
  console.log('9000 start');
});
var urlencodedParser = bodyParser.urlencoded({ extended : false});

app.use(multer({dest:'./uploads/'}));

app.post('/signup', urlencodedParser, function(req, res){
 console.log("signup");
 if(!req.body) return res.sendStatus(400);
 client.query("SELECT * FROM user WHERE id = ?",[req.body.id],function(err,result){
 if(err){
console.error(err);
throw err;
}
 if(result.length!=0){
	res.send('no');
}else{
	client.query('INSERT INTO user (id,pwd) VALUES (?,?)',[req.body.id, req.body.pwd]);
	res.send('welcome, ' + req.body.id);
}
});
});

app.post('/login',urlencodedParser, function(req,res){
 console.log("login");
 if(!req.body) return res.sendStatus(400);
 client.query("SELECT * FROM user WHERE id = ? and pwd=?",[req.body.id, req.body.pwd],function(err,result){
 if(err){
  console.error(err);
  throw err;
}
 if(result.length==0) res.send('nothing');
 else res.send(result);
});
});

app.post('/upload',function(req,res){
	console.log(req.files);
	fs.readFile(req.files.image.path, function(err,data){
		var dirname = "./disk/";
		fs.writeFile(dirname+req.files.image.originalname, data, function(err){
		if(err){
			res.send('error');
		}
		else
			res.send('good');

		});
	});
});
