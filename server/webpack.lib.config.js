const path = require('path');
const ConcatPlugin = require('webpack-concat-plugin');

const dir = './server/js/lib/';

module.exports = {
    entry: dir+'md5.min.js',
    output: {
        filename: 'lib-build-side-effect.js',
        path: path.resolve(__dirname, 'ROOT/js')
    },
    plugins: [
        new ConcatPlugin({
            filesToConcat: [
                dir+'jquery.min.js',
                dir+'jquery.mobile.min.js',
                dir+'browser.js',
                dir+'md5.min.js'],
            fileName: 'lib.js'
        })
    ]
};
