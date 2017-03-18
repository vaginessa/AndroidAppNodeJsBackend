var express = require('express');
var router = express.Router();
var db = require('../queries');

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});


router.get('/api/photos', db.getPhotosToLike);
//router.get('/api/puppies/:id', db.getSinglePuppy);
router.post('/api/photo', db.addPhotoToLike);
router.post('/api/user', db.addUser);
//router.put('/api/puppies/:id', db.updatePuppy);
//router.delete('/api/puppies/:id', db.removePuppy);
router.get('/api/photo/:id', db.updatePhotoLikeStatus);

  
  
module.exports = router;

