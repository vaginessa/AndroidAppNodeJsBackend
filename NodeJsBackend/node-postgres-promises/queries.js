var promise = require('bluebird');

var options = {
    // Initialization Options
    promiseLib: promise
};

var pgp = require('pg-promise')(options);
var connectionString = 'postgres://postgres:12qwaszx@localhost:5432/VKontacts';
var db = pgp(connectionString);

// add query functions
function getPhotosToLike(req, res, next) {
   /* db.any('select * from public.like_photos lp,public.user_likes ul where lp.usr_id != $1 and lp.id = ul.id and  lp.usr_id  != ul.usr_id  ', req.params.usr_id)
        .then(function (data) {
            res.status(200)
                .json({
                    status: 'success',
                    data: data,
                    message: 'photos to like'
                });
        })
        .catch(function (err) {
            return next(err);
        });
        */
         db.any(' SELECT * FROM public.like_photos lp WHERE lp.usr_id != $1 and (SELECT count(*) FROM public.user_likes ul WHERE ul.like_photos_id = lp.id and ul.usr_id = $1) = 0 ',parseInt(req.params.usr_id))
        .then(function (data) {
            res.status(200)
                .json({
                    status: 'success',
                    data: data,
                    message: 'photos to like'
                });
        })
        .catch(function (err) {
            return next(err);
        });
       
}

/*
function getSinglePuppy(req, res, next) {
var pupID = parseInt(req.params.id);
db.one('select * from pups where id = $1', pupID)
.then(function (data) {
res.status(200)
.json({
status: 'success',
data: data,
message: 'Retrieved ONE puppy'
});
})
.catch(function (err) {
return next(err);
});
}
*/

function addPhotoToLike(req, res, next) {
    // console.log(req);
    //console.log(req.header);
    console.log(req.body);
    db.none('insert into like_photos(id, url,usr_id,likes_left)' +
        'values(${id}, ${url},${usr_id},${likes})',
        req.body)
        .then(function () {
            res.status(200)
                .json({
                    status: 'success',
                    message: 'added photo'
                });
        })
        .catch(function (err) {
            return next(err);
        });
}

function addUser(req, res, next) {
    req.body.id = parseInt(req.body.id);
    console.log(req.body);
    // db.none('insert into test(id, likes)' + 'values(12,12)',
    //req.body)
    var id = req.body.id;
    db.none("insert into users(id, likes) values($1,$2)",
        [parseInt(id), 0])
        .then(function () {
            res.status(200)
                .json({
                    status: 'success',
                    message: 'Created new user'
                });
        })
        .catch(function (err) {
            return next(err);
        });
}


function updatePhotoLikeStatus(req, res, next) {
     req.body.id = parseInt(req.params.id);
     req.body.usr_id = parseInt(req.params.usr_id)
    db.one('select likes_left from like_photos where id=$1', id)
        .then(function (data) {
            db.none('update like_photos set likes_left=$1 where id=$2',
                [data.likes_left - 1, parseInt(req.params.id)])
                .then(function () {
                    res.status(200)
                        .json({
                            status: 'success',
                            message: ('Updated photo likes left $1', data.likes_left - 1)
                        });
                })
                .catch(function (err) {
                    return next(err);
                });
             db.none('insert into user_likes(user_id, like_photo_id)' +
                'values(${usr_id},${id})',
                req.body)
                .then(function () {
                    res.status(200)
                        .json({
                            status: 'success',
                            message: ('Inserted new like record')
                        });
                })
                .catch(function (err) {
                    return next(err);
                });
        })
}


/*
function removePuppy(req, res, next) {
var pupID = parseInt(req.params.id);
db.result('delete from pups where id = $1', pupID)
.then(function (result) {
res.status(200)
.json({
status: 'success',
message: `Removed ${result.rowCount} puppy`
});
})
.catch(function (err) {
return next(err);
});
}

*/



module.exports = {
    getPhotosToLike: getPhotosToLike,
    addPhotoToLike: addPhotoToLike,
    addUser: addUser,
    updatePhotoLikeStatus: updatePhotoLikeStatus,

};