const path = require('path');
const entry = require('webpack-glob-entry');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');


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
        new UglifyJsPlugin({
            uglifyOptions: {
                ecma: 6
            }
        })
    ],
    externals: ['jquery'],
    devtool: "source-map"
};

const appDef = Object.assign({
    entry: entry('./server/js/app/*.ts'),
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'ROOT/js')
    }
}, commonDef);

module.exports = [appDef];
