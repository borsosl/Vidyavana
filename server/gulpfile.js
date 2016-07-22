"use strict";

var ENV = process.env.NODE_ENV || 'dev';
var DEV = ENV === "dev";
var PROD = ENV === "prod";
console.log(ENV + " mode");

var gulp = require('gulp');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var sourcemaps = require("gulp-sourcemaps");
var browserify = require('browserify');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var es = require('event-stream');
var WATCHES = [];


gulp.task('lib', function() {
    var paths = [
        'js/lib/jquery.min.js',
        'js/lib/jquery*',
        'js/lib/*',
        'js/lib/**/*'
    ];

    return gulp.src(paths)
        .pipe(concat("lib.js"))
        .pipe(gulp.dest('ROOT/js/'));
});


gulp.task('js', function() {
    var paths = ['js/app/*.js', 'js/app/**/*.js'];
    WATCHES.push({
        path: paths,
        taskName: ['js']
    });
    var bundleRoots = [
        'js/app/main.js',
        'js/app/login.js',
        'js/app/admin.js',
        'js/app/server-monitor.js',
        'js/app/test/highlight.js'
    ];
    var tasks = bundleRoots.map(function(entry) {
        var task = browserify({
            entries: [entry],
            debug: true
        })
            .bundle()
            .pipe(source(entry.substring(7)))
            .pipe(buffer());

        if(DEV) {
            task = task
                .pipe(sourcemaps.init({loadMaps: true}))
                .pipe(sourcemaps.write('./'));
        }

        if(PROD) {
            task = task.pipe(uglify({
                compress: {
                    drop_debugger: false
                }
            }));
        }

        if(DEV) {
            task = task.pipe(sourcemaps.write('./'));
        }
        task = task.pipe(gulp.dest('ROOT/js/'));
        return task;
    });
    return es.merge.apply(null, tasks);
});


gulp.task('watch', function() {
    if(DEV) {
        WATCHES.forEach(function(watchable) {
            gulp.watch(watchable.path, watchable.taskName)
        });
    }
});
