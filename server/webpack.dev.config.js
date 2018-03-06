const path = require('path');
const entry = require('webpack-glob-entry');
const watchTimePlugin = require('webpack-watch-time-plugin');

const commonDef = {
    resolve: {
        extensions: ['.ts']
    },
    module: {
        loaders: [
            {
                test: /\.ts$/,
                loader: 'ts-loader',
                options: {
                    onlyCompileBundledFiles: true
                }
            }
        ]
    },
    plugins: [
        watchTimePlugin,
    ],
    externals: ['jquery'],
    devtool: "source-map",
    watchOptions: {
        aggregateTimeout: 1000,
        poll: 2000
    }
};

const appDef = Object.assign({
    entry: entry('./server/js/app/*.ts'),
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'ROOT/js')
    }
}, commonDef);

const testDef = Object.assign({
    entry: entry('./server/js/app/test/*.ts'),
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'ROOT/js/test')
    }
}, commonDef);


module.exports = [appDef, testDef];
