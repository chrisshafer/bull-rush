var gulp = require('gulp');
var plugins = require('gulp-load-plugins')({
    pattern: ["gulp-*", "del"],
    scope: ["dependencies", "devDependencies"]
});

var workingDir = "./resources";
var scalaJSDir = "./js/target/scala-2.11";
var developmentDir = "./.temp/bull-rush";
var bowerFilter = plugins.filter(['**/*.{min.js,min.css,css,js}']);

gulp.task('preview', function() {
    return gulp.src(".temp/bull-rush/")
        .pipe(plugins.serverLivereload({
            livereload: true,
            directoryListing: false,
            basePath: "/",
            open: true
        }));
});

gulp.task('watch', function() {
    gulp.watch([scalaJSDir + "/**/*.js", scalaJSDir + "/**/*.map"], ["scalajs"]);
    gulp.watch(workingDir + "/**/*.js", ["js"]);
    gulp.watch(workingDir + "/scss/**/*.scss", ["css"]);
    gulp.watch(["**/*.html", "!node_modules/**", "!bower_components/**", "!dev/**", "!dist/**"], ["html"]);
    gulp.watch("**/*.{woff,ttf,woff2}", ["font"]);
    gulp.watch(workingDir + "/img/**/*.{png,jpeg,jpg,gif,svg}", ["img"]);
});

gulp.task("html", function() {
    return gulp.src(["**/*.html", "!node_modules/**", "!bower_components/**", "!dev/**", "!dist/**"])
        .pipe(gulp.dest(developmentDir));
});

gulp.task("css", function() {
    return gulp.src(workingDir + '/scss/**/*.scss')
        .pipe(plugins.sass().on('error', plugins.sass.logError))
        .pipe(gulp.dest(developmentDir + "/resources/css"));
});

gulp.task("js", function() {
    return gulp.src([workingDir + '/js/**/*.js'])
        .pipe(gulp.dest(developmentDir + "/resources/js"));
});

gulp.task("scalajs", function() {
    return gulp.src([scalaJSDir + '/**/*.js', scalaJSDir + '/**/*.map'])
        .pipe(gulp.dest(developmentDir + "/resources/js"));
});

gulp.task("font", function() {
    return gulp.src(['**/*.woff','**/*.ttf','**/*.woff2'])
        .pipe(plugins.rename({dirname: ''}))
        .pipe(gulp.dest(developmentDir + "/resources/fonts"));
});

gulp.task("copy", function() {
    return gulp.src(workingDir + "/lib/**/*")
        .pipe(gulp.dest(developmentDir + "/resources/lib"));
});

gulp.task('bower', function() {
    return plugins.bower('bower_components')
        .pipe(bowerFilter)
        .pipe(plugins.rename({dirname: ''}))
        .pipe(gulp.dest(workingDir + '/lib'));
});


gulp.task('build', plugins.sequence("bower", ["copy", "css", "js", "scalajs", "html", "font"]));
gulp.task('develop', plugins.sequence("bower", ["copy", "css", "js", "scalajs", "html", "font"], "preview", "watch"));
gulp.task('default', ['build']);